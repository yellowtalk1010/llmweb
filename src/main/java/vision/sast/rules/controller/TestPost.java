package vision.sast.rules.controller;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestPost {

    @PostMapping("post")
    public  String post(){
        System.out.println("post");
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1);
        map.put("name", "小王");
        map.put("method", "post");
        return JSONObject.toJSONString(map);
    }

    @GetMapping("get")
    public  String get(){
        System.out.println("get");
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1);
        map.put("name", "小王");
        map.put("method", "get");
        return JSONObject.toJSONString(map);
    }

    @GetMapping("get1")
    public  Map get1(){
        System.out.println("get");
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1);
        map.put("name", "小王");
        map.put("method", "get");
        return map;
    }

}
