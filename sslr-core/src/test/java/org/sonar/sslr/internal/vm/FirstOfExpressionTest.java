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

import static org.fest.assertions.Assertions.assertThat;

public class FirstOfExpressionTest {

  @Test(timeout = 5000)
  public void test() {
    CharExpression a = new CharExpression('a');
    CharExpression b = new CharExpression('b');
    CharExpression c = new CharExpression('c');
    Instr[] instructions = Instr.appendEnd(new FirstOfExpression(a, b, c).compile());
    assertThat(new Machine("a", instructions).execute()).isTrue();
    assertThat(new Machine("b", instructions).execute()).isTrue();
    assertThat(new Machine("c", instructions).execute()).isTrue();
    assertThat(new Machine("d", instructions).execute()).isFalse();
  }

}
