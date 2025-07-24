package zuk;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import zuk.sast.CBaseListener;
import zuk.sast.CLexer;
import zuk.sast.CParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ExprASTExample {

    public static void main(String[] args) throws IOException {
        // 从文件读取源码
        String source = Files.readString(Paths.get("grammars-v4/c/examples/zuk_keil51.c"));

        // 1. 构造词法分析器
        CharStream input = CharStreams.fromString(source);
        CLexer lexer = new CLexer(input);

        // 2. 构造语法分析器
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CParser parser = new CParser(tokens);

        // 3. 构造语法树
        ParseTree tree = parser.compilationUnit();
        System.out.println(tree.getText());


        // 4. 打印语法树结构（可视化）
        System.out.println("Parse Tree:\n" + tree.toStringTree(parser));

        // 5. 遍历 AST（语法树）
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new MyListener(), tree);


    }

    public static class MyListener extends CBaseListener {
        @Override
        public void enterFunctionDefinitonSuffix(CParser.FunctionDefinitonSuffixContext ctx) {
            List constants = ctx.Constant();
            List identifiers = ctx.Identifier();
            List<ParseTree> children = ctx.children;
            System.out.println("");
            super.enterFunctionDefinitonSuffix(ctx);
        }

        @Override
        public void exitFunctionDefinitonSuffix(CParser.FunctionDefinitonSuffixContext ctx) {
            super.exitFunctionDefinitonSuffix(ctx);
        }
    }


}
