package Phenotype;

import Stuff.Customer;
import Stuff.Depot;
import Stuff.Genome;
import Stuff.Problem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Phenotype {

    private final Genome genome;
    private final Problem problem;
    private final Customer[] customers;
    private final Depot[] depots;
    private double[][] cost;
    private int customerCount;
    HashMap<List<Integer>, Double> routeCost;
    HashMap<List<Integer>, Double> routeLoad;
    public List<List<List<Integer>>> FML;

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

        var sum = 0;
        for(var x : fml){
            for(var y : x){
                sum += y.size();
                System.out.println(y);
            }
        }

        System.out.println("Is: " + sum + ". Should be: " + problem.customers.size());

        //phase2(fml, routeCost, routeLoad);
        this.FML = fml;
        this.routeCost = routeCost;
        this.routeLoad = routeLoad;
    }

    private void phase2(List<List<List<Integer>>> fml, HashMap<List<Integer>, Double> routeCost, HashMap<List<Integer>, Double> routeLoad) {
        for(int i = 0; i < fml.size(); i++){

            var currDepList = fml.get(i);

            for(int j = currDepList.size() - 1 ; j > 0; j--){

                var currVehicle = currDepList.get(j);
                var prevVehicle = currDepList.get(j-1);

                var currCost = routeCost.getOrDefault(currVehicle, 0.0);
                var prevCost = routeCost.getOrDefault(prevVehicle, 0.0);

                var currLoad = routeLoad.getOrDefault(currVehicle, 0.0);
                var prevLoad = routeLoad.getOrDefault(prevVehicle, 0.0);

                int cToBeMoved = prevVehicle.get(prevVehicle.size()-1);

                double demand = customers[cToBeMoved].demand;

                double newLoad = currLoad + demand;

                if(newLoad > depots[i].maxLoad)
                    continue;


                var newCost = currCost
                        - cost[i+customerCount][currVehicle.get(0)]
                        + cost[cToBeMoved][currVehicle.get(0)] + cost[i+customerCount][cToBeMoved];

                if(newCost > depots[i].maxRoute)
                    continue;

                double newPrevCost = valueMinusLast(i, prevVehicle, prevCost);

                if(newCost + newPrevCost >= currCost + prevCost)
                    continue;

                prevVehicle.remove(prevVehicle.size()-1);
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

        originalCost-= cost[depIndex][lastIndex];
        originalCost -= cost[lastIndex][lastIndex-1];
        originalCost += cost[lastIndex-1][depIndex];

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

            routes.add(currRoute);
            fml.add(routes);
        }
        return fml;
    }


}
