package Types;

import Stuff.Depot;
import javafx.geometry.Point2D;

import java.util.List;

public class MaybeCustomer{

    public final Point2D point;
    public final int index;
    public final List<Depot> uncertainty; // is null if not uncertain which depot should belong to

    public MaybeCustomer(Point2D point, int index, List<Depot> uncertainty){
        this.point = point;
        this.index = index;
        this.uncertainty = uncertainty;
    }
}
