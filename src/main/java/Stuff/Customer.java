package Stuff;

import javafx.geometry.Point2D;

public class Customer {

    public final int duration;
    public final int demand;
    public final Point2D point;

    public Customer(Point2D point, int duration, int demand){
        this.point = point;
        this.duration = duration;
        this.demand = demand;
    }
}
