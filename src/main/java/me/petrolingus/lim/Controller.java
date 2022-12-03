package me.petrolingus.lim;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.DoubleDataSet;
import de.gsi.math.DataSetMath;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static me.petrolingus.lim.Configuration.*;

public class Controller {

    public ImageView imageView;
    public StackPane stackPane;
    public StackPane stackPane1;
    public TextField maxStepsField;
    public TextField temperatureField;
    private XYChart chart;
    private XYChart chart1;

    public void initialize() {

        imageView.setFitWidth(IMAGE_SIZE);
        imageView.setFitHeight(IMAGE_SIZE);

        chart = new XYChart(new DefaultNumericAxis(), new DefaultNumericAxis());
        chart.setAnimated(false);
        stackPane.getChildren().add(chart);

        ErrorDataSetRenderer errorRenderer = new ErrorDataSetRenderer();
        chart.getRenderers().setAll(errorRenderer);
        errorRenderer.setErrorType(ErrorStyle.NONE);
        errorRenderer.setDrawMarker(false);

        chart1 = new XYChart(new DefaultNumericAxis(), new DefaultNumericAxis());
        chart1.setAnimated(false);
        stackPane1.getChildren().add(chart1);
    }

    public void startButton() {
        double temperature = Double.parseDouble(temperatureField.getText());
        run(temperature);
    }

    private void run(double temperature) {

        Algorithm algorithm = new Algorithm(temperature);
        algorithm.initialize();

        int[] buffer = new int[IMAGE_PIXELS];
        WritableImage writableImage = new WritableImage(IMAGE_SIZE, IMAGE_SIZE);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        String stringTemperature = String.format("%8.2f", temperature);
        final DoubleDataSet dataSet = new DoubleDataSet("Energy" + stringTemperature);
        dataSet.setStyle("strokeWidth=1");
        chart.getDatasets().add(dataSet);

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
        executorService.scheduleWithFixedDelay(() -> {
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
            pixelWriter.setPixels(0, 0, IMAGE_SIZE, IMAGE_SIZE, PixelFormat.getIntArgbInstance(), buffer, 0, IMAGE_SIZE);
            imageView.setImage(writableImage);
        }, 300, 1, TimeUnit.MILLISECONDS);

        executorService.scheduleAtFixedRate(() -> {
            Double poll = algorithm.energyList.poll();
            if (poll != null) {
                dataSet.add(algorithm.getTime(), poll);
            }
            if (algorithm.isDone()) {
                DataSet derivative = DataSetMath.derivativeFunction(dataSet);
                derivative.setStyle("strokeColor=darkgreen;fillColor=darkgreen;strokeWidth=1");
                Platform.runLater(() -> {
                    chart1.getDatasets().addAll(derivative);
                });
                executorService.shutdown();
            }
        }, 300, 1, TimeUnit.MILLISECONDS);
    }
}
