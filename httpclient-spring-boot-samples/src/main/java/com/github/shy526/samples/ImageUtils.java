package com.github.shy526.samples;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.ref.Reference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Slf4j
public class ImageUtils {

    private final static int BLACK = new Color(0, 0, 0).getRGB();
    private final static int WHITE = new Color(255, 255, 255).getRGB();

    public static BufferedImage gray(BufferedImage image) {
        int[][] pixelMatrix = image2pixelMatrix(image, (rpg) -> {
            int gray = getGray(rpg);
            return colorToRgb(255, gray, gray, gray);
        });
        return pixelMatrix2image(pixelMatrix);
    }

    private static int getGray(Integer rpg) {
        final int r = (rpg >> 16) & 0xff;
        final int g = (rpg >> 8) & 0xff;
        final int b = rpg & 0xff;
        return (int) (0.3 * r + 0.59 * g + 0.11 * b);
    }

    public static BufferedImage binary(BufferedImage image, int thresholdMin, int thresholdMax) {
        AtomicInteger grayTotal = new AtomicInteger(0);
        int[][] pixelMatrix = image2pixelMatrix(image, (rpg) -> {
            int gray = getGray(rpg);
            grayTotal.addAndGet(gray);
            return gray;
        });
        for (int i = 0; i < pixelMatrix.length; i++) {
            for (int j = 0; j < pixelMatrix[i].length; j++) {
                int gray = pixelMatrix[i][j];
                int rpg = WHITE;
                if (gray >= thresholdMin && gray <= thresholdMax) {
                    rpg = BLACK;
                }
                pixelMatrix[i][j] = rpg;
            }
        }
        return pixelMatrix2image(pixelMatrix);
    }

    private static int colorToRgb(int alpha, int red, int green, int blue) {
        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red;
        newPixel = newPixel << 8;
        newPixel += green;
        newPixel = newPixel << 8;
        newPixel += blue;
        return newPixel;
    }

    public static int[][] image2pixelMatrix(BufferedImage image, Function<Integer, Integer> rule) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] result = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                result[y][x] = rule != null ? rule.apply(rgb) : rgb;
            }
        }
        return result;
    }

    /**
     * 像素矩阵转图片
     *
     * @param pixelMatrix 像素矩阵
     * @return BufferedImage 图片
     */
    public static BufferedImage pixelMatrix2image(int[][] pixelMatrix) {
        int width = pixelMatrix[0].length;
        int height = pixelMatrix.length;
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                result.setRGB(x, y, pixelMatrix[y][x]);
            }
        }
        return result;
    }

    public static void saveImage(Path path, int[][] pixelMatrix) {
        try {
            ImageIO.write(pixelMatrix2image(pixelMatrix), "png", path.toFile());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void saveImage(Path path, BufferedImage image) {
        try {
            ImageIO.write(image, "png", path.toFile());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static BufferedImage readImage(Path path) {
        try {
            return ImageIO.read(path.toFile());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static BufferedImage corrode(BufferedImage bufferedImage, int[][] kernel) {
        int[][] pixelMatrix = image2pixelMatrix(bufferedImage, null);
        int height = pixelMatrix.length;
        int width = pixelMatrix[0].length;
        List<String> corrodeList = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                boolean flag = false;
                for (int y = 0; y < kernel.length; y++) {
                    for (int x = 0; x < kernel[y].length; x++) {
                        int temp = kernel[y][x];
                        if (j + x >= width || i + y >= height||temp==WHITE) {
                            continue;
                        }
                        if (pixelMatrix[i + y][j + x] != temp) {
                            flag = true;
                        }
                    }
                }
                if (flag) {
                    if (pixelMatrix[i][j] != WHITE) {
                        corrodeList.add(i + "," + j);
                    }

                }
            }
        }
        for (String str : corrodeList) {
            String[] split = str.split(",");
            pixelMatrix[Integer.parseInt(split[0])][Integer.parseInt(split[1])] = WHITE;
        }
        return pixelMatrix2image(pixelMatrix);
    }

    public static void main(String[] args) {
        Path rootPath = Paths.get("D:\\codeup\\httpclient-boot-starter\\httpclient-spring-boot-samples\\src\\main\\resources");
        String fileName = "img.png";
        Path imagePath = rootPath.resolve(fileName);
        BufferedImage image = ImageUtils.readImage(imagePath);
        BufferedImage gray = ImageUtils.gray(image);
        saveImage(rootPath.resolve("gray.png"), gray);
        BufferedImage binary = ImageUtils.binary(image, 78, 165);
        saveImage(rootPath.resolve("binary.png"), binary);
        BufferedImage corrode = corrode(binary, new int[][]{
                {BLACK,WHITE },
                {BLACK,WHITE }
        });
        corrode = corrode(corrode, new int[][]{
                {BLACK,BLACK }
        });
        saveImage(rootPath.resolve("corrode.png"), corrode);
    }
}
