/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sola.services.ejbs.billing.businesslogic;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.sola.services.common.ejbs.AbstractEJB;
import org.sola.services.common.repository.CommonSqlProvider;
import org.sola.services.ejbs.billing.repository.entities.CalculationType;
import org.sola.services.ejbs.billing.repository.entities.CustomerBill;
import org.sola.services.ejbs.billing.repository.entities.PayPeriod;
import org.sola.services.ejbs.billing.repository.entities.PayRate;
import org.sola.services.ejbs.billing.repository.entities.RegisteredLease;

/**
 *
 * @author nmafereka
 */
@Stateless
@EJB(name = "java:global/SOLA/BillingEJBLocal", beanInterface = BillingEJBLocal.class)
public class BillingEJB extends AbstractEJB implements BillingEJBLocal {

    @Override
    public CustomerBill saveCustomerBill(CustomerBill bill) {
         if (bill == null) {
            return bill;
        }

        bill = getRepository().saveEntity(bill);

        return bill;
    }

    @Override
    public RegisteredLease getLeaseById(String leaseNumber) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, RegisteredLease.QUERY_WHERE_BYLEASENUMBER);
        params.put(RegisteredLease.QUERY_PARAMETER_LEASE_NUMBER, leaseNumber);
        return getRepository().getEntity(RegisteredLease.class, params);
    }

    @Override
    public List<PayPeriod> getPayPeriod(String languageCode) {
        return getRepository().getCodeList(PayPeriod.class, languageCode);
    }

    @Override
    public List<CalculationType> getCalculationType(String languageCode) {
        return getRepository().getCodeList(CalculationType.class, languageCode);
    }

    @Override
    public boolean isPeriodBilliable(int paymentPeriod, int lastPaymentYear) {

        if (paymentPeriod > lastPaymentYear) {
            return false;
        }

        return true;
    }

    @Override
    public BigDecimal calculateGroundRent(BigDecimal leasedArea, BigDecimal groundRentRate,
            String calculationMethod, int complianceCode) {
        BigDecimal personalLevy;
        personalLevy = new BigDecimal(complianceCode);

        if (calculationMethod.equals(CalculationType.FIXED_VALUE)) {
            return groundRentRate;
        }
        return leasedArea.multiply(groundRentRate).multiply(personalLevy);

    }

    @Override
    public List<PayRate> getPayRate(int billingPeriod, int registrationYear,
            String landUse, String groundRentZone) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, PayRate.QUERY_WHERE_SEARCHBYZONEANDUSEANDPERIOD);
        params.put(PayRate.QUERY_PARAMETER_BILLINGPERIOD, billingPeriod);
        params.put(PayRate.QUERY_PARAMETER_YEAROFREGISTRATION, registrationYear);
        params.put(PayRate.QUERY_PARAMETER_LANDUSE, landUse);
        params.put(PayRate.QUERY_PARAMETER_GROUNDRENTZONE, groundRentZone);
        return getRepository().getEntityList(PayRate.class, params);

    }
}
