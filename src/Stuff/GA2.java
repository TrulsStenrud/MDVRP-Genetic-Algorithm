package Stuff;

import Types.MaybeCustomer;
import Types.Phenotype;
import javafx.geometry.Point2D;

import java.util.*;
import java.util.stream.Collectors;

public class GA2 {

    public final Problem problem;

    int population = 200;
    int nParents = 10;
    double mutationRate = 0.2;

    public Phenotype[] genes;

    private double[][] cost;


    public GA2(Problem p, int population, int nParents, double mutationRate) {
        this.problem = p;
        this.population = population;
        this.nParents = nParents;
        this.mutationRate = mutationRate;
    }

    public static int[] generateShortRoute(Problem problem) {
        List<Point2D> points = problem.customers.stream().map(x -> x.point).collect(Collectors.toList());
        var remaining = new ArrayList<Point2D>();

        for (var point : points) {
            remaining.add(point);
        }

        List<Integer> indices = new ArrayList<>(points.size());
        int first = (int) (Math.random() * points.size());
        var current = points.get(first);
        remaining.remove(first);
        indices.add(first);

        while (!remaining.isEmpty()) {
            Point2D closest = remaining.stream().min(Comparator.comparingDouble(current::distance)).get();
            remaining.remove(closest);
            indices.add(points.indexOf(closest));
            current = closest;
        }

        int[] result = new int[indices.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = indices.get(i);
        }
        return result;
    }

    public Phenotype initiate() {
        genes = new Phenotype[population];

        for (int i = 0; i < genes.length; i++) {
            var route = generateShortRoute(problem);
            var initState = createInitRoutes(route);
            genes[i] = new Phenotype(initState, problem);
        }

        evaluate();
        return genes[0];
    }

    private void evaluate() {
        Arrays.sort(genes, Comparator.comparingDouble(Phenotype::fitness));
    }


    private List<List<Integer>> createInitRoutes(int[] order) {
        //TODO could be interesting to maybe look through customers in some order

        HashMap<Depot, List<MaybeCustomer>> clusters = new HashMap<>();

        for (Depot d : problem.depots) {
            clusters.put(d, new ArrayList<>());
        }

        for (int i = 0; i < order.length; i++) {

            double min = Double.POSITIVE_INFINITY;
            var current = problem.customers.get(order[i]);
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
            clusters.get(minDepot).add(new MaybeCustomer(current.point, order[i], null));
        }


        var result = new ArrayList<List<Integer>>(problem.depots.size());
        for (int i = 0; i < problem.depots.size(); i++) {
            result.add(new ArrayList<>());
        }

        for (int i = 0; i < problem.depots.size(); i++) {
            var customers = clusters.get(problem.depots.get(i));

            for (var c : customers) {
                result.get(i).add(c.index);
            }
        }

        return result;
    }

    public Phenotype generation() {

        int power = 5;
        double sum = 0.0;
        for (var d : genes) {
            var fitness = d.fitness() + ((d).isFeasable() ? 0 : 800);
            sum += Math.pow(100 / fitness, power);
        }

        List<Phenotype> parents = new ArrayList<>();
        double[] probabilityFitness = new double[genes.length];
        double t = 0.0;
        boolean atLeastOnewFeasable = false;
        for (int i = 0; i < genes.length; i++) {
            var isFeasable = genes[i].isFeasable();

            if (!atLeastOnewFeasable)
                if (isFeasable) {
                    if (i != 0)
                        parents.add(genes[i]);
                    atLeastOnewFeasable = true;
                }

            double fitness = genes[i].fitness() + (isFeasable ? 0 : 800);
            var fp = Math.pow(100 / fitness, power) / sum;
            probabilityFitness[i] = t += fp;
        }

        //System.out.println(Arrays.toString(probabilityFitness));
        parents.add(genes[0]);
        while (parents.size() < nParents) {
            double r = Math.random();

            for (int i = 0; i < genes.length; i++) {
                if (probabilityFitness[i] > r) {
                    if (!parents.contains(genes[i])) {
                        parents.add(genes[i]);
                    }
                    break;
                }
            }
        }
//        System.out.println(Arrays.toString(p));
//        System.out.println(Arrays.toString(Arrays.stream(genes).map(fitness::get).toArray(Double[]::new)));
//        System.out.println(Arrays.toString(parents.stream().map(fitness::get).toArray(Double[]::new)));

        while (parents.size() < population) {

            int pA = (int) (Math.random() * nParents);
            int pB = (int) (Math.random() * nParents);
            while (pB == pA) {
                pB = (int) (Math.random() * nParents);
            }

            var parentA = parents.get(pA);
            var parentB = parents.get(pB);

            var A = parentA.copy();
            var B = parentB.copy();

            A.Reproduce(B);


            //if (!isFeasable(A))
            A.makeFeseable();

            parents.add(A);
            if (parents.size() < population) {
                //    if (isFeasable(B))
                parents.add(B);
            }

        }

        int nMutations = (int) Math.round(((double) population) * mutationRate);


        HashSet<Integer> taken = new HashSet<>();
        for (int i = 0; i < nMutations; i++) {
            int r = (int) (Math.random() * (population - nParents)) + nParents;
            while (taken.contains(r)) {
                r = (int) (Math.random() * (population - nParents)) + nParents;
            }
            taken.add(r);

            var x = Math.random();
            if(x < 0.5){
                mutate(parents.get(r));
            }
            else if(x < 1){
                inverseMutate(parents.get(r));
            }
            else{
                interDepotMutation(parents.get(r));
            }


        }


        for (int i = 0; i < population; i++) {
            genes = parents.toArray(Phenotype[]::new);
        }

        evaluate();
        return genes[0];
    }

    private void interDepotMutation(Phenotype phenotype) {
        int depot = (int) (Math.random()*phenotype.FML.size());

        var d = phenotype.FML.get(depot);

        int v = (int) (Math.random()*d.size());

        var vehicle = d.get(v);

        d.remove(v);
        int newD;
        while ((newD = (int) (Math.random()*phenotype.FML.size())) == depot){
        }

        for(var c : vehicle){
            phenotype.insertCheapest(newD, c);
        }
    }

    private void inverseMutate(Phenotype phenotype) {
        int depot = (int) (Math.random()*phenotype.FML.size());

        var d = phenotype.FML.get(depot);

        int v = (int) (Math.random()*d.size());

        var vehicle = d.get(v);

        int r1 = (int) (Math.random()*vehicle.size()),
                r2 = (int) (Math.random()*vehicle.size());

        int index1 = Math.min(r1, r2),
                index2 = Math.max(r1, r2);

        inverseSubstring(vehicle, index1, index2);
    }

    public static void inverseSubstring(List<Integer> gene, int index1, int index2) {
        for(int i = 0; i <= (index2 - index1)/2; i++){
            int x = index1 + i;
            int y = index2 - i;
            switchPlace(gene, x, y);
        }
    }

    private static void switchPlace(List<Integer> gene, int x, int y) {
        int temp = gene.get(x);
        gene.set(x, gene.get(y));
        gene.set(y, temp);
    }

    private void mutate(Phenotype x) {

        for (int i = 0; i < x.FML.size(); i++) {
            var d = x.FML.get(i);
        }

        int depIndex = (int) (Math.random() * x.FML.size());

        var depot = x.FML.get(depIndex);

        int vIndex = (int) (Math.random() * depot.size());

        var vehicle = depot.get(vIndex);

        int cIndex = (int) (Math.random() * vehicle.size());
        int customer = vehicle.get(cIndex);

        vehicle.remove(Integer.valueOf(customer));
        if (vehicle.size() == 0)
            depot.remove(vIndex);

        x.insertCheapest(depIndex, customer);
    }


}
