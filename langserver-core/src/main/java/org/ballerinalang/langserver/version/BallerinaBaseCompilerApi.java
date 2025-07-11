/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.ballerinalang.langserver.version;

import io.ballerina.compiler.api.Types;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ExpressionFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompilerApi;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.Map;
import java.util.Optional;

/**
 * The base fallback implementation for the Ballerina Compiler API.
 * <p>
 * This implementation is used as a default when a version-specific implementation for the current Ballerina environment
 * cannot be found.
 *
 * @since 1.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.BallerinaCompilerApi")
public class BallerinaBaseCompilerApi extends BallerinaCompilerApi {

    @Override
    public String getVersion() {
        return "2201.0.0";
    }

    @Override
    public boolean isNaturalExpressionBody(ExpressionFunctionBodyNode expressionFunctionBodyNode) {
        return false;
    }

    @Override
    public boolean isNaturalExpressionBodiedFunction(FunctionDefinitionNode functionDefNode) {
        return false;
    }

    @Override
    public boolean hasOptimizedDependencyCompilation(Project project) {
        return false;
    }

    @Override
    public Optional<TypeSymbol> getType(Types types, Document document, String typeName,
                                        Map<String, BLangPackage> packageMap) {
        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> getType(Types types, Document document, String typeName) {
        return Optional.empty();
    }
}
