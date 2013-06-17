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

/**
 * TODO
 * character classes
 * raw
 */
public abstract class Re {

  public abstract String asString();

  static class Raw extends Re {
    final String value;

    public Raw(String value) {
      this.value = value;
    }

    public String asString() {
      return value;
    }
  }

  static class Single extends Raw {
    public Single(String value) {
      super(value);
    }
  }

  public static class ReClass extends Single {

    public ReClass(String value) {
      super(value);
    }

    public ReClass or(ReClass another) {
      return new ReClass(value + another.asString());
    }

    @Override
    public String asString() {
      return "[" + value + "]";
    }
  }

  static class Sequence extends Re {
    private final Re[] inner;

    public Sequence(Re... inner) {
      this.inner = inner;
    }

    @Override
    public String asString() {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < inner.length; i++) {
        sb.append(inner[i].asString());
      }
      return sb.toString();
    }
  }

  static class OneOf extends Re {
    private final Re[] inner;

    public OneOf(Re... inner) {
      this.inner = inner;
    }

    @Override
    public String asString() {
      StringBuilder sb = new StringBuilder();
      sb.append(inner[0].asString());
      for (int i = 1; i < inner.length; i++) {
        sb.append('|').append(inner[i].asString());
      }
      return sb.toString();
    }
  }

  static class Term extends Re {
    private final Re inner;
    private final String quantifier;

    public Term(Re inner, String quantifier) {
      this.inner = inner;
      this.quantifier = quantifier;
    }

    @Override
    public String asString() {
      return inner.asString() + quantifier;
    }
  }

  static class Group extends Re {
    static final String CAPTURING = "";
    static final String NON_CAPTURING = "?:";
    static final String POSITIVE_LOOKAHEAD = "?=";
    static final String NEGATIVE_LOOKAHEAD = "?!";

    private final String type;
    private final Re inner;

    public Group(String type, Re inner) {
      this.type = type;
      this.inner = inner;
    }

    @Override
    public String asString() {
      return "(" + type + inner.asString() + ")";
    }
  }

}
