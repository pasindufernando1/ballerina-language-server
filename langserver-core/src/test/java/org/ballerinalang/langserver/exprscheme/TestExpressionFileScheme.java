/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.exprscheme;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Tests the expression file scheme based workspace proxy operations.
 * 
 * @since 1.0.0
 */
public class TestExpressionFileScheme {
    private static final Path RESOURCE_DIRECTORY = Path.of("src/test/resources/project");
    protected Endpoint serviceEndpoint;

    @BeforeClass
    public void init() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
        this.serviceEndpoint = null;
    }

    @Test
    public void testExprScheme() throws IOException {
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject2").resolve("main.bal");

        byte[] encodedContent = Files.readAllBytes(filePath);
        String originalContent = new String(encodedContent);
        URI originalUri = URI.create(filePath.toUri().toString());
        URI exprUri = URI.create(filePath.toUri().toString().replace("file:///", "expr:///"));

        TestUtil.openDocument(serviceEndpoint, originalUri.toString(), originalContent);
        TestUtil.openDocument(serviceEndpoint, exprUri.toString(), originalContent);

        // Get the completions over the original content.
        Position pos1 = new Position(1, 2);
        String completionRes1 = TestUtil.getCompletionResponse(originalUri, pos1, serviceEndpoint, "");

        // send a did change with the expr scheme
        String exprContent = originalContent + System.lineSeparator() + "function exprEdit() {}";
        TestUtil.didChangeDocument(serviceEndpoint, exprUri, exprContent);

        // Get completions over the original content after changing the content over expr scheme
        String completionRes2 = TestUtil.getCompletionResponse(originalUri, pos1, serviceEndpoint, "");
        // Both the responses should be the same and ensures the original content is not affected 
        Assert.assertEquals(completionRes1, completionRes2);

        // Get completions over the expr content after changing the content over expr scheme
        String completionRes3 = TestUtil.getCompletionResponse(exprUri, pos1, serviceEndpoint, "");
        // Check whether the expr content change has modified the content
        JsonArray exprCompletions = JsonParser.parseString(completionRes3)
                .getAsJsonObject().get("result")
                .getAsJsonObject().get("left")
                .getAsJsonArray();
        Assert.assertFalse(exprCompletions.isEmpty(), "Expr context completions cannot be empty");
        Assert.assertNotEquals(completionRes1, completionRes3);

        TestUtil.closeDocument(serviceEndpoint, originalUri.toString());
        TestUtil.closeDocument(serviceEndpoint, exprUri.toString());
    }

    /**
     * Tests the code action requests using file and expr schemes.
     *
     * @throws IOException IO Errors
     */
    @Test
    public void testExprSchemeCodeActions() throws IOException {
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject2").resolve("main.bal");

        byte[] encodedContent = Files.readAllBytes(filePath);
        String originalContent = new String(encodedContent);
        URI originalUri = URI.create(filePath.toUri().toString());
        URI exprUri = URI.create(filePath.toUri().toString().replace("file:///", "expr:///"));

        // Send a did change with the expr scheme. Adds a function call to getData() function.
        String[] lines = originalContent.split(System.lineSeparator());
        lines[1] = "getData()";
        String exprContent = String.join(System.lineSeparator(), lines);
        TestUtil.didChangeDocument(serviceEndpoint, exprUri, exprContent);
        
        Position pos = new Position(1, 1);
        // Get expr scheme code actions
        String exprCodeActionResponse = TestUtil.getCodeActionResponse(serviceEndpoint, 
                new TextDocumentIdentifier(exprUri.toString()), 
                new Range(pos, pos), new CodeActionContext(new ArrayList<>()));

        // Get original scheme code actions
        String originalCodeActionResponse = TestUtil.getCodeActionResponse(serviceEndpoint,
                new TextDocumentIdentifier(originalUri.toString()),
                new Range(pos, pos), new CodeActionContext(new ArrayList<>()));
        
        // Original and expr code action responses should be different
        JsonArray originalCodeActions = JsonParser.parseString(originalCodeActionResponse).getAsJsonObject()
                        .get("result").getAsJsonArray();
        JsonArray exprCodeActions = JsonParser.parseString(exprCodeActionResponse).getAsJsonObject()
                        .get("result").getAsJsonArray();
        Assert.assertEquals(originalCodeActions.size(), 0);
        Assert.assertEquals(exprCodeActions.size(), 4);

        TestUtil.openDocument(serviceEndpoint, originalUri.toString(), originalContent);
        TestUtil.openDocument(serviceEndpoint, exprUri.toString(), originalContent);
    }
}
