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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import java.util.List;

public final class ReBuilder {

  public static ReBuilder create() {
    return new ReBuilder();
  }

  private ReBuilder() {
  }

  public Re.ReClass anyOf(char c1, char c2) {
    return new Re.ReClass(Character.toString(c1) + Character.toString(c2));
  }

  public Re.ReClass inRange(char from, char to) {
    return new Re.ReClass(from + "-" + to);
  }

  public Re.ReClass not(Re.ReClass c) {
    return new Re.ReClass("^" + c.value);
  }

  public Re.ReClass not(char c1) {
    return new Re.ReClass("^" + c1);
  }

  public Re.ReClass not(char c1, char c2) {
    return new Re.ReClass("^" + c1 + c2);
  }

  /**
   * <pre>
   * .
   * </pre>
   */
  public Re anyChar() {
    // TODO singleton
    return new Re.Single(".");
  }

  /**
   * TODO rename into anyOf ?
   * <pre>
   * e1 | e2
   * </pre>
   */
  public Re oneOf(Object e1, Object e2, Object... rest) {
    List<Object> e = Lists.asList(e1, e2, rest);
    Re[] inner = new Re[e.size()];
    for (int i = 0; i < e.size(); i++) {
      inner[i] = convert(e.get(i));
    }
    return new Re.OneOf(inner);
  }

  /**
   * <pre>
   * e1 e2
   * </pre>
   */
  public Re sequence(Object e1, Object e2, Object... rest) {
    List<Object> e = Lists.asList(e1, e2, rest);
    Re[] inner = new Re[e.size()];
    for (int i = 0; i < e.size(); i++) {
      Re re = convert(e.get(i));
      if (re instanceof Re.OneOf) {
        re = new Re.Group(Re.Group.NON_CAPTURING, re);
      }
      inner[i] = re;
    }
    return new Re.Sequence(inner);
  }

  /**
   * <pre>
   * e ?+
   * </pre>
   */
  public Re optional(Object e) {
    return new Re.Term(group(convert(e)), "?+");
  }

  /**
   * <pre>
   * e ++
   * </pre>
   */
  public Re oneOrMore(Object e) {
    return new Re.Term(group(convert(e)), "++");
  }

  /**
   * <pre>
   * e *+
   * </pre>
   */
  public Re zeroOrMore(Object e) {
    return new Re.Term(group(convert(e)), "*+");
  }

  /**
   * <pre>
   * e *?
   * </pre>
   */
  public Re reluctantZeroOrMore(Object e) {
    return new Re.Term(group(convert(e)), "*?");
  }

  /**
   * Non-capturing group.
   * <pre>
   * (?: e )
   * </pre>
   */
  private Re group(Object e) {
    Re re = convert(e);
    if (re instanceof Re.Single) {
      return re;
    } else if (re instanceof Re.Group) {
      return re;
    }
    return new Re.Group(Re.Group.NON_CAPTURING, re);
  }

  /**
   * Capturing group.
   * <pre>
   * ( e )
   * </pre>
   */
  public Re capturingGroup(Object e) {
    return new Re.Group(Re.Group.CAPTURING, convert(e));
  }

  /**
   * Positive lookahead.
   * <pre>
   * (?= e )
   * </pre>
   */
  public Re next(Object e) {
    return new Re.Group(Re.Group.POSITIVE_LOOKAHEAD, convert(e));
  }

  /**
   * Negative lookahead.
   * <pre>
   * (?! e )
   * </pre>
   */
  public Re nextNot(Object e) {
    return new Re.Group(Re.Group.NEGATIVE_LOOKAHEAD, convert(e));
  }

  /**
   * Back reference.
   * <pre>
   * \\ n
   * </pre>
   */
  public Re backReference(int n) {
    return new Re.Single("\\" + n);
  }

  @VisibleForTesting
  Re convert(Object obj) {
    if (obj instanceof Re) {
      return (Re) obj;
    } else if (obj instanceof Character) {
      return new Re.Single(escape(((Character) obj).charValue()));
    } else if (obj instanceof String) {
      String s = (String) obj;
      if (s.length() == 1) {
        return new Re.Single(escape(s.charAt(0)));
      }
      // TODO probably not optimal
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < s.length(); i++) {
        sb.append(escape(s.charAt(i)));
      }
      return new Re.Raw(sb.toString());
    }
    throw new IllegalArgumentException();
  }

  // TODO figure out - is it enough?
  private static final char[] ESCAPE = {'\\', '.', '*', '+', '(', ')', '[', ']', '{', '}'};

  private static String escape(char ch) {
    for (char c : ESCAPE) {
      if (ch == c) {
        return "\\" + c;
      }
    }
    return Character.toString(ch);
  }

}
