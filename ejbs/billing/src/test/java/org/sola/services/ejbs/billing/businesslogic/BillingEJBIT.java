package org.sola.services.ejbs.billing.businesslogic;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.sola.services.common.test.AbstractEJBTest;
import org.sola.services.ejbs.billing.repository.entities.RegisteredLease;

/**
 *
 * @author nmafereka
 */
public class BillingEJBIT extends AbstractEJBTest {

    //private static final String LOGIN_USER = "nmafereka";
    //private static final String LOGIN_PASS = "test";
    private static final String LEASE_ID = "12345-001";

    public BillingEJBIT() {
        super();
    }

 

    @Before
    public void setUp() throws Exception {
        //login(LOGIN_USER, LOGIN_PASS);
    }

    @After
    public void tearDown() throws Exception {
        //logout();
    }

    @Test
    public void testGetLease() throws Exception {
        System.out.println(">>> Trying to get lease data by lease ID.");

        try {
            BillingEJBLocal instance = (BillingEJBLocal) getEJBInstance(BillingEJB.class.getSimpleName());

            RegisteredLease result = instance.getLeaseById(LEASE_ID);

            assertNotNull("lease not found.", result);
            System.out.println(">>> Found lease " + result.getLeaseNumber());
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }
}
