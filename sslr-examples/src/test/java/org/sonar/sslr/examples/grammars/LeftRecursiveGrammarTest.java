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
package org.sonar.sslr.examples.grammars;

import com.sonar.sslr.api.Grammar;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.sslr.grammar.GrammarException;
import org.sonar.sslr.parser.ParseRunner;

import static org.sonar.sslr.tests.Assertions.assertThat;

public class LeftRecursiveGrammarTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test() {
    assertThat(LeftRecursiveGrammar.simple().rule(LeftRecursiveGrammar.A))
      .matches("b")
      .matches("ba")
      .matches("baa");

    assertThat(LeftRecursiveGrammar.usual().rule(LeftRecursiveGrammar.A))
      .matches("n")
      .matches("n+n")
      .matches("n+n+n")
      .matches("n+(n-n+n)-n");

    assertThat(LeftRecursiveGrammar.complex().rule(LeftRecursiveGrammar.A))
      .matches("n+n+n")
      .matches("n-n-n");

    assertThat(LeftRecursiveGrammar.leftAndRightRecursive().rule(LeftRecursiveGrammar.A))
      .matches("n")
      .matches("n+n")
      .matches("n+n+n");
  }

  @Test
  public void lValues() {
    assertThat(LeftRecursiveGrammar.lValues().rule(LeftRecursiveGrammar.A))
      .matches("x")
      .matches("x.x")
      .matches("x.x.x")
      .matches("x(n).x")
      .matches("x(n)(n).x")
      .matches("x(n)(n).x(n).x");
  }

  @Test
  public void memo() {
    assertThat(LeftRecursiveGrammar.memo().rule(LeftRecursiveGrammar.A))
      .matches("n")
      .matches("n+n")
      .matches("n+n+n");
  }

  @Test
  public void ford() {
    assertThat(LeftRecursiveGrammar.ford1().rule(LeftRecursiveGrammar.A))
      .notMatches("");
    assertThat(LeftRecursiveGrammar.ford2().rule(LeftRecursiveGrammar.A))
      .matches("a")
      .notMatches("aa");
    assertThat(LeftRecursiveGrammar.ford3().rule(LeftRecursiveGrammar.A))
      .notMatches("a");
    assertThat(LeftRecursiveGrammar.ford3().rule(LeftRecursiveGrammar.B))
      .matches("a");
    assertThat(LeftRecursiveGrammar.ford4().rule(LeftRecursiveGrammar.A))
      .matches("aaa")
      .notMatches("aaaa");
  }

  @Ignore
  @Test
  public void should_detect_immediate_left_recursion() {
    Grammar grammar = LeftRecursiveGrammar.immediateLeftRecursion();
    thrown.expect(GrammarException.class);
    thrown.expectMessage("Left recursion has been detected, involved rule: " + LeftRecursiveGrammar.A);
    new ParseRunner(grammar.rule(LeftRecursiveGrammar.A)).parse("".toCharArray());
  }

  @Test
  public void eliminated_immediate_left_recursion() {
    Grammar grammar = LeftRecursiveGrammar.eliminatedImmediateLeftRecursion();
    assertThat(grammar.rule(LeftRecursiveGrammar.A))
      .matches("s1")
      .matches("s2")
      .matches("s1t1")
      .matches("s1t2")
      .matches("s1t1t2")
      .matches("s1t2t1")
      .matches("s2t1")
      .matches("s2t2")
      .matches("s2t1t2")
      .matches("s2t2t1");
  }

  @Ignore
  @Test
  public void should_detect_indirect_left_recursion() {
    Grammar grammar = LeftRecursiveGrammar.indirectLeftRecursion();
    thrown.expect(GrammarException.class);
    thrown.expectMessage("Left recursion has been detected, involved rule: " + LeftRecursiveGrammar.B);
    new ParseRunner(grammar.rule(LeftRecursiveGrammar.A)).parse("".toCharArray());
  }

  @Test
  public void eliminated_indirect_left_recursion() {
    Grammar grammar = LeftRecursiveGrammar.eliminatedIndirectLeftRecursion();
    assertThat(grammar.rule(LeftRecursiveGrammar.A))
      .matches("s2t1")
      .matches("s2t1t2t1")
      .matches("s2t1t2t1t2t1")
      .matches("s1")
      .matches("s1t2t1")
      .matches("s1t2t1t2t1");
  }

}
