# Details

Date : 2023-09-26 21:24:02

Directory c:\\Users\\zj\\Desktop\\编译实验\\Compiler

Total : 96 files,  2508 codes, 74 comments, 297 blanks, all 2879 lines

[Summary](results.md) / Details / [Diff Summary](diff.md) / [Diff Details](diff-details.md)

## Files
| filename | language | code | comment | blank | total |
| :--- | :--- | ---: | ---: | ---: | ---: |
| [.idea/checkstyle-idea.xml](/.idea/checkstyle-idea.xml) | XML | 16 | 0 | 0 | 16 |
| [.idea/misc.xml](/.idea/misc.xml) | XML | 6 | 0 | 0 | 6 |
| [.idea/modules.xml](/.idea/modules.xml) | XML | 8 | 0 | 0 | 8 |
| [.idea/uiDesigner.xml](/.idea/uiDesigner.xml) | XML | 124 | 0 | 0 | 124 |
| [Compiler.iml](/Compiler.iml) | XML | 11 | 0 | 0 | 11 |
| [README.md](/README.md) | Markdown | 10 | 0 | 9 | 19 |
| [bin/Compiler.class](/bin/Compiler.class) | Java | 27 | 0 | 0 | 27 |
| [bin/error/ErrorHandler.class](/bin/error/ErrorHandler.class) | Java | 9 | 0 | 0 | 9 |
| [bin/error/ParsingFailedException.class](/bin/error/ParsingFailedException.class) | Java | 5 | 0 | 0 | 5 |
| [bin/lexical/CategoryCode.class](/bin/lexical/CategoryCode.class) | Java | 21 | 0 | 0 | 21 |
| [bin/lexical/LexicalAnalyser.class](/bin/lexical/LexicalAnalyser.class) | Java | 47 | 0 | 0 | 47 |
| [bin/lexical/LexicalManager.class](/bin/lexical/LexicalManager.class) | Java | 24 | 4 | 0 | 28 |
| [bin/lexical/Reader.class](/bin/lexical/Reader.class) | Java | 20 | 0 | 0 | 20 |
| [bin/lexical/Symbol.class](/bin/lexical/Symbol.class) | Java | 15 | 0 | 0 | 15 |
| [bin/syntaxAnalyser/TreeNode.class](/bin/syntaxAnalyser/TreeNode.class) | Java | 3 | 0 | 0 | 3 |
| [bin/syntaxAnalyser/nodes/AddExp.class](/bin/syntaxAnalyser/nodes/AddExp.class) | Java | 15 | 0 | 0 | 15 |
| [bin/syntaxAnalyser/nodes/BType.class](/bin/syntaxAnalyser/nodes/BType.class) | Java | 20 | 0 | 0 | 20 |
| [bin/syntaxAnalyser/nodes/Block.class](/bin/syntaxAnalyser/nodes/Block.class) | Java | 16 | 0 | 0 | 16 |
| [bin/syntaxAnalyser/nodes/BlockItem.class](/bin/syntaxAnalyser/nodes/BlockItem.class) | Java | 15 | 0 | 0 | 15 |
| [bin/syntaxAnalyser/nodes/CompUnit.class](/bin/syntaxAnalyser/nodes/CompUnit.class) | Java | 25 | 0 | 0 | 25 |
| [bin/syntaxAnalyser/nodes/Cond.class](/bin/syntaxAnalyser/nodes/Cond.class) | Java | 15 | 0 | 0 | 15 |
| [bin/syntaxAnalyser/nodes/ConstDecl.class](/bin/syntaxAnalyser/nodes/ConstDecl.class) | Java | 26 | 0 | 0 | 26 |
| [bin/syntaxAnalyser/nodes/ConstDef.class](/bin/syntaxAnalyser/nodes/ConstDef.class) | Java | 27 | 0 | 0 | 27 |
| [bin/syntaxAnalyser/nodes/ConstExp.class](/bin/syntaxAnalyser/nodes/ConstExp.class) | Java | 16 | 0 | 0 | 16 |
| [bin/syntaxAnalyser/nodes/ConstInitVal.class](/bin/syntaxAnalyser/nodes/ConstInitVal.class) | Java | 25 | 0 | 0 | 25 |
| [bin/syntaxAnalyser/nodes/Decl.class](/bin/syntaxAnalyser/nodes/Decl.class) | Java | 22 | 0 | 0 | 22 |
| [bin/syntaxAnalyser/nodes/EqExp.class](/bin/syntaxAnalyser/nodes/EqExp.class) | Java | 16 | 0 | 0 | 16 |
| [bin/syntaxAnalyser/nodes/Exp.class](/bin/syntaxAnalyser/nodes/Exp.class) | Java | 15 | 0 | 0 | 15 |
| [bin/syntaxAnalyser/nodes/ForStmt.class](/bin/syntaxAnalyser/nodes/ForStmt.class) | Java | 15 | 0 | 0 | 15 |
| [bin/syntaxAnalyser/nodes/FuncDef.class](/bin/syntaxAnalyser/nodes/FuncDef.class) | Java | 23 | 0 | 0 | 23 |
| [bin/syntaxAnalyser/nodes/FuncFParam.class](/bin/syntaxAnalyser/nodes/FuncFParam.class) | Java | 25 | 0 | 0 | 25 |
| [bin/syntaxAnalyser/nodes/FuncFParams.class](/bin/syntaxAnalyser/nodes/FuncFParams.class) | Java | 24 | 0 | 0 | 24 |
| [bin/syntaxAnalyser/nodes/FuncRParams.class](/bin/syntaxAnalyser/nodes/FuncRParams.class) | Java | 15 | 0 | 0 | 15 |
| [bin/syntaxAnalyser/nodes/FuncType.class](/bin/syntaxAnalyser/nodes/FuncType.class) | Java | 20 | 0 | 0 | 20 |
| [bin/syntaxAnalyser/nodes/InitVal.class](/bin/syntaxAnalyser/nodes/InitVal.class) | Java | 25 | 0 | 0 | 25 |
| [bin/syntaxAnalyser/nodes/LAndExp.class](/bin/syntaxAnalyser/nodes/LAndExp.class) | Java | 15 | 0 | 0 | 15 |
| [bin/syntaxAnalyser/nodes/LOrExp.class](/bin/syntaxAnalyser/nodes/LOrExp.class) | Java | 15 | 0 | 0 | 15 |
| [bin/syntaxAnalyser/nodes/LVal.class](/bin/syntaxAnalyser/nodes/LVal.class) | Java | 15 | 0 | 0 | 15 |
| [bin/syntaxAnalyser/nodes/MainFuncDef.class](/bin/syntaxAnalyser/nodes/MainFuncDef.class) | Java | 21 | 0 | 0 | 21 |
| [bin/syntaxAnalyser/nodes/MulExp.class](/bin/syntaxAnalyser/nodes/MulExp.class) | Java | 15 | 0 | 0 | 15 |
| [bin/syntaxAnalyser/nodes/Number.class](/bin/syntaxAnalyser/nodes/Number.class) | Java | 15 | 0 | 0 | 15 |
| [bin/syntaxAnalyser/nodes/PrimaryExp.class](/bin/syntaxAnalyser/nodes/PrimaryExp.class) | Java | 7 | 0 | 0 | 7 |
| [bin/syntaxAnalyser/nodes/RelExp.class](/bin/syntaxAnalyser/nodes/RelExp.class) | Java | 15 | 0 | 0 | 15 |
| [bin/syntaxAnalyser/nodes/Stmt.class](/bin/syntaxAnalyser/nodes/Stmt.class) | Java | 15 | 0 | 0 | 15 |
| [bin/syntaxAnalyser/nodes/UnaryExp.class](/bin/syntaxAnalyser/nodes/UnaryExp.class) | Java | 16 | 0 | 0 | 16 |
| [bin/syntaxAnalyser/nodes/UnaryOp.class](/bin/syntaxAnalyser/nodes/UnaryOp.class) | Java | 15 | 0 | 0 | 15 |
| [bin/syntaxAnalyser/nodes/VarDecl.class](/bin/syntaxAnalyser/nodes/VarDecl.class) | Java | 26 | 0 | 0 | 26 |
| [bin/syntaxAnalyser/nodes/VarDef.class](/bin/syntaxAnalyser/nodes/VarDef.class) | Java | 25 | 0 | 0 | 25 |
| [out/production/Compiler/CategoryCode.class](/out/production/Compiler/CategoryCode.class) | Java | 24 | 0 | 0 | 24 |
| [out/production/Compiler/Compiler.class](/out/production/Compiler/Compiler.class) | Java | 26 | 0 | 0 | 26 |
| [out/production/Compiler/ErrorHandler.class](/out/production/Compiler/ErrorHandler.class) | Java | 8 | 0 | 0 | 8 |
| [out/production/Compiler/LexicalAnalyser.class](/out/production/Compiler/LexicalAnalyser.class) | Java | 81 | 0 | 0 | 81 |
| [out/production/Compiler/Reader.class](/out/production/Compiler/Reader.class) | Java | 26 | 0 | 0 | 26 |
| [out/production/Compiler/Symbol.class](/out/production/Compiler/Symbol.class) | Java | 14 | 0 | 0 | 14 |
| [src/Compiler.java](/src/Compiler.java) | Java | 16 | 1 | 3 | 20 |
| [src/error/ErrorHandler.java](/src/error/ErrorHandler.java) | Java | 6 | 0 | 2 | 8 |
| [src/error/ParsingFailedException.java](/src/error/ParsingFailedException.java) | Java | 3 | 0 | 3 | 6 |
| [src/lexical/CategoryCode.java](/src/lexical/CategoryCode.java) | Java | 42 | 0 | 2 | 44 |
| [src/lexical/LexicalAnalyser.java](/src/lexical/LexicalAnalyser.java) | Java | 205 | 6 | 11 | 222 |
| [src/lexical/LexicalManager.java](/src/lexical/LexicalManager.java) | Java | 55 | 13 | 12 | 80 |
| [src/lexical/Reader.java](/src/lexical/Reader.java) | Java | 40 | 0 | 7 | 47 |
| [src/lexical/Symbol.java](/src/lexical/Symbol.java) | Java | 8 | 0 | 3 | 11 |
| [src/syntaxAnalyser/TreeNode.java](/src/syntaxAnalyser/TreeNode.java) | Java | 3 | 0 | 3 | 6 |
| [src/syntaxAnalyser/nodes/AddExp.java](/src/syntaxAnalyser/nodes/AddExp.java) | Java | 18 | 0 | 6 | 24 |
| [src/syntaxAnalyser/nodes/BType.java](/src/syntaxAnalyser/nodes/BType.java) | Java | 21 | 0 | 6 | 27 |
| [src/syntaxAnalyser/nodes/Block.java](/src/syntaxAnalyser/nodes/Block.java) | Java | 29 | 1 | 8 | 38 |
| [src/syntaxAnalyser/nodes/BlockItem.java](/src/syntaxAnalyser/nodes/BlockItem.java) | Java | 27 | 1 | 9 | 37 |
| [src/syntaxAnalyser/nodes/CompUnit.java](/src/syntaxAnalyser/nodes/CompUnit.java) | Java | 39 | 0 | 7 | 46 |
| [src/syntaxAnalyser/nodes/Cond.java](/src/syntaxAnalyser/nodes/Cond.java) | Java | 17 | 1 | 6 | 24 |
| [src/syntaxAnalyser/nodes/ConstDecl.java](/src/syntaxAnalyser/nodes/ConstDecl.java) | Java | 34 | 1 | 7 | 42 |
| [src/syntaxAnalyser/nodes/ConstDef.java](/src/syntaxAnalyser/nodes/ConstDef.java) | Java | 33 | 1 | 8 | 42 |
| [src/syntaxAnalyser/nodes/ConstExp.java](/src/syntaxAnalyser/nodes/ConstExp.java) | Java | 18 | 0 | 6 | 24 |
| [src/syntaxAnalyser/nodes/ConstInitVal.java](/src/syntaxAnalyser/nodes/ConstInitVal.java) | Java | 39 | 2 | 11 | 52 |
| [src/syntaxAnalyser/nodes/Decl.java](/src/syntaxAnalyser/nodes/Decl.java) | Java | 30 | 1 | 8 | 39 |
| [src/syntaxAnalyser/nodes/EqExp.java](/src/syntaxAnalyser/nodes/EqExp.java) | Java | 18 | 0 | 6 | 24 |
| [src/syntaxAnalyser/nodes/Exp.java](/src/syntaxAnalyser/nodes/Exp.java) | Java | 18 | 1 | 6 | 25 |
| [src/syntaxAnalyser/nodes/ForStmt.java](/src/syntaxAnalyser/nodes/ForStmt.java) | Java | 27 | 1 | 7 | 35 |
| [src/syntaxAnalyser/nodes/FuncDef.java](/src/syntaxAnalyser/nodes/FuncDef.java) | Java | 32 | 1 | 8 | 41 |
| [src/syntaxAnalyser/nodes/FuncFParam.java](/src/syntaxAnalyser/nodes/FuncFParam.java) | Java | 35 | 1 | 7 | 43 |
| [src/syntaxAnalyser/nodes/FuncFParams.java](/src/syntaxAnalyser/nodes/FuncFParams.java) | Java | 30 | 1 | 8 | 39 |
| [src/syntaxAnalyser/nodes/FuncRParams.java](/src/syntaxAnalyser/nodes/FuncRParams.java) | Java | 29 | 1 | 7 | 37 |
| [src/syntaxAnalyser/nodes/FuncType.java](/src/syntaxAnalyser/nodes/FuncType.java) | Java | 25 | 1 | 9 | 35 |
| [src/syntaxAnalyser/nodes/InitVal.java](/src/syntaxAnalyser/nodes/InitVal.java) | Java | 42 | 2 | 10 | 54 |
| [src/syntaxAnalyser/nodes/LAndExp.java](/src/syntaxAnalyser/nodes/LAndExp.java) | Java | 18 | 0 | 6 | 24 |
| [src/syntaxAnalyser/nodes/LOrExp.java](/src/syntaxAnalyser/nodes/LOrExp.java) | Java | 18 | 0 | 6 | 24 |
| [src/syntaxAnalyser/nodes/LVal.java](/src/syntaxAnalyser/nodes/LVal.java) | Java | 30 | 1 | 8 | 39 |
| [src/syntaxAnalyser/nodes/MainFuncDef.java](/src/syntaxAnalyser/nodes/MainFuncDef.java) | Java | 30 | 1 | 8 | 39 |
| [src/syntaxAnalyser/nodes/MulExp.java](/src/syntaxAnalyser/nodes/MulExp.java) | Java | 18 | 1 | 6 | 25 |
| [src/syntaxAnalyser/nodes/Number.java](/src/syntaxAnalyser/nodes/Number.java) | Java | 18 | 0 | 6 | 24 |
| [src/syntaxAnalyser/nodes/PrimaryExp.java](/src/syntaxAnalyser/nodes/PrimaryExp.java) | Java | 38 | 1 | 7 | 46 |
| [src/syntaxAnalyser/nodes/RelExp.java](/src/syntaxAnalyser/nodes/RelExp.java) | Java | 18 | 0 | 6 | 24 |
| [src/syntaxAnalyser/nodes/Stmt.java](/src/syntaxAnalyser/nodes/Stmt.java) | Java | 110 | 22 | 10 | 142 |
| [src/syntaxAnalyser/nodes/UnaryExp.java](/src/syntaxAnalyser/nodes/UnaryExp.java) | Java | 41 | 4 | 7 | 52 |
| [src/syntaxAnalyser/nodes/UnaryOp.java](/src/syntaxAnalyser/nodes/UnaryOp.java) | Java | 24 | 1 | 6 | 31 |
| [src/syntaxAnalyser/nodes/VarDecl.java](/src/syntaxAnalyser/nodes/VarDecl.java) | Java | 32 | 1 | 8 | 41 |
| [src/syntaxAnalyser/nodes/VarDef.java](/src/syntaxAnalyser/nodes/VarDef.java) | Java | 34 | 1 | 8 | 43 |

[Summary](results.md) / Details / [Diff Summary](diff.md) / [Diff Details](diff-details.md)