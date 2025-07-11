/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.MatchGuardNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion resolver for the {@link MatchGuardNode}.
 * 
 * @since 1.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class MatchGuardNodeContext extends AbstractCompletionProvider<MatchGuardNode> {

    public MatchGuardNodeContext() {
        super(MatchGuardNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context,
                                                 MatchGuardNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
            List<Symbol> symbols = QNameRefCompletionUtil.getExpressionContextEntries(context, qNameRef);
            completionItems.addAll(this.getCompletionItemList(symbols, context));
        } else {
            completionItems.addAll(this.expressionCompletions(context));
        }
        this.sort(context, node, completionItems);
        
        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, MatchGuardNode node) {
        Token ifKeyword = node.ifKeyword();
        int cursor = context.getCursorPositionInTree();

        return !ifKeyword.isMissing() && cursor > ifKeyword.textRange().endOffset();
    }
}
