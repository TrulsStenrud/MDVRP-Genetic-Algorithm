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
