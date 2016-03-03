/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sola.services.ejbs.billing.repository.entities;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.entities.AbstractEntity;

/**
 *
 * @author nmafereka
 */
public class CustomerBill extends AbstractEntity {
    
    @Id 
    @Column(name = "bill_number")
    private String billNumber;
    
    @Column(name ="bill_status")
    private String billStatus;
    
    @Column(name = "bill_date")
    private Date billDate;
    
    @Column(name="bill_period")
    private String billPeriod;
    
    @Column(name="lease_number")
    private String leaseNumber;
    
    @ChildEntityList(parentIdField = "billNumber")
    private List<PaymentSchedule> billItemList;

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public String getBillStatus() {
        return billStatus;
    }

    public void setBillStatus(String billStatus) {
        this.billStatus = billStatus;
    }

    public Date getBillDate() {
        return billDate;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
    }

    public String getBillPeriod() {
        return billPeriod;
    }

    public void setBillPeriod(String billPeriod) {
        this.billPeriod = billPeriod;
    }

    public String getLeaseNumber() {
        return leaseNumber;
    }

    public void setLeaseNumber(String leaseNumber) {
        this.leaseNumber = leaseNumber;
    }

    public List<PaymentSchedule> getBillItemList() {
        return billItemList;
    }

    public void setBillItemList(List<PaymentSchedule> billItemList) {
        this.billItemList = billItemList;
    }
    
}
