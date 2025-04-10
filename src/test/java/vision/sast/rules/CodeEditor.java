package vision.sast.rules;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

public class CodeEditor {

    public static void createAndShowGUI(String text) {
        JFrame frame = new JFrame("Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);

        // 创建 RSyntaxTextArea
        RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS); //c++语法
        textArea.setCodeFoldingEnabled(true);
        textArea.setText(text);

        // 将 RSyntaxTextArea 包装在 RTextScrollPane 中
        RTextScrollPane sp = new RTextScrollPane(textArea);
        frame.add(sp);

        frame.setVisible(true);
    }
}
