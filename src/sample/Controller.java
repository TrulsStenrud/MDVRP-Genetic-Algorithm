package sample;

import DataFiles.FileParser;
import Types.Phenotype;
import Stuff.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controller {
    public Color[] colors = new Color[]{
            Color.RED,
            Color.YELLOW,
            Color.BLUE,
            Color.GREEN,
            Color.AQUA,
            Color.BLACK,
            Color.INDIGO,
            Color.ROYALBLUE
    };

    public Canvas canvas;
    public ComboBox<String> taskChooser;
    public Button startButton;
    public LineChart chart;
    public ProgressBar progressBar;
    public Label scoreLabel;
    public TextField iterationsField;
    public TextField populationField;
    public TextField parentsField;
    public TextField mutationRateField;
    public TextField threadsField;
    public GridPane settings;
    private GraphicsContext gc = null;
    private Problem problem;

    private AnimationTimer timer;
    private double ofsetX;
    private double ofsetY;
    private double currentScaling = 1;

    private List<List<List<Integer>>> currentPath = null;
    public List<List<List<Integer>>> newPath = null;

    private List<XYChart.Series> series;

    private List<Thread> threads;

    int counter = 0;
    int iterations;
    private int threadCount = Runtime.getRuntime().availableProcessors();
    ;
    private int[] progress;
    private double currentBestScore;
    private Thread mainThread;
    private boolean runIndefinetly;

    public Controller() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                updateGUI();
            }
        };

    }

    private void updateGUI() {
        if (newPath != currentPath) {
            currentPath = newPath;
            scoreLabel.setText(String.valueOf(Math.round(currentBestScore)));
            drawBoard();
        }
        progressBar.setProgress(Arrays.stream(progress).average().getAsDouble() / iterations);
    }

    private void finalUpdateGui(){
        updateGUI();

        var isFeasable = new Phenotype(problem, currentPath).isFeasable();

        System.out.println(isFeasable);
    }

    private void initiateThreads() {
        int nThreads;
        var text = threadsField.getText();
        nThreads = text.isBlank() ? threadCount : Integer.parseInt(text);


        threads = new ArrayList<>(nThreads);

        progress = new int[nThreads];
        Arrays.fill(progress, 0);
        currentBestScore = Double.POSITIVE_INFINITY;
        series.forEach(x -> x.getData().clear());

        int population = Integer.parseInt(populationField.getText());
        int nParents = Integer.parseInt(parentsField.getText());
        double mutationRate = Double.parseDouble(mutationRateField.getText());

        for (int t = 0; t < nThreads; t++) {

            int finalT = t;
            var thread = new Thread(() -> {

                double score = Double.POSITIVE_INFINITY;
                var ga = new GA(problem, population, nParents, mutationRate);
                ga.initiate();
                Phenotype a = null;
                int generation = 0;
                while (progress[finalT]++ < iterations) {

                    a = ga.generation();
                    int finalI = generation++;
                    double finalFitnes = a.fitness();
                    if (finalFitnes < score) {
                        if(runIndefinetly)
                            progress[finalT] = 0;
                        score = a.fitness();
                        var finalPath = a.getPath();
                        Platform.runLater(() -> {
                            trySetBestScore(finalPath, finalFitnes);
                            series.get(finalT).getData().add(new XYChart.Data(finalI, finalFitnes));
                        });
                    }

                }

                Platform.runLater(() -> {
                    settings.setDisable(false);
                    progressBar.setProgress(0);
                    drawBoard();
                });

            });
            threads.add(thread);
        }

        mainThread = new Thread(() -> {
            for (var t : threads) {
                t.start();
            }

            for (var t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            timer.stop();
            Platform.runLater(this::finalUpdateGui);
        });
    }

    private void trySetBestScore(List<List<List<Integer>>> finalPath, double scoreCandidate) {
        if (scoreCandidate < currentBestScore) {
            newPath = finalPath;
            currentBestScore = scoreCandidate;
        }
    }

    @FXML
    public void initialize() {
        var list = FileParser.getFiles();
        Arrays.sort(list);
        taskChooser.setItems(FXCollections.observableArrayList(list));

        gc = canvas.getGraphicsContext2D();

        taskChooser.setOnAction(this::onTaskCHoosen);
        startButton.setOnAction(this::buttonClicked);

        series = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            var s = new XYChart.Series();
            chart.getData().add(s);
            series.add(s);
        }

        if (list.length > 0) {
            taskChooser.setValue(list[0]);
            initiateChoosenTask();
        }

        progressBar.setProgress(0);
        iterationsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                iterationsField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void onTaskCHoosen(Event event) {
        initiateChoosenTask();
    }

    private void initiateChoosenTask() {
        reset();
        var task = taskChooser.getValue();
        problem = FileParser.readParseFile(task);
        series.forEach(x -> x.getData().clear());
        calculateOffsetsAndScaling();
        drawBoard();
        counter = 0;
    }

    private void reset() {
        scoreLabel.setText("");

        currentPath = null;
        newPath = null;
    }

    private void calculateOffsetsAndScaling() {
        double minX = Double.POSITIVE_INFINITY,
                minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY,
                maxY = Double.NEGATIVE_INFINITY;

        for (var c : problem.customers) {
            minX = Math.min(c.point.getX(), minX);
            minY = Math.min(c.point.getY(), minY);
            maxX = Math.max(c.point.getX(), maxX);
            maxY = Math.max(c.point.getY(), maxY);
        }
        for (var d : problem.depots) {
            minX = Math.min(d.point.getX(), minX);
            minY = Math.min(d.point.getY(), minY);
            maxX = Math.max(d.point.getX(), maxX);
            maxY = Math.max(d.point.getY(), maxY);
        }

        var scaling = 800.0 / Math.max(maxX - minX, maxY - minY);
        var s = scaling / currentScaling;
        gc.scale(s, s);
        currentScaling = scaling;

        ofsetX = -minX + 10;
        ofsetY = -minY + 10;
    }

    private void buttonClicked(ActionEvent actionEvent) {
        settings.setDisable(true);

        var text = iterationsField.getText();
        if(text.isBlank()){
            iterations = 500;
            runIndefinetly = true;
        }else{
            iterations = Integer.parseInt(iterationsField.getText());
            runIndefinetly = false;
        }

        //drawPaths(result);

//        drawBoard();


        initiateThreads();

        mainThread.start();
        timer.start();
    }


    private void drawPaths(List<List<List<Integer>>> FML) {
        gc.setStroke(Color.BLACK);
        var d = problem.depots;
        var c = problem.customers;

        var sum = 0.0;
        int color = 0;
        for (int depoI = 0; depoI < FML.size(); depoI++) {

            var depot = d[depoI].point;
            var current = FML.get(depoI);

            for (int car = 0; car < current.size(); car++) {

                gc.setStroke(colors[color % colors.length]);
                color++;

                var currCar = current.get(car);

                if (currCar.size() == 0)
                    continue;

                var start = c[currCar.get(0)].point;
                gc.strokeLine(ofsetX + depot.getX(), ofsetY + depot.getY(), ofsetX + start.getX(), ofsetY + start.getY());
                sum += new Point2D(start.getX(), start.getY()).distance(new Point2D(depot.getX(), depot.getY()));
                for (int i = 1; i < currCar.size(); i++) {
                    var pointA = c[currCar.get(i)].point;
                    var pointB = c[currCar.get(i - 1)].point;
                    gc.strokeLine(ofsetX + pointA.getX(), ofsetY + pointA.getY(), ofsetX + pointB.getX(), ofsetY + pointB.getY());
                    sum += new Point2D(pointA.getX(), pointA.getY()).distance(new Point2D(pointB.getX(), pointB.getY()));
                }

                var pointA = c[currCar.get(currCar.size() - 1)].point;
                var pointB = d[depoI].point;
                sum += new Point2D(pointA.getX(), pointA.getY()).distance(new Point2D(pointB.getX(), pointB.getY()));
                gc.strokeLine(ofsetX + pointA.getX(), ofsetY + pointA.getY(), ofsetX + pointB.getX(), ofsetY + pointB.getY());

            }


        }
    }

    private void drawBoard() {
        gc.clearRect(0, 0, 1000, 1000);
        if (currentPath != null) {
            drawPaths(currentPath);
        }
        drawDots();
        drawDepos();
    }

    private void drawDepos() {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(0.2);
        for (Depot d :
                problem.depots) {
            gc.strokeRect(ofsetX + d.point.getX() - 0.5, ofsetY + d.point.getY() - 0.5, 1, 1);
        }
    }

    private void drawDots() {
        gc.setFill(Color.BLACK);
        for (Customer c :
                problem.customers) {
            gc.fillOval(ofsetX + c.point.getX() - 0.5, ofsetY + c.point.getY() - 0.5, 1, 1);
        }
    }
}
