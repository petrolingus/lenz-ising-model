package me.petrolingus.lim;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static me.petrolingus.lim.Configuration.*;

public class Algorithm {

    private static final int[][] initialConfiguration = new int[N][N];
    private final int[][] matrix = new int[N][N];
    private int time;
    private double energy;
    private boolean isDone = false;

    private final double temperature;
    private final int maxSteps;

    public Algorithm(double temperature, int maxSteps) {
        this.temperature = temperature;
        this.maxSteps = maxSteps;
    }

    public static void init() {
        // Generate spins
        for (int i = 0; i < N * N / 2; i++) {
            while (true) {
                int x = ThreadLocalRandom.current().nextInt(N);
                int y = ThreadLocalRandom.current().nextInt(N);
                if (initialConfiguration[y][x] == 0) {
                    initialConfiguration[y][x] = 1;
                    break;
                }
            }
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (initialConfiguration[i][j] == 0) {
                    initialConfiguration[i][j] = -1;
                }
            }
        }
    }

    public void initialize() {

//        // Generate spins
//        for (int i = 0; i < N * N / 2; i++) {
//            while (true) {
//                int x = ThreadLocalRandom.current().nextInt(N);
//                int y = ThreadLocalRandom.current().nextInt(N);
//                if (matrix[y][x] == 0) {
//                    matrix[y][x] = 1;
//                    break;
//                }
//            }
//        }
//        for (int i = 0; i < N; i++) {
//            for (int j = 0; j < N; j++) {
//                if (matrix[i][j] == 0) {
//                    matrix[i][j] = -1;
//                }
//            }
//        }

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                matrix[i][j] = initialConfiguration[i][j];
            }
        }

        time = 0;
        // TODO: 0 or calculateEnergy();
        energy = 0;

        // Check generated spins
//        testCorrectSpinGeneration();

//        System.out.println("Initialize done");
//        System.out.println("Init energy is equal to " + energy);
    }

    public void step() {
        if (isDone) {
            return;
        }
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
                double gibbs = Math.exp(-delta / temperature);
                if (delta <= 0 || ThreadLocalRandom.current().nextDouble() < gibbs) {
                    energy += delta / (N * N);
                } else {
                    matrix[y0][x0] = s0;
                    matrix[y1][x1] = s1;
                }
            }
        }
        time++;
        if (time == maxSteps) {
            isDone = true;
        }
    }

    public int[][] getMatrix() {
        return matrix;
    }

    public int getTime() {
        return time;
    }

    @SuppressWarnings("unused")
    public double getEnergy() {
        return energy;
    }

    public boolean isDone() {
        return isDone;
    }

    public double calculateEnergy() {
        double energy = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int left = (j == 0) ? matrix[i][N - 1] : matrix[i][j - 1];
                int top = (i == 0) ? matrix[N - 1][j] : matrix[i - 1][j];
                energy += matrix[i][j] * (left + top);
            }
        }
        return -J * energy / (N * N);
    }

    private double deltaEnergy(int i, int j) {
        int left = (j == 0) ? matrix[i][N - 1] : matrix[i][j - 1];
        int right = (j == N - 1) ? matrix[i][0] : matrix[i][j + 1];
        int top = (i == 0) ? matrix[N - 1][j] : matrix[i - 1][j];
        int bottom = (i == N - 1) ? matrix[0][j] : matrix[i + 1][j];
        return J * matrix[i][j] * (left + right + top + bottom);
    }

    private void testCorrectSpinGeneration() {

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
}
