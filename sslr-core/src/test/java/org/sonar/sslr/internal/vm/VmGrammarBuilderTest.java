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
import org.sonar.sslr.internal.matchers.TriviaMatcher;
import org.sonar.sslr.internal.matchers.ZeroOrMoreMatcher;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class VmGrammarBuilderTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test() {
    Matcher e1 = mock(Matcher.class);
    Matcher e2 = mock(Matcher.class);
    Matcher e3 = mock(Matcher.class);
    VmGrammarBuilder b = VmGrammarBuilder.create();

    assertThat(VmGrammarBuilder.convertToExpression(e1)).isSameAs(e1);
    assertThat(VmGrammarBuilder.convertToExpression("")).isInstanceOf(StringMatcher.class);
    assertThat(VmGrammarBuilder.convertToExpression('c')).isInstanceOf(StringMatcher.class);
    assertThat(VmGrammarBuilder.convertToExpression(mock(GrammarRuleKey.class))).isInstanceOf(RuleRefExpression.class);

    assertThat(b.sequence(e1, e2)).isInstanceOf(SequenceMatcher.class);
    assertThat(b.sequence(e1, e2, e3)).isInstanceOf(SequenceMatcher.class);

    assertThat(b.firstOf(e1, e2)).isInstanceOf(FirstOfMatcher.class);
    assertThat(b.firstOf(e1, e2, e3)).isInstanceOf(FirstOfMatcher.class);

    assertThat(b.optional(e1)).isInstanceOf(OptionalMatcher.class);
    assertThat(b.optional(e1, e2)).isInstanceOf(OptionalMatcher.class);

    assertThat(b.oneOrMore(e1)).isInstanceOf(OneOrMoreMatcher.class);
    assertThat(b.oneOrMore(e1, e2)).isInstanceOf(OneOrMoreMatcher.class);

    assertThat(b.zeroOrMore(e1)).isInstanceOf(ZeroOrMoreMatcher.class);
    assertThat(b.zeroOrMore(e1, e2)).isInstanceOf(ZeroOrMoreMatcher.class);

    assertThat(b.next(e1)).isInstanceOf(TestMatcher.class);
    assertThat(b.next(e1, e2)).isInstanceOf(TestMatcher.class);

    assertThat(b.nextNot(e1)).isInstanceOf(TestNotMatcher.class);
    assertThat(b.nextNot(e1, e2)).isInstanceOf(TestNotMatcher.class);

    assertThat(b.nothing()).isInstanceOf(NothingMatcher.class);

    assertThat(b.regexp("")).isInstanceOf(PatternMatcher.class);

    assertThat(b.endOfInput()).isInstanceOf(EndOfInputMatcher.class);

    assertThat(b.commentTrivia(e1)).isInstanceOf(TriviaMatcher.class);
    assertThat(b.skippedTrivia(e1)).isInstanceOf(TriviaMatcher.class);
  }

  @Test
  public void test_wrong_argument() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Incorrect type of parsing expression: class java.lang.Object");
    VmGrammarBuilder.convertToExpression(new Object());
  }

  @Test
  public void test_undefined_rule() {
    VmGrammarBuilder b = VmGrammarBuilder.create();
    GrammarRuleKey ruleKey = mock(GrammarRuleKey.class);
    b.rule(ruleKey);
    thrown.expect(GrammarException.class);
    thrown.expectMessage("The rule '" + ruleKey + "' hasn't beed defined.");
    b.build();
  }

  @Test
  public void test_used_undefined_rule() {
    VmGrammarBuilder b = VmGrammarBuilder.create();
    GrammarRuleKey ruleKey1 = mock(GrammarRuleKey.class);
    GrammarRuleKey ruleKey2 = mock(GrammarRuleKey.class);
    b.rule(ruleKey1).is(ruleKey2);
    thrown.expect(GrammarException.class);
    thrown.expectMessage("The rule " + ruleKey2 + " has been used somewhere in grammar, but not defined.");
    b.build();
  }

}
