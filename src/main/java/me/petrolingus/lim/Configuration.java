package me.petrolingus.lim;

public interface Configuration {

    int N = 200; // 20, 50, 100, 200, 500

    double J = 1;

    int MAX_STEPS = 1000;

    int IMAGE_SIZE = 800;
    int IMAGE_PIXELS = IMAGE_SIZE * IMAGE_SIZE;
    int PIXEL_SIZE = IMAGE_SIZE / N;

}
