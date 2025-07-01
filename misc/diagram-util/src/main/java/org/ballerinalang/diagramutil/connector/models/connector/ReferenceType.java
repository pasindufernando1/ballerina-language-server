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

public class ReferenceType {
    private static final Map<String, RefType> visitedTypeMap = new HashMap<>();
    
    public record Field(String fieldName, RefType type, boolean optional, String defaultValue) {
        public String getName() {
            return fieldName;
        }

        public RefType getType() {
            return type;
        }
    }

    public static RefType fromSemanticSymbol(Symbol symbol) {
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
        } else if (kind == SymbolKind.CONSTANT) {
            typeSymbol = ((VariableSymbol) symbol).typeDescriptor();
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported symbol kind: " + kind + " for symbol: " + symbol.getName().orElse("unknown"));
        }

        if (typeSymbol == null) {
            return null;
        }

        String moduleId = symbol.getModule().isPresent()
                ? symbol.getModule().get().id().toString()
                : null;
        RefType type = fromSemanticSymbol(typeSymbol, typeSymbol.getName().orElse(""), moduleId);
        Type originaltype = Type.fromSemanticSymbol(typeSymbol);

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
        if (name == null || name.isEmpty()) {
            name = ((BallerinaSingletonTypeSymbol) symbol).getBType().toString().orElse("");
        }
        String hashCode = String.valueOf((moduleID + name).hashCode());
        TypeDescKind kind = symbol.typeKind();
        if (kind == TypeDescKind.RECORD) {

            RefType type = visitedTypeMap.get(hashCode);
            if (type != null) {
                return type;
            }

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
                        t.typeInfo = fieldType.typeInfo;
                        recordType.fields.add(new Field(fieldName, t, fieldSymbol.isOptional(), ""));
                    } else {
                        recordType.fields.add(new Field(fieldName, fieldType, fieldSymbol.isOptional(), ""));
                    }
                } else {
                    RefType t = new RefType(fieldType.name);
                    t.hashCode = fieldType.hashCode;
                    t.typeName = fieldType.typeName;
                    t.typeInfo = fieldType.typeInfo;
                    recordType.dependentTypeHashes.addAll(fieldType.dependentTypeHashes);
                    recordType.fields.add(new Field(fieldName, t, fieldSymbol.isOptional(), ""));
                }
                if (fieldType.hashCode != null) {
                    recordType.dependentTypeHashes.add(fieldType.hashCode);
                }
            });

            return recordType;
        } else if (kind == TypeDescKind.ARRAY) {
            RefType type = visitedTypeMap.get(hashCode);
            if (type != null) {
                return type;
            }
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
                    t.typeInfo = elementType.typeInfo;
                    arrayType.elementType = t;
                } else {
                    arrayType.elementType = elementType;
                }
            } else {
                RefType t = new RefType(elementType.name);
                t.hashCode = elementType.hashCode;
                t.typeName = elementType.typeName;
                t.typeInfo = elementType.typeInfo;
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
            return new RefType("int", "int");
        } else if (kind == TypeDescKind.STRING) {
            return new RefType("string", "string");
        } else if (kind == TypeDescKind.FLOAT) {
            return new RefType("float", "float");
        } else if (kind == TypeDescKind.BOOLEAN) {
            return new RefType("boolean", "boolean");
        } else if (kind == TypeDescKind.NIL) {
            return new RefType("nil", "nil");
        } else if (kind == TypeDescKind.DECIMAL) {
            return new RefType("decimal", "decimal");
        } else if (kind == TypeDescKind.NEVER) {
            return new RefType("never", "never");
        } else if (kind == TypeDescKind.SINGLETON) {
            return new RefType(name, name);
        }
        throw new UnsupportedOperationException(
                "Unsupported type kind: " + kind + " for symbol: " + symbol.getName().orElse("unknown"));
    }
}
