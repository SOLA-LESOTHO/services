/**
 * ******************************************************************************************
 * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO). All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,this list of conditions
 * and the following disclaimer. 2. Redistributions in binary form must reproduce the above
 * copyright notice,this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution. 3. Neither the name of FAO nor the names of its
 * contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
/**
 *
 * @author thoriso - LAA
 */
package org.sola.services.ejb.search.repository.entities;

import java.util.Date;
import javax.persistence.*;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;

@Table(name = "dispute", schema = "administrative")
public class DisputeSearchResult extends AbstractReadOnlyEntity {
    
    public static final String QUERY_PARAM_DISP_NR = "nr";
    public static final String QUERY_PARAM_LEASE_NR = "leaseNr";
    public static final String QUERY_PARAM_PLOT_NR = "plotNr";
    public static final String QUERY_PARAM_LODGEMENT_DATE_FROM = "lodgeDateFrom";
    public static final String QUERY_PARAM_LODGEMENT_DATE_TO = "lodgementDateTo";
    public static final String QUERY_PARAM_COMPLETION_DATE_FROM = "completionDateFrom";
    public static final String QUERY_PARAM_COMPLETION_DATE_TO = "completionDateTo";
    public static final String QUERY_ORDER_BY = "disp.nr";
    
     
    public static final String SELECT_PART = "SELECT disp.id, disp.nr,disp.lodgement_date,disp.completion_date, "
                               + "disp.rrr_id, disp.cadastre_object_id";
    public static final String FROM_PART = " FROM administrative.dispute disp";
    public static final String WHERE_PART = " WHERE compare_strings(#{" + QUERY_PARAM_DISP_NR + "}, COALESCE(disp.nr, ''))"
                              + " AND compare_strings(#{" + QUERY_PARAM_LEASE_NR + "}, COALESCE(disp.rrr_id, ''))"
                              + " AND compare_strings(#{" + QUERY_PARAM_PLOT_NR + "}, COALESCE(disp.cadastre_object_id, ''))"
                              + " AND disp.lodgement_date BETWEEN #{" + QUERY_PARAM_LODGEMENT_DATE_FROM + "} AND #{" + QUERY_PARAM_LODGEMENT_DATE_TO + "}"
                              + " AND disp.completion_date BETWEEN #{" + QUERY_PARAM_COMPLETION_DATE_FROM + "} AND #{" + QUERY_PARAM_COMPLETION_DATE_TO + "}";
    public static final String SEARCH_QUERY = SELECT_PART + FROM_PART + WHERE_PART + "LIMIT 101";
    
    
    
    @Id
    @Column
    private String id;
    @Column(name = "nr")
    private String nr;
    @Column(name = "lodgement_date")
    @Temporal(TemporalType.DATE)
    private Date lodgementDate;
    @Column(name = "completion_date")
    @Temporal(TemporalType.DATE)
    private Date completiondate;
    @Column(name = "rrr_id")
    private String leaseNr;
    @Column(name = "cadastre_object_id")
    private String plotNr;

    public Date getCompletiondate() {
        return completiondate;
    }

    public void setCompletiondate(Date completiondate) {
        this.completiondate = completiondate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLeaseNr() {
        return leaseNr;
    }

    public void setLeaseNr(String leaseNr) {
        this.leaseNr = leaseNr;
    }

    public Date getLodgementDate() {
        return lodgementDate;
    }

    public void setLodgementDate(Date lodgementDate) {
        this.lodgementDate = lodgementDate;
    }

    public String getNr() {
        return nr;
    }

    public void setNr(String nr) {
        this.nr = nr;
    }

    public String getPlotNr() {
        return plotNr;
    }

    public void setPlotNr(String plotNr) {
        this.plotNr = plotNr;
    }

}

