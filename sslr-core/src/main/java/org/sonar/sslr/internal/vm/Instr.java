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

public class Instr {

  public enum Opcode {
    CHAR,
    JUMP,
    CALL,
    OPEN_CALL,
    RETURN,
    FAIL,
    CHOICE,
    COMMIT,
    COMMIT_VERIFY,
    END,
    END_OF_INPUT,
    NATIVE_CALL,
    FAIL_TWICE,
    BACK_COMMIT;
  }

  private final Opcode opcode;
  private final int offset;
  private final AbstractCompilableMatcher matcher;

  public Instr(Opcode opcode, int offset) {
    this.opcode = opcode;
    this.offset = offset;
    this.matcher = null;
  }

  public Instr(Opcode opcode, int offset, AbstractCompilableMatcher matcher) {
    this.opcode = opcode;
    this.offset = offset;
    this.matcher = matcher;
  }

  public Opcode getOpcode() {
    return opcode;
  }

  public int getOffset() {
    return offset;
  }

  public AbstractCompilableMatcher getMatcher() {
    return matcher;
  }

  public static Instr ch(char ch, AbstractCompilableMatcher matcher) {
    return new Instr(Opcode.CHAR, ch, matcher);
  }

  public static Instr end() {
    return new Instr(Opcode.END, 0);
  }

  public static Instr choice(int offset) {
    return new Instr(Opcode.CHOICE, offset);
  }

  public static Instr commit(int offset) {
    return new Instr(Opcode.COMMIT, offset);
  }

  public static Instr commitVerify(int offset) {
    return new Instr(Opcode.COMMIT_VERIFY, offset);
  }

  public static Instr fail() {
    return new Instr(Opcode.FAIL, 0);
  }

  public static Instr failTwice() {
    return new Instr(Opcode.FAIL_TWICE, 0);
  }

  public static Instr backCommit(int offset) {
    return new Instr(Opcode.BACK_COMMIT, offset);
  }

  public static Instr endOfInput() {
    return new Instr(Opcode.END_OF_INPUT, 0);
  }

  public static Instr ret() {
    return new Instr(Opcode.RETURN, 0);
  }

  public static Instr call(int offset, AbstractCompilableMatcher matcher) {
    return new Instr(Opcode.CALL, offset, matcher);
  }

  public static Instr openCall(AbstractCompilableMatcher matcher) {
    return new Instr(Opcode.OPEN_CALL, 0, matcher);
  }

  public static Instr jump(int offset) {
    return new Instr(Opcode.JUMP, offset);
  }

  public static Instr native_call(AbstractCompilableMatcher matcher) {
    return new Instr(Opcode.NATIVE_CALL, 0, matcher);
  }

  public static Instr[] appendEnd(Instr[] instructions) {
    Instr[] result = new Instr[instructions.length + 1];
    System.arraycopy(instructions, 0, result, 0, instructions.length);
    result[instructions.length] = Instr.end();
    return result;
  }

}
