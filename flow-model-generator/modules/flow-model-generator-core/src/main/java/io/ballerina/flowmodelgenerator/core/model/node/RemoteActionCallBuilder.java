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

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.flowmodelgenerator.core.model.FlowNode;
import io.ballerina.flowmodelgenerator.core.model.NodeKind;
import io.ballerina.flowmodelgenerator.core.model.Property;
import io.ballerina.flowmodelgenerator.core.model.SourceBuilder;
import io.ballerina.flowmodelgenerator.core.utils.FlowNodeUtil;
import io.ballerina.modelgenerator.commons.FunctionData;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents the generalized action invocation node in the flow model.
 *
 * @since 1.0.0
 */
public class RemoteActionCallBuilder extends CallBuilder {

    public static final String TARGET_TYPE_KEY = "targetType";

    @Override
    public Map<Path, List<TextEdit>> toSource(SourceBuilder sourceBuilder) {
        sourceBuilder.newVariableWithInferredType();
        FlowNode flowNode = sourceBuilder.flowNode;

        if (FlowNodeUtil.hasCheckKeyFlagSet(flowNode)) {
            sourceBuilder.token().keyword(SyntaxKind.CHECK_KEYWORD);
        }

        Optional<Property> connection = sourceBuilder.getProperty(Property.CONNECTION_KEY);
        if (connection.isEmpty()) {
            throw new IllegalStateException("Client must be defined for an action call node");
        }

        return sourceBuilder.token()
                .name(connection.get().toSourceCode())
                .keyword(SyntaxKind.RIGHT_ARROW_TOKEN)
                .name(flowNode.metadata().label())
                .stepOut()
                .functionParameters(flowNode,
                        Set.of(Property.CONNECTION_KEY, Property.VARIABLE_KEY, Property.TYPE_KEY,
                                Property.CHECK_ERROR_KEY))
                .textEdit()
                .acceptImportWithVariableType()
                .build();
    }

    @Override
    protected NodeKind getFunctionNodeKind() {
        return NodeKind.REMOTE_ACTION_CALL;
    }

    @Override
    protected FunctionData.Kind getFunctionResultKind() {
        return FunctionData.Kind.REMOTE;
    }
}
