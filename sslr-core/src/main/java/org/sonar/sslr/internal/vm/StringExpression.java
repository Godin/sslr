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


// TODO should create node
public class StringExpression extends AbstractCompilableMatcher implements CompilableMatcher, NativeMatcher {

  private final String string;

  public StringExpression(String string) {
    this.string = string;
  }

  public boolean execute(NativeMatcherContext context) {
    if (context.length() < string.length()) {
      return false;
    }
    for (int i = 0; i < string.length(); i++) {
      if (context.charAt(i) != string.charAt(i)) {
        return false;
      }
    }
    context.advanceIndex(string.length());
    // context.createNode();
    return true;
  }

  public Instr[] compile() {
    return new Instr[] {Instr.native_call(this)};
  }

}
