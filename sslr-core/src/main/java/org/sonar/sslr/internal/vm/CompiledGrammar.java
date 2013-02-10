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

import java.util.Map;

public class CompiledGrammar {

  private final Map<GrammarRuleKey, RuleExpression> rules;
  // TODO Is there more efficient way to store this information? I.e. without primitive wrappers.
  private final Map<GrammarRuleKey, Integer> offsets;
  private final Instr[] instructions;

  public CompiledGrammar(Instr[] instructions, Map<GrammarRuleKey, RuleExpression> rules, Map<GrammarRuleKey, Integer> offsets) {
    this.instructions = instructions;
    this.rules = rules;
    this.offsets = offsets;
  }

  public int getOffset(GrammarRuleKey ruleKey) {
    return offsets.get(ruleKey);
  }

  public AbstractCompilableMatcher getMatcher(GrammarRuleKey ruleKey) {
    return rules.get(ruleKey);
  }

  public Instr[] getInstructions() {
    return instructions;
  }

}
