package Stuff;

import Phenotype.MaybeCustomer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Genome {
    private final Chromosome[] chromosomes;


    public Genome(int depoCount){
        this.chromosomes = new Chromosome[depoCount];
        for(int i = 0; i < depoCount; i++){
            chromosomes[i] = new Chromosome();
        }
    }

    private Genome(Chromosome[] clone) {
        this.chromosomes = clone;
    }

    public Genome copy(){
        var chromosomes = new Chromosome[this.chromosomes.length];
        for(int i = 0; i < chromosomes.length; i++){
            chromosomes[i] = this.chromosomes[i].copy();
        }
        return new Genome(chromosomes);
    }

    public void add(int i, int customer) {
        chromosomes[i].add(customer);
    }

    public Chromosome get(int i){
        return chromosomes[i];
    }

    public int lenght() {
        return chromosomes.length;
    }
}
