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
import org.sonar.sslr.internal.vm.Instr;

import java.util.List;

/**
 * A {@link Matcher} trying all of its submatchers in sequence.
 * Succeeds when the first submatcher succeeds.
 */
public class FirstOfMatcher implements Matcher {

  private final Matcher[] subMatchers;

  public FirstOfMatcher(Matcher... subMatchers) {
    this.subMatchers = subMatchers;
  }

  public boolean match(MatcherContext context) {
    for (Matcher subMatcher : subMatchers) {
      if (context.getSubContext(subMatcher).runMatcher()) {
        context.skipNode();
        return true;
      }
    }
    return false;
  }

  /**
   * <pre>
   * Choice L1
   * subExpression[0]
   * Commit E
   * L1: Choice L2
   * subExpression[1]
   * Commit E
   * L2: Choice L3
   * subExpression[2]
   * Commit E
   * L3: subExpression[3]
   * E: ...
   * </pre>
   */
  public Instr[] compile() {
    int[] offsets = new int[subMatchers.length - 1];
    List<Instr> result = Lists.newArrayList();
    for (int i = 0; i < subMatchers.length - 1; i++) {
      // add placeholder for "Choice"
      result.add(null);
      // add program
      addAll(result, subMatchers[i].compile());
      // add placeholder for "Commit"
      result.add(null);
      offsets[i] = result.size();
    }
    // add last program
    addAll(result, subMatchers[subMatchers.length - 1].compile());

    // replace placholders
    int index = 0;
    for (int i = 0; i < subMatchers.length - 1; i++) {
      while (result.get(index) != null) {
        index++;
      }
      result.set(index, Instr.choice(offsets[i] - index));
      while (result.get(index) != null) {
        index++;
      }
      result.set(index, Instr.commit(result.size() - index));
    }

    return result.toArray(new Instr[result.size()]);
  }

  private static void addAll(List<Instr> list, Instr[] array) {
    for (Instr i : array) {
      list.add(i);
    }
  }

}
