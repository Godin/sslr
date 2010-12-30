/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.sslr.api.flow;

import java.util.LinkedList;
import java.util.List;

import com.sonar.sslr.api.AstNode;

public class Block {

  private final List<AstNode> stmts = new LinkedList<AstNode>();
  private final AstNode firstStatement;

  Block(AstNode firstStatement) {
    stmts.add(firstStatement);
    this.firstStatement = firstStatement;
  }

  void addStatement(AstNode stmt) {
    stmts.add(stmt);
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Block) {
      return firstStatement == ((Block) object).firstStatement;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return firstStatement.hashCode();
  }

  public List<AstNode> getStatements() {
    return stmts;
  }

  public AstNode getFirstStatement() {
    return firstStatement;
  }

  public AstNode getLastStatement() {
    return stmts.get(stmts.size() - 1);
  }

  @Override
  public String toString() {
    return "Block starting with statement '" + getFirstStatement() + "' and ending with statement '" + getLastStatement() + "'";
  }
}