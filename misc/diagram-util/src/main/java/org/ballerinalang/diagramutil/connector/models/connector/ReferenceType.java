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

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import org.ballerinalang.diagramutil.connector.models.connector.reftypes.RefArrayType;
import org.ballerinalang.diagramutil.connector.models.connector.reftypes.RefRecordType;
import org.ballerinalang.diagramutil.connector.models.connector.reftypes.RefType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ReferenceType {
    private static final Map<String, RefType> visitedTypeMap = new HashMap<>();
    
    public record Field(String fieldName, RefType type, boolean optional, String defaultValue) {
        public RefType getType() {
            return type;
        }
    }

    public static RefType fromSemanticSymbol(Symbol symbol) {
        SymbolKind kind = symbol.kind();
        TypeSymbol typeSymbol = null;
        String name = "";
        if (kind == SymbolKind.TYPE_DEFINITION) {
            typeSymbol = ((TypeDefinitionSymbol) symbol).typeDescriptor();
            name = symbol.getName().orElseThrow();
        } else if (kind == SymbolKind.PARAMETER) {
            typeSymbol = ((ParameterSymbol) symbol).typeDescriptor();
            name = typeSymbol.getName().orElseThrow();
        } else if (kind == SymbolKind.RECORD_FIELD) {
            typeSymbol = ((RecordFieldSymbol) symbol).typeDescriptor();
            name = typeSymbol.getName().orElseThrow();
        } else if (kind == SymbolKind.VARIABLE) {
            typeSymbol = ((VariableSymbol) symbol).typeDescriptor();
            Optional<String> optName = typeSymbol.getName();
            // If the variable does not have a name, we can use the symbol name
            // which is usually the variable name.
            name = optName.orElseGet(() -> symbol.getName().orElseThrow());
        } else if (kind == SymbolKind.TYPE) {
            typeSymbol = (TypeSymbol) symbol;
            name = typeSymbol.getName().orElseThrow();
        } else if (kind == SymbolKind.CONSTANT) {
            typeSymbol = ((VariableSymbol) symbol).typeDescriptor();
            Optional<String> optName = typeSymbol.getName();
            name = optName.orElseGet(() -> symbol.getName().orElseThrow());
        }

        if (typeSymbol == null) {
            return null;
        }

        String moduleId = symbol.getModule().isPresent()
                ? symbol.getModule().get().id().toString()
                : null;
        RefType type = fromSemanticSymbol(typeSymbol, name, moduleId);

        for (String dependentTypeHash : type.dependentTypeHashes) {
            RefType dependentType = visitedTypeMap.get(dependentTypeHash);
            if (dependentType != null) {
                RefType clonedDependentType = dependentType.clone();
                if (type.dependentTypes == null) {
                    type.dependentTypes = new HashMap<>();
                }
                clonedDependentType.dependentTypes = null;
                if (type.dependentTypes != null) {
                    type.dependentTypes.put(dependentTypeHash, clonedDependentType);
                }
            }
        }

        return type;
    }

    public static RefType fromSemanticSymbol(TypeSymbol symbol, String name, String moduleID) {
        String hashCode = String.valueOf((moduleID + name).hashCode());
        RefType type = visitedTypeMap.get(hashCode);
        if (type != null) {
            return type;
        }

        TypeDescKind kind = symbol.typeKind();
        if (kind == TypeDescKind.RECORD) {
            RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) symbol;
            RefRecordType recordType = new RefRecordType(name);
            recordType.hashCode = hashCode;
            visitedTypeMap.put(hashCode, recordType);

            Map<String, RecordFieldSymbol> fieldDescriptors = recordTypeSymbol.fieldDescriptors();
            fieldDescriptors.forEach((fieldName, fieldSymbol) -> {
                TypeSymbol fieldTypeSymbol = fieldSymbol.typeDescriptor();
                String fieldTypeName = fieldTypeSymbol.getName().orElse("");
                String fieldModuleId = fieldSymbol.getModule().isPresent()
                        ? fieldSymbol.getModule().get().id().toString()
                        : null;
                RefType fieldType = fromSemanticSymbol(fieldTypeSymbol, fieldTypeName, fieldModuleId);
                if (fieldType.dependentTypeHashes == null || fieldType.dependentTypeHashes.isEmpty()) {
                    if (fieldType.hashCode != null) {
                        RefType t = new RefType(fieldType.name);
                        t.hashCode = fieldType.hashCode;
                        t.typeName = fieldType.typeName;
                        recordType.fields.add(new Field(fieldName, t, fieldSymbol.isOptional(), ""));
                    } else {
                        recordType.fields.add(new Field(fieldName, fieldType, fieldSymbol.isOptional(), ""));
                    }
                } else {
                    RefType t = new RefType(fieldType.name);
                    t.hashCode = fieldType.hashCode;
                    t.typeName = fieldType.typeName;
                    recordType.dependentTypeHashes.addAll(fieldType.dependentTypeHashes);
                    recordType.fields.add(new Field(fieldName, t, fieldSymbol.isOptional(), ""));
                }
                if (fieldType.hashCode != null) {
                    recordType.dependentTypeHashes.add(fieldType.hashCode);
                }
            });

            return recordType;
        } else if (kind == TypeDescKind.ARRAY) {
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) symbol;
            RefArrayType arrayType = new RefArrayType(name);
            arrayType.hashCode = hashCode;
            TypeSymbol elementTypeSymbol = arrayTypeSymbol.memberTypeDescriptor();
            String elementTypeName = elementTypeSymbol.getName().orElse("");
            String moduleId = elementTypeSymbol.getModule().isPresent()
                    ? elementTypeSymbol.getModule().get().id().toString()
                    : null;
            RefType elementType = fromSemanticSymbol(elementTypeSymbol, elementTypeName, moduleId);
            if (elementType.dependentTypeHashes == null || elementType.dependentTypeHashes.isEmpty()) {
                if (elementType.hashCode != null) {
                    RefType t = new RefType(elementType.name);
                    t.hashCode = elementType.hashCode;
                    t.typeName = elementType.typeName;
                    arrayType.elementType = t;
                } else {
                    arrayType.elementType = elementType;
                }
            } else {
                RefType t = new RefType(elementType.name);
                t.hashCode = elementType.hashCode;
                t.typeName = elementType.typeName;
                arrayType.dependentTypeHashes.addAll(elementType.dependentTypeHashes);
                arrayType.elementType = t;
            }
            if (elementType.hashCode != null) {
                arrayType.dependentTypeHashes.add(elementType.hashCode);
            }
            arrayType.hashCode = arrayType.elementType.hashCode;
            return arrayType;
        } else if (kind == TypeDescKind.TYPE_REFERENCE) {
            TypeReferenceTypeSymbol typeRefSymbol = (TypeReferenceTypeSymbol) symbol;
            TypeSymbol typeSymbol = typeRefSymbol.typeDescriptor();
            String moduleId = typeRefSymbol.getModule().isPresent()
                    ? typeRefSymbol.getModule().get().id().toString()
                    : null;
            return fromSemanticSymbol(typeSymbol, name, moduleId);
        } else if (kind == TypeDescKind.INT) {
            RefType refType = new RefType("int");
            refType.typeName = "int";
            return refType;
        } else if (kind == TypeDescKind.STRING) {
            RefType refType = new RefType("string");
            refType.typeName = "string";
            return refType;
        } else if (kind == TypeDescKind.FLOAT) {
            RefType refType = new RefType("float");
            refType.typeName = "float";
            return refType;
        } else if (kind == TypeDescKind.BOOLEAN) {
            RefType refType = new RefType("boolean");
            refType.typeName = "boolean";
            return refType;
        } else if (kind == TypeDescKind.NIL) {
            RefType refType = new RefType("nil");
            refType.typeName = "nil";
            return refType;
        } else if (kind == TypeDescKind.DECIMAL) {
            RefType refType = new RefType("decimal");
            refType.typeName = "decimal";
            return refType;
        } else if (kind == TypeDescKind.NEVER) {
            RefType refType = new RefType("never");
            refType.typeName = "never";
            return refType;
        } else if (kind == TypeDescKind.SINGLETON){
            RefType refType = new RefType("singleton");
            refType.typeName = name;
            return refType;
        }
        throw new UnsupportedOperationException(
                "Unsupported type kind: " + kind + " for symbol: " + symbol.getName().orElse("unknown"));
    }
}
