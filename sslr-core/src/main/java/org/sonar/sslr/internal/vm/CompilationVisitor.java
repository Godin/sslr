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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.internal.matchers.Matcher;

import java.util.List;
import java.util.Map;
import java.util.Queue;

public class CompilationVisitor {

  public static CompiledGrammar compile(CompilableGrammarRule rule) {
    return new CompilationVisitor().doCompile(rule);
  }

  private final Queue<CompilableGrammarRule> compilationQueue = Lists.newLinkedList();
  private final Map<GrammarRuleKey, CompilableGrammarRule> matchers = Maps.newHashMap();
  private final Map<GrammarRuleKey, Integer> offsets = Maps.newHashMap();

  private CompiledGrammar doCompile(CompilableGrammarRule start) {
    List<Instruction> instructions = Lists.newArrayList();

    // Compile

    compilationQueue.add(start);
    matchers.put(start.getRuleKey(), start);

    while (!compilationQueue.isEmpty()) {
      CompilableGrammarRule rule = compilationQueue.poll();
      GrammarRuleKey ruleKey = rule.getRuleKey();

      offsets.put(ruleKey, instructions.size());
      Instruction.addAll(instructions, compile(rule.getExpression()));
      instructions.add(Instruction.ret());
    }

    // Link

    Instruction[] result = instructions.toArray(new Instruction[instructions.size()]);
    for (int i = 0; i < result.length; i++) {
      Instruction instruction = result[i];
      if (instruction instanceof RuleRefExpression) {
        RuleRefExpression expression = (RuleRefExpression) instruction;
        GrammarRuleKey ruleKey = expression.getRuleKey();
        int offset = offsets.get(ruleKey);
        result[i] = Instruction.call(offset - i, matchers.get(ruleKey));
      }
    }

    return new CompiledGrammar(result, matchers, start.getRuleKey(), offsets.get(start.getRuleKey()));
  }

  public Instruction[] compile(ParsingExpression expression) {
    if (expression instanceof SequenceExpression) {
      return compileSequence((SequenceExpression) expression);
    } else if (expression instanceof FirstOfExpression) {
      return compileFirstOf((FirstOfExpression) expression);
    } else if (expression instanceof OptionalExpression) {
      return compileOptional((OptionalExpression) expression);
    } else if (expression instanceof OneOrMoreExpression) {
      return compileOneOrMore((OneOrMoreExpression) expression);
    } else if (expression instanceof ZeroOrMoreExpression) {
      return compileZeroOrMore((ZeroOrMoreExpression) expression);
    } else if (expression instanceof NextExpression) {
      return compileNext((NextExpression) expression);
    } else if (expression instanceof NextNotExpression) {
      return compileNextNot((NextNotExpression) expression);
    } else if (expression instanceof NativeExpression) {
      return compileNative((NativeExpression) expression);
    } else if (expression instanceof CompilableGrammarRule) {
      return compileRule((CompilableGrammarRule) expression);
    } else if (expression instanceof TokenExpression) {
      return compileToken((TokenExpression) expression);
    } else if (expression instanceof TriviaExpression) {
      return compileTrivia((TriviaExpression) expression);
    } else {
      throw new IllegalArgumentException("Unexpected expression: " + expression.getClass());
    }
  }

  public Instruction[] compileRule(CompilableGrammarRule rule) {
    if (!matchers.containsKey(rule.getRuleKey())) {
      compilationQueue.add(rule);
      matchers.put(rule.getRuleKey(), rule);
    }
    return new Instruction[]{new RuleRefExpression(rule.getRuleKey())};
  }

  /**
   * Compiles {@link TriviaExpression} into a sequence of instructions:
   * <pre>
   * Call L1
   * Jump L2
   * L1: subExpression
   * Return
   * L2: ...
   * </pre>
   */
  public Instruction[] compileTrivia(TriviaExpression expression) {
    return compileTokenOrTrivia(expression, expression.subExpression);
  }

  /**
   * Compiles {@link TokenExpression} into a sequence of instructions:
   * <pre>
   * Call L1
   * Jump L2
   * L1: subExpression
   * Return
   * L2: ...
   * </pre>
   */
  public Instruction[] compileToken(TokenExpression expression) {
    return compileTokenOrTrivia(expression, expression.subExpression);
  }

  public Instruction[] compileTokenOrTrivia(Matcher expression, ParsingExpression subExpression) {
    // TODO maybe can be optimized
    Instruction[] instr = compile(subExpression);
    Instruction[] result = new Instruction[instr.length + 4];
    result[0] = Instruction.call(2, expression);
    result[1] = Instruction.jump(instr.length + 3);
    result[2] = Instruction.ignoreErrors();
    System.arraycopy(instr, 0, result, 3, instr.length);
    result[3 + instr.length] = Instruction.ret();
    return result;
  }

  /**
   * Compiles {@link SequenceExpression} into a sequence of instructions:
   * <pre>
   * subExpressions[0]
   * subExpressions[1]
   * subExpressions[2]
   * ...
   * </pre>
   */
  public Instruction[] compileSequence(SequenceExpression expression) {
    List<Instruction> result = Lists.newArrayList();
    for (ParsingExpression subExpression : expression.subExpressions) {
      Instruction.addAll(result, compile(subExpression));
    }
    return result.toArray(new Instruction[result.size()]);
  }

  /**
   * Compiles {@link FirstOfExpression} into a sequence of instructions:
   * <pre>
   * Choice L1
   * subExpression[0]
   * Commit E
   * L1: Choice L2
   * subExpression[1]
   * Commit E
   * L2: Choice L3
   * subExpression[2]
   * Commit E
   * L3: subExpression[3]
   * E: ...
   * </pre>
   */
  public Instruction[] compileFirstOf(FirstOfExpression expression) {
    ParsingExpression[] subExpressions = expression.subExpressions;

    int index = 0;
    Instruction[][] sub = new Instruction[subExpressions.length][];
    for (int i = 0; i < subExpressions.length; i++) {
      sub[i] = compile(subExpressions[i]);
      index += sub[i].length;
    }
    Instruction[] result = new Instruction[index + (subExpressions.length - 1) * 2];

    index = 0;
    for (int i = 0; i < subExpressions.length - 1; i++) {
      result[index] = Instruction.choice(sub[i].length + 2);
      System.arraycopy(sub[i], 0, result, index + 1, sub[i].length);
      index += sub[i].length + 1;
      result[index] = Instruction.commit(result.length - index);
      index++;
    }
    System.arraycopy(sub[sub.length - 1], 0, result, index, sub[sub.length - 1].length);

    return result;
  }

  /**
   * Compiles {@link OptionalExpression} into a sequence of instructions:
   * <pre>
   * Choice L1
   * subExpression
   * Commit L1
   * L1: ...
   * </pre>
   */
  public Instruction[] compileOptional(OptionalExpression expression) {
    // not described in paper
    Instruction[] instr = compile(expression.subExpression);
    Instruction[] result = new Instruction[instr.length + 2];
    result[0] = Instruction.choice(result.length);
    System.arraycopy(instr, 0, result, 1, instr.length);
    result[instr.length + 1] = Instruction.commit(1);
    return result;
  }

  /**
   * Compiles {@link OneOrMoreExpression} into a sequence of instructions:
   * <pre>
   * Choice L3
   * L1: subExpression
   * CommitVerify L2
   * L2: Choice L4
   * Jump L1
   * L3: Fail
   * L4: ....
   * </pre>
   * Should be noted that can be compiled with help of {@link ZeroOrMoreExpression}:
   * <pre>
   * subExpresson
   * L1: Choice L2
   * subExpression
   * CommitVerify L1
   * L2: ...
   * </pre>
   */
  public Instruction[] compileOneOrMore(OneOrMoreExpression expression) {
    Instruction[] sub = compile(expression.subExpression);
    Instruction[] result = new Instruction[sub.length + 5];
    result[0] = Instruction.choice(sub.length + 4);
    System.arraycopy(sub, 0, result, 1, sub.length);
    result[sub.length + 1] = Instruction.commitVerify(1);
    result[sub.length + 2] = Instruction.choice(3);
    result[sub.length + 3] = Instruction.jump(-2 - sub.length);
    result[sub.length + 4] = Instruction.backtrack();
    return result;
  }

  /**
   * Compiles this {@link ZeroOrMoreExpression} into a sequence of instructions:
   * <pre>
   * L1: Choice L2
   * subExpression
   * CommitVerify L1
   * L2: ...
   * </pre>
   */
  public Instruction[] compileZeroOrMore(ZeroOrMoreExpression expression) {
    // TODO maybe can be optimized by introduction of new instruction PartialCommit
    Instruction[] sub = compile(expression.subExpression);
    Instruction[] result = new Instruction[sub.length + 2];
    result[0] = Instruction.choice(sub.length + 2);
    System.arraycopy(sub, 0, result, 1, sub.length);
    result[sub.length + 1] = Instruction.commitVerify(-1 - sub.length);
    return result;
  }

  /**
   * Compiles {@link NextExpression} into a sequence of instructions:
   * <pre>
   * Choice L1
   * subExpression
   * BackCommit L2
   * L1: Fail
   * L2: ...
   * </pre>
   * Should be noted that can be compiled without usage of {@link Instruction.BackCommitInstruction}:
   * <pre>
   * Choice L1
   * Choice L2
   * subExpression
   * L2: Commit L3
   * L3: Fail
   * L1: ...
   * </pre>
   */
  public Instruction[] compileNext(NextExpression expression) {
    Instruction[] sub = compile(expression.subExpression);
    Instruction[] result = new Instruction[sub.length + 3];
    result[0] = Instruction.choice(result.length - 1);
    System.arraycopy(sub, 0, result, 1, sub.length);
    result[sub.length + 1] = Instruction.backCommit(2);
    result[sub.length + 2] = Instruction.backtrack();
    return result;
  }

  /**
   * Compiles {@link NextNotExpression} into a sequence of instructions:
   * <pre>
   * Choice L1
   * subExpression
   * FailTwice
   * L1: ...
   * </pre>
   * Should be noted that can be compiled without usage of {@link Instruction.FailTwiceInstruction}:
   * <pre>
   * Choice L2
   * subExpression
   * Commit L1
   * L1: Fail
   * L2: ...
   * </pre>
   */
  public Instruction[] compileNextNot(NextNotExpression expression) {
    Instruction[] sub = compile(expression.subExpression);
    Instruction[] result = new Instruction[sub.length + 2];
    result[0] = Instruction.predicateChoice(sub.length + 2);
    System.arraycopy(sub, 0, result, 1, sub.length);
    result[sub.length + 1] = Instruction.failTwice();
    return result;
  }

  /**
   * Compiles {@link NativeExpression}.
   */
  public Instruction[] compileNative(NativeExpression expression) {
    return new Instruction[]{expression};
  }

}
