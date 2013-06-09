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
package org.sonar.sslr.experiments.lexer;

import com.google.common.base.Preconditions;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.TokenType;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.internal.vm.Machine;
import org.sonar.sslr.internal.vm.NativeExpression;
import org.sonar.sslr.internal.vm.ParsingExpression;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokens {

  /**
   * Note that pattern must be consistent with order of values in {@link Type}.
   */
  private final Matcher tokenMatcher = Pattern.compile("([0-9]++)|(\\+)|(-)|(/)|(\\*)|(\\()|(\\))").matcher("");

  public final ParsingExpression number = token(Type.NUMBER);
  public final ParsingExpression plus = token(Type.PLUS);
  public final ParsingExpression minus = token(Type.MINUS);
  public final ParsingExpression div = token(Type.DIV);
  public final ParsingExpression mul = token(Type.MUL);
  public final ParsingExpression leftParenthesis = token(Type.LEFT_PARENTHESIS);
  public final ParsingExpression rightParenthesis = token(Type.RIGHT_PARENTHESIS);

  private final Token ERR = new Token(null, 0);

  /**
   * Cache of tokens seen by lexer. Indexed by the position of the first character in token.
   * {@link #ERR} indicates that given position is occupied by the token, which starts before this position.
   */
  private Token[] tokens;

  private int end;

  public final ParsingExpression init = new NativeExpression() {
    @Override
    public void execute(Machine machine) {
      tokens = new Token[machine.length()];
      end = readWhitespace(machine, 0);
      machine.advanceIndex(end);
      machine.jump(1);
    }
  };

  public final ParsingExpression destroy = new NativeExpression() {
    @Override
    public void execute(Machine machine) {
      tokens = null;
      end = 0;
      machine.jump(1);
    }
  };

  public ParsingExpression token(final Type type) {
    return new NativeExpression() {
      @Override
      public void execute(Machine machine) {
        if (machine.length() > 0) {
          Token token = readToken(machine);
          if (token.type == type) {
            machine.createLeafNode(type, token.length);
            machine.jump(1);
            return;
          }
        }
        machine.backtrack();
      }
    };
  }

  private Token readToken(Machine machine) {
    // following check guarantees that lexer reads tokens strictly sequentially:
    Preconditions.checkState(machine.getIndex() <= end);
    Token result = tokens[machine.getIndex()];
    if (result != null) {
      return result;
    }
    if (!tokenMatcher.reset(machine).lookingAt()) {
      throw new AssertionError();
    }
    for (Type type : Type.values()) {
      if (tokenMatcher.start(type.ordinal() + 1) != -1) {
        result = new Token(type, readWhitespace(machine, tokenMatcher.end()));
        tokens[machine.getIndex()] = result;
        end = machine.getIndex() + result.length;
        Arrays.fill(tokens, machine.getIndex() + 1, end, ERR);
        return result;
      }
    }
    throw new AssertionError();
  }

  private int readWhitespace(Machine machine, int offset) {
    while (offset < machine.length() && machine.charAt(offset) == ' ') {
      offset++;
    }
    return offset;
  }

  private static class Token {
    final Type type;
    final int length;

    Token(Type type, int length) {
      this.type = type;
      this.length = length;
    }
  }

  public enum Type implements TokenType, org.sonar.sslr.internal.matchers.Matcher, GrammarRuleKey {

    NUMBER,
    PLUS,
    MINUS,
    DIV,
    MUL,
    LEFT_PARENTHESIS,
    RIGHT_PARENTHESIS;

    public String getName() {
      return name();
    }

    public String getValue() {
      return null;
    }

    public boolean hasToBeSkippedFromAst(AstNode node) {
      return false;
    }
  }

}
