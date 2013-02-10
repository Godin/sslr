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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.sslr.grammar.GrammarException;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.internal.matchers.Matcher;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class RuleExpressionTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test(timeout = 5000)
  public void test() {
    CharExpression a = new CharExpression('a');
    RuleExpression rule = new RuleExpression(mock(GrammarRuleKey.class));
    Instr[] instructions = {Instr.call(2, rule), Instr.end(), Instr.ch('a', a), Instr.ret()};
    assertThat(new Machine("a", instructions).execute()).isTrue();
    assertThat(new Machine("b", instructions).execute()).isFalse();
  }

  @Test
  public void should_return_ruleKey() {
    GrammarRuleKey ruleKey = mock(GrammarRuleKey.class);
    RuleExpression rule = new RuleExpression(ruleKey);
    assertThat(rule.getRuleKey()).isSameAs(ruleKey);
  }

  @Test
  public void should_compile() {
    GrammarRuleKey ruleKey = mock(GrammarRuleKey.class);
    RuleExpression rule = new RuleExpression(ruleKey);
    Matcher e = mock(Matcher.class);
    rule.is(e);
    rule.compile();
    verify(e).compile();
  }

  @Test
  public void should_fail_to_compile_if_not_defined() {
    GrammarRuleKey ruleKey = mock(GrammarRuleKey.class);
    RuleExpression rule = new RuleExpression(ruleKey);
    thrown.expect(GrammarException.class);
    thrown.expectMessage("The rule '" + ruleKey + "' hasn't beed defined.");
    rule.compile();
  }

  @Test
  public void should_not_allow_redefinition() {
    GrammarRuleKey ruleKey = mock(GrammarRuleKey.class);
    RuleExpression rule = new RuleExpression(ruleKey);
    rule.is(mock(Matcher.class));
    thrown.expect(GrammarException.class);
    thrown.expectMessage("The rule '" + ruleKey + "' has already been defined somewhere in the grammar.");
    rule.is(mock(Matcher.class));
  }

  @Test
  public void should_override() {
    GrammarRuleKey ruleKey = mock(GrammarRuleKey.class);
    RuleExpression rule = new RuleExpression(ruleKey);
    Matcher e1 = mock(Matcher.class);
    rule.is(e1);
    Matcher e2 = mock(Matcher.class);
    rule.override(e2);
    rule.compile();
    verify(e1, never()).compile();
    verify(e2).compile();
  }

  @Test
  public void recovery_rule_not_supported() {
    GrammarRuleKey ruleKey = mock(GrammarRuleKey.class);
    RuleExpression rule = new RuleExpression(ruleKey);
    thrown.expect(UnsupportedOperationException.class);
    rule.recoveryRule();
  }

}
