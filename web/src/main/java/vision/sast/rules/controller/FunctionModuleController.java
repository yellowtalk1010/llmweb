package vision.sast.rules.controller;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import vision.sast.rules.DatabaseFunctionModule;
import vision.sast.rules.dto.IssueDto;
import vision.sast.rules.dto.fm.FunctionModuleInputOutputDto;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController()
public class FunctionModuleController {

    @Data
    private static class FuncModuleRequestDto {
        private String issueId;
        private List<String> paramValues;
    }

    @PostMapping("handle_func_module")
    public synchronized Map<String, Object> handle_func_module(@RequestBody FuncModuleRequestDto funcModuleRequestDto) {
        System.out.println("函数输入输出:" + JSON.toJSONString(funcModuleRequestDto));
        Map<String, Object> map = new HashMap<>();
        map.put("status", "失败");
        if(funcModuleRequestDto.getIssueId()!=null
                && StringUtils.isNotBlank(funcModuleRequestDto.getIssueId())
                && funcModuleRequestDto.getParamValues()!=null
                && funcModuleRequestDto.getParamValues().stream().filter(e->(e!=null && ( e.equals("") || e.equals("in") || e.equals("out")))).count() == funcModuleRequestDto.getParamValues().size()){
            IssueDto issueDto = DatabaseFunctionModule.queryIssueDtoById(funcModuleRequestDto.getIssueId());
            if(issueDto!=null){
                Object object = issueDto.getData();
                if(object instanceof FunctionModuleInputOutputDto){
                    FunctionModuleInputOutputDto functionModuleInputOutputDto = (FunctionModuleInputOutputDto) object;
                    if(functionModuleInputOutputDto.getParams()!=null
                            && funcModuleRequestDto.getParamValues()!=null
                            && functionModuleInputOutputDto.getParams().size()==funcModuleRequestDto.getParamValues().size()){
                        AtomicInteger index = new AtomicInteger(0);
                        functionModuleInputOutputDto.getParams().stream().forEach(p->{
                            String val = funcModuleRequestDto.getParamValues().get(index.getAndIncrement());
                            p.setIn_out(val);
                        });

                        map.put("status", "success");
                        map.put("data", DatabaseFunctionModule.queryIssueDtoById(funcModuleRequestDto.getIssueId()));
                        return map;
                    }
                }
            }
        }
        return map;
    }





}
