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

package io.ballerina.architecturemodelgenerator.extension.architecture;

import io.ballerina.architecturemodelgenerator.extension.Constants;
import org.ballerinalang.langserver.commons.registration.BallerinaServerCapability;

/**
 * Server capabilities for the solution architecture modeling service.
 *
 * @since 1.0.0
 */
public class ArchitectureModelGeneratorServerCapabilities extends BallerinaServerCapability {

    private boolean getMultiServiceModel;

    public ArchitectureModelGeneratorServerCapabilities() {
        super(Constants.CAPABILITY_NAME);
    }

    public boolean isGetMultiServiceModel() {
        return getMultiServiceModel;
    }

    public void setGetMultiServiceModel(boolean getMultiServiceModel) {
        this.getMultiServiceModel = getMultiServiceModel;
    }
}
