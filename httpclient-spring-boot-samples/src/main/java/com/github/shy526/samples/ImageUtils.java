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

    /**
     * 不符合模型的腐蚀图片
     *
     * @param bufferedImage 原图
     * @param kernel        模型
     * @param orig          模型远点
     * @return 腐蚀后的图
     */
    public static BufferedImage corrode(BufferedImage bufferedImage, int[][] kernel, int[] orig) {
        int[][] pixelMatrix = image2pixelMatrix(bufferedImage, null);
        int height = pixelMatrix.length;
        int width = pixelMatrix[0].length;
        Set<Integer[]> corrodePoint = new HashSet<>();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                List<Integer[]> result = checkKernel(kernel, pixelMatrix, i, j, orig);
                if (!result.isEmpty()) {
                    corrodePoint.add(new Integer[]{i, j});
                }
            }
        }
        for (Integer[] point : corrodePoint) {
            pixelMatrix[point[0]][point[1]] = WHITE;
        }
        return pixelMatrix2image(pixelMatrix);
    }

    private static List<Integer[]> checkKernel(int[][] kernel, int[][] pixelMatrix, int sourceY, int sourceX, int[] orig) {
        int height = pixelMatrix.length;
        int width = pixelMatrix[0].length;
        int origX = orig[1];
        int origY = orig[0];
        List<Integer[]> result = new ArrayList<>();
        if (pixelMatrix[sourceY][sourceX] == WHITE) {
            return result;
        }
        for (int y = 0; y < kernel.length; y++) {
            for (int x = 0; x < kernel[y].length; x++) {
                int tempColor = kernel[y][x];
                if (tempColor == WHITE) {
                    continue;
                }
                int offsetX = x - origX;
                int offsetY = y - origY;
                int tempY = sourceY + offsetY;
                int tempX = sourceX + offsetX;
                if (tempX < 0 || tempX >= width || tempY < 0 || tempY >= height) {
                    return result;
                }
                int color = pixelMatrix[tempY][tempX];
                if (color != tempColor) {
                    result.add(new Integer[]{tempY, tempX});
                }
            }
        }
        return result;
    }


    public static BufferedImage expand(BufferedImage bufferedImage, int[][] kernel, int[] orig) {
        int[][] pixelMatrix = image2pixelMatrix(bufferedImage, null);
        int height = pixelMatrix.length;
        int width = pixelMatrix[0].length;
        List<Integer[]> result = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                result.addAll(checkKernel(kernel, pixelMatrix, i, j, orig));

            }
        }
        for (Integer[] point : result) {
            pixelMatrix[point[0]][point[1]] = BLACK;
        }
        return pixelMatrix2image(pixelMatrix);
    }

    /**
     * 利用射线+扩散切取文字主题
     *
     * @param image 原图
     * @return 切割的图片
     */
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
            spread(image, startX, startY, check, blackPoint);
            int maxX = 0;
            int maxY = 0;
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            for (String pointStr : blackPoint) {
                String[] point = pointStr.split(",");
                int x = Integer.parseInt(point[0]);
                int y = Integer.parseInt(point[1]);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
            }
            image.setRGB(startX, startY, Color.GREEN.getRGB());
            startX = maxX + 1;
            int offset = 30;
            int width = maxX - minX + 2 + offset;

            int height = maxY - minY + 2 + offset;
            Set<String> newPoint = new HashSet<String>();
            for (String pointStr : blackPoint) {
                String[] point = pointStr.split(",");
                int x = Integer.parseInt(point[0]);
                int y = Integer.parseInt(point[1]);
                int newX = x - minX + offset / 2;
                int newY = y - minY + offset / 2;
                newPoint.add(newX + "," + newY);
            }
            BufferedImage subImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (newPoint.contains(x + "," + y)) {
                        subImage.setRGB(x, y, BLACK);
                    } else {
                        subImage.setRGB(x, y, WHITE);
                    }

                }
            }
            result.add(subImage);
        }
        return result;
    }


    /**
     * 扩散选取莫个主题
     *
     * @param image      原题
     * @param startX     起始点位
     * @param startY     起始点位
     * @param check      检查过的点
     * @param blackPoint 目标点
     */
    private static void spread(BufferedImage image, int startX, int startY, Set<String> check, Set<String> blackPoint) {
        int width = image.getWidth();
        int height = image.getHeight();
        List<String> scopePoint = scope(startX, startY, height, width);
        for (String pointStr : scopePoint) {
            String[] point = pointStr.split(",");
            int x = Integer.parseInt(point[0]);
            int y = Integer.parseInt(point[1]);
            int rgb = image.getRGB(x, y);
            if (!check.contains(pointStr) && rgb == BLACK) {
                check.add(pointStr);
                blackPoint.add(pointStr);
                spread(image, x, y, check, blackPoint);
                image.setRGB(x, y, Color.yellow.getRGB());
            } else {
                check.add(pointStr);
            }
        }
    }

    /**
     * 获取莫个点四周的点
     *
     * @param startX 初始点位
     * @param startY 初始点位
     * @param height 高
     * @param width  宽
     * @return 四周的点
     */
    private static List<String> scope(int startX, int startY, int height, int width) {
        List<String> pointList = new ArrayList<>();
        int upY = startY - 1;
        if (upY >= 0 && upY < height) {
            pointList.add(startX + "," + upY);
        }
        int downY = startY + 1;
        if (downY >= 0 && downY < height) {
            pointList.add(startX + "," + downY);
        }
        int leftX = startX - 1;
        if (leftX >= 0 && leftX < width) {
            pointList.add(leftX + "," + startY);
        }
        int rightX = startX + 1;
        if (rightX >= 0 && rightX < width) {
            pointList.add(rightX + "," + startY);
        }
        return pointList;
    }

    /**
     * 射线横向碰撞检测
     *
     * @param image  图片
     * @param startX 初始x
     * @param target 目标颜色
     * @return [x, y]
     */
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
        Path rootPath = Paths.get("httpclient-spring-boot-samples/src/main/resources");
        String fileName = "img3.png";
        Path imagePath = rootPath.resolve(fileName);
        BufferedImage image = ImageUtils.readImage(imagePath);
        BufferedImage gray = ImageUtils.gray(image);
        saveImage(rootPath.resolve("gray.png"), gray);
       // BufferedImage binary = ImageUtils.binary(image, 68, 180);
        BufferedImage binary = ImageUtils.binary(image, 200, 250);

        //  BufferedImage binary = ImageUtils.binary(image, 101, 215);
        saveImage(rootPath.resolve("binary.png"), binary);
        BufferedImage corrode = corrode(binary, new int[][]{
                {BLACK, WHITE},
                {BLACK, WHITE}
        }, new int[]{0, 0});
        corrode = corrode(corrode, new int[][]{
                {BLACK, BLACK}
        }, new int[]{0, 0});
        saveImage(rootPath.resolve("corrode.png"), corrode);
/*        BufferedImage expand = ImageUtils.expand(corrode, new int[][]{
                {BLACK, BLACK, BLACK},
                {BLACK, BLACK, BLACK},
                {BLACK, BLACK, BLACK}
        }, new int[]{1, 1});
        saveImage(rootPath.resolve("expand.png"), expand);
        List<BufferedImage> bufferedImages = ImageUtils.raySubImage(expand);
        for (int i = 0; i < bufferedImages.size(); i++) {
            saveImage(rootPath.resolve("sub-" + i + ".png"), bufferedImages.get(i));
        }*/
    }
}
