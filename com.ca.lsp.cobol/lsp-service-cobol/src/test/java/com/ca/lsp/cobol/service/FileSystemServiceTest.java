/*
 * Copyright (c) 2020 Broadcom.
 *
 * The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Broadcom, Inc. - initial API and implementation
 *
 */
package com.ca.lsp.cobol.service;

import com.broadcom.lsp.cdi.LangServerCtx;
import com.broadcom.lsp.domain.cobol.event.api.EventObserver;
import com.broadcom.lsp.domain.cobol.event.model.FetchedCopybookEvent;
import com.broadcom.lsp.domain.cobol.event.model.RequiredCopybookEvent;
import com.ca.lsp.cobol.FileSystemConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

@Slf4j
public class FileSystemServiceTest extends FileSystemConfiguration
    implements EventObserver<RequiredCopybookEvent> {

  private static final String DEP_EXTENSION = ".dep";
  private FileSystemServiceImpl fileSystemService =
      (FileSystemServiceImpl) LangServerCtx.getInjector().getInstance(FileSystemService.class);

  // Activities performed
  // 1 - Create folder structure in temp folder - Create two copybooks in the provided structure
  // 2 - Create two copybooks in the provided structure
  // 3 - Initialize the workspaceFolder to reproduce what client does when a new workspace is opened
  //     on the IDE
  // 4 - Initialize the list of workspaces (workspace roots) that WorkspaceManager should have to
  //     apply search operations

  @Before
  public void initActivities() {
    // the delegate will prepare the structure and this method will just setup the list of workspace
    // folders
    fileSystemService.setWorkspaceFolders(initWorkspaceFolderList());
  }

  @After
  public void cleanupTempFolder() {
    try {
      Files.walk(Paths.get(getWorkspaceFolderPath()))
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /*
   * This test take in input a copybook name, retrieve the URI of the file (if it exists) and in case affermative return the
   * content, null otherwise.
   */
  @Test
  public void getContentByCopybookName() {
    assertNotNull(fileSystemService.getContentByCopybookName(CPY_OUTER_NAME_ONLY2));
  }

  @Test
  public void getNullWithNotCopybookNotFound() {
    assertNull(fileSystemService.getContentByCopybookName("ANTHR_CPY"));
  }

  @Test
  public void testDataSentOnDatabus() {
    FetchedCopybookEvent fetchedCopybookEvent =
        FetchedCopybookEvent.builder().name(CPY_OUTER_NAME_ONLY2).content("SOME_CONTENT").build();

    assertTrue(
        fetchedCopybookEvent.getName().length() > 0
            && fetchedCopybookEvent.getContent().length() > 0);
  }

  /**
   * This test verify that the dependency file is created when a new copybook is found by the
   * preprocessor.
   */
  @Test
  public void depFileGenerationPositiveTest() {
    Path depFileReference = getDepFilePathReference();
    assertTrue(Files.exists(depFileReference));
  }

  /**
   * This test verify that an already defined dependency file is updated with a new copybook
   * required to be downloaded.
   */
  @Test
  public void depFileUpdatePositiveTest() {
    // create the dep folder with the SOMPROG.dep
    Path depFileReference = getDepFilePathReference();

    // check the number of elements before the update
    int numberOfElements = getNumberOfElementsFromDepFile(depFileReference);

    // update dep file with a new copybook
    fileSystemService.updateDependencyList(depFileReference, "ANEWCPY2");
    // read the list again..
    // assert that number of element change by 1
    assertEquals(numberOfElements + 1, getNumberOfElementsFromDepFile(depFileReference));
  }

  /** This test verify that an empty value for the copybook is not added in the deplist. */
  @Test
  public void depFileWithoutEmptyCopybookName() throws IOException, URISyntaxException {
    // create the dep folder with the SOMPROG.dep
    Path depFileReference = getDepFilePathReference();

    // check the number of elements before the update
    int numberOfElements = getNumberOfElementsFromDepFile(depFileReference);

    // update dep file with a new copybook
    fileSystemService.addCopybookInDepFile(EMPTY_COPYBOOK_NAME, DOCUMENT_URI);

    // assert that number of element didn't change
    assertEquals(numberOfElements, getNumberOfElementsFromDepFile(depFileReference));
  }

  private int getNumberOfElementsFromDepFile(Path depFileReference) {
    List<String> depFileReferenceElementList;

    depFileReferenceElementList = getElementListFromDepFile(depFileReference);

    assert depFileReferenceElementList != null;
    return depFileReferenceElementList.size();
  }

  private List<String> getElementListFromDepFile(Path depFileReference) {
    try {
      return Files.readAllLines(depFileReference);
    } catch (IOException e) {
      log.error(e.getLocalizedMessage());
      return null;
    }
  }

  private Path getDepFilePathReference() {
    fileSystemService.generateDependencyFile(DEP_FILE_COST_NAME, getDependencyFolder());
    return Paths.get(
        getDependencyFolder() + filesystemSeparator() + DEP_FILE_COST_NAME + DEP_EXTENSION);
  }

  private Path getDependencyFolder() {
    try {
      return Paths.get(new URI(getWorkspaceFolderPath() + ".cobdeps"));
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public void observerCallback(RequiredCopybookEvent event) {
    log.info(event.toString());
    log.info("OBSERVER CALLBACK FROM TEST METHOD");
  }
}