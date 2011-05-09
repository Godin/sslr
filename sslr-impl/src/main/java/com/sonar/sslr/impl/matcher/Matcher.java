/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.sslr.impl.matcher;

import java.util.List;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeSkippingPolicy;
import com.sonar.sslr.api.Rule;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.TokenType;
import com.sonar.sslr.impl.ParsingState;
import com.sonar.sslr.impl.RecognitionExceptionImpl;

public abstract class Matcher implements AstNodeSkippingPolicy {

  protected RuleImpl parentRule;
  protected Matcher[] children;
  
  public Matcher(Matcher... children) {
  	this.children = children;
  }
  
  public Matcher[] getChildren() {
  	return this.children;
  }

  public void setChild(int i, Matcher child) {
  	this.children[i] = child;
  }
  
  public void setParentRule(RuleImpl parentRule) {
  	this.parentRule = parentRule;
  	for (Matcher child: this.children) {
			child.setParentRule(parentRule);
		}
  }

  public RuleImpl getRule() {
    return parentRule;
  }

  public final boolean isMatching(ParsingState parsingState) {
  	return matchToIndex(parsingState) >= 0;
  }
  
  public int matchToIndex(ParsingState parsingState) {
    int indexBeforeStarting = parsingState.lexerIndex;
    boolean leftRecursionState = parsingState.hasPendingLeftRecursion();
    try {
      match(parsingState);
      return parsingState.lexerIndex;
    } catch (RecognitionExceptionImpl e) {
      return -1;
    } finally {
      parsingState.lexerIndex = indexBeforeStarting;
      parsingState.setLeftRecursionState(leftRecursionState);
    }
  }

  public boolean hasToBeSkippedFromAst(AstNode node) {
    return true;
  }

  public abstract AstNode match(ParsingState parsingState);

  public final AstNode parse(ParsingState parsingState) {
    try {
      return this.match(parsingState);
    } catch (RecognitionExceptionImpl e) {
      throw new RecognitionExceptionImpl(parsingState);
    }
  }

  public final AstNode parse(List<Token> tokens) {
    return parse(new ParsingState(tokens));
  }

  protected static final Matcher[] convertToMatchers(Object[] objects) {
    Matcher[] matchers = new Matcher[objects.length];
    for (int i = 0; i < matchers.length; i++) {
      matchers[i] = convertToMatcher(objects[i]);
    }
    return matchers;
  }

  @SuppressWarnings("rawtypes")
  protected static final Matcher convertToMatcher(Object object) {
    Matcher matcher;
    if (object instanceof String) {
      matcher = new TokenValueMatcher((String) object);
    } else if (object instanceof TokenType) {
      TokenType tokenType = (TokenType) object;
      matcher = new TokenTypeMatcher(tokenType, tokenType.hasToBeSkippedFromAst(null));
    } else if (object instanceof Class) {
      matcher = new TokenTypeClassMatcher((Class) object);
    } else {
      try {
        matcher = (Matcher) object;
      } catch (ClassCastException e) {
        throw new IllegalStateException("The matcher object can't be anything else than a Matcher, String or TokenType. Object = " + object);
      }
    }
    if (matcher instanceof Rule) {
      return new MemoizerMatcher(matcher);
    } else {
      return matcher;
    }
  }
  
}
