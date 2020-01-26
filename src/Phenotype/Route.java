package Phenotype;

import javafx.geometry.Point2D;

import java.util.ArrayList;

public class Route {

    private final ArrayList<Integer> route;
    private double[][] cost;


    public Route(int depot, double[][] cost){
        this.cost = cost;
        route = new ArrayList<Integer>();
    }


    public void add(int customer){
        route.add(customer);
    }


}
