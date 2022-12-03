package me.petrolingus.lim;

import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static me.petrolingus.lim.Configuration.N;

public class Controller {

    public ImageView imageView;

    public void initialize() {

        Algorithm algorithm = new Algorithm();
        algorithm.initialize();

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(() -> {
            System.out.println(algorithm.getTime());
            algorithm.step();
            int[][] matrix = algorithm.getMatrix();

            int s = 800;
            int size = s * s;
            double pixelSize = (double) s / N;
            int[] buffer = new int[size];

            for (int i = 0; i < size; i++) {
                int x = (int) Math.floor((i % s) / pixelSize);
                int y = (int) Math.floor((i / s) / pixelSize);
                if (matrix[y][x] == -1) {
                    buffer[i] = 0xFF333333;
                } else {
                    buffer[i] = 0xFFFFFFFF;
                }
            }

            WritableImage writableImage = new WritableImage(s, s);
            PixelWriter pixelWriter = writableImage.getPixelWriter();
            pixelWriter.setPixels(0, 0, s, s, PixelFormat.getIntArgbInstance(), buffer, 0, s);
            imageView.setImage(writableImage);
            imageView.setFitWidth(s);
            imageView.setFitHeight(s);
        }, 1000, 1, TimeUnit.MILLISECONDS);
    }


}
