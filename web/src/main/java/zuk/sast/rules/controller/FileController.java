package zuk.sast.rules.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import zuk.sast.rules.DatabaseIssue;
import zuk.sast.rules.dto.IssueDto;
import zuk.sast.rules.utils.TreeNodeUtil;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class FileController {

    @GetMapping("file_path")
    public Map<String, Object> file_path(String path){
        List<IssueDto> ls = DatabaseIssue.queryIssuesByFile(path);
        File file = new File(path);
        System.out.print(file.getName() + "，" + file.exists() + "，问题总数" + ls.size());

        //数量
        Map<String, List<IssueDto>> vtidGroupMap = ls.stream().collect(Collectors.groupingBy(dto->dto.getVtId()));

        List<Map<String, String>> list = new ArrayList<>();
        vtidGroupMap.entrySet().stream().forEach(entry->{
            String vtid = entry.getKey();
            List<IssueDto> dtos = entry.getValue();
            if(dtos.size()>0){
                Map<String, String> map = new HashMap<>();
                map.put("size", dtos.size() + "");
                map.put("vtid", vtid);
                map.put("rule", dtos.get(0).getRule());
                map.put("defectLevel", dtos.get(0).getDefectLevel());
                map.put("ruleDesc", dtos.get(0).getRuleDesc());

                list.add(map);
            }
        });

        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("path", path);

        return map;
    }

    @GetMapping("file_tree")
    public List<TreeNodeUtil.TreeNode> file_tree(String vtid, String file){
        System.out.println("file_tree，入参vtid=" + vtid + " ，file=" + file);
        List<String> filterFiles = getIssueFiles(vtid, file);
        System.out.println("文件总数：" + DatabaseIssue.queryAllFiles().size() + "，返回：" + filterFiles.size());
        TreeNodeUtil.TreeNode treeNode = TreeNodeUtil.buildTree(filterFiles);
        TreeNodeUtil.TreeNode relativeTreeNode = TreeNodeUtil.getRelativeTreeNode(treeNode);
        traverseTreeNodeForData(relativeTreeNode);
        return Arrays.asList(relativeTreeNode);
    }

    private void traverseTreeNodeForData(TreeNodeUtil.TreeNode treeNode){
        if(treeNode!=null){
            String path = treeNode.getPath();
            if(DatabaseIssue.queryIssuesByFile(path)!=null){
                treeNode.getData().put("size", DatabaseIssue.queryIssuesByFile(path).size());
            }
            treeNode.getChildren().forEach(child->{
                traverseTreeNodeForData(child);
            });
        }
    }

    private List<String> getIssueFiles(String vtid, String path){
//        if(vtid!=null && vtid.equals(DatabaseFunctionModule.FunctionModuleVtid)){
//            //函数建模
//            return DatabaseFunctionModule.queryAllFiles();
//        }
//        else if(vtid!=null && vtid.equals(DatabaseSYSTEM_CONSTRAINTS_01.VTID)){
//            //语法错误
//            return DatabaseSYSTEM_CONSTRAINTS_01.queryAllFiles();
//        }
//        else {
            //
            List<String> files = new ArrayList<>();
            if(vtid!=null && StringUtils.isNotEmpty(vtid)){
                files = DatabaseIssue.queryFilesByVtid(vtid);
            }
            else {
                files = DatabaseIssue.queryAllFiles();
            }
            List<String> filterFiles = new ArrayList<>();
            if(path!=null && StringUtils.isNotEmpty(path)){
                filterFiles = files.stream().filter(e->e.equals(path)).collect(Collectors.toList());
            }
            else {
                filterFiles = files;
            }
            return filterFiles;
//        }

    }

    @GetMapping("file_list")
    public List<Map<String, String>> file_list(String vtid, String file){
        System.out.println("file_list，入参vtid=" + vtid + " ，file=" + file);
        List<String> filterFiles = getIssueFiles(vtid, file);
        System.out.println("文件总数：" + DatabaseIssue.queryAllFiles().size() + "，返回：" + filterFiles.size());
        List<Map<String, String>> list = new ArrayList<>();
        filterFiles.stream().forEach(f->{
            Map<String, String> map = new HashMap<>();
            map.put("file", f);
            map.put("size", DatabaseIssue.queryIssuesByFile(f).size() + "");
            list.add(map);
        });

        return list;
    }

    @GetMapping("llm_files")
    public String llm_files(){
        System.out.println("文件总数：" + DatabaseIssue.queryAllFiles().size());
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseIssue.queryAllFiles().stream().map(file->{
            String str = "<a href='llm_file?f="+file+"'>"+file+"</a>&nbsp;&nbsp;&nbsp;" + DatabaseIssue.queryIssuesByFile(file).size();
            return str + "<br>";
        }).forEach(stringBuilder::append);
        return stringBuilder.toString();
    }

    @GetMapping("llm_file")
    public synchronized String llm_file(String f) {
        List<IssueDto> ls = DatabaseIssue.queryIssuesByFile(f);
        File file = new File(f);
        System.out.print(file.getName() + "，" + file.exists() + "，问题总数" + ls.size());


        //数量
        Map<String, List<IssueDto>> vtidGroupMap = ls.stream().collect(Collectors.groupingBy(dto->dto.getVtId()));

        Map<String, String> vtidMap = new HashMap<>();
        ls.stream().forEach(dto->{
            int size = 0;
            if(vtidGroupMap.get(dto.getVtId())!=null){
                size = vtidGroupMap.get(dto.getVtId()).size();
            }
            String url = "<a href='llm_sourcecode?vtid=" + dto.getVtId() + "&file=" + f + "'>"+ dto.getVtId() + "</a>&nbsp;&nbsp;&nbsp;" + size
                    + "<br>" + dto.getRule()
                    + "<br>" + dto.getDefectLevel()
                    + "<br>" + dto.getRuleDesc()
                    + "<br>" + "-------------------------------------------------------"
                    + "<br>";
            vtidMap.put(dto.getVtId(), url);
        });

        StringBuilder stringBuilder = new StringBuilder();
        vtidMap.values().forEach(vtid->stringBuilder.append(vtid));

        return f + ", " + ls.size() + "<br>" + stringBuilder.toString();
    }

}
