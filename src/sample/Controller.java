package sample;

import DataFiles.FileParser;
import Types.Phenotype;
import Stuff.*;
import Types.Phenotype1;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.List;

public class Controller {
    public Canvas canvas;
    public ComboBox taskChooser;
    public Button startButton;
    public LineChart chart;
    private GraphicsContext gc = null;
    private GA2 ga = null;
    private Problem problem;
    int counter = 0;
    private XYChart.Series s;

    private AnimationTimer timer;
    private Thread thread;
    private double ofsetX;
    private double ofsetY;
    private double currentScaling = 1;

    @FXML
    public void initialize() {
        var list = FileParser.getFiles();
        Arrays.sort(list);
        taskChooser.setItems(FXCollections.observableArrayList(list));

        s = new XYChart.Series();
        chart.getData().add(s);

        gc = canvas.getGraphicsContext2D();

        taskChooser.setOnAction(this::onTaskCHoosen);
        startButton.setOnAction(this::buttonClicked);

        if (list.length > 0) {
            taskChooser.setValue(list[0]);
            initiateChoosenTask();
        }


        timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                drawBoard();
            }
        };
    }

    private void onTaskCHoosen(Event event) {
        initiateChoosenTask();
    }

    private void initiateChoosenTask() {
        var task = (String) taskChooser.getValue();
        problem = FileParser.readParseFile(task);
        ga = new GA2(problem);
        calculateOffsetsAndScaling();
        drawBoard();
        counter = 0;
    }

    private void calculateOffsetsAndScaling() {
        double minX = Double.POSITIVE_INFINITY,
                minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY,
                maxY = Double.NEGATIVE_INFINITY;

        for(var c : problem.customers){
            minX = Math.min(c.point.getX(), minX);
            minY = Math.min(c.point.getY(), minY);
            maxX = Math.max(c.point.getX(), maxX);
            maxY = Math.max(c.point.getY(), maxY);
        }
        for(var c : problem.depots){
            minX = Math.min(c.point.getX(), minX);
            minY = Math.min(c.point.getY(), minY);
            maxX = Math.max(c.point.getX(), maxX);
            maxY = Math.max(c.point.getY(), maxY);
        }

        var scaling = 800.0 / Math.max(maxX - minX, maxY - minY);
        var s = scaling / currentScaling;
        gc.scale(s, s);
        currentScaling = scaling;

        ofsetX = - minX + 10;
        ofsetY = - minY + 10;
    }

    private void buttonClicked(ActionEvent actionEvent) {
        startButton.setDisable(true);
        System.out.println("Initiating...");
        var result = ga.initiate();
        System.out.println("initiating finished");
        //drawPaths(result);
        counter++;

        drawBoard();
        //drawPaths(result);


        thread = new Thread() {
            public void run() {

                int i = 0;
                double score = Double.POSITIVE_INFINITY;
                Phenotype a = null;
                while (i++ < 100) {
                    a = ga.generation();
                    int finalI = i;
                    double finalFitnes = a.fitness();
                    if (finalFitnes < score) {
                        score = a.fitness();
                        Platform.runLater(() -> s.getData().add(new XYChart.Data(finalI, finalFitnes)));
                    }

                }
                timer.stop();
                var b = a;
                Platform.runLater(() -> drawPaths(b.FML));
                System.out.println("finished: " + b.fitness());
                System.out.println(b.isFeasable());
            }
        };

        thread.start();
        timer.start();


    }

    private void drawPaths(List<List<List<Integer>>> FML) {
        gc.setStroke(Color.BLACK);
        var d = ga.problem.depots;
        var c = ga.problem.customers;

        var sum = 0.0;

        for (int depoI = 0; depoI < FML.size(); depoI++) {

            var depot = d.get(depoI).point;
            var current = FML.get(depoI);

            for (int car = 0; car < current.size(); car++) {

                var currCar = current.get(car);

                if (currCar.size() == 0)
                    continue;

                var start = c.get(currCar.get(0)).point;
                gc.strokeLine(ofsetX + depot.getX(), ofsetY + depot.getY(), ofsetX + start.getX(), ofsetY + start.getY());
                sum += new Point2D(start.getX(), start.getY()).distance(new Point2D(depot.getX(), depot.getY()));
                for (int i = 1; i < currCar.size(); i++) {
                    var pointA = c.get(currCar.get(i)).point;
                    var pointB = c.get(currCar.get(i - 1)).point;
                    gc.strokeLine(ofsetX + pointA.getX(), ofsetY + pointA.getY(), ofsetX + pointB.getX(), ofsetY + pointB.getY());
                    sum += new Point2D(pointA.getX(), pointA.getY()).distance(new Point2D(pointB.getX(), pointB.getY()));
                }

                var pointA = c.get(currCar.get(currCar.size() - 1)).point;
                var pointB = d.get(depoI).point;
                sum += new Point2D(pointA.getX(), pointA.getY()).distance(new Point2D(pointB.getX(), pointB.getY()));
                gc.strokeLine(ofsetX + pointA.getX(), ofsetY + pointA.getY(), ofsetX + pointB.getX(), ofsetY + pointB.getY());

            }


        }
        System.out.println(sum);

//        gc.setStroke(Color.RED);
//        for(var x: result.FML){
//            for(var y: x){
//                for(var z: y){
//                    var pointA = c.get(z).point;
//                    var pointB = d.get(0).point;
//                    gc.strokeLine(pointA.getX(), pointA.getY(), pointB.getX(), pointB.getY());
//                }
//            }
//        }
    }

    private void drawBoard() {
        gc.clearRect(0, 0, 1000, 1000);
        drawDots();
        drawDepos();
        //drawLines(ga.genes[0], Color.BLACK);
    }

    private void drawLines(int[] gene, Color color) {
        gc.setStroke(color);
        var d = ga.problem.depots;
        var c = ga.problem.customers;


        var depot = d.get(0).point;
        var start = c.get(gene[0]).point;
        gc.strokeLine(ofsetX + depot.getX(), ofsetY + depot.getY(), ofsetX + start.getX(), ofsetY + start.getY());

        for (int i = 1; i < gene.length; i++) {
            var pointA = c.get(gene[i]).point;
            var pointB = c.get(gene[i - 1]).point;
            gc.strokeLine(ofsetX + pointA.getX(), ofsetY + pointA.getY(), ofsetX + pointB.getX(), ofsetY + pointB.getY());
        }

        var pointA = c.get(gene[gene.length - 1]).point;
        var pointB = d.get(0).point;
        gc.strokeLine(ofsetX + pointA.getX(), ofsetY + pointA.getY(), ofsetX + pointB.getX(), ofsetY + pointB.getY());

    }

    private void drawDepos() {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(0.2);
        for (Depot d :
                ga.problem.depots) {
            gc.strokeRect(ofsetX + d.point.getX() - 0.5, ofsetY + d.point.getY() - 0.5, 1, 1);
        }
    }

    private void drawDots() {
        gc.setFill(Color.BLACK);
        for (Customer c :
                ga.problem.customers) {
            gc.fillOval(ofsetX + c.point.getX() - 0.5, ofsetY + c.point.getY() - 0.5, 1, 1);
        }
    }
}
