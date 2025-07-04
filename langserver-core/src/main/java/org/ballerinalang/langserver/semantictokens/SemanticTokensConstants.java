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
package org.ballerinalang.langserver.semantictokens;

import java.util.UUID;

/**
 * Constants related to the semantic tokens APIs.
 *
 * @since 1.0.0
 */
public final class SemanticTokensConstants {

    private SemanticTokensConstants() {
    }

    public static final String REGISTRATION_ID = UUID.randomUUID().toString();

    public static final String REQUEST_METHOD = "textDocument/semanticTokens";

    static final String SELF = "self";

    static final String READONLY = "readonly";

    static final String RETURN = "return";
}
