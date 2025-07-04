/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.commons.workspace;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleCompilation;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import org.ballerinalang.langserver.commons.eventsync.exceptions.EventSyncException;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Contains a set of utility methods to manage projects.
 *
 * @since 1.0.0
 */
public interface WorkspaceManager {

    /**
     * Get the relative file path of the document in the given path.
     *
     * @param path document path to evaluate
     * @return {@link String} relative path
     */
    Optional<String> relativePath(Path path);

    /**
     * Get the relative file path of the document in the given path.
     *
     * @param path          document path to evaluate
     * @param cancelChecker Cancel checker for the operation which calls this method
     * @return {@link String} relative path
     */
    Optional<String> relativePath(Path path, CancelChecker cancelChecker);

    /**
     * Returns a project root from the path provided.
     *
     * @param path ballerina project or standalone file path
     * @return project root
     */
    Path projectRoot(Path path);

    /**
     * Returns a project root from the path provided.
     *
     * @param path          ballerina project or standalone file path
     * @param cancelChecker Cancel checker for the operation which calls this method
     * @return project root
     */
    Path projectRoot(Path path, CancelChecker cancelChecker);

    /**
     * Returns project from the path provided.
     *
     * @param filePath ballerina project or standalone file path
     * @return project of applicable type
     */
    Optional<Project> project(Path filePath);

    /**
     * Load the project from the path provided.
     *
     * @param filePath ballerina project or standalone file path
     * @return project of applicable type
     * @throws ProjectException when the filePath is invalid
     */
    Project loadProject(Path filePath) throws ProjectException, WorkspaceDocumentException, EventSyncException;

    /**
     * Returns module from the path provided.
     *
     * @param filePath file path of the document
     * @return project of applicable type
     */
    Optional<Module> module(Path filePath);

    /**
     * Returns module from the path provided.
     *
     * @param filePath      file path of the document
     * @param cancelChecker Cancel checker for the operation which calls this method
     * @return project of applicable type
     */
    Optional<Module> module(Path filePath, CancelChecker cancelChecker);

    /**
     * Returns document of the project of this path.
     *
     * @param filePath file path of the document
     * @return {@link Document}
     */
    Optional<Document> document(Path filePath);

    /**
     * Returns document of the project of this path.
     *
     * @param filePath      file path of the document
     * @param cancelChecker Cancel checker for the operation which calls this method
     * @return {@link Document}
     */
    Optional<Document> document(Path filePath, CancelChecker cancelChecker);

    /**
     * Returns syntax tree from the path provided.
     *
     * @param filePath file path of the document
     * @return {@link SyntaxTree}
     */
    Optional<SyntaxTree> syntaxTree(Path filePath);

    /**
     * Returns syntax tree from the path provided.
     *
     * @param filePath      file path of the document
     * @param cancelChecker Cancel checker for the operation which calls this method
     * @return {@link SyntaxTree}
     */
    Optional<SyntaxTree> syntaxTree(Path filePath, CancelChecker cancelChecker);

    /**
     * Returns semantic model from the path provided.
     *
     * @param filePath file path of the document
     * @return project of applicable type
     */
    Optional<SemanticModel> semanticModel(Path filePath);

    /**
     * Returns semantic model from the path provided.
     *
     * @param filePath      file path of the document
     * @param cancelChecker Cancel checker for the operation which calls this method
     * @return project of applicable type
     */
    Optional<SemanticModel> semanticModel(Path filePath, @NonNull CancelChecker cancelChecker);

    /**
     * Returns module compilation from the file path provided.
     *
     * @param filePath file path of the document
     * @return {@link ModuleCompilation}
     */
    Optional<PackageCompilation> waitAndGetPackageCompilation(Path filePath);

    /**
     * Returns module compilation from the file path provided.
     *
     * @param filePath      file path of the document
     * @param cancelChecker Cancel checker for the operation which calls this method
     * @return {@link ModuleCompilation}
     */
    Optional<PackageCompilation> waitAndGetPackageCompilation(Path filePath, CancelChecker cancelChecker);

    /**
     * The document open notification is sent from the client to the server to signal newly opened text documents.
     *
     * @param filePath {@link Path} of the document
     * @param params   {@link DidOpenTextDocumentParams}
     * @throws WorkspaceDocumentException when project or document not found
     */
    void didOpen(Path filePath, DidOpenTextDocumentParams params) throws WorkspaceDocumentException;

    /**
     * The document change notification is sent from the client to the server to signal changes to a text document.
     *
     * @param filePath {@link Path} of the document
     * @param params   {@link DidChangeTextDocumentParams}
     * @throws WorkspaceDocumentException when project or document not found
     */
    void didChange(Path filePath, DidChangeTextDocumentParams params) throws WorkspaceDocumentException;

    /**
     * The document close notification is sent from the client to the server when the document got closed in the
     * client.
     *
     * @param filePath {@link Path} of the document
     * @param params   {@link DidCloseTextDocumentParams}
     */
    void didClose(Path filePath, DidCloseTextDocumentParams params);

    /**
     * The file change notification is sent from the client to the server to signal changes to watched files.
     *
     * @param filePath  {@link Path} of the document
     * @param fileEvent {@link FileEvent}
     * @throws WorkspaceDocumentException when project or document not found
     */
    void didChangeWatched(Path filePath, FileEvent fileEvent) throws WorkspaceDocumentException;

    /**
     * The file change notification is sent from the client to the server to signal changes to watched files.
     *
     * @param params watched files event parameters
     * @return list of project roots which were reloaded. If a project has not been reloaded,
     * then the list will be empty
     * @throws WorkspaceDocumentException when project or document not found
     */
    List<Path> didChangeWatched(DidChangeWatchedFilesParams params) throws WorkspaceDocumentException;

    /**
     * Get the URI scheme associated with the given workspace manager.
     * 
     * @return {@link String}
     */
    String uriScheme();

    /**
     * Compiles and runs the project of the given file path. Run happens in a separate process.
     *
     * @param runContext context related to the project to be run.
     * @return Process created by running the project. Empty if failed due to non process related issues.
     * @throws IOException If failed to start the process.
     * @since 1.0.0
     */
    RunResult run(RunContext runContext) throws IOException;

    /**
     * Stop a running process started with {@link #run}.
     * @param filePath Path that belongs to the project to be stopped.
     * @return {@code true} if the process was stopped successfully (or already dead), {@code false} otherwise.
     * @since 1.0.0
     */
    boolean stop(Path filePath);

    /**
     * Returns the map of projects loaded in the workspace manager.
     *
     * @return map of project's source root to project 
     */
    CompletableFuture<Map<Path, Project>> workspaceProjects();
}
