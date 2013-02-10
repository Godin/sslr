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
import org.sonar.sslr.grammar.GrammarException;
import org.sonar.sslr.grammar.GrammarRuleBuilder;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.internal.matchers.SequenceMatcher;

// TODO should create node
public class RuleExpression extends AbstractCompilableMatcher implements CompilableMatcher, GrammarRuleBuilder {

  private final GrammarRuleKey ruleKey;

  private CompilableMatcher expression;

  public RuleExpression(GrammarRuleKey ruleKey) {
    this.ruleKey = ruleKey;
  }

  public GrammarRuleKey getRuleKey() {
    return ruleKey;
  }

  // CompilableMatcher

  public Instr[] compile() {
    if (expression == null) {
      throw new GrammarException("The rule '" + ruleKey + "' hasn't beed defined.");
    }
    return expression.compile();
  }

  // GrammarRuleBuilder

  public GrammarRuleBuilder is(Object e) {
    if (expression != null) {
      throw new GrammarException("The rule '" + ruleKey + "' has already been defined somewhere in the grammar.");
    }
    expression = VmGrammarBuilder.convertToExpression(e);
    return this;
  }

  public GrammarRuleBuilder is(Object e, Object... rest) {
    return is(new SequenceMatcher(VmGrammarBuilder.convertToExpressions(Lists.asList(e, rest))));
  }

  public GrammarRuleBuilder override(Object e) {
    expression = VmGrammarBuilder.convertToExpression(e);
    return this;
  }

  public GrammarRuleBuilder override(Object e, Object... rest) {
    return override(new SequenceMatcher(VmGrammarBuilder.convertToExpressions(Lists.asList(e, rest))));
  }

  public void skip() {
    // TODO
  }

  public void skipIfOneChild() {
    // TODO
  }

  public void recoveryRule() {
    throw new UnsupportedOperationException();
  }

}
