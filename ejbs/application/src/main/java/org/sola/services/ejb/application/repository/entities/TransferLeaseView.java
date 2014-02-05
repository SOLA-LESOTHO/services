/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sola.services.ejb.application.repository.entities;

/**
 *
 * @author Charlizza
 */

import javax.persistence.Column;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;
public class TransferLeaseView extends AbstractReadOnlyEntity{
    
    public static final String PARAMETER_FROM = "fromDate";
    
    public static final String PARAMETER_TO = "toDate";
  
    public static final String QUERY_GET_TRANSFERRED_LEASES = 
                    "select * from administrative.transfer_lease_report(#{" + PARAMETER_FROM + "},"
                    + " #{" + PARAMETER_TO + "}) "
                    + "AS TransferReport(service_name varchar, service_count integer, male_count integer, female_count integer, "
                    + "entity_count integer, total_amount float, stamp_duty float, transfer_duty float, registration_fee float)";

    public TransferLeaseView() {
        super();
    }
    
    @Column(name="service_name")
    private String serviceName;
    
    @Column(name="service_count")
    private Integer serviceCount;
    
    @Column(name="male_count")
    private Integer maleCount;
    
    @Column(name="female_count")
    private Integer femaleCount;
    
    @Column(name="entity_count")
    private Integer entityCount;
    
    @Column(name="total_amount")
    private double totalAmount;
    
    @Column(name="stamp_duty")
    private double stampDuty;
    
    @Column(name="transfer_duty")
    private double transferDuty;
    
    @Column(name="registration_fee")
    private double registrationFee;

    public Integer getEntityCount() {
        return entityCount;
    }

    public void setEntityCount(Integer entityCount) {
        this.entityCount = entityCount;
    }

    public Integer getFemaleCount() {
        return femaleCount;
    }

    public void setFemaleCount(Integer femaleCount) {
        this.femaleCount = femaleCount;
    }

    public Integer getMaleCount() {
        return maleCount;
    }

    public void setMaleCount(Integer maleCount) {
        this.maleCount = maleCount;
    }

    public double getRegistrationFee() {
        return registrationFee;
    }

    public void setRegistrationFee(double registrationFee) {
        this.registrationFee = registrationFee;
    }

    public Integer getServiceCount() {
        return serviceCount;
    }

    public void setServiceCount(Integer serviceCount) {
        this.serviceCount = serviceCount;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getStampDuty() {
        return stampDuty;
    }

    public void setStampDuty(double stampDuty) {
        this.stampDuty = stampDuty;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getTransferDuty() {
        return transferDuty;
    }

    public void setTransferDuty(double transferDuty) {
        this.transferDuty = transferDuty;
    }
        
}
