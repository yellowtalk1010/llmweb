package vision.sast.rules.controller.html;

public class HTML {

    /***
     * 悬浮层 html 代码
     */
    public static String floatWindowHtml(){
        String html = """
                  <button id="toggleBtn">打开悬浮层</button>
                                
                  <div id="popup">
                    <div>这是一个悬浮层</div>
                    <button id="closeBtn" class="close-btn">关闭</button>
                  </div>
                """;
        return html.trim();
    }

}
