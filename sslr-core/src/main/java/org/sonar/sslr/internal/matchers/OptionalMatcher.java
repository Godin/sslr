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
 * A {@link Matcher} that tries its submatcher once against the input.
 * Always succeeds.
 */
public class OptionalMatcher implements Matcher {

  private final Matcher subMatcher;

  public OptionalMatcher(Matcher subMatcher) {
    this.subMatcher = subMatcher;
  }

  public boolean match(MatcherContext context) {
    context.getSubContext(subMatcher).runMatcher();
    context.skipNode();
    return true;
  }

  /**
   * Compiles this expression into a set of instructions:
   * </pre>
   * Choice L1
   * subExpression
   * Commit L1
   * L1: ...
   * </pre>
   */
  public Instr[] compile() {
    // not described in paper
    Instr[] instr = subMatcher.compile();
    Instr[] result = new Instr[instr.length + 2];
    result[0] = Instr.choice(result.length);
    System.arraycopy(instr, 0, result, 1, instr.length);
    result[instr.length + 1] = Instr.commit(1);
    return result;
  }

}
