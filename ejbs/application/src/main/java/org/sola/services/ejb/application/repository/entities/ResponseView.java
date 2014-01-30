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
package org.sola.services.ejb.application.repository.entities;

import java.util.Date;
import javax.persistence.Column;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;

/**
 *
 * @author ntsane
 */
public class ResponseView  extends AbstractReadOnlyEntity{
    
    public static final String PARAMETER_FROM = "fromDate";
    
    public static final String PARAMETER_TO = "toDate";  
    
    public static final String PARAMETER_CATEGORY_CODE = "requestCategoryCode";
    
    public static final String QUERY_GET_RESPONSE = 
            "select * from application.getResponseTime(#{" + PARAMETER_FROM + "},"
            + " #{" + PARAMETER_TO + "},"
            + " #{" + PARAMETER_CATEGORY_CODE + "})"
            + " AS ResponseTimeReport(request_type varchar, service_count integer, "
            + "total_time integer, average_time float, frequent_day integer, std_deviation integer "
	    + ",min_days integer, max_days integer, range integer)";
    
    public static final String QUERY_GET_RESPONSE2 = 
            "select * from application.getResponseTime(#{" + PARAMETER_FROM + "},"
            + " #{" + PARAMETER_TO + "})"                   
            + " AS ResponseTimeReport(request_type varchar, service_count integer, "
            + "total_time integer, average_time float, frequent_day integer, std_deviation integer "
	    + ",min_days integer, max_days integer, range integer)";
    
    @Column(name="request_type")
    private String requestType;
    @Column(name="service_count")
    private Integer serviceCount;
    @Column(name="total_time")
    private Integer totalTime;
    @Column(name="average_time")
    private double averageTime;
    @Column(name="frequent_day")
    private Integer frequentDay;
    @Column(name="std_deviation")
    private Integer stdDeviation;
    @Column(name="min_days")
    private Integer minDays;
    @Column(name="max_days")
    private Integer maxDays;
    @Column(name="range")
    private Integer range;

    public double getAverageTime() {
        return averageTime;
    }

    public Integer getFrequentDay() {
        return frequentDay;
    }

    public void setFrequentDay(Integer frequentDay) {
        this.frequentDay = frequentDay;
    }

    public Integer getMaxDays() {
        return maxDays;
    }

    public void setMaxDays(Integer maxDays) {
        this.maxDays = maxDays;
    }

    public Integer getMinDays() {
        return minDays;
    }

    public void setMinDays(Integer minDays) {
        this.minDays = minDays;
    }

    public Integer getRange() {
        return range;
    }

    public void setRange(Integer range) {
        this.range = range;
    }

    public Integer getStdDeviation() {
        return stdDeviation;
    }

    public void setStdDeviation(Integer stdDeviation) {
        this.stdDeviation = stdDeviation;
    }

    public void setAverageTime(double averageTime) {
        this.averageTime = averageTime;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public Integer getServiceCount() {
        return serviceCount;
    }

    public void setServiceCount(Integer serviceCount) {
        this.serviceCount = serviceCount;
    }

    public Integer getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Integer totalTime) {
        this.totalTime = totalTime;
    }
    
}
