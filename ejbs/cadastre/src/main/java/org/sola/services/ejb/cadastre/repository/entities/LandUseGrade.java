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
package org.sola.services.ejb.cadastre.repository.entities;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.entities.AbstractEntity;

/**
 * Entity representing the cadastre.land_use_grade table.
 */
@Table(name = "land_use_grade", schema = "cadastre")
public class LandUseGrade extends AbstractEntity {

    /**
     * WHERE clause to return current CO's intersecting the specified point
     */
    public static final String QUERY_WHERE_SEARCHBYLANDUSEANDGRADE = "land_use_code= #{land_use_code} "
            + "and land_grade_code= #{land_grade_code}";
    @Id
    @Column(name = "land_use_code")
    private String landUseCode;
    @Id
    @Column(name = "land_grade_code")
    private String landGradeCode;
    @Column(name = "admin_fee")
    BigDecimal adminFee;
    @Column(name = "ground_rent_rate")
    BigDecimal groundRentRate;

    public LandUseGrade() {
        super();
    }

    public BigDecimal getAdminFee() {
        return adminFee;
    }

    public void setAdminFee(BigDecimal adminFee) {
        this.adminFee = adminFee;
    }

    public BigDecimal getGroundRentRate() {
        return groundRentRate;
    }

    public void setGroundRentRate(BigDecimal groundRentRate) {
        this.groundRentRate = groundRentRate;
    }

    public String getLandGradeCode() {
        return landGradeCode;
    }

    public void setLandGradeCode(String landGradeCode) {
        this.landGradeCode = landGradeCode;
    }

    public String getLandUseCode() {
        return landUseCode;
    }

    public void setLandUseCode(String landUseCode) {
        this.landUseCode = landUseCode;
    }
}