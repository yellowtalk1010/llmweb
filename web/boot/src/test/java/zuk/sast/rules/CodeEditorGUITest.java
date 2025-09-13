package zuk.sast.rules;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.TokenMaker;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.modes.CPlusPlusTokenMaker;

import javax.swing.text.Segment;

/***
 * 可视化代码编辑器
 */
public class CodeEditorGUITest {

    public static void main(String[] args) {

//        SwingUtilities.invokeLater(()->{
//            String code = getCatCode();
//            CodeCatEditor.createAndShowGUI(code);
//        });

        String code = getCode();
        CodeEditor.createAndShowGUI(code);

        TokenMaker tokenMaker;
        TokenMakerFactory tokenMakerFactory;
        CPlusPlusTokenMaker cPlusPlusTokenMaker;
        SyntaxScheme syntaxScheme;
//        syntaxScheme.getStyleCount();
        SyntaxConstants syntaxConstants;
        Segment segment;
//        STYLE_COMMENT_EOL;
//        STYLE_LITERAL_STRING_DOUBLE_QUOTE;
//        STYLE_LITERAL_CHAR;
//        STYLE_LITERAL_BACKQUOTE;
//        STYLE_LITERAL_NUMBER_DECIMAL_INT;
//        STYLE_LITERAL_NUMBER_FLOAT;
//        STYLE_LITERAL_NUMBER_HEXADECIMAL;
//        STYLE_LITERAL_BOOLEAN;
//        STYLE_IDENTIFIER;
//        STYLE_FUNCTION;
//        STYLE_PREPROCESSOR;

    }

    private static String getCode() {
        return """
               #include <stdio.h>
               using namespace std;
                              
               int main() {  //你好
                   cout << "Hello, C++!" << endl;
                   return 0;
               }
               """;
    }
}
