/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sola.services.ejbs.billing.businesslogic;

import java.math.BigDecimal;
import java.util.List;
import javax.ejb.Local;
import org.sola.services.common.ejbs.AbstractEJBLocal;
import org.sola.services.ejbs.billing.repository.entities.CalculationType;
import org.sola.services.ejbs.billing.repository.entities.CustomerBill;
import org.sola.services.ejbs.billing.repository.entities.PayPeriod;
import org.sola.services.ejbs.billing.repository.entities.PayRate;
import org.sola.services.ejbs.billing.repository.entities.RegisteredLease;

/**
 *
 * @author nmafereka Billing service business logic EJB interface
 */
@Local
public interface BillingEJBLocal extends AbstractEJBLocal {

    /**
     * Bill a particular Lease.
     *
     * @param customerBill The lease in question.
     * @return true if customer lease has been billed.
     */
    CustomerBill saveCustomerBill(CustomerBill customerBill);
    
    RegisteredLease getLeaseById(String leaseNumber);
    
    List<PayRate> getPayRate(int billingPeriod, int registrationYear, 
                                String landUse, String groundRentZone);
    
    List<PayPeriod> getPayPeriod(String languageCode);
    
    List<CalculationType> getCalculationType(String languageCode);
    
    boolean isPeriodBilliable(int paymentPeriod, int lastPaymentYear);
    
    BigDecimal calculateGroundRent(BigDecimal leasedArea, BigDecimal groundRentRate, 
                                    String calculationMethod, int complianceCode);

}
