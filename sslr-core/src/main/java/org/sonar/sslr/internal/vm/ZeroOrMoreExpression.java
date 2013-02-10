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
package org.sonar.sslr.internal.vm;

// TODO should contribute all nodes to parent
public class ZeroOrMoreExpression extends AbstractCompilableMatcher implements CompilableMatcher {

  private final CompilableMatcher subExpression;

  public ZeroOrMoreExpression(CompilableMatcher subExpression) {
    this.subExpression = subExpression;
  }

  /**
   * <pre>
   * L1: Choice L2
   * subExpression
   * Commit L1
   * L2: ...
   * </pre>
   *
   * TODO paper says that it can be optimized by introduction of new instruction PartialCommit
   */
  public Instr[] compile() {
    Instr[] p = subExpression.compile();
    Instr[] result = new Instr[p.length + 2];
    result[0] = Instr.choice(p.length + 2);
    System.arraycopy(p, 0, result, 1, p.length);
    result[p.length + 1] = Instr.commitVerify(-1 - p.length);
    return result;
  }

}
