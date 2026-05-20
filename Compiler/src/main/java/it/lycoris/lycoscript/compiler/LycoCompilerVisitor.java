package it.lycoris.lycoscript.compiler;

import it.lycoris.lycoscript.compiler.parser.LycoScriptBaseVisitor;
import it.lycoris.lycoscript.compiler.parser.LycoScriptParser;
import it.lycoris.lycoscript.compiler.symtab.StructInfo;
import it.lycoris.lycoscript.compiler.symtab.Symbol;
import it.lycoris.lycoscript.compiler.symtab.SymbolTable;
import it.lycoris.lycoscript.compiler.symtab.SymbolType;

import java.util.List;

public class LycoCompilerVisitor extends LycoScriptBaseVisitor<Void> {
    private final SymbolTable symTab = new SymbolTable();
    private final CodeGenerator cg = new CodeGenerator();

    public String getCompiledAssembly() {
        return cg.getOutput();
    }

    @Override
    public Void visitProgram(LycoScriptParser.ProgramContext ctx) {
        cg.emitComment("--- Auto-generated LycoScript Compiler Output ---");
        cg.emit(".segment \"CODE\"");

        // --- PASS 1: Forward Declarations for Functions ---
        for (LycoScriptParser.DeclarationContext declCtx : ctx.declaration()) {
            if (declCtx.getChild(0) instanceof LycoScriptParser.FuncDefContext funcCtx) {
                String funcName = funcCtx.identifier().getText();
                String assemblyLabel = "FUNC_" + funcName;

                symTab.defineFunction(funcName, assemblyLabel);

                if (funcCtx.paramList() != null) {
                    for (int i = 0; i < funcCtx.paramList().identifier().size(); i++) {
                        String paramName = funcCtx.paramList().identifier(i).getText();
                        String pType = funcCtx.paramList().type(i).getText();
                        SymbolType symType = pType.equals("int") ? SymbolType.INT : SymbolType.BYTE;
                        symTab.defineParameter(funcName, paramName, symType);
                    }
                }
            } else if (declCtx.getChild(0) instanceof LycoScriptParser.NativeFuncDefContext nativeCtx) {
                String funcName = nativeCtx.identifier().getText();
                String assemblyLabel = "FUNC_" + funcName;

                symTab.defineFunction(funcName, assemblyLabel);

                if (nativeCtx.paramList() != null) {
                    int nativeZpAddress = 0x10;
                    for (int i = 0; i < nativeCtx.paramList().identifier().size(); i++) {
                        String paramName = nativeCtx.paramList().identifier(i).getText();
                        String pType = nativeCtx.paramList().type(i).getText();
                        SymbolType symType = pType.equals("int") ? SymbolType.INT : SymbolType.BYTE;

                        // We bypass defineParameter to force the Zero Page address manually
                        Symbol paramSymbol = new Symbol(paramName, symType, nativeZpAddress, false, 0, null, 1);
                        symTab.getFunctionParameters(funcName).add(paramSymbol);

                        // Increment Zero Page pointer (2 bytes for int/string, 1 byte for byte/boolean)
                        nativeZpAddress += (symType == SymbolType.INT || symType == SymbolType.STRING || symType == SymbolType.POINTER) ? 2 : 1;
                    }
                }
            }
        }

        // Bootloader: Jump to user's main function securely
        cg.emitComment("System Boot");
        cg.emitJumpToSubroutine("FUNC_main");
        cg.emitComment("End of Execution (Infinite loop to halt CPU safely)");
        cg.emitLabel("HALT");
        cg.jump("HALT");

        // --- PASS 2: Actual Code Generation ---
        super.visitProgram(ctx);

        return null;
    }

    @Override
    public Void visitImportStatement(LycoScriptParser.ImportStatementContext ctx) {
        return null;
    }

    @Override
    public Void visitGlobalVarDecl(LycoScriptParser.GlobalVarDeclContext ctx) {
        String varName = ctx.identifier().getText();
        String typeStr = ctx.type().getText();
        boolean isConst = ctx.getChild(0).getText().equals("const");

        int initValue = 0;
        if (ctx.expression() instanceof LycoScriptParser.NumberExprContext numCtx) {
            initValue = Integer.parseInt(numCtx.NUMBER().getText());
        } else if (ctx.expression() instanceof LycoScriptParser.HexNumberExprContext hexCtx) {
            String hexString = hexCtx.HEX_NUMBER().getText().substring(2);
            initValue = Integer.parseInt(hexString, 16);
        }

        if (!isConst) {
            cg.emitComment("Declaring Global: " + varName);
            if (typeStr.equals("byte") || typeStr.equals("boolean") || typeStr.equals("char")) {
                Symbol sym = symTab.defineGlobal(varName, SymbolType.BYTE, false, 0, null);
                cg.loadAccumulatorImmediate(initValue);
                cg.storeAccumulatorAbsolute(sym.getAddress());
            } else if (typeStr.equals("int")) {
                Symbol sym = symTab.defineGlobal(varName, SymbolType.INT, false, 0, null);

                // Little Endian storage logic
                cg.loadAccumulatorImmediate(initValue & 0xFF);
                cg.storeAccumulatorAbsolute(sym.getAddress());

                cg.loadAccumulatorImmediate((initValue >> 8) & 0xFF);
                cg.storeAccumulatorAbsolute(sym.getAddress() + 1);
            }
        } else {
            SymbolType symType = typeStr.equals("int") ? SymbolType.INT : SymbolType.BYTE;
            symTab.defineConstant(varName, symType, initValue);
        }

        return null;
    }

    @Override
    public Void visitVarAssignment(LycoScriptParser.VarAssignmentContext ctx) {
        String varName = ctx.identifier().getText();
        Symbol sym = symTab.getSymbol(varName);

        if (sym == null) throw new RuntimeException("Compilation Error: Undefined variable '" + varName + "'");
        if (sym.isConst()) throw new RuntimeException("Compilation Error: Cannot assign to constant '" + varName + "'");

        cg.emitComment("Assignment: " + varName);

        if (ctx.expression() instanceof LycoScriptParser.AddressOfExprContext) {
            visit(ctx.expression());
            cg.storeAccumulatorAbsolute(sym.getAddress());
            cg.emit("TXA");
            cg.storeAccumulatorAbsolute(sym.getAddress() + 1);
            return null;
        }

        visit(ctx.expression());

        if (sym.getType() == SymbolType.BYTE || sym.getType() == SymbolType.BOOLEAN || sym.getType() == SymbolType.CHAR) {
            cg.storeAccumulatorAbsolute(sym.getAddress());
        } else if (sym.getType() == SymbolType.INT || sym.getType() == SymbolType.STRING || sym.getType() == SymbolType.POINTER) {
            cg.storeAccumulatorAbsolute(sym.getAddress());
            cg.emit("TXA");
            cg.storeAccumulatorAbsolute(sym.getAddress() + 1);
        }

        return null;
    }

    @Override
    public Void visitWhileLoop(LycoScriptParser.WhileLoopContext ctx) {
        String loopStartLabel = cg.createLabel("WHILE_START");
        String loopEndLabel = cg.createLabel("WHILE_END");

        cg.emitComment("While Loop");
        cg.emitLabel(loopStartLabel);

        // Evaluate the condition
        visit(ctx.expression());

        // Check condition: if Accumulator is 0, jump to end
        cg.compareAccumulatorImmediate(0);
        cg.branchIfEqual(loopEndLabel);

        // Execute block statements
        for (LycoScriptParser.StatementContext stmtCtx : ctx.statement()) visit(stmtCtx);

        // Loop back
        cg.jump(loopStartLabel);

        cg.emitLabel(loopEndLabel);

        return null;
    }

    @Override
    public Void visitMathAddSubExpr(LycoScriptParser.MathAddSubExprContext ctx) {
        visit(ctx.left);

        int tempAddress = symTab.pushExpressionTemp();
        cg.storeToTemp(tempAddress);

        visit(ctx.right);

        String operator = ctx.op.getText();
        if (operator.equals("+")) {
            cg.addTempToAccumulator(tempAddress);
        } else if (operator.equals("-")) {
            int rightTempAddress = symTab.pushExpressionTemp();
            cg.storeToTemp(rightTempAddress);

            cg.loadFromTemp(tempAddress);
            cg.subtractTempFromAccumulator(rightTempAddress);

            symTab.popExpressionTemp();
        }

        symTab.popExpressionTemp();
        return null;
    }

    @Override
    public Void visitBitwiseAndExpr(LycoScriptParser.BitwiseAndExprContext ctx) {
        visit(ctx.left);

        int tempAddr = symTab.pushExpressionTemp();
        cg.storeToTemp(tempAddr);

        visit(ctx.right);

        cg.bitwiseAndTemp(tempAddr);

        symTab.popExpressionTemp();
        return null;
    }

    @Override
    public Void visitNumberExpr(LycoScriptParser.NumberExprContext ctx) {
        int value = Integer.parseInt(ctx.NUMBER().getText());
        cg.loadAccumulatorImmediate(value & 0xFF);
        cg.emit("LDX #$" + String.format("%02X", (value >> 8) & 0xFF));
        return null;
    }

    @Override
    public Void visitHexNumberExpr(LycoScriptParser.HexNumberExprContext ctx) {
        String hexString = ctx.HEX_NUMBER().getText().substring(2);
        int value = Integer.parseInt(hexString, 16);
        cg.loadAccumulatorImmediate(value);
        return null;
    }

    @Override
    public Void visitIdentifierExpr(LycoScriptParser.IdentifierExprContext ctx) {
        String varName = ctx.identifier().getText();
        Symbol sym = symTab.getSymbol(varName);

        if (sym == null) throw new RuntimeException("Compilation Error: Undefined variable '" + varName + "'");

        if (sym.isConst()) cg.loadAccumulatorImmediate(sym.getConstValue());
        else cg.loadAccumulatorAbsolute(sym.getAddress());

        return null;
    }

    @Override
    public Void visitIfElseBlock(LycoScriptParser.IfElseBlockContext ctx) {
        String elseLabel = cg.createLabel("IF_ELSE");
        String endLabel = cg.createLabel("IF_END");

        cg.emitComment("If Block");

        visit(ctx.expression());

        cg.compareAccumulatorImmediate(0);
        if (ctx.getChildCount() > 5) cg.branchIfEqual(elseLabel);
        else cg.branchIfEqual(endLabel);

        for (LycoScriptParser.StatementContext stmtCtx : ctx.statement()) {
            if (stmtCtx.getParent() == ctx && stmtCtx.getStart().getTokenIndex() < (ctx.getChildCount() > 5 ? ctx.getChild(5).getSourceInterval().a : Integer.MAX_VALUE)) visit(stmtCtx);
        }

        if (ctx.getChildCount() > 5) {
            cg.jump(endLabel);
            cg.emitLabel(elseLabel);
            cg.emitComment("Else Block");

            boolean inElse = false;
            for (int i = 0; i < ctx.getChildCount(); i++) {
                if (ctx.getChild(i).getText().equals("else")) inElse = true;
                if (inElse && ctx.getChild(i) instanceof LycoScriptParser.StatementContext) visit(ctx.getChild(i));
            }
        }

        cg.emitLabel(endLabel);
        return null;
    }

    @Override
    public Void visitEqualityExpr(LycoScriptParser.EqualityExprContext ctx) {
        visit(ctx.left);
        int tempAddr = symTab.pushExpressionTemp();
        cg.storeToTemp(tempAddr);

        visit(ctx.right);
        String op = ctx.op.getText();

        String isTrueLabel = cg.createLabel("EQ_TRUE");
        String endLabel = cg.createLabel("EQ_END");

        cg.compareAccumulatorImmediate(0);

        int rightTempAddr = symTab.pushExpressionTemp();
        cg.storeToTemp(rightTempAddr);
        cg.loadFromTemp(tempAddr);
        cg.emit("CMP $" + String.format("%02X", rightTempAddr));
        symTab.popExpressionTemp();

        if (op.equals("==")) cg.branchIfEqual(isTrueLabel);
        else cg.branchIfNotEqual(isTrueLabel);

        // False path
        cg.loadAccumulatorImmediate(0);
        cg.jump(endLabel);

        // True path
        cg.emitLabel(isTrueLabel);
        cg.loadAccumulatorImmediate(1);

        cg.emitLabel(endLabel);
        symTab.popExpressionTemp();
        return null;
    }

    @Override
    public Void visitRelationalExpr(LycoScriptParser.RelationalExprContext ctx) {
        visit(ctx.left);
        int tempAddr = symTab.pushExpressionTemp();
        cg.storeToTemp(tempAddr);

        visit(ctx.right);
        int rightTempAddr = symTab.pushExpressionTemp();
        cg.storeToTemp(rightTempAddr);

        cg.loadFromTemp(tempAddr);
        cg.emit("CMP $" + String.format("%02X", rightTempAddr));

        String op = ctx.op.getText();
        String isTrueLabel = cg.createLabel("REL_TRUE");
        String endLabel = cg.createLabel("REL_END");

        switch (op) {
            case "<":
                cg.branchIfCarryClear(isTrueLabel);
                break;
            case ">=":
                cg.branchIfCarrySet(isTrueLabel);
                break;
            case ">":
                cg.branchIfEqual(endLabel);
                cg.branchIfCarrySet(isTrueLabel);
                break;
            case "<=":
                cg.branchIfEqual(isTrueLabel);
                cg.branchIfCarryClear(isTrueLabel);
                break;
        }

        // False path
        cg.loadAccumulatorImmediate(0);
        cg.jump(endLabel);

        // True path
        cg.emitLabel(isTrueLabel);
        cg.loadAccumulatorImmediate(1);

        cg.emitLabel(endLabel);
        symTab.popExpressionTemp();
        symTab.popExpressionTemp();
        return null;
    }

    @Override
    public Void visitBitwiseOrExpr(LycoScriptParser.BitwiseOrExprContext ctx) {
        visit(ctx.left);
        int tempAddr = symTab.pushExpressionTemp();
        cg.storeToTemp(tempAddr);
        visit(ctx.right);
        cg.bitwiseOrTemp(tempAddr);
        symTab.popExpressionTemp();
        return null;
    }

    @Override
    public Void visitBitwiseXorExpr(LycoScriptParser.BitwiseXorExprContext ctx) {
        visit(ctx.left);
        int tempAddr = symTab.pushExpressionTemp();
        cg.storeToTemp(tempAddr);
        visit(ctx.right);
        cg.bitwiseXorTemp(tempAddr);
        symTab.popExpressionTemp();
        return null;
    }

    @Override
    public Void visitBitwiseNotExpr(LycoScriptParser.BitwiseNotExprContext ctx) {
        visit(ctx.expression());
        cg.bitwiseNotAccumulator();
        return null;
    }

    @Override
    public Void visitBooleanTrueExpr(LycoScriptParser.BooleanTrueExprContext ctx) {
        cg.loadAccumulatorImmediate(1);
        return null;
    }

    @Override
    public Void visitBooleanFalseExpr(LycoScriptParser.BooleanFalseExprContext ctx) {
        cg.loadAccumulatorImmediate(0);
        return null;
    }

    @Override
    public Void visitCharExpr(LycoScriptParser.CharExprContext ctx) {
        String charLiteral = ctx.CHAR_LITERAL().getText();
        char c = charLiteral.charAt(1);
        cg.loadAccumulatorImmediate((int) c);
        return null;
    }

    @Override
    public Void visitLocalVarDecl(LycoScriptParser.LocalVarDeclContext ctx) {
        String varName = ctx.identifier().getText();
        String typeStr = ctx.type().getText();

        int initValue = 0;
        if (ctx.expression() instanceof LycoScriptParser.NumberExprContext numCtx) initValue = Integer.parseInt(numCtx.NUMBER().getText());

        cg.emitComment("Local variable: " + varName);
        SymbolType symType = typeStr.equals("int") ? SymbolType.INT : SymbolType.BYTE;
        Symbol sym = symTab.defineLocal(varName, symType);

        if (symType == SymbolType.BYTE || symType == SymbolType.BOOLEAN || symType == SymbolType.CHAR) {
            cg.loadAccumulatorImmediate(initValue);
            cg.storeAccumulatorAbsolute(sym.getAddress());
        } else if (symType == SymbolType.INT) {
            cg.loadAccumulatorImmediate(initValue & 0xFF);
            cg.storeAccumulatorAbsolute(sym.getAddress());
            cg.loadAccumulatorImmediate((initValue >> 8) & 0xFF);
            cg.storeAccumulatorAbsolute(sym.getAddress() + 1);
        }

        return null;
    }

    @Override
    public Void visitFuncDef(LycoScriptParser.FuncDefContext ctx) {
        String funcName = ctx.identifier().getText();
        String assemblyLabel = symTab.getFunctionLabel(funcName);

        symTab.enterFunctionScope(funcName);

        cg.emit("");
        cg.emitComment("--- Function: " + funcName + " ---");
        cg.emitLabel(assemblyLabel);

        for (LycoScriptParser.StatementContext stmtCtx : ctx.statement()) {
            visit(stmtCtx);
        }

        cg.emitReturnFromSubroutine();
        symTab.exitFunctionScope();
        return null;
    }

    @Override
    public Void visitFunctionCallStmt(LycoScriptParser.FunctionCallStmtContext ctx) {
        String funcName = ctx.identifier().getText();
        generateFunctionCall(funcName, ctx.argList());
        return null;
    }

    @Override
    public Void visitFunctionCallExpr(LycoScriptParser.FunctionCallExprContext ctx) {
        String funcName = ctx.identifier().getText();
        generateFunctionCall(funcName, ctx.argList());
        return null;
    }

    @Override
    public Void visitNativeFuncDef(LycoScriptParser.NativeFuncDefContext ctx) {
        return null;
    }

    @Override
    public Void visitReturnStmt(LycoScriptParser.ReturnStmtContext ctx) {
        if (ctx.expression() != null) {
            cg.emitComment("Return value");
            visit(ctx.expression());
        }

        cg.emitReturnFromSubroutine();
        return null;
    }

    @Override
    public Void visitStructDef(LycoScriptParser.StructDefContext ctx) {
        String structName = ctx.identifier().getText();
        StructInfo structInfo = new StructInfo(structName);

        for (LycoScriptParser.StructFieldContext fieldCtx : ctx.structField()) {
            String fieldTypeStr;
            String fieldName;
            int arraySize = 1;

            if (fieldCtx instanceof LycoScriptParser.StructSimpleFieldContext simpleCtx) {
                fieldTypeStr = simpleCtx.type().getText();
                fieldName = simpleCtx.identifier().getText();
            } else if (fieldCtx instanceof LycoScriptParser.StructArrayFieldContext arrCtx) {
                fieldTypeStr = arrCtx.type().getText();
                fieldName = arrCtx.identifier().getText();
                arraySize = Integer.parseInt(arrCtx.NUMBER().getText());
            } else {
                continue;
            }

            SymbolType fieldType = SymbolType.BYTE;
            if (fieldTypeStr.equals("int")) fieldType = SymbolType.INT;
            else if (fieldTypeStr.equals("boolean")) fieldType = SymbolType.BOOLEAN;
            else if (fieldTypeStr.equals("string")) fieldType = SymbolType.STRING;
            else if (fieldTypeStr.endsWith("*")) fieldType = SymbolType.POINTER;

            structInfo.addField(fieldName, fieldType, arraySize);
        }

        symTab.defineStruct(structInfo);
        return null;
    }

    @Override
    public Void visitStructFieldAssignment(LycoScriptParser.StructFieldAssignmentContext ctx) {
        String objName = ctx.identifier(0).getText();
        String fieldName = ctx.identifier(1).getText();

        Symbol objSym = symTab.getSymbol(objName);
        StructInfo structInfo = symTab.getStruct(objSym.getStructName());

        int offset = structInfo.getFieldOffset(fieldName);

        visit(ctx.expression());
        cg.storeAccumulatorAbsolute(objSym.getAddress() + offset);
        return null;
    }

    @Override
    public Void visitStructFieldAccessExpr(LycoScriptParser.StructFieldAccessExprContext ctx) {
        String objName = ctx.identifier(0).getText();
        String fieldName = ctx.identifier(1).getText();

        Symbol objSym = symTab.getSymbol(objName);
        StructInfo structInfo = symTab.getStruct(objSym.getStructName());
        int offset = structInfo.getFieldOffset(fieldName);

        cg.loadAccumulatorAbsolute(objSym.getAddress() + offset);
        return null;
    }

    @Override
    public Void visitGlobalArrayDecl(LycoScriptParser.GlobalArrayDeclContext ctx) {
        String varName = ctx.identifier().getText();
        String typeStr = ctx.type().getText();
        int size = Integer.parseInt(ctx.NUMBER().getText());
        SymbolType type = typeStr.equals("int") ? SymbolType.INT : SymbolType.BYTE;

        symTab.defineGlobalArray(varName, type, size);
        return null;
    }

    @Override
    public Void visitLocalArrayDecl(LycoScriptParser.LocalArrayDeclContext ctx) {
        String varName = ctx.identifier().getText();
        String typeStr = ctx.type().getText();
        int size = Integer.parseInt(ctx.NUMBER().getText());
        SymbolType type = typeStr.equals("int") ? SymbolType.INT : SymbolType.BYTE;

        symTab.defineLocalArray(varName, type, size);
        return null;
    }

    @Override
    public Void visitArrayAssignment(LycoScriptParser.ArrayAssignmentContext ctx) {
        String arrName = ctx.identifier().getText();
        Symbol sym = symTab.getSymbol(arrName);

        visit(ctx.expression(1));
        int valTemp = symTab.pushExpressionTemp();
        cg.storeToTemp(valTemp);

        visit(ctx.expression(0));
        cg.transferAccumulatorToY();

        cg.loadFromTemp(valTemp);
        cg.storeAccumulatorAbsoluteIndexedY(sym.getAddress());

        symTab.popExpressionTemp();
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(LycoScriptParser.ArrayAccessExprContext ctx) {
        String arrName = ctx.identifier().getText();
        Symbol sym = symTab.getSymbol(arrName);

        visit(ctx.expression());
        cg.transferAccumulatorToY();
        cg.loadAccumulatorAbsoluteIndexedY(sym.getAddress());
        return null;
    }


    @Override
    public Void visitAddressOfExpr(LycoScriptParser.AddressOfExprContext ctx) {
        String varName = ctx.identifier().getText();
        Symbol sym = symTab.getSymbol(varName);

        cg.emit("LDA #$" + String.format("%02X", sym.getAddress() & 0xFF));
        cg.emit("LDX #$" + String.format("%02X", (sym.getAddress() >> 8) & 0xFF));
        return null;
    }

    @Override
    public Void visitDereferenceExpr(LycoScriptParser.DereferenceExprContext ctx) {
        String ptrName = ctx.identifier().getText();
        Symbol ptrSym = symTab.getSymbol(ptrName);

        cg.emitComment("Dereferencing pointer: *" + ptrName);
        cg.loadAccumulatorAbsolute(ptrSym.getAddress());
        cg.emit("STA $10");
        cg.loadAccumulatorAbsolute(ptrSym.getAddress() + 1);
        cg.emit("STA $11");

        cg.emit("LDY #$00");
        cg.loadIndirectY(0x10);
        return null;
    }

    @Override
    public Void visitPointerAssignment(LycoScriptParser.PointerAssignmentContext ctx) {
        String ptrName = ctx.identifier().getText();
        Symbol ptrSym = symTab.getSymbol(ptrName);

        visit(ctx.expression());
        int valTemp = symTab.pushExpressionTemp();
        cg.storeToTemp(valTemp);

        cg.emitComment("Writing to pointer: *" + ptrName);
        cg.loadAccumulatorAbsolute(ptrSym.getAddress());
        cg.emit("STA $10");
        cg.loadAccumulatorAbsolute(ptrSym.getAddress() + 1);
        cg.emit("STA $11");

        cg.emit("LDY #$00");
        cg.loadFromTemp(valTemp);
        cg.storeIndirectY(0x10);

        symTab.popExpressionTemp();
        return null;
    }

    @Override
    public Void visitStringExpr(LycoScriptParser.StringExprContext ctx) {
        String strValue = ctx.STRING_LITERAL().getText();
        strValue = strValue.substring(1, strValue.length() - 1);
        String label = cg.addStringLiteral(strValue);

        cg.emit("LDA #<" + label);
        cg.emit("LDX #>" + label);
        return null;
    }

    @Override
    public Void visitForLoop(LycoScriptParser.ForLoopContext ctx) {
        if (ctx.forInit() != null) visit(ctx.forInit());

        String loopStart = cg.createLabel("FOR_START");
        String loopEnd = cg.createLabel("FOR_END");

        cg.emitComment("For Loop");
        cg.emitLabel(loopStart);

        if (ctx.expression() != null) {
            visit(ctx.expression());
            cg.compareAccumulatorImmediate(0);
            cg.branchIfEqual(loopEnd);
        }

        for (LycoScriptParser.StatementContext stmtCtx : ctx.statement()) {
            visit(stmtCtx);
        }

        if (ctx.forUpdate() != null) visit(ctx.forUpdate());

        cg.jump(loopStart);
        cg.emitLabel(loopEnd);

        return null;
    }

    @Override
    public Void visitForInit(LycoScriptParser.ForInitContext ctx) {
        if (ctx.type() != null) {
            String varName = ctx.identifier().getText();
            String typeStr = ctx.type().getText();
            SymbolType symType = typeStr.equals("int") ? SymbolType.INT : SymbolType.BYTE;
            Symbol sym = symTab.defineLocal(varName, symType);

            visit(ctx.expression());
            cg.storeAccumulatorAbsolute(sym.getAddress());
            if (symType == SymbolType.INT) {
                cg.emit("TXA");
                cg.storeAccumulatorAbsolute(sym.getAddress() + 1);
            }
        } else {
            String varName = ctx.identifier().getText();
            Symbol sym = symTab.getSymbol(varName);
            visit(ctx.expression());
            cg.storeAccumulatorAbsolute(sym.getAddress());
        }

        return null;
    }

    @Override
    public Void visitForUpdate(LycoScriptParser.ForUpdateContext ctx) {
        String varName = ctx.identifier().getText();
        Symbol sym = symTab.getSymbol(varName);
        visit(ctx.expression());
        cg.storeAccumulatorAbsolute(sym.getAddress());
        return null;
    }

    @Override
    public Void visitForEachLoop(LycoScriptParser.ForEachLoopContext ctx) {
        String varTypeStr = ctx.type().getText();
        String varName = ctx.identifier(0).getText(); // The iterator variable (e.g., 'item')
        String arrName = ctx.identifier(1).getText(); // The array to iterate over (e.g., 'buffer')

        Symbol arrSym = symTab.getSymbol(arrName);
        if (arrSym == null) throw new RuntimeException("Compilation Error: Undefined array '" + arrName + "'");

        int arrLen = arrSym.getArrayLength();

        // 1. Define the iterator variable that the user will use inside the loop
        SymbolType symType = varTypeStr.equals("int") ? SymbolType.INT : SymbolType.BYTE;
        Symbol iterSym = symTab.defineLocal(varName, symType);

        // 2. Define a hidden internal variable to act as the index counter
        Symbol idxSym = symTab.defineLocal("_idx_" + varName + "_" + cg.createLabel(""), SymbolType.BYTE);

        String loopStart = cg.createLabel("FOREACH_START");
        String loopEnd = cg.createLabel("FOREACH_END");

        cg.emitComment("For-Each Loop on array: " + arrName);

        // Initialize hidden index to 0
        cg.loadAccumulatorImmediate(0);
        cg.storeAccumulatorAbsolute(idxSym.getAddress());

        cg.emitLabel(loopStart);

        // Check condition: if hidden_index == array_length, exit loop
        cg.loadAccumulatorAbsolute(idxSym.getAddress());
        cg.compareAccumulatorImmediate(arrLen);
        cg.branchIfEqual(loopEnd);

        // Fetch the current item from the array
        // The Accumulator currently holds the index. Transfer it to Y for indexed addressing.
        cg.transferAccumulatorToY();

        if (symType == SymbolType.INT) {
            // Arrays of INTs take 2 bytes per element. We must multiply the index by 2 (Shift Left).
            cg.emit("ASL A");
            cg.transferAccumulatorToY();

            cg.loadAccumulatorAbsoluteIndexedY(arrSym.getAddress());
            cg.storeAccumulatorAbsolute(iterSym.getAddress());

            cg.emit("INY");

            cg.loadAccumulatorAbsoluteIndexedY(arrSym.getAddress());
            cg.storeAccumulatorAbsolute(iterSym.getAddress() + 1);
        } else {
            // Standard 8-bit array fetch
            cg.loadAccumulatorAbsoluteIndexedY(arrSym.getAddress());
            cg.storeAccumulatorAbsolute(iterSym.getAddress());
        }

        // Execute the user's block of statements
        for (LycoScriptParser.StatementContext stmtCtx : ctx.statement()) {
            visit(stmtCtx);
        }

        // Increment the hidden index safely
        cg.loadAccumulatorAbsolute(idxSym.getAddress());
        cg.emit("CLC");
        cg.emit("ADC #$01");
        cg.storeAccumulatorAbsolute(idxSym.getAddress());

        // Loop back
        cg.jump(loopStart);
        cg.emitLabel(loopEnd);

        return null;
    }

    private void generateFunctionCall(String funcName, LycoScriptParser.ArgListContext argListCtx) {
        String label = symTab.getFunctionLabel(funcName);
        if (label == null) throw new RuntimeException("Compilation Error: Call to undefined function '" + funcName + "'");

        List<Symbol> expectedParams = symTab.getFunctionParameters(funcName);
        int argCount = argListCtx == null ? 0 : argListCtx.expression().size();

        if (expectedParams.size() != argCount) throw new RuntimeException("Compilation Error: Function '" + funcName + "' expects " + expectedParams.size() + " arguments, got " + argCount);

        cg.emitComment("Call to " + funcName);

        if (argCount > 0) {
            for (int i = 0; i < argCount; i++) {
                Symbol paramSymbol = expectedParams.get(i);
                LycoScriptParser.ExpressionContext exprCtx = argListCtx.expression(i);

                cg.emitComment("Passing argument to " + paramSymbol.getName());

                if (paramSymbol.getType() == SymbolType.INT) {
                    switch (exprCtx) {
                        case LycoScriptParser.NumberExprContext numCtx -> {
                            int val = Integer.parseInt(numCtx.NUMBER().getText());
                            cg.loadAccumulatorImmediate(val & 0xFF);
                            cg.storeAccumulatorAbsolute(paramSymbol.getAddress());
                            cg.loadAccumulatorImmediate((val >> 8) & 0xFF);
                            cg.storeAccumulatorAbsolute(paramSymbol.getAddress() + 1);
                        }
                        case LycoScriptParser.HexNumberExprContext hexCtx -> {
                            int val = Integer.parseInt(hexCtx.HEX_NUMBER().getText().substring(2), 16);
                            cg.loadAccumulatorImmediate(val & 0xFF);
                            cg.storeAccumulatorAbsolute(paramSymbol.getAddress());
                            cg.loadAccumulatorImmediate((val >> 8) & 0xFF);
                            cg.storeAccumulatorAbsolute(paramSymbol.getAddress() + 1);
                        }
                        case LycoScriptParser.IdentifierExprContext idCtx -> {
                            String varName = idCtx.identifier().getText();
                            Symbol sourceSym = symTab.getSymbol(varName);
                            if (sourceSym == null) throw new RuntimeException("Undefined variable: " + varName);

                            if (sourceSym.isConst()) {
                                int val = sourceSym.getConstValue();
                                cg.loadAccumulatorImmediate(val & 0xFF);
                                cg.storeAccumulatorAbsolute(paramSymbol.getAddress());
                                cg.loadAccumulatorImmediate((val >> 8) & 0xFF);
                                cg.storeAccumulatorAbsolute(paramSymbol.getAddress() + 1);
                            } else {
                                cg.loadAccumulatorAbsolute(sourceSym.getAddress());
                                cg.storeAccumulatorAbsolute(paramSymbol.getAddress());
                                cg.loadAccumulatorAbsolute(sourceSym.getAddress() + 1);
                                cg.storeAccumulatorAbsolute(paramSymbol.getAddress() + 1);
                            }
                        }
                        case null, default -> {
                            visit(exprCtx);
                            cg.storeAccumulatorAbsolute(paramSymbol.getAddress());
                            cg.loadAccumulatorImmediate(0);
                            cg.storeAccumulatorAbsolute(paramSymbol.getAddress() + 1);
                        }
                    }
                } else {
                    visit(exprCtx);
                    cg.storeAccumulatorAbsolute(paramSymbol.getAddress());
                }
            }
        }

        cg.emitJumpToSubroutine(label);
    }
}
