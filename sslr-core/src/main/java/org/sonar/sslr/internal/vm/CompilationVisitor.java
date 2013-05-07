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

  private final List<Instruction> instructions = Lists.newArrayList();

  private CompiledGrammar doCompile(CompilableGrammarRule start) {
    // Compile

    compilationQueue.add(start);
    matchers.put(start.getRuleKey(), start);

    while (!compilationQueue.isEmpty()) {
      CompilableGrammarRule rule = compilationQueue.poll();
      GrammarRuleKey ruleKey = rule.getRuleKey();

      offsets.put(ruleKey, instructions.size());

      compile(rule.getExpression());

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

  public void compile(ParsingExpression expression) {
    if (expression instanceof SequenceExpression) {
      compileSequence((SequenceExpression) expression);
    } else if (expression instanceof FirstOfExpression) {
      compileFirstOf((FirstOfExpression) expression);
    } else if (expression instanceof OptionalExpression) {
      compileOptional((OptionalExpression) expression);
    } else if (expression instanceof OneOrMoreExpression) {
      compileOneOrMore((OneOrMoreExpression) expression);
    } else if (expression instanceof ZeroOrMoreExpression) {
      compileZeroOrMore((ZeroOrMoreExpression) expression);
    } else if (expression instanceof NextExpression) {
      compileNext((NextExpression) expression);
    } else if (expression instanceof NextNotExpression) {
      compileNextNot((NextNotExpression) expression);
    } else if (expression instanceof NativeExpression) {
      compileNative((NativeExpression) expression);
    } else if (expression instanceof CompilableGrammarRule) {
      compileRule((CompilableGrammarRule) expression);
    } else if (expression instanceof TokenExpression) {
      compileToken((TokenExpression) expression);
    } else if (expression instanceof TriviaExpression) {
      compileTrivia((TriviaExpression) expression);
    } else {
      Instruction.addAll(instructions, expression.compile(null));
//      throw new IllegalArgumentException("Unexpected expression: " + expression.getClass());
    }
  }

  public void compileRule(CompilableGrammarRule rule) {
    if (!matchers.containsKey(rule.getRuleKey())) {
      compilationQueue.add(rule);
      matchers.put(rule.getRuleKey(), rule);
    }
    instructions.add(new RuleRefExpression(rule.getRuleKey()));
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
  public void compileTrivia(TriviaExpression expression) {
    compileTokenOrTrivia(expression, expression.subExpression);
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
  public void compileToken(TokenExpression expression) {
    compileTokenOrTrivia(expression, expression.subExpression);
  }

  public Instruction[] compileTokenOrTrivia(Matcher expression, ParsingExpression subExpression) {
    // TODO maybe can be optimized
    instructions.add(Instruction.call(2, expression));
    int start = instructions.size();
    instructions.add(null); // reserve space for "Jump L"
    instructions.add(Instruction.ignoreErrors());
    compile(subExpression);
    instructions.add(Instruction.ret());
    instructions.set(start, Instruction.jump(instructions.size() - start)); // fill "Jump L"
    return null;
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
  public void compileSequence(SequenceExpression expression) {
    for (ParsingExpression subExpression : expression.subExpressions) {
      compile(subExpression);
    }
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
  public void compileFirstOf(FirstOfExpression expression) {
    ParsingExpression[] subExpressions = expression.subExpressions;

    int s = instructions.size();
    for (int i = 0; i < subExpressions.length - 1; i++) {
      int start = instructions.size();
      instructions.add(null); // reserve space for "Choice L"
      compile(subExpressions[i]);
      instructions.add(null); // reserve space for "Commit E"
      instructions.set(start, Instruction.choice(instructions.size() - start)); // fill "Choice L"
    }
    compile(subExpressions[subExpressions.length - 1]);
    for (int i = instructions.size() - 1; i > s; i--) {
      if (instructions.get(i) == null) {
        instructions.set(i, Instruction.commit(instructions.size() - i)); // fill "Commit E"
      }
    }
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
  public void compileOptional(OptionalExpression expression) {
    // not described in paper
    int start = instructions.size();
    instructions.add(null); // reserve space for "Choice L"
    compile(expression.subExpression);
    instructions.add(Instruction.commit(1));
    instructions.set(start, Instruction.choice(instructions.size() - start)); // fill "Choice L"
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
  public void compileOneOrMore(OneOrMoreExpression expression) {
    int start = instructions.size();
    instructions.add(null); // reserve space for "Choice L"
    compile(expression.subExpression);
    instructions.add(Instruction.commitVerify(1));
    instructions.add(Instruction.choice(3));
    instructions.add(Instruction.jump(start - instructions.size() + 1));
    instructions.set(start, Instruction.choice(instructions.size() - start)); // fill "Choice L"
    instructions.add(Instruction.backtrack());
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
  public void compileZeroOrMore(ZeroOrMoreExpression expression) {
    // TODO maybe can be optimized by introduction of new instruction PartialCommit
    int start = instructions.size();
    instructions.add(null); // reserve space for "Choice L"
    compile(expression.subExpression);
    instructions.add(Instruction.commitVerify(start - instructions.size()));
    instructions.set(start, Instruction.choice(instructions.size() - start)); // fill "Choice L"
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
  public void compileNext(NextExpression expression) {
    int start = instructions.size();
    instructions.add(null); // reserve space for "Choice L"
    compile(expression.subExpression);
    instructions.add(Instruction.backCommit(2));
    // TODO why not predicateChoice?
    instructions.set(start, Instruction.choice(instructions.size() - start)); // fill "Choice L"
    instructions.add(Instruction.backtrack());
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
  public void compileNextNot(NextNotExpression expression) {
    int start = instructions.size();
    instructions.add(null); // reserve space for "Choice L"
    compile(expression.subExpression);
    instructions.add(Instruction.failTwice());
    instructions.set(start, Instruction.predicateChoice(instructions.size() - start)); // fill "Choice L"
  }

  /**
   * Compiles {@link NativeExpression}.
   */
  public void compileNative(NativeExpression expression) {
    instructions.add(expression);
  }

}
