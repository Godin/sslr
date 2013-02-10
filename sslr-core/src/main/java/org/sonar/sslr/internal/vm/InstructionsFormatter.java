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

import org.sonar.sslr.internal.vm.Instr.Opcode;

public class InstructionsFormatter {

  public String format(Instr[] instructions) {
    int label = 0;
    int[] labels = new int[instructions.length];
    for (int i = 0; i < instructions.length; i++) {
      if (instructions[i].getOpcode() != Opcode.CHAR && instructions[i].getOffset() != 0) {
        int j = i + instructions[i].getOffset();
        if (labels[j] == 0) {
          label++;
          labels[j] = label;
        }
      }
    }

    StringBuilder sb = new StringBuilder();

    int padding = Integer.toString(label).length();
    String labelFormat = "L%1$" + padding + "d: ";

    for (int i = 0; i < instructions.length; i++) {
      Instr instruction = instructions[i];
      if (labels[i] != 0) {
        sb.append(String.format(labelFormat, labels[i]));
      } else {
        for (int j = 0; j < padding + 3; j++) {
          sb.append(' ');
        }
      }
      sb.append(instructions[i].getOpcode());
      if (instruction.getOpcode() == Opcode.CHAR) {
        sb.append(' ').append((char) instruction.getOffset());
      } else if (instructions[i].getOffset() != 0) {
        int j = i + instructions[i].getOffset();
        sb.append(" L").append(labels[j]);
      }
      sb.append('\n');
    }

    return sb.toString();
  }

}
