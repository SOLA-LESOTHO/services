package org.sola.services.ejbs.billing.repository.entities;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.entities.AbstractEntity;

/**
 * Represents pay rate.
 */
@Table(name = "pay_rate", schema = "billing")
public class PayRate extends AbstractEntity {

    public PayRate() {
        super();
    }

    public static final String QUERY_PARAMETER_BILLINGPERIOD = "billingPerid";
    
    public static final String QUERY_PARAMETER_YEAROFREGISTRATION = "yearOfRegistration";
    
    public static final String QUERY_PARAMETER_LANDUSE = "landUse";
    
    public static final String QUERY_PARAMETER_GROUNDRENTZONE = "groundRentZone";
    
    /**
     * WHERE clause to return current CO's intersecting the specified point
     */
    public static final String QUERY_WHERE_SEARCHBYZONEANDUSEANDPERIOD = 
            "pay_period_code between #{" + QUERY_PARAMETER_BILLINGPERIOD + "} AND "
            + "#{" + QUERY_PARAMETER_YEAROFREGISTRATION + "}  AND "
            + "land_use_code = #{" + QUERY_PARAMETER_LANDUSE + "} AND "
            + "land_grade_code = #{" + QUERY_PARAMETER_GROUNDRENTZONE + "}";

    @Id
    @Column(name = "land_use_code")
    private String landUseCode;

    @Id
    @Column(name = "land_grade_code")
    private String landGradeCode;

    @Id
    @Column(name = "pay_period_code")
    private int payPeriodCode;

    @Column(name = "rate_amount")
    BigDecimal rateAmount;

    @Column(name = "calculation_type_code")
    private String calculationTypeCode;

    public String getLandUseCode() {
        return landUseCode;
    }

    public void setLandUseCode(String landUseCode) {
        this.landUseCode = landUseCode;
    }

    public String getLandGradeCode() {
        return landGradeCode;
    }

    public void setLandGradeCode(String landGradeCode) {
        this.landGradeCode = landGradeCode;
    }

    public int getPayPeriodCode() {
        return payPeriodCode;
    }

    public void setPayPeriodCode(int payPeriodCode) {
        this.payPeriodCode = payPeriodCode;
    }

    public BigDecimal getRateAmount() {
        return rateAmount;
    }

    public void setRateAmount(BigDecimal rateAmount) {
        this.rateAmount = rateAmount;
    }

    public String getCalculationTypeCode() {
        return calculationTypeCode;
    }

    public void setCalculationTypeCode(String calculationTypeCode) {
        this.calculationTypeCode = calculationTypeCode;
    }

}
