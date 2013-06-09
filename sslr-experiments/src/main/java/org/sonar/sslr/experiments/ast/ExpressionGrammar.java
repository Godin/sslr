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
package org.sonar.sslr.experiments.ast;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sonar.sslr.api.Grammar;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;
import org.sonar.sslr.internal.vm.Machine;
import org.sonar.sslr.internal.vm.NativeExpression;
import org.sonar.sslr.internal.vm.ParsingExpression;

import java.util.List;

public enum ExpressionGrammar implements GrammarRuleKey {

  EXPRESSION,
  ADDITIVE_EXPRESSION,
  MULTIPLICATIVE_EXPRESSION,
  NUMBER;

  public static Grammar create() {
    final State state = new State();
    ParsingExpression enter = new Action() {
      @Override
      public void execute() {
        state.nodes = new Nodes(state.nodes);
      }
    };
    ParsingExpression leave = new Action() {
      @Override
      public void execute() {
        state.nodes = state.nodes.parent;
      }
    };
    ParsingExpression createBinaryOpAstNode = new Action() {
      @Override
      public void execute() {
        List<CustomAstNode> list = state.nodes.list;
        CustomAstNode node = list.get(0);
        // skip this node, if it has only one child, otherwise build sub-tree with left associativity
        if (list.size() > 1) {
          for (int i = 1; i < list.size(); i++) {
            node = new BinaryOpAstNode(node, list.get(i));
          }
        }
        state.nodes.parent.list.add(node);
      }
    };
    ParsingExpression createNumberAstNode = new Action() {
      @Override
      public void execute() {
        NumberAstNode node = new NumberAstNode();
        state.nodes.list.add(node);
      }
    };
    ParsingExpression done = new Action() {
      @Override
      public void execute() {
        Preconditions.checkState(state.nodes.list.size() == 1);
        System.out.println(state.nodes.list.get(0));
      }
    };

    LexerlessGrammarBuilder b = LexerlessGrammarBuilder.create();

    b.rule(EXPRESSION).is(ADDITIVE_EXPRESSION, b.endOfInput(), done);
    b.rule(ADDITIVE_EXPRESSION).is(wrap(b, enter, b.sequence(
      MULTIPLICATIVE_EXPRESSION,
      b.zeroOrMore(b.firstOf("+", "-"), MULTIPLICATIVE_EXPRESSION),
      createBinaryOpAstNode
    ), leave));
    b.rule(MULTIPLICATIVE_EXPRESSION).is(wrap(b, enter, b.sequence(
      NUMBER,
      b.zeroOrMore(b.firstOf("/", "*"), NUMBER),
      createBinaryOpAstNode
    ), leave));
    b.rule(NUMBER).is(
      b.regexp("[0-9]++"),
      createNumberAstNode);

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

  private static interface CustomAstNode {
  }

  private static class NumberAstNode implements CustomAstNode {
    @Override
    public String toString() {
      return "num";
    }
  }

  private static class BinaryOpAstNode implements CustomAstNode {
    private CustomAstNode left, right;

    public BinaryOpAstNode(CustomAstNode left, CustomAstNode right) {
      this.left = left;
      this.right = right;
    }

    @Override
    public String toString() {
      return "(" + left + ", " + right + ")";
    }
  }

  private static class Nodes {
    Nodes parent;
    List<CustomAstNode> list = Lists.newArrayList();

    public Nodes() {
      this.parent = null;
    }

    public Nodes(Nodes parent) {
      this.parent = parent;
    }
  }

  private static class State {
    Nodes nodes = new Nodes();
  }

  private abstract static class Action extends NativeExpression {
    @Override
    public final void execute(Machine machine) {
      execute();
      machine.jump(1);
    }

    abstract void execute();
  }

}
