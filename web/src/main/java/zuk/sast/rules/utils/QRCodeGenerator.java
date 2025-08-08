package zuk.sast.rules.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class QRCodeGenerator {

    public static int MAX = 2000; //最大切割

    public static List<String> erweima(String text, int size) {
        int times = 0;
        if(text.length() % MAX == 0){
            times = text.length() / MAX;
        }
        else {
            times = (text.length() / MAX) + 1;
        }
        List<String> list = new ArrayList<String>();
        for (int i = 1; i <= times; i++) {
            String string = "";
            if(i < times){
                string = text.substring( (i-1) * MAX, i * MAX);
            }
            else {
                string = text.substring( (i-1) * MAX);
            }
            list.add(string);
        }
        StringBuilder sb = new StringBuilder();
        list.forEach(sb::append);
        if(text.equals(sb.toString())){
            System.out.println("文件切割后拼接成功，切割数是:" + list.size());
        }
        AtomicInteger index = new AtomicInteger(1);

        return list.stream().map(s->{
            return gener(s,size,index.getAndIncrement());
        }).toList();

    }

    private static String gener(String text, int size, int times){

        int width = size;
        int height = size;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                }
            }
            String filePath = "qrs/" + times + "-qrcode.png";

            if(!new File(filePath).getParentFile().exists()){
                new File(filePath).getParentFile().mkdirs();
            }
            ImageIO.write(image, "png", new File(filePath));
            //System.out.println("二维码已生成: " + filePath);
            return new File(filePath).getAbsolutePath();
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return "";
        }
    }

}