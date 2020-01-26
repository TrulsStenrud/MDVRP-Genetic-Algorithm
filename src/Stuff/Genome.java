package Stuff;

import Phenotype.MaybeCustomer;

import java.util.ArrayList;
import java.util.List;

public class Genome {
    private final Chromosome[] chromosomes;


    public Genome(int depoCount){
        this.chromosomes = new Chromosome[depoCount];
        for(int i = 0; i < depoCount; i++){
            chromosomes[i] = new Chromosome();
        }
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
