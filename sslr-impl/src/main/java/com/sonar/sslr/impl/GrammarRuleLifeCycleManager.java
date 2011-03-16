/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.sslr.impl;

import java.lang.reflect.Field;

import org.apache.commons.lang.ClassUtils;

import com.sonar.sslr.api.LeftRecursiveRule;
import com.sonar.sslr.api.Rule;
import com.sonar.sslr.impl.matcher.LeftRecursiveRuleImpl;
import com.sonar.sslr.impl.matcher.RuleImpl;

/**
 * Utility class for handling grammar rule life cycle
 */
public final class GrammarRuleLifeCycleManager {

  private GrammarRuleLifeCycleManager() {
  }

  /**
   * Initializes the given grammar with standard rules.
   * 
   * @param rules
   *          the grammar object to initialize
   * @param grammar
   *          the class that represents this grammar
   */
  public static void initializeRuleFields(Object rules, Class<?> grammar) {
    Field[] fields = grammar.getDeclaredFields();
    for (Field field : fields) {
      String fieldName = field.getName();
      try {
        if (field.getType() == LeftRecursiveRule.class) {
          field.set(rules, new LeftRecursiveRuleImpl(fieldName));
        } else if (field.getType() == Rule.class) {
          field.set(rules, new RuleImpl(fieldName));
        }
      } catch (Exception e) {
        throw new RuntimeException("Unable to instanciate the rule '" + grammar.getName() + "." + fieldName + "'", e);
      }
    }
  }

  /**
   * Notify the rules of the given grammar that a parsing has just ended so that they can optionally reinitialize their state.
   * 
   * @param rules
   *          the grammar object that contains the rules to notify
   */
  static void notifyEndParsing(Object grammar) {
    Field[] fields = grammar.getClass().getDeclaredFields();
    for (Field field : fields) {
      if (field.getType() == LeftRecursiveRule.class) {
        try {
          Object rule = field.get(grammar);
          if (ClassUtils.isAssignable(rule.getClass(), LeftRecursiveRuleImpl.class)) {
            ((LeftRecursiveRuleImpl) rule).endParsing();
          }
        } catch (Exception e) {
          throw new RuntimeException("Unable to call endParsing() method on the following left recursive rule rule '"
              + grammar.getClass().getName() + "." + field.getName() + "'", e);
        }
      }
    }
  }

}