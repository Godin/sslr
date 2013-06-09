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
package org.sonar.sslr.experiments.heredoc;

import com.google.common.collect.Iterables;
import com.sonar.sslr.api.Grammar;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;
import org.sonar.sslr.internal.matchers.ParseNode;
import org.sonar.sslr.internal.vm.Machine;
import org.sonar.sslr.internal.vm.NativeExpression;
import org.sonar.sslr.internal.vm.ParsingExpression;

/**
 * Grammar, which mimics PHP heredoc syntax.
 * See <a href="http://www.php.net/manual/en/language.types.string.php#language.types.string.syntax.heredoc">PHP Language Reference</a>.
 */
public enum HeredocGrammar implements GrammarRuleKey {

  HEREDOC,
  HEREDOC_END,
  IDENTIFIER;

  public static Grammar create() {
    final State state = new State();
    ParsingExpression capture = new NativeExpression() {
      @Override
      public void execute(Machine machine) {
        state.capture = getIdentifier(machine);
        machine.jump(1);
      }
    };
    ParsingExpression check = new NativeExpression() {
      @Override
      public void execute(Machine machine) {
        if (state.capture.equals(getIdentifier(machine))) {
          machine.jump(1);
        } else {
          machine.backtrack();
        }
      }
    };

    LexerlessGrammarBuilder b = LexerlessGrammarBuilder.create();

    b.rule(HEREDOC).is(
      "<<<", IDENTIFIER, capture, "\n",
      b.zeroOrMore(b.nextNot(HEREDOC_END), b.regexp(".")),
      HEREDOC_END);
    b.rule(HEREDOC_END).is("\n", IDENTIFIER, check);
    b.rule(IDENTIFIER).is(b.regexp("[a-zA-Z]++"));

    return b.build();
  }

  private static class State {
    String capture;
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
