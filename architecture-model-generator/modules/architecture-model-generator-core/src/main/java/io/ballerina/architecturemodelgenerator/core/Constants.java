/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.architecturemodelgenerator.core;

import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Constants use for Solution Architecture model generation.
 *
 * @since 1.0.0
 */
public class Constants {

    /**
     * Enum to select the type of the parameter.
     */
    public enum ParameterIn {
        BODY("body"),
        QUERY("query"),
        HEADER("header"),
        PATH("path");

        private final String parameterIn;

        ParameterIn(String parameterIn) {
            this.parameterIn = parameterIn;
        }

        public String getValue() {
            return this.parameterIn;
        }
    }

    public static final String MODEL_VERSION = "0.4.0";
    public static final String COLON = ":";
    public static final String FORWARD_SLASH = "/";
    public static final String SERVICE_ANNOTATION = "choreo:Service";
    public static final String DISPLAY_ANNOTATION = "display";
    public static final String CLIENT_ANNOTATION = "choreo:Client";
    public static final String ID = "id";
    public static final String LABEL = "label";
    public static final String MODULES = "modules";
    public static final String SERVICE_ID = "serviceId";
    public static final String ARRAY = "[]";
    public static final String LISTENER = ":Listener";
    public static final String CLIENT = ":Client";
    public static final String MAIN = "main";

    // Entity Model Generator Related Consts
    public static final String CONSTRAINT_KEYWORD = "constraint";
    public static final String CONSTRAINT_ARRAY = "Array";
    public static final String MIN_LENGTH_FIELD = "minLength";
    public static final String MAX_LENGTH_FIELD = "maxLength";
    public static final String DEFAULT_SERVICE_BASE_PATH = "_defaultBasePath";

    // REST Methods
    public static final String GET_KEYWORD = "get";

    /**
     * Enum for cardinality types.
     */
    public enum CardinalityValue {
        ZERO("0"),
        ZERO_OR_ONE("0-1"),
        ZERO_OR_MANY("0-m"),
        ONE("1"),
        ONE_AND_ONLY_ONE("1-1"),
        ONE_OR_MANY("1-m"),
        MANY("m"),
        CUSTOM();

        private final String cardinalityValue;

        CardinalityValue(String cardinalityValue) {
            this.cardinalityValue = cardinalityValue;
        }

        CardinalityValue() {
            this.cardinalityValue = "";
        }

        public String getValue() {
            return this.cardinalityValue;
        }

        public String getCustomValue(String min, String max) {
            return min + "-" + max;
        }
    }

    public static final Map<SyntaxKind, String> TYPE_MAP;

    // todo: for hex literals ?
    static {
        Map<SyntaxKind, String> typeMap = new HashMap<>();
        typeMap.put(SyntaxKind.STRING_LITERAL, "string");
        typeMap.put(SyntaxKind.BOOLEAN_LITERAL, "boolean");
        typeMap.put(SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN, "float");
        typeMap.put(SyntaxKind.NUMERIC_LITERAL, "decimal");
        typeMap.put(SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN, "float");
        TYPE_MAP = Collections.unmodifiableMap(typeMap);
    }

}
