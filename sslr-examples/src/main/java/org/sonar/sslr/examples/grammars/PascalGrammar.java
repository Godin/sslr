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
  CONTROL_VARIABLE,
  INITIAL_VALUE,
  FINAL_VALUE,
  WITH_STATEMENT,

  STATEMENT_SEQUENCE,

  BOOLEAN_EXPRESSION,
  EXPRESSION,
  RELATIONAL_OPERATOR,
  SIMPLE_EXPRESSION,
  TERM,
  ADDING_OPERATOR,
  FACTOR,
  MULTIPLYING_OPERATOR,
  UNSIGNED_CONSTANT,
  UNSIGNED_NUMBER,
  UNSIGNED_INTEGER,
  UNSIGNED_REAL,
  CHARACTER_STRING,
  CONSTANT_IDENTIFIER,

  SIGN,

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
      b.rule(FOR_STATEMENT).is(w("for"), CONTROL_VARIABLE, w(":="), INITIAL_VALUE, b.firstOf(w("to"), w("downto")), FINAL_VALUE, w("do"), STATEMENT);
      b.rule(CONTROL_VARIABLE).is(b.nothing()); // TODO
      b.rule(INITIAL_VALUE).is(EXPRESSION);
      b.rule(FINAL_VALUE).is(EXPRESSION);

      b.rule(WITH_STATEMENT).is(b.nothing()); // TODO
    }

    private void expressions() {
      b.rule(BOOLEAN_EXPRESSION).is(EXPRESSION);
      b.rule(EXPRESSION).is(SIMPLE_EXPRESSION, b.optional(RELATIONAL_OPERATOR, SIMPLE_EXPRESSION));
      b.rule(RELATIONAL_OPERATOR).is(b.firstOf(w("="), w("<>"), w("<="), w(">="), w("<"), w(">"), w("in")));
      b.rule(SIMPLE_EXPRESSION).is(b.optional(SIGN), TERM, b.zeroOrMore(ADDING_OPERATOR, TERM));
      b.rule(SIGN).is(b.firstOf(w("+"), w("-")));
      b.rule(ADDING_OPERATOR).is(b.firstOf(w("+"), w("-"), w("or")));
      b.rule(TERM).is(FACTOR, b.zeroOrMore(MULTIPLYING_OPERATOR, FACTOR));
      b.rule(MULTIPLYING_OPERATOR).is(b.firstOf(w("*"), w("/"), w("div"), w("mod"), w("and")));
      b.rule(FACTOR).is(b.firstOf(
        b.sequence(w("("), EXPRESSION, w(")")),
        b.sequence(w("not"), FACTOR),
        VARIABLE_ACCESS,
        UNSIGNED_CONSTANT
//        FUNCTION_DESIGNATOR,
//        SET_CONSTRUCTOR
      ));
      b.rule(UNSIGNED_CONSTANT).is(b.firstOf(
        UNSIGNED_NUMBER,
        CHARACTER_STRING,
        "nil",
        CONSTANT_IDENTIFIER
      ));
      b.rule(UNSIGNED_NUMBER).is(b.firstOf(
        UNSIGNED_INTEGER,
        UNSIGNED_REAL
      ));
      b.rule(UNSIGNED_INTEGER).is(b.regexp("[0-9]++"), WHITESPACE);
      b.rule(UNSIGNED_REAL).is(b.regexp("[0-9]++(\\.[0-9]++(e[+-]?+[0-9]++)?+|e[+-]?+[0-9]++)"), WHITESPACE);
      b.rule(CHARACTER_STRING).is(b.regexp("'[^']*+'"), WHITESPACE);
      b.rule(CONSTANT_IDENTIFIER).is(IDENTIFIER);
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
      b.rule(VARIABLE_ACCESS).is(b.nothing());
      b.rule(FUNCTION_IDENTIFIER).is(b.nothing());

      expressions();
      statements();
      return b.build();
    }
  }

}
