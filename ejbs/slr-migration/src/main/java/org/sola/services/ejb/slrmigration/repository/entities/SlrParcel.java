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

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.AccessFunctions;
import org.sola.services.common.repository.entities.AbstractEntity;

/**
 * Entity for the slr.slr_source table. Used by the SLR Migration to create
 * source records in SOLA.
 *
 * @author soladev
 */
@Table(name = "slr_parcel", schema = "slr")
public class SlrParcel extends AbstractEntity {

    @Id
    @Column
    private String id;
    @Column(name = "ground_rent_zone")
    private int groundRentZone;
    @Column(name = "lease_number")
    private String leaseNumber;
    @Column
    private String village;
    @Column(name = "area_desc")
    private String areaDesc;
    @Column(name = "adjudication_parcel_number")
    private String adjudicationParcelNumber;
    @Column
    private Double area;
    @Column(name = "address_id")
    private String addressId;
    @Column
    @AccessFunctions(onSelect = "st_asewkb(geom)",
            onChange = "st_setSRID(#{geom}, 22287)")
    private byte[] geom;

    public SlrParcel() {
        super();
    }

    public String getId() {
        id = id == null ? generateId() : id;
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getGroundRentZone() {
        return groundRentZone;
    }

    public void setGroundRentZone(int groundRentZone) {
        this.groundRentZone = groundRentZone;
    }

    public String getLeaseNumber() {
        return leaseNumber;
    }

    public void setLeaseNumber(String leaseNumber) {
        this.leaseNumber = leaseNumber;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getAreaDesc() {
        return areaDesc;
    }

    public void setAreaDesc(String areaDesc) {
        this.areaDesc = areaDesc;
    }

    public String getAdjudicationParcelNumber() {
        return adjudicationParcelNumber;
    }

    public void setAdjudicationParcelNumber(String adjudicationParcelNumber) {
        this.adjudicationParcelNumber = adjudicationParcelNumber;
    }

    public byte[] getGeom() {
        return geom;
    }

    public void setGeom(byte[] geom) {
        this.geom = geom;
    }

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public String getAddressId() {
        addressId = addressId == null ? generateId() : addressId;
        return addressId;
    }

    public void setAddressId(String id) {
        this.addressId = id;
    }
}
