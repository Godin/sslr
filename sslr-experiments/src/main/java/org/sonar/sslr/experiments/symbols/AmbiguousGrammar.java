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
package org.sonar.sslr.experiments.symbols;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.sonar.sslr.api.Grammar;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;
import org.sonar.sslr.internal.matchers.ParseNode;
import org.sonar.sslr.internal.vm.Machine;
import org.sonar.sslr.internal.vm.NativeExpression;
import org.sonar.sslr.internal.vm.ParsingExpression;

import java.util.Set;

public enum AmbiguousGrammar implements GrammarRuleKey {

  // Syntactic rules

  STATEMENTS,
  COMPOUND_STATEMENT,
  TYPE_DECLARATION,
  TYPE_CAST,
  FUNCTION_CALL,

  // Lexical rules

  TYPEDEF,
  IDENTIFIER,
  SEMICOLON,
  LPAR,
  RPAR,
  LCURLY,
  RCURLY,
  WHITESPACE;

  public static Grammar create() {
    final State state = new State();
    ParsingExpression enterScope = new NativeExpression() {
      @Override
      public void execute(Machine machine) {
        state.scope = new Scope(state.scope);
        machine.jump(1);
      }
    };
    ParsingExpression leaveScope = new NativeExpression() {
      @Override
      public void execute(Machine machine) {
        state.scope = state.scope.parent;
        machine.jump(1);
      }
    };
    ParsingExpression declareType = new NativeExpression() {
      @Override
      public void execute(Machine machine) {
        String identifier = getIdentifier(machine);
        state.scope.types.add(identifier);
        machine.jump(1);
      }
    };
    ParsingExpression checkType = new NativeExpression() {
      @Override
      public void execute(Machine machine) {
        String identifier = getIdentifier(machine);
        if (state.scope.types.contains(identifier)) {
          machine.jump(1);
        } else {
          machine.backtrack();
        }
      }
    };

    LexerlessGrammarBuilder b = LexerlessGrammarBuilder.create();

    b.rule(STATEMENTS).is(b.zeroOrMore(b.firstOf(
      COMPOUND_STATEMENT,
      TYPE_DECLARATION,
      TYPE_CAST,
      FUNCTION_CALL)));
    b.rule(COMPOUND_STATEMENT).is(LCURLY, enterScope, STATEMENTS, RCURLY, leaveScope);
    b.rule(TYPE_DECLARATION).is(TYPEDEF, IDENTIFIER, declareType, SEMICOLON);
    b.rule(TYPE_CAST).is(LPAR, IDENTIFIER, checkType, RPAR, LPAR, IDENTIFIER, RPAR, SEMICOLON, "// type-cast", WHITESPACE);
    b.rule(FUNCTION_CALL).is(LPAR, IDENTIFIER, RPAR, LPAR, IDENTIFIER, RPAR, SEMICOLON, "// function-call", WHITESPACE);

    b.rule(WHITESPACE).is(b.regexp("[ \n\r]*+"));
    b.rule(IDENTIFIER).is(b.regexp("[a-z]++"));
    b.rule(TYPEDEF).is("typedef", WHITESPACE);
    b.rule(SEMICOLON).is(";", WHITESPACE);
    b.rule(LPAR).is("(", WHITESPACE);
    b.rule(RPAR).is(")", WHITESPACE);
    b.rule(LCURLY).is("{", WHITESPACE);
    b.rule(RCURLY).is("}", WHITESPACE);

    return b.build();
  }

  private static class Scope {
    final Scope parent;
    final Set<String> types = Sets.newHashSet();

    Scope() {
      this.parent = null;
    }

    Scope(Scope parent) {
      this.parent = parent;
    }
  }

  private static class State {
    Scope scope = new Scope();
  }

  private static String getIdentifier(Machine machine) {
    ParseNode parseNode = Iterables.getLast(machine.peek().subNodes());
    StringBuilder sb = new StringBuilder();
    for (int i = parseNode.getStartIndex() - parseNode.getEndIndex(); i < 0; i++) {
      sb.append(machine.charAt(i));
    }
    return sb.toString();
  }

}
