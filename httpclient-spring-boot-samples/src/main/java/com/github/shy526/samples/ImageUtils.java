package com.github.shy526.samples;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
                        if (j + x >= width || i + y >= height || temp == WHITE) {
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


    public static BufferedImage expand(BufferedImage bufferedImage, int[][] kernel) {
        int[][] pixelMatrix = image2pixelMatrix(bufferedImage, null);
        int height = pixelMatrix.length;
        int width = pixelMatrix[0].length;
        Set<String> corrodeList = new HashSet<>();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int source = pixelMatrix[i][j];
                if (source == WHITE) {
                    continue;
                }
                for (int y = 0; y < kernel.length; y++) {
                    for (int x = 0; x < kernel[y].length; x++) {
                        int temp = kernel[y][x];
                        if (j + x >= width || i + y >= height || temp == WHITE) {
                            continue;
                        }
                        if (pixelMatrix[i + y][j + x] != temp) {
                            corrodeList.add((i + y) + "," + (j + x));
                        }
                    }
                }
            }
        }
        for (String str : corrodeList) {
            String[] split = str.split(",");
            pixelMatrix[Integer.parseInt(split[0])][Integer.parseInt(split[1])] = BLACK;
        }
        return pixelMatrix2image(pixelMatrix);
    }

    public static List<BufferedImage> raySubImage(BufferedImage image) {
        List<BufferedImage> result = new ArrayList<>();

        int startX = 0;
        Set<String> check = new HashSet<String>();

        for (; true; ) {
            Set<String> blackPoint = new HashSet<String>();
            int[] startPoint = collide(image, startX, BLACK);
            startX = startPoint[0];
            int startY = startPoint[1];
            if (startX == Integer.MAX_VALUE) {
                break;
            }
            spread(image, startX, startY, check,blackPoint);
            int maxX=0;
            for (String pointStr : blackPoint) {
                String[] point = pointStr.split(",");
                int x = Integer.parseInt(point[0]);
                int y = Integer.parseInt(point[1]);
                maxX=Math.max(maxX,x);
            }
            image.setRGB(startX,startY,Color.GREEN.getRGB());
            startX=maxX+1;

        }
        // ImageUtils.saveImage(Paths.get("D:\\codeup\\httpclient-boot-starter\\httpclient-spring-boot-samples\\src\\main\\resources").resolve("test.png"), image);
        return result;
    }

    private static void spread(BufferedImage image,int startX, int startY, Set<String> check,Set<String> blackPoint) {
        int width = image.getWidth();
        int height = image.getHeight();
        List<String> scopePoint = scope(startX, startY, height, width);
        System.out.println(startX+","+startY+":"+ scopePoint);
        for (String pointStr : scopePoint) {
            String[] point = pointStr.split(",");
            int x = Integer.parseInt(point[0]);
            int y = Integer.parseInt(point[1]);
            int rgb = image.getRGB(x, y);
            if (!check.contains(pointStr) && rgb == BLACK) {
                check.add(pointStr);
                blackPoint.add(pointStr);
                spread(image,x, y, check,blackPoint);
                image.setRGB(x,y,Color.yellow.getRGB());
            }else {
                check.add(pointStr);
            }
        }
    }

    private static List<String> scope(int startX, int startY, int height, int width) {
        List<String> pointList = new ArrayList<>();
        int upY = startY - 1;
        if (upY >= 0 || upY < height) {
            pointList.add(startX + "," + upY);
        }
        int downY = startY + 1;
        if (downY >= 0 || downY < height) {
            pointList.add(startX + "," + downY);
        }
        int leftX = startX - 1;
        if (leftX >= 0 || leftX < width) {
            pointList.add(leftX + "," + startY);
        }
        int rightX = startX + 1;
        if (rightX >= 0 || leftX < width) {
            pointList.add(rightX + "," + startY);
        }
        return pointList;
    }

    private static int[] collide(BufferedImage image, int startX, int target) {
        int width = image.getWidth();
        int height = image.getHeight();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (int y = 0; y < height; y++) {
            for (int x = startX; x < width; x++) {
                int rgb = image.getRGB(x, y);
                if (rgb == target) {
                    if (minX > x) {
                        minX = x;
                        minY = y;
                    }
                    break;
                }
            }
        }
        return new int[]{minX, minY};
    }

    public static void main(String[] args) {
        Path rootPath = Paths.get("D:\\codeup\\httpclient-boot-starter\\httpclient-spring-boot-samples\\src\\main\\resources");
        String fileName = "img.png";
        Path imagePath = rootPath.resolve(fileName);
        BufferedImage image = ImageUtils.readImage(imagePath);
        BufferedImage gray = ImageUtils.gray(image);
        saveImage(rootPath.resolve("gray.png"), gray);
        BufferedImage binary = ImageUtils.binary(image, 78, 165);
        //  BufferedImage binary = ImageUtils.binary(image, 101, 215);
        saveImage(rootPath.resolve("binary.png"), binary);
        BufferedImage corrode = corrode(binary, new int[][]{
                {BLACK, WHITE},
                {BLACK, WHITE}
        });
        corrode = corrode(corrode, new int[][]{
                {BLACK, BLACK}
        });
        saveImage(rootPath.resolve("corrode.png"), corrode);
        BufferedImage expand = ImageUtils.expand(corrode, new int[][]{
                {BLACK, BLACK},
                {BLACK, BLACK}
        });
        saveImage(rootPath.resolve("expand.png"), expand);
        List<BufferedImage> bufferedImages = ImageUtils.raySubImage(expand);
        for (int i = 0; i < bufferedImages.size(); i++) {
            saveImage(rootPath.resolve("sub-" + i + ".png"), bufferedImages.get(i));
        }
    }
}
