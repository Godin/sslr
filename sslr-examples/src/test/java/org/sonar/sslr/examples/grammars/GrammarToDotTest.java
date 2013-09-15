package org.sonar.sslr.examples.grammars;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sonar.sslr.api.Grammar;
import org.junit.Test;
import org.sonar.java.ast.parser.JavaGrammar;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.internal.vm.CompilableGrammarRule;
import org.sonar.sslr.internal.vm.CompilationHandler;
import org.sonar.sslr.internal.vm.Instruction;
import org.sonar.sslr.internal.vm.ParsingExpression;

import java.util.Queue;
import java.util.Set;

public class GrammarToDotTest {

  private static class Edge {
    private final String from, to;

    public Edge(String from, String to) {
      this.from = from;
      this.to = to;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(from, to);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Edge) {
        Edge other = (Edge) obj;
        return Objects.equal(this.from, other.from)
          && Objects.equal(this.to, other.to);
      }
      return false;
    }
  }

  @Test
  public void test() {
    Grammar g = JavaGrammar.createGrammar();
    DotCompiler dotCompiler = new DotCompiler();
    dotCompiler.doCompile((CompilableGrammarRule) g.getRootRule());

    System.out.println("digraph {");
    for (Edge edge : dotCompiler.edges) {
      System.out.println(edge.from + " -> " + edge.to);
    }
    System.out.println("}");
  }

  static class DotCompiler extends CompilationHandler {
    private final Set<Edge> edges = Sets.newHashSet();
    private final Queue<CompilableGrammarRule> compilationQueue = Lists.newLinkedList();
    private final Set<GrammarRuleKey> seen = Sets.newHashSet();
    private GrammarRuleKey currentRule;

    private void doCompile(CompilableGrammarRule start) {
      compilationQueue.add(start);
      seen.add(start.getRuleKey());

      while (!compilationQueue.isEmpty()) {
        CompilableGrammarRule rule = compilationQueue.poll();
        currentRule = rule.getRuleKey();
        compile(rule.getExpression());
      }
    }

    @Override
    public Instruction[] compile(ParsingExpression expression) {
      if (expression instanceof CompilableGrammarRule) {
        CompilableGrammarRule rule = (CompilableGrammarRule) expression;
        if (!seen.contains(rule.getRuleKey())) {
          compilationQueue.add(rule);
          seen.add(rule.getRuleKey());
        }
        addEdge(currentRule, rule.getRuleKey());
        return rule.compile(this);
      } else {
        return expression.compile(this);
      }
    }

    private void addEdge(GrammarRuleKey from, GrammarRuleKey to) {
      if (from instanceof JavaGrammar && to instanceof JavaGrammar) {
        edges.add(new Edge(currentRule.toString(), to.toString()));
      }
    }
  }

}
