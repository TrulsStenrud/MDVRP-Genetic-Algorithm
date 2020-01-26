package Stuff;

import java.util.ArrayList;

public class Chromosome {

    private final ArrayList<Integer> dna;

    public Chromosome(){
        this.dna = new ArrayList<Integer>();
    }

    private Chromosome(ArrayList<Integer> dna) {
        this.dna = dna;
    }

    public void add(int customer) {
        dna.add(customer);
    }

    public int get(int i){
        return dna.get(i);
    }

    public int length(){
        return dna.size();
    }

    public Chromosome copy() {
        var newList = new ArrayList<Integer>();
        newList.addAll(dna);
        return new Chromosome(dna);
    }

    public void update(ArrayList<Integer> gene) {
        dna.clear();
        dna.addAll(gene);
    }
}
