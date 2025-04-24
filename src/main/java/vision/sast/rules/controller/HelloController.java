package vision.sast.rules.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HelloController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("name", "Spring User");
        System.out.println("Hello Spring User");
        return "index"; // 这将返回templates目录下的index.html文件
    }
}