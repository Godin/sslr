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

import org.fest.assertions.Assertions;
import org.fest.assertions.GenericAssert;
import org.fest.assertions.StringAssert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assert {

  public static StringAssert that(String actual) {
    return Assertions.assertThat(actual);
  }

  public static ReAssert that(Re actual) {
    return new ReAssert(actual);
  }

  public static class ReAssert extends GenericAssert<ReAssert, Re> {
    private ReAssert(Re actual) {
      super(ReAssert.class, actual);
    }

    public ReAssert matches(String s) {
      if (!Pattern.matches(actual.asString(), s)) {
        fail("");
      }
      return this;
    }

    public ReAssert matchesPrefix(String prefix, String s) {
      Matcher matcher = Pattern.compile(actual.asString()).matcher(prefix + s);
      if (!matcher.lookingAt() || matcher.end() != prefix.length()) {
        fail("");
      }
      return this;
    }

    public ReAssert notMatches(String s) {
      if (Pattern.matches(actual.asString(), s)) {
        fail("");
      }
      return this;
    }
  }

}
