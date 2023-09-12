package Stuff;

import Types.Phenotype;
import javafx.geometry.Point2D;

import java.util.*;
import java.util.stream.Collectors;

public class GA {

    public final Problem problem;

    int population;
    int nParents;
    double mutationRate;

    public Phenotype[] genes;


    public GA(Problem p, int population, int nParents, double mutationRate) {
        this.problem = p;
        this.population = population;
        this.nParents = nParents;
        this.mutationRate = mutationRate;
    }

    public static int[] generateShortRoute(Problem problem) {
        List<Point2D> points = Arrays.stream(problem.customers).map(x -> x.point).collect(Collectors.toList());
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


    private List<List<Integer>> createInitRoutes(int[] route) {
        //TODO could be interesting to maybe look through customers in some order

        var result = new ArrayList<List<Integer>>();

        for (Depot d : problem.depots) {
            result.add(new ArrayList<>());
        }

        for (var c : route) {
            result.get(problem.closestDepots.get(c).get(0)).add(c);
        }

        return result;
    }

    public Phenotype generation() {

        int power = 4;
        double sum = 0.0;
        for (var d : genes) {
            var fitness = d.fitness() + ((d).correctCarCount() ? 0 : 800);
            sum += Math.pow(1000 / fitness, power);
        }

        List<Phenotype> parents = new ArrayList<>();
        double[] probabilityFitness = new double[genes.length];
        double t = 0.0;
        boolean atLeastOnewFeasable = false;
        for (int i = 0; i < genes.length; i++) {
            var isFeasable = genes[i].correctCarCount();

            if (!atLeastOnewFeasable)
                if (isFeasable) {
                    if (i != 0)
                        parents.add(genes[i]);
                    atLeastOnewFeasable = true;
                }

            double fitness = genes[i].fitness() + (isFeasable ? 0 : 800);
            var fp = Math.pow(1000 / fitness, power) / sum;
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
            if (x < 0.6) {
                mutate(parents.get(r));
            } else {
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
        var a = new ArrayList<Integer>();

        for (int i = 0; i < problem.depots.length; i++) {
            if (problem.depots[i].maxCarCount < phenotype.FML.get(i).size()) {
                a.add(i);
            }
        }

        int depot;

        if (a.isEmpty()) {
            depot = (int) (Math.random() * phenotype.FML.size());
        } else {
            int r = (int) (Math.random() * a.size());
            depot = a.get(r);
        }


        var d = phenotype.FML.get(depot);

        int v = (int) (Math.random() * d.size());

        var vehicle = d.get(v);

        var swapable = new ArrayList<Integer>();

        for (var c : vehicle) {
            if (problem.closestDepots.get(c).size() > 1)
                swapable.add(c);
        }

        if (!swapable.isEmpty()) {
            int r = (int) (Math.random() * swapable.size());

            int c = swapable.get(r);

            vehicle.remove(Integer.valueOf(c));

            if (vehicle.isEmpty())
                d.remove(vehicle);

            int newD;
            var closeDepos = problem.closestDepots.get(c);

            while ((newD = closeDepos.get((int) (Math.random() * closeDepos.size()))) == depot) {
            }

            phenotype.insertCheapest(newD, c);
        }
    }

    public static void inverseSubstring(List<Integer> gene, int index1, int index2) {
        for (int i = 0; i <= (index2 - index1) / 2; i++) {
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
