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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.sonar.sslr.grammar.GrammarException;
import org.sonar.sslr.internal.matchers.Matcher;
import org.sonar.sslr.internal.matchers.ParseNode;
import org.sonar.sslr.internal.vm.NativeMatcher.NativeMatcherContext;

import java.util.List;

public class Machine implements NativeMatcherContext {

  private final char[] input;
  private final Instr[] instructions;

  private int index;
  private boolean running;
  private boolean result;
  private int address;

  @VisibleForTesting
  public Machine(String input, Instr[] instructions) {
    this(input.toCharArray(), instructions);
  }

  public Machine(char[] input, Instr[] instructions) {
    this.input = input;
    this.instructions = instructions;
  }

  private StackElement stack;

  public static class StackElement {
    public final StackElement parent;
    public final List<ParseNode> subNodes;

    public StackElement child;

    public int address;
    public int index;
    public AbstractCompilableMatcher matcher;

    public StackElement(StackElement parent) {
      this.parent = parent;
      this.subNodes = Lists.newArrayList();
    }

    public boolean isBacktrack() {
      return matcher == null;
    }
  }

  private void push(int address) {
    if (stack.child == null) {
      stack.child = new StackElement(stack);
    }
    stack = stack.child;
    stack.subNodes.clear();
    stack.address = address;
    stack.index = index;
  }

  private void pushReturn(int address, AbstractCompilableMatcher matcher) {
    push(address);
    stack.matcher = matcher;
  }

  private void pushBacktrack(int address) {
    push(address);
    stack.matcher = null;
  }

  public boolean execute() {
    stack = new StackElement(null);

    index = 0;
    address = 0;
    running = true;
    while (running) {
      Instr instruction = instructions[address];
      switch (instruction.getOpcode()) {
        case JUMP:
          address += instruction.getOffset();
          break;
        case CALL:
          pushReturn(address + 1, instruction.getMatcher());
          address += instruction.getOffset();
          break;
        case RETURN:
          // return from rule, so should create node
          createNode();

          address = stack.address;
          stack = stack.parent;
          break;
        case NATIVE_CALL:
          NativeMatcher matcher = (NativeMatcher) instruction.getMatcher();
          if (matcher.execute(this)) {
            address++;
            // all native matchers (String, Pattern) should create node
            createNode(matcher);
          } else {
            fail();
          }
          break;
        case END_OF_INPUT:
          if (index != input.length) {
            fail();
          }
          address++;
          break;
        case CHOICE:
          pushBacktrack(address + instruction.getOffset());
          address++;
          break;
        case COMMIT_VERIFY:
          if (stack.parent.index == index) {
            throw new GrammarException("The inner part of ZeroOrMore must not allow empty matches");
          }
        case COMMIT:
          // Preconditions.checkState(stack.isBacktrack()); // should be always backtrack

          // should contribute all created nodes to parent
          stack.parent.subNodes.addAll(stack.subNodes);

          stack = stack.parent;
          address += instruction.getOffset();
          break;
        case FAIL:
          fail();
          break;
        case FAIL_TWICE:
          // remove pending alternative pushed by Choice instruction
          stack = stack.parent;
          fail();
          break;
        case BACK_COMMIT:
          // restore state
          index = stack.index;
          stack = stack.parent;
          // jump
          address += instruction.getOffset();
          break;
        case END:
          running = false;
          result = true;
          break;
        case CHAR:
          char ch = (char) instruction.getOffset();
          if (index < input.length && input[index] == ch) {
            index++;
            address++;
            createNode(instruction.getMatcher());
          } else {
            fail();
          }
          break;
        default:
          throw new UnsupportedOperationException();
      }
    }
    return result;
  }

  private void createNode() {
    ParseNode node = new ParseNode(stack.index, index, stack.subNodes, stack.matcher);
    stack.parent.subNodes.add(node);
  }

  private void createNode(Matcher matcher) {
    ParseNode node = new ParseNode(stack.index, index, stack.subNodes, matcher);
    stack.subNodes.add(node);
  }

  private void fail() {
    // pop any return addresses from the top of the stack
    while (!stack.isBacktrack()) {
      stack = stack.parent;
    }
    if (stack.parent == null) {
      running = false;
      result = false;
    } else {
      StackElement backtrack = stack;
      index = backtrack.index;
      address = backtrack.address;
      stack = stack.parent;
    }
  }

  // CharSequence

  public CharSequence subSequence(int start, int end) {
    throw new UnsupportedOperationException();
  }

  public int length() {
    return input.length - index;
  }

  public char charAt(int offset) {
    return input[index + offset];
  }

  public void advanceIndex(int length) {
    index += length;
  }

  public ParseNode getNode() {
    return stack.subNodes.get(0);
  }

}
