package pv260.customeranalysis.entities;

public class Customer {

    private long id;

    private String name;

    private int credit;

    public Customer(long id, String name, int credit) {
        this.id = id;
        this.name = name;
        this.credit = credit;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCredit() {
        return credit;
    }
}
