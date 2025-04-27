package vision.sast.rules.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("name", "Spring User");
        return "index"; // 这将返回templates目录下的index.html文件
    }
}