package me.petrolingus.lim;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

import static me.petrolingus.lim.Configuration.*;

public class Algorithm {

    private double time = 0;

    private int[][] matrix = new int[N][N];

    private double energy;

    public void initialize() {

        matrix = new int[N][N];

        // Generate spins
        generate();

        energy = calculateEnergy();

        // Check generated spins
        testCorrectSpinGeneration();

        System.out.println("Initialize done");
        System.out.println("Init energy is equal to " + energy);
    }

    public void generate() {
        matrix = new int[N][N];

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
    }

    public void step() {
        for (int i = 0; i < N * N; i++) {
            int x0 = ThreadLocalRandom.current().nextInt(N);
            int y0 = ThreadLocalRandom.current().nextInt(N);
            int x1 = ThreadLocalRandom.current().nextInt(N);
            int y1 = ThreadLocalRandom.current().nextInt(N);
            int s0 = matrix[y0][x0];
            int s1 = matrix[y1][x1];
            if (s0 != s1) {
                double delta0 = deltaEnergy(y0, x0);
                matrix[y0][x0] = s1;
                double delta1 = deltaEnergy(y1, x1);
                matrix[y1][x1] = s0;
                double delta = 2 * (delta0 + delta1);
                double gibbs = Math.exp(-delta / 0.5);
                if (delta <= 0 || Math.random() < gibbs) {

                } else {
                    matrix[y0][x0] = s0;
                    matrix[y1][x1] = s1;
                }
            }
            time += 1.0 / (N * N);
        }
    }

    public double getTime() {
        return time;
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

    public int[][] getMatrix() {
        return matrix;
    }

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

        return -J * energy;
    }

    public double deltaEnergy(int i, int j) {
        int left = (j == 0) ? matrix[i][N - 1] : matrix[i][j - 1];
        int right = (j == N - 1) ? matrix[i][0] : matrix[i][j + 1];
        int top = (i == 0) ? matrix[N - 1][j] : matrix[i - 1][j];
        int bottom = (i == N - 1) ? matrix[0][j] : matrix[i + 1][j];
        return J * matrix[i][j] * (left + right + top + bottom);
    }

    public void test() {

        System.out.println("Start test");

        for (int i = 0; i < 100_000; i++) {

            generate();

            double energy0 = calculateEnergy();

            int x0;
            int y0;
            int x1;
            int y1;
            int s0;
            int s1;

            while (true) {
                x0 = ThreadLocalRandom.current().nextInt(N);
                y0 = ThreadLocalRandom.current().nextInt(N);
                x1 = ThreadLocalRandom.current().nextInt(N);
                y1 = ThreadLocalRandom.current().nextInt(N);
                s0 = matrix[y0][x0];
                s1 = matrix[y1][x1];
                if (s0 != s1) {
                    break;
                }
            }

            matrix[y0][x0] = s1;
            matrix[y1][x1] = s0;
            double energy1 = calculateEnergy();
            double diff0 = energy1 - energy0;

            // Restore state
            matrix[y0][x0] = s0;
            matrix[y1][x1] = s1;

            double delta0 = deltaEnergy(y0, x0);
            matrix[y0][x0] = s1;
            double delta1 = deltaEnergy(y1, x1);
            matrix[y1][x1] = s0;
            double delta = 2 * (delta0 + delta1);

            if (delta != diff0) {
                System.out.println("Test #" + i + " failed!");
                System.out.println(new Point(x0, y0) + " " + new Point(x1, y1));
                System.out.println("Diff0 = " + diff0);
                System.out.println("delta = " + delta);
            }
        }
    }
}
