/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sola.services.ejbs.billing.repository.entities;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Id;
import org.sola.services.common.repository.entities.AbstractEntity;

/**
 *
 * @author nmafereka
 */
public class RegisteredLease extends AbstractEntity {

    public RegisteredLease() {
        super();
    }

    public static final String QUERY_PARAMETER_LEASE_NUMBER = "leaseNumber";

    public static final String QUERY_WHERE_BYLEASENUMBER
            = "lease_number = #{" + QUERY_PARAMETER_LEASE_NUMBER + "} ";

    @Id
    @Column(name = "lease_number")
    private String leaseNumber;

    @Column(name = "land_use_code")
    private String landUseCode;

    @Column(name = "ground_rent_zone")
    private String groundRentZone;

    @Column(name = "leased_area")
    private BigDecimal leasedArea;

    @Column(name = "registration_date")
    private Date registrationDate;

    @Column(name = "last_payment_date")
    private Date lastPaymentDate;

    @Column(name = "compliance_code")
    private int complianceCode;

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

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Date getLastPaymentDate() {
        return lastPaymentDate;
    }

    public void setLastPaymentDate(Date lastPaymentDate) {
        this.lastPaymentDate = lastPaymentDate;
    }

    public int getComplianceCode() {
        return complianceCode;
    }

    public void setComplianceCode(int complianceCode) {
        this.complianceCode = complianceCode;
    }

}
