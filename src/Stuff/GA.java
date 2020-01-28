package Stuff;

import Types.Phenotype1;
import javafx.geometry.Point2D;

import java.util.*;
import java.util.stream.Collectors;

public class GA {

    public final Problem problem;
    private final double[][] cost;

    int population = 500;
    int nParents = 20;
    double mutationRate = 0.3;

    public int[][] genes;

    HashMap<int[], Double> fitness = new HashMap<>();
    HashMap<int[], Phenotype1> phenotypes = new HashMap<>();


    public GA(Problem p){
        this.problem = p;
        cost = calculateCostMatrix();
    }

    public Phenotype1 initiate(){
        genes = new int[population][];

        for(int x = 0; x < genes.length; x++){
            int[] gene = generateShortRoute(problem);
            genes[x] = gene;
        }

        fitness.clear();
        evaluate();
        return phenotypes.get(genes[0]);
    }

    private double[][] calculateCostMatrix() {
        int cSize = problem.customers.size();
        int size = cSize + problem.depots.size();

        double[][] cost = new double[size][size];

        for (int i = 0; i < problem.customers.size(); i++) {
            var cPoint = problem.customers.get(i).point;

            for (int j = 0; j < problem.customers.size(); j++) {
                var distance = cPoint.distance(problem.customers.get(j).point);
                cost[i][j] = distance;
                cost[j][i] = distance;
            }

            for (int j = 0; j < problem.depots.size(); j++) {
                var distance = cPoint.distance(problem.depots.get(j).point);
                cost[i][cSize + j] = distance;
                cost[cSize + j][i] = distance;
            }
        }

        return cost;
    }


    private int[] generateRandomRoute() {
        var gene = new int[problem.customers.size()];

        for (int i = 0; i < gene.length; i++){
            gene[i] = i;
        }

        for (int i = 0; i < gene.length; i++){
            int r = (int) (Math.random() * gene.length);
            switchPlace(gene, r, i);
        }
        return gene;
    }

    public static int[] generateShortRoute(Problem problem) {
        List<Point2D> points = problem.customers.stream().map(x -> x.point).collect(Collectors.toList());
        var remaining = new ArrayList<Point2D>();

        for(var point : points){
            remaining.add(point);
        }

        List<Integer> indices = new ArrayList<>(points.size());
        int first = (int)(Math.random()*points.size());
        var current = points.get(first);
        remaining.remove(first);
        indices.add(first);

        while (!remaining.isEmpty()){
            Point2D closest = remaining.stream().min(Comparator.comparingDouble(current::distance)).get();
            remaining.remove(closest);
            indices.add(points.indexOf(closest));
            current = closest;
        }

        int[] result = new int[indices.size()];
        for(int i = 0; i < result.length; i++){
            result[i] = indices.get(i);
        }
        return result;
    }

    public void evaluate(){
        Arrays.sort(genes, Comparator.comparingDouble(this::evaluate));
    }

    public Phenotype1 generation(){

        int power = 4;
        double sum = 0.0;
        for (Double d : fitness.values()) {
            sum+=Math.pow(1/d, power);
        }

        List<int[]> parents = new ArrayList<>();
        double[] probabilityFitness = new double[genes.length];
        double t = 0.0;
        for (int i = 0; i < genes.length; i++) {

            var fp = Math.pow(1/fitness.get(genes[i]), power)/sum;
            probabilityFitness[i] = t+=fp;
        }

        //System.out.println(Arrays.toString(probabilityFitness));

        while (parents.size() < nParents){
            double r = Math.random();

            for(int i = 0; i < genes.length; i++){
                if(probabilityFitness[i] > r){
                    if(!parents.contains(genes[i])){
                        parents.add(genes[i]);
                    }
                    break;
                }
            }
        }
//        System.out.println(Arrays.toString(p));
//        System.out.println(Arrays.toString(Arrays.stream(genes).map(fitness::get).toArray(Double[]::new)));
//        System.out.println(Arrays.toString(parents.stream().map(fitness::get).toArray(Double[]::new)));

        while (parents.size() < population){

            int pA = (int) (Math.random()*nParents);
            int pB = (int) (Math.random()*nParents);
            while (pB == pA){
                pB = (int) (Math.random()*nParents);
            }

            var children = Reproduction.PartiallyMappedCrossover(parents.get(pA), parents.get(pB));

            if(children!=null){
                parents.add(children[0]);

                if(parents.size()<population){
                    parents.add(children[1]);
                }
            }
            else{
                System.out.println("Children were null, not good");
            }
        }

        int nMutations = (int) Math.round(((double)population)*mutationRate);


        HashSet<Integer> taken = new HashSet<>();
        for(int i = 0; i < nMutations; i++){
            int r = (int) (Math.random() *(population - nParents));
            while (taken.contains(r)){
                r = (int) (Math.random() *(population - nParents));
            }
            taken.add(r);
            scrambleMutation(parents.get(r));
        }

        fitness.clear();
        for(int i = 0; i < population; i++){
            genes[i] = parents.get(i);
        }
        phenotypes.clear();
        fitness.clear();
        evaluate();
        //System.out.println(fitness.get(genes[0]));

        return phenotypes.get(genes[0]);
    }

    private void insertMutation(int[] gene) {
        int index1 = (int) (Math.random()*gene.length),
                index2 = (int) (Math.random()*gene.length);

        switchPlace(gene, index1, index2);
    }

    private void scrambleMutation(int[] gene) {
        int r1 = (int) (Math.random()*gene.length),
                r2 = (int) (Math.random()*gene.length);

        int index1 = Math.min(r1, r2),
                index2 = Math.max(r1, r2);

        for(int i = index1; i <= index2; i++){
            int r = (int) (index1 + Math.random()*(index2-index1));
            switchPlace(gene, i, r);
        }
    }

    private void inverseMutation(int[] gene) {
        int r1 = (int) (Math.random()*gene.length),
                r2 = (int) (Math.random()*gene.length);

        int index1 = Math.min(r1, r2),
                index2 = Math.max(r1, r2);

        inverseSubstring(gene, index1, index2);
    }

    public static void inverseSubstring(int[] gene, int index1, int index2) {
        for(int i = 0; i <= (index2 - index1)/2; i++){
            int x = index1 + i;
            int y = index2 - i;
            switchPlace(gene, x, y);
        }
    }

    private static void switchPlace(int[] gene, int x, int y) {
        int temp = gene[x];
        gene[x] = gene[y];
        gene[y] = temp;
    }

    private Double evaluate(int[] x) {
        if(fitness.containsKey(x)){
            return fitness.get(x);
        }
        var value = getPhenotype(x).fitness();
        fitness.put(x, value);

        return value;
    }


    private Phenotype1 getPhenotype(int[] gene) {
        if(phenotypes.containsKey(gene)){
           return phenotypes.get(gene);
        }

        var p = new Phenotype1(gene, cost, problem);
        phenotypes.put(gene, p);
        return p;
    }

}
