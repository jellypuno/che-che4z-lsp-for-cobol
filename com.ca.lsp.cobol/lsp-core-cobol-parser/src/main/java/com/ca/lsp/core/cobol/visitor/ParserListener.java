/*
 * Copyright (c) 2020 Broadcom.
 * The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Broadcom, Inc. - initial API and implementation
 *
 */
package com.ca.lsp.core.cobol.visitor;

import com.ca.lsp.core.cobol.model.SyntaxError;
import lombok.Getter;
import org.antlr.v4.runtime.*;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.reverse;

public class ParserListener extends BaseErrorListener {

  @Getter private List<SyntaxError> errors = new ArrayList<>();

  @Override
  public void syntaxError(
      Recognizer<?, ?> recognizer,
      Object offendingSymbol,
      int line,
      int charPositionInLine,
      String msg,
      RecognitionException e) {
    String suggestion = msg;
    List<String> stack = new ArrayList<>();
    if (recognizer instanceof Parser) {
      stack = ((Parser) recognizer).getRuleInvocationStack();
      reverse(stack);
    }
    if (recognizer instanceof Lexer) {
      stack.add(((Lexer) recognizer).getText());
      suggestion = msg.concat(" on ").concat(stack.get(stack.size() - 1));
    }
    errors.add(
        SyntaxError.syntaxError()
            .startToken((CommonToken) offendingSymbol)
            .ruleStack(stack)
            .suggestion(suggestion)
            .severity(1)
            .build());
  }
}
