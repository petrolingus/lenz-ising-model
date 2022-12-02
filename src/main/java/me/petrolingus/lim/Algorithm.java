package me.petrolingus.lim;

import java.util.concurrent.ThreadLocalRandom;

import static me.petrolingus.lim.Configuration.*;

public class Algorithm {

    private int[][] matrix = new int[N][N];

    public void initialize() {

        // Generate spins
        for (int i = 0; i < N * N / 2; i++) {
            while (true) {
                int x = ThreadLocalRandom.current().nextInt(N);
                int y = ThreadLocalRandom.current().nextInt(N);
                if (matrix[y][x] == 0) {
                    matrix[y][x] = 1;
                    break;
                }
            }
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (matrix[i][j] == 0) {
                    matrix[i][j] = -1;
                }
            }
        }

        // Check generated spins
        testCorrectSpinGeneration();

        double e = calculateEnergy();
        System.out.println(e);
    }

    public void testCorrectSpinGeneration() {

        int sum = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                sum += matrix[i][j];
            }
        }

        if (sum != 0) {
            System.err.println("Not corrected fill matrix!");
        }
    }

    public void testPerformance() {

        double e = 0;
        long start1 = System.currentTimeMillis();
        for (int i = 0; i < 1_000_000; i++) {
            e += calculateEnergy();
        }
        long stop1 = System.currentTimeMillis();
        System.out.println(stop1 - start1 + ": " + e);


        e = 0;
        long start2 = System.currentTimeMillis();
        for (int i = 0; i < 1_000_000; i++) {
            e += calculateEnergy2();
        }
        long stop2 = System.currentTimeMillis();
        System.out.println(stop2 - start2 + ": " + e);

    }

//    private void process(int[][] matrix) {
//
////        long start = System.currentTimeMillis();
//
//        for (int i = 0; i < NN; i++) {
//            while (true) {
//                int x0 = ThreadLocalRandom.current().nextInt(1, N + 1);
//                int y0 = ThreadLocalRandom.current().nextInt(1, N + 1);
//                int x1 = ThreadLocalRandom.current().nextInt(1, N + 1);
//                int y1 = ThreadLocalRandom.current().nextInt(1, N + 1);
//                int s0 = matrix[y0][x0];
//                int s1 = matrix[y1][x1];
//                if (s0 != s1) {
//                    matrix[y0][x0] = s1;
//                    matrix[y1][x1] = s0;
//                    break;
//                }
//            }
//        }
//
////        long stop = System.currentTimeMillis();
////        System.out.println("Process took " + (stop - start) + "ms!");
//    }

    public double calculateEnergy() {

        double energy = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int left = (j == 0) ? matrix[i][N - 1] : matrix[i][j - 1];
//                int right = (j == N - 1) ? matrix[i][0] : matrix[i][j + 1];
                int top = (i == 0) ? matrix[N - 1][j] : matrix[i - 1][j];
//                int bottom = (i == N - 1) ? matrix[0][j] : matrix[i + 1][j];
                energy += matrix[i][j] * (left + top);
            }
        }

        return -ENERGY_SHIFT * energy / 2;
    }

    public double calculateEnergy2() {

        double energy = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int left = (j == 0) ? matrix[i][N - 1] : matrix[i][j - 1];
                int right = (j == N - 1) ? matrix[i][0] : matrix[i][j + 1];
                int top = (i == 0) ? matrix[N - 1][j] : matrix[i - 1][j];
                int bottom = (i == N - 1) ? matrix[0][j] : matrix[i + 1][j];
                energy += matrix[i][j] * (left + right + top + bottom);
            }
        }

        return -ENERGY_SHIFT * energy / 4;
    }
}
