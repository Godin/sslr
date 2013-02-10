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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.sslr.grammar.GrammarException;
import org.sonar.sslr.grammar.GrammarRuleKey;

public class LeftRecursionTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private enum ImmediateLeftRecursion implements GrammarRuleKey {
    A,
    T;

    private static CompiledGrammar create() {
      VmGrammarBuilder b = new VmGrammarBuilder();
      b.rule(A).is(b.firstOf(
          b.sequence(A, T),
          T));
      b.rule(T).is("t");
      return b.build();
    }
  }

  @Test
  public void test_immediate_left_recursion() {
    CompiledGrammar g = ImmediateLeftRecursion.create();
    CompiledGrammarParseRunner parseRunner = new CompiledGrammarParseRunner(g, ImmediateLeftRecursion.A);
    thrown.expect(GrammarException.class);
    thrown.expectMessage("Left recursion has been detected, involved rule: " + IndirectLeftRecursion.A);
    parseRunner.parse("".toCharArray());
  }

  private enum IndirectLeftRecursion implements GrammarRuleKey {
    A,
    T1,
    S1,
    B,
    T2,
    S2;

    private static CompiledGrammar create() {
      VmGrammarBuilder b = new VmGrammarBuilder();
      b.rule(A).is(b.firstOf(
          b.sequence(B, T1),
          S1));
      b.rule(B).is(b.firstOf(
          b.sequence(A, T2),
          S2));
      b.rule(T1).is("t1");
      b.rule(S1).is("s1");
      b.rule(T2).is("t2");
      b.rule(S2).is("s2");
      return b.build();
    }
  }

  @Test
  public void test_indirect_left_recursion() {
    CompiledGrammar g = IndirectLeftRecursion.create();
    CompiledGrammarParseRunner parseRunner = new CompiledGrammarParseRunner(g, IndirectLeftRecursion.A);
    thrown.expect(GrammarException.class);
    thrown.expectMessage("Left recursion has been detected, involved rule: " + IndirectLeftRecursion.A);
    parseRunner.parse("".toCharArray());
  }

}
