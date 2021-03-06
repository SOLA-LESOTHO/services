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
package org.sola.services.ejb.cadastre.businesslogic;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.sola.common.RolesConstants;
import org.sola.common.SOLAException;
import org.sola.common.messaging.ServiceMessage;
import org.sola.services.common.ejbs.AbstractEJB;
import org.sola.services.common.repository.CommonSqlProvider;
import org.sola.services.ejb.cadastre.repository.entities.*;

/**
 * EJB to manage data in the cadastre schema. Supports retrieving and saving of
 * cadastre objects. Also provides methods for retrieving reference codes from
 * the administrative schema.
 */
@Stateless
@EJB(name = "java:global/SOLA/CadastreEJBLocal", beanInterface = CadastreEJBLocal.class)
public class CadastreEJB extends AbstractEJB implements CadastreEJBLocal {

    /**
     * Retrieves all cadastre.cadastre_object_type code values.
     *
     * @param languageCode The language code to use for localization of display
     * values.
     */
    @Override
    public List<CadastreObjectType> getCadastreObjectTypes(String languageCode) {
        return getRepository().getCodeList(CadastreObjectType.class, languageCode);
    }

    /**
     * Retrieves a cadastre object using the specified identifier.
     *
     * @param id The identifier of the cadastre object to retrieve.
     */
    @Override
    public CadastreObject getCadastreObject(String id) {
        return getRepository().getEntity(CadastreObject.class, id);
    }

    /**
     * Retrieves a list of cadastre object matching the list of ids provided.
     *
     * @param cadastreObjIds A list of cadaster object ids to use for retrieval.
     */
    @Override
    public List<CadastreObject> getCadastreObjects(List<String> cadastreObjIds) {
        return getRepository().getEntityListByIds(CadastreObject.class, cadastreObjIds);
    }

    /**
     * Returns a maximum of 30 cadastre objects that have a name first part
     * and/or name last part that matches the specified search string. This
     * method supports partial matches and is case insensitive.
     *
     * @param searchString The search string to use
     * @return The list of cadastre objects matching the search string
     */
    @Override
    public List<CadastreObject> getCadastreObjectByParts(String searchString) {
        Integer numberOfMaxRecordsReturned = 30;
        HashMap params = new HashMap();
        // Replace / and \ with space to improve the search
        searchString = searchString.replaceAll("\\\\|\\/", " ");
        params.put("search_string", searchString);
        params.put(CommonSqlProvider.PARAM_LIMIT_PART, numberOfMaxRecordsReturned);
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, CadastreObject.QUERY_ORDER_BY_SEARCHBYPARTS);
        return getRepository().getEntityList(CadastreObject.class,
                CadastreObject.QUERY_WHERE_SEARCHBYPARTS, params);
    }

    /**
     * Returns a maximum of 30 cadastre objects with current and pending status
     * that have a name first part and/or name last part that matches the
     * specified search string. This method supports partial matches and is case
     * insensitive.
     *
     * @param searchString The search string to use
     * @return The list of cadastre objects matching the search string
     */
    @Override
    public List<CadastreObject> getCadastreObjectByAllParts(String searchString) {
        Integer numberOfMaxRecordsReturned = 30;
        HashMap params = new HashMap();
        params.put("search_string", searchString);
        params.put(CommonSqlProvider.PARAM_LIMIT_PART, numberOfMaxRecordsReturned);
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, CadastreObject.QUERY_ORDER_BY_SEARCHBYPARTS);
        return getRepository().getEntityList(CadastreObject.class,
                CadastreObject.QUERY_WHERE_SEARCHBYALLPARTS, params);
    }

    /**
     * Returns the cadastre object that is located at the point specified or
     * null if there is no cadastre object at that location. Uses the PostGIS
     * ST_Intersects function to perform the comparison.
     *
     * @param x The x ordinate of the location
     * @param y The y ordinate of the location
     * @param srid The SRID identifying the coordinate system for the x,y
     * coordinate. Must match the SRID used by SOLA.
     */
    @Override
    public CadastreObject getCadastreObjectByPoint(
            double x, double y, int srid, String typeCode) {
        HashMap params = new HashMap();
        params.put("x", x);
        params.put("y", y);
        params.put("srid", srid);
        params.put("type_code", typeCode);
        return getRepository().getEntity(
                CadastreObject.class, CadastreObject.QUERY_WHERE_SEARCHBYPOINT, params);
    }

    /**
     * Can be used to create a new cadastre object or save any updates to the
     * details of an existing cadastre object.
     *
     * @param cadastreObject The cadastre object to create/save.
     * @return The cadastre object after the save is completed.
     */
    @Override
    @RolesAllowed({RolesConstants.CADASTRE_PARCEL_SAVE, RolesConstants.ADMINISTRATIVE_BA_UNIT_SAVE})
    public CadastreObject saveCadastreObject(CadastreObject cadastreObject) {
        return getRepository().saveEntity(cadastreObject);
    }

    @Override
    @RolesAllowed(RolesConstants.CADASTRE_PARCEL_SAVE)
    public boolean terminateCadastreObject(String transactionId, String cadastreObjectId) {
        if (cadastreObjectId == null || transactionId == null) {
            return false;
        }

        List<CadastreObjectTarget> targets = getCadastreObjectTargetsByTransaction(transactionId);
        boolean targetFound = false;

        if (targets != null && targets.size() > 0) {
            for (CadastreObjectTarget target : targets) {
                if (target.getCadastreObjectId().equals(cadastreObjectId)) {
                    targetFound = true;
                    break;
                }
            }
        }

        if (!targetFound) {
            // Create target cadastre object
            CadastreObjectTarget newTarget = new CadastreObjectTarget();
            newTarget.setCadastreObjectId(cadastreObjectId);
            newTarget.setTransactionId(transactionId);
            getRepository().saveEntity(newTarget);
        }

        // Make cadastre object as historic
        CadastreObjectStatusChanger coChanger = getRepository().getEntity(
                CadastreObjectStatusChanger.class, cadastreObjectId);
        coChanger.setStatusCode("historic");
        getRepository().saveEntity(coChanger);
        return true;
    }

    /**
     * Retrieves all cadastre objects linked to the specified BA Unit.
     *
     * @param baUnitId Identifier of the BA Unit
     */
    @Override
    public List<CadastreObject> getCadastreObjectsByBaUnit(String baUnitId) {
        HashMap params = new HashMap();
        params.put("ba_unit_id", baUnitId);
        return getRepository().getEntityList(CadastreObject.class,
                CadastreObject.QUERY_WHERE_SEARCHBYBAUNIT, params);
    }

    /**
     * Retrieves all cadastre objects linked to the specified Service through
     * transaction.
     *
     * @param serviceId Identifier of the Service
     */
    @Override
    public List<CadastreObject> getCadastreObjectsByService(String serviceId) {
        HashMap params = new HashMap();
        params.put("service_id", serviceId);
        return getRepository().getEntityList(CadastreObject.class,
                CadastreObject.QUERY_WHERE_SEARCHBYSERVICE, params);
    }

    /**
     * Updates the status of cadastre objects associated with the specified
     * transaction. The filter parameter can be used to indicate the where
     * clause to apply.
     *
     * @param transactionId Identifier of the transaction associated to the
     * cadastre objects to be updated
     * @param filter The where clause to use when retrieving the cadastre
     * objects. Must be
     * {@linkplain CadastreObjectStatusChanger#QUERY_WHERE_SEARCHBYTRANSACTION_PENDING}
     * or
     * {@linkplain CadastreObjectStatusChanger#QUERY_WHERE_SEARCHBYTRANSACTION_TARGET}.
     * @param statusCode The status code to set on the selected cadastre
     * objects.
     */
    @Override
    @RolesAllowed({RolesConstants.APPLICATION_APPROVE, RolesConstants.APPLICATION_SERVICE_COMPLETE})
    public void ChangeStatusOfCadastreObjects(
            String transactionId, String filter, String statusCode) {

        if (!this.isInRole(RolesConstants.CADASTRE_PARCEL_SAVE)) {
            // The user must be able to save parcels before they can complete this method
            throw new SOLAException(ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS);
        }

        HashMap params = new HashMap();
        params.put("transaction_id", transactionId);
        List<CadastreObjectStatusChanger> involvedCoList
                = getRepository().getEntityList(CadastreObjectStatusChanger.class, filter, params);
        for (CadastreObjectStatusChanger involvedCo : involvedCoList) {
            involvedCo.setStatusCode(statusCode);
            getRepository().saveEntity(involvedCo);
        }
    }

    /**
     * Retrieves the list of Cadastre Object Targets associated with the
     * transaction.
     * <p>
     * Cadastre Object Targets are used to link the cadastre object to new
     * transactions that may occur on the cadastre object after it has been
     * initially created - for example the transaction to extinguish the
     * cadastre object.</p>
     *
     * @param transactionId The identifier of the transaction
     */
    @Override
    public List<CadastreObjectTarget> getCadastreObjectTargetsByTransaction(String transactionId) {
        Map params = new HashMap<String, Object>();
        params.put(
                CommonSqlProvider.PARAM_WHERE_PART,
                CadastreObjectTarget.QUERY_WHERE_SEARCHBYTRANSACTION);
        params.put("transaction_id", transactionId);
        return getRepository().getEntityList(CadastreObjectTarget.class, params);
    }

    /**
     * Retrieves all Survey Points associated with the transaction.
     *
     * @param transactionId The identifier of the transaction
     */
    @Override
    public List<SurveyPoint> getSurveyPointsByTransaction(String transactionId) {
        Map params = new HashMap<String, Object>();
        params.put(
                CommonSqlProvider.PARAM_WHERE_PART,
                SurveyPoint.QUERY_WHERE_SEARCHBYTRANSACTION);
        params.put("transaction_id", transactionId);
        return getRepository().getEntityList(SurveyPoint.class, params);
    }

    /**
     * Retrieves all Cadastre Objects created by the transaction.
     *
     * @param transactionId The identifier of the transaction
     */
    @Override
    public List<CadastreObject> getCadastreObjectsByTransaction(String transactionId) {
        Map params = new HashMap<String, Object>();
        params.put(
                CommonSqlProvider.PARAM_WHERE_PART,
                CadastreObject.QUERY_WHERE_SEARCHBYTRANSACTION);
        params.put("transaction_id", transactionId);
        return getRepository().getEntityList(CadastreObject.class, params);

    }

    /**
     * Retrieves all node points from the underlying cadastre objects that
     * intersect the specified bounding box coordinates. All of the node points
     * within the bounding box are used to create a single geometry -
     * {@linkplain CadastreObjectNode#geom}. The cadastre objects used as the
     * source of the node points are also captured in the
     * {@linkplain CadastreObjectNode#cadastreObjectList}.
     *
     * @param xMin The xMin ordinate of the bounding box
     * @param yMin The yMin ordinate of the bounding box
     * @param xMax The xMax ordinate of the bounding box
     * @param yMax The yMax ordinate of the bounding box
     * @param srid The SRID to use to create the bounding box. Must be the same
     * SRID as the one used by the cadastre_object table.
     * @return The CadastreObjectNode representing all node points within the
     * bounding box as well as the list of cadastre objects used to obtain the
     * node points.
     */
    @Override
    public CadastreObjectNode getCadastreObjectNode(
            double xMin, double yMin, double xMax, double yMax, int srid,
            String cadastreObjectType) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_FROM_PART,
                CadastreObjectNode.QUERY_GET_BY_RECTANGLE_FROM_PART);
        params.put(CommonSqlProvider.PARAM_WHERE_PART,
                CadastreObjectNode.QUERY_GET_BY_RECTANGLE_WHERE_PART);
        params.put(CommonSqlProvider.PARAM_LIMIT_PART, 1);
        params.put("minx", xMin);
        params.put("miny", yMin);
        params.put("maxx", xMax);
        params.put("maxy", yMax);
        params.put("srid", srid);
        params.put("cadastre_object_type", cadastreObjectType);
        CadastreObjectNode cadastreObjectNode = getRepository().getEntity(
                CadastreObjectNode.class, params);
        if (cadastreObjectNode != null) {
            params.clear();
            params.put("geom", cadastreObjectNode.getGeom());
            params.put("type_code", cadastreObjectType);
            params.put("srid", srid);
            cadastreObjectNode.setCadastreObjectList(getRepository().getEntityList(
                    CadastreObject.class, CadastreObject.QUERY_WHERE_SEARCHBYGEOM, params));
        }
        return cadastreObjectNode;

    }

    /**
     * Unknown
     *
     * @param xMin The xMin ordinate of the bounding box
     * @param yMin The yMin ordinate of the bounding box
     * @param xMax The xMax ordinate of the bounding box
     * @param yMax The yMax ordinate of the bounding box
     * @param srid The SRID to use to create the bounding box. Must be the same
     * SRID as the one used by the cadastre_object table.
     */
    @Override
    public CadastreObjectNode getCadastreObjectNodePotential(
            double xMin, double yMin, double xMax, double yMax, int srid,
            String cadastreObjectType) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_FROM_PART,
                CadastreObjectNode.QUERY_GET_BY_RECTANGLE_POTENTIAL_FROM_PART);
        params.put(CommonSqlProvider.PARAM_LIMIT_PART, 1);
        params.put("minx", xMin);
        params.put("miny", yMin);
        params.put("maxx", xMax);
        params.put("maxy", yMax);
        params.put("srid", srid);
        params.put("cadastre_object_type", cadastreObjectType);
        CadastreObjectNode cadastreObjectNode = getRepository().getEntity(
                CadastreObjectNode.class, params);
        if (cadastreObjectNode != null) {
            params.clear();
            params.put("geom", cadastreObjectNode.getGeom());
            params.put("type_code", cadastreObjectType);
            params.put("srid", srid);
            cadastreObjectNode.setCadastreObjectList(getRepository().getEntityList(
                    CadastreObject.class, CadastreObject.QUERY_WHERE_SEARCHBYGEOM, params));
        }
        return cadastreObjectNode;
    }

    /**
     * Retrieves all Cadastre Object Node Targets associated to the transaction.
     * <p>
     * A Cadastre Object Node Target</p> is used to identify the nodes that have
     * been added, moved or removed as part of a redefinition transaction.
     * </p>
     *
     * @param transactionId The identifier of the transaction
     */
    @Override
    public List<CadastreObjectNodeTarget> getCadastreObjectNodeTargetsByTransaction(
            String transactionId) {
        Map params = new HashMap<String, Object>();
        params.put(
                CommonSqlProvider.PARAM_WHERE_PART,
                CadastreObjectNodeTarget.QUERY_WHERE_SEARCHBYTRANSACTION);
        params.put("transaction_id", transactionId);
        return getRepository().getEntityList(CadastreObjectNodeTarget.class, params);
    }

    /**
     * Retrieves the cadastre objects that have been associated with a cadastre
     * redefinition transaction.
     *
     * @param transactionId The identifier of the transaction
     */
    @Override
    public List<CadastreObjectTargetRedefinition> getCadastreObjectRedefinitionTargetsByTransaction(
            String transactionId) {
        Map params = new HashMap<String, Object>();
        params.put(
                CommonSqlProvider.PARAM_WHERE_PART,
                CadastreObjectTarget.QUERY_WHERE_SEARCHBYTRANSACTION);
        params.put("transaction_id", transactionId);
        return getRepository().getEntityList(CadastreObjectTargetRedefinition.class, params);
    }

    /**
     * Approves the changes to cadastre objects as a result of a cadastre
     * redefinition.
     *
     * @param transactionId The identifier of the transaction
     */
    @Override
    @RolesAllowed({RolesConstants.APPLICATION_APPROVE, RolesConstants.APPLICATION_SERVICE_COMPLETE})
    public void approveCadastreRedefinition(String transactionId) {

        if (!this.isInRole(RolesConstants.CADASTRE_PARCEL_SAVE)) {
            // The user must be able to save parcels before they can complete this method
            throw new SOLAException(ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS);
        }

        List<CadastreObjectTargetRedefinition> targetObjectList
                = this.getCadastreObjectRedefinitionTargetsByTransaction(transactionId);
        for (CadastreObjectTargetRedefinition targetObject : targetObjectList) {
            CadastreObjectStatusChanger cadastreObject
                    = this.getRepository().getEntity(CadastreObjectStatusChanger.class,
                            targetObject.getCadastreObjectId());
            cadastreObject.setGeomPolygon(targetObject.getGeomPolygon());
            cadastreObject.setTransactionId(transactionId);
            cadastreObject.setApprovalDatetime(null);
            this.saveEntity(cadastreObject);
        }
    }

    /**
     * Retrieves the temporary cadastre objects that have been associated with a
     * bulk operation transaction.
     *
     * @param transactionId The identifier of the transaction
     */
    @Override
    public List<SpatialUnitTemporary> getSpatialUnitTemporaryListByTransaction(String transactionId) {
        Map params = new HashMap<String, Object>();
        params.put(
                CommonSqlProvider.PARAM_WHERE_PART,
                SpatialUnitTemporary.QUERY_WHERE_SEARCHBYTRANSACTION);
        params.put("transaction_id", transactionId);
        return getRepository().getEntityList(SpatialUnitTemporary.class, params);
    }

    /**
     * Locates cadastre object's area size
     *
     *
     *
     * @param colist the list of cadastre object
     * @return The total area size
     */
    @Override
    public SpatialValueArea getSpatialValueArea(String colist) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LIMIT_PART, 1);
        params.put(SpatialValueArea.QUERY_PARAMETER_COLIST, colist);
        return getRepository().getEntity(SpatialValueArea.class, params);
    }

    @Override
    public GroundRentMultiplicationFactor getMultiplicationFacotr(String landUseCode, String landGradeCode, String valuationZoneCode) {

        HashMap params = new HashMap();
        params.put("land_use_code", landUseCode);
        params.put("land_grade_code", landGradeCode);
        params.put("valuation_zone", valuationZoneCode);

        return getRepository().getEntity(GroundRentMultiplicationFactor.class,
                GroundRentMultiplicationFactor.QUERY_WHERE_SEARCHBYLANDUSEGRADEANDZONE, params);
    }

    /**
     * Retrieves all cadastre.land_use_grade code value based on the land use
     * and land grade
     *
     * @param landUseCode The land use code to use for determining specific land
     * use of the parcel.
     * @param landGradeCode The land grade code to use for determining grade of
     * a given parcel.
     */
    @Override
    public LandUseGrade getLandUseGrade(String landUseCode, String landGradeCode) {
        if ((landUseCode != null) && (landGradeCode != null)) {
            HashMap params = new HashMap();
            params.put("land_use_code", landUseCode);
            params.put("land_grade_code", landGradeCode);

            return getRepository().getEntity(LandUseGrade.class,
                    LandUseGrade.QUERY_WHERE_SEARCHBYLANDUSEANDGRADE, params);
        } else {
            return null;
        }
    }

    /**
     * Retrieves all cadastre.land_grade_type code values.
     *
     * @param languageCode The language code to use for localization of display
     * values.
     */
    @Override
    public List<LandGradeType> getLandGradeTypes(String languageCode) {
        return getRepository().getCodeList(LandGradeType.class, languageCode);
    }

    @Override
    public List<RoadClassType> getRoadClassTypes(String langugageCode) {
        return getRepository().getCodeList(RoadClassType.class, langugageCode);
    }

    /**
     * Retrieves all cadastre.land_use_type code values.
     *
     * @param languageCode The language code to use for localization of display
     * values.
     */
    @Override
    public List<LandUseType> getLandUseTypes(String languageCode) {
        return getRepository().getCodeList(LandUseType.class, languageCode);
    }

    @Override
    public BigDecimal getRoadClassFactor(String roadClassCode, String languageCode) {
        if (roadClassCode != null) {
            HashMap params = new HashMap();
            params.put("code", roadClassCode);
            RoadClassType roadClassType = getRepository().getCode(
                    RoadClassType.class, roadClassCode, languageCode);
            if (roadClassType != null) {
                return roadClassType.getRoadFactor();
            } else {
                return BigDecimal.ONE;
            }
        } else {
            return BigDecimal.ONE;
        }

    }

    @Override
    public boolean isCalculationPerPlot(String landUseCode) {

        boolean calculationPerPlot = false;

        if ((landUseCode.equals(LandUseType.CODE_HOSPITAL))
                || (landUseCode.equals(LandUseType.CODE_CHARITABLE))
                || (landUseCode.equals(LandUseType.CODE_RECREATIONAL))
                || (landUseCode.equals(LandUseType.CODE_EDUCATIONAL))
                || (landUseCode.equals(LandUseType.CODE_INSTITUTIONAL))
                || (landUseCode.equals(LandUseType.CODE_BENOVOLENT))
                || (landUseCode.equals(LandUseType.CODE_DEVOTIONAL))
                || (landUseCode.equals(LandUseType.CODE_RELIGIOUS))) {
            calculationPerPlot = true;
        }

        return calculationPerPlot;

    }

    @Override
    public boolean isCalculationPerHectare(String landUseCode) {
        if (isAgriculturalLease(landUseCode)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Retrieves all cadastre.parcel_jurisdiction_type code values.
     *
     * @param languageCode The language code to use for localization of display
     * values.
     */
    @Override
    public List<ParcelJurisdictionType> getParcelJurisdictionTypes(String languageCode) {
        return getRepository().getCodeList(ParcelJurisdictionType.class, languageCode);
    }

    private boolean isAgriculturalLease(String landUseCode) {
        if ((landUseCode.equals(LandUseType.CODE_AGRIC_IRRIGATED))
                || (landUseCode.equals(LandUseType.CODE_AGRIC_NON_IRRIGATED))
                || (landUseCode.equals(LandUseType.CODE_AGRIC_RANGE_GRAZING))
                || (landUseCode.equals(LandUseType.CODE_AGRIC))
                || (landUseCode.equals(LandUseType.CODE_AGRIC_OTHER))
                || (landUseCode.equals(LandUseType.CODE_AGRIC_LIVESTOCK))) {
            return true;
        } else {
            return false;
        }
    }

}
