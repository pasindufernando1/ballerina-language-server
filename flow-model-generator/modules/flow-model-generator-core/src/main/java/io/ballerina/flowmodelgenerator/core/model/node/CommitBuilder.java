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
import io.ballerina.flowmodelgenerator.core.model.NodeBuilder;
import io.ballerina.flowmodelgenerator.core.model.NodeKind;
import io.ballerina.flowmodelgenerator.core.model.SourceBuilder;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Represents the properties of a commit node.
 *
 * @since 1.0.0
 */
public class CommitBuilder extends NodeBuilder {

    public static final String LABEL = "Commit";
    public static final String DESCRIPTION = "Commit transaction";

    @Override
    public void setConcreteConstData() {
        metadata().label(LABEL).description(DESCRIPTION);
        codedata().node(NodeKind.COMMIT);
    }

    @Override
    public void setConcreteTemplateData(TemplateContext context) {
    }

    @Override
    public Map<Path, List<TextEdit>> toSource(SourceBuilder sourceBuilder) {
        if (sourceBuilder.flowNode.hasFlag(FlowNode.NODE_FLAG_CHECKED)) {
            sourceBuilder.token().keyword(SyntaxKind.CHECK_KEYWORD);
        }

        return sourceBuilder
                .token()
                .keyword(SyntaxKind.COMMIT_KEYWORD)
                .endOfStatement()
                .stepOut()
                .textEdit().build();
    }
}
