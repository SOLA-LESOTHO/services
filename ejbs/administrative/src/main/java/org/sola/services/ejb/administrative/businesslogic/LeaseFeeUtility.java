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
package org.sola.services.ejb.administrative.businesslogic;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import org.sola.common.Money;
import org.sola.services.common.ejbs.AbstractEJB;
import org.sola.services.ejb.administrative.repository.entities.AdminFeeRate;
import org.sola.services.ejb.administrative.repository.entities.AdminFeeType;
import org.sola.services.ejb.administrative.repository.entities.AdminRateType;
import org.sola.services.ejb.administrative.repository.entities.Rrr;
import org.sola.services.ejb.cadastre.businesslogic.CadastreEJBLocal;
import org.sola.services.ejb.cadastre.repository.entities.CadastreObject;
import org.sola.services.ejb.cadastre.repository.entities.GroundRentMultiplicationFactor;
import org.sola.services.ejb.cadastre.repository.entities.LandUseGrade;
import org.sola.services.ejb.cadastre.repository.entities.SpatialValueArea;

public class LeaseFeeUtility extends AbstractEJB {

    private CadastreEJBLocal cadastreEJB;

    public void setCadastreEJB(CadastreEJBLocal cadastreEJB) {
        this.cadastreEJB = cadastreEJB;
    }

    private List<AdminFeeType> getAdminFeeTypes(String languageCode) {
        return getRepository().getCodeList(AdminFeeType.class, languageCode);
    }

    private List<AdminRateType> getAdminRateTypes(String languageCode) {
        return getRepository().getCodeList(AdminRateType.class, languageCode);
    }

    private AdminFeeRate getAdminFeeRate(String feeCode, String rateCode) {

        if ((feeCode != null) && (rateCode != null)) {
            HashMap params = new HashMap();
            params.put("fee_code", feeCode);
            params.put("rate_code", rateCode);

            return getRepository().getEntity(AdminFeeRate.class,
                    AdminFeeRate.QUERY_WHERE_SEARCHBYFEEANDRATE, params);
        } else {
            return null;
        }
    }

    private String getLandGradeCode(CadastreObject co) {

        String landGradeCode = "";
        if (co.getLandGradeCode() != null) {
            landGradeCode = co.getLandGradeCode();
        }

        return landGradeCode;
    }

    private String getLandUseCode(Rrr leaseRight) {

        String landUseCode = "";
        if (leaseRight.getLandUseCode() != null) {
            landUseCode = leaseRight.getLandUseCode();
        }

        return landUseCode;
    }

    private BigDecimal getRateValue(String feeCode, String rateCode) {

        BigDecimal rateValue = BigDecimal.ZERO;

        AdminFeeRate adminFeeRate;

        adminFeeRate = this.getAdminFeeRate(feeCode, rateCode);

        if (adminFeeRate != null) {
            rateValue = adminFeeRate.getRateValue();
        }

        return rateValue;
    }

    private BigDecimal getTotalArea(CadastreObject co) {

        BigDecimal totalArea = BigDecimal.ZERO;

        SpatialValueArea spatialValueArea;

        spatialValueArea = cadastreEJB.getSpatialValueArea(co.getId());

        if (spatialValueArea != null) {
            totalArea = spatialValueArea.getCalculatedAreaSize();
        }

        return totalArea;
    }

    private BigDecimal getMultiplicationFactor(String landUse, String landGrade, String zone) {

        BigDecimal groundRentFactor;

        GroundRentMultiplicationFactor multiplicationFactor;

        multiplicationFactor = cadastreEJB.getMultiplicationFacotr(landUse, landGrade, zone);

        if (multiplicationFactor != null) {
            groundRentFactor = multiplicationFactor.getMultiplicationFactor();
        } else {
            groundRentFactor = BigDecimal.ONE;
        }

        return groundRentFactor;

    }

    private BigDecimal getGroundRentRate(String landUse, String landGrade) {

        LandUseGrade landUseGrade;

        BigDecimal groundRentRate;

        landUseGrade = cadastreEJB.getLandUseGrade(landUse, landGrade);

        if (landUseGrade != null) {
            groundRentRate = landUseGrade.getGroundRentRate();
        } else {
            groundRentRate = BigDecimal.ONE;
        }

        return groundRentRate;

    }

    private static BigDecimal getLandUsableValue(Rrr leaseRight) {

        BigDecimal landUsable = BigDecimal.ZERO;

        if (leaseRight.getLandUsable() != null) {
            landUsable = leaseRight.getLandUsable();
        }

        return landUsable;
    }

    private static BigDecimal getPersonalLevy(Rrr leaseRight) {

        BigDecimal personalLevy = BigDecimal.ZERO;

        if (leaseRight.getLandUsable() != null) {
            personalLevy = leaseRight.getPersonalLevy();
        }

        return personalLevy;
    }

    private static BigDecimal getLandUsableFactor(final BigDecimal usableLand) {

        BigDecimal landUsableValue = usableLand.divide(new BigDecimal("100"));

        BigDecimal landUsableFactor = landUsableValue.add(BigDecimal.ONE).divide(new BigDecimal("2"));

        return landUsableFactor;

    }

    private static Money calculatePerPlot(BigDecimal groundRentRate) {

        if (groundRentRate.compareTo(BigDecimal.ONE) > 0) {
            return new Money(groundRentRate);
        } else {
            return new Money(BigDecimal.ZERO);
        }

    }

    private static Money CalculatePerHectare(BigDecimal groundRentRate, BigDecimal totalArea) {
        Money groundRent = new Money(BigDecimal.ONE);
        if (groundRentRate.compareTo(BigDecimal.ONE) == 0) {
            groundRent = new Money(BigDecimal.ZERO);
        } else {
            groundRent = groundRent.times(groundRentRate).times(totalArea).div(BigDecimal.valueOf(10000));
            
        }
        return groundRent;
    }

    private static Money calculatePerArea(BigDecimal groundRentRate, BigDecimal multiplicationFactor,
                                   BigDecimal roadClassFactor, BigDecimal totalArea,
                                   BigDecimal personalLevy, BigDecimal landUsableFactor) {

        Money groundRent = new Money(BigDecimal.ONE);

        groundRent = groundRent.times(totalArea).
                times(groundRentRate).
                times(multiplicationFactor).
                times(roadClassFactor).
                times(personalLevy).
                times(landUsableFactor);
        return groundRent;
    }
    
    private static BigDecimal getStampDutyFactor(BigDecimal groundRent){
        
       
        int groundRentDivisor = Integer.valueOf(100);
        int groundRentDivident;
        int stampDutyFactor;

         BigDecimal groundRentValue = roundDecimalValue(groundRent, 0, RoundingMode.UP);
        
        groundRentDivident = groundRentValue.intValue();

        if (groundRentDivident > groundRentDivisor){
            stampDutyFactor = groundRentDivident % groundRentDivisor;
            
            if (stampDutyFactor > 0){
                groundRentDivident = groundRentDivident + groundRentDivisor - stampDutyFactor;
                stampDutyFactor = groundRentDivident/groundRentDivisor;
            }else{
                stampDutyFactor = groundRentDivident/groundRentDivisor;
            }
        }else if (groundRentDivident > 0){
            stampDutyFactor = Integer.valueOf(1);
        } else{
            stampDutyFactor = Integer.valueOf(0);
        }
       
        return new BigDecimal(stampDutyFactor);
        
    }
    
    private static BigDecimal roundDecimalValue(BigDecimal groundRent, int scale, RoundingMode roundingMode){
        BigDecimal groundRentValue = groundRent.setScale(scale, roundingMode);
        return groundRentValue;
    }

    public Money calculateGroundRent(CadastreObject co, Rrr leaseRight) {

        String landGradeCode;
        String landUseCode;

        String valuationZone = "";
        String roadClassCode = "";

        BigDecimal personalLevy;
        BigDecimal landUsable;


        BigDecimal totalArea;
        BigDecimal groundRentRate;
        BigDecimal multiplicationFactor;
        BigDecimal roadClassFactor;
        BigDecimal landUsableFactor;

        if (co == null) {
            return new Money(BigDecimal.ZERO);
        }

        landUsable = getLandUsableValue(leaseRight);

        landUseCode = getLandUseCode(leaseRight);

        personalLevy = getPersonalLevy(leaseRight);

        landGradeCode = getLandGradeCode(co);

        if (co.getValuationZone() != null) {
            valuationZone = co.getValuationZone();
        }

        if (co.getRoadClassCode() != null) {
            roadClassCode = co.getRoadClassCode();
        }

        totalArea = getTotalArea(co);

        roadClassFactor = cadastreEJB.getRoadClassFactor(roadClassCode, "en");

        multiplicationFactor = getMultiplicationFactor(landUseCode, landGradeCode, valuationZone);

        groundRentRate = getGroundRentRate(landUseCode, landGradeCode);

        landUsableFactor = getLandUsableFactor(landUsable);


        if (cadastreEJB.isCalculationPerPlot(landUseCode)) {
            return calculatePerPlot(groundRentRate);
        } else if (cadastreEJB.isCalculationPerHectare(landUseCode)) {
            return CalculatePerHectare(groundRentRate, totalArea);
        } else {
            return calculatePerArea(groundRentRate, multiplicationFactor,
                                    roadClassFactor, totalArea,
                                    personalLevy, landUsableFactor);
        }
    }

    public Money calculateDutyOnGroundRent(CadastreObject cadastreObject, Rrr leaseRight) {

        String landUse = "";
        String landGrade;
        
        BigDecimal groundRent = BigDecimal.ZERO;
        BigDecimal dutyOnGroundRentFactor = BigDecimal.ZERO;
        BigDecimal groundRentModulo;
        Money dutyOnGroundRent = new Money(BigDecimal.ONE);

        LandUseGrade landUseGrade;

        if (leaseRight.getGroundRent() != null) {
            groundRent = leaseRight.getGroundRent();
        }

        if (leaseRight.getLandUseCode() != null) {
            landUse = leaseRight.getLandUseCode();
        }

        landGrade = getLandGradeCode(cadastreObject);

        if (groundRent.compareTo(BigDecimal.ZERO) == 0) {
            return new Money(BigDecimal.ZERO);
        }
        
        groundRentModulo = getStampDutyFactor(groundRent);
        
        landUseGrade = cadastreEJB.getLandUseGrade(landUse, landGrade);

        if (landUseGrade != null) {
            dutyOnGroundRentFactor = landUseGrade.getDutyOnGroundRent();
        }

        dutyOnGroundRent = dutyOnGroundRent.times(dutyOnGroundRentFactor).times(groundRentModulo);

        return dutyOnGroundRent;
    }

    public Money calculateDutyOnTransfer(Money valuationAmount) {

        BigDecimal lowerRate;

        BigDecimal upperRate;

        Money thresholdValue;

        Money surplusValue;

        Money duty;

        String feeType = AdminFeeType.TRANSFER_DUTY;

        lowerRate = getRateValue(feeType, AdminRateType.LOWER_RATE);

        upperRate = getRateValue(feeType, AdminRateType.UPPER_RATE);

        thresholdValue = new Money(getRateValue(feeType, AdminRateType.THRESHOLD_VALUE));

        if (valuationAmount.compareTo(thresholdValue) == 1) {
            surplusValue = valuationAmount.minus(thresholdValue);
            duty = surplusValue.times(upperRate).plus(thresholdValue.times(lowerRate));
        } else {
            duty = valuationAmount.times(lowerRate);
        }

        return duty;

    }

    public Money determineServiceFee(String landUse, String landGrade) {

        Money serviceFee;
        LandUseGrade landUseGrade;

        landUseGrade = cadastreEJB.getLandUseGrade(landUse, landGrade);

        if (landUseGrade != null) {
            serviceFee = new Money(landUseGrade.getAdminFee().abs());
        } else {
            serviceFee = new Money(BigDecimal.ZERO);
        }

        return serviceFee;
    }

    public Money determineRegistrationFee(String landUse, String landGrade) {

        Money registrationFee;
        LandUseGrade landUseGrade;

        landUseGrade = cadastreEJB.getLandUseGrade(landUse, landGrade);

        if (landUseGrade != null) {
            registrationFee = new Money(landUseGrade.getRegistrationFee().abs());
        } else {
            registrationFee = new Money(BigDecimal.ZERO);
        }

        return registrationFee;

    }
}
