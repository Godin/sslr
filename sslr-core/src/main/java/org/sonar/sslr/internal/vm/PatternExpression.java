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

import org.sonar.sslr.grammar.GrammarException;
import org.sonar.sslr.internal.matchers.MatcherContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternExpression extends NativeExpression implements org.sonar.sslr.internal.matchers.Matcher {

  private final Matcher matcher;

  public PatternExpression(String regex) {
    matcher = Pattern.compile(regex).matcher("");
  }

  @Override
  public void execute(Machine machine) {
    matcher.reset(machine);
    boolean result;
    try {
      result = matcher.lookingAt();
    } catch (StackOverflowError e) {
      throw new GrammarException(e, "The regular expression '" + matcher.pattern().pattern() + "' has led to a stack overflow error."
          + " This error is certainly due to an inefficient use of alternations. See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5050507");
    }
    if (result) {
      // TODO what if end == 0 ???
      machine.advanceIndex(matcher.end());
      machine.createLeafNode(this);
      machine.jump(1);
    } else {
      machine.backtrack();
    }
  }

  public boolean match(MatcherContext context) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "Pattern " + matcher.pattern().pattern();
  }

}
