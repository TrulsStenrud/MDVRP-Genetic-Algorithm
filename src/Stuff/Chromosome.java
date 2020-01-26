package Stuff;

import java.util.ArrayList;

public class Chromosome {

    private final ArrayList<Integer> dna;

    public Chromosome(){
        this.dna = new ArrayList<Integer>();
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
}
