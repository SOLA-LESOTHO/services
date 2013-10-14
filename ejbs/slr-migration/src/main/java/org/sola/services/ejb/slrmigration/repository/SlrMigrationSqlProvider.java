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
import static org.apache.ibatis.jdbc.SqlBuilder.*;

/**
 *
 * @author soladev
 */
public class SlrMigrationSqlProvider {

    public static final String QUERY_PARAM_FROM_DATE = "fromDate";
    public static final String QUERY_PARAM_TO_DATE = "toDate";
    public static final String OFFICIAL_AREA_TYPE = "'officialArea'";
    public static final String CALCULATED_AREA_TYPE = "'calculatedArea'";

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
     * @param fromDate Used with toDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     * @param toDate Used with fromDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     */
    public static String buildGetSlrSourceSql(boolean registeredOnly, Date fromDate, Date toDate) {
        String result;
        BEGIN();
        SELECT("DISTINCT d_sar1.[LeaseNumberFinal] AS reference_nr");
        SELECT("d.[Created] AS recordation");
        SELECT("CAST(df.[FileId] AS VARCHAR(40)) AS ext_archive_id");
        SELECT("CAST(df.[FileVersion] AS VARCHAR(40)) AS version");
        SELECT("d.[Class] AS document_type");
        SELECT("d.[DocumentDescription] AS description");
        SELECT("dbo.GetState('SAR1LeaseRegisteredByLAA', d_sar1.[WorkflowState]) AS registered");
        SELECT("dbo.StripAdjudicationParcelNumber(d.[AdjudicationParcelNumberSuffix]) AS adjudication_parcel_number");
        FROM("dbo.mfDocuments d");
        FROM("dbo.mfDocumentFiles df");
        FROM("dbo.mfDocuments d_sar1");
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
            WHERE("dbo.GetState('SAR1LeaseRegisteredByLAA', d_sar1.[WorkflowState]) = 1");
        } else {
            // Pull through all documents that are ready for LAA to review
            WHERE("dbo.GetState('SAR1AdjudicationRecordDeliveredToLAA', d_sar1.[WorkflowState]) = 1");
        }
        if (fromDate != null && toDate != null) {
            // Try to limit the records returned by using the LastModified date for the SAR1 document
            WHERE("d_sar1.[LastModified] BETWEEN #{" + QUERY_PARAM_FROM_DATE
                    + "} AND #{" + QUERY_PARAM_TO_DATE + "}");
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
     * @param fromDate Used with toDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     * @param toDate Used with fromDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     */
    public static String buildGetSlrParcelSql(Date fromDate, Date toDate) {
        String result;
        BEGIN();
        SELECT("DISTINCT d.[LeaseNumberFinal] AS lease_number");
        SELECT("p.[Geom].STAsBinary() AS geom");
        SELECT("ROUND(p.[Geom].STArea(), 0, 1) AS area");
        SELECT("d.[AreaVillage] AS village");
        SELECT("CASE WHEN d.[AdjudicationAreaDescription] LIKE '30-%' THEN 'MAZENOD - MASERU DISTRICT' "
                + "  WHEN d.[AdjudicationAreaDescription] LIKE '31-%' THEN 'HLOTSE URBAN AREA' "
                + "  WHEN d.[AdjudicationAreaDescription] LIKE '32-%' THEN 'MAPUTSOE URBAN AREA' "
                + "  ELSE 'MASERU URBAN AREA' END AS area_desc");
        SELECT("dbo.GetGroundRentZone(p.[Geom]) AS ground_rent_zone");
        SELECT("p.[AdjudicationParcelNumber] AS adjudication_parcel_number");
        FROM("dbo.mfDocuments d");
        FROM("dbo.PublishedParcels p");
        WHERE("dbo.StripAdjudicationParcelNumber(d.[AdjudicationParcelNumberSuffix]) = p.[AdjudicationParcelNumber]");
        // Class 5 is the SAR1 form that is the primary form for each lease
        WHERE("d.[Class] = 5");
        WHERE("d.[LeaseNumberFinal] IS NOT NULL");
        WHERE("d.[LeaseNumberFinal] LIKE '%-%'");
        // Exclude any cancelled SAR1 forms
        WHERE("dbo.GetState(N'SAR1Cancelled', d.[WorkflowState]) <> 1");
        if (fromDate != null && toDate != null) {
            // Try to limit the records returned by using the LastModified date for the SAR1 document
            WHERE("d.[LastModified] BETWEEN #{" + QUERY_PARAM_FROM_DATE
                    + "} AND #{" + QUERY_PARAM_TO_DATE + "}");
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
}
