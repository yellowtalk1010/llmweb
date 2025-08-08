package zuk.sast.rules.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ResourcesController {

    /***
     * 获取 c/c++ 代码高亮样式
     */
    @RequestMapping("cpp.css")
    public String cpp_css(){
        return "cpp.css";
    }

    /***
     * 获取 悬浮窗口 样式
     */
    @RequestMapping("float_window.css")
    public String float_window_css(){
        return "float_window.css";
    }

    /***
     * 获取 悬浮窗口 js
     */
    @RequestMapping("float_window.js")
    public String float_window_js(){
        return "float_window.js";
    }


    /***
     * 获取websocket的html代码demo
     */
    @RequestMapping("webSocket.html")
    public String webSocket(){
        return "webSocket.html";
    }

    /***
     * 获取在html中显示markdown的代码demo
     */
    @RequestMapping("markdown.html")
    public String markdown(){
        return "markdown.html";
    }

}
