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

import org.junit.Test;
import org.sonar.sslr.internal.matchers.ParseTreePrinter;
import org.sonar.sslr.parser.ParsingResult;

import static org.fest.assertions.Assertions.assertThat;

public class CompiledExpressionGrammarTest {

  @Test
  public void test() {
    CompiledGrammarParseRunner parseRunner = new CompiledGrammarParseRunner(CompiledExpressionGrammar.create(), CompiledExpressionGrammar.EXPRESSION);
    String inputString = "20 * ( 2 + 2 ) - var";
    char[] input = inputString.toCharArray();
    ParsingResult result = parseRunner.parse(input);
    assertThat(result.isMatched()).isTrue();
    ParseTreePrinter.print(result.getParseTreeRoot(), input);

    assertThat(ParseTreePrinter.leafsToString(result.getParseTreeRoot(), input))
        .as("full-fidelity")
        .isEqualTo(inputString);
  }

}
