package Stuff;

import java.util.List;

public class Problem {

    public final List<Depot> depots;
    public final List<Customer> customers;
    public final double[][] cost;

    public Problem(List<Depot> depots, List<Customer> customers){
        this.depots = depots;
        this.customers = customers;
        this.cost = calculateCostMatrix();
    }

    private double[][] calculateCostMatrix() {
        int cSize = customers.size();
        int size = cSize + depots.size();

        double[][] cost = new double[size][size];

        for (int i = 0; i < customers.size(); i++) {
            var cPoint = customers.get(i).point;

            for (int j = 0; j < customers.size(); j++) {
                var distance = cPoint.distance(customers.get(j).point);
                cost[i][j] = distance;
                cost[j][i] = distance;
            }

            for (int j = 0; j < depots.size(); j++) {
                var distance = cPoint.distance(depots.get(j).point);
                cost[i][cSize + j] = distance;
                cost[cSize + j][i] = distance;
            }
        }

        return cost;
    }
}
