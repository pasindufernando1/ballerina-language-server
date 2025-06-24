/*
 *  Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.diagramutil.connector.models.connector;

import com.google.gson.annotations.Expose;
import io.ballerina.compiler.api.symbols.*;

import java.util.*;

/**
 * Reference-based type model.
 *
 * @since language-server 1.0.0
 */
public class RefType {
    private static final Map<String, Type> visitedTypeMap = new HashMap<>();

    public static class Type {
        Set<String> dependentTypeHashes = new HashSet<>();
        @Expose
        public String hashCode;
        @Expose
        public String name;
        @Expose
        public Map<String, Type> dependentTypes;

        public Type(String name) {
            this.name = name;
        }

    }

    public static class RecordType extends Type {
        @Expose
        List<Field> fields = new ArrayList<>();

        public RecordType(String name) {
            super(name);
        }
    }

    public static class ArrayType extends Type {
        @Expose
        Type elementType;

        public ArrayType(String name) {
            super(name);
        }
    }

    public record Field(String fieldName, Type type, boolean optional, String defaultValue) {
    }

    public static Type fromSymbol(Symbol symbol) {
        SymbolKind kind = symbol.kind();
        TypeSymbol typeSymbol = null;
        if (kind == SymbolKind.TYPE_DEFINITION) {
            typeSymbol = ((TypeDefinitionSymbol) symbol).typeDescriptor();
        } else if (kind == SymbolKind.PARAMETER) {
            typeSymbol = ((ParameterSymbol) symbol).typeDescriptor();
        } else if (kind == SymbolKind.RECORD_FIELD) {
            typeSymbol = ((RecordFieldSymbol) symbol).typeDescriptor();
        } else if (kind == SymbolKind.VARIABLE) {
            typeSymbol = ((VariableSymbol) symbol).typeDescriptor();
        } else if (kind == SymbolKind.TYPE) {
            typeSymbol = (TypeSymbol) symbol;
        }

        if (typeSymbol == null) {
            return null;
        }
        Type type = fromSymbol(typeSymbol, symbol.getName().orElseThrow());

        for (String dependentTypeHash : type.dependentTypeHashes) {
            Type dependentType = visitedTypeMap.get(dependentTypeHash);
            if (dependentType != null) {
                if (type.dependentTypes == null) {
                    type.dependentTypes = new HashMap<>();
                }
                type.dependentTypes.put(dependentTypeHash, dependentType);
            }
        }
        return type;
    }

    public static Type fromSymbol(TypeSymbol symbol, String name) {
        String h = String.valueOf(symbol.hashCode());
        Type type = visitedTypeMap.get(h);
        if (type != null) {
            return type;
        }

        TypeDescKind kind = symbol.typeKind();
        if (kind == TypeDescKind.RECORD) {
            RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) symbol;
            RecordType recordType = new RecordType(name);
            recordType.hashCode = h;
            visitedTypeMap.put(h, recordType);

            Map<String, RecordFieldSymbol> fieldDescriptors = recordTypeSymbol.fieldDescriptors();
            fieldDescriptors.forEach((fieldName, fieldSymbol) -> {
                TypeSymbol fieldTypeSymbol = fieldSymbol.typeDescriptor();
                Type fieldType = fromSymbol(fieldTypeSymbol, fieldTypeSymbol.getName().orElse(""));
                if (fieldType.dependentTypeHashes == null || fieldType.dependentTypeHashes.isEmpty()) {
                    if (fieldType.hashCode != null) {
                        Type t = new Type(fieldType.name);
                        t.hashCode = fieldType.hashCode;
                        recordType.fields.add(new Field(fieldName, t, fieldSymbol.isOptional(), ""));
                    } else {
                        recordType.fields.add(new Field(fieldName, fieldType, fieldSymbol.isOptional(), ""));
                    }
                } else {
                    Type t = new Type(fieldType.name);
                    t.hashCode = fieldType.hashCode;
                    recordType.dependentTypeHashes.addAll(fieldType.dependentTypeHashes);
                    recordType.fields.add(new Field(fieldName, t, fieldSymbol.isOptional(), ""));
                }
                if (fieldType.hashCode != null) {
                    recordType.dependentTypeHashes.add(fieldType.hashCode);
                }
            });

            return recordType;
        } else if (kind == TypeDescKind.TYPE_REFERENCE) {
            TypeReferenceTypeSymbol typeRefSymbol = (TypeReferenceTypeSymbol) symbol;
            TypeSymbol typeSymbol = typeRefSymbol.typeDescriptor();
            return fromSymbol(typeSymbol, name);
        } else if (kind == TypeDescKind.INT) {
            return new Type("int");
        } else if (kind == TypeDescKind.STRING) {
            return new Type("string");
        }
        throw new UnsupportedOperationException(
                "Unsupported type kind: " + kind + " for symbol: " + symbol.getName().orElse("unknown"));
    }
}