/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sola.services.ejb.application.repository.entities;

import javax.persistence.Column;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;

/**
 *
 * @author Charlizza
 */
public class MortgageStatsView extends AbstractReadOnlyEntity{
    
    public static final String PARAMETER_FROM = "fromDate";
    
    public static final String PARAMETER_TO = "toDate";
  
    
    public static final String QUERY_GET_MORTGAGE_STATS = 
                        "select * from application.getmortgagestats(#{" + PARAMETER_FROM + "},"
                    + " #{" + PARAMETER_TO + "})"
                    + " AS MortgageStatsReport(mortgages varchar, amount float,"
                    + " average_amount float)";
    
    @Column(name="mortgages")
    private Integer mortgages;
    @Column(name="amount")
    private Integer amount;
    @Column(name="average_amount")
    private double averageAmount;

    public MortgageStatsView() {
        super();
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public double getAverageAmount() {
        return averageAmount;
    }

    public void setAverageAmount(double averageAmount) {
        this.averageAmount = averageAmount;
    }

    public Integer getMortgages() {
        return mortgages;
    }

    public void setMortgages(Integer mortgages) {
        this.mortgages = mortgages;
    }
        
}
