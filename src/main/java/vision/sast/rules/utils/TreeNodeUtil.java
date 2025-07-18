package vision.sast.rules.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 将list的目录转成tree输出
 * */
public class TreeNodeUtil {

    public static class TreeNode {
        String name;
        Map<String, TreeNode> children = new TreeMap<>();

        TreeNode(String name) {
            this.name = name;
        }
    }

    // 构建目录树
    public static TreeNode buildTree(List<String> paths) {
        TreeNode root = new TreeNode(""); // 根节点

        for (String path : paths) {
            String[] parts = path.split("/|\\\\"); // 支持 Unix 和 Windows 路径分隔符
            TreeNode current = root;

            for (String part : parts) {
                current.children.putIfAbsent(part, new TreeNode(part));
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
                "a/b/c.txt",
                "a/b/d.txt",
                "a/e/f.txt",
                "g/h.txt"
        );

        TreeNode root = buildTree(paths);
        printTree(root, "");
    }
}
