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
package org.sola.services.ejb.application.repository.entities;

import javax.persistence.Column;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;

/**
 *
 * @author ntsane
 */
public class StatisticalView extends AbstractReadOnlyEntity{
    
    public static final String PARAMETER_FROM = "fromDate";
    public static final String PARAMETER_TO = "toDate";
    public static final String PARAMETER_CATEGORY_CODE = "requestCategoryCode";
    
    public static final String QUERY_GETSTATISTICS = 
            "application.get_work_statistics(#{" + PARAMETER_FROM + "}, #{" + PARAMETER_TO + "}) ";
    
    public static final String QUERY_GETSTATISTICS2 = 
            "application.get_work_statistics(#{" + PARAMETER_FROM + "}, #{" + PARAMETER_TO + "}, #{" + PARAMETER_CATEGORY_CODE + "}) ";
   
    @Column(name = "req_type")
    private String requestType;
  
    @Column(name = "req_cat")
    private String requestCategory;
  
    @Column(name = "group_idx")
    private int groupIndex;
    
    @Column(name = "lodged")
    private int lodgedApplications;
    
    @Column(name = "cancelled")
    private int cancelledApplications;
    
    @Column(name = "completed")
    private int completedApplications;

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestCategory() {
        return requestCategory;
    }

    public void setRequestCategory(String requestCategory) {
        this.requestCategory = requestCategory;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    public int getLodgedApplications() {
        return lodgedApplications;
    }

    public void setLodgedApplications(int lodgedApplications) {
        this.lodgedApplications = lodgedApplications;
    }

    public int getCancelledApplications() {
        return cancelledApplications;
    }

    public void setCancelledApplications(int cancelledApplications) {
        this.cancelledApplications = cancelledApplications;
    }

    public int getCompletedApplications() {
        return completedApplications;
    }

    public void setCompletedApplications(int completedApplications) {
        this.completedApplications = completedApplications;
    }
    
}
