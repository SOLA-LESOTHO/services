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
package org.sola.services.ejb.search.repository.entities;

import javax.persistence.Column;

import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;

@Table(name = "application_stage_type", schema = "application")
public class ApplicationStageSearchResult extends AbstractReadOnlyEntity {
    
    protected static final String SELECT_QUERY = 
            "SELECT DISTINCT s.code, s.display_value, s.description, "
            + "(SELECT string_agg(tmp.appgroup_id, ', ') FROM "
            + "(SELECT appgroup_id FROM system.appstage_appgroup ag INNER JOIN application.application_stage_type st "
            + "ON st.code = ag.appstage_code WHERE st.code = s.code) tmp "
            + ") AS groups_list "
            + "FROM application.application_stage_type s "
            + "LEFT JOIN system.appstage_appgroup ag1 "
            + "ON s.code = ag1.appstage_code ";
    
    public static final String QUERY_ADVANCED_STAGE_SEARCH = ApplicationStageSearchResult.SELECT_QUERY
            //+ "WHERE s.code = #{code}";
            + "WHERE EXISTS ( "
            + "SELECT st.appstage_code "
            + "FROM system.appstage_appgroup st "
            + "WHERE st.appgroup_id = #{groupId} "
            + "AND st.appstage_code = s.code) ";
    
    public static final String QUERY_ALL_STAGES_SEARCH = ApplicationStageSearchResult.SELECT_QUERY
            + "WHERE TRUE";
    
    @Column(name="code")
    String code;
    @Column(name="display_value")
    String displayValue;
    @Column(name="description")
    String description;
    @Column(name="groups_list")
    String groupsList;
    
    public ApplicationStageSearchResult() {
        super();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getGroupsList() {
        return groupsList;
    }

    public void setGroupsList(String groupsList) {
        this.groupsList = groupsList;
    }               
        
}
