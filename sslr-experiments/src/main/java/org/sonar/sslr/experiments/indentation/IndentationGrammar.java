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

import com.google.common.collect.Iterables;
import com.sonar.sslr.api.Grammar;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;
import org.sonar.sslr.internal.matchers.ParseNode;
import org.sonar.sslr.internal.vm.Machine;
import org.sonar.sslr.internal.vm.NativeExpression;

/**
 * Grammar, which mimics Python rules of indentation.
 * Python is a language, which is adhere to <a href="http://en.wikipedia.org/wiki/Off-side_rule">off-side rule</a> - see
 * <a href="http://docs.python.org/release/3.3.2/reference/lexical_analysis.html">Lexical analysis in Python Language Reference</a>.
 */
public enum IndentationGrammar implements GrammarRuleKey {

  // Lexical rules

  NEWLINE,
  COMMENT,
  EXPLICIT_LINE_JOINING,
  IMPLICIT_LINE_JOINING,
  WHITESPACE,
  INDENTATION,
  BLANK_LINE,

  // Keywords

  DEF,
  IF,
  ELSE,

  // Delimiters

  COLON,
  COMMA,
  LEFT_PARENTHESIS,
  RIGHT_PARENTHESIS,

  // Syntactic rules

  STATEMENTS,
  STATEMENT,
  SIMPLE_STATEMENT,
  COMPOUND_STATEMENT,
  IF_STATEMENT,
  BLOCK,
  FUNCDEF,
  ARGLIST;

  public static Grammar create() {
    final State state = new State();
    Action indent = new Action() {
      public void execute() {
        state.indentationLevel = new IndentationLevel(state.indentationLevel);
      }
    };
    Action dedent = new Action() {
      public void execute() {
        state.indentationLevel = state.indentationLevel.parent;
      }
    };
    Predicate checkIndentation = new Predicate() {
      public boolean check(Machine machine) {
        ParseNode parseNode = Iterables.getLast(machine.peek().subNodes());
        // TODO
        // Seems that "explicit line joining" should be taken into account,
        // because such lines form one logical line and indentation rules are defined using logical lines.
        // And also tabulation characters should be replaced by spaces,
        // formfeed character should be ignored at the start of the line.
        int whitespaces = parseNode.getEndIndex() - parseNode.getStartIndex();
        if (state.indentationLevel.value == -1) {
          if (whitespaces > state.indentationLevel.parent.value) {
            state.indentationLevel.value = whitespaces;
            return true;
          } else {
            return false;
          }
        } else {
          return state.indentationLevel.value == whitespaces;
        }
      }
    };
    Action enterImplicitLineJoining = new Action() {
      @Override
      public void execute() {
        state.implicitLineJoining++;
      }
    };
    Action exitImplicitLineJoining = new Action() {
      @Override
      public void execute() {
        state.implicitLineJoining--;
      }
    };
    Predicate checkImplicitLineJoining = new Predicate() {
      @Override
      public boolean check(Machine machine) {
        return state.implicitLineJoining > 0;
      }
    };

    LexerlessGrammarBuilder b = LexerlessGrammarBuilder.create();

    b.rule(COMMENT).is(b.regexp("#[^\n]*+"));
    b.rule(NEWLINE).is(b.optional(COMMENT), "\n");
    b.rule(EXPLICIT_LINE_JOINING).is(b.regexp("(?:[ \t]|\\\\\n)*+"));
    b.rule(IMPLICIT_LINE_JOINING).is(
      EXPLICIT_LINE_JOINING,
      b.zeroOrMore(NEWLINE, EXPLICIT_LINE_JOINING));
    b.rule(WHITESPACE).is(b.firstOf(
      b.sequence(checkImplicitLineJoining, IMPLICIT_LINE_JOINING),
      EXPLICIT_LINE_JOINING));
    b.rule(INDENTATION).is(EXPLICIT_LINE_JOINING, checkIndentation);
    b.rule(BLANK_LINE).is(EXPLICIT_LINE_JOINING, NEWLINE);

    b.rule(DEF).is("def", WHITESPACE);
    b.rule(IF).is("if", WHITESPACE);
    b.rule(ELSE).is("else", WHITESPACE);

    b.rule(COLON).is(":", WHITESPACE);
    b.rule(COMMA).is(",", WHITESPACE);
    // Following two rules produce side-effects, which will not be canceled during backtracking,
    // however this allows to localize actions in single place:
    b.rule(LEFT_PARENTHESIS).is("(", enterImplicitLineJoining, WHITESPACE);
    b.rule(RIGHT_PARENTHESIS).is(")", exitImplicitLineJoining, WHITESPACE);

    b.rule(STATEMENTS).is(b.oneOrMore(b.firstOf(
      BLANK_LINE,
      b.sequence(INDENTATION, STATEMENT))));
    b.rule(STATEMENT).is(b.firstOf(
      b.sequence(SIMPLE_STATEMENT, NEWLINE),
      COMPOUND_STATEMENT));
    b.rule(SIMPLE_STATEMENT).is("something");
    b.rule(COMPOUND_STATEMENT).is(b.firstOf(
      IF_STATEMENT,
      FUNCDEF));
    b.rule(IF_STATEMENT).is(IF, "cond", COLON, BLOCK, b.optional(INDENTATION, ELSE, COLON, BLOCK));
    b.rule(BLOCK).is(
      NEWLINE,
      indent,
      b.firstOf(
        STATEMENTS,
        // Cancel side-effects of action "indent", if parser fails to parse statements:
        b.sequence(dedent, b.nothing())),
      dedent);
    b.rule(FUNCDEF).is(DEF, "funcname", LEFT_PARENTHESIS, b.optional(ARGLIST), RIGHT_PARENTHESIS, COLON, BLOCK);
    b.rule(ARGLIST).is("arg", b.zeroOrMore(COMMA, "arg"));

    return b.build();
  }

  private static class State {
    IndentationLevel indentationLevel = new IndentationLevel();
    int implicitLineJoining = 0;
  }

  private static class IndentationLevel {
    IndentationLevel parent;
    int value;

    public IndentationLevel() {
      this.parent = null;
      this.value = 0;
    }

    public IndentationLevel(IndentationLevel parent) {
      this.parent = parent;
      this.value = -1;
    }
  }

  private static abstract class Action extends NativeExpression {
    @Override
    public final void execute(Machine machine) {
      execute();
      machine.jump(1);
    }

    public abstract void execute();
  }

  private static abstract class Predicate extends NativeExpression {
    @Override
    public final void execute(Machine machine) {
      if (check(machine)) {
        machine.jump(1);
      } else {
        machine.backtrack();
      }
    }

    public abstract boolean check(Machine machine);
  }

}
