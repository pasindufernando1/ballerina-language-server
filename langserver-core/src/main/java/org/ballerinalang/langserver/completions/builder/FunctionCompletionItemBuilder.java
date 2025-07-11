/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.builder;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.PathRestParam;
import io.ballerina.compiler.api.symbols.resourcepath.PathSegmentList;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.ModuleUtil;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.providers.context.util.ModulePartNodeContextUtil;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.SnippetGenerator;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is being used to build function type completion item.
 *
 * @since 1.0.0
 */
public final class FunctionCompletionItemBuilder {

    private FunctionCompletionItemBuilder() {
    }

    /**
     * Creates and returns a completion item.
     *
     * @param functionSymbol BSymbol
     * @param context        LS context
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(FunctionSymbol functionSymbol, BallerinaCompletionContext context) {
        CompletionItem item = new CompletionItem();
        setMeta(item, functionSymbol, context);
        if (functionSymbol != null && functionSymbol.getName().isPresent()) {
            String funcName = functionSymbol.getName().get();
            Pair<String, String> functionSignature = getFunctionInvocationSignature(functionSymbol, funcName, context);
            item.setLabel(functionSignature.getRight());
            item.setInsertText(functionSignature.getLeft());
            item.setFilterText(funcName);
        }
        return item;
    }

    /**
     * Creates and returns a completion item.
     *
     * @param functionSymbol BSymbol
     * @param context        LS context
     * @return {@link CompletionItem}
     */
    public static CompletionItem buildFunctionPointer(FunctionSymbol functionSymbol,
                                                      BallerinaCompletionContext context) {
        CompletionItem item = new CompletionItem();
        setMeta(item, functionSymbol, context);
        if (functionSymbol != null) {
            // Override function signature
            String funcName = functionSymbol.getName().orElse("");
            item.setInsertText(CommonUtil.escapeSpecialCharsInInsertText(funcName));
            item.setLabel(funcName);
            item.setFilterText(funcName);
            item.setKind(CompletionItemKind.Variable);
        }
        return item;
    }

    public static CompletionItem build(ClassSymbol typeDesc, InitializerBuildMode mode,
                                       BallerinaCompletionContext ctx) {
        MethodSymbol initMethod = null;
        if (typeDesc.initMethod().isPresent()) {
            initMethod = typeDesc.initMethod().get();
        }
        CompletionItem item = new CompletionItem();
        setMeta(item, initMethod, ctx);
        String functionName;
        if (mode == InitializerBuildMode.EXPLICIT) {
            functionName = getQualifiedFunctionName(typeDesc.getName().get(), ctx, initMethod);
        } else {
            functionName = "new";
        }
        Pair<String, String> functionSignature = getFunctionInvocationSignature(initMethod,
                functionName, ctx);
        item.setInsertText(functionSignature.getLeft());
        item.setLabel(functionSignature.getRight());

        return item;
    }

    /**
     * Creates and returns a completion item.
     *
     * @param functionSymbol BSymbol
     * @param context        LS context
     * @return {@link CompletionItem}
     */
    public static CompletionItem buildMethod(@Nonnull FunctionSymbol functionSymbol,
                                             BallerinaCompletionContext context) {
        CompletionItem item = new CompletionItem();
        setMeta(item, functionSymbol, context);
        String funcName = functionSymbol.getName().get();
        Pair<String, String> functionSignature = getFunctionInvocationSignature(functionSymbol, funcName, context);
        item.setInsertText("self." + functionSignature.getLeft());
        item.setLabel("self." + functionSignature.getRight());
        item.setFilterText("self." + funcName);

        return item;
    }

    static void setMeta(CompletionItem item, FunctionSymbol functionSymbol, BallerinaCompletionContext ctx) {
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setKind(CompletionItemKind.Function);
        if (functionSymbol != null) {
            FunctionTypeSymbol functionTypeDesc = functionSymbol.typeDescriptor();
            Optional<TypeSymbol> typeSymbol = functionTypeDesc.returnTypeDescriptor();
            typeSymbol.ifPresent(symbol -> item.setDetail(NameUtil.getModifiedTypeName(ctx, symbol)));
            List<String> funcArguments = CommonUtil.getFuncArguments(functionSymbol, ctx);
            if (!funcArguments.isEmpty()) {
                Command cmd = new Command("editor.action.triggerParameterHints", "editor.action.triggerParameterHints");
                item.setCommand(cmd);
            }
            boolean skipFirstParam = CommonUtil.skipFirstParam(ctx, functionSymbol);
            if (functionSymbol.documentation().isPresent()) {
                item.setDocumentation(getDocumentation(functionSymbol, skipFirstParam, ctx));
            }
        }
    }

    private static Either<String, MarkupContent> getDocumentation(FunctionSymbol functionSymbol,
                                                                  boolean skipFirstParam,
                                                                  BallerinaCompletionContext ctx) {
        FunctionTypeSymbol functionTypeDesc = functionSymbol.typeDescriptor();

        Optional<Documentation> docAttachment = functionSymbol.documentation();
        String description = docAttachment.isEmpty() || docAttachment.get().description().isEmpty()
                ? "" : docAttachment.get().description().get();
        Map<String, String> docParamsMap = new HashMap<>();
        docAttachment.ifPresent(documentation -> documentation.parameterMap().forEach(docParamsMap::put));

        List<ParameterSymbol> functionParameters = new ArrayList<>();
        List<ParameterSymbol> defaultParams = new ArrayList<>();

        if (functionTypeDesc.params().isPresent()) {
            functionParameters.addAll(functionTypeDesc.params().get());
            defaultParams.addAll(functionParameters.stream()
                    .filter(parameter -> parameter.paramKind() == ParameterKind.DEFAULTABLE)
                    .toList());
        }

        MarkupContent docMarkupContent = new MarkupContent();
        docMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        StringBuilder documentation = new StringBuilder();
        if (functionSymbol.getModule().isPresent()) {
            String moduleId = functionSymbol.getModule().get().id().toString();
            documentation.append("**Package:** _")
                    .append(moduleId).append("_")
                    .append(CommonUtil.MD_LINE_SEPARATOR)
                    .append(CommonUtil.MD_LINE_SEPARATOR);
        }
        documentation.append(description).append(CommonUtil.MD_LINE_SEPARATOR);

        StringJoiner joiner = new StringJoiner(CommonUtil.MD_LINE_SEPARATOR);

        //handle path parameters
        if (functionSymbol.kind() == SymbolKind.RESOURCE_METHOD) {
            ResourcePath resourcePath = ((ResourceMethodSymbol) functionSymbol).resourcePath();
            List<PathParameterSymbol> pathParameterSymbols = new ArrayList<>();
            switch (resourcePath.kind()) {
                case PATH_SEGMENT_LIST:
                    PathSegmentList pathSegmentList = (PathSegmentList) resourcePath;
                    pathParameterSymbols.addAll(pathSegmentList.pathParameters());
                    if (pathSegmentList.pathRestParameter().isPresent()) {
                        pathParameterSymbols.add(pathSegmentList.pathRestParameter().get());
                    }
                    break;
                case PATH_REST_PARAM:
                    pathParameterSymbols.add(((PathRestParam) resourcePath).parameter());
                    break;
                default:
                    //ignore
            }
            for (PathParameterSymbol pathParameterSymbol : pathParameterSymbols) {
                String paramType = NameUtil.getModifiedTypeName(ctx, pathParameterSymbol.typeDescriptor());
                StringBuilder paramDescription = new StringBuilder("- " + "`" + paramType + "`");
                pathParameterSymbol.getName().ifPresent(name -> {
                    paramDescription.append(" ").append(name);
                    if (docParamsMap.containsKey(name)) {
                        paramDescription.append(": ").append(docParamsMap.get(name));
                    }
                });
                joiner.add(paramDescription);
            }
        }

        //handle function parameters
        if (functionTypeDesc.restParam().isPresent()) {
            functionParameters.add(functionTypeDesc.restParam().get());
        }
        for (int i = 0; i < functionParameters.size(); i++) {
            ParameterSymbol param = functionParameters.get(i);
            String paramType = NameUtil.getModifiedTypeName(ctx, param.typeDescriptor());
            if (i == 0 && skipFirstParam) {
                continue;
            }

            Optional<ParameterSymbol> defaultVal = defaultParams.stream()
                    .filter(parameter -> parameter.getName().isPresent() && param.getName().isPresent()
                            && parameter.getName().get().equals(param.getName().get()))
                    .findFirst();
            StringBuilder paramDescription = new StringBuilder("- " + "`" + paramType + "`");
            param.getName().ifPresent(name -> {
                paramDescription.append(" ").append(name);
                if (docParamsMap.containsKey(name)) {
                    paramDescription.append(": ").append(docParamsMap.get(name));
                }
            });
            if (defaultVal.isPresent()) {
                joiner.add(paramDescription + "(Defaultable)");
            } else {
                joiner.add(paramDescription);
            }
        }
        String paramsStr = joiner.toString();

        if (!paramsStr.isEmpty()) {
            documentation.append("**Params**").append(CommonUtil.MD_LINE_SEPARATOR).append(paramsStr);
        }

        if (functionTypeDesc.returnTypeDescriptor().isPresent()
                && functionTypeDesc.returnTypeDescriptor().get().typeKind() != TypeDescKind.NIL) {
            // Sets the return type description only if the return type descriptor is not NIL type
            String desc = "";
            if (docAttachment.isPresent() && docAttachment.get().returnDescription().isPresent()
                    && !docAttachment.get().returnDescription().get().isEmpty()) {
                desc = "- " + CommonUtil.MD_NEW_LINE_PATTERN.matcher(docAttachment.get().returnDescription().get())
                        .replaceAll(CommonUtil.MD_LINE_SEPARATOR) + CommonUtil.MD_LINE_SEPARATOR;
            }
            documentation.append(CommonUtil.MD_LINE_SEPARATOR).append(CommonUtil.MD_LINE_SEPARATOR)
                    .append("**Return**").append(" `")
                    .append(NameUtil.getModifiedTypeName(ctx, functionTypeDesc.returnTypeDescriptor().get()))
                    .append("` ").append(CommonUtil.MD_LINE_SEPARATOR).append(desc)
                    .append(CommonUtil.MD_LINE_SEPARATOR);
        }
        docMarkupContent.setValue(documentation.toString());

        return Either.forRight(docMarkupContent);
    }

    /**
     * Get the function invocation signature.
     *
     * @param functionSymbol ballerina function instance
     * @param functionName   function name
     * @param ctx            Language Server Operation context
     * @return {@link Pair} of insert text(left-side) and signature label(right-side)
     */
    private static Pair<String, String> getFunctionInvocationSignature(FunctionSymbol functionSymbol,
                                                                       String functionName,
                                                                       BallerinaCompletionContext ctx) {
        String escapedFunctionName = CommonUtil.escapeEscapeCharsInIdentifier(functionName);
        if (functionSymbol == null) {
            return ImmutablePair.of(escapedFunctionName + "()", functionName + "()");
        }
        StringBuilder signature = new StringBuilder(functionName + "(");
        StringBuilder insertText = new StringBuilder(escapedFunctionName + "(");
        List<String> funcArguments = CommonUtil.getFuncArguments(functionSymbol, ctx);
        if (!funcArguments.isEmpty()) {
            signature.append(String.join(", ", funcArguments));
            insertText.append("${1}");
        }
        signature.append(")");
        insertText.append(")");

        return new ImmutablePair<>(insertText.toString(), signature.toString());
    }

    /**
     * Given a path parameter symbol generates the corresponding
     * resource access action's syntax part.
     *
     * @param param path parameter symbol
     * @param ctx   completion context
     * @return {@link Optional<String>} signature part
     */
    public static Optional<String> getFunctionParameterSyntax(PathParameterSymbol param,
                                                              BallerinaCompletionContext ctx) {

        if (param.pathSegmentKind() == PathSegment.Kind.PATH_REST_PARAMETER) {
            return Optional.of(NameUtil.getModifiedTypeName(ctx, param.typeDescriptor())
                    + (param.getName().isEmpty() ? "" : "... "
                    + param.getName().get()));
        }

        if (param.typeDescriptor().typeKind() == TypeDescKind.COMPILATION_ERROR) {
            // Invalid parameters are ignored, but empty string is used to indicate there's a parameter
            return Optional.empty();
        } else {
            return Optional.of(NameUtil.getModifiedTypeName(ctx, param.typeDescriptor()) +
                    ((param.getName().isEmpty() || param.isTypeOnlyParam()) ? "" : " " + param.getName().get()));
        }
    }

    private static String getQualifiedFunctionName(String functionName, BallerinaCompletionContext ctx,
                                                   @Nullable FunctionSymbol functionSymbol) {
        if (functionSymbol == null) {
            return functionName;
        }
        boolean onQNameRef = QNameRefCompletionUtil.onQualifiedNameIdentifier(ctx, ctx.getNodeAtCursor());
        Optional<ModuleSymbol> module = functionSymbol.getModule();
        if (module.isEmpty() || onQNameRef || functionName.equals(SyntaxKind.NEW_KEYWORD.stringValue())) {
            return functionName;
        }
        ModuleID moduleID = module.get().id();
        String modulePrefix = ModuleUtil.getModulePrefix(ctx, moduleID.orgName(), moduleID.moduleName());

        if (modulePrefix.isEmpty()) {
            return functionName;
        }

        return modulePrefix + SyntaxKind.COLON_TOKEN.stringValue() + functionName;
    }

    /**
     * Creates and returns the main function completion item.
     *
     * @param context Ballerina completion context
     * @return {@link CompletionItem} generated main function completion item.
     */
    public static LSCompletionItem buildMainFunction(BallerinaCompletionContext context) {
        NonTerminalNode node = context.getNodeAtCursor();
        Optional<Token> lastQualifier = Optional.empty();
        while (node != null) {
            lastQualifier = ModulePartNodeContextUtil.getLastQualifier(context, node);
            if (lastQualifier.isPresent() || node.kind() == SyntaxKind.MODULE_PART) {
                break;
            }
            node = node.parent();
        }

        CompletionItem completionItem = new CompletionItem();
        String insertText;
        if (lastQualifier.isPresent() && lastQualifier.get().kind() == SyntaxKind.PUBLIC_KEYWORD) {
            insertText = "function main() ";
        } else {
            insertText = "public function main() ";
        }
        completionItem.setInsertText(insertText + "{" + CommonUtil.LINE_SEPARATOR + "\t${1}"
                + CommonUtil.LINE_SEPARATOR + "}");
        completionItem.setLabel("public main function");
        completionItem.setFilterText(SnippetGenerator.generateFilterText
                (Arrays.asList(ItemResolverConstants.PUBLIC_KEYWORD, ItemResolverConstants.FUNCTION, "main")));
        completionItem.setKind(CompletionItemKind.Snippet);
        completionItem.setDetail(ItemResolverConstants.SNIPPET_TYPE);
        return new StaticCompletionItem(context, completionItem, StaticCompletionItem.Kind.MAIN_FUNCTION);
    }

    /**
     * Build mode, either explicit or implicit initializer.
     *
     * @since 1.0.0
     */
    public enum InitializerBuildMode {
        EXPLICIT,
        IMPLICIT
    }
}
