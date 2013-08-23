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

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

public class CadastreObjectSearchResult extends AbstractVersionedEntity {

    @Column(name = "id")
    private String id;
    @Column(name = "type_code")
    private String typeCode;
    @Column(name = "approval_datetime")
    private Date approvalDatetime;
    @Column(name = "historic_datetime")
    private Date historicDatetime;
    @Column(name = "source_reference")
    private String sourceReference;
    @Column(name = "valuation_amount")
    private BigDecimal valuationAmount;
    @Column(name = "name_firstpart")
    private String nameFirstpart;
    @Column(name = "name_lastpart")
    private String nameLastpart;
    @Column(name = "status_code")
    private String statusCode;
    @Column(name = "land_grade_code")
    private String landGradeCode;
    @Column(name = "survey_date")
    private Date surveyDate;
    @Column(name = "survey_fee")
    private BigDecimal surveyFee;
    @Column(name = "surveyor_id")
    private String surveyorId;
    @Column(name = "surveyor")
    private String surveyor;
    @Column(name = "remarks")
    private String remarks;
    @Column(name = "address")
    private String address;
    @Column(name = "area")
    private BigDecimal area;
    @Column(name = "valuation_zone")
    private String valuationZone;
    @Column(name = "has_lease")
    private boolean hasLease;
    @Column(name = "has_dispute")
    private boolean hasDispute;
    
    public static final String PARAM_NAME_FIRST_PART = "nameFirstPart";
    public static final String PARAM_NAME_LAST_PART = "nameLastPart";
    public static final String PARAM_ADDRESS = "address";
    public static final String QUERY_SEARCH = ""
            + "SELECT DISTINCT co.id, co.type_code, co.name_firstpart, co.name_lastpart, co.approval_datetime, co.historic_datetime, "
            + "(SELECT size FROM cadastre.spatial_value_area cov WHERE cov.spatial_unit_id = co.id AND type_code='officialArea' LIMIT 1) AS area, "
            + "(SELECT string_agg(ad.description, ', ') FROM address.address ad INNER JOIN cadastre.spatial_unit_address sad "
            + "  ON ad.id = sad.spatial_unit_id WHERE sa.spatial_unit_id = co.id) AS address, "
            + "co.source_reference, co.land_grade_code, co.valuation_amount, co.valuation_zone, "
            + "(SELECT COUNT(1)>0 FROM administrative.ba_unit where cadastre_object_id = co.id AND status_code!='historic') AS has_lease,"
            + "(SELECT COUNT(1)>0 FROM administrative.dispute where cadastre_object_id = (name_firstpart||'-'||name_lastpart) AND status_code!='Resolved') AS has_dispute,"
            + "co.status_code, co.rowversion, co.change_user, co.rowidentifier "
            + "FROM cadastre.cadastre_object co LEFT JOIN cadastre.spatial_unit_address sa ON co.id = sa.spatial_unit_id "
            + "LEFT JOIN address.address a ON sa.address_id = a.id "
            + "WHERE POSITION(LOWER(#{" + PARAM_NAME_FIRST_PART + "}) IN LOWER(co.name_firstpart)) > 0 AND "
            + "POSITION(LOWER(#{" + PARAM_NAME_LAST_PART + "}) IN LOWER(co.name_lastpart)) > 0 AND "
            + "POSITION(LOWER(#{" + PARAM_ADDRESS + "}) IN LOWER(COALESCE(a.description, ''))) > 0 "
            + "ORDER BY co.name_firstpart, co.name_lastpart "
            + "LIMIT 100";

    public CadastreObjectSearchResult() {
        super();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getApprovalDatetime() {
        return approvalDatetime;
    }

    public void setApprovalDatetime(Date approvalDatetime) {
        this.approvalDatetime = approvalDatetime;
    }

    public Date getHistoricDatetime() {
        return historicDatetime;
    }

    public void setHistoricDatetime(Date historicDatetime) {
        this.historicDatetime = historicDatetime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLandGradeCode() {
        return landGradeCode;
    }

    public void setLandGradeCode(String landGradeCode) {
        this.landGradeCode = landGradeCode;
    }

    public String getNameFirstpart() {
        return nameFirstpart;
    }

    public void setNameFirstpart(String nameFirstpart) {
        this.nameFirstpart = nameFirstpart;
    }

    public String getNameLastpart() {
        return nameLastpart;
    }

    public void setNameLastpart(String nameLastpart) {
        this.nameLastpart = nameLastpart;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    public void setSourceReference(String sourceReference) {
        this.sourceReference = sourceReference;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public BigDecimal getValuationAmount() {
        return valuationAmount;
    }

    public void setValuationAmount(BigDecimal valuationAmount) {
        this.valuationAmount = valuationAmount;
    }

    public BigDecimal getSurveyFee() {
        return surveyFee;
    }

    public boolean isHasLease() {
        return hasLease;
    }

    public void setHasLease(boolean hasLease) {
        this.hasLease = hasLease;
    }

    public void setSurveyFee(BigDecimal surveyFee) {
        this.surveyFee = surveyFee;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Date getSurveyDate() {
        return surveyDate;
    }

    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    public String getSurveyorId() {
        return surveyorId;
    }

    public void setSurveyorId(String surveyorId) {
        this.surveyorId = surveyorId;
    }

    public String getSurveyor() {
        return surveyor;
    }

    public void setSurveyor(String surveyor) {
        this.surveyor = surveyor;
    }

    public String getValuationZone() {
        return valuationZone;
    }

    public void setValuationZone(String valuationZone) {
        this.valuationZone = valuationZone;
    }

    public boolean isHasDispute() {
        return hasDispute;
    }

    public void setHasDispute(boolean hasDispute) {
        this.hasDispute = hasDispute;
    }
}
