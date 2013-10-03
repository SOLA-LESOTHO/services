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
 * Entity for the slr.slr_source table. Used by the SLR Migration to create
 * source records in SOLA.
 *
 * @author soladev
 */
@Table(name = "slr_source", schema = "slr")
public class SlrSource extends AbstractEntity {

    @Id
    @Column
    private String id;
    @Column(name = "document_type")
    private int documentType;
    @Column(name = "reference_nr")
    private String referenceNr;
    @Column
    private Date recordation;
    @Column(name = "ext_archive_id")
    private String extArchiveId;
    @Column
    private String version;
    @Column
    private String description;
    @Column(name = "registered")
    private boolean registered;
    @Column(name = "adjudication_parcel_number")
    private String adjudicationParcelNumber;

    public SlrSource() {
        super();
    }

    public String getId() {
        id = id == null ? generateId() : id;
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDocumentType() {
        return documentType;
    }

    public void setDocumentType(int documentType) {
        this.documentType = documentType;
    }

    public String getReferenceNr() {
        return referenceNr;
    }

    public void setReferenceNr(String referenceNr) {
        this.referenceNr = referenceNr;
    }

    public Date getRecordation() {
        return recordation;
    }

    public void setRecordation(Date recordation) {
        this.recordation = recordation;
    }

    public String getExtArchiveId() {
        return extArchiveId;
    }

    public void setExtArchiveId(String extArchiveId) {
        this.extArchiveId = extArchiveId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public String getAdjudicationParcelNumber() {
        return adjudicationParcelNumber;
    }

    public void setAdjudicationParcelNumber(String adjudicationParcelNumber) {
        this.adjudicationParcelNumber = adjudicationParcelNumber;
    }
}
