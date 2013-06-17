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
package org.sonar.sslr.examples.grammars;

import com.sonar.sslr.api.Grammar;
import org.junit.Test;
import static org.sonar.sslr.tests.Assertions.assertThat;

public class PascalGrammarTest {

  private Grammar g = PascalGrammar.create();

  {
    g.rule(PascalGrammar.LABEL).mock();
    g.rule(PascalGrammar.VARIABLE_ACCESS).mock();
    g.rule(PascalGrammar.FUNCTION_IDENTIFIER).mock();
  }

  @Test
  public void statement() {
    g.rule(PascalGrammar.SIMPLE_STATEMENT).mock();
    g.rule(PascalGrammar.STRUCTURED_STATEMENT).mock();
    assertThat(g.rule(PascalGrammar.STATEMENT))
      .matches("SIMPLE_STATEMENT")
      .matches("STRUCTURED_STATEMENT")
      .matches("LABEL : SIMPLE_STATEMENT")
      .matches("LABEL : STRUCTURED_STATEMENT");
  }

  @Test
  public void empty_statement() {
    assertThat(g.rule(PascalGrammar.EMPTY_STATEMENT))
      .matches("");
  }

  @Test
  public void assignment_statement() {
    g.rule(PascalGrammar.EXPRESSION).mock();
    assertThat(g.rule(PascalGrammar.ASSIGNMENT_STATEMENT))
      .matches("VARIABLE_ACCESS := EXPRESSION")
      .matches("FUNCTION_IDENTIFIER := EXPRESSION");
  }

  @Test
  public void procedure_statement() {
    assertThat(g.rule(PascalGrammar.PROCEDURE_STATEMENT));
  }

  @Test
  public void goto_statement() {
    assertThat(g.rule(PascalGrammar.GOTO_STATEMENT))
      .matches("goto LABEL");
  }

  @Test
  public void compound_statement() {
    g.rule(PascalGrammar.STATEMENT).mock();
    assertThat(g.rule(PascalGrammar.COMPOUND_STATEMENT))
      .matches("begin STATEMENT end ")
      .matches("begin STATEMENT ; STATEMENT end ");
  }

  @Test
  public void if_statement() {
    g.rule(PascalGrammar.BOOLEAN_EXPRESSION).mock();
    g.rule(PascalGrammar.STATEMENT).mock();
    assertThat(g.rule(PascalGrammar.IF_STATEMENT))
      .matches("if BOOLEAN_EXPRESSION then STATEMENT")
      .matches("if BOOLEAN_EXPRESSION then STATEMENT else STATEMENT");
  }

  @Test
  public void repeat_statement() {
    g.rule(PascalGrammar.BOOLEAN_EXPRESSION).mock();
    g.rule(PascalGrammar.STATEMENT).mock();
    assertThat(g.rule(PascalGrammar.REPEAT_STATEMENT))
      .matches("repeat STATEMENT until BOOLEAN_EXPRESSION")
      .matches("repeat STATEMENT ; STATEMENT until BOOLEAN_EXPRESSION");
  }

  @Test
  public void while_statement() {
    g.rule(PascalGrammar.BOOLEAN_EXPRESSION).mock();
    g.rule(PascalGrammar.STATEMENT).mock();
    assertThat(g.rule(PascalGrammar.WHILE_STATEMENT))
      .matches("while BOOLEAN_EXPRESSION do STATEMENT");
  }

  @Test
  public void for_statement() {
    g.rule(PascalGrammar.EXPRESSION).mock();
    g.rule(PascalGrammar.STATEMENT).mock();
    g.rule(PascalGrammar.CONTROL_VARIABLE).mock();
    assertThat(g.rule(PascalGrammar.FOR_STATEMENT))
      .matches("for CONTROL_VARIABLE := EXPRESSION to EXPRESSION do STATEMENT")
      .matches("for CONTROL_VARIABLE := EXPRESSION downto EXPRESSION do STATEMENT");
  }

  @Test
  public void relational_operator() {
    assertThat(g.rule(PascalGrammar.RELATIONAL_OPERATOR))
      .matches("= ")
      .matches("<> ")
      .matches("<= ")
      .matches(">= ")
      .matches("< ")
      .matches("> ")
      .matches("in ");
  }

  @Test
  public void expression() {
    assertThat(g.rule(PascalGrammar.EXPRESSION))
      .matches("1 = 2 ");
  }

  @Test
  public void adding_operator() {
    assertThat(g.rule(PascalGrammar.ADDING_OPERATOR))
      .matches("+ ")
      .matches("- ")
      .matches("or ");
  }

  @Test
  public void simple_expression() {
    assertThat(g.rule(PascalGrammar.SIMPLE_EXPRESSION))
      .matches("1 + 2 ");
  }

  @Test
  public void multiplying_operator() {
    assertThat(g.rule(PascalGrammar.MULTIPLYING_OPERATOR))
      .matches("* ")
      .matches("/ ")
      .matches("div ")
      .matches("mod ")
      .matches("and ");
  }

  @Test
  public void term() {
    assertThat(g.rule(PascalGrammar.TERM))
      .matches("1 * 2 ");
  }

  @Test
  public void unsigned_integer() {
    assertThat(g.rule(PascalGrammar.UNSIGNED_INTEGER))
      .matches("0 ")
      .matches("42 ");
  }

  @Test
  public void unsigned_real() {
    assertThat(g.rule(PascalGrammar.UNSIGNED_REAL))
      .matches("1.2 ")
      .matches("1.2e+3 ")
      .matches("1e-2 ")
      .notMatches("1 ");
  }

  @Test
  public void character_string() {
    assertThat(g.rule(PascalGrammar.CHARACTER_STRING))
      .matches("'' ")
      .matches("'foo' ");
  }

  @Test
  public void factor() {
    assertThat(g.rule(PascalGrammar.FACTOR))
      .matches("42 ")
      .matches("not 42 ")
      .matches("( 42 ) ")
      .matches("identifier");
  }

}
