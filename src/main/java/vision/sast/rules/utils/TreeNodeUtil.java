package vision.sast.rules.utils;

import java.util.*;

/**
 * 将list的目录转成tree输出
 * */
public class TreeNodeUtil {

    public static class TreeNode {
        String name; //文件名称，或者文件夹名称
        String path; //完整路径
        Map<String, TreeNode> children = new TreeMap<>();

        TreeNode(String name, String path) {
            this.name = name;
            this.path = path;
        }
    }

    // 构建目录树
    public static TreeNode buildTree(List<String> paths) {
        TreeNode root = new TreeNode(""); // 根节点

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

    public static void main(String[] args) {
        List<String> paths = Arrays.asList(
                "D:/a/b/c.txt",
                "D:/a/b/d.txt",
                "D:/a/e/f.txt",
                "D:/g/h.txt"
        );

        TreeNode root = buildTree(paths);
        printTree(root, "");
    }
}
