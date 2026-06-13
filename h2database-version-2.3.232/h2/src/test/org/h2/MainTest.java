package org.h2;

public class MainTest {
    public static final String DATABASE_FILE_URL = "jdbc:h2:./data/h2_database";  //文件数据库
    public static void main(String[] args) {
        String args1[] = new String[]{"-user", "sa", "-password", "123456", "-url", DATABASE_FILE_URL};
        try {
            org.h2.tools.Console.main(args1); //启动h2数据库
        }catch (Exception exception) {
            exception.printStackTrace();
        }

    }
}
