/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.sslr.internal.vm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sonar.sslr.api.TokenType;
import com.sonar.sslr.api.Trivia.TriviaKind;
import org.sonar.sslr.grammar.GrammarException;
import org.sonar.sslr.grammar.GrammarRuleBuilder;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.internal.matchers.EndOfInputMatcher;
import org.sonar.sslr.internal.matchers.FirstOfMatcher;
import org.sonar.sslr.internal.matchers.Matcher;
import org.sonar.sslr.internal.matchers.NothingMatcher;
import org.sonar.sslr.internal.matchers.OneOrMoreMatcher;
import org.sonar.sslr.internal.matchers.OptionalMatcher;
import org.sonar.sslr.internal.matchers.PatternMatcher;
import org.sonar.sslr.internal.matchers.SequenceMatcher;
import org.sonar.sslr.internal.matchers.StringMatcher;
import org.sonar.sslr.internal.matchers.TestMatcher;
import org.sonar.sslr.internal.matchers.TestNotMatcher;
import org.sonar.sslr.internal.matchers.TokenMatcher;
import org.sonar.sslr.internal.matchers.TriviaMatcher;
import org.sonar.sslr.internal.matchers.ZeroOrMoreMatcher;
import org.sonar.sslr.internal.vm.Instr.Opcode;

import java.util.List;
import java.util.Map;

public class VmGrammarBuilder {

  public static VmGrammarBuilder create() {
    return new VmGrammarBuilder();
  }

  private final Map<GrammarRuleKey, RuleExpression> rules = Maps.newHashMap();

  public GrammarRuleBuilder rule(GrammarRuleKey ruleKey) {
    RuleExpression rule = rules.get(ruleKey);
    if (rule == null) {
      rule = new RuleExpression(ruleKey);
      rules.put(ruleKey, rule);
    }
    return rule;
  }

  public CompiledGrammar build() {
    // Compile all rules
    Map<GrammarRuleKey, Integer> offsets = Maps.newHashMap();
    Instr[][] instr = new Instr[rules.size()][];
    int offset = 0;
    int i = 0;
    for (RuleExpression rule : rules.values()) {
      offsets.put(rule.getRuleKey(), offset);
      instr[i] = rule.compile();
      offset += instr[i].length + 1;
      i++;
    }
    Instr[] result = new Instr[offset];
    offset = 0;
    for (i = 0; i < rules.size(); i++) {
      System.arraycopy(instr[i], 0, result, offset, instr[i].length);
      offset += instr[i].length + 1;
      result[offset - 1] = Instr.ret();
    }

    // Link
    for (i = 0; i < result.length; i++) {
      Instr instruction = result[i];
      if (result[i].getOpcode() == Opcode.OPEN_CALL) {
        RuleRefExpression matcher = (RuleRefExpression) instruction.getMatcher();
        GrammarRuleKey ruleKey = matcher.getRuleKey();
        if (!offsets.containsKey(ruleKey)) {
          throw new GrammarException("The rule " + ruleKey + " has been used somewhere in grammar, but not defined.");
        }
        offset = offsets.get(ruleKey);
        result[i] = Instr.call(offset - i, rules.get(ruleKey));
      }
    }

    return new CompiledGrammar(result, rules, offsets);
  }

  public Object sequence(Object e1, Object e2) {
    return new SequenceMatcher(convertToExpression(e1), convertToExpression(e2));
  }

  public Object sequence(Object e1, Object e2, Object... rest) {
    return new SequenceMatcher(convertToExpressions(Lists.asList(e1, e2, rest)));
  }

  public Object firstOf(Object e1, Object e2) {
    return new FirstOfMatcher(convertToExpression(e1), convertToExpression(e2));
  }

  public Object firstOf(Object e1, Object e2, Object... rest) {
    return new FirstOfMatcher(convertToExpressions(Lists.asList(e1, e2, rest)));
  }

  public Object optional(Object e) {
    return new OptionalMatcher(convertToExpression(e));
  }

  public Object optional(Object e1, Object... rest) {
    return new OptionalMatcher(new SequenceMatcher(convertToExpressions(Lists.asList(e1, rest))));
  }

  public Object oneOrMore(Object e) {
    return new OneOrMoreMatcher(convertToExpression(e));
  }

  public Object oneOrMore(Object e1, Object... rest) {
    return new OneOrMoreMatcher(new SequenceMatcher(convertToExpressions(Lists.asList(e1, rest))));
  }

  public Object zeroOrMore(Object e) {
    return new ZeroOrMoreMatcher(convertToExpression(e));
  }

  public Object zeroOrMore(Object e1, Object... rest) {
    return new ZeroOrMoreMatcher(new SequenceMatcher(convertToExpressions(Lists.asList(e1, rest))));
  }

  public Object next(Object e) {
    return new TestMatcher(convertToExpression(e));
  }

  public Object next(Object e1, Object... rest) {
    return new TestMatcher(new SequenceMatcher(convertToExpressions(Lists.asList(e1, rest))));
  }

  public Object nextNot(Object e) {
    return new TestNotMatcher(convertToExpression(e));
  }

  public Object nextNot(Object e1, Object... rest) {
    return new TestNotMatcher(new SequenceMatcher(convertToExpressions(Lists.asList(e1, rest))));
  }

  public Object nothing() {
    return new NothingMatcher();
  }

  public Object regexp(String regexp) {
    return new PatternMatcher(regexp);
  }

  public Object endOfInput() {
    return new EndOfInputMatcher();
  }

  public Object token(TokenType tokenType, Object e) {
    return new TokenMatcher(tokenType, convertToExpression(e));
  }

  public Object commentTrivia(Object e) {
    return new TriviaMatcher(TriviaKind.COMMENT, convertToExpression(e));
  }

  public Object skippedTrivia(Object e) {
    return new TriviaMatcher(TriviaKind.SKIPPED_TEXT, convertToExpression(e));
  }

  static Matcher convertToExpression(Object e) {
    if (e instanceof CompilableMatcher) {
      return (Matcher) e;
    } else if (e instanceof String) {
      return new StringMatcher((String) e);
    } else if (e instanceof Character) {
      return new StringMatcher(((Character) e).toString());
    } else if (e instanceof GrammarRuleKey) {
      return new RuleRefExpression((GrammarRuleKey) e);
    } else {
      throw new IllegalArgumentException("Incorrect type of parsing expression: " + e.getClass().toString());
    }
  }

  static Matcher[] convertToExpressions(List<Object> expressions) {
    Matcher[] result = new Matcher[expressions.size()];
    for (int i = 0; i < expressions.size(); i++) {
      result[i] = convertToExpression(expressions.get(i));
    }
    return result;
  }

  public void setRootRule(GrammarRuleKey ruleKey) {
    // TODO
    throw new UnsupportedOperationException();
  }

}
