package Phenotype;

import Stuff.Customer;
import Stuff.Depot;
import Stuff.Genome;
import Stuff.Problem;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Phenotype {

    public Genome genome;
    private final Problem problem;
    private final Customer[] customers;
    private final Depot[] depots;
    private double[][] cost;
    private int customerCount;
    //HashMap<List<Integer>, Double> routeCost;
    //HashMap<List<Integer>, Double> routeLoad;
    public List<List<List<Integer>>> FML;
    private double fitness = -1;

    public Phenotype(Genome genome, double[][] cost, Problem problem) {
        this.genome = genome;
        this.cost = cost;
        this.customerCount = problem.customers.size();
        this.problem = problem;
        this.customers = problem.customers.toArray(Stuff.Customer[]::new);
        this.depots = problem.depots.toArray(Stuff.Depot[]::new);

        convert();
    }

    private void convert() {
        var routeCost = new HashMap<List<Integer>, Double>();
        var routeLoad = new HashMap<List<Integer>, Double>();

        List<List<List<Integer>>> fml = phase1(routeCost, routeLoad);

        int sum = getSum(fml);

        if(sum != genome.sum()){
            int a = 2;
        }

        if(sum != problem.customers.size())
             System.out.println("Is: " + sum + ". Should be: " + problem.customers.size() + " in phenotype");

        phase2(fml, routeCost, routeLoad);
        this.FML = fml;
        //this.routeCost = routeCost;
        //this.routeLoad = routeLoad;
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

    private List<List<List<Integer>>> phase1(HashMap<List<Integer>, Double> routeCost, HashMap<List<Integer>, Double> routeLoad) {
        List<List<List<Integer>>> fml = new ArrayList<>();

        for (int i = 0; i < genome.lenght(); i++) {

            var chromosome = genome.get(i);

            int depIndex = customerCount + i;

            int maxLoad = depots[i].maxLoad;
            int maxDuration = depots[i].maxRoute;
            if (maxDuration == 0)
                maxDuration = Integer.MAX_VALUE;


            List<List<Integer>> routes = new ArrayList<>();
            List<Integer> currRoute = new ArrayList<>();

            double currDur = 0, currLoad = 0;

            int currCustomer = chromosome.get(0);
            currRoute.add(currCustomer);
            currDur += cost[depIndex][currCustomer];
            currLoad += customers[currCustomer].demand;
            int prevCustomer = currCustomer;

            for (int j = 1; j < chromosome.length(); j++) {
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
        if (fitness == -1) {
            double sum = 0;
            //for (double value : routeCost.values()) {
              //  sum += value;
            //}
            for(int i = 0; i < FML.size(); i++){
                var current = FML.get(i);
                for(int j = 0; j < current.size(); j++){
                    sum+= getRouteCost(current.get(j), i);
                }
            }
            fitness = sum;
        }

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

        var fromA = depotA.get(routeA);
        var fromB = depotB.get(routeB);

        var routeFromA = new ArrayList<Integer>();
        routeFromA.addAll(fromA);
        var routeFromB = new ArrayList<Integer>();
        routeFromB.addAll(fromB);

        for(var depo: FML){
            for(var routes : depo){
                routes.removeAll(routeFromB);
            }
        }

        var sum = getSum(b.FML);

        for(var depo: b.FML){
            for(var routes : depo){
                routes.removeAll(routeFromA);
            }
        }
        var sum2 = getSum(b.FML);

        if(sum2 != sum - routeFromA.size()){
            int stop = 0;
        }

        for (int newC : routeFromB) {
            insertCheapest(depot, depotA, newC);
        }

        for (int newC : routeFromA) {
            b.insertCheapest(depot, depotB, newC);
        }
        var sum3 = getSum(b.FML);

        if(sum3 != sum){
            int stop = 0;
        }



        updateGenome(depot);
        b.updateGenome(depot);
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

    public void updateGenome(int depot) {
        if(genome.sum() != customerCount)
            System.out.println("update genem");

        for(int i = 0; i < FML.size(); i++){
            var depList = FML.get(i);
            var gene = new ArrayList<Integer>();

            for (var route : depList) {
                gene.addAll(route);
            }

            genome.get(i).update(gene);

        }

        if(genome.sum() != customerCount)
            System.out.println("update genem");
    }

    public void insertCheapest(int depot, List<List<Integer>> routes, int newC) {

        int minRoute = -1;
        int minI = -1;
        double minCost = Double.POSITIVE_INFINITY;

        for (int i = 0; i < routes.size(); i++) {
            var currRoute = routes.get(i);
            double currCost = getRouteCost(currRoute, depot);

            double routeLoad = getRouteLoad(currRoute);
            if ((routeLoad + customers[newC].demand) > depots[depot].maxLoad) {
                continue;
            }

            for (int index = 0; index < currRoute.size(); index++) {
                var costOfInsertion = costOfInsertion(currRoute, depot, newC, index);

                if(currCost + costOfInsertion <= depots[depot].maxRoute){
                    if(costOfInsertion < minCost){
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
            //routeCost.put(newRoute, cost[depot][newC] * 2);
            //outeLoad.put(newRoute, (double) customers[newC].demand);
        } else {
            var route = routes.get(minRoute);
            route.add(minI, newC);
            //routeCost.put(route, routeCost.get(route) + minCost);
            //routeLoad.put(route, routeLoad.get(route) + customers[newC].demand);
        }

    }

    private double getRouteLoad(List<Integer> currRoute) {
        double sum = 0.0;
        for(var c: currRoute){
            sum+= customers[c].demand;
        }
        return sum;
    }

    private double getRouteCost(List<Integer> currRoute, int depot) {
        double c = 0.0;

        if(currRoute.size() == 0)
            return 0;

        c += cost[depot + customerCount][currRoute.get(0)];

        for(int i = 1; i < currRoute.size()-2; i++){
            c+= cost[currRoute.get(i-1)][currRoute.get(i)];
        }

        c += cost[depot + customerCount][currRoute.get(currRoute.size()-1)];
        return c;
    }

    private double costOfInsertion(List<Integer> currRoute, int depot, int newC, int index) {

        int routSize = currRoute.size();

        if (routSize > 0) {

            if (index == routSize) {
                double newCost =
                        -cost[currRoute.get(index-1)][depot + customerCount]
                                + cost[newC][depot + customerCount]
                                + cost[currRoute.get(index-1)][newC];
                return newCost;
            }
            if(index == 0){
                double newCost =
                        -cost[currRoute.get(0)][depot + customerCount]
                                + cost[newC][depot + customerCount]
                                + cost[currRoute.get(0)][newC];
                return newCost;
            }

            double newCost =
                    -cost[currRoute.get(index)][depot + customerCount]
                            + cost[newC][depot + customerCount]
                            + cost[currRoute.get(index)][newC];
            return newCost;
        }

        return 2*cost[depot + customerCount][newC];
    }
}
