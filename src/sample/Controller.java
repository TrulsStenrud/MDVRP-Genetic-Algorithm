package sample;

import DataFiles.FileParser;
import Stuff.Customer;
import Stuff.Depot;
import Stuff.Problem;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;

import java.awt.*;

public class Controller {
    public Canvas canvas;
    public ComboBox taskChooser;
    private GraphicsContext gc = null;
    private Problem problem = null;

    @FXML
    public void initialize(){
        problem = FileParser.readParseFile("p20");
        gc = canvas.getGraphicsContext2D();
        gc.scale(6, 6);
        drawDots();
        drawDepos();

        var genes = problem.initiate();

        int c = 0;
        for(int i = 0; i < 1; i++){
            var color = Color.rgb(c, c, c);
            drawLines(genes[i], color);
            c+=20;
        }
    }

    private void drawLines(int[] gene, Color color) {
        gc.setStroke(color);

        var depot = problem.depots.get(0).point;
        var start = problem.customers.get(gene[0]).point;
        gc.strokeLine(depot.getX(), depot.getY(), start.getX(), start.getY());

        for(int i = 1; i < gene.length; i++){
            var pointA = problem.customers.get(gene[i]).point;
            var pointB = problem.customers.get(gene[i-1]).point;
            gc.strokeLine(pointA.getX(), pointA.getY(), pointB.getX(), pointB.getY());
        }

        var pointA = problem.customers.get(gene[gene.length-1]).point;
        var pointB = problem.depots.get(0).point;
        gc.strokeLine(pointA.getX(), pointA.getY(), pointB.getX(), pointB.getY());

    }

    private void drawDepos() {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(0.2);
        for (Depot d :
                problem.depots) {
            gc.strokeRect(d.point.getX(), d.point.getY(), 1, 1);
        }
    }

    private void drawDots() {
        gc.setFill(Color.BLACK);
        for (Customer c :
                problem.customers) {
            gc.fillOval(c.point.getX(), c.point.getY(), 1, 1);
        }
    }
}
