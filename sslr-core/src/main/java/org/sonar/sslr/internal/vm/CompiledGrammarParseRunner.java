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

import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.internal.matchers.ImmutableInputBuffer;
import org.sonar.sslr.internal.matchers.InputBuffer;
import org.sonar.sslr.parser.ParsingResult;

public class CompiledGrammarParseRunner {

  private final Instr[] instructions;

  public CompiledGrammarParseRunner(CompiledGrammar grammar, GrammarRuleKey ruleKey) {
    Instr[] instr = grammar.getInstructions();
    this.instructions = new Instr[instr.length + 2];

    // TODO no need to create those instructions - just ask machine to push element into the stack before execution
    instructions[0] = Instr.call(grammar.getOffset(ruleKey) + 2, grammar.getMatcher(ruleKey));
    instructions[1] = Instr.end();
    System.arraycopy(instr, 0, instructions, 2, instr.length);
  }

  public ParsingResult parse(char[] input) {
    InputBuffer inputBuffer = new ImmutableInputBuffer(input);
    Machine machine = new Machine(input, instructions);
    boolean matched = machine.execute();
    return new ParsingResult(inputBuffer, matched, matched ? machine.getNode() : null, null);
  }

}
