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
package org.sonar.sslr.re;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.fest.assertions.Assertions.assertThat;

public class ReBuilderTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private ReBuilder re = ReBuilder.create();

  @Test
  public void atom() {
    assertThat(re.convert('e')).isInstanceOf(Re.Single.class);
    assertThat(re.convert("e")).isInstanceOf(Re.Single.class);
    assertThat(re.convert("e").asString()).isEqualTo("e");

    assertThat(re.convert("exp")).isInstanceOf(Re.Raw.class);
    assertThat(re.convert("exp").asString()).isEqualTo("exp");

    // TODO Forbid empty strings?
    assertThat(re.convert("")).isInstanceOf(Re.Raw.class);
    assertThat(re.convert("").asString()).isEqualTo("");
  }

  @Test
  public void escape() {
    assertThat(re.convert('\\').asString()).isEqualTo("\\\\");
    assertThat(re.convert('.').asString()).isEqualTo("\\.");
    assertThat(re.convert('*').asString()).isEqualTo("\\*");
    assertThat(re.convert('+').asString()).isEqualTo("\\+");
    assertThat(re.convert('(').asString()).isEqualTo("\\(");
    assertThat(re.convert(')').asString()).isEqualTo("\\)");
    assertThat(re.convert('[').asString()).isEqualTo("\\[");
    assertThat(re.convert(']').asString()).isEqualTo("\\]");
    assertThat(re.convert('{').asString()).isEqualTo("\\{");
    assertThat(re.convert('}').asString()).isEqualTo("\\}");
  }

  @Test
  public void illegal() {
    thrown.expect(IllegalArgumentException.class);
    re.convert(new Object());
  }

  @Test
  public void anyChar() {
    assertThat(re.anyChar().asString()).isEqualTo(".");
  }

  @Test
  public void anyOf() {
    assertThat(re.anyOf('a', 'b').asString()).isEqualTo("[ab]");
  }

  @Test
  public void inRange() {
    assertThat(re.inRange('a', 'z').asString()).isEqualTo("[a-z]");
    assertThat(re.inRange('a', 'z').or(re.inRange('A', 'Z')).asString()).isEqualTo("[a-z[A-Z]]");
  }

  @Test
  public void not() {
    assertThat(re.not(re.anyOf('a', 'b')).asString()).isEqualTo("[^ab]");
    assertThat(re.not(re.inRange('a', 'z')).asString()).isEqualTo("[^a-z]");
  }

  @Test
  public void oneOf() {
    assertThat(re.oneOf('a', 'b').asString()).isEqualTo("a|b");
    assertThat(re.oneOf('a', re.oneOf('b', 'c')).asString()).isEqualTo("a|b|c");
  }

  @Test
  public void sequence() {
    assertThat(re.sequence('a', 'b').asString()).isEqualTo("ab");
    assertThat(re.sequence(re.oneOf('a', 'b'), 'c').asString()).isEqualTo("(?:a|b)c");
    assertThat(re.sequence('a', re.oneOf('b', 'c')).asString()).isEqualTo("a(?:b|c)");
  }

  @Test
  public void optional() {
    assertThat(re.optional("exp").asString()).isEqualTo("(?:exp)?+");
    assertThat(re.optional('e').asString()).isEqualTo("e?+");
    assertThat(re.optional(re.anyChar()).asString()).isEqualTo(".?+");
    assertThat(re.optional(re.oneOf('a', 'b')).asString()).isEqualTo("(?:a|b)?+");
    assertThat(re.optional(re.sequence('a', 'b')).asString()).isEqualTo("(?:ab)?+");
    assertThat(re.optional(re.capturingGroup('e')).asString()).isEqualTo("(e)?+");
  }

  @Test
  public void oneOrMore() {
    assertThat(re.oneOrMore("exp").asString()).isEqualTo("(?:exp)++");
    assertThat(re.oneOrMore('e').asString()).isEqualTo("e++");
    assertThat(re.oneOrMore(re.anyChar()).asString()).isEqualTo(".++");
    assertThat(re.oneOrMore(re.oneOf('a', 'b')).asString()).isEqualTo("(?:a|b)++");
    assertThat(re.oneOrMore(re.sequence('a', 'b')).asString()).isEqualTo("(?:ab)++");
    assertThat(re.oneOrMore(re.capturingGroup('e')).asString()).isEqualTo("(e)++");
  }

  @Test
  public void zeroOrMore() {
    assertThat(re.zeroOrMore("exp").asString()).isEqualTo("(?:exp)*+");
    assertThat(re.zeroOrMore('e').asString()).isEqualTo("e*+");
    assertThat(re.zeroOrMore(re.anyChar()).asString()).isEqualTo(".*+");
    assertThat(re.zeroOrMore(re.oneOf('a', 'b')).asString()).isEqualTo("(?:a|b)*+");
    assertThat(re.zeroOrMore(re.sequence('a', 'b')).asString()).isEqualTo("(?:ab)*+");
    assertThat(re.zeroOrMore(re.capturingGroup('e')).asString()).isEqualTo("(e)*+");
  }

  @Test
  public void next() {
    assertThat(re.next('e').asString()).isEqualTo("(?=e)");
    assertThat(re.next(re.oneOf('a', 'b')).asString()).isEqualTo("(?=a|b)");
  }

  @Test
  public void nextNot() {
    assertThat(re.nextNot('e').asString()).isEqualTo("(?!e)");
    assertThat(re.nextNot(re.oneOf('a', 'b')).asString()).isEqualTo("(?!a|b)");
  }

  @Test
  public void capturingGroup() {
    assertThat(re.capturingGroup('e').asString()).isEqualTo("(e)");
    assertThat(re.capturingGroup(re.oneOf('a', 'b')).asString()).isEqualTo("(a|b)");
  }

  @Test
  public void backReference() {
    assertThat(re.backReference(1).asString()).isEqualTo("\\1");
    assertThat(re.backReference(2).asString()).isEqualTo("\\2");
  }

}
