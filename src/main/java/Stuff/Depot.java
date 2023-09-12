package Stuff;

import javafx.geometry.Point2D;

public class Depot {
    public final int maxCarCount;
    public final int maxRoute;
    public final int maxLoad;
    public Point2D point = Point2D.ZERO;

    public Depot(int maxCarCount, int maxRoute, int maxLoad){
        this.maxCarCount = maxCarCount;
        this.maxRoute = maxRoute;
        this.maxLoad = maxLoad;
    }

}
