/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.capability.InitializationOptions;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.util.TestUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test cases for Extract to Constant code action.
 * 
 * @since 1.0.0
 */
public class ExtractToConstantTest extends AbstractCodeActionTest {

    @Override
    protected void setupLanguageServer(TestUtil.LanguageServerBuilder builder) {
        builder.withInitOption(InitializationOptions.KEY_POSITIONAL_RENAME_SUPPORT, true);
    }

    @Override
    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @Override
    @Test(dataProvider = "negative-test-data-provider")
    public void negativeTest(String config) throws IOException, WorkspaceDocumentException {
        super.negativeTest(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"extractIntToConstant.json"},
                {"extractHexIntToConstant.json"},
                {"extractFloatingPointToConstant.json"},
                {"extractHexFloatingPointToConstant.json"},
                {"extractBooleanToConstant.json"},
                {"extractStringToConstant.json"},
                {"extractClassDefToConstant.json"},
                {"extractIntRangeToConstant.json"},
                {"extractExpressionToConstant.json"},
                {"extractConstDeclToConstant1.json"},
                {"extractToConstantWithImports.json"},
                {"extractToConstantWithImports2.json"},
                {"extractToConstantWithImports3.json"},
                {"extractUnaryNumericExprToConstant.json"},
                {"extractUnaryNumericExprToConstant2.json"},
                {"extractUnaryLogicalExprToConstant.json"},
                {"extractBooleanLiteralInUnaryExprToConstant.json"},
                {"extractNumericLiteralInUnaryExprToConstant.json"},
                {"extractNumericLiteralInUnaryExprToConstant2.json"},
                {"extractExprToConstant1.json"},
                {"extractExprToConstant2.json"},
                {"extractTwoLinesToConstant.json"},
                {"extractToConstantWithAmbiguousType1.json"},
                {"extractToConstantWithAmbiguousType2.json"},
                {"extractToConstantWithAmbiguousType3.json"},
                {"extractToConstantWithAmbiguousType4.json"},
                {"extractToConstantWithAmbiguousType5.json"},
                {"extractToConstantWithAmbiguousType6.json"},
                {"extractToConstantWithAmbiguousType7.json"},
                {"extractToConstantWithAmbiguousType8.json"},
                {"extractToConstantWithAmbiguousType9.json"},
                {"extractToConstantWithAmbiguousType10.json"},
        };
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"extractConstDeclToConstant.json"},
                {"extractInvalidExprStmtToConstant.json"},
                {"extractExpressionToConstant1.json"},
                {"extractExpressionToConstant2.json"},
                {"extractExpressionToConstant3.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "extract-to-constant";
    }
}
