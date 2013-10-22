/**
 * ******************************************************************************************
 * Copyright (c) 2013 Food and Agriculture Organization of the United Nations
 * (FAO) and the Lesotho Land Administration Authority (LAA). All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,this
 * list of conditions and the following disclaimer. 2. Redistributions in binary
 * form must reproduce the above copyright notice,this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. 3. Neither the names of FAO, the LAA nor the names of
 * its contributors may be used to endorse or promote products derived from this
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
package org.sola.services.ejb.slrmigration.repository.entities;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.entities.AbstractEntity;

/**
 * Entity for the slr.slr_lease table. Used by the SLR Migration to create
 * ba_unit and RRR records in SOLA.
 *
 * @author soladev
 */
@Table(name = "slr_lease", schema = "slr")
public class SlrLease extends AbstractEntity {

    public static final String NIL_TEXT = "Nil";
    public static final String X_TEXT = "x";
    public static final String MOLUTI_SYMBOL = "M";
    @Id
    @Column
    private String id;
    @Column(name = "rrr_id")
    private String rrrId;
    @Column(name = "notation_id")
    private String notationId;
    @Column(name = "lease_number")
    private String leaseNumber;
    @Column(name = "land_use")
    private String landUse;
    @Column(name = "stamp_duty")
    private String stampDuty;
    @Column(name = "ground_rent")
    private String groundRent;
    @Column(name = "reg_fee")
    private String regFee;
    @Column(name = "term")
    private String term;
    @Column
    private int area;
    @Column
    private String status;
    @Column(name = "adjudication_parcel_number")
    private String adjudicationParcelNumber;
    @Column(name = "reg_date")
    private Date regDate;

    public SlrLease() {
        super();
    }

    public String getId() {
        id = id == null ? generateId() : id;
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRrrId() {
        rrrId = rrrId == null ? generateId() : rrrId;
        return rrrId;
    }

    public void setRrrId(String rrrId) {
        this.rrrId = rrrId;
    }

    public String getNotationId() {
        notationId = notationId == null ? generateId() : notationId;
        return notationId;
    }

    public void setNotationId(String notationId) {
        this.notationId = notationId;
    }

    public String getLeaseNumber() {
        return leaseNumber;
    }

    public void setLeaseNumber(String leaseNumber) {
        this.leaseNumber = leaseNumber;
    }

    public String getLandUse() {
        return landUse;
    }

    public void setLandUse(String landUse) {
        this.landUse = landUse;
    }

    public String getStampDuty() {
        return stampDuty;
    }

    public void setStampDuty(String stampDuty) {
        if (NIL_TEXT.equalsIgnoreCase(stampDuty) || X_TEXT.equalsIgnoreCase(stampDuty)) {
            stampDuty = null;
        } else if (stampDuty != null) {
            stampDuty = stampDuty.replaceAll(MOLUTI_SYMBOL, "").trim();
        }
        this.stampDuty = stampDuty;
    }

    public String getGroundRent() {
        return groundRent;
    }

    public void setGroundRent(String groundRent) {
        if (NIL_TEXT.equalsIgnoreCase(groundRent) || X_TEXT.equalsIgnoreCase(groundRent)) {
            groundRent = null;
        } else if (groundRent != null) {
            groundRent = groundRent.replaceAll(MOLUTI_SYMBOL, "").trim();
        }
        this.groundRent = groundRent;
    }

    public String getRegFee() {
        return regFee;
    }

    public void setRegFee(String regFee) {
        if (NIL_TEXT.equalsIgnoreCase(regFee) || X_TEXT.equalsIgnoreCase(regFee)) {
            regFee = null;
        } else if (regFee != null) {
            regFee = regFee.replaceAll(MOLUTI_SYMBOL, "").trim();
        }
        this.regFee = regFee;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        if (term != null) {
            term = term.replaceAll("\\D", "");
        }
        this.term = term;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdjudicationParcelNumber() {
        return adjudicationParcelNumber;
    }

    public void setAdjudicationParcelNumber(String adjudicationParcelNumber) {
        this.adjudicationParcelNumber = adjudicationParcelNumber;
    }

    public Date getRegDate() {
        return regDate;
    }

    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }
}
