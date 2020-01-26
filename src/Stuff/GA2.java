package Stuff;

import Phenotype.MaybeCustomer;
import Phenotype.Phenotype;

import java.util.*;

public class GA2 {

    public final Problem problem;

    int population = 1000;
    int nParents = 50;
    double mutationRate = 0.1;

    public Genome[] genes;


    HashMap<int[], Double> fitness = new HashMap<>();


    public GA2(Problem p){
        this.problem = p;
    }

    public Phenotype initiate(){


        //TODO could be interesting to maybe look through customers in some order
        HashMap<Depot, List<MaybeCustomer>> clusters = new HashMap<>();

        for (Depot d:problem.depots) {
            clusters.put(d, new ArrayList<>());
        }

        for(int i = 0; i < problem.customers.size(); i++){

            double min = Double.POSITIVE_INFINITY;
            var current = problem.customers.get(i);
            Depot minDepot = null;

            for(int j = 0; j < problem.depots.size(); j++){
                var d = problem.depots.get(j);
                var distance = d.point.distance(current.point);

                if(distance < min){
                    min = distance;
                    minDepot = d;
                }
            }

            //TODO also find "close" depots, to add to uncertainty. Maybe also just create a hashmap, mapping customer
            // index to uncertain depots
            clusters.get(minDepot).add(new MaybeCustomer(current.point, i, null));
        }


        var gene = new Genome(problem.depots.size());

        for(int i = 0; i < problem.depots.size(); i++){
            var customers = clusters.get(problem.depots.get(i));

            for(var c : customers){
                gene.add(i, c.index);
            }
        }

        double[][] cost = calculateCostMatrix();

        int sum = 0;
        for(int i = 0; i < gene.lenght(); i++){
            var x = gene.get(i);
            sum += x.length();
        }

        System.out.println("Is: " + sum + ". Should be: " + problem.customers.size());

        return new Phenotype(gene, cost, problem);
    }

    private double[][] calculateCostMatrix() {
        int cSize = problem.customers.size();
        int size = cSize + problem.depots.size();

        double[][] cost = new double[size][size];

        for (int i = 0; i < problem.customers.size(); i++){
            var cPoint = problem.customers.get(i).point;

            for (int j = 0; j < problem.customers.size(); j++){
                var distance = cPoint.distance(problem.customers.get(j).point);
                cost[i][j] = distance;
                cost[j][i] = distance;
            }

            for (int j = 0; j < problem.depots.size(); j++){
                var distance = cPoint.distance(problem.depots.get(j).point);
                cost[i][cSize + j] = distance;
                cost[cSize + j][i] = distance;
            }
        }

        return cost;
    }


}
