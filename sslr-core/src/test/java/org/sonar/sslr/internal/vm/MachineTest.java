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
import org.mockito.Mockito;
import org.sonar.sslr.grammar.GrammarException;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.internal.vm.NativeMatcher.NativeMatcherContext;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MachineTest {

  @Rule
  public Timeout timeout = new Timeout(5000);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test_init() {
    Instr[] instructions = {};
    Machine machine = new Machine("", instructions);
    assertThat(machine.getAddress()).isEqualTo(0);
    assertThat(machine.isResult()).isFalse();
    assertThat(machine.isRunning()).isTrue();
  }

  @Test
  public void test_end() {
    Instr[] instructions = {
      Instr.end()
    };
    Machine machine = new Machine("", instructions);
    machine.executeInstruction();
    assertThat(machine.isRunning()).isFalse();
    assertThat(machine.isResult()).isTrue();
  }

  @Test
  public void test_call_return() {
    Instr[] instructions = {
      Instr.call(2, mock(AbstractCompilableMatcher.class)),
      Instr.end(),
      Instr.ret()
    };
    Machine machine = new Machine("", instructions);
    machine.executeInstruction();
    assertThat(machine.isRunning()).isTrue();
    assertThat(machine.getAddress()).isEqualTo(2);
    // TODO check state of stack
    machine.executeInstruction();
    assertThat(machine.isRunning()).isTrue();
    assertThat(machine.getAddress()).isEqualTo(1);
    // TODO check state of stack
  }

  @Test
  public void test_native_call() {
    NativeMatcher target = mock(NativeMatcher.class);
    when(target.execute(Mockito.any(NativeMatcherContext.class))).thenReturn(true);
    Instr[] instructions = {
      Instr.native_call(target)
    };
    Machine machine = new Machine("", instructions);
    machine.executeInstruction();
    assertThat(machine.isRunning()).isTrue();
    assertThat(machine.getAddress()).isEqualTo(1);
    verify(target).execute(Mockito.any(NativeMatcherContext.class));
    // TODO check that node created
  }

  @Test
  public void test_native_call_failed() {
    NativeMatcher target = mock(NativeMatcher.class);
    when(target.execute(Mockito.any(NativeMatcherContext.class))).thenReturn(false);
    Instr[] instructions = {
      Instr.native_call(target)
    };
    Machine machine = new Machine("", instructions);
    machine.executeInstruction();
    assertThat(machine.isRunning()).isFalse();
    assertThat(machine.getAddress()).isEqualTo(0);
    verify(target).execute(Mockito.any(NativeMatcherContext.class));
    // TODO check stack
  }

  @Test
  public void test_end_of_input() {
    Instr[] instructions = {
      Instr.endOfInput()
    };
    Machine machine = new Machine("", instructions);
    machine.executeInstruction();
    assertThat(machine.isRunning()).isTrue();
    assertThat(machine.getAddress()).isEqualTo(1);
  }

  @Test
  public void test_end_of_input_failed() {
    Instr[] instructions = {
      Instr.endOfInput()
    };
    Machine machine = new Machine("a", instructions);
    machine.executeInstruction();
    assertThat(machine.isRunning()).isFalse();
    assertThat(machine.getAddress()).isEqualTo(0);
    // TODO check stack
  }

  @Test
  public void test_choice_commit() {
    Instr[] instructions = {
      Instr.choice(2),
      Instr.commit(2)
    };
    Machine machine = new Machine("a", instructions);
    machine.executeInstruction();
    assertThat(machine.isRunning()).isTrue();
    assertThat(machine.getAddress()).isEqualTo(1);
    // TODO check stack

    machine.executeInstruction();
    assertThat(machine.isRunning()).isTrue();
    assertThat(machine.getAddress()).isEqualTo(3);
    // TODO check stack
  }

  @Test
  public void test_fail() {
    Instr[] instructions = {
      Instr.fail()
    };
    Machine machine = new Machine("a", instructions);
    machine.executeInstruction();
    assertThat(machine.isRunning()).isFalse();
    assertThat(machine.isResult()).isFalse();
    // TODO check backtrack
  }

  @Test
  public void test_incorrect_opcode() {
    Instr[] instructions = {Instr.openCall(mock(AbstractCompilableMatcher.class))};
    thrown.expect(UnsupportedOperationException.class);
    new Machine("", instructions).execute();
  }

  @Test
  public void should_detect_infinite_recursion() {
    RuleExpression matcher = mock(RuleExpression.class);
    GrammarRuleKey ruleKey = mock(GrammarRuleKey.class);
    when(matcher.getRuleKey()).thenReturn(ruleKey);
    Instr[] instructions = {
      Instr.call(2, matcher),
      Instr.end(),
      Instr.call(0, matcher),
      Instr.ret()
    };
    thrown.expect(GrammarException.class);
    thrown.expectMessage("Left recursion has been detected, involved rule: " + ruleKey);
    new Machine("", instructions).execute();
  }

}
