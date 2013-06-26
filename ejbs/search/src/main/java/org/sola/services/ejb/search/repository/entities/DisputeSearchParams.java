/**
 * ******************************************************************************************
 * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice,this list
 *       of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice,this list
 *       of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
 *       promote products derived from this software without specific prior written permission.
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
package org.sola.services.ejb.search.repository.entities;

import org.sola.services.common.repository.entities.AbstractEntity;
import java.util.Date;

public class DisputeSearchParams extends AbstractEntity {
    
    private String nr;
    private Date lodgementDateFrom;
    private Date lodgementDateTo;
    private Date completionDateFrom;
    private Date completionDateTo;
    private String leaseNumber;
    private String plotNumber;
    private String caseType;
 
    
    public DisputeSearchParams(){
        super();
    }

    public Date getCompletionDateFrom() {
        return completionDateFrom;
    }

    public void setCompletionDateFrom(Date completionDateFrom) {
        this.completionDateFrom = completionDateFrom;
    }

    public Date getCompletionDateTo() {
        return completionDateTo;
    }

    public void setCompletionDateTo(Date completionDateTo) {
        this.completionDateTo = completionDateTo;
    }

    public String getLeaseNumber() {
        return leaseNumber;
    }

    public void setLeaseNumber(String leaseNumber) {
        this.leaseNumber = leaseNumber;
    }

    public Date getLodgementDateFrom() {
        return lodgementDateFrom;
    }

    public void setLodgementDateFrom(Date lodgementDateFrom) {
        this.lodgementDateFrom = lodgementDateFrom;
    }

    public Date getLodgementDateTo() {
        return lodgementDateTo;
    }

    public void setLodgementDateTo(Date lodgementDateTo) {
        this.lodgementDateTo = lodgementDateTo;
    }

    public String getNr() {
        return nr;
    }

    public void setNr(String nr) {
        this.nr = nr;
    }

    public String getPlotNumber() {
        return plotNumber;
    }

    public void setPlotNumber(String plotNumber) {
        this.plotNumber = plotNumber;
    }

    public String getCaseType() {
        return caseType;
    }

    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }

}
