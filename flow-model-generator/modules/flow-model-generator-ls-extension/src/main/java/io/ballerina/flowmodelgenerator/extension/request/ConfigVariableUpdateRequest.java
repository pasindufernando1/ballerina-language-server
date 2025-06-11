/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
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

package io.ballerina.flowmodelgenerator.extension.request;

import com.google.gson.JsonElement;

/**
 * Represents the request to update config variables in Config API V2.
 *
 * @param packageName    name of the package
 * @param moduleName     name of the module
 * @param configFilePath path of the config file
 * @param configVariable config variable to be updated
 * @since 1.0.0
 */
public record ConfigVariableUpdateRequest(String packageName, String moduleName, String configFilePath,
                                          JsonElement configVariable) {

}
