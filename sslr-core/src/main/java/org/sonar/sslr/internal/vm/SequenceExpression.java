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

import com.google.common.collect.Lists;

import java.util.List;

// TODO should contribute all nodes to parent
public class SequenceExpression extends AbstractCompilableMatcher implements CompilableMatcher {

  private final CompilableMatcher[] subExpressions;

  public SequenceExpression(CompilableMatcher... subExpressions) {
    this.subExpressions = subExpressions;
  }

  /**
   * <pre>
   * subExpressions[0]
   * subExpressions[1]
   * subExpressions[2]
   * ...
   * </pre>
   */
  public Instr[] compile() {
    List<Instr> result = Lists.newArrayList();
    for (CompilableMatcher subExpression : subExpressions) {
      addAll(result, subExpression.compile());
    }
    return result.toArray(new Instr[result.size()]);
  }

  private static void addAll(List<Instr> list, Instr[] array) {
    for (Instr i : array) {
      list.add(i);
    }
  }

}
