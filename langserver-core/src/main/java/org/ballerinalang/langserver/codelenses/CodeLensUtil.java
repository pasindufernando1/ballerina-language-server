/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codelenses;

import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.codelenses.spi.LSCodeLensesProvider;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.TextDocumentIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides code lenses related common functionalities.
 *
 * @since 1.0.0
 */
public final class CodeLensUtil {

    private CodeLensUtil() {
    }

    /**
     * Compile and get code lenses.
     *
     * @param codeLensContext LSContext
     * @return a list of code lenses
     */
    public static List<CodeLens> getCodeLenses(DocumentServiceContext codeLensContext, TextDocumentIdentifier txtDoc) {
        List<CodeLens> lenses = new ArrayList<>();
        List<LSCodeLensesProvider> providers = LSCodeLensesProviderHolder
                .getInstance(codeLensContext.languageServercontext()).getProviders();
        for (LSCodeLensesProvider provider : providers) {
            codeLensContext.checkCancelled();
            lenses.addAll(provider.getLenses(codeLensContext));
        }
        return lenses;
    }
}
