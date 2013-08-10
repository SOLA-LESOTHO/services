/**
 * ******************************************************************************************
 * Copyright (c) 2013 Food and Agriculture Organization of the United Nations (FAO)
 * and the Lesotho Land Administration Authority (LAA). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice,this list
 *       of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice,this list
 *       of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *    3. Neither the names of FAO, the LAA nor the names of its contributors may be used to
 *       endorse or promote products derived from this software without specific prior
 * 	  written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.ExternalEJB;
import org.sola.services.common.repository.RepositoryUtility;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;
import org.sola.services.ejb.cadastre.businesslogic.CadastreEJBLocal;
import org.sola.services.ejb.source.businesslogic.SourceEJBLocal;
import org.sola.services.ejb.source.repository.entities.Source;
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
    @Column(name = "lodgement_date", updatable = false, insertable = false)
    private Date lodgementDate;
    @Column(name = "completion_date", updatable = false, insertable = false)
    private Date completionDate;
    @Column(name = "dispute_category_code")
    private String disputeCategoryCode;
    @Column (name  = "dispute_description")
    private String disputeDescription;
    @Column(name = "dispute_type_code")
    private String disputeTypeCode;
    @Column(name = "status_code")
    private String statusCode;
    @Column(name = "rrr_id")
    private String leaseNumber;
    @Column(name = "plot_location")
    private String plotLocation;
    @Column(name = "cadastre_object_id")
    private String plotNumber;
    @Column(name = "casetype")
    private String caseType;
    @Column(name = "primary_respondent")
    private boolean primaryRespondent;
    @Column(name = "action_required")
    private String actionRequired;
    @ChildEntityList(parentIdField = "disputeNr")
    private List<DisputeComments> disputeCommentsList;
    @ChildEntityList(parentIdField = "disputeNr")
    private List<DisputeParty> disputePartyList;
    @ExternalEJB(ejbLocalClass = SourceEJBLocal.class,
    loadMethod = "getSources", saveMethod = "saveSource")
    @ChildEntityList(parentIdField = "disputeId", childIdField = "sourceId",
    manyToManyClass = DisputeSource.class)
    private List<Source> sourceList;

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

    public String getDisputeDescription() {
        return disputeDescription;
    }

    public void setDisputeDescription(String disputeDescription) {
        this.disputeDescription = disputeDescription;
    }

    public String getCaseType() {
        return caseType;
    }

    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }

    public String getActionRequired() {
        return actionRequired;
    }

    public void setActionRequired(String actionRequired) {
        this.actionRequired = actionRequired;
    }

    public boolean isPrimaryRespondent() {
        return primaryRespondent;
    }

    public void setPrimaryRespondent(boolean primaryRespondent) {
        this.primaryRespondent = primaryRespondent;
    }

    public String getPlotNumber() {
        return plotNumber;
    }

    public void setPlotNumber(String plotNumber) {
        this.plotNumber = plotNumber;
    }

    public String getDisputeCategoryCode() {
        return disputeCategoryCode;
    }

    public void setDisputeCategoryCode(String disputeCategoryCode) {
        this.disputeCategoryCode = disputeCategoryCode;
    }

    public String getDisputeTypeCode() {
        return disputeTypeCode;
    }

    public void setDisputeTypeCode(String disputeTypeCode) {
        this.disputeTypeCode = disputeTypeCode;
    }

    public Date getLodgementDate() {
        return lodgementDate;
    }

    public void setLodgementDate(Date lodgementDate) {
        this.lodgementDate = lodgementDate;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
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

    public String getLeaseNumber() {
        return leaseNumber;
    }

    public void setLeaseNumber(String leaseNumber) {
        this.leaseNumber = leaseNumber;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getId() {
        id = id == null ? generateId() : id;
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<DisputeComments> getDisputeCommentsList() {
        disputeCommentsList = disputeCommentsList == null ? new ArrayList<DisputeComments>() : disputeCommentsList;
        return disputeCommentsList;
    }

    public void setDisputeCommentsList(List<DisputeComments> disputeCommentsList) {
        this.disputeCommentsList = disputeCommentsList;
    }

    public List<DisputeParty> getDisputePartyList() {
        disputePartyList = disputePartyList == null ? new ArrayList<DisputeParty>() : disputePartyList;
        return disputePartyList;
    }

    public void setDisputePartyList(List<DisputeParty> disputePartyList) {
        this.disputePartyList = disputePartyList;
    }

    public List<Source> getSourceList() {
        sourceList = sourceList == null ? new ArrayList<Source>() : sourceList;
        return sourceList;
    }

    public void setSourceList(List<Source> sourceList) {
        this.sourceList = sourceList;
    }
   
    @Override
    public void preSave() {
        if (this.isNew() && this.getNr() == null) {
            setNr(generateDisputeNumber());
        }
        super.preSave();
    }
}
