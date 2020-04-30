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

package com.ca.lsp.cobol.usecases;

import com.ca.lsp.cobol.positive.CobolText;
import com.ca.lsp.cobol.service.delegates.validations.AnalysisResult;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.ca.lsp.cobol.service.delegates.validations.UseCaseUtils.*;
import static java.util.Collections.singletonList;
import static org.eclipse.lsp4j.DiagnosticSeverity.Warning;
import static org.junit.Assert.*;

/** This test checks that a misspelled keyword after a copybook usage is in the correct position */
public class TestMisspelledKeywordWarningInCorrectPosition {

  private static final String TEXT =
      "0      Identification Division. \n"
          + "1      Program-id.    ProgramId.\n"
          + "2      Data Division.\n"
          + "3      Working-Storage Section.\n"
          + "4       COPY STRUC1. \n"
          + "5      Procedure Division.\n"
          + "6      000-Main-Logic.\n"
          + "7          DISPLA \"hello\".\n"
          + "8          DISPLAY CHILD1 OF PARENT1."
          + "9      End program ProgramId.";

  private static final String STRUC1 =
      "       01  PARENT1.\n"
          + "           03  CHILD1         PIC 9   VALUE IS '0'.\n"
          + "           03  CHILD2         PIC 9   VALUE IS '1'.\n"
          + "           03  CHILD3         PIC 9   VALUE IS '2'.";

  private static final String STRUC1_NAME = "STRUC1";

  private static final String PARENT1 = "PARENT1";
  private static final String MAIN_LOGIC = "000-MAIN-LOGIC";
  private static final String CHILD1 = "CHILD1";
  private static final String CHILD2 = "CHILD2";
  private static final String CHILD3 = "CHILD3";

  @Test
  public void test() {
    AnalysisResult result = analyze(TEXT, singletonList(new CobolText(STRUC1_NAME, STRUC1)));
    assertDiagnostics(result.getDiagnostics());

    assertCopybookUsages(result.getCopybookUsages());
    assertCopybookDefinitions(result.getCopybookDefinitions());

    assertVariableUsages(result.getVariableUsages());
    assertVariableDefinitions(result.getVariableDefinitions());

    assertParagraphDefinitions(result.getParagraphDefinitions());
    assertParagraphUsages(result.getParagraphUsages());
  }

  private void assertDiagnostics(List<Diagnostic> diagnostics) {
    assertEquals("Diagnostics: " + diagnostics.toString(), 3, diagnostics.size());

    Diagnostic diagnostic =
        diagnostics.stream()
            .filter(it -> it.getMessage().contains("misspelled"))
            .findFirst()
            .orElse(null);

    assertNotNull(diagnostic);
    assertEquals("A misspelled word, maybe you want to put DISPLAY", diagnostic.getMessage());
    assertEquals(new Range(new Position(7, 11), new Position(7, 17)), diagnostic.getRange());
    assertEquals(Warning, diagnostic.getSeverity());
    assertEquals("COBOL Language Support - W", diagnostic.getSource());
    assertNull(diagnostic.getCode());
  }

  private void assertParagraphUsages(Map<String, List<Location>> usages) {
    assertEquals("Paragraph usages: " + usages.toString(), 0, usages.size());
  }

  private void assertParagraphDefinitions(Map<String, List<Location>> definitions) {
    assertEquals("Paragraph definitions: " + definitions.toString(), 1, definitions.size());
    assertNumberOfLocations(definitions, MAIN_LOGIC, 1);
    assertLocation(definitions, MAIN_LOGIC, DOCUMENT_URI, 6, 7);
  }

  private void assertCopybookDefinitions(Map<String, List<Location>> copybookDefinitions) {
    assertEquals(
        "Copybook definitions: " + copybookDefinitions.toString(), 0, copybookDefinitions.size());
  }

  private void assertCopybookUsages(Map<String, List<Location>> copybookUsages) {
    assertEquals("Copybook usages: " + copybookUsages.toString(), 1, copybookUsages.size());
    assertNumberOfLocations(copybookUsages, STRUC1_NAME, 1);
    assertLocation(copybookUsages, STRUC1_NAME, DOCUMENT_URI, 4, 13);
  }

  private void assertVariableDefinitions(Map<String, List<Location>> definitions) {
    assertEquals("Variable definitions: " + definitions.toString(), 4, definitions.size());

    assertNumberOfLocations(definitions, PARENT1, 1);
    assertLocation(definitions, PARENT1, STRUC1_NAME, 0, 11);

    assertNumberOfLocations(definitions, CHILD1, 1);
    assertLocation(definitions, CHILD1, STRUC1_NAME, 1, 15);

    assertNumberOfLocations(definitions, CHILD2, 1);
    assertLocation(definitions, CHILD2, STRUC1_NAME, 2, 15);

    assertNumberOfLocations(definitions, CHILD3, 1);
    assertLocation(definitions, CHILD3, STRUC1_NAME, 3, 15);
  }

  private void assertVariableUsages(Map<String, List<Location>> usages) {
    assertEquals("Variable usages: " + usages.toString(), 2, usages.size());

    assertNumberOfLocations(usages, CHILD1, 1);
    assertLocation(usages, CHILD1, DOCUMENT_URI, 8, 19);

    assertNumberOfLocations(usages, PARENT1, 1);
    assertLocation(usages, PARENT1, DOCUMENT_URI, 8, 29);
  }
}
