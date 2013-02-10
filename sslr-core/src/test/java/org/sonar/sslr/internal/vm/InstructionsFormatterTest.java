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

public class InstructionsFormatterTest {

  @Test
  public void test() {
    InstructionsFormatter formatter = new InstructionsFormatter();
    Instr[] instructions = new Instr[] {Instr.jump(3), Instr.jump(2), Instr.jump(-1), Instr.ch('a', null), Instr.end()};
    String expected = new StringBuilder()
        .append("    JUMP L1\n")
        .append("L2: JUMP L1\n")
        .append("    JUMP L2\n")
        .append("L1: CHAR a\n")
        .append("    END\n")
        .toString();
    assertThat(formatter.format(instructions)).isEqualTo(expected);
  }

}
