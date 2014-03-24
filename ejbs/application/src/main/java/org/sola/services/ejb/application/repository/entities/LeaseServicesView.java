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
public class LeaseServicesView extends AbstractReadOnlyEntity{
    
    public static final String PARAMETER_FROM = "fromDate";
    
    public static final String PARAMETER_TO = "toDate";  
    
    public static final String QUERY_GET_LEASE_SERVICES_REPORT = 
                    "select * from application.lease_services_report(#{" + PARAMETER_FROM + "},"
                    + " #{" + PARAMETER_TO + "})";
    
    
    @Column(name="application")
    private String application;
    @Column(name="tobeprocessed")
    private Integer tobeprocessed;
    @Column(name="inprogress")
    private Integer inprogress;
    @Column(name="queried")
    private Integer queried;
    @Column(name="cancelled")
    private Integer cancelled;
    @Column(name="processed")
    private Integer processed;
    @Column(name="approved")
    private Integer approved;
    @Column(name="overdue")
    private Integer overdue;

    public LeaseServicesView() {
        super();
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public Integer getApproved() {
        return approved;
    }

    public void setApproved(Integer approved) {
        this.approved = approved;
    }

    public Integer getCancelled() {
        return cancelled;
    }

    public void setCancelled(Integer cancelled) {
        this.cancelled = cancelled;
    }

    public Integer getInprogress() {
        return inprogress;
    }

    public void setInprogress(Integer in_progress) {
        this.inprogress = in_progress;
    }

    public Integer getOverdue() {
        return overdue;
    }

    public void setOverdue(Integer overdue) {
        this.overdue = overdue;
    }

    public Integer getProcessed() {
        return processed;
    }

    public void setProcessed(Integer processed) {
        this.processed = processed;
    }

    public Integer getQueried() {
        return queried;
    }

    public void setQueried(Integer queried) {
        this.queried = queried;
    }

    public Integer getTobeprocessed() {
        return tobeprocessed;
    }

    public void setTobeprocessed(Integer to_be_processed) {
        this.tobeprocessed = to_be_processed;
    }    
    
}
