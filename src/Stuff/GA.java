package Stuff;

import javafx.geometry.Point2D;

import java.util.*;
import java.util.stream.Collectors;

public class GA {

    public final Problem problem;

    int population = 1000;
    int nParents = 50;
    double mutationRate = 0.1;

    public int[][] genes;
    private HashMap<int[], Phenotype> phenotypes;
    HashMap<int[], Double> fitness = new HashMap<>();


    public GA(Problem p){
        this.problem = p;
    }

    public void initiate(){
        genes = new int[population][];

        for(int x = 0; x < genes.length; x++){
            int[] gene = generateRandomRoute();
            genes[x] = gene;
        }

        fitness.clear();
        evaluate();
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

    private int[] generateShortRoute() {
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

    public void generation(){

        int power = 5;
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

        evaluate();
        System.out.println(fitness.get(genes[0]));
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
        var value = coreEvaluate(x);
        fitness.put(x, value);

        return value;
    }


    private Double coreEvaluate(int[] gene) {
        double sum = 0;
        var d = problem.depots;
        var c = problem.customers;

        sum+= d.get(0).point.distance(c.get(gene[0]).point);

        for(int i = 1; i < gene.length; i++){
            sum += c.get(gene[i]).point.distance(c.get(gene[i-1]).point);
        }

        sum+= d.get(0).point.distance(c.get(gene[gene.length-1]).point);

        return sum;
    }

}
