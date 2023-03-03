package com.github.shy526.samples;


import com.github.shy526.http.HttpClientService;
import com.github.shy526.http.HttpResult;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.BindException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * @author shy526
 */

@SpringBootApplication
@Slf4j
public class SampleMapperApplication implements CommandLineRunner {

    @Autowired
    private HttpClientService httpClientService;

    @Autowired
    @Qualifier("myHttp")
    private HttpClientService myHttp;


    public static void main(String[] args) {
        SpringApplication.run(SampleMapperApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        /**
         *         String url = "https://lixianla.com/";
         *         Document htmlDocument = getHtmlDocument(url, null);
         *         ifLogin(htmlDocument);
         *         String cookie = " bbs_sid=d6bml66lev6a4bdi0oa1oord14; bbs_token=8aofZcBYMV_2FmTr83JWOw8vk1NCDkw06QRMtp9Jt7f1_2B7IOHQHAgX1pPlmltP8t8HT69O72v3cIXz3FTaRdySPMGtmWk_3Ds";
         *         htmlDocument = getHtmlDocument(url, cookie);
         *         ifLogin(htmlDocument);
         */
        Tesseract tessreact = new Tesseract();
        //需要指定训练集 训练集到 https://github.com/tesseract-ocr/tessdata 下载。
        tessreact.setDatapath("D:\\tessdata-4.1.0");

        Path rootPath = Paths.get("D:\\codeup\\httpclient-boot-starter\\httpclient-spring-boot-samples\\src\\main\\resources");
        String fileName = "img.png";
        File imageFile = rootPath.resolve(fileName).toFile();
        BufferedImage read = ImageIO.read(imageFile);
        int[][] ints = imageGray(read);
        BufferedImage grayImag = saveImg(rootPath.resolve("img-h.png").toFile().getPath(), ints);
        BufferedImage erode = erode(grayImag, new int[]{2, 2});
        ImageIO.write(erode, "png", rootPath.resolve("img-p-f.png").toFile());
       // List<BufferedImage> bufferedImages = subImag(read, 36, 0, 20, read.getHeight(), 4);
/*        for (int i = 0; i < bufferedImages.size(); i++) {
            BufferedImage bufferedImage = bufferedImages.get(i);
            ImageIO.write(bufferedImage, "png", rootPath.resolve("test-sub" + i + ".png").toFile());
            int[][] imageGray = imageGray(bufferedImage);
            Path grayPath = rootPath.resolve("test-sub-h" + i + ".png");
            BufferedImage grayImag = saveImg(grayPath.toFile().getPath(), imageGray);
            try {
                String result = tessreact.doOCR(grayImag);
                System.out.println(result);
            } catch (TesseractException e) {
                log.error(e.getMessage(), e);
            }
        }*/
    }

    private List<BufferedImage> subImag(BufferedImage source, int x, int y, int w, int h, int indexMax) {
        //33,24
        List<BufferedImage> subImagList = new ArrayList<BufferedImage>();
        for (int index = 0; index < indexMax; index++, x += w) {
            subImagList.add(source.getSubimage(x, y, w, h));
        }
        return subImagList;
    }

    private Document getHtmlDocument(String url, String cookie) {
        Map<String, String> header = new HashMap<>();
        header.put("cookie", cookie);
        try (HttpResult httpResult = httpClientService.get(url, null, header)) {
            String entityStr = httpResult.getEntityStr();
            return Jsoup.parse(entityStr);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Jsoup.parse("<html></html>");
    }

    boolean ifLogin(Document html) {
        Elements signIn = html.select("#header .icon-sign-in");
        Elements user = html.select("#header .icon-user");
        if (!signIn.isEmpty() || user.isEmpty()) {
            log.error("未登录");
            return false;
        }
        log.error("已登录");
        return true;
    }


    /**
     * 转换为灰度数组
     *
     * @param image
     * @return
     */
    private int[][] imageGray(BufferedImage image) {
        // BufferedImage灰化到数组
        int[][] result = new int[image.getHeight()][image.getWidth()];
        int black = new Color(0, 0, 0).getRGB();
        int white = new Color(255, 255, 255).getRGB();
        int[] grayCount = new int[255];
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int rgb = image.getRGB(i, j);
                final int r = (rgb >> 16) & 0xff;
                final int g = (rgb >> 8) & 0xff;
                final int b = rgb & 0xff;
                int gray = (int) (0.3 * r + 0.59 * g + 0.11 * b);
                grayCount[gray] += 1;
                result[j][i] = gray;
            }
        }
        int temp = 0;
        int tempValue = 0;
        for (int i = 0; i < grayCount.length; i++) {
            int value = grayCount[i];
            if (value > tempValue) {
                tempValue = value;
                temp = i;
            }
        }
        grayCount[temp] = 0;
        int temp2 = 0;
        int temp2Value = 0;
        for (int i = 0; i < grayCount.length; i++) {
            int value = grayCount[i];
            if (value > temp2Value) {
                temp2Value = value;
                temp2 = i;
            }
        }
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int gray = result[j][i];
                if (gray < temp) {
                    result[j][i] = black;
                } else {
                    result[j][i] = white;
                }
            }
        }
        return result;
    }

    private int rpg2gray(int rpg) {
        final int r = (rpg >> 16) & 0xff;
        final int g = (rpg >> 8) & 0xff;
        final int b = rpg & 0xff;
        int gray = (int) (0.3 * r + 0.59 * g + 0.11 * b);
        int newPixel = 0;
        newPixel += 255;
        for (int i = 0; i < 3; i++) {
            newPixel = newPixel << 8;
            newPixel += gray;
        }
        return newPixel;
    }

/*    public int[][] imageBinary(BufferedImage image) {

        int [][] result = new int[image.getHeight()][image.getWidth()];
        int black=new Color(0,0,0).getRGB();
        int white=new Color(255,255,255).getRGB();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (grayArray[j][i]>180){
                    result[j][i]=black;
                }else {
                    result[j][i]=white;
                }
            }
        }

        return result;
    }*/

    private BufferedImage saveImg(String path, int[][] array) {
        File file = new File(path);
        BufferedImage br = new BufferedImage(array[0].length, array.length, BufferedImage.TYPE_INT_RGB);
        try {
            for (int i = 0; i < array.length; i++) {
                for (int j = 0; j < array[i].length; j++) {
                    br.setRGB(j, i, array[i][j]);//设置像素
                }
            }
            ImageIO.write(br, "png", file);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return br;
    }

    public static BufferedImage erode(BufferedImage image, int[] kernel) {
        int black = new Color(0, 0, 0).getRGB();
        int white = new Color(255, 255, 255).getRGB();
        int w = image.getWidth();
        int h = image.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int min = 255;
                for (int i = x; i < x + kernel[0]; i++) {
                    for (int j = y; j < y + kernel[1]; j++) {
                        if (i >= 0 && i < w && j >= 0 && j < h) {
                            int value = image.getRGB(i, j) & 0xff;
                            if (value < min) {
                                min = value;
                            }
                        }
                    }
                }
                if (min == 255) {
                    image.setRGB(x, y, white);
                } else {
                    image.setRGB(x, y, black);
                }
            }
        }
        return image;
    }

}

