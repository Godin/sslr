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

import com.google.common.collect.Lists;
import org.sonar.sslr.internal.vm.CompilableMatcher;
import org.sonar.sslr.internal.vm.Instr;

import java.util.List;

/**
 * A {@link Matcher} that executes all of its submatchers in sequence.
 * Succeeds only if all submatchers succeed.
 */
public class SequenceMatcher implements Matcher {

  private final Matcher[] subMatchers;

  public SequenceMatcher(Matcher... subMatchers) {
    this.subMatchers = subMatchers;
  }

  public boolean match(MatcherContext context) {
    for (Matcher subMatcher : subMatchers) {
      if (!context.getSubContext(subMatcher).runMatcher()) {
        return false;
      }
    }
    context.skipNode();
    return true;
  }

  public Matcher[] getSubMatchers() {
    Matcher[] result = new Matcher[subMatchers.length];
    System.arraycopy(subMatchers, 0, result, 0, subMatchers.length);
    return result;
  }

  /**
   * Compiles this expression into a set of instructions:
   * <pre>
   * subExpressions[0]
   * subExpressions[1]
   * subExpressions[2]
   * ...
   * </pre>
   */
  public Instr[] compile() {
    List<Instr> result = Lists.newArrayList();
    for (CompilableMatcher subExpression : subMatchers) {
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
