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
 *   Broadcom, Inc. - initial API and implementation
 */
package com.ca.lsp.core.cobol.preprocessor;

import com.ca.lsp.core.cobol.model.PreprocessedInput;
import com.ca.lsp.core.cobol.model.ResultWithErrors;
import com.ca.lsp.core.cobol.semantics.SemanticContext;

/** The CobolPreprocessor is engaged in order to process, transform and parse the document */
public interface CobolPreprocessor {
  ResultWithErrors<PreprocessedInput> process(
      String documentUri, String cobolCode, String textDocumentSyncType);

  ResultWithErrors<PreprocessedInput> process(
      String documentUri,
      String cobolCode,
      SemanticContext semanticContext,
      String textDocumentSyncType);
}
