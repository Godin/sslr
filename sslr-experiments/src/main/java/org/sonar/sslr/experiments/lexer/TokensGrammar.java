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
package org.sonar.sslr.experiments.lexer;

import com.sonar.sslr.api.Grammar;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;
import org.sonar.sslr.internal.vm.ParsingExpression;

public enum TokensGrammar implements GrammarRuleKey {

  ROOT,
  EXPRESSION,
  ADDITIVE_EXPRESSION,
  MULTIPLICATIVE_EXPRESSION,
  PRIMARY,
  PARENS;

  public static Grammar create() {
    LexerlessGrammarBuilder b = LexerlessGrammarBuilder.create();
    Tokens t = new Tokens();

    b.rule(ROOT).is(wrap(b, t.init, EXPRESSION, t.destroy));
    b.rule(EXPRESSION).is(ADDITIVE_EXPRESSION, b.endOfInput());
    b.rule(ADDITIVE_EXPRESSION).is(
      MULTIPLICATIVE_EXPRESSION,
      b.zeroOrMore(b.firstOf(t.plus, t.minus), MULTIPLICATIVE_EXPRESSION));
    b.rule(MULTIPLICATIVE_EXPRESSION).is(
      PRIMARY,
      b.zeroOrMore(b.firstOf(t.div, t.mul), PRIMARY));
    b.rule(PRIMARY).is(b.firstOf(t.number, PARENS));
    b.rule(PARENS).is(t.leftParenthesis, ADDITIVE_EXPRESSION, t.rightParenthesis);

    return b.build();
  }

  private static Object wrap(LexerlessGrammarBuilder b, ParsingExpression init, Object expression, ParsingExpression cancel) {
    return b.sequence(
      init,
      b.firstOf(
        b.sequence(expression, cancel),
        // Cancel side-effects of action "init", if parser fails to parse expression:
        b.sequence(cancel, b.nothing())));
  }

}
