package org.sonar.sslr.internal.vm;

import org.sonar.sslr.internal.matchers.Matcher;
import org.sonar.sslr.internal.matchers.MatcherContext;

public abstract class AbstractCompilableMatcher implements Matcher, CompilableMatcher {

  public boolean match(MatcherContext context) {
    throw new UnsupportedOperationException();
  }

}
