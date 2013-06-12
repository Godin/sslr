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
    g.rule(PascalGrammar.EXPRESSION).mock();
    g.rule(PascalGrammar.BOOLEAN_EXPRESSION).mock();
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
    g.rule(PascalGrammar.STATEMENT).mock();
    assertThat(g.rule(PascalGrammar.IF_STATEMENT))
      .matches("if BOOLEAN_EXPRESSION then STATEMENT")
      .matches("if BOOLEAN_EXPRESSION then STATEMENT else STATEMENT");
  }

  @Test
  public void repeat_statement() {
    g.rule(PascalGrammar.STATEMENT).mock();
    assertThat(g.rule(PascalGrammar.REPEAT_STATEMENT))
      .matches("repeat STATEMENT until BOOLEAN_EXPRESSION")
      .matches("repeat STATEMENT ; STATEMENT until BOOLEAN_EXPRESSION");
  }

  @Test
  public void while_statement() {
    g.rule(PascalGrammar.STATEMENT).mock();
    assertThat(g.rule(PascalGrammar.WHILE_STATEMENT))
      .matches("while BOOLEAN_EXPRESSION do STATEMENT");
  }

}
