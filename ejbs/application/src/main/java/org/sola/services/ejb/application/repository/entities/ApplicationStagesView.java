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
public class ApplicationStagesView extends AbstractReadOnlyEntity{
    
    public static final String PARAMETER_FROM = "fromDate";
    
    public static final String PARAMETER_TO = "toDate"; 
    
    public static final String PARAMETER_CATEGORY_CODE = "requestCategoryCode";
    
    public static final String QUERY_GET_APPLICATION_STAGES_REPORT = 
                    "select * from application.application_stages_report(#{" + PARAMETER_FROM + "},"
                    + " #{" + PARAMETER_TO + "},"
                    + " #{" + PARAMETER_CATEGORY_CODE + "})";
    
    
    @Column(name="application")
    private String application;
    
    @Column(name="app_lodged")
    private Integer appLodged;
    
    @Column(name="to_be_processed")
    private Integer toBeProcessed;
    
    @Column(name="missing_plot")
    private Integer missingPlot;
    
    @Column(name="area_mismatch")
    private Integer areaMismatch;
    
    @Column(name="aqueried")
    private Integer queried;
    
    @Column(name="bind_draft")
    private Integer bindDraft;
    
    @Column(name="check_draft")
    private Integer checkDraft;
    
    @Column(name="log_draft")
    private Integer logDraft;
    
    @Column(name="executive_to_sign")
    private Integer executiveToSign;
    
    @Column(name="app_to_be_approved")
    private Integer awaitingApproval;
    
    @Column(name="customer_to_sign")
    private Integer customerToSign;
    
    @Column(name="to_be_archived")
    private Integer toBeArchived;
    
    @Column(name="call_customer")
    private Integer callCustomer;
    
    @Column(name="collected_by_customer")
    private Integer collectedByCustomer;
    
    @Column(name="to_be_registered")
    private Integer toBeRegistered;

    public ApplicationStagesView() {
    }

    public Integer getAppLodged() {
        return appLodged;
    }

    public void setAppLodged(Integer appLodged) {
        this.appLodged = appLodged;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public Integer getAreaMismatch() {
        return areaMismatch;
    }

    public void setAreaMismatch(Integer areaMismatch) {
        this.areaMismatch = areaMismatch;
    }

    public Integer getAwaitingApproval() {
        return awaitingApproval;
    }

    public void setAwaitingApproval(Integer awaitingApproval) {
        this.awaitingApproval = awaitingApproval;
    }

    public Integer getBindDraft() {
        return bindDraft;
    }

    public void setBindDraft(Integer bindDraft) {
        this.bindDraft = bindDraft;
    }

    public Integer getCallCustomer() {
        return callCustomer;
    }

    public void setCallCustomer(Integer callCustomer) {
        this.callCustomer = callCustomer;
    }

    public Integer getCheckDraft() {
        return checkDraft;
    }

    public void setCheckDraft(Integer checkDraft) {
        this.checkDraft = checkDraft;
    }

    public Integer getCollectedByCustomer() {
        return collectedByCustomer;
    }

    public void setCollectedByCustomer(Integer collectedByCustomer) {
        this.collectedByCustomer = collectedByCustomer;
    }

    public Integer getCustomerToSign() {
        return customerToSign;
    }

    public void setCustomerToSign(Integer customerToSign) {
        this.customerToSign = customerToSign;
    }

    public Integer getExecutiveToSign() {
        return executiveToSign;
    }

    public void setExecutiveToSign(Integer executiveToSign) {
        this.executiveToSign = executiveToSign;
    }

    public Integer getLogDraft() {
        return logDraft;
    }

    public void setLogDraft(Integer logDraft) {
        this.logDraft = logDraft;
    }

    public Integer getMissingPlot() {
        return missingPlot;
    }

    public void setMissingPlot(Integer missingPlot) {
        this.missingPlot = missingPlot;
    }

    public Integer getQueried() {
        return queried;
    }

    public void setQueried(Integer queried) {
        this.queried = queried;
    }

    public Integer getToBeArchived() {
        return toBeArchived;
    }

    public void setToBeArchived(Integer toBeArchived) {
        this.toBeArchived = toBeArchived;
    }

    public Integer getToBeProcessed() {
        return toBeProcessed;
    }

    public void setToBeProcessed(Integer toBeProcessed) {
        this.toBeProcessed = toBeProcessed;
    }

    public Integer getToBeRegistered() {
        return toBeRegistered;
    }

    public void setToBeRegistered(Integer toBeRegistered) {
        this.toBeRegistered = toBeRegistered;
    }
       
}
