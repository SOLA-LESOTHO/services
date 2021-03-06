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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sola.services.ejb.transaction.repository.entities;

import java.util.List;
import javax.persistence.Table;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.ExternalEJB;
import org.sola.services.ejb.cadastre.businesslogic.CadastreEJBLocal;
import org.sola.services.ejb.cadastre.repository.entities.CadastreObject;
import org.sola.services.ejb.cadastre.repository.entities.CadastreObjectTarget;
import org.sola.services.ejb.cadastre.repository.entities.SurveyPoint;

/**
 *
 * @author Elton Manoku
 */
@Table(name = "transaction", schema = "transaction")
public class TransactionCadastreChange extends Transaction {

    @ChildEntityList(parentIdField = "transactionId")
    @ExternalEJB(
            ejbLocalClass = CadastreEJBLocal.class, 
            loadMethod = "getCadastreObjectsByTransaction", 
            saveMethod="saveCadastreObject")
    List<CadastreObject> CadastreObjectList;

    @ChildEntityList(parentIdField = "transactionId")
    @ExternalEJB(
            ejbLocalClass = CadastreEJBLocal.class, 
            loadMethod = "getSurveyPointsByTransaction", 
            saveMethod="saveEntity")
    List<SurveyPoint> surveyPointList;

    @ChildEntityList(parentIdField = "transactionId")
    @ExternalEJB(
            ejbLocalClass = CadastreEJBLocal.class, 
            loadMethod = "getCadastreObjectTargetsByTransaction", 
            saveMethod="saveEntity")
    private List<CadastreObjectTarget> cadastreObjectTargetList;

    @ChildEntityList(parentIdField = "transactionId")
    private List<TransactionSource> transactionSourceList;

    public List<CadastreObject> getCadastreObjectList() {
        return CadastreObjectList;
    }

    public void setCadastreObjectList(List<CadastreObject> CadastreObjectList) {
        this.CadastreObjectList = CadastreObjectList;
    }

    public List<SurveyPoint> getSurveyPointList() {
        return surveyPointList;
    }

    public void setSurveyPointList(List<SurveyPoint> surveyPointList) {
        this.surveyPointList = surveyPointList;
    }

    public List<CadastreObjectTarget> getCadastreObjectTargetList() {
        return cadastreObjectTargetList;
    }

    public void setCadastreObjectTargetList(List<CadastreObjectTarget> cadastreObjectTargetList) {
        this.cadastreObjectTargetList = cadastreObjectTargetList;
    }

    public List<TransactionSource> getTransactionSourceList() {
        return transactionSourceList;
    }

    public void setTransactionSourceList(List<TransactionSource> transactionSourceList) {
        this.transactionSourceList = transactionSourceList;
    }

    @Override
    public void preSave() {
        if (this.isNew() && this.getCadastreObjectList() != null){
            for(CadastreObject cadastreObject:this.getCadastreObjectList()){
                cadastreObject.setId(null);
            }
        }
        super.preSave();
    }
}
