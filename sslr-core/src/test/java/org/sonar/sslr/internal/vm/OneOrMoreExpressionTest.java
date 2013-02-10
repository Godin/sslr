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
import org.junit.rules.Timeout;
import org.sonar.sslr.grammar.GrammarException;
import org.sonar.sslr.internal.matchers.OneOrMoreMatcher;
import org.sonar.sslr.internal.matchers.OptionalMatcher;

import static org.fest.assertions.Assertions.assertThat;

public class OneOrMoreExpressionTest {

  @Rule
  public Timeout timeout = new Timeout(5000);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test() {
    CharExpression a = new CharExpression('a');
    Instr[] instructions = Instr.appendEnd(new OneOrMoreMatcher(a).compile());
    assertThat(new Machine("", instructions).execute()).isFalse();
    assertThat(new Machine("a", instructions).execute()).isTrue();
    assertThat(new Machine("aa", instructions).execute()).isTrue();
  }

  @Test
  public void should_check_that_moves_forward() {
    Instr[] instructions = Instr.appendEnd(new OneOrMoreMatcher(new OptionalMatcher(new CharExpression('a'))).compile());
    thrown.expect(GrammarException.class);
    thrown.expectMessage("The inner part of ZeroOrMore must not allow empty matches");
    new Machine("", instructions).execute();
  }

}
