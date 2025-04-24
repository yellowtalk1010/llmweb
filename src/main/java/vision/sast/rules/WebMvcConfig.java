package vision.sast.rules;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
//    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/{spring:[a-zA-Z0-9_\\-]+}")
//                .setViewName("forward:/index.html");
//        registry.addViewController("/**/{spring:[a-zA-Z0-9_\\-]+}")
//                .setViewName("forward:/index.html");
//        registry.addViewController("/{spring:[a-zA-Z0-9_\\-]+}/**{spring:?!(\\.js|\\.css|\\.png)$}")
//                .setViewName("forward:/index.html");
//    }

//    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//        // 所有路径都转发到 index.html（排除资源文件：.js, .css, .png 等）
//        registry.addViewController("/{path:[^\\.]*}")
//                .setViewName("forward:/index.html");
//        registry.addViewController("/**/{path:^(?!.*\\.).*$}")
//                .setViewName("forward:/index.html");
//    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 根路径直接转发到 index.html
        registry.addViewController("/")
                .setViewName("forward:/index.html");

        // 一级路径转发，例如 /about
        registry.addViewController("/{x:[\\w\\-]+}")
                .setViewName("forward:/index.html");

        // 二级路径转发，例如 /user/123
        registry.addViewController("/{x:^(?!api$).*$}/{y:[\\w\\-]+}")
                .setViewName("forward:/index.html");
    }
}
