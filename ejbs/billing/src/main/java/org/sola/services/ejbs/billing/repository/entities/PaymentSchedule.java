/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sola.services.ejbs.billing.repository.entities;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Table;
import org.sola.services.common.repository.entities.AbstractEntity;

/**
 *
 * @author nmafereka
 */
@Table(name = "payment_schedule", schema = "billing")
public class PaymentSchedule extends AbstractEntity {
    
   @Column(name = "billing_period")
   private String billingPeriod;
   
   @Column(name="bill_number")
   private String billNumber;
   
   @Column(name = "lease_number")
   private String leaseNumber;
   
   @Column(name = "land_use_code")
   private String landUseCode;
   
   @Column(name = "ground_rent_zone")
   private String groundRentZone;
   
   @Column(name = "leased_area")
   private BigDecimal leasedArea; //subject to change
   
   @Column(name = "pay_period_code")
   private String payPeriodCode;
   
   @Column(name = "calculation_method")
   private String calculationMethod;
   
   @Column(name = "impose_penalty")
   private int imposePenalty; //for previos transactions
   
   @Column(name = "compliance_code")
   private int complianceCode; //defaults to 1 or 2 for non-compliance
   
   @Column(name = "status_code")
   private String statusCode; //to determine if the bill is still relevant

    public String getBillingPeriod() {
        return billingPeriod;
    }

    public void setBillingPeriod(String billingPeriod) {
        this.billingPeriod = billingPeriod;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public String getLeaseNumber() {
        return leaseNumber;
    }

    public void setLeaseNumber(String leaseNumber) {
        this.leaseNumber = leaseNumber;
    }

    public String getLandUseCode() {
        return landUseCode;
    }

    public void setLandUseCode(String landUseCode) {
        this.landUseCode = landUseCode;
    }

    public String getGroundRentZone() {
        return groundRentZone;
    }

    public void setGroundRentZone(String groundRentZone) {
        this.groundRentZone = groundRentZone;
    }

    public BigDecimal getLeasedArea() {
        return leasedArea;
    }

    public void setLeasedArea(BigDecimal leasedArea) {
        this.leasedArea = leasedArea;
    }

    public String getPayPeriodCode() {
        return payPeriodCode;
    }

    public void setPayPeriodCode(String payPeriodCode) {
        this.payPeriodCode = payPeriodCode;
    }

    public String getCalculationMethod() {
        return calculationMethod;
    }

    public void setCalculationMethod(String calculationMethod) {
        this.calculationMethod = calculationMethod;
    }

    public int getImposePenalty() {
        return imposePenalty;
    }

    public void setImposePenalty(int imposePenalty) {
        this.imposePenalty = imposePenalty;
    }

    public int getComplianceCode() {
        return complianceCode;
    }

    public void setComplianceCode(int complianceCode) {
        this.complianceCode = complianceCode;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
   
}
