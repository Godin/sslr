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
package org.sonar.sslr.grammar;

import com.sonar.sslr.api.TokenType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.sslr.internal.vm.FirstOfExpression;
import org.sonar.sslr.internal.vm.NextExpression;
import org.sonar.sslr.internal.vm.NextNotExpression;
import org.sonar.sslr.internal.vm.NothingExpression;
import org.sonar.sslr.internal.vm.OneOrMoreExpression;
import org.sonar.sslr.internal.vm.OptionalExpression;
import org.sonar.sslr.internal.vm.ParsingExpression;
import org.sonar.sslr.internal.vm.SequenceExpression;
import org.sonar.sslr.internal.vm.ZeroOrMoreExpression;
import org.sonar.sslr.internal.vm.lexerful.AnyTokenExpression;
import org.sonar.sslr.internal.vm.lexerful.TillNewLineExpression;
import org.sonar.sslr.internal.vm.lexerful.TokenTypeClassExpression;
import org.sonar.sslr.internal.vm.lexerful.TokenTypeExpression;
import org.sonar.sslr.internal.vm.lexerful.TokenTypesExpression;
import org.sonar.sslr.internal.vm.lexerful.TokenValueExpression;
import org.sonar.sslr.internal.vm.lexerful.TokensBridgeExpression;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class LexerfulGrammarBuilderTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void should_create_expressions() {
    LexerfulGrammarBuilder b = LexerfulGrammarBuilder.create();
    ParsingExpression e1 = mock(ParsingExpression.class);
    ParsingExpression e2 = mock(ParsingExpression.class);
    ParsingExpression e3 = mock(ParsingExpression.class);

    assertThat(b.convertToExpression(e1)).isSameAs(e1);
    assertThat(b.convertToExpression("")).isInstanceOf(TokenValueExpression.class);
    assertThat(b.convertToExpression(mock(TokenType.class))).isInstanceOf(TokenTypeExpression.class);
    assertThat(b.convertToExpression(Object.class)).isInstanceOf(TokenTypeClassExpression.class);

    assertThat(b.sequence(e1, e2)).isInstanceOf(SequenceExpression.class);
    assertThat(b.sequence(e1, e2, e3)).isInstanceOf(SequenceExpression.class);

    assertThat(b.firstOf(e1, e2)).isInstanceOf(FirstOfExpression.class);
    assertThat(b.firstOf(e1, e2, e3)).isInstanceOf(FirstOfExpression.class);

    assertThat(b.optional(e1)).isInstanceOf(OptionalExpression.class);
    assertThat(b.optional(e1, e2)).isInstanceOf(OptionalExpression.class);

    assertThat(b.oneOrMore(e1)).isInstanceOf(OneOrMoreExpression.class);
    assertThat(b.oneOrMore(e1, e2)).isInstanceOf(OneOrMoreExpression.class);

    assertThat(b.zeroOrMore(e1)).isInstanceOf(ZeroOrMoreExpression.class);
    assertThat(b.zeroOrMore(e1, e2)).isInstanceOf(ZeroOrMoreExpression.class);

    assertThat(b.next(e1)).isInstanceOf(NextExpression.class);
    assertThat(b.next(e1, e2)).isInstanceOf(NextExpression.class);

    assertThat(b.nextNot(e1)).isInstanceOf(NextNotExpression.class);
    assertThat(b.nextNot(e1, e2)).isInstanceOf(NextNotExpression.class);

    assertThat(b.nothing()).as("singleton").isSameAs(NothingExpression.INSTANCE);

    assertThat(b.isOneOfThem(mock(TokenType.class), mock(TokenType.class))).isInstanceOf(TokenTypesExpression.class);
    assertThat(b.bridge(mock(TokenType.class), mock(TokenType.class))).isInstanceOf(TokensBridgeExpression.class);

    assertThat(b.everything()).as("singleton").isSameAs(AnyTokenExpression.INSTANCE);
    assertThat(b.anyToken()).as("singleton").isSameAs(AnyTokenExpression.INSTANCE);
    assertThat(b.tillNewLine()).as("singleton").isSameAs(TillNewLineExpression.INSTANCE);
  }

  @Test
  public void test_incorrect_type_of_parsing_expression() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Incorrect type of parsing expression: class java.lang.Object");
    LexerfulGrammarBuilder.create().convertToExpression(new Object());
  }

  @Test
  public void test_undefined_root_rule() {
    LexerfulGrammarBuilder b = LexerfulGrammarBuilder.create();
    GrammarRuleKey ruleKey = mock(GrammarRuleKey.class);
    b.setRootRule(ruleKey);
    thrown.expect(GrammarException.class);
    thrown.expectMessage("The rule '" + ruleKey + "' hasn't beed defined.");
    b.build();
  }

  @Test
  public void test_undefined_rule() {
    LexerfulGrammarBuilder b = LexerfulGrammarBuilder.create();
    GrammarRuleKey ruleKey = mock(GrammarRuleKey.class);
    b.rule(ruleKey);
    thrown.expect(GrammarException.class);
    thrown.expectMessage("The rule '" + ruleKey + "' hasn't beed defined.");
    b.build();
  }

  @Test
  public void test_used_undefined_rule() {
    LexerfulGrammarBuilder b = LexerfulGrammarBuilder.create();
    GrammarRuleKey ruleKey1 = mock(GrammarRuleKey.class);
    GrammarRuleKey ruleKey2 = mock(GrammarRuleKey.class);
    b.rule(ruleKey1).is(ruleKey2);
    thrown.expect(GrammarException.class);
    // TODO
    thrown.expectMessage("The rule '" + ruleKey2 + "' hasn't beed defined.");
    // thrown.expectMessage("The rule " + ruleKey2 + " has been used somewhere in grammar, but not defined.");
    b.build();
  }

}
