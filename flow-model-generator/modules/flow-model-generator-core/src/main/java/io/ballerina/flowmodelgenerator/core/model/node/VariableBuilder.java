/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
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

package io.ballerina.flowmodelgenerator.core.model.node;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.flowmodelgenerator.core.model.NodeBuilder;
import io.ballerina.flowmodelgenerator.core.model.NodeKind;
import io.ballerina.flowmodelgenerator.core.model.Property;
import io.ballerina.flowmodelgenerator.core.model.SourceBuilder;
import io.ballerina.projects.Document;
import org.ballerinalang.langserver.common.utils.DefaultValueGenerationUtil;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the properties of a variable declaration node.
 *
 * @since 1.0.0
 */
public class VariableBuilder extends NodeBuilder {

    public static final String LABEL = "Declare Variable";
    public static final String DESCRIPTION = "New variable with type";
    public static final String EXPRESSION_DOC = "Initialize with value";

    @Override
    public void setConcreteConstData() {
        metadata().label(LABEL);
        codedata().node(NodeKind.VARIABLE);
    }

    @Override
    public Map<Path, List<TextEdit>> toSource(SourceBuilder sourceBuilder) {
        Property flowNodeType = sourceBuilder.flowNode.properties().get("type");
        String value = flowNodeType.value().toString();
        Path filePath = sourceBuilder.filePath;
        SemanticModel semanticModel = sourceBuilder.workspaceManager.semanticModel(filePath).orElseThrow();
        Document document = sourceBuilder.workspaceManager.document(filePath).orElseThrow();
        TypeSymbol typeSymbol = semanticModel.types().getType(document, value).orElseThrow();
        String nodeType;
        if (typeSymbol.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            nodeType = ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor().typeKind().toString();
        } else {
            nodeType = typeSymbol.typeKind().toString();
        }

        String defaultValue = null;
        if (nodeType.equals("RECORD")) {
            defaultValue = "{}";
        } else if (nodeType.equals("ARRAY")) {
            defaultValue = "[]";
        }

        Optional<Property> type = sourceBuilder.getProperty(Property.TYPE_KEY);
        Optional<Property> variable = sourceBuilder.getProperty(Property.VARIABLE_KEY);
        if (type.isPresent() && variable.isPresent()) {
            sourceBuilder.token().expressionWithType(type.get(), variable.get());
        }

        Optional<Property> exprProperty = sourceBuilder.getProperty(Property.EXPRESSION_KEY);
        if (exprProperty.isPresent() && !exprProperty.get().toSourceCode().isEmpty()) {
            sourceBuilder.token()
                    .keyword(SyntaxKind.EQUAL_TOKEN)
                    .expression(exprProperty.get());
        } else if (defaultValue != null) {
            sourceBuilder.token()
                    .keyword(SyntaxKind.EQUAL_TOKEN)
                    .expression(defaultValue);
        }

        sourceBuilder.token().endOfStatement();
        return sourceBuilder
                .textEdit()
                .build();
    }

    @Override
    public void setConcreteTemplateData(TemplateContext context) {
        metadata().description(DESCRIPTION);
        properties()
                .dataVariable(null, true, context.getAllVisibleSymbolNames())
                .expressionOrAction(null, EXPRESSION_DOC, true);
    }
}
