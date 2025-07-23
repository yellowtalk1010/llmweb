package vision.sast.rules.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static int MAX = 4000; //最大切割
    public static void main(String[] args) {
        System.out.println("command params命令参数:" +
                "\n-file filepPath //文件路径" +
                "\n-string stringContent //内容" +
                "\n-size 900 //图片大小" +
                "\n-max 4000");
        List<String> list = Arrays.stream(args).toList();
        String file = null;
        String string = "helloworld.";
        int size = 900;
        for(int i = 0; i < list.size(); ++i){
            String arg = list.get(i);
            if(arg.equals("-file")){
                file = list.get(i+1);
            }else if(arg.equals("-string")){
                string = list.get(i+1);
            }else if(arg.equals("-size")){
                try{
                    size = Integer.parseInt(list.get(i+1));
                }catch (Exception e) {}
            }
            else if(arg.equals("-max")){
                try{
                    MAX = Integer.parseInt(list.get(i+1));
                }catch (Exception e) {}
            }
        }
        if(file!=null){
            headleFile(file, size);
        } else if(string!=null){
            handleString(string, size);
        }
    }

    private static void headleFile(String file, int size) {
        File f = new File(file);
        System.out.println("文件: " + file + ", 是否存在:" + f.exists());
        StringBuilder stringBuilder = new StringBuilder();
        try {
            List<String> lines = FileUtils.readLines(f, "UTF-8");
            lines.stream().map(l->l+"\n").forEach(stringBuilder::append);
        }catch (Exception e) {
            e.printStackTrace();
        }
        QRCodeGenerator.erweima(stringBuilder.toString(), size);
    }

    private static void handleString(String string, int size) {
        System.out.println("内容: " + string);
        QRCodeGenerator.erweima(string, size);
    }


}