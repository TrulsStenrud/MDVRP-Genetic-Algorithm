package Stuff;

import java.util.List;

public class Problem {
    private final List<Depot> depots;
    private final List<Customer> customers;

    public Problem(List<Depot> depots, List<Customer> customers){
        this.depots = depots;
        this.customers = customers;
    }
}
