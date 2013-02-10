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

import org.junit.Test;
import org.sonar.sslr.internal.matchers.SequenceMatcher;

import static org.fest.assertions.Assertions.assertThat;

public class SequenceExpressionTest {

  @Test(timeout = 5000)
  public void test() {
    CharExpression p1 = new CharExpression('b');
    CharExpression p2 = new CharExpression('a');
    CharExpression p3 = new CharExpression('r');
    Instr[] instr = Instr.appendEnd(new SequenceMatcher(p1, p2, p3).compile());
    assertThat(new Machine("bar", instr).execute()).isTrue();
    assertThat(new Machine("foo", instr).execute()).isFalse();
  }

}
