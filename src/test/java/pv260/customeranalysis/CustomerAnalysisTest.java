package pv260.customeranalysis;

import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.logging.Logger;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import pv260.customeranalysis.exceptions.GeneralException;
import pv260.customeranalysis.entities.Customer;
import pv260.customeranalysis.entities.Offer;
import pv260.customeranalysis.entities.Product;
import pv260.customeranalysis.interfaces.AnalyticalEngine;
import pv260.customeranalysis.interfaces.Storage;
import pv260.customeranalysis.interfaces.ErrorHandler;
import pv260.customeranalysis.interfaces.NewsList;
import static org.mockito.Mockito.times;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.logging.Level;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustomerAnalysisTest {

    private static final String MOCK_QUERY = "FMCG";

    private static final long MOCK_ID = 260;

    @Mock
    private Product demo_Product_1;
    @Mock
    private AnalyticalEngine demo_engine;
    @Mock
    private CustomerAnalysis analysis;

    private final List<AnalyticalEngine> supplier = new ArrayList<>();

    private final List<Customer> customerList = new ArrayList<>();

    @Before
    public void setUp() {

        supplier.add(demo_engine);
        supplier.add(demo_engine);
        supplier.add(demo_engine);
        customerList.add(new Customer(MOCK_ID, "Customer_A", 100));
        customerList.add(new Customer(MOCK_ID+1, "Customer_B", 200));

    }

    /**
     * Verify the ErrorHandler is invoked when one of the AnalyticalEngine methods throws exception and the exception is not re-thrown from the CustomerAnalysis.The exception is passed to the ErrorHandler directly, not wrapped.
     * @throws pv260.customeranalysis.exceptions.GeneralException
     */
    @Test(expected = GeneralException.class)
    public void testErrorHandlerInvokedWhenEngineThrows() throws GeneralException {
        Product demo_Product = mock(Product.class);
        CustomerAnalysis C_analysis = mock(CustomerAnalysis.class);
        when(C_analysis.findInterestingCustomers(demo_Product)).thenThrow(GeneralException.class);
        C_analysis.findInterestingCustomers(demo_Product);
        ErrorHandler errhandle = mock(ErrorHandler.class);
        verify(errhandle, times(1)).handle(isA(GeneralException.class));
    }

    /**
     * Verify that if first AnalyticalEngine fails by throwing an exception, subsequent engines are tried with the same input.Ordering of engines is given by their order in the List passed to constructor of AnalyticalEngine
     * @throws pv260.customeranalysis.exceptions.GeneralException
     */
    @Test
    public void testSubsequentEnginesTriedIfOneFails() throws GeneralException {
        Product demo_Product = mock(Product.class);
        AnalyticalEngine A_engine = mock(AnalyticalEngine.class);
        when(A_engine.interesetingCustomers(demo_Product)).thenReturn(new LinkedList<>());
        List<AnalyticalEngine> supplier = new ArrayList<>();
        supplier.add(A_engine);
        supplier.add(A_engine);
        supplier.add(A_engine);
        CustomerAnalysis C_analysis = new CustomerAnalysis(supplier, mock(Storage.class), mock(NewsList.class), mock(ErrorHandler.class));

        if (true) {
            try {
                when(C_analysis.findInterestingCustomers(demo_Product)).thenThrow(GeneralException.class);
            } catch (pv260.customeranalysis.exceptions.ServiceUnavailableException serviceUnavailableException) {
                System.out.println("When Exception thrown:" + serviceUnavailableException);
            }
        }

        try {
            C_analysis.findInterestingCustomers(demo_Product);
        } catch (pv260.customeranalysis.exceptions.ServiceUnavailableException serviceUnavailableException) {
            System.out.println("Exception thrown:" + serviceUnavailableException);
        }

        verify(A_engine, times(3)).interesetingCustomers(demo_Product);
    }

    /**
     * @throws pv260.customeranalysis.exceptions.GeneralException
     */
    @Test
    public void testNoMoreEnginesTriedAfterOneSucceeds() throws GeneralException {
        when(demo_engine.interesetingCustomers(demo_Product_1)).thenReturn(customerList);
        List<Customer> result = analysis.findInterestingCustomers(demo_Product_1);

        try {
            System.out.println(result.get(0));
        } catch (Exception e) {
            System.out.println("Why no result?");
        }

        analysis = new CustomerAnalysis(new ArrayList<>(), mock(Storage.class), mock(NewsList.class), mock(ErrorHandler.class));

        try {
            Field field = analysis.getClass().getDeclaredField("engines");
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(analysis, supplier);

        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(CustomerAnalysisTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<Customer> result2 = analysis.findInterestingCustomers(demo_Product_1);
        verify(demo_engine, times(1)).interesetingCustomers(demo_Product_1);
        assertEquals(result2.get(0), customerList.get(0));

    }
    /**
      @throws pv260.customeranalysis.exceptions.GeneralException
     */
    @Test
    public void testOfferIsPersistedBefreAddedToNewsList() throws GeneralException {
        
        ErrorHandler Handler = mock(ErrorHandler.class);
        Product Product_1 = mock(Product.class);
        Customer customer = mock(Customer.class);
        AnalyticalEngine engine = mock(AnalyticalEngine.class);
        Storage storage = mock(Storage.class);
        when(storage.find(Product.class, 0)).thenReturn(Product_1);
        when(engine.interesetingCustomers(Product_1)).thenReturn(asList(customer));
        NewsList newsList = mock(NewsList.class);
        CustomerAnalysis analysis = new CustomerAnalysis(asList(engine),storage, newsList, Handler);
        analysis.prepareOfferForProduct(0);
        ArgumentCaptor<Offer> offer_captor1= ArgumentCaptor.forClass(Offer.class);
        ArgumentCaptor<Offer> offer_captor2= ArgumentCaptor.forClass(Offer.class);
        InOrder inOrder = inOrder(storage, newsList);
        inOrder.verify(storage).persist(offer_captor1.capture());
        inOrder.verify(newsList).sendPeriodically(offer_captor2.capture());
        Offer offer1 = offer_captor1.getValue();
        assertEquals(customer, offer1.getCustomer());
        Offer offer2 = offer_captor2.getValue();
        assertEquals(customer, offer2.getCustomer());
        assertEquals(offer1, offer2);
    }

}