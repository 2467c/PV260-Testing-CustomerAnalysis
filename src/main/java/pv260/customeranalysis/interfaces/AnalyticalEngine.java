package pv260.customeranalysis.interfaces;

import pv260.customeranalysis.entities.Customer;
import pv260.customeranalysis.entities.Product;
import pv260.customeranalysis.exceptions.GeneralException;

import java.util.List;
import java.util.Map;

public interface AnalyticalEngine {

    List<Customer> interesetingCustomers(Product product) throws GeneralException;

    Map<String, Integer> uninterestingData() throws GeneralException;
}