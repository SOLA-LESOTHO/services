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
public class CustomerServicesView extends AbstractReadOnlyEntity{
    
    public static final String PARAMETER_FROM = "fromDate";
    
    public static final String PARAMETER_TO = "toDate";  
    
    public static final String QUERY_GET_CUSTOMER_SERVICES_REPORT = 
                    "select * from application.customer_services_report(#{" + PARAMETER_FROM + "},"
                    + " #{" + PARAMETER_TO + "})";
    
    
    @Column(name="application")
    private String application;
    @Column(name="lodged")
    private Integer lodged;
    @Column(name="queried")
    private double queried;
    @Column(name="awaitingcollection")
    private Integer awaitingcollection;
    @Column(name="collected")
    private Integer collected;

    public CustomerServicesView() {
        
        super();
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public Integer getAwaitingcollection() {
        return awaitingcollection;
    }

    public void setAwaitingcollection(Integer awaiting_collection) {
        this.awaitingcollection = awaiting_collection;
    }

    public Integer getCollected() {
        return collected;
    }

    public void setCollected(Integer collected) {
        this.collected = collected;
    }

    public Integer getLodged() {
        return lodged;
    }

    public void setLodged(Integer lodged) {
        this.lodged = lodged;
    }

    public double getQueried() {
        return queried;
    }

    public void setQueried(double queried) {
        this.queried = queried;
    }    
    
}