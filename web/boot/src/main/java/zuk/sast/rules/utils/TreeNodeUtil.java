package zuk.sast.rules.utils;

import lombok.Getter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将list的目录转成tree输出
 * */
public class TreeNodeUtil {

    /**
     * window默认文件夹、文件排序
     **/
    public static class NaturalOrderComparator implements Comparator<String> {
        private final Pattern pattern = Pattern.compile("(\\d+)|(\\D+)");

        @Override
        public int compare(String a, String b) {
            Matcher ma = pattern.matcher(a.toLowerCase());
            Matcher mb = pattern.matcher(b.toLowerCase());

            while (ma.find() && mb.find()) {
                String pa = ma.group();
                String pb = mb.group();

                int result;
                if (Character.isDigit(pa.charAt(0)) && Character.isDigit(pb.charAt(0))) {
                    result = Integer.compare(Integer.parseInt(pa), Integer.parseInt(pb));
                } else {
                    result = pa.compareTo(pb);
                }

                if (result != 0) {
                    return result;
                }
            }

            return Integer.compare(a.length(), b.length());
        }
    }

    public static class TreeNode {
        @Getter
        private String name; //文件名称，或者文件夹名称
        @Getter
        private String path; //完整路径
        @Getter
        private Map<String, Object> data = new HashMap<>(); //备份数据

        private Map<String, TreeNode> children = new TreeMap<>();

        public List<TreeNode> getChildren(){
//            return new ArrayList<>(children.values()).stream().sorted(Comparator.comparing(e->e.getName())).toList(); //按名称排序
            return new ArrayList<>(children.values());
        }

        TreeNode(String name, String path) {
            this.name = name;
            this.path = path;
        }
    }

    // 构建目录树
    public static TreeNode buildTree(List<String> paths) {
        TreeNode root = new TreeNode("",""); // 根节点

        for (String path : paths) {
            String[] parts = path.split("/|\\\\"); // 支持 Unix 和 Windows 路径分隔符
            TreeNode current = root;

            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];


                List<String> lines = new ArrayList<>();
                for (int j = 0; j <= i; j++) {
                    lines.add(parts[j]);
                }
                String partPath = String.join("/", lines);

                current.children.putIfAbsent(part, new TreeNode(part, partPath));
                current = current.children.get(part);
            }
        }

        return root;
    }

    // 打印树结构
    public static void printTree(TreeNode node, String prefix) {
        for (TreeNode child : node.children.values()) {
            System.out.println(prefix + child.name);
            printTree(child, prefix + "    ");
        }
    }

    // 获取相对路径的TreeNode
    public static TreeNode getRelativeTreeNode(TreeNode node) {
        if(node.getChildren()==null || node.getChildren().isEmpty()){
            //没有子节点，则当前节点就是相对路径的根节点
            return node;
        }
        else {
            if(node.getChildren().size()==1){
                return getRelativeTreeNode(node.getChildren().get(0));
            }
            else {
                //多个子节点，则当前节点就是相对路径根节点
                return node;
            }
        }
    }


    public static void main(String[] args) {
        List<String> paths = Arrays.asList(
                "D:/a/b/c.txt",
                "D:/a/b/d.txt",
                "D:/a/e/f.txt",
                "D:/g/h.txt"
        );

        System.out.println("================绝对路径输出=============");

        TreeNode root = buildTree(paths);
        printTree(root, "");

        System.out.println("================相对路径输出=============");

        TreeNode relativeTreeNode = getRelativeTreeNode(root);
        printTree(relativeTreeNode, "");
    }
}
