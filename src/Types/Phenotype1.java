package Types;

import Stuff.Customer;
import Stuff.Depot;
import Stuff.Problem;

import java.util.*;

public class Phenotype1 {

    public int[] genome;
    private final Problem problem;
    private final Customer[] customers;
    private final Depot[] depots;
    private double[][] cost;
    private int customerCount;
    //HashMap<List<Integer>, Double> routeCost;
    //HashMap<List<Integer>, Double> routeLoad;
    public List<List<List<Integer>>> FML;
    private double fitness = -1;

    public Phenotype1(int[] genome, double[][] cost, Problem problem) {
        this.genome = genome;
        this.cost = cost;
        this.customerCount = problem.customers.size();
        this.problem = problem;
        this.customers = problem.customers.toArray(Customer[]::new);
        this.depots = problem.depots.toArray(Depot[]::new);

        convert();
    }

    private void convert() {

        List<List<List<Integer>>> fml = phase1();

        int sum = getSum(fml);

        if (sum != problem.customers.size())
            System.out.println("Is: " + sum + ". Should be: " + problem.customers.size() + " in phenotype");

        phase2(fml);
        this.FML = fml;
        //this.routeCost = routeCost;
        //this.routeLoad = routeLoad;
    }

    private void phase2(List<List<List<Integer>>> fml) {
        for (int i = 0; i < fml.size(); i++) {

            var currDepList = fml.get(i);

            for (int j = currDepList.size() - 1; j > 0; j--) {

                var currVehicle = currDepList.get(j);
                var prevVehicle = currDepList.get(j - 1);

                if (currVehicle.size() == 0)
                    continue;

                var currCost = getRouteCost(currVehicle, i);
                var prevCost = getRouteCost(prevVehicle, i - 1);

                var currLoad = getRouteLoad(currVehicle);
                var prevLoad = getRouteLoad(prevVehicle);

                int cToBeMoved = prevVehicle.get(prevVehicle.size() - 1);

                double demand = customers[cToBeMoved].demand;

                double newLoad = currLoad + demand;

                if (newLoad > depots[i].maxLoad)
                    continue;


                var newCost = currCost
                        - cost[i + customerCount][currVehicle.get(0)]
                        + cost[cToBeMoved][currVehicle.get(0)] + cost[i + customerCount][cToBeMoved];

                if (newCost > depots[i].maxRoute)
                    continue;

                double newPrevCost = valueMinusLast(i, prevVehicle, prevCost);

                if (newCost + newPrevCost >= currCost + prevCost)
                    continue;

                prevVehicle.remove(prevVehicle.size() - 1);
                currVehicle.add(0, cToBeMoved);

            }
        }
    }

    private double valueMinusLast(int depIndex, List<Integer> route, double originalCost) {
        if (route.size() == 1)
            return 0;

        int lastIndex = route.size() - 1;

        originalCost -= cost[depIndex][lastIndex];
        originalCost -= cost[lastIndex][lastIndex - 1];
        originalCost += cost[lastIndex - 1][depIndex];

        return originalCost;
    }

    private List<List<List<Integer>>> phase1() {
        List<List<List<Integer>>> fml = new ArrayList<>();

        for (int i = 0; i < depots.length; i++) {
            fml.add(new ArrayList<>());
        }

        int d = findClosestDepo(genome[0]);
        var curDep = fml.get(d);
        var currRoute = new ArrayList<Integer>();
        double currCost = cost[customerCount + d][genome[0]];
        double currLoad = customers[genome[0]].demand;

        int maxLoad = depots[d].maxLoad;
        int maxDuration = depots[d].maxRoute;
        if (maxDuration == 0)
            maxDuration = Integer.MAX_VALUE;

        int prevCustomer = genome[0];
        currRoute.add(prevCustomer);
        for (int i = 1; i < genome.length; i++) {
            int currCustomer = genome[i];
            var stepCost = cost[prevCustomer][currCustomer];

            var load = customers[currCustomer].demand;

            if (currLoad + load > maxLoad
                    || (currCost + stepCost + cost[currCustomer][d] > maxDuration)) {

                curDep.add(currRoute);
                currRoute = new ArrayList<>();
                currLoad = 0;
                currCost = 0;
                d = findClosestDepo(currCustomer);
                curDep = fml.get(d);
                stepCost = cost[d][currCustomer];
            }

            currLoad += load;
            currCost += stepCost;
            currRoute.add(currCustomer);
        }

        curDep.add(currRoute);

        return fml;
    }

    private int findClosestDepo(int customer) {
        double min = Double.POSITIVE_INFINITY;
        int minI = -1;

        for (int i = 0; i < depots.length; i++) {
            var currCost = cost[customer][customerCount + i];

            if (currCost < min) {
                min = currCost;
                minI = i;
            }
        }
        return minI;
    }


    public double fitness() {
        if (fitness == -1) {
            double sum = 0;
            //for (double value : routeCost.values()) {
            //  sum += value;
            //}
            for (int i = 0; i < FML.size(); i++) {
                var current = FML.get(i);
                for (int j = 0; j < current.size(); j++) {
                    sum += getRouteCost(current.get(j), i);
                }
            }
            fitness = sum;
        }

        return fitness;
    }

    private int getSum(List<List<List<Integer>>> fml) {
        var sum = 0;
        for (var a : fml) {
            for (var c : a) {
                sum += c.size();
            }
        }
        return sum;
    }

    private double getRouteLoad(List<Integer> currRoute) {
        double sum = 0.0;
        for (var c : currRoute) {
            sum += customers[c].demand;
        }
        return sum;
    }

    private double getRouteCost(List<Integer> currRoute, int depot) {
        double c = 0.0;

        if (currRoute.size() == 0)
            return 0;

        c += cost[depot + customerCount][currRoute.get(0)];

        for (int i = 1; i < currRoute.size() - 2; i++) {
            c += cost[currRoute.get(i - 1)][currRoute.get(i)];
        }

        c += cost[depot + customerCount][currRoute.get(currRoute.size() - 1)];
        return c;
    }


}
