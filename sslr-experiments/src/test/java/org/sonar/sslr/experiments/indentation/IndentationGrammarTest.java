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
package org.sonar.sslr.experiments.indentation;

import com.sonar.sslr.api.Grammar;
import org.junit.Test;

import static org.sonar.sslr.tests.Assertions.assertThat;

public class IndentationGrammarTest {

  private Grammar g = IndentationGrammar.create();

  @Test
  public void comment() {
    assertThat(g.rule(IndentationGrammar.COMMENT))
      .matchesPrefix("# comment", "\n");
  }

  @Test
  public void newline() {
    assertThat(g.rule(IndentationGrammar.NEWLINE))
      .matches("\n")
      .matches("# comment\n");
  }

  /**
   * Explicit line joining:
   * Two or more physical lines may be joined into logical lines using backslash characters, as follows:
   * when a physical line ends in a backslash that is not part of a string literal or comment,
   * it is joined with the following forming a single logical line,
   * deleting the backslash and the following end-of-line character.
   * A line ending in a backslash cannot carry a comment.
   * A backslash does not continue a comment.
   * A backslash does not continue a token except for string literals
   * (i.e., tokens other than string literals cannot be split across physical lines using a backslash).
   * A backslash is illegal elsewhere on a line outside a string literal.
   */
  @Test
  public void explicit_line_joining() {
    assertThat(g.rule(IndentationGrammar.EXPLICIT_LINE_JOINING))
      .matchesPrefix("  \t \\\n ", "\n");
    assertThat(g.rule(IndentationGrammar.STATEMENTS))
      .matches("" +
        "if\\\n" +
        "cond:\n" +
        "  something\n");
  }

  /**
   * Implicit line joining:
   * Expressions in parentheses, square brackets or curly braces can be split over more than one physical line without using backslashes.
   * Implicitly continued lines can carry comments.
   * The indentation of the continuation lines is not important.
   * Blank continuation lines are allowed.
   */
  @Test
  public void implicit_line_joining() {
    assertThat(g.rule(IndentationGrammar.IMPLICIT_LINE_JOINING))
      .matches(" \t \\\n \n #comment\n  \n ");
    assertThat(g.rule(IndentationGrammar.STATEMENTS))
      .matches("" +
        "def funcname(arg, \n" +
        "             arg):\n" +
        "  something\n");
  }

  @Test
  public void blank_line() {
    assertThat(g.rule(IndentationGrammar.BLANK_LINE))
      .matches("\n")
      .matches("  \n")
      .matches("# comment\n")
      .matches("  # comment\n");
  }

  @Test
  public void correct_indentation() {
    assertThat(g.rule(IndentationGrammar.STATEMENTS))
      .matches("" +
        "if cond:\n" +
        "  something\n" +
        "  if cond:\n" +
        "    something\n" +
        "  else:\n" +
        "    something\n" +
        "    something\n" +
        "  something\n" +
        "if cond:\n" +
        "  something\n" +
        "  something\n");
  }

  @Test
  public void first_line_should_not_be_indented() {
    assertThat(g.rule(IndentationGrammar.STATEMENTS))
      .matches("something\n")
      .notMatches("  something\n");
  }

  @Test
  public void missing_indent() {
    assertThat(g.rule(IndentationGrammar.STATEMENTS))
      .notMatches("" +
        "if cond:\n" +
        "something\n");
  }

  @Test
  public void unexpected_indent() {
    assertThat(g.rule(IndentationGrammar.STATEMENTS))
      .notMatches("" +
        "if cond:\n" +
        "  something\n" +
        "    something\n");
  }

  @Test
  public void inconsistent_dedent() {
    assertThat(g.rule(IndentationGrammar.STATEMENTS))
      .notMatches("" +
        "if cond:\n" +
        "    something\n" +
        "  something\n");
  }

  @Test
  public void indentation_can_be_inconsistent() {
    assertThat(g.rule(IndentationGrammar.STATEMENTS))
      .matches("" +
        "if cond:\n" +
        "  something\n" +
        "if cond:\n" +
        "    something\n");
  }

  @Test
  public void blank_lines_should_be_ignored() {
    assertThat(g.rule(IndentationGrammar.STATEMENTS))
      .matches("" +
        "if cond:\n" +
        "\n" +
        "    # commment\n" +
        "  something\n");
  }

  @Test
  public void funcdef() {
    assertThat(g.rule(IndentationGrammar.STATEMENTS))
      .matches("" +
        "def funcname():\n" +
        "  something\n")
      .matches("" +
        "def funcname(arg):\n" +
        "  something\n")
      .matches("" +
        "def funcname(arg, arg):\n" +
        "  something\n");
  }

}
