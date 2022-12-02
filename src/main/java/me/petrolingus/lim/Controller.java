package me.petrolingus.lim;

import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.concurrent.ThreadLocalRandom;

public class Controller {

    public ImageView imageView;

    public void initialize() {

        int N = 20;
        int N2 = N * N;
        int HALF = N2 / 2;

        int[][] matrix = new int[N + 2][N + 2];

        for (int i = 0; i < HALF; i++) {
            while (true) {
                int x = ThreadLocalRandom.current().nextInt(N);
                int y = ThreadLocalRandom.current().nextInt(N);
                if (matrix[y][x] == 0) {
                    matrix[y][x] = 1;
                    break;
                }
            }
        }

        int n = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                if (matrix[i][j] == 1) {
                    n++;
                }
            }
        }
        System.out.println(n);


//        WritableImage writableImage = new WritableImage(40, 40);
//        PixelWriter pixelWriter = writableImage.getPixelWriter();
//
//        for (int i = 0; i < 40; i++) {
//            for (int j = 0; j < 40; j++) {
//                double r = ThreadLocalRandom.current().nextDouble();
//                double g = ThreadLocalRandom.current().nextDouble();
//                double b = ThreadLocalRandom.current().nextDouble();
//                pixelWriter.setColor(i, j, Color.color(r, g, b));
//            }
//        }

//        imageView.setImage(writableImage);
//        imageView.setSmooth(true);
//        imageView.
    }
}
