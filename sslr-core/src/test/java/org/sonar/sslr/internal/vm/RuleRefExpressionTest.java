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
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.internal.vm.Instr.Opcode;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class RuleRefExpressionTest {

  @Test
  public void test_compile() {
    RuleRefExpression expression = new RuleRefExpression(mock(GrammarRuleKey.class));
    Instr[] instructions = expression.compile();
    assertThat(instructions).hasSize(1);
    assertThat(instructions[0].getOpcode()).isEqualTo(Opcode.OPEN_CALL);
    assertThat(instructions[0].getMatcher()).isSameAs(expression);
  }

}
