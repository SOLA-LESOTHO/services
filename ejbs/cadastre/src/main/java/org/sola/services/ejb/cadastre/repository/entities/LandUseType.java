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
package org.sola.services.ejb.cadastre.repository.entities;

import javax.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 * Entity representing cadastre.land_use_type code table.
 *
 * @author soladev
 */
@Table(name = "land_use_type", schema = "cadastre")
@DefaultSorter(sortString = "display_value")
public class LandUseType extends AbstractCodeEntity {

    public static final String CODE_HOSPITAL = "hospital";
    public static final String CODE_CHARITABLE = "charitable";
    public static final String CODE_RECREATIONAL = "recreational";
    public static final String CODE_EDUCATIONAL = "educational";
    public static final String CODE_INSTITUTIONAL = "institutional";
    public static final String CODE_RELIGIOUS = "religious";
    public static final String CODE_BENOVOLENT = "benovelent";
    public static final String CODE_DEVOTIONAL = "devotional";
    public static final String CODE_AGRIC_IRRIGATED = "agricIrrigated";
    public static final String CODE_AGRIC_NON_IRRIGATED = "agricNonIrrigated";
    public static final String CODE_AGRIC_RANGE_GRAZING = "agricRangeGrazing";
    public static final String CODE_AGRIC_OTHER = "agricOther";
    public static final String CODE_AGRIC_LIVESTOCK = "agricIntensive";
     public static final String CODE_AGRIC = "agricultural";
    
    public LandUseType() {
        super();
    }
}
