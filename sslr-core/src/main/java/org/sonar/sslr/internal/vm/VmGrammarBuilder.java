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
import org.sonar.sslr.grammar.GrammarRuleBuilder;
import org.sonar.sslr.grammar.GrammarRuleKey;
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
        offset = offsets.get(ruleKey);
        result[i] = Instr.call(offset - i, rules.get(ruleKey));
      }
    }

    return new CompiledGrammar(result, rules, offsets);
  }

  public Object sequence(Object e1, Object e2) {
    return new SequenceExpression(convertToExpression(e1), convertToExpression(e2));
  }

  public Object sequence(Object e1, Object e2, Object... rest) {
    return new SequenceExpression(convertToExpressions(Lists.asList(e1, e2, rest)));
  }

  public Object firstOf(Object e1, Object e2) {
    return new FirstOfExpression(convertToExpression(e1), convertToExpression(e2));
  }

  public Object firstOf(Object e1, Object e2, Object... rest) {
    return new FirstOfExpression(convertToExpressions(Lists.asList(e1, e2, rest)));
  }

  public Object optional(Object e) {
    return new OptionalExpression(convertToExpression(e));
  }

  public Object optional(Object e1, Object... rest) {
    return new OptionalExpression(new SequenceExpression(convertToExpressions(Lists.asList(e1, rest))));
  }

  public Object oneOrMore(Object e) {
    // not described in paper
    CompilableMatcher subMatcher = convertToExpression(e);
    return new SequenceExpression(subMatcher, new ZeroOrMoreExpression(subMatcher));
  }

  public Object oneOrMore(Object e1, Object... rest) {
    return oneOrMore(new SequenceExpression(convertToExpressions(Lists.asList(e1, rest))));
  }

  public Object zeroOrMore(Object e) {
    return new ZeroOrMoreExpression(convertToExpression(e));
  }

  public Object zeroOrMore(Object e1, Object... rest) {
    return new ZeroOrMoreExpression(new SequenceExpression(convertToExpressions(Lists.asList(e1, rest))));
  }

  public Object next(Object e) {
    return new NextExpression(convertToExpression(e));
  }

  public Object next(Object e1, Object... rest) {
    return new NextExpression(new SequenceExpression(convertToExpressions(Lists.asList(e1, rest))));
  }

  public Object nextNot(Object e) {
    return new NextNotExpression(convertToExpression(e));
  }

  public Object nextNot(Object e1, Object... rest) {
    return new NextNotExpression(new SequenceExpression(convertToExpressions(Lists.asList(e1, rest))));
  }

  public Object nothing() {
    return new NothingExpression();
  }

  public Object regexp(String regexp) {
    return new PatternExpression(regexp);
  }

  public Object endOfInput() {
    return new EndOfInputExpression();
  }

  public Object token(TokenType tokenType, Object e) {
    // TODO
    return convertToExpression(e);
  }

  public Object commentTrivia(Object e) {
    // TODO
    return convertToExpression(e);
  }

  public Object skippedTrivia(Object e) {
    // TODO
    return convertToExpression(e);
  }

  static CompilableMatcher convertToExpression(Object e) {
    if (e instanceof CompilableMatcher) {
      return (CompilableMatcher) e;
    } else if (e instanceof Character) {
      return new StringExpression(((Character) e).toString());
      // return new CharExpression((Character) e);
    } else if (e instanceof String) {
      return new StringExpression((String) e);
    } else if (e instanceof GrammarRuleKey) {
      return new RuleRefExpression((GrammarRuleKey) e);
    } else {
      throw new IllegalArgumentException("Incorrect type of parsing expression: " + e.getClass().toString());
    }
  }

  static CompilableMatcher[] convertToExpressions(List<Object> expressions) {
    CompilableMatcher[] result = new CompilableMatcher[expressions.size()];
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
