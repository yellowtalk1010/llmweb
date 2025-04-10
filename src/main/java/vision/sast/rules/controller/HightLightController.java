package vision.sast.rules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.RulesApplication;
import vision.sast.rules.utils.HighLightUtil;
import vision.sast.rules.utils.PropertiesKey;

@RestController
public class HightLightController {


    @GetMapping("highLight")
    public synchronized String highLight(String file) {
        try {
            String html = HighLightUtil.highlightFile(file);

            html = "<html>" +
                    "<head>" +
//                    "<link rel='stylesheet' href='cpp.css'>" +
                    "</head>" +
                    "<body>" +
                    "<pre><code class='language-cpp'>" +
                    html +
                    "</code></pre>" +
                    "</body>" +
                    "</html>";

            return html;
        }
        catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

}
