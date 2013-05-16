/**
 * ******************************************************************************************
 * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations
 * (FAO). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,this
 * list of conditions and the following disclaimer. 2. Redistributions in binary
 * form must reproduce the above copyright notice,this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. 3. Neither the name of FAO nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sola.services.ejb.administrative.repository.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.AccessFunctions;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.RepositoryUtility;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;
import org.sola.services.ejb.system.br.Result;
import org.sola.services.ejb.system.businesslogic.SystemEJBLocal;

/**
 * This Entity represents the administrative.dispute table.
 *
 * @author thoriso
 */
@Table(name = "dispute", schema = "administrative")
@DefaultSorter(sortString = "lodgement_date, nr")
public class Dispute extends AbstractVersionedEntity {
    
    public static final String QUERY_PARAMETER_USERID = "userId";
    public static final String QUERY_WHERE_BYUSERID = "user_id = "
           + "#{" + QUERY_PARAMETER_USERID + "}";
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "nr")
    private String nr;
    @Column(name = "lodgement_date")
    private String lodgementDate;
    @Column(name = "completion_date")
    private String completiondate;
    @Column(name = "dispute_category")
    private String disputeCategory;
    @Column(name = "dispute_type")
    private String disputeType;
    @Column(name = "status_code", updatable = false)
    private String statusCode;
    @Column(name = "application_id")
    private String applicationId;
    @Column(name = "rrr_id")
    private String rrrId;
    @Column(name = "plot_location")
    private String plotLocation;
    @Column(name = "cadastre_object_id")
    private String cadastreObjectId;
    //@Column(name = "user_id")
    private String userId;
    

     public Dispute() {
        super();
    }
    
    private String generateDisputeNumber() {
        String result = "";
        SystemEJBLocal systemEJB = RepositoryUtility.tryGetEJB(SystemEJBLocal.class);
        if (systemEJB != null) {
            Result newNumberResult = systemEJB.checkRuleGetResultSingle("generate-dispute-nr", null);
            if (newNumberResult != null && newNumberResult.getValue() != null) {
                result = newNumberResult.getValue().toString();
            }
        }
        return result;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getCadastreObjectId() {
        return cadastreObjectId;
    }

    public void setCadastreObjectId(String cadastreObjectId) {
        this.cadastreObjectId = cadastreObjectId;
    }

    public String getDisputeCategory() {
        return disputeCategory;
    }

    public void setDisputeCategory(String disputeCategory) {
        this.disputeCategory = disputeCategory;
    }

    public String getDisputeType() {
        return disputeType;
    }

    public void setDisputeType(String disputeType) {
        this.disputeType = disputeType;
    }

    public String getLodgementDate() {
        return lodgementDate;
    }

    public void setLodgementDate(String lodgementDate) {
        this.lodgementDate = lodgementDate;
    }

    public String getCompletiondate() {
        return completiondate;
    }

    public void setCompletiondate(String completiondate) {
        this.completiondate = completiondate;
    }

    public String getNr() {
        return nr;
    }

    public void setNr(String nr) {
        this.nr = nr;
    }

    public String getPlotLocation() {
        return plotLocation;
    }

    public void setPlotLocation(String plotLocation) {
        this.plotLocation = plotLocation;
    }

    public String getRrrId() {
        return rrrId;
    }

    public void setRrrId(String rrrId) {
        this.rrrId = rrrId;
    }

  /*  public String getPendingActionCode() {
        return pendingActionCode;
    }

    public void setPendingActionCode(String pendingActionCode) {
        this.pendingActionCode = pendingActionCode;
    }
    * 
    */

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        // Prevent changes to the status code if the value has been loaded from the database. 
        // Updates to the status code are made via the DisputeStatusChanger
        if (isNew()) {
            this.statusCode = statusCode;
        }
    }

    public String getId() {
        id = id == null ? generateId() : id;
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    //public String getUserId() {
   //     return userId;
   // }

   // public void setUserId(String userId) {
   //     this.userId = userId;
   // }

    @Override
    public void preSave() {

        if (isNew() && getNr() == null) {
            setNr(generateDisputeNumber());
        }
        super.preSave();
    }
}
