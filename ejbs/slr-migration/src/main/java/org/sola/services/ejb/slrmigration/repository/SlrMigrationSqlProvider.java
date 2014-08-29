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
package org.sola.services.ejb.slrmigration.repository;

import java.util.Date;
import org.apache.ibatis.jdbc.SqlBuilder;
import static org.apache.ibatis.jdbc.SqlBuilder.*;
import org.sola.services.common.logging.LogUtility;

/**
 *
 * @author soladev
 */
public class SlrMigrationSqlProvider {

    public static final String QUERY_PARAM_FROM_DATE = "fromDate";
    public static final String QUERY_PARAM_TO_DATE = "toDate";
    public static final String QUERY_PARAM_ADJUDICATION_AREA = "adjudicationArea";
    public static final String OFFICIAL_AREA_TYPE = "'officialArea'";
    public static final String CALCULATED_AREA_TYPE = "'calculatedArea'";

    /**
     * Creates the clause to use when the Adjudication Area string contains more
     * than one area.
     *
     * @param numAreas The number of adjudication areas that will be included in
     * the query
     * @param tablePrefix the prefix used on the mfDocuments table.
     * @return Where Clause
     */
    private static String buildAreasClause(int numAreas, String tablePrefix) {
        String result = "1=1";
        if (numAreas > 0) {
            String clause = tablePrefix + ".[OwnerAdjudicationAreaDescription] IN ( ";
            for (int cnt = 1; cnt <= numAreas; cnt++) {
                clause += "#{" + QUERY_PARAM_ADJUDICATION_AREA + cnt + "},";
            }
            clause = clause.substring(0, clause.length() - 1) + " ) ";
            result = clause;
        }
        return result;
    }

    /**
     * Disables all triggers on the specified table. QUERY_PARAM_TABLE_NAME is
     * required.
     */
    public static String buildDisableTriggerSql(String tableName) {
        return "ALTER TABLE " + tableName + " DISABLE TRIGGER ALL";
    }

    /**
     * Enables all triggers on the specified table. QUERY_PARAM_TABLE_NAME is
     * required.
     */
    public static String buildEnableTriggerSql(String tableName) {
        return "ALTER TABLE " + tableName + " ENABLE TRIGGER ALL";
    }

    /**
     * Retrieves the SLR Source records from the SQL Server summary database.
     *
     * @param registeredOnly If true, only documents associated with a
     * registered SAR1 from are retrieved. If false,all documents that are at
     * the stage of being submitted to LAA are retrieved.
     * @param numAreas Used to limit the number of records returned based on the
     * adjudication area (e.g. 11-1, 30-1, etc). This value indicates the number
     * of adjudication areas that will be part of query
     * @param fromDate Used with toDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     * @param toDate Used with fromDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     */
    public static String buildGetSlrSourceSql(boolean registeredOnly, int numAreas,
            Date fromDate, Date toDate) {
        String result;
        BEGIN();
        SELECT("DISTINCT d_sar1.[LeaseNumberFinal] AS reference_nr");
        SELECT("d.[Created] AS recordation");
        SELECT("CAST(df.[FileId] AS VARCHAR(40)) AS ext_archive_id");
        SELECT("CAST(df.[FileVersion] AS VARCHAR(40)) AS version");
        SELECT("d.[Class] AS document_type");
        SELECT("d.[DocumentDescription] AS description");
        SELECT("Lesotho.dbo.GetState('SAR1LeaseRegisteredByLAA', d_sar1.[WorkflowState]) AS registered");
        SELECT("Lesotho.dbo.StripAdjudicationParcelNumber(d.[AdjudicationParcelNumberSuffix]) AS adjudication_parcel_number");
        FROM("Lesotho.dbo.mfDocuments d");
        FROM("Lesotho.dbo.mfDocumentFiles df");
        FROM("Lesotho.dbo.mfDocuments d_sar1");
        WHERE("df.[Id] = d.[Id]");
        WHERE("df.[Version] = d.[Version]");
        // If the FileId and FileVersion is 0, it means this is an old version of the document, so exclude it
        WHERE("df.[FileId] != 0");
        WHERE("df.[FileVersion] != 0");
        WHERE("d_sar1.[AdjudicationParcelNumberSuffix] = d.[AdjudicationParcelNumberSuffix]");
        // Class 5 is the SAR1 form that is the primary form for each lease
        WHERE("d_sar1.[Class] = 5");
        if (registeredOnly) {
            // Only process the documents for leases that have been registered by LAA
            WHERE("Lesotho.dbo.GetState('SAR1LeaseRegisteredByLAA', d_sar1.[WorkflowState]) = 1");
        } else {
            // Pull through all documents that are ready for LAA to review
            WHERE("Lesotho.dbo.GetState('SAR1AdjudicationRecordDeliveredToLAA', d_sar1.[WorkflowState]) = 1");
        }
        if (fromDate != null && toDate != null) {
            // Try to limit the records returned by using the LastModified date for the SAR1 document
            WHERE("d_sar1.[LastModified] BETWEEN #{" + QUERY_PARAM_FROM_DATE
                    + "} AND #{" + QUERY_PARAM_TO_DATE + "}");
        }
        if (numAreas > 0) {
            WHERE(buildAreasClause(numAreas, "d_sar1"));
        }
        result = SQL();
        return result;
    }

    /**
     * Creates Delete Statement to clear the slr.slr_source table prior to
     * transferring the records from the MS SQL Server (Lesotho) database.
     *
     */
    public static String buildDeleteSlrSourceSql() {
        String result;
        BEGIN();
        DELETE_FROM("slr.slr_source");
        result = SQL();
        return result;
    }

    /**
     * Loads the source records from the slr.slr_source table into the
     * source.source table using a bulk insert statement. Manually sets the
     * rowversion as triggers are disabled on the source.source table.
     *
     */
    public static String buildLoadSourceSql() {
        String result;
        result = "INSERT INTO source.source (id, la_nr, reference_nr, recordation, submission,"
                + " ext_archive_id, type_code, version, description, adjudication_parcel_number,"
                + " change_user, rowversion) ";
        BEGIN();
        SELECT("slr.id");
        SELECT("to_char(now(), 'yymmdd') || '-' || trim(to_char(nextval('source.source_la_nr_seq'), '000000000'))");
        SELECT("slr.reference_nr");
        SELECT("slr.recordation");
        SELECT("slr.submission");
        SELECT("'slr-' || slr.ext_archive_id || '-' || slr.version");
        SELECT("map.sola_type");
        SELECT("slr.version");
        SELECT("slr.description");
        SELECT("slr.adjudication_parcel_number");
        SELECT("'slr-migration'");
        SELECT("1");
        FROM("slr.slr_source slr");
        FROM("slr.source_type_map map");
        WHERE("slr.document_type = map.slr_type");
        WHERE("NOT EXISTS (SELECT s.id FROM source.source s"
                + " WHERE s.reference_nr = slr.reference_nr"
                + " AND   s.ext_archive_id = 'slr-' || slr.ext_archive_id || '-' || slr.version"
                + " AND   s.version = slr.version)");

        result += SQL();
        return result;
    }

    /**
     * Retrieves the SLR Parcel records from the SQL Server summary database.
     * Note that all parcels in the PublishedParcels table are considered
     * registered
     *
     * @param numAreas Used to limit the number of records returned based on the
     * adjudication area (e.g. 11-1, 30-1, etc). This value indicates the number
     * of adjudication areas that will be part of query
     * @param fromDate Used with toDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     * @param toDate Used with fromDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     */
    public static String buildGetSlrParcelSql(int numAreas, Date fromDate, Date toDate) {
        String result;
        BEGIN();
        SELECT("DISTINCT d.[LeaseNumberFinal] AS lease_number");
        SELECT("p.[Geom].STAsBinary() AS geom");
        SELECT("CAST(ROUND(p.[Geom].STArea(), 0, 1) AS INTEGER) AS area");
        SELECT("d.[AreaVillage] AS village");
        SELECT("CASE WHEN d.[AdjudicationAreaDescription] LIKE '30-%' THEN 'MAZENOD - MASERU DISTRICT' "
                + "  WHEN d.[AdjudicationAreaDescription] LIKE '31-%' THEN 'HLOTSE URBAN AREA' "
                + "  WHEN d.[AdjudicationAreaDescription] LIKE '32-%' THEN 'MAPUTSOE URBAN AREA' "
                + "  ELSE 'MASERU URBAN AREA' END AS area_desc");
        SELECT("Lesotho.dbo.GetGroundRentZone(p.[Geom]) AS ground_rent_zone");
        SELECT("p.[AdjudicationParcelNumber] AS adjudication_parcel_number");
        FROM("Lesotho.dbo.mfDocuments d");
        FROM("Lesotho.dbo.PublishedParcels p");
        WHERE("Lesotho.dbo.StripAdjudicationParcelNumber(d.[AdjudicationParcelNumberSuffix]) = p.[AdjudicationParcelNumber]");
        // Class 5 is the SAR1 form that is the primary form for each lease
        WHERE("d.[Class] = 5");
        WHERE("d.[LeaseNumberFinal] IS NOT NULL");
        WHERE("d.[LeaseNumberFinal] LIKE '%-%'");
        // Exclude any cancelled SAR1 forms
        WHERE("Lesotho.dbo.GetState(N'SAR1Cancelled', d.[WorkflowState]) <> 1");
        if (fromDate != null && toDate != null) {
            // Try to limit the records returned by using the LastModified date for the SAR1 document
            WHERE("d.[LastModified] BETWEEN #{" + QUERY_PARAM_FROM_DATE
                    + "} AND #{" + QUERY_PARAM_TO_DATE + "}");
        }
        if (numAreas > 0) {
            WHERE(buildAreasClause(numAreas, "d"));
        }
        result = SQL();
        return result;
    }

    /**
     * Creates Delete Statement to clear the slr.slr_parcel table prior to
     * transferring the records from the MS SQL Server (Lesotho) database.
     *
     */
    public static String buildDeleteSlrParcelSql() {
        String result;
        BEGIN();
        DELETE_FROM("slr.slr_parcel");
        result = SQL();
        return result;
    }

    /**
     * Prepares the slr_parcel table for transfer into the SOLA schema tables by
     * checking if the parcel record already exists in SOLA and if so,
     * determines which values should be updated. The flags on this table can be
     * changed prior to the load operation to control which values are updated
     * into SOLA
     */
    public static String buildUpdateSlrParcelSql() {
        String result;
        BEGIN();
        UPDATE("slr.slr_parcel");
        SET("id = co.id");
        SET("address_id = COALESCE((SELECT address_id "
                + " FROM cadastre.spatial_unit_address "
                + " WHERE spatial_unit_id = co.id), address_id)");
        SET("matched = TRUE");
        SET("update_geom = CASE WHEN st_equals(geom, co.geom_polygon) = FALSE "
                + " AND geometrytype(geom) = 'POLYGON'"
                + " AND change_user = 'test' THEN TRUE ELSE FALSE END");
        SET("update_address = CASE WHEN EXISTS (SELECT spatial_unit_id "
                + " FROM cadastre.spatial_unit_address "
                + " WHERE spatial_unit_id = co.id) THEN FALSE ELSE TRUE END");
        SET("update_area = CASE WHEN EXISTS (SELECT spatial_unit_id "
                + " FROM cadastre.spatial_value_area "
                + " WHERE spatial_unit_id = co.id "
                + " AND   type_code = 'officialArea' "
                + " AND  (change_user != 'test' OR size = area)) THEN FALSE ELSE TRUE END");
        SET("update_zone = CASE WHEN co.land_grade_code != ('grade' || ground_rent_zone::VARCHAR) "
                + " AND change_user = 'test' THEN TRUE ELSE FALSE END "
                + " FROM cadastre.cadastre_object co"); // Add the table here as update does not recognize FROM
        WHERE("co.name_firstpart = TRIM((regexp_split_to_array (lease_number, '-'))[1])");
        WHERE("co.name_lastpart = TRIM((regexp_split_to_array (lease_number, '-'))[2])");
        result = SQL();
        return result;
    }

    /**
     * Validates the content of the slr_parcel table and reports any data
     * inconsistencies for further investigation.
     *
     * @return
     */
    public static String buildValidateSlrParcelSql() {
        String result;
        BEGIN();
        SELECT("slr.lease_number");
        SELECT("slr.adjudication_parcel_number as apn");
        SELECT("'MULTIPOLYGON' as msg");
        FROM("slr.slr_parcel slr");
        WHERE("geometrytype(slr.geom) = 'MULTIPOLYGON'");
        result = SQL();
        return result;
    }

    /**
     * Generates the insert statement for the Spatial Unit table based on the
     * unmatched records in the slr.slr_parcel table.
     */
    public static String buildInsertSpatialUnitSql() {
        String result;
        result = "INSERT INTO cadastre.spatial_unit (id, label, level_id, change_user) ";
        BEGIN();
        SELECT("slr.id");
        SELECT("slr.lease_number");
        SELECT("l.id");
        SELECT("'slr-migration'");
        FROM("slr.slr_parcel slr");
        FROM("cadastre.level l");
        WHERE("slr.matched = FALSE");
        WHERE("l.name = 'Parcels'");
        result += SQL();
        return result;
    }

    /**
     * Generates the insert statement for the Cadastre Object table based on the
     * unmatched records in the slr.slr_parcel table. The
     * buildInsertSpatialUnitSql must be executed before this method to ensure
     * spatial_unit records are created.
     */
    public static String buildInsertCadastreObjectSql() {
        String result;
        result = "INSERT INTO cadastre.cadastre_object (id, source_reference,"
                + " name_firstpart, name_lastpart, status_code, geom_polygon,"
                + " transaction_id, land_grade_code, change_user, rowversion, "
                + " adjudication_parcel_number) ";
        BEGIN();
        SELECT("slr.id");
        SELECT("'slr'");
        SELECT("TRIM((regexp_split_to_array (slr.lease_number, '-'))[1])");
        SELECT("TRIM((regexp_split_to_array (slr.lease_number, '-'))[2])");
        SELECT("'current'");
        SELECT("CASE WHEN geometrytype(slr.geom) = 'MULTIPOLYGON' "
                + " THEN st_geometryN(slr.geom, 1) ELSE slr.geom END");
        SELECT("'slr-migration'");
        SELECT("'grade' || slr.ground_rent_zone::VARCHAR");
        SELECT("'slr-migration'");
        SELECT("1");
        SELECT("slr.adjudication_parcel_number");
        FROM("slr.slr_parcel slr");
        WHERE("slr.matched = FALSE");
        result += SQL();
        return result;
    }

    /**
     * Generates the insert statement for the Spatial Value Area table based on
     * the unmatched records in the slr.slr_parcel table. Can be used to create
     * inserts for officalArea and calculatedArea types.
     *
     * @param typeCode Either officialArea or calculatedArea
     */
    public static String buildInsertSpatialValueAreaSql(String typeCode) {
        String result;
        result = "INSERT INTO cadastre.spatial_value_area (spatial_unit_id,"
                + " type_code, size, change_user) ";
        BEGIN();
        SELECT("slr.id");
        SELECT(typeCode);
        SELECT("slr.area");
        SELECT("'slr-migration'");
        FROM("slr.slr_parcel slr");
        WHERE("slr.matched = FALSE");
        result += SQL();
        return result;
    }

    /**
     * Generates the insert statement for the Address table based on the
     * unmatched records in the slr.slr_parcel table as well as the records
     * marked with update_address.
     */
    public static String buildInsertAddressSql() {
        String result;
        result = "INSERT INTO address.address (id,"
                + " description, change_user) ";
        BEGIN();
        SELECT("slr.address_id");
        SELECT("slr.village || ', ' || slr.area_desc");
        SELECT("'slr-migration'");
        FROM("slr.slr_parcel slr");
        WHERE("slr.matched = FALSE OR update_address = TRUE");
        result += SQL();
        return result;
    }

    /**
     * Generates the insert statement for the Spatial Unit Address table based
     * on the unmatched records in the slr.slr_parcel table as well as the
     * records marked with update_address.
     */
    public static String buildInsertParcelAddressSql() {
        String result;
        result = "INSERT INTO cadastre.spatial_unit_address (spatial_unit_id,"
                + " address_id, change_user) ";
        BEGIN();
        SELECT("slr.id");
        SELECT("slr.address_id");
        SELECT("'slr-migration'");
        FROM("slr.slr_parcel slr");
        WHERE("slr.matched = FALSE OR update_address = TRUE");
        result += SQL();
        return result;
    }

    /**
     * Generates the statement to update the geom_polygon field the Cadastre
     * Object table based on the records marked with update_geom in the
     * slr.slr_parcel table.
     */
    public static String buildUpdateCadastreObjectSql() {
        String result;
        BEGIN();
        UPDATE("cadastre.cadastre_object");
        SET("adjudication_parcel_number = slr.adjudication_parcel_number");
        SET("geom_polygon = CASE WHEN slr.update_geom = TRUE "
                + " THEN slr.geom ELSE geom_polygon END");
        SET("land_grade_code = CASE WHEN slr.update_zone = TRUE "
                + " THEN 'grade' || slr.ground_rent_zone::VARCHAR ELSE land_grade_code END");
        SET("change_user = 'slr-migration' "
                + " FROM slr.slr_parcel slr");
        WHERE("slr.id = cadastre_object.id");
        WHERE("slr.matched = TRUE");
        result = SQL();
        return result;
    }

    /**
     * Generates the statement to update size field the Spatial Value Area table
     * based on the records marked with update_area the slr.slr_parcel table.
     * Can be used to create updates for officalArea and calculatedArea types.
     *
     * @param typeCode Either officialArea or calculatedArea
     */
    public static String buildUpdateSpatialValueAreaSql(String typeCode) {
        String result;
        BEGIN();
        UPDATE("cadastre.spatial_value_area");
        SET("size = slr.area");
        SET("change_user = 'slr-migration' "
                + " FROM slr.slr_parcel slr");
        WHERE("slr.id = spatial_value_area.spatial_unit_id");
        WHERE("spatial_value_area.type_code = " + typeCode);
        WHERE("slr.update_area = TRUE");
        result = SQL();
        return result;
    }

    /**
     * Retrieves the SLR Party records from the SQL Server summary database.
     * Uses a sequence of UNION queries to retrieve the party details from the
     * mfDocuments table
     *
     * @param numAreas Used to limit the number of records returned based on the
     * adjudication area (e.g. 11-1, 30-1, etc). This value indicates the number
     * of adjudication areas that will be part of query
     * @param registeredOnly If true, only records associated with a registered
     * SAR1 from are retrieved. If false,all documents that are at the stage of
     * being submitted to LAA are retrieved.
     * @param fromDate Used with toDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     * @param toDate Used with fromDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     */
    public static String buildGetSlrPartySql(int numAreas, boolean registeredOnly,
            Date fromDate, Date toDate) {
        String result;
        BEGIN();
        SELECT("d.[AdjudicationRole1Description] AS party_role");
        SELECT("LEFT(d.[FamilyName1], 255) AS last_name");
        SELECT("CASE WHEN LOWER(d.[OtherNames1]) = 'n/a' THEN NULL ELSE LEFT(d.[OtherNames1], 255) END AS name");
        SELECT("CASE WHEN LOWER(d.[MaidenName1]) = 'n/a' THEN NULL ELSE LEFT(d.[MaidenName1], 50) END AS alias");
        SELECT("d.[Gender1Description] AS gender");
        SELECT("d.[DateOfBirth1] AS dob");
        SELECT("CASE WHEN LOWER(d.[Email1]) = 'n/a' THEN NULL ELSE LEFT(d.[Email1], 50) END AS email");
        SELECT("CASE WHEN LOWER(d.[PhoneCell1]) = 'n/a' THEN NULL ELSE LEFT(d.[PhoneCell1], 15) END AS mobile");
        SELECT("CASE WHEN LOWER(d.[PhoneHome1]) = 'n/a' THEN NULL ELSE LEFT(d.[PhoneHome1], 15) END AS home_phone");
        SELECT("CASE WHEN LOWER(d.[PhoneWork1]) = 'n/a' THEN NULL ELSE LEFT(d.[PhoneWork1], 15) END AS work_phone");
        SELECT("d.[MaritalStatus1Description] AS marital_status");
        SELECT("d.[MarriageType1Description] AS marriage_type");
        SELECT("CASE WHEN LOWER(d.[PostalAddress1]) = 'n/a' THEN NULL ELSE LEFT(d.[PostalAddress1], 255) END AS addr");
        SELECT("d.[LeaseNumberFinal] AS lease_number");
        SELECT("1 AS account_holder");
        SELECT("'p1:' + Lesotho.dbo.StripAdjudicationParcelNumber(d.[AdjudicationParcelNumberSuffix]) AS slr_reference");
        FROM("Lesotho.dbo.mfDocuments d");
        WHERE("d.[Class] = 5");
        WHERE("d.[LeaseNumberFinal] IS NOT NULL");
        WHERE("ISNULL(LOWER(d.[FamilyName1]), 'n/a') != 'n/a'");
        WHERE("d.[AdjudicationRole1Description] IS NOT NULL");
        if (registeredOnly) {
            // Only process the records for leases that have been registered by LAA
            WHERE("Lesotho.dbo.GetState('SAR1LeaseRegisteredByLAA', d.[WorkflowState]) = 1");
        } else {
            // Pull through all records that are ready for LAA to review
            WHERE("Lesotho.dbo.GetState('SAR1AdjudicationRecordDeliveredToLAA', d.[WorkflowState]) = 1");
        }
        if (fromDate != null && toDate != null) {
            // Try to limit the records returned by using the LastModified date for the SAR1 document
            WHERE("d.[LastModified] BETWEEN #{" + QUERY_PARAM_FROM_DATE
                    + "} AND #{" + QUERY_PARAM_TO_DATE + "}");
        }
        if (numAreas > 0) {
            WHERE(buildAreasClause(numAreas, "d"));
        }
        result = SQL() + " UNION ";

        BEGIN();
        SELECT("d.[AdjudicationRole2Description] AS party_role");
        SELECT("LEFT(d.[FamilyName2], 255) AS last_name");
        SELECT("CASE WHEN LOWER(d.[OtherNames2]) = 'n/a' THEN NULL ELSE LEFT(d.[OtherNames2], 255) END AS name");
        SELECT("CASE WHEN LOWER(d.[MaidenName2]) = 'n/a' THEN NULL ELSE LEFT(d.[MaidenName2], 50) END AS alias");
        SELECT("d.[Gender2Description] AS gender");
        SELECT("d.[DateOfBirth2] AS dob");
        SELECT("CASE WHEN LOWER(d.[Email2]) = 'n/a' THEN NULL ELSE LEFT(d.[Email2], 50) END AS email");
        SELECT("CASE WHEN LOWER(d.[PhoneCell2]) = 'n/a' THEN NULL ELSE LEFT(d.[PhoneCell2], 15) END AS mobile");
        SELECT("CASE WHEN LOWER(d.[PhoneHome2]) = 'n/a' THEN NULL ELSE LEFT(d.[PhoneHome2], 15) END AS home_phone");
        SELECT("CASE WHEN LOWER(d.[PhoneWork2]) = 'n/a' THEN NULL ELSE LEFT(d.[PhoneWork2], 15) END AS work_phone");
        SELECT("d.[MaritalStatus2Description] AS marital_status");
        SELECT("d.[MarriageType2Description] AS marriage_type");
        SELECT("CASE WHEN LOWER(d.[PostalAddress2]) = 'n/a' THEN NULL ELSE LEFT(d.[PostalAddress2], 255) END AS addr");
        SELECT("d.[LeaseNumberFinal] AS lease_number");
        SELECT("CASE WHEN d.[AdjudicationRole1Description] IS NULL THEN 1 ELSE 0 END AS account_holder");
        SELECT("'p2:' + Lesotho.dbo.StripAdjudicationParcelNumber(d.[AdjudicationParcelNumberSuffix]) AS slr_reference");
        FROM("Lesotho.dbo.mfDocuments d");
        WHERE("d.[Class] = 5");
        WHERE("d.[LeaseNumberFinal] IS NOT NULL");
        WHERE("ISNULL(LOWER(d.[FamilyName2]), 'n/a') != 'n/a'");
        WHERE("d.[AdjudicationRole2Description] IS NOT NULL");
        if (registeredOnly) {
            // Only process the records for leases that have been registered by LAA
            WHERE("Lesotho.dbo.GetState('SAR1LeaseRegisteredByLAA', d.[WorkflowState]) = 1");
        } else {
            // Pull through all records that are ready for LAA to review
            WHERE("Lesotho.dbo.GetState('SAR1AdjudicationRecordDeliveredToLAA', d.[WorkflowState]) = 1");
        }
        if (fromDate != null && toDate != null) {
            // Try to limit the records returned by using the LastModified date for the SAR1 document
            WHERE("d.[LastModified] BETWEEN #{" + QUERY_PARAM_FROM_DATE
                    + "} AND #{" + QUERY_PARAM_TO_DATE + "}");
        }
        if (numAreas > 0) {
            WHERE(buildAreasClause(numAreas, "d"));
        }
        result += SQL() + " UNION ";

        BEGIN();
        SELECT("d.[AdjudicationRole3Description] AS party_role");
        SELECT("LEFT(d.[FamilyName3], 255) AS last_name");
        SELECT("CASE WHEN LOWER(d.[OtherNames3]) = 'n/a' THEN NULL ELSE LEFT(d.[OtherNames3], 255) END AS name");
        SELECT("CASE WHEN LOWER(d.[MaidenName3]) = 'n/a' THEN NULL ELSE LEFT(d.[MaidenName3], 50) END AS alias");
        SELECT("d.[Gender3Description] AS gender");
        SELECT("d.[DateOfBirth3] AS dob");
        SELECT("CASE WHEN LOWER(d.[Email3]) = 'n/a' THEN NULL ELSE LEFT(d.[Email3], 50) END AS email");
        SELECT("CASE WHEN LOWER(d.[PhoneCell3]) = 'n/a' THEN NULL ELSE LEFT(d.[PhoneCell3], 15) END AS mobile");
        SELECT("CASE WHEN LOWER(d.[PhoneHome3]) = 'n/a' THEN NULL ELSE LEFT(d.[PhoneHome3], 15) END AS home_phone");
        SELECT("CASE WHEN LOWER(d.[PhoneWork3]) = 'n/a' THEN NULL ELSE LEFT(d.[PhoneWork3], 15) END AS work_phone");
        SELECT("d.[MaritalStatus3Description] AS marital_status");
        SELECT("d.[MarriageType3Description] AS marriage_type");
        SELECT("CASE WHEN LOWER(d.[PostalAddress3]) = 'n/a' THEN NULL ELSE LEFT(d.[PostalAddress3], 255) END AS addr");
        SELECT("d.[LeaseNumberFinal] AS lease_number");
        SELECT("CASE WHEN d.[AdjudicationRole1Description] IS NULL AND d.[AdjudicationRole2Description] IS NULL "
                + "THEN 1 ELSE 0 END AS account_holder");
        SELECT("'p3:' + Lesotho.dbo.StripAdjudicationParcelNumber(d.[AdjudicationParcelNumberSuffix]) AS slr_reference");
        FROM("Lesotho.dbo.mfDocuments d");
        WHERE("d.[Class] = 5");
        WHERE("d.[LeaseNumberFinal] IS NOT NULL");
        WHERE("ISNULL(LOWER(d.[FamilyName3]), 'n/a') != 'n/a'");
        WHERE("d.[AdjudicationRole3Description] IS NOT NULL");
        if (registeredOnly) {
            // Only process the records for leases that have been registered by LAA
            WHERE("Lesotho.dbo.GetState('SAR1LeaseRegisteredByLAA', d.[WorkflowState]) = 1");
        } else {
            // Pull through all records that are ready for LAA to review
            WHERE("Lesotho.dbo.GetState('SAR1AdjudicationRecordDeliveredToLAA', d.[WorkflowState]) = 1");
        }
        if (fromDate != null && toDate != null) {
            // Try to limit the records returned by using the LastModified date for the SAR1 document
            WHERE("d.[LastModified] BETWEEN #{" + QUERY_PARAM_FROM_DATE
                    + "} AND #{" + QUERY_PARAM_TO_DATE + "}");
        }
        if (numAreas > 0) {
            WHERE(buildAreasClause(numAreas, "d"));
        }
        result += SQL() + " UNION ";

        BEGIN();
        SELECT("d.[AdjudicationRole4Description] AS party_role");
        SELECT("LEFT(d.[FamilyName4], 255) AS last_name");
        SELECT("CASE WHEN LOWER(d.[OtherNames4]) = 'n/a' THEN NULL ELSE LEFT(d.[OtherNames4], 255) END AS name");
        SELECT("CASE WHEN LOWER(d.[MaidenName4]) = 'n/a' THEN NULL ELSE LEFT(d.[MaidenName4], 50) END AS alias");
        SELECT("d.[Gender4Description] AS gender");
        SELECT("d.[DateOfBirth4] AS dob");
        SELECT("CASE WHEN LOWER(d.[Email4]) = 'n/a' THEN NULL ELSE LEFT(d.[Email4], 50) END AS email");
        SELECT("CASE WHEN LOWER(d.[PhoneCell4]) = 'n/a' THEN NULL ELSE LEFT(d.[PhoneCell4], 15) END AS mobile");
        SELECT("CASE WHEN LOWER(d.[PhoneHome4]) = 'n/a' THEN NULL ELSE LEFT(d.[PhoneHome4], 15) END AS home_phone");
        SELECT("CASE WHEN LOWER(d.[PhoneWork4]) = 'n/a' THEN NULL ELSE LEFT(d.[PhoneWork4], 15) END AS work_phone");
        SELECT("d.[MaritalStatus4Description] AS marital_status");
        SELECT("d.[MarriageType4Description] AS marriage_type");
        SELECT("CASE WHEN LOWER(d.[PostalAddress4]) = 'n/a' THEN NULL ELSE LEFT(d.[PostalAddress4], 255) END AS addr");
        SELECT("d.[LeaseNumberFinal] AS lease_number");
        SELECT("CASE WHEN d.[AdjudicationRole1Description] IS NULL AND d.[AdjudicationRole2Description] IS NULL AND "
                + "d.[AdjudicationRole3Description] IS NULL THEN 1 ELSE 0 END AS account_holder");
        SELECT("'p4:' + Lesotho.dbo.StripAdjudicationParcelNumber(d.[AdjudicationParcelNumberSuffix]) AS slr_reference");
        FROM("Lesotho.dbo.mfDocuments d");
        WHERE("d.[Class] = 5");
        WHERE("d.[LeaseNumberFinal] IS NOT NULL");
        WHERE("ISNULL(LOWER(d.[FamilyName4]), 'n/a') != 'n/a'");
        WHERE("d.[AdjudicationRole4Description] IS NOT NULL");
        if (registeredOnly) {
            // Only process the records for leases that have been registered by LAA
            WHERE("Lesotho.dbo.GetState('SAR1LeaseRegisteredByLAA', d.[WorkflowState]) = 1");
        } else {
            // Pull through all records that are ready for LAA to review
            WHERE("Lesotho.dbo.GetState('SAR1AdjudicationRecordDeliveredToLAA', d.[WorkflowState]) = 1");
        }
        if (fromDate != null && toDate != null) {
            // Try to limit the records returned by using the LastModified date for the SAR1 document
            WHERE("d.[LastModified] BETWEEN #{" + QUERY_PARAM_FROM_DATE
                    + "} AND #{" + QUERY_PARAM_TO_DATE + "}");
        }
        if (numAreas > 0) {
            WHERE(buildAreasClause(numAreas, "d"));
        }
        result += SQL();
        return result;
    }

    /**
     * Retrieves the SLR Lease records from the SQL Server summary database. The
     * query uses a WITH clause to preprocess the ground rent zone for each
     * lease and improve the overall performance of the query.
     *
     * @param numAreas Used to limit the number of records returned based on the
     * adjudication area (e.g. 11-1, 30-1, etc). This value indicates the number
     * of adjudication areas that will be part of query
     * @param registeredOnly If true, only documents associated with a
     * registered SAR1 from are retrieved. If false,all documents that are at
     * the stage of being submitted to LAA are retrieved.
     * @param fromDate Used with toDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     * @param toDate Used with fromDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     */
    public static String buildGetSlrLeaseSql(int numAreas,
            boolean registeredOnly, Date fromDate, Date toDate) {

        String result = "WITH grz (pId,  min_zone) AS ( ";

        // Create temp table with preprocessed ground rent zone details for 
        // each parcel. This greatly improves the overal performance of the 
        // query as grz is used multiple times in the main query. 
        BEGIN();
        SELECT("p.[Id] AS pId");
        SELECT("MIN(lz.GroundRentZone) AS min_zone");
        FROM("Lesotho.dbo.PublishedParcels p");
        FROM("Lesotho.dbo.LandUseZones lz");
        WHERE("lz.SP_GEOMETRY.STContains(p.[Geom].STCentroid()) = 1");
        WHERE("p.[Geom] IS NOT NULL");
        GROUP_BY("p.[Id]");
        result += SQL() + "), summary (docId, zone, area) AS ( ";

        // Create query for summary table containg the parcel area and a default
        // ground rent zone if one could not be determined during pre-processing. 
        BEGIN();
        SELECT("d.id AS docId");
        SELECT("ISNULL((SELECT min_zone FROM grz WHERE pId = p.Id), 4) AS zone");
        SELECT("CAST(ROUND(p.[Geom].STArea(), 0, 1) AS INTEGER) AS area");
        FROM("Lesotho.dbo.[PublishedParcels] p");
        FROM("Lesotho.dbo.[mfDocuments] d");
        WHERE("p.[Geom] IS NOT NULL");
        WHERE("d.[LeaseNumberFinal] IS NOT NULL");
        WHERE("Lesotho.dbo.StripAdjudicationParcelNumber(d.[AdjudicationParcelNumberSuffix]) = p.[AdjudicationParcelNumber]");
        if (registeredOnly) {
            // Only process the documents for leases that have been registered by LAA
            WHERE("Lesotho.dbo.GetState('SAR1LeaseRegisteredByLAA', d.[WorkflowState]) = 1");
        } else {
            // Pull through all documents that are ready for LAA to review
            WHERE("Lesotho.dbo.GetState('SAR1AdjudicationRecordDeliveredToLAA', d.[WorkflowState]) = 1");
        }
        if (numAreas > 0) {
            WHERE(buildAreasClause(numAreas, "d"));
        }
        result += SQL() + ") ";

        // Create main query to retrive lease details. 
        BEGIN();
        SELECT("d.[LeaseNumberFinal] AS lease_number");
        SELECT("d.[LandUseTypeActualDescription] AS land_use");
        SELECT("Lesotho.[dbo].[GetLeaseData](d.[LandUseTypeActualDescription], s.zone, s.area, d.[PropertyType], d.[RequestExemptGroundRent], N'StampDuty') AS stamp_duty");
        SELECT("Lesotho.[dbo].[GetLeaseData](d.[LandUseTypeActualDescription], s.zone, s.area, d.[PropertyType], d.[RequestExemptGroundRent], N'GroundRent') AS ground_rent");
        SELECT("Lesotho.[dbo].[GetLeaseData](d.[LandUseTypeActualDescription], s.zone, s.area, d.[PropertyType], d.[RequestExemptGroundRent], N'RegistrationFee') AS reg_fee");
        SELECT("Lesotho.[dbo].[GetLeaseData](d.[LandUseTypeActualDescription], s.zone, s.area, d.[PropertyType], d.[RequestExemptGroundRent], N'LeaseDuration') AS term");
        SELECT("CASE WHEN Lesotho.dbo.GetState('SAR1LeaseRegisteredByLAA', d.[WorkflowState]) = 1 THEN 'current' ELSE 'pending' END AS status");
        SELECT("Lesotho.dbo.StripAdjudicationParcelNumber(d.[AdjudicationParcelNumberSuffix]) AS adjudication_parcel_number");
        SELECT("s.[area] AS area");
        FROM("Lesotho.dbo.[mfDocuments] d");
        FROM("summary s");
        WHERE("s.[docId] = d.[Id]");
        if (fromDate != null && toDate != null) {
            // Try to limit the records returned by using the LastModified date for the SAR1 document
            WHERE("d.[LastModified] BETWEEN #{" + QUERY_PARAM_FROM_DATE
                    + "} AND #{" + QUERY_PARAM_TO_DATE + "}");
        }
        result += SQL();

        return result;
    }

    /**
     * Creates Delete Statement to clear the slr.slr_lease table prior to
     * transferring the records from the MS SQL Server (Lesotho) database.
     *
     */
    public static String buildDeleteSlrLeaseSql() {
        String result;
        BEGIN();
        DELETE_FROM("slr.slr_lease");
        result = SQL();
        return result;
    }

    /**
     * Creates Delete Statement to clear the slr.slr_party table prior to
     * transferring the records from the MS SQL Server (Lesotho) database.
     *
     */
    public static String buildDeleteSlrPartySql() {
        String result;
        BEGIN();
        DELETE_FROM("slr.slr_party");
        result = SQL();
        return result;
    }

    /**
     * Creates bulk update statement that checks the slr_lease records against
     * the ba_unit records in SOLA and flags any leases that already exist in
     * SOLA. Those leases will not be updated / inserted into SOLA.
     */
    public static String buildMatchSlrLeaseSql() {
        String result;
        BEGIN();
        UPDATE("slr.slr_lease");
        SET("id = ba.id");
        SET("rrr_id = null");
        SET("notation_id = null");
        SET("matched = TRUE "
                + "FROM administrative.ba_unit ba");
        WHERE("ba.name = slr_lease.lease_number");
        result = SQL();
        return result;
    }

    /**
     * Creates bulk update statement that sets the status of all records in the
     * slr.slr_lease table to current
     */
    public static String buildUpdateSlrLeaseStatusSql() {
        String result;
        BEGIN();
        UPDATE("slr.slr_lease");
        SET("status  = 'current'");
        result = SQL();
        return result;
    }

    /**
     * Creates bulk update statement that updates the slr.slr_lease table with
     * the cadastre_objects (a.k.a. parcels) that match the lease number of each
     * lease record.
     */
    public static String buildUpdateSlrLeaseParcelSql() {
        String result;
        BEGIN();
        UPDATE("slr.slr_lease");
        SET("cadastre_object_id  = co.id "
                + "FROM cadastre.cadastre_object co");
        WHERE("co.name_firstpart = TRIM((regexp_split_to_array(slr_lease.lease_number, '-'))[1])");
        WHERE("co.name_lastpart = TRIM((regexp_split_to_array(slr_lease.lease_number, '-'))[2])");
        WHERE("matched = FALSE");
        result = SQL();
        return result;
    }

    /**
     * Creates bulk update statement that flags any slr_party records matched to
     * existing leases in SOLA.
     */
    public static String buildMatchSlrPartySql() {
        String result;
        BEGIN();
        UPDATE("slr.slr_party");
        SET("matched = TRUE "
                + "FROM administrative.ba_unit ba");
        WHERE("ba.name = slr_party.lease_number");
        result = SQL();
        return result;
    }

    /**
     * Generates the insert statement for the BA Unit table based on the
     * unmatched records in the slr.slr_lease table.
     */
    public static String buildInsertBaUnitSql() {
        String result;
        result = "INSERT INTO administrative.ba_unit(id, cadastre_object_id, "
                + "name, name_firstpart, name_lastpart, creation_date, "
                + "expiration_date, status_code, transaction_id, change_user, "
                + "adjudication_parcel_number) ";
        BEGIN();
        SELECT("slr.id");
        SELECT("slr.cadastre_object_id");
        SELECT("slr.lease_number");
        SELECT("TRIM((regexp_split_to_array(slr.lease_number, '-'))[1])");
        SELECT("TRIM((regexp_split_to_array(slr.lease_number, '-'))[2])");
        SELECT("slr.reg_date");
        // Calculate the expiration date based on the term for the lease
        SELECT("slr.reg_date + COALESCE(term, '90')::INT * INTERVAL '1 year'");
        SELECT("slr.status");
        SELECT("'slr-migration'");
        SELECT("'slr-migration'");
        SELECT("slr.adjudication_parcel_number");
        FROM("slr.slr_lease slr");
        WHERE("slr.matched = FALSE");
        result += SQL();
        return result;
    }

    /**
     * Generates the insert statement for the Rrr table based on the unmatched
     * records in the slr.slr_lease table.
     */
    public static String buildInsertRrrSql() {
        String result;
        result = "INSERT INTO administrative.rrr(id, ba_unit_id, "
                + "nr, type_code, status_code, is_primary, transaction_id, "
                + "registration_date, registration_number, lease_number, land_use_code, "
                + "start_date, expiration_date, land_usable, personal_levy, stamp_duty, "
                + "registration_fee, ground_rent, change_user) ";
        BEGIN();
        SELECT("slr.rrr_id");
        SELECT("slr.id");
        SELECT("trim(to_char(nextval('administrative.rrr_nr_seq'), '000000'))");
        SELECT("'lease'");
        SELECT("slr.status");
        SELECT("TRUE");
        SELECT("'slr-migration'");
        SELECT("slr.reg_date");
        SELECT("slr.lease_number");
        SELECT("slr.lease_number");
        SELECT("CASE TRIM(LOWER(slr.land_use)) "
                + "WHEN 'residential' THEN TRIM(LOWER(slr.land_use)) "
                + "WHEN 'commercial' THEN TRIM(LOWER(slr.land_use)) "
                + "WHEN 'recreational' THEN TRIM(LOWER(slr.land_use)) "
                + "WHEN 'educational' THEN TRIM(LOWER(slr.land_use)) "
                + "WHEN 'charitable' THEN TRIM(LOWER(slr.land_use)) "
                + "WHEN 'industrial' THEN TRIM(LOWER(slr.land_use)) "
                + "WHEN 'government' THEN 'institutional' "
                + "WHEN 'health' THEN 'hospital' "
                + "WHEN 'religion' THEN 'religious'"
                + "ELSE 'other' END ");
        SELECT("slr.reg_date");
        // Calculate the expiration date based on the term for the lease
        SELECT("slr.reg_date + COALESCE(term, '90')::INT * INTERVAL '1 year'");
        SELECT("100::NUMERIC(19,2)");
        SELECT("1::NUMERIC(19,2)");
        SELECT("COALESCE(slr.stamp_duty, '0')::NUMERIC(29,2)");
        SELECT("COALESCE(slr.reg_fee, '0')::NUMERIC(29,2)");
        SELECT("COALESCE(slr.ground_rent, '0')::NUMERIC(29,2)");
        SELECT("'slr-migration'");
        FROM("slr.slr_lease slr");
        WHERE("slr.matched = FALSE");
        result += SQL();
        return result;
    }

    /**
     * Generates the insert statement for the Notation table based on the
     * unmatched records in the slr.slr_lease table
     */
    public static String buildInsertNotationSql() {
        String result;
        result = "INSERT INTO administrative.notation (id, rrr_id, "
                + "transaction_id, reference_nr, notation_text, notation_date, "
                + "status_code, change_user) ";
        BEGIN();
        SELECT("slr.notation_id");
        SELECT("slr.rrr_id");
        SELECT("'slr-migration'");
        SELECT("trim(to_char(nextval('administrative.notation_reference_nr_seq'), '000000'))");
        SELECT("'lease'");
        SELECT("slr.reg_date");
        SELECT("slr.status");
        SELECT("'slr-migration'");
        FROM("slr.slr_lease slr");
        WHERE("slr.matched = FALSE");
        result += SQL();
        return result;
    }

    /**
     * Generates the insert statement for the Party for Rrr table based on the
     * unmatched records in the slr.slr_lease and slr.slr_party table.
     */
    public static String buildInsertPartyForRrrSql() {
        String result;
        result = "INSERT INTO administrative.party_for_rrr (rrr_id, "
                + "party_id, change_user) ";
        BEGIN();
        SELECT("l.rrr_id");
        SELECT("p.id");
        SELECT("'slr-migration'");
        FROM("slr.slr_lease l");
        FROM("slr.slr_party p");
        WHERE("l.matched = FALSE");
        WHERE("l.lease_number = p.lease_number");
        result += SqlBuilder.SQL();
        return result;
    }

    /**
     * Generates the insert statement for the Party table based on the unmatched
     * records in the slr.slr_party table.
     */
    public static String buildInsertPartySql() {
        String result;
        result = "INSERT INTO party.party(id, ext_id, type_code, name, "
                + "last_name, legal_type, alias, gender_code, birth_date, "
                + "address_id, email, mobile, phone, fax, change_user ) ";
        BEGIN();
        SELECT("slr.id");
        SELECT("slr.slr_reference");
        SELECT("CASE WHEN slr.name IS NULL THEN 'nonNaturalPerson' ELSE 'naturalPerson' END ");
        SELECT("COALESCE(slr.name, slr.last_name)");
        SELECT("CASE WHEN slr.name IS NULL THEN NULL ELSE slr.last_name END");
        SELECT("CASE WHEN slr.name IS NULL THEN NULL "
                + "WHEN TRIM(slr.marital_status) = 'Divorced' THEN TRIM(slr.marital_status) "
                + "WHEN TRIM(slr.marital_status) = 'Single' THEN TRIM(slr.marital_status) "
                + "WHEN TRIM(slr.marital_status) = 'Widowed' THEN TRIM(slr.marital_status) "
                + "WHEN TRIM(LOWER(slr.marital_status)) = 'married' AND "
                + "     TRIM(LOWER(slr.marriage_type)) LIKE '%community%' THEN 'Married in community of property' "
                + "WHEN TRIM(LOWER(slr.marital_status)) = 'married' AND "
                + "     TRIM(LOWER(slr.marriage_type)) LIKE '%prenup%' THEN 'Married out of community of property' "
                + "ELSE NULL END ");
        SELECT("CASE WHEN slr.name IS NULL THEN NULL ELSE slr.alias END");
        SELECT("CASE WHEN slr.name IS NULL THEN NULL ELSE TRIM(LOWER(slr.gender)) END");
        SELECT("CASE WHEN slr.name IS NULL THEN NULL ELSE slr.dob END");
        SELECT("slr.addr_id");
        SELECT("slr.email");
        SELECT("slr.mobile");
        SELECT("slr.home_phone");
        SELECT("slr.work_phone");
        SELECT("'slr-migration'");
        FROM("slr.slr_party slr");
        WHERE("slr.matched = FALSE");
        result += SQL();
        return result;
    }

    /**
     * Generates the insert statement for the Address table based on the
     * unmatched records in the slr.slr_party table
     */
    public static String buildInsertPartyAddressSql() {
        String result;
        result = "INSERT INTO address.address (id,"
                + " description, change_user) ";
        BEGIN();
        SELECT("slr.addr_id");
        SELECT("slr.addr");
        SELECT("'slr-migration'");
        FROM("slr.slr_party slr");
        WHERE("slr.matched = FALSE");
        WHERE("slr.addr_id IS NOT NULL");
        result += SQL();
        return result;
    }

    /**
     * Generates the insert statement for the Party Role table based on the
     * unmatched records in the slr.slr_party table.
     */
    public static String buildInsertPartyRoleSql() {
        String result;
        result = "INSERT INTO party.party_role (party_id, "
                + " type_code, change_user) ";
        BEGIN();
        SELECT("slr.id");
        SELECT("'accountHolder'");
        SELECT("'slr-migration'");
        FROM("slr.slr_party slr");
        WHERE("slr.matched = FALSE");
        WHERE("slr.account_holder = 1");

        result += SQL() + " UNION ";

        BEGIN();
        SELECT("slr.id");
        SELECT("CASE TRIM(LOWER(slr.party_role)) "
                + "WHEN 'occupant' THEN TRIM(LOWER(slr.party_role)) "
                + "WHEN 'landlord' THEN TRIM(LOWER(slr.party_role)) "
                + "WHEN 'trustor' THEN TRIM(LOWER(slr.party_role)) "
                + "WHEN 'authorised trustee' THEN 'trustee' "
                + "WHEN 'authorised representative' THEN 'representative' "
                + "ELSE NULL END ");
        SELECT("'slr-migration'");
        FROM("slr.slr_party slr");
        WHERE("slr.matched = FALSE");
        WHERE("slr.party_role IS NOT NULL");
        result += SQL();
        return result;
    }

    /**
     * Generates the insert statement for the Source Describes Rrr table. This
     * statement links the Rrr and Source tables. It does not require any data
     * in the SLR tables.
     */
    public static String buildInsertSourceRrrSql() {
        String result;
        result = "INSERT INTO administrative.source_describes_rrr (rrr_id, "
                + "source_id, change_user) ";
        BEGIN();
        SELECT("r.id");
        SELECT("s.id");
        SELECT("'slr-migration'");
        FROM("administrative.rrr r");
        FROM("source.source s");
        WHERE("r.lease_number = s.reference_nr");
        WHERE("r.type_code = 'lease'");
        WHERE("r.transaction_id = 'slr-migration'");
        WHERE("s.ext_archive_id LIKE 'slr-%'");
        WHERE("NOT EXISTS (SELECT sr.rrr_id FROM administrative.source_describes_rrr sr "
                + " WHERE sr.rrr_id = r.id AND sr.source_id = s.id)");
        result += SqlBuilder.SQL();
        return result;
    }
}
