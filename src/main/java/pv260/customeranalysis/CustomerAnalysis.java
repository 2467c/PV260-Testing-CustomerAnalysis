package pv260.customeranalysis;

import pv260.customeranalysis.entities.Customer;
import pv260.customeranalysis.entities.Offer;
import pv260.customeranalysis.entities.Product;
import pv260.customeranalysis.exceptions.GeneralException;
import pv260.customeranalysis.exceptions.ServiceUnavailableException;
import pv260.customeranalysis.interfaces.AnalyticalEngine;
import pv260.customeranalysis.interfaces.ErrorHandler;
import pv260.customeranalysis.interfaces.NewsList;
import pv260.customeranalysis.interfaces.Storage;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class CustomerAnalysis {

    private static final Pattern VALID_PARAM = Pattern.compile("\\w{3,}");

    private final List<AnalyticalEngine> engines;

    private final Storage storage;

    private final NewsList newslist;

    private final ErrorHandler errorHandler;

    public CustomerAnalysis(
            List<AnalyticalEngine> supplier,
            Storage storage,
            NewsList newslist,
            ErrorHandler errorHandler) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(storage);
        Objects.requireNonNull(newslist);
        Objects.requireNonNull(errorHandler);
        this.engines = supplier;
        this.storage = storage;
        this.newslist= newslist;
        this.errorHandler = errorHandler;
    }

    List<Customer> findInterestingCustomers(Product product) throws ServiceUnavailableException {
        for (AnalyticalEngine engine : this.engines) {
            try {
                return engine.interesetingCustomers(product);
            } catch (GeneralException e) {
                this.errorHandler.handle(e);
            }
        }
        throw new ServiceUnavailableException();
    }


    public void prepareOfferForProduct(long productId) throws GeneralException {
       
        Product prod = this.storage.find(Product.class, productId);
        
        List<Customer> customers = this.findInterestingCustomers(prod);
        
        for (Customer customer : customers) {
            try {
                Offer offer = new Offer(customer, prod);
                this.storage.persist(offer);
                this.newslist.sendPeriodically(offer);
            } catch (ServiceUnavailableException e) {
                throw e;
            } catch (GeneralException e) {
                this.errorHandler.handle(e);
            }
        }
       
    }




}
