/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.sslr.impl.matcher;

import com.sonar.sslr.api.AstListener;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeSkippingPolicy;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.impl.ParsingState;
import com.sonar.sslr.impl.RecognitionExceptionImpl;
import com.sonar.sslr.impl.ast.NeverSkipFromAst;

public class RuleMatcher extends Matcher {

  protected String name;
  private AstListener listener;
  private Class adapterClass;
  private AstNodeType astNodeSkippingPolicy = new NeverSkipFromAst();
  private boolean recoveryRule = false;
  private AstNodeType astNodeType;

  public RuleMatcher(String name) {
    this.name = name;
  }

  public AstNode match(ParsingState parsingState) {
    int startIndex = parsingState.lexerIndex;
    if (super.children.length == 0) {
      throw new IllegalStateException("The rule '" + name + "' hasn't beed defined.");
    }
    if (recoveryRule) {
      RecognitionException recognitionException = new RecognitionExceptionImpl(parsingState);
      if (super.children[0].isMatching(parsingState)) {
        parsingState.notifyListerners(recognitionException);
      }
    }
    AstNode childNode = super.children[0].match(parsingState);

    AstNode astNode = new AstNode(astNodeType, name, parsingState.peekTokenIfExists(startIndex, super.children[0]));
    astNode.setAstNodeListener(listener);
    astNode.addChild(childNode);
    return astNode;
  }

  public boolean hasToBeSkippedFromAst(AstNode node) {
    if (AstNodeSkippingPolicy.class.isAssignableFrom(astNodeSkippingPolicy.getClass())) {
      return ((AstNodeSkippingPolicy) astNodeSkippingPolicy).hasToBeSkippedFromAst(node);
    }
    return false;
  }

  protected void setMatcher(Matcher matcher) {
    super.children = new Matcher[] { matcher };
  }

  public void setListener(AstListener listener) {
    this.listener = listener;
  }

  public void skipIf(AstNodeType astNodeSkipPolicy) {
    this.astNodeSkippingPolicy = astNodeSkipPolicy;
  }

  public void setNodeType(AstNodeType astNodeType) {
    this.astNodeType = astNodeType;
  }

  public RuleMatcher plug(Class adapterClass) {
    this.adapterClass = adapterClass;
    return this;
  }

  public Class getAdapter() {
    return adapterClass;
  }

  public String getName() {
    return name;
  }

  public void recoveryRule() {
    recoveryRule = true;
  }

  public void endParsing() {
    /* Nothing, used by left recurisve rule only so far */
  }

  @Override
  public String toString() {
    return getName();
  }

}