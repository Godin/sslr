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

import org.sonar.sslr.internal.vm.Instr;

/**
 * A {@link Matcher} that tries its submatcher against the input.
 * Succeeds if submatcher succeeds.
 */
public class TestMatcher implements Matcher {

  private final Matcher subMatcher;

  public TestMatcher(Matcher subMatcher) {
    this.subMatcher = subMatcher;
  }

  public boolean match(MatcherContext context) {
    if (!context.getSubContext(subMatcher).runMatcher()) {
      return false;
    }
    context.resetIndex();
    return true;
  }

  /**
   * Compiles this expression into a set of instructions:
   * <pre>
   * Choice L1
   * subExpression
   * BackCommit L2
   * L1: Fail
   * L2: ...
   * </pre>
   *
   * Should be noted that can be compiled without usage instruction "BackCommit":
   * <pre>
   * Choice L1
   * Choice L2
   * subExpression
   * L2: Commit L3
   * L3: Fail
   * L1: ...
   * </pre>
   */
  public Instr[] compile() {
    Instr[] instr = subMatcher.compile();
    Instr[] result = new Instr[instr.length + 3];
    result[0] = Instr.choice(result.length - 1);
    System.arraycopy(instr, 0, result, 1, instr.length);
    result[instr.length + 1] = Instr.backCommit(2);
    result[instr.length + 2] = Instr.fail();
    return result;
  }

}
