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

        result = result += SQL();
        return result;
    }

    public static String buildGetSlrParcelSql(Date fromDate, Date toDate) {
        String result;
        BEGIN();
        SELECT("DISTINCT d.[LeaseNumberFinal] AS lease_number");
        SELECT("p.[Geom].STAsBinary() AS geom");
        SELECT("d.[AreaVillage] AS village");
        SELECT("CASE WHEN d.[AdjudicationAreaDescription] LIKE '30-%' THEN 'MAZENOD - MASERU DISTRICT' "
                + "  WHEN d.[AdjudicationAreaDescription] LIKE '31-%' THEN 'HLOTSE URBAN AREA' "
                + "  WHEN d.[AdjudicationAreaDescription] LIKE '32-%' THEN 'MAPUTSOE URBAN AREA' "
                + "  ELSE 'MASERU URBAN AREA' END AS area_desc");
        SELECT("dbo.GetGroundRentZone(pp.[Geom]) AS ground_rent_zone");
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
}
