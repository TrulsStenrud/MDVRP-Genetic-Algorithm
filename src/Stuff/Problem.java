package Stuff;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Problem {

    public final List<Depot> depots;
    public final List<Customer> customers;

    public Problem(List<Depot> depots, List<Customer> customers){
        this.depots = depots;
        this.customers = customers;
    }
}
