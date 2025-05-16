package vision.sast.rules.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringEscapeUtils;
import vision.sast.rules.Database;
import vision.sast.rules.RulesApplication;
import vision.sast.rules.dto.IssueDto;
import vision.sast.rules.dto.Trace;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SourceCodeUtil {


    /***
     * 读取文件
     */
     public static List<String> openFile(String fileName) throws Exception {

        String codeFormat = "GBK";
        if(Database.PROPERTIES.get(PropertiesKey.codeFormat)!=null){
            codeFormat = (String) Database.PROPERTIES.get(PropertiesKey.codeFormat);
        }
        System.out.println("open file format = " + codeFormat);

        List<String> lines = FileUtils.readLines(new File(fileName),codeFormat);

        return lines;

    }

    public static String show(String fileName, List<IssueDto> dtoList, Integer redLine) throws Exception {

        System.out.println("fileName = " + fileName + ", dtoList = " + dtoList.size());
        List<IssueDto> sortedList = dtoList.stream().sorted(Comparator.comparing(IssueDto::getLine)).toList();

        List<String> lines = openFile(fileName);
        HighLightUtil highlighterUtil = new HighLightUtil();
        AtomicInteger counter = new AtomicInteger(0);
        List<String> newLines = lines.stream().map(line->{
//            line = StringEscapeUtils.escapeHtml4(line);
            line = highlighterUtil.highlightLine(line);
            counter.getAndIncrement();
            if(redLine!=null && counter.get()==redLine){
                line = "<div style='border: 2px solid red;'>" + line + "</div>"; //标记为红色边框
            }
            line = "<li>" + line + "</li>";

            return line;
        }).collect(Collectors.toList());


        int insertTime = 0;
        for (IssueDto dto : sortedList) {
            List<Trace> traces = dto.getTraces();
            StringBuilder traceBuilder = new StringBuilder();
            traces.stream().forEach(trace->{
                String s = "";
                if(trace.getFile().equals(fileName)){
                    s = "当前文件" + "&nbsp#&nbsp" + trace.getLine() + "&nbsp#&nbsp" + trace.getMessage();
                }
                else {
                    s = "<a href='llm_sourcecode?file="+trace.getFile()+"&line="+trace.getLine()+"'>" + s + "</a>";
                }

                traceBuilder.append(s + "<br>");
            });

            int line = dto.getLine();
            int index = line + insertTime;
            if (index > 0) {
                String divStr = "<div style='background-color: pink'>"
                        + dto.getName() + "<br>"
                        + dto.getLine() + "/" + dto.getVtId() + "/" + dto.getRule() + "/" + dto.getDefectLevel() + "/" + dto.getDefectType() + "/" + "<br>"
                        + dto.getRuleDesc() + "<br>"
                        + dto.getIssueDesc() + "<br>"
                        + traceBuilder.toString()
                        + "<a class='btn' id='"+dto.getId()+"'>AI审计</a>" + "<br>"
                        + "</div>";
                newLines.add(index, divStr);
                insertTime++;
            }
        }

        StringBuilder sb = new StringBuilder("<ol>");
        for (String line : newLines) {
            sb.append(line);
            sb.append("<br>");
        }

        sb.append("</ol>");
        return sb.toString();
    }

    public static Pair<List<String>, List<IssueDto>> show1(String fileName, List<IssueDto> dtoList) throws Exception {

        System.out.println("fileName = " + fileName + ", dtoList = " + dtoList.size());
        List<IssueDto> sortedList = dtoList.stream().sorted(Comparator.comparing(IssueDto::getLine)).toList();

        List<String> lines = openFile(fileName);
        HighLightUtil highlighterUtil = new HighLightUtil();
        AtomicInteger lineNumber = new AtomicInteger(1);
        List<String> newLines = lines.stream().map(line -> {
//            line = StringEscapeUtils.escapeHtml4(line);
            line = highlighterUtil.highlightLine(line);
            line = "<li id='line_" + lineNumber.getAndIncrement() + "'>" + line + "</li>";
            return line;
        }).collect(Collectors.toList());

        Pair<List<String>, List<IssueDto>> pair = new ImmutablePair<>(newLines, sortedList);
        return pair;
    }

    public static List<String> show2(String fileName) throws Exception {

        System.out.println("fileName = " + fileName + "");
        List<String> lines = openFile(fileName);
        HighLightUtil highlighterUtil = new HighLightUtil();
        AtomicInteger lineNumber = new AtomicInteger(1);
        List<String> newLines = lines.stream().map(line -> {
//            line = StringEscapeUtils.escapeHtml4(line);
            line = highlighterUtil.highlightLine(line);
            line = "<li id='otherfileline_" + lineNumber.getAndIncrement() + "'>" + line + "</li>";
            return line;
        }).collect(Collectors.toList());

        return newLines;
    }
}
