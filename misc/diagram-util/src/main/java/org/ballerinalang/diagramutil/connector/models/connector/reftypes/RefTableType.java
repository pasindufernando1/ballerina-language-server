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
package org.ballerinalang.diagramutil.connector.models.connector.reftypes;

import com.google.gson.annotations.Expose;
import org.ballerinalang.diagramutil.connector.models.connector.RefType;

import java.util.List;


/**
 * Table type model.
 */
public class RefTableType extends RefType {
    @Expose
    public RefType rowType;

    @Expose
    public List<String> keys;

    @Expose
    public RefType constraintType;

    public RefTableType(RefType rowType, List<String> keys, RefType constraintType) {
        this.typeName = "table";
        this.keys = keys;
        this.rowType = rowType;
        this.constraintType = constraintType;
    }

    public RefTableType(RefTableType tableType, boolean isFullType, boolean needDependentTypes) {
        this.typeName = "table";
        if (isFullType) {
            this.rowType = tableType.rowType;
        }
        if (!needDependentTypes) {
            if(this.rowType != null) {
                this.rowType.dependentTypes = null;
            }
        }
    }

}
