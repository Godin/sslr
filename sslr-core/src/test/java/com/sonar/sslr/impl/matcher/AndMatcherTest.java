/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.sslr.impl.matcher;

import static com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.isFalse;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.isTrue;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.and;
import static com.sonar.sslr.impl.matcher.HamcrestMatchMatcher.match;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class AndMatcherTest {

  @Test
  public void ok() {
    assertThat(and(isTrue(), isTrue(), isTrue()), match("one two three"));
    assertThat(and(isTrue(), isFalse()), not(match("one two")));
    assertThat(and(isFalse(), isFalse()), not(match("one two")));
  }
  
  @Test
  public void testToString() {
  	assertEquals(and("(", ")").toString(), "and"); /* With a single argument, the and() is removed automatically */
  }

}
