package zuk.test.jni;

public class MainJNI {

    public native String process(String input);

    static {
        System.loadLibrary("myjni"); // 加载动态库
    }

    public static void main(String[] args) {
        System.loadLibrary("myjni"); // 加载动态库
        MainJNI obj = new MainJNI();
        System.out.println(obj.process("Hello World!"));
    }
}
