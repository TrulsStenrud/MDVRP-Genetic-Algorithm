package Stuff;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Problem {

    public final Depot[] depots;
    public final Customer[] customers;
    public final double[][] cost;
    public final List<List<Integer>> closestDepots;
    private double bound = 2;

    public Problem(List<Depot> depots, List<Customer> customers){
        this.depots = depots.toArray(Depot[]::new);
        this.customers = customers.toArray(Customer[]::new);
        this.cost = calculateCostMatrix();
        this.closestDepots = calculateClosestDepos();
    }

    private List<List<Integer>> calculateClosestDepos() {
        var result = new ArrayList<List<Integer>>();
        var max = depots[0].maxRoute == 0 ? Double.POSITIVE_INFINITY: depots[0].maxRoute;
        var t = new int[depots.length];
        for(int i = 0 ; i < t.length; i++){
            t[i] = i;
        }

        for(int c = 0; c < customers.length; c++){
            double min = Double.POSITIVE_INFINITY;
            int minIndex = 0;

            for(var d : t){
                var distance = cost[c][customers.length + d];
                if(distance < min){
                    min = distance;
                    minIndex = d;
                }
            }

            var current = new ArrayList<Integer>();
            current.add(minIndex);

            for(var d : t){
                if (d == minIndex)
                    continue;

                if(cost[c][d + customers.length] < max/2 && (cost[c][d + customers.length] - min)/ min <= bound){
                    current.add(d);
                }
            }

            int finalC = c;
            current.sort(Comparator.comparingDouble(x -> cost[finalC][x + customers.length]));
            result.add(current);
        }

        return result;
    }

    private double[][] calculateCostMatrix() {
        int cSize = customers.length;
        int size = cSize + depots.length;

        double[][] cost = new double[size][size];

        for (int i = 0; i < customers.length; i++) {
            var cPoint = customers[i].point;

            for (int j = 0; j < customers.length; j++) {
                var distance = cPoint.distance(customers[j].point);
                cost[i][j] = distance;
                cost[j][i] = distance;
            }

            for (int j = 0; j < depots.length; j++) {
                var distance = cPoint.distance(depots[j].point);
                cost[i][cSize + j] = distance;
                cost[cSize + j][i] = distance;
            }
        }

        return cost;
    }
}
