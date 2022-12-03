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

    Algorithm algorithm = new Algorithm();
    int s = 800;
    int size = s * s;
    double pixelSize = (double) s / N;
    int[] buffer = new int[size];
    WritableImage writableImage = new WritableImage(s, s);
    PixelWriter pixelWriter = writableImage.getPixelWriter();

    public void initialize() {

        algorithm.initialize();
//        algorithm.test();

        imageView.setFitWidth(s);
        imageView.setFitHeight(s);

        final long[] start = {System.currentTimeMillis()};

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleWithFixedDelay(() -> {
            foo1();
            foo2();
            foo3();
            long stop = System.currentTimeMillis();
            if (stop - start[0] > 1000) {
                foo4();
                start[0] = stop;
            }
        }, 1000, 1, TimeUnit.MILLISECONDS);
    }

    public void foo1() {
        algorithm.step();
    }

    public void foo2() {
        int[][] matrix = algorithm.getMatrix();
        for (int i = 0; i < size; i++) {
            int x = (int) Math.floor((i % s) / pixelSize);
            int y = (int) Math.floor((i / s) / pixelSize);
            if (matrix[y][x] == -1) {
                buffer[i] = 0xFF333333;
            } else {
                buffer[i] = 0xFFFFFFFF;
            }
        }
    }

    public void foo3() {
        pixelWriter.setPixels(0, 0, s, s, PixelFormat.getIntArgbInstance(), buffer, 0, s);
        imageView.setImage(writableImage);
    }

    public void foo4() {
        System.out.println(algorithm.getTime());
    }
}
