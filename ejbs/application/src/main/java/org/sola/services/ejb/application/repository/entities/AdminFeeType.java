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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sola.services.ejb.application.repository.entities;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 * Entity representing the application.administrative fee code table. This code
 * entity includes some additional field beyond the standard code, description,
 * display_value and status used for most code entities.
 *
 * @author soladev
 */
@Table(name = "admin_fee_type", schema = "application")
@DefaultSorter(sortString = "display_value")
public class AdminFeeType extends AbstractCodeEntity {
    public static final String CODE_SUFFIX = "_";
    public static final String STAMP_DUTY = "stamp";
    public static final String TRANSFER_DUTY = "transfer";
    public static final String LOWER_RATE = "lowerrate";
    public static final String UPPER_RATE = "upperrate";
    public static final String THRESHOLD_VALUE = "threshold";
    public static final String STAMP_DUTY_LOWER_RATE = STAMP_DUTY + CODE_SUFFIX + LOWER_RATE;
    public static final String STAMP_DUTY_THRESHOLD_VALUE = STAMP_DUTY + CODE_SUFFIX + THRESHOLD_VALUE;
    public static final String STAMP_DUTY_UPPER_RATE = STAMP_DUTY + CODE_SUFFIX + UPPER_RATE;
    public static final String TRANSFER_DUTY_LOWER_RATE = TRANSFER_DUTY + CODE_SUFFIX + LOWER_RATE;
    public static final String TRANSFER_DUTY_THRESHOLD_VALUE = TRANSFER_DUTY + CODE_SUFFIX + THRESHOLD_VALUE;
    public static final String TRANSFER_DUTY_UPPER_RATE = TRANSFER_DUTY + CODE_SUFFIX + UPPER_RATE;
    
    @Column(name = "rate")
    private BigDecimal rate;

    public AdminFeeType() {
        super();
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}