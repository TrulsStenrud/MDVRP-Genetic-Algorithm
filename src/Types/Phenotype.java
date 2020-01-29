package Types;

import Stuff.Customer;
import Stuff.Depot;
import Stuff.Problem;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Phenotype {
    private final Problem problem;
    private final Customer[] customers;
    private final Depot[] depots;
    private double[][] cost;
    private int customerCount;
    //HashMap<List<Integer>, Double> routeCost;
    //HashMap<List<Integer>, Double> routeLoad;
    public List<List<List<Integer>>> FML;
    private double fitness = -1;

    public Phenotype(List<List<Integer>> initRoute, Problem problem) {
        this.cost = problem.cost;
        this.customerCount = problem.customers.length;
        this.problem = problem;
        this.customers = problem.customers;
        this.depots = problem.depots;

        initialize(initRoute);
    }

    public Phenotype(Problem problem, List<List<List<Integer>>> initFML) {
        this.cost = problem.cost;
        this.customerCount = problem.customers.length;
        this.problem = problem;
        this.customers = problem.customers;
        this.depots = problem.depots;

        this.FML = initFML;
    }

    private void initialize(List<List<Integer>> initRoute) {
        var routeCost = new HashMap<List<Integer>, Double>();
        var routeLoad = new HashMap<List<Integer>, Double>();

        List<List<List<Integer>>> fml = phase1(initRoute, routeCost, routeLoad);
        int sum = getSum(fml);

        if (sum != problem.customers.length)
            System.out.println("Is: " + sum + ". Should be: " + problem.customers.length + " in phenotype");

        phase2(fml, routeCost, routeLoad);
        this.FML = fml;
    }

    private void phase2(List<List<List<Integer>>> fml, HashMap<List<Integer>, Double> routeCost, HashMap<List<Integer>, Double> routeLoad) {
        for (int i = 0; i < fml.size(); i++) {

            var currDepList = fml.get(i);

            for (int j = currDepList.size() - 1; j > 0; j--) {

                var currVehicle = currDepList.get(j);
                var prevVehicle = currDepList.get(j - 1);

                if (currVehicle.size() == 0)
                    continue;

                var currCost = routeCost.getOrDefault(currVehicle, 0.0);
                var prevCost = routeCost.getOrDefault(prevVehicle, 0.0);

                var currLoad = routeLoad.getOrDefault(currVehicle, 0.0);
                var prevLoad = routeLoad.getOrDefault(prevVehicle, 0.0);

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

                routeCost.put(currVehicle, newCost);
                routeCost.put(prevVehicle, newPrevCost);
                routeLoad.put(currVehicle, newLoad);
                routeLoad.put(prevVehicle, prevLoad - customers[cToBeMoved].demand);
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

    private List<List<List<Integer>>> phase1(List<List<Integer>> initRoute, HashMap<List<Integer>, Double> routeCost, HashMap<List<Integer>, Double> routeLoad) {
        List<List<List<Integer>>> fml = new ArrayList<>();

        for (int i = 0; i < initRoute.size(); i++) {

            var chromosome = initRoute.get(i);

            int depIndex = customerCount + i;

            int maxLoad = depots[i].maxLoad;
            int maxDuration = depots[i].maxRoute;

            if (maxDuration == 0)
                maxDuration = Integer.MAX_VALUE;

            if (maxLoad == 0)
                maxLoad = Integer.MAX_VALUE;


            List<List<Integer>> routes = new ArrayList<>();
            List<Integer> currRoute = new ArrayList<>();

            double currDur = 0, currLoad = 0;

            int currCustomer = chromosome.get(0);
            currRoute.add(currCustomer);
            currDur += cost[depIndex][currCustomer];
            currLoad += customers[currCustomer].demand;
            int prevCustomer = currCustomer;

            for (int j = 1; j < chromosome.size(); j++) {
                currCustomer = chromosome.get(j);
                var stepCost = cost[prevCustomer][currCustomer];
                var load = customers[currCustomer].demand;

                if (currLoad + load > maxLoad
                        || (currDur + stepCost + cost[currCustomer][depIndex] > maxDuration)) {

                    routes.add(currRoute);
                    routeCost.put(currRoute, currDur + cost[depIndex][currCustomer]);
                    routeLoad.put(currRoute, currLoad);

                    currRoute = new ArrayList<>();
                    currLoad = 0;
                    currDur = 0;
                    stepCost = cost[depIndex][currCustomer];
                }

                currLoad += load;
                currDur += stepCost;
                currRoute.add(currCustomer);

            }
            routeLoad.put(currRoute, currLoad);
            routeCost.put(currRoute, currDur);
            routes.add(currRoute);
            fml.add(routes);
        }
        return fml;
    }


    public double fitness() {
        // if (fitness == -1) {
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
        //}

        return fitness;
    }

    public void Reproduce(Phenotype b) {

        if (b.FML.size() != this.FML.size())
            System.out.println("Should not be merged");

        int depot = (int) (Math.random() * FML.size());

        var depotA = FML.get(depot);
        var depotB = b.FML.get(depot);

        int routeA = (int) (Math.random() * depotA.size());
        int routeB = (int) (Math.random() * depotB.size());

        var fromA = (depotA.size() == 0) ? new ArrayList<Integer>() : depotA.get(routeA);
        var fromB = depotB.size() == 0 ? new ArrayList<Integer>() : depotB.get(routeB);

        var routeFromA = new ArrayList<Integer>();
        routeFromA.addAll(fromA);
        var routeFromB = new ArrayList<Integer>();
        routeFromB.addAll(fromB);

        for (var depo : FML) {
            for (var route : depo) {
                route.removeAll(routeFromB);
            }
        }
        for (var a : FML) {
            a.removeIf(List::isEmpty);
        }

        var sum = getSum(b.FML);

        for (var depo : b.FML) {
            for (var route : depo) {
                route.removeAll(routeFromA);
            }
        }
        for (var a : b.FML) {
            a.removeIf(List::isEmpty);
        }
        var sum2 = getSum(b.FML);

        if (sum2 != sum - routeFromA.size()) {
            int stop = 0;
        }

        for (int newC : routeFromB) {

            insertCheapest(depot, newC);

        }

        for (int newC : routeFromA) {
            b.insertCheapest(depot, newC);
        }
        var sum3 = getSum(b.FML);

        fitness = -1;
        b.fitness = -1;

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


    public void insertCheapest(int depot, int newC) {

        var routes = FML.get(depot);
        int minRoute = -1;
        int minI = -1;
        double minCost = Double.POSITIVE_INFINITY;
        var maxDemand = depots[depot].maxRoute;
        if (maxDemand == 0)
            maxDemand = Integer.MAX_VALUE;

        for (int i = 0; i < routes.size(); i++) {
            var currRoute = routes.get(i);
            double currCost = getRouteCost(currRoute, depot);
            double routeLoad = getRouteLoad(currRoute);
            if ((routeLoad + customers[newC].demand) > depots[depot].maxLoad) {
                continue;
            }

            for (int index = 0; index < currRoute.size(); index++) {
                var costOfInsertion = insertionCost(depot, index, newC, currRoute);

                if (currCost + costOfInsertion <= maxDemand) {
                    if (costOfInsertion < minCost) {
                        minRoute = i;
                        minCost = costOfInsertion;
                        minI = index;
                    }
                }
            }
        }

        if (minRoute == -1) {
            var newRoute = new ArrayList<Integer>();
            newRoute.add(newC);
            routes.add(newRoute);
            var a = getRouteCost(newRoute, depot);
            if (a > maxDemand) {
                int stop = 2;
            }

        } else {
            var route = routes.get(minRoute);
            route.add(minI, newC);
            if (getRouteCost(route, depot) > maxDemand) {
                int a = 2;
            }
        }

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

        for (int i = 1; i < currRoute.size(); i++) {
            c += cost[currRoute.get(i - 1)][currRoute.get(i)];
        }

        c += cost[depot + customerCount][currRoute.get(currRoute.size() - 1)];
        return c;
    }

    private double insertionCost(int depot, int i, int newC, List<Integer> vehicle) {
        double resultCost = 0.0;
        if (i == 0) {
            resultCost -= cost[depot + customerCount][vehicle.get(0)];
            resultCost += cost[newC][vehicle.get(0)];
            resultCost += cost[depot + customerCount][newC];
            return resultCost;
        }
        if (i == vehicle.size()) {
            resultCost -= cost[depot + customerCount][vehicle.get(vehicle.size() - 1)];
            resultCost += cost[newC][vehicle.get(vehicle.size() - 1)];
            resultCost += cost[depot + customerCount][newC];
            return resultCost;
        }

        resultCost -= cost[vehicle.get(i)][vehicle.get(i - 1)];
        resultCost += cost[newC][vehicle.get(i - 1)];
        resultCost += cost[newC][vehicle.get(i)];

        return resultCost;
    }

    public Phenotype copy() {

        List<List<List<Integer>>> newFML = getPath();

        return new Phenotype(problem, newFML);
    }

    public List<List<List<Integer>>> getPath() {
        List<List<List<Integer>>> newFML = new ArrayList<>(FML.size());
        for (var a : FML) {
            var x = new ArrayList<List<Integer>>();
            newFML.add(x);
            for (var b : a) {
                var y = new ArrayList<Integer>();
                x.add(y);
                y.addAll(b);
            }
        }
        return newFML;
    }

    public void makeFeseable() {

    }

    public boolean correctCarCount() {
        for (int i = 0; i < FML.size(); i++) {
            var max = depots[i].maxCarCount;
           if(FML.get(i).size() > max)
               return false;
        }

        return true;
    }

    public boolean isFeasable() {
        for (int i = 0; i < FML.size(); i++) {
            var max = depots[i].maxCarCount;
            var maxCost = depots[i].maxRoute == 0 ? Double.POSITIVE_INFINITY : depots[i].maxRoute;
            var maxLoad = depots[i].maxLoad == 0 ? Double.POSITIVE_INFINITY : depots[i].maxLoad;

            if (FML.get(i).size() > max) {
                return false;
            }

            for (var v : FML.get(i)) {
                var cost = 0;
                var load = 0;

                var curr = v.get(0);
                cost += this.cost[curr][i + customers.length];
                load += customers[curr].demand;
                var prev = curr;
                for (int c = 1; c < v.size(); c++) {
                    curr = v.get(c);
                    load += customers[curr].demand;
                    cost += this.cost[curr][prev];
                    prev = curr;
                }
                cost += this.cost[prev][i + customers.length];

                if (cost > maxCost || load > maxLoad) {
                    if (cost > maxCost) {
                        System.out.println("Too much cost");
                        //var a = getRouteCost(v, i);
                        //int b = 2;
                    }
                    if (load > maxLoad) {
                        System.out.println(load + " should be " + maxLoad);
                        System.out.println("Too much load");
                    }
                    return false;
                }
            }

        }

        return true;
    }
}
