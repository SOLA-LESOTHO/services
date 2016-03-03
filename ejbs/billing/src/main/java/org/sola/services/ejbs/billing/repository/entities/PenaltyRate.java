/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sola.services.ejbs.billing.repository.entities;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.entities.AbstractEntity;

/**
 *
 * @author nmafereka
 */
@Table(name = "penalty_rate", schema = "billing")
public class PenaltyRate extends AbstractEntity {

    public PenaltyRate() {
        super();
    }

    @Id
    @Column(name = "land_use_code")
    private String landUseCode;

    @Id
    @Column(name = "land_grade_code")
    private String landGradeCode;

    @Id
    @Column(name = "pay_period_code")
    private String payPeriodCode;

    @Column(name = "penalty_levy")
    BigDecimal penaltyLevy;

    @Column(name = "minimum_penalty")
    BigDecimal minimumPenalty;

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

    public String getPayPeriodCode() {
        return payPeriodCode;
    }

    public void setPayPeriodCode(String payPeriodCode) {
        this.payPeriodCode = payPeriodCode;
    }

    public BigDecimal getPenaltyLevy() {
        return penaltyLevy;
    }

    public void setPenaltyLevy(BigDecimal penaltyLevy) {
        this.penaltyLevy = penaltyLevy;
    }

    public BigDecimal getMinimumPenalty() {
        return minimumPenalty;
    }

    public void setMinimumPenalty(BigDecimal minimumPenalty) {
        this.minimumPenalty = minimumPenalty;
    }

    public String getCalculationTypeCode() {
        return calculationTypeCode;
    }

    public void setCalculationTypeCode(String calculationTypeCode) {
        this.calculationTypeCode = calculationTypeCode;
    }

}
