package Stuff;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Problem {

    private int population = 1000;
    private double survivalFraction = 0.3;

    public final List<Depot> depots;
    public final List<Customer> customers;

    private int[][] genes;

    public Problem(List<Depot> depots, List<Customer> customers){
        this.depots = depots;
        this.customers = customers;
    }

    public int[][] initiate(){

        var genes = new int[population][];

        for(int x = 0; x < genes.length; x++){
            var gene = new int[customers.size()];

            for (int i = 0; i < gene.length; i++){
                gene[i] = i;
            }

            for (int i = 0; i < gene.length; i++){
                int r = (int) (Math.random() * gene.length);
                int temp = gene[r];
                gene[r] = gene[i];
                gene[i] = temp;
            }

            genes[x] = gene;
        }

        HashMap<int[], Double> a = new HashMap<>();

        Arrays.sort(genes, Comparator.comparingDouble(x -> evaluate(x, a)));

        return genes;
    }

    private Double evaluate(int[] x, HashMap<int[], Double> map) {
        if(map.containsKey(x)){
            return map.get(x);
        }
        var value = coreEvaluate(x);
        map.put(x, value);

        return value;
        //return  map.containsKey(x) ? map.get(x) : map.put(x, coreEvaluate(x));
    }


    private Double coreEvaluate(int[] gene) {
        double sum = 0;
        sum+= depots.get(0).point.distance(customers.get(gene[0]).point);

        for(int i = 1; i < gene.length; i++){
            sum += customers.get(gene[i]).point.distance(customers.get(gene[i-1]).point);
        }

        sum+= depots.get(0).point.distance(customers.get(gene[gene.length-1]).point);


        return sum;
    }

    public void generation(){
        var divider = population * survivalFraction;

        for(int i = divider; i < genes.length; i++){

        }

    }


}
