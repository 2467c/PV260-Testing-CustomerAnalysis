package pv260.customeranalysis.entities;

public class Offer {

    private Customer customer;
    private Product product;

    public Offer(Customer customer, Product product) {
        this.customer = customer;
        this.product = product;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Product getProduct() {
        return product;
    }
}
