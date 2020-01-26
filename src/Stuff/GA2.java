package Stuff;

import Phenotype.MaybeCustomer;
import Phenotype.Phenotype;

import java.util.*;

public class GA2 {

    public final Problem problem;

    int population = 10;
    int nParents = 5;
    double mutationRate = 0.1;
    HashMap<Genome, Phenotype> phenotypes = new HashMap<Genome, Phenotype>();

    public Genome[] genes;

    private double[][] cost;


    public GA2(Problem p) {
        this.problem = p;
    }

    public Phenotype initiate() {

        cost = calculateCostMatrix();

        genes = new Genome[population];
        for (int i = 0; i < population; i++) {
            genes[i] = createNewGenome();
        }

        evaluate();
        return phenotypes.get(genes[0]);
    }

    private void evaluate() {
        Arrays.sort(genes, Comparator.comparingDouble(this::coreEvaluate));
    }



    private double coreEvaluate(Genome gene) {
        Phenotype phenotype;
        if (phenotypes.containsKey(gene)) {
            phenotype = phenotypes.get(gene);
        }
        else{
            phenotype = new Phenotype(gene, cost, problem);
            phenotypes.put(gene, phenotype);
        }

        return phenotype.fitness();

    }

    private Genome createNewGenome() {
        //TODO could be interesting to maybe look through customers in some order

        HashMap<Depot, List<MaybeCustomer>> clusters = new HashMap<>();

        for (Depot d : problem.depots) {
            clusters.put(d, new ArrayList<>());
        }

        for (int i = 0; i < problem.customers.size(); i++) {

            double min = Double.POSITIVE_INFINITY;
            var current = problem.customers.get(i);
            Depot minDepot = null;

            for (int j = 0; j < problem.depots.size(); j++) {
                var d = problem.depots.get(j);
                var distance = d.point.distance(current.point);

                if (distance < min) {
                    min = distance;
                    minDepot = d;
                }
            }

            //TODO also find "close" depots, to add to uncertainty. Maybe also just create a hashmap, mapping customer
            // index to uncertain depots
            clusters.get(minDepot).add(new MaybeCustomer(current.point, i, null));
        }


        var gene = new Genome(problem.depots.size());

        for (int i = 0; i < problem.depots.size(); i++) {
            var customers = clusters.get(problem.depots.get(i));

            for (var c : customers) {
                gene.add(i, c.index);
            }
        }

        int sum = 0;
        for (int i = 0; i < gene.lenght(); i++) {
            var x = gene.get(i);
            sum += x.length();
        }

        System.out.println("Is: " + sum + ". Should be: " + problem.customers.size());
        return gene;
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


    public Phenotype generation(){

        int power = 5;
        double sum = 0.0;
        for (var d : phenotypes.values()) {
            sum+=Math.pow(1/d.fitness(), power);
        }

        List<Genome> parents = new ArrayList<>();
        double[] probabilityFitness = new double[genes.length];
        double t = 0.0;
        for (int i = 0; i < genes.length; i++) {
            double fitness = phenotypes.get(genes[i]).fitness();
            var fp = Math.pow(1/fitness, power)/sum;
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

            Phenotype A = new Phenotype(phenotypes.get(parents.get(pA)).genome, cost,  problem), B = new Phenotype(phenotypes.get(parents.get(pA)).genome, cost, problem);

            A.Reproduce(B);

               parents.add(A.genome);
               phenotypes.put(A.genome, A);
                if(parents.size()<population){
                    parents.add(B.genome);
                    phenotypes.put(B.genome, B);
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
            //scrambleMutation(parents.get(r));
        }


        for(int i = 0; i < population; i++){
            genes[i] = parents.get(i);
        }

        evaluate();
        return phenotypes.get(genes[0]);
    }


}
