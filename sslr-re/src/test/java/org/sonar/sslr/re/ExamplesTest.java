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
package org.sonar.sslr.re;

import org.junit.Test;

public class ExamplesTest {

  private ReBuilder r = ReBuilder.create();

  @Test
  public void eol() {
    Re eol = r.oneOf(
      r.sequence(r.optional('\r'), '\n'),
      '\r'
    );
    Assert.that(eol.asString())
      .isEqualTo("\r?+\n|\r");
    Assert.that(eol)
      .matches("\r")
      .matches("\n")
      .matches("\r\n");
  }

  @Test
  public void inline_comment() {
    Re inlineComment = r.sequence("//", r.zeroOrMore(r.not('\n', '\r')));
    Assert.that(inlineComment.asString())
      .isEqualTo("//[^\n\r]*+");
    Assert.that(inlineComment)
      .matches("// comment");
  }

  @Test
  public void multi_line_comment() {
    Re multiLineComment = r.sequence(
      "/*",
      r.reluctantZeroOrMore(r.anyChar()),
      "*/");
    Assert.that(multiLineComment.asString())
      .isEqualTo("/\\*.*?\\*/");
    Assert.that(multiLineComment)
      .matches("/**/")
      .matches("/* foo */")
      .matches("/* foo*bar */")
      .matches("/* foo/bar */")
      .matches("/* foo/*bar */")
      .matchesPrefix("/* foo */", "bar */")
      .notMatches("/* foo")
      .notMatches("foo */");
  }

  @Test
  public void multi_line_comment2() {
    Re multiLineComment = r.sequence(
      "/*",
      r.zeroOrMore(r.oneOf(
        r.not('*'),
        r.sequence(r.oneOrMore("*"), r.not('*', '/')))),
      r.oneOrMore("*"), "/");
    Assert.that(multiLineComment.asString())
      .isEqualTo("/\\*(?:[^*]|\\*++[^*/])*+\\*++/");
    Assert.that(multiLineComment)
      .matches("/**/")
      .matches("/* foo */")
      .matches("/* foo*bar */")
      .matches("/* foo/bar */")
      .matches("/* foo/*bar */")
      .matchesPrefix("/* foo */", "bar */")
      .notMatches("/* foo")
      .notMatches("foo */");
  }

  @Test
  public void exponent() {
    Re exponent = r.sequence(r.anyOf('e', 'E'), r.optional(r.anyOf('+', '-')), r.oneOrMore(r.inRange('0', '9')));
    Assert.that(exponent.asString())
      .isEqualTo("[eE][+-]?+[0-9]++");
    Assert.that(exponent)
      .matches("E+42")
      .matches("e-13");
  }

  @Test
  public void literal() {
    Re literal = r.sequence("'", r.zeroOrMore(r.not('\'')), "'");
    Assert.that(literal.asString())
      .isEqualTo("'[^']*+'");
    Assert.that(literal)
      .matches("'literal'");
  }

}
