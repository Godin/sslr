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
package org.sonar.sslr.internal.matchers;

import com.sonar.sslr.api.Trivia.TriviaKind;
import org.sonar.sslr.internal.vm.AbstractCompilableMatcher;
import org.sonar.sslr.internal.vm.Instr;

public class TriviaMatcher extends AbstractCompilableMatcher implements Matcher {

  private final Matcher subMatcher;
  private final TriviaKind triviaKind;

  public TriviaMatcher(TriviaKind triviaKind, Matcher subMatcher) {
    this.triviaKind = triviaKind;
    this.subMatcher = subMatcher;
  }

  public boolean match(MatcherContext context) {
    context.ignoreErrors();
    if (context.getSubContext(subMatcher).runMatcher()) {
      context.createNode();
      return true;
    }
    return false;
  }

  public TriviaKind getTriviaKind() {
    return triviaKind;
  }

  /**
   * Compiles this expression into a set of instructions:
   * <pre>
   * Call L1
   * Jump L2
   * L1: subExpression
   * Return
   * L2: ...
   * </pre>
   */
  public Instr[] compile() {
    Instr[] instr = subMatcher.compile();
    Instr[] result = new Instr[instr.length + 3];
    result[0] = Instr.call(2, this);
    result[1] = Instr.jump(instr.length + 2);
    System.arraycopy(instr, 0, result, 2, instr.length);
    result[instr.length + 2] = Instr.ret();
    return result;
  }

}
