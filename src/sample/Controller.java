package sample;

import DataFiles.FileParser;
import Stuff.Customer;
import Stuff.Depot;
import Stuff.GA;
import Stuff.Problem;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;

import java.awt.*;
import java.util.Arrays;

public class Controller {
    public Canvas canvas;
    public ComboBox taskChooser;
    public Button startButton;
    private GraphicsContext gc = null;
    private GA ga = null;
    private AnimationTimer timer;

    @FXML
    public void initialize(){
        var list = FileParser.getFiles();
        Arrays.sort(list);
        taskChooser.setItems(FXCollections.observableArrayList(list));

        gc = canvas.getGraphicsContext2D();
        gc.scale(6, 6);

        taskChooser.setOnAction(this::onTaskCHoosen);
        startButton.setOnAction(this::buttonClicked);

        if(list.length > 0)
        {
            taskChooser.setValue(list[0]);
            initiateChoosenTask();
        }


        timer = new AnimationTimer(){
            @Override
            public void handle(long l) {
                drawPath();
            }
        };
    }

    private void onTaskCHoosen(Event event) {
        initiateChoosenTask();
    }

    private void initiateChoosenTask() {
        var task = (String)taskChooser.getValue();
        ga  = new GA(FileParser.readParseFile(task));
        ga.initiate();

        drawPath();
    }

    private void buttonClicked(ActionEvent actionEvent) {


        Thread thread = new Thread(){
            public void run(){

                int i = 0;
                while(i++ < 500){
                    ga.generation();
                }
                timer.stop();
            }
        };

        thread.start();
        timer.start();
    }

    private void drawPath(){
        gc.clearRect(0, 0, 1000, 1000);
        drawDots();
        drawDepos();
        drawLines(ga.genes[0], Color.BLACK);
    }

    private void drawLines(int[] gene, Color color) {
        gc.setStroke(color);
        var d = ga.problem.depots;
        var c = ga.problem.customers;


        var depot = d.get(0).point;
        var start = c.get(gene[0]).point;
        gc.strokeLine(depot.getX(), depot.getY(), start.getX(), start.getY());

        for(int i = 1; i < gene.length; i++){
            var pointA = c.get(gene[i]).point;
            var pointB = c.get(gene[i-1]).point;
            gc.strokeLine(pointA.getX(), pointA.getY(), pointB.getX(), pointB.getY());
        }

        var pointA = c.get(gene[gene.length-1]).point;
        var pointB = d.get(0).point;
        gc.strokeLine(pointA.getX(), pointA.getY(), pointB.getX(), pointB.getY());

    }

    private void drawDepos() {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(0.2);
        for (Depot d :
                ga.problem.depots) {
            gc.strokeRect(d.point.getX()-0.5, d.point.getY()-0.5, 1, 1);
        }
    }

    private void drawDots() {
        gc.setFill(Color.BLACK);
        for (Customer c :
                ga.problem.customers) {
            gc.fillOval(c.point.getX() -0.5, c.point.getY()-0.5, 1, 1);
        }
    }
}
