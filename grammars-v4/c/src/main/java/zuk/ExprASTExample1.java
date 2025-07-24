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

//
public class ExprASTExample1 {

    public static void main(String[] args) throws IOException {
        String source = Files.readString(Paths.get("grammars-v4/c/examples/keil51_demo1.c"));

        System.out.println("=== 原始源码 ===");
        System.out.println(source);
        System.out.println("================\n");

        CharStream input = CharStreams.fromString(source);
        CLexer lexer = new CLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CParser parser = new CParser(tokens);

        ParseTree tree = parser.compilationUnit();

        // ⬇️ 创建 TokenStreamRewriter
        TokenStreamRewriter rewriter = new TokenStreamRewriter(tokens);

        // ⬇️ 启动语法树遍历
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new MyListener(rewriter), tree);

        // ⬇️ 打印修改后的源代码
        String modifiedSource = rewriter.getText();
        System.out.println("替换后的源码：");
        System.out.println(modifiedSource);
    }

    public static class MyListener extends CBaseListener {
        private final TokenStreamRewriter rewriter;

        public MyListener(TokenStreamRewriter rewriter) {
            this.rewriter = rewriter;
        }

        @Override
        public void enterFunctionDefinitonSuffix(CParser.FunctionDefinitonSuffixContext ctx) {
            List<ParseTree> children = ctx.children;
            if(children!=null && children.size()>0){
                Token start = ctx.getStart();
                Token stop = ctx.getStop();

                // 计算总长度（含换行、空格）
                int startIdx = start.getStartIndex();
                int stopIdx = stop.getStopIndex();
                int length = stopIdx - startIdx + 1;

                // 生成等长空格字符串
                StringBuilder blank = new StringBuilder();
                for (int i = 0; i < length; i++) blank.append(' ');

                // 替换原始区域为空格
                rewriter.replace(start, stop, blank.toString());

                System.out.println("清空 FunctionDefinitonSuffix 区域：" + startIdx + " ~ " + stopIdx);
            }

        }
    }

}
