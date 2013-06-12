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
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;

/**
 * ISO 7185
 */
public enum PascalGrammar implements GrammarRuleKey {

  PROGRAM,
  PROGRAM_HEADING,
  PROGRAM_BLOCK,
  PROGRAM_PARAMETER_LIST,

  IDENTIFIER,
  IDENTIFIER_LIST,

  BLOCK,
//  LABEL_DECLARATION_PART,
//  CONSTANT_DEFINITION_PART,
//  TYPE_DEFINITION_PART,
//  VARIABLE_DECLARATION_PART,
//  PROCEDURE_AND_FUNCTION_DECLARATION_PART,
  STATEMENT_PART,

  STATEMENT,
  LABEL,
  SIMPLE_STATEMENT,
  EMPTY_STATEMENT,
  ASSIGNMENT_STATEMENT,
  VARIABLE_ACCESS,
  FUNCTION_IDENTIFIER,
  PROCEDURE_STATEMENT,
  GOTO_STATEMENT,
  STRUCTURED_STATEMENT,
  COMPOUND_STATEMENT,
  CONDITIONAL_STATEMENT,
  IF_STATEMENT,
  ELSE_PART,
  CASE_STATEMENT,
  REPETITIVE_STATEMENT,
  REPEAT_STATEMENT,
  WHILE_STATEMENT,
  FOR_STATEMENT,
  WITH_STATEMENT,

  STATEMENT_SEQUENCE,

  EXPRESSION,
  BOOLEAN_EXPRESSION,

  WHITESPACE,
  ;

  public static Grammar create() {
    return new PascalGrammarBuilder().build();
  }

  private static class PascalGrammarBuilder {
    private final LexerlessGrammarBuilder b;

    private PascalGrammarBuilder() {
      this.b = LexerlessGrammarBuilder.create();
    }

    private void statements() {
      b.rule(STATEMENT_SEQUENCE).is(STATEMENT, b.zeroOrMore(w(";"), STATEMENT));

      b.rule(STATEMENT).is(b.optional(LABEL, w(":")), b.firstOf(SIMPLE_STATEMENT, STRUCTURED_STATEMENT));

      b.rule(SIMPLE_STATEMENT).is(b.firstOf(
        EMPTY_STATEMENT,
        ASSIGNMENT_STATEMENT,
        PROCEDURE_STATEMENT,
        GOTO_STATEMENT
      ));
      b.rule(EMPTY_STATEMENT).is("");
      b.rule(ASSIGNMENT_STATEMENT).is(b.firstOf(VARIABLE_ACCESS, FUNCTION_IDENTIFIER), w(":="), EXPRESSION);
      b.rule(PROCEDURE_STATEMENT).is(b.nothing()); // TODO
      b.rule(GOTO_STATEMENT).is(w("goto"), LABEL);

      b.rule(STRUCTURED_STATEMENT).is(b.firstOf(
        COMPOUND_STATEMENT,
        CONDITIONAL_STATEMENT,
        REPETITIVE_STATEMENT,
        WITH_STATEMENT
      ));
      b.rule(COMPOUND_STATEMENT).is(w("begin"), STATEMENT_SEQUENCE, w("end"));

      b.rule(CONDITIONAL_STATEMENT).is(b.firstOf(IF_STATEMENT, CASE_STATEMENT));
      b.rule(IF_STATEMENT).is(w("if"), BOOLEAN_EXPRESSION, w("then"), STATEMENT, b.optional(ELSE_PART));
      b.rule(ELSE_PART).is(w("else"), STATEMENT);
      b.rule(CASE_STATEMENT).is(b.nothing()); // TODO

      b.rule(REPETITIVE_STATEMENT).is(b.firstOf(
        REPEAT_STATEMENT,
        WHILE_STATEMENT,
        FOR_STATEMENT
      ));
      b.rule(REPEAT_STATEMENT).is(w("repeat"), STATEMENT_SEQUENCE, w("until"), BOOLEAN_EXPRESSION);
      b.rule(WHILE_STATEMENT).is(w("while"), BOOLEAN_EXPRESSION, w("do"), STATEMENT);
      b.rule(FOR_STATEMENT).is(b.nothing()); // TODO

      b.rule(WITH_STATEMENT).is(b.nothing()); // TODO
    }

    private Object w(String word) {
      return b.sequence(word, WHITESPACE);
    }

    public Grammar build() {
      b.rule(WHITESPACE).is(b.regexp("[ ]++"));

      b.rule(PROGRAM).is(PROGRAM_HEADING, ";", PROGRAM_BLOCK, ".");
      b.rule(PROGRAM_BLOCK).is(BLOCK);
      b.rule(PROGRAM_HEADING).is("program", IDENTIFIER, b.optional("(", PROGRAM_PARAMETER_LIST, ")"));
      b.rule(PROGRAM_PARAMETER_LIST).is(IDENTIFIER_LIST);

      b.rule(BLOCK).is(
//        LABEL_DECLARATION_PART,
//        CONSTANT_DEFINITION_PART,
//        TYPE_DEFINITION_PART,
//        VARIABLE_DECLARATION_PART,
//        PROCEDURE_AND_FUNCTION_DECLARATION_PART,
        STATEMENT_PART
      );

      b.rule(STATEMENT_PART).is(COMPOUND_STATEMENT);

      b.rule(IDENTIFIER).is(b.regexp("[a-zA-Z][a-zA-Z0-9]*+"));
      b.rule(IDENTIFIER_LIST).is(IDENTIFIER, b.zeroOrMore(w(","), IDENTIFIER));

      b.rule(LABEL).is(/* digit-sequence: */b.regexp("[0-9]++"));

      // FIXME:
      b.rule(BOOLEAN_EXPRESSION).is(b.nothing());
      b.rule(VARIABLE_ACCESS).is(b.nothing());
      b.rule(FUNCTION_IDENTIFIER).is(b.nothing());
      b.rule(EXPRESSION).is(b.nothing());

      statements();
      return b.build();
    }
  }

}
