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

import com.sonar.sslr.api.TokenType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.sslr.grammar.GrammarRuleKey;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class VmGrammarBuilderTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test() {
    CompilableMatcher e1 = mock(CompilableMatcher.class);
    CompilableMatcher e2 = mock(CompilableMatcher.class);
    CompilableMatcher e3 = mock(CompilableMatcher.class);
    VmGrammarBuilder b = VmGrammarBuilder.create();

    assertThat(VmGrammarBuilder.convertToExpression(e1)).isSameAs(e1);
    assertThat(VmGrammarBuilder.convertToExpression("")).isInstanceOf(StringExpression.class);
    assertThat(VmGrammarBuilder.convertToExpression('c')).isInstanceOf(StringExpression.class);
    assertThat(VmGrammarBuilder.convertToExpression(mock(GrammarRuleKey.class))).isInstanceOf(RuleRefExpression.class);

    assertThat(b.sequence(e1, e2)).isInstanceOf(SequenceExpression.class);
    assertThat(b.sequence(e1, e2, e3)).isInstanceOf(SequenceExpression.class);

    assertThat(b.firstOf(e1, e2)).isInstanceOf(FirstOfExpression.class);
    assertThat(b.firstOf(e1, e2, e3)).isInstanceOf(FirstOfExpression.class);

    assertThat(b.optional(e1)).isInstanceOf(OptionalExpression.class);
    assertThat(b.optional(e1, e2)).isInstanceOf(OptionalExpression.class);

    assertThat(b.oneOrMore(e1)).isInstanceOf(SequenceExpression.class);
    assertThat(b.oneOrMore(e1, e2)).isInstanceOf(SequenceExpression.class);

    assertThat(b.zeroOrMore(e1)).isInstanceOf(ZeroOrMoreExpression.class);
    assertThat(b.zeroOrMore(e1, e2)).isInstanceOf(ZeroOrMoreExpression.class);

    assertThat(b.next(e1)).isInstanceOf(NextExpression.class);
    assertThat(b.next(e1, e2)).isInstanceOf(NextExpression.class);

    assertThat(b.nextNot(e1)).isInstanceOf(NextNotExpression.class);
    assertThat(b.nextNot(e1, e2)).isInstanceOf(NextNotExpression.class);

    assertThat(b.nothing()).isInstanceOf(NothingExpression.class);

    assertThat(b.regexp("")).isInstanceOf(PatternExpression.class);

    assertThat(b.endOfInput()).isInstanceOf(EndOfInputExpression.class);

    assertThat(b.token(mock(TokenType.class), e1)).isSameAs(e1);
    assertThat(b.commentTrivia(e1)).isSameAs(e1);
    assertThat(b.skippedTrivia(e1)).isSameAs(e1);
  }

  @Test
  public void test_wrong_argument() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Incorrect type of parsing expression: class java.lang.Object");
    VmGrammarBuilder.convertToExpression(new Object());
  }

}
