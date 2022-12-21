package me.petrolingus.lim;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.plugins.XValueIndicator;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static me.petrolingus.lim.Configuration.*;

public class Controller {

    public ImageView imageView;
    public StackPane stackPane;
    public StackPane stackPane1;
    public TextField maxStepsField;
    public TextField minTemperatureField;
    public TextField maxTemperatureField;
    public TextField temperatureSamplesField;
    private XYChart chart;
    private XYChart chart1;

    private double[] xis;
    private double[] yis;

    public void initialize() {

        imageView.setFitWidth(IMAGE_SIZE);
        imageView.setFitHeight(IMAGE_SIZE);

        chart = new XYChart(new DefaultNumericAxis("Temperature", null), new DefaultNumericAxis("Energy", null));
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        stackPane.getChildren().add(chart);

        ErrorDataSetRenderer errorRenderer = new ErrorDataSetRenderer();
        chart.getRenderers().setAll(errorRenderer);
        errorRenderer.setErrorType(ErrorStyle.NONE);
        errorRenderer.setDrawMarker(false);

        DefaultNumericAxis xAxis = new DefaultNumericAxis("Temperature", null);
        chart1 = new XYChart(xAxis, new DefaultNumericAxis("C", null));
        chart1.setAnimated(false);
        final XValueIndicator xValueIndicator = new XValueIndicator(xAxis, 2.269, "Tc");
        xValueIndicator.setEditable(false);
        chart1.getPlugins().add(xValueIndicator);
        stackPane1.getChildren().add(chart1);

        ErrorDataSetRenderer errorRenderer2 = new ErrorDataSetRenderer();
        chart1.getRenderers().setAll(errorRenderer2);
        errorRenderer2.setErrorType(ErrorStyle.NONE);
        errorRenderer2.setDrawMarker(false);
    }

    public void startButton() {

        double minTemperature = Double.parseDouble(minTemperatureField.getText());
        double maxTemperature = Double.parseDouble(maxTemperatureField.getText());
        int temperatureSamples = Integer.parseInt(temperatureSamplesField.getText());
        int maxSteps = Integer.parseInt(maxStepsField.getText());

        double h = (maxTemperature - minTemperature) / (temperatureSamples - 1);

        final DoubleDataSet dataSet = new DoubleDataSet("Energy");
        dataSet.setStyle("strokeWidth=1");
        chart.getDatasets().add(dataSet);

        xis = new double[temperatureSamples];
        yis = new double[temperatureSamples];

        Algorithm.init();

        new Thread(() -> {
            for (int i = 0; i < temperatureSamples; i++) {
                double temperature = minTemperature + h * i;
                System.out.println(temperature);
                double energy = run(temperature, maxSteps);
                Platform.runLater(() -> {
                    dataSet.add(temperature, energy);
                });
                xis[i] = temperature;
                yis[i] = energy;

                // Save Image
                String stringTemperature = String.format("%1.2f", temperature);
                File outputFile = new File("img/system-n" + N + "-t" + stringTemperature + "-norm" + maxSteps + ".png");
                BufferedImage bImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                try {
                    ImageIO.write(bImage, "png", outputFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void onInterpolate() {

        if (chart1.getDatasets().size() != 0) {
            chart1.getDatasets().clear();
        }

        SplineInterpolator splineInterpolator = new SplineInterpolator();
        PolynomialSplineFunction interpolate = splineInterpolator.interpolate(xis, yis);
        UnivariateFunction derivative = interpolate.derivative();

        final DoubleDataSet dataSet = new DoubleDataSet("δε/δT");
        dataSet.setStyle("strokeWidth=1");
        chart1.getDatasets().add(dataSet);

        double minTemperature = Double.parseDouble(minTemperatureField.getText());
        double maxTemperature = Double.parseDouble(maxTemperatureField.getText());
        int temperatureSamples = 256;

        double h = (maxTemperature - minTemperature) / (temperatureSamples - 1);

        for (int i = 0; i < temperatureSamples; i++) {
            double temperature = minTemperature + h * i;
            dataSet.add(temperature, derivative.value(temperature));
        }
    }

    private double run(double temperature, int maxSteps) {

        Algorithm algorithm = new Algorithm(temperature, maxSteps);
        algorithm.initialize();

        int[] buffer = new int[IMAGE_PIXELS];
        WritableImage writableImage = new WritableImage(IMAGE_SIZE, IMAGE_SIZE);

        while (!algorithm.isDone()) {
            algorithm.step();
            int[][] matrix = algorithm.getMatrix();
            for (int i = 0; i < IMAGE_PIXELS; i++) {
                int x = (int) Math.floor((i % IMAGE_SIZE) / PIXEL_SIZE);
                int y = (int) Math.floor((i / IMAGE_SIZE) / PIXEL_SIZE);
                if (matrix[y][x] == -1) {
                    buffer[i] = 0xFF333333;
                } else {
                    buffer[i] = 0xFFFFFFFF;
                }
            }
            Platform.runLater(() -> {
                PixelWriter pixelWriter = writableImage.getPixelWriter();
                pixelWriter.setPixels(0, 0, IMAGE_SIZE, IMAGE_SIZE, PixelFormat.getIntArgbInstance(), buffer, 0, IMAGE_SIZE);
                imageView.setImage(writableImage);
            });
        }

        return algorithm.calculateEnergy();
    }
}
