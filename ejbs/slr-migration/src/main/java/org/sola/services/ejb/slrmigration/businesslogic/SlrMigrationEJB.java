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
package org.sola.services.ejb.slrmigration.businesslogic;

import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.sola.common.DateUtility;
import org.sola.common.StringUtility;
import org.sola.common.logging.LogUtility;
import org.sola.services.common.ejbs.AbstractEJB;
import org.sola.services.common.faults.FaultUtility;
import org.sola.services.common.repository.CommonRepository;
import org.sola.services.common.repository.CommonRepositoryImpl;
import org.sola.services.common.repository.CommonSqlProvider;
import org.sola.services.common.repository.DatabaseConnectionManager;
import org.sola.services.ejb.slrmigration.repository.SlrMigrationSqlProvider;
import org.sola.services.ejb.slrmigration.repository.entities.SlrLease;
import org.sola.services.ejb.slrmigration.repository.entities.SlrParcel;
import org.sola.services.ejb.slrmigration.repository.entities.SlrParty;
import org.sola.services.ejb.slrmigration.repository.entities.SlrSource;
import org.sola.services.ejb.slrmigration.repository.entities.SlrValidation;

/**
 * EJB to manage data in the SLR Migration processes.
 */
@Stateless
@EJB(name = "java:global/SOLA/SlrMigrationEJBLocal", beanInterface = SlrMigrationEJBLocal.class)
public class SlrMigrationEJB extends AbstractEJB implements SlrMigrationEJBLocal {

    CommonRepository slrRepository;

    /**
     * Sets the entity package for the EJB to
     * Party.class.getPackage().getName(). This is used to restrict the save and
     * retrieval of Code Entities.
     *
     * @see AbstractEJB#getCodeEntity(java.lang.Class, java.lang.String,
     * java.lang.String) AbstractEJB.getCodeEntity
     * @see AbstractEJB#getCodeEntityList(java.lang.Class, java.lang.String)
     * AbstractEJB.getCodeEntityList
     * @see
     * AbstractEJB#saveCodeEntity(org.sola.services.common.repository.entities.AbstractCodeEntity)
     * AbstractEJB.saveCodeEntity
     */
    @Override
    protected void postConstruct() {
        setEntityPackage(SlrSource.class.getPackage().getName());
    }

    /**
     * Returns a database connection to the SLR summary database in SQL Server
     */
    private CommonRepository getSlrRepository() {
        if (slrRepository == null) {
            URL connectConfigFileUrl = this.getClass().getResource(CommonRepository.CONNECT_CONFIG_FILE_NAME);
            slrRepository = new CommonRepositoryImpl(connectConfigFileUrl,
                    DatabaseConnectionManager.SQL_SERVER_ENV);
        }
        return slrRepository;
    }

    /**
     * Parses the adjudicationArea parameter to determine the separate areas to
     * use for the query and populates the query parameters.
     *
     * @param adjudicationArea The text entered by the user indicating the
     * adjudication areas to transfer
     * @param params The Map of parameters for the query
     * @return
     */
    private int prepAdjudicationAreaParams(String adjudicationArea, Map params) {
        int result = 0;
        if (!StringUtility.isEmpty(adjudicationArea)) {
            // Remove any spaces or single quotes and then split the string
            // at each comma. 
            String[] tmp = adjudicationArea.replaceAll(" |'", "").split(",");
            for (String area : tmp) {
                result++;
                params.put(SlrMigrationSqlProvider.QUERY_PARAM_ADJUDICATION_AREA + result, area);
            }
        }
        return result;
    }

    /**
     * Transfers a summary of the documents in the Lesotho (SLR) database into
     * the SOLA SLR schema.
     *
     * @param adjudicationArea Used to limit the number of records returned
     * based on the adjudication area (e.g. 11-1, 30-1, etc)
     * @param registeredOnly If true, only documents associated with a
     * registered SAR1 from are retrieved. If false,all documents that are at
     * the stage of being submitted to LAA are retrieved.
     * @param fromDate Used with toDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     * @param toDate Used with fromDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     * @return Summary messages describing the progress of the transfer.
     */
    @Override
    public String transferSlrSource(String adjudicationArea, boolean registeredOnly,
            Date fromDate, Date toDate) {
        String result = "";
        int count = 0;
        SlrSource current = null;
        long startTime = System.currentTimeMillis();
        try {
            result += System.lineSeparator() + "Retrieving SlrSource with parameters;"
                    + " Adjudication Area = " + (adjudicationArea == null ? "null" : adjudicationArea)
                    + ", Registered Only = " + registeredOnly
                    + ", fromDate = " + (fromDate == null ? "null" : DateUtility.getMediumDateString(fromDate, false))
                    + ", toDate = " + (toDate == null ? "null" : DateUtility.getMediumDateString(toDate, false));
            result += System.lineSeparator() + "SlrSource transfer started: "
                    + DateUtility.getDateTimeString(DateUtility.now(), DateFormat.MEDIUM, DateFormat.LONG);

            Map params = new HashMap<String, Object>();
            // Remove all records from the slr.slr_source table first
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildDeleteSlrSourceSql());
            int rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Deleted " + rows + " rows from slr.slr_source";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            int numAreas = prepAdjudicationAreaParams(adjudicationArea, params);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildGetSlrSourceSql(registeredOnly, numAreas, fromDate, toDate));
            params.put(SlrMigrationSqlProvider.QUERY_PARAM_FROM_DATE, fromDate);
            params.put(SlrMigrationSqlProvider.QUERY_PARAM_TO_DATE, toDate);

            List<SlrSource> sources = getSlrRepository().getEntityList(SlrSource.class, params);
            if (sources != null) {
                result += System.lineSeparator() + sources.size() + " SlrSources to process";
                for (SlrSource s : sources) {
                    current = s;
                    // Configure the entity so that it will be inserted into the database
                    s.clearOriginalValues();
                    s.setLoaded(false);
                    // Save each new slr_source record into the SOLA database
                    getRepository().saveEntity(s);
                    count++;
                }
            } else {
                result += System.lineSeparator() + "No SlrSources to process";
            }
        } catch (Exception ex) {
            result += System.lineSeparator() + "Processed " + count + " records";
            if (current != null) {
                result += System.lineSeparator() + "Current SlrSource = "
                        + current.getAdjudicationParcelNumber() + ", FileId: " + current.getExtArchiveId();
            }
            result += System.lineSeparator() + "EXCEPTION > " + FaultUtility.getStackTraceAsString(ex);
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        result += System.lineSeparator() + "SlrSource transfer completed in " + elapsed + "s";
        LogUtility.log(result);
        return result;
    }

    /**
     * Loads the slr_source records into the source.source table.
     *
     * @return Summary messages describing the progress of the load.
     */
    @Override
    public String loadSource() {
        String result = "";
        long startTime = System.currentTimeMillis();
        try {
            result += System.lineSeparator() + "Load Source started: "
                    + DateUtility.getDateTimeString(DateUtility.now(), DateFormat.MEDIUM, DateFormat.LONG);

            result += System.lineSeparator() + "Disable triggers on source.source";
            Map params = new HashMap<String, Object>();
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildDisableTriggerSql("source.source"));
            getRepository().bulkUpdate(params);

            result += System.lineSeparator() + "Load SLR source records into source.source";
            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildLoadSourceSql());
            int rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Loaded " + rows + " records into source.source";

            result += System.lineSeparator() + "Enable triggers on source.source";
            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildEnableTriggerSql("source.source"));
            getRepository().bulkUpdate(params);

        } catch (Exception ex) {
            result += System.lineSeparator() + "EXCEPTION > " + FaultUtility.getStackTraceAsString(ex);
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        result += System.lineSeparator() + "Load Source completed in " + elapsed + "s";
        LogUtility.log(result);
        return result;
    }

    /**
     * Transfers the published parcels from the Lesotho (SLR) database into the
     * SOLA SLR schema. Also checks the data to load into SOLA and marks which
     * data attributes will be updated if a matching record already exists in
     * SOLA. Modifying the flags in the slr.slr_parcel table will affect which
     * records are uploaded/modified.
     *
     * @param adjudicationArea Used to limit the number of records returned
     * based on the adjudication area (e.g. 11-1, 30-1, etc)
     * @param fromDate Used with toDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table. If null,
     * all parcels in the PublishedParcels table will be transferred.
     * @param toDate Used with fromDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table. If null,
     * all parcels in the PublishedParcels table will be transferred.
     * @return Summary messages describing the progress of the transfer.
     */
    @Override
    public String transferSlrParcel(String adjudicationArea, Date fromDate, Date toDate) {
        String result = "";
        int count = 0;
        SlrParcel current = null;
        long startTime = System.currentTimeMillis();
        try {
            result += System.lineSeparator() + "Retrieving SlrParcel with parameters;"
                    + " Adjudication Area = " + (adjudicationArea == null ? "null" : adjudicationArea)
                    + ", fromDate = " + (fromDate == null ? "null" : DateUtility.getMediumDateString(fromDate, false))
                    + ", toDate = " + (toDate == null ? "null" : DateUtility.getMediumDateString(toDate, false));
            result += System.lineSeparator() + "SlrParcel transfer started: "
                    + DateUtility.getDateTimeString(DateUtility.now(), DateFormat.MEDIUM, DateFormat.LONG);

            Map params = new HashMap<String, Object>();
            // Remove all records from the slr.slr_parcel table first
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildDeleteSlrParcelSql());
            int rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Deleted " + rows + " rows from slr.slr_parcel";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            int numAreas = prepAdjudicationAreaParams(adjudicationArea, params);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildGetSlrParcelSql(numAreas, fromDate, toDate));
            params.put(SlrMigrationSqlProvider.QUERY_PARAM_FROM_DATE, fromDate);
            params.put(SlrMigrationSqlProvider.QUERY_PARAM_TO_DATE, toDate);
            List<SlrParcel> parcels = getSlrRepository().getEntityList(SlrParcel.class, params);
            if (parcels != null) {
                result += System.lineSeparator() + parcels.size() + " SlrParcels to process";
                for (SlrParcel p : parcels) {
                    current = p;
                    // Configure the entity so that it will be inserted into the database
                    p.clearOriginalValues();
                    p.setLoaded(false);
                    // Save each new slr_parcel record into the SOLA database
                    getRepository().saveEntity(p);
                    count++;
                }

                // Set the flags on the slr_parcel table to indicate which data attributes will be updated
                // as well has identify the new records that will be created. 
                params.remove(CommonSqlProvider.PARAM_QUERY);
                params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildUpdateSlrParcelSql());
                rows = getRepository().bulkUpdate(params);
                result += System.lineSeparator() + "Updated " + rows + " rows in slr.slr_parcel. Check the"
                        + " slr.slr_parcel table and modify as required to control that data that will be loaded"
                        + " into SOLA. ";

                // Run validations on the SlrParcel records
                params.remove(CommonSqlProvider.PARAM_QUERY);
                params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildValidateSlrParcelSql());
                List<SlrValidation> list = getRepository().getEntityList(SlrValidation.class, params);
                if (list != null && list.size() > 0) {
                    result += System.lineSeparator() + list.size() + " validation messages...";
                    for (SlrValidation v : list) {
                        if (v.getMsg().equals("MULTIPOLYGON")) {
                            // SOLA only accepts parcels with a geometry type of POLYGON
                            result += System.lineSeparator() + "Parcel has multipolygon geometry and will not be loaded into SOLA;"
                                    + " Lease Num = " + v.getLeaseNumber() + ", APN = " + v.getApn();
                        }
                    }
                }
            } else {
                result += System.lineSeparator() + "No SlrParcels to process";
            }
        } catch (Exception ex) {
            result += System.lineSeparator() + "Processed " + count + " records";
            if (current != null) {
                result += System.lineSeparator() + "Current SlrParcel = "
                        + current.getAdjudicationParcelNumber() + ", Lease: " + current.getLeaseNumber();
            }
            result += System.lineSeparator() + "EXCEPTION > " + FaultUtility.getStackTraceAsString(ex);
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        result += System.lineSeparator() + "SlrParcel transfer completed in " + elapsed + "s";
        LogUtility.log(result);
        return result;
    }

    /**
     * Loads the slr_parcel records into the cadastre schema. The records and
     * data attributes to update/modify can be controlled by setting the update
     * flags on the slr_parcel table.
     *
     * @return Summary messages describing the progress of the load.
     */
    @Override
    public String loadParcel() {
        String result = "";
        long startTime = System.currentTimeMillis();
        try {
            result += System.lineSeparator() + "Load Parcel started: "
                    + DateUtility.getDateTimeString(DateUtility.now(), DateFormat.MEDIUM, DateFormat.LONG);

            // Only disable triggers on cadastre.cadastre_object as the triggers try to insert a new 
            // spatial_unit record and set the name_firstpart and name_lastpart based on the location 
            // of the geom. It is not necessary to disable triggers on the other tables. 
            result += System.lineSeparator() + "Disable triggers on cadastre.cadastre_object";
            Map params = new HashMap<String, Object>();
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildDisableTriggerSql("cadastre.cadastre_object"));
            getRepository().bulkUpdate(params);

            result += System.lineSeparator() + "Load new parcel records into SOLA";
            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertSpatialUnitSql());
            int rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Inserted " + rows + " records into cadastre.spatial_unit";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertCadastreObjectSql());
            rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Inserted " + rows + " records into cadastre.cadastre_object";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY,
                    SlrMigrationSqlProvider.buildInsertSpatialValueAreaSql(SlrMigrationSqlProvider.OFFICIAL_AREA_TYPE));
            rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Inserted " + rows + " officalArea records into cadastre.spatial_value_area";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY,
                    SlrMigrationSqlProvider.buildInsertSpatialValueAreaSql(SlrMigrationSqlProvider.CALCULATED_AREA_TYPE));
            rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Inserted " + rows + " calculatedArea records into cadastre.spatial_value_area";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertAddressSql());
            rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Inserted " + rows + " records into address.address";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertParcelAddressSql());
            rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Inserted " + rows + " records into cadastre.spatial_unit_address";

            result += System.lineSeparator() + "Update existing parcel records in SOLA";
            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildUpdateCadastreObjectSql());
            rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Updated " + rows + " records in cadastre.cadastre_object";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY,
                    SlrMigrationSqlProvider.buildUpdateSpatialValueAreaSql(SlrMigrationSqlProvider.OFFICIAL_AREA_TYPE));
            rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Updated " + rows + " official areas in cadastre.spatial_value_area";

            // Does not update the calculated area values as these can remain unchanged. 

            result += System.lineSeparator() + "Enable triggers on cadastre.cadastre_object";
            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildEnableTriggerSql("cadastre.cadastre_object"));
            getRepository().bulkUpdate(params);

        } catch (Exception ex) {
            result += System.lineSeparator() + "EXCEPTION > " + FaultUtility.getStackTraceAsString(ex);
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        result += System.lineSeparator() + "Load Parcels completed in " + elapsed + "s";
        LogUtility.log(result);
        return result;
    }

    /**
     * Transfers lease details from the Lesotho (SLR) database into the SOLA SLR
     * schema.
     *
     * @param registrationDate The date to use as the registration date for the
     * leases If null, a default value of 15-Aug-2013 will be used.
     * @param adjudicationArea Used to limit the number of records returned
     * based on the adjudication area (e.g. 11-1, 30-1, etc)
     * @param registeredOnly If true, only leases associated with a registered
     * SAR1 from are retrieved. If false,all leases that are at the stage of
     * being submitted to LAA are retrieved.
     * @param fromDate Used with toDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     * @param toDate Used with fromDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     * @return Summary messages describing the progress of the transfer.
     */
    @Override
    public String transferSlrLease(Date registrationDate, String adjudicationArea,
            boolean registeredOnly, Date fromDate, Date toDate) {
        String result = "";
        registrationDate = registrationDate == null
                ? new GregorianCalendar(2013, GregorianCalendar.AUGUST, 15).getTime() : registrationDate;
        int count = 0;
        SlrLease current = null;
        long startTime = System.currentTimeMillis();
        try {
            result += System.lineSeparator() + "Retrieving SlrLeases with parameters;"
                    + " Registration Date = " + DateUtility.getMediumDateString(registrationDate, false)
                    + ", Adjudication Area = " + (adjudicationArea == null ? "null" : adjudicationArea)
                    + ", Registered Only = " + registeredOnly
                    + ", fromDate = " + (fromDate == null ? "null" : DateUtility.getMediumDateString(fromDate, false))
                    + ", toDate = " + (toDate == null ? "null" : DateUtility.getMediumDateString(toDate, false));
            result += System.lineSeparator() + "SlrLease transfer started: "
                    + DateUtility.getDateTimeString(DateUtility.now(), DateFormat.MEDIUM, DateFormat.LONG);

            Map params = new HashMap<String, Object>();
            // Remove all records from the slr.slr_lease table first
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildDeleteSlrLeaseSql());
            int rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Deleted " + rows + " rows from slr.slr_lease";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            int numAreas = prepAdjudicationAreaParams(adjudicationArea, params);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildGetSlrLeaseSql(numAreas, registeredOnly, fromDate, toDate));
            params.put(SlrMigrationSqlProvider.QUERY_PARAM_FROM_DATE, fromDate);
            params.put(SlrMigrationSqlProvider.QUERY_PARAM_TO_DATE, toDate);
            List<SlrLease> leases = getSlrRepository().getEntityList(SlrLease.class, params);
            if (leases != null) {
                result += System.lineSeparator() + leases.size() + " SlrLeases to process";
                for (SlrLease l : leases) {
                    current = l;
                    // Configure the entity so that it will be inserted into the database
                    l.clearOriginalValues();
                    l.setLoaded(false);
                    l.setRegDate(registrationDate);
                    // Save each new slr_lease record into the SOLA database
                    getRepository().saveEntity(l);
                    count++;
                }

                // Determine the leases that already exist in SOLA and flag them  
                params.remove(CommonSqlProvider.PARAM_QUERY);
                params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildMatchSlrLeaseSql());
                rows = getRepository().bulkUpdate(params);
                result += System.lineSeparator() + "Found " + rows + " SlrLease records that match leases "
                        + "in SOLA. The matched leases will not be uploaded to SOLA.";

                // Determine the parcel (i.e. cadastre_object) matching the lease number  
                params.remove(CommonSqlProvider.PARAM_QUERY);
                params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildUpdateSlrLeaseParcelSql());
                rows = getRepository().bulkUpdate(params);
                result += System.lineSeparator() + "Matched " + rows + " cadastre_objects to the leases that will be uploaded.";
            } else {
                result += System.lineSeparator() + "No SlrLeases to process";
            }
        } catch (Exception ex) {
            result += System.lineSeparator() + "Processed " + count + " records";
            if (current != null) {
                result += System.lineSeparator() + "Current SlrLease = "
                        + current.getAdjudicationParcelNumber();
            }
            result += System.lineSeparator() + "EXCEPTION > " + FaultUtility.getStackTraceAsString(ex);
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        result += System.lineSeparator() + "SlrLease transfer completed in " + elapsed + "s";
        LogUtility.log(result);
        return result;
    }

    /**
     * Transfers lease details from the Lesotho (SLR) database into the SOLA SLR
     * schema.
     *
     * @param adjudicationArea Used to limit the number of records returned
     * based on the adjudication area (e.g. 11-1, 30-1, etc)
     * @param registeredOnly If true, only leases associated with a registered
     * SAR1 from are retrieved. If false,all leases that are at the stage of
     * being submitted to LAA are retrieved.
     * @param fromDate Used with toDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     * @param toDate Used with fromDate to limit the number of records returned
     * by checking the lastModified date of the dbo.mfDocuments table.
     * @return Summary messages describing the progress of the transfer.
     */
    @Override
    public String transferSlrParty(String adjudicationArea, boolean registeredOnly,
            Date fromDate, Date toDate) {
        String result = "";
        int count = 0;
        SlrParty current = null;
        long startTime = System.currentTimeMillis();
        try {
            result += System.lineSeparator() + "Retrieving SlrParties with parameters;"
                    + " Adjudication Area = " + (adjudicationArea == null ? "null" : adjudicationArea)
                    + ", Registered Only = " + registeredOnly
                    + ", fromDate = " + (fromDate == null ? "null" : DateUtility.getMediumDateString(fromDate, false))
                    + ", toDate = " + (toDate == null ? "null" : DateUtility.getMediumDateString(toDate, false));
            result += System.lineSeparator() + "SlrParty transfer started: "
                    + DateUtility.getDateTimeString(DateUtility.now(), DateFormat.MEDIUM, DateFormat.LONG);

            Map params = new HashMap<String, Object>();
            // Remove all records from the slr.slr_party table first
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildDeleteSlrPartySql());
            int rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Deleted " + rows + " rows from slr.slr_party";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            int numAreas = prepAdjudicationAreaParams(adjudicationArea, params);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildGetSlrPartySql(numAreas, registeredOnly, fromDate, toDate));
            params.put(SlrMigrationSqlProvider.QUERY_PARAM_FROM_DATE, fromDate);
            params.put(SlrMigrationSqlProvider.QUERY_PARAM_TO_DATE, toDate);
            List<SlrParty> parties = getSlrRepository().getEntityList(SlrParty.class, params);
            if (parties != null) {
                result += System.lineSeparator() + parties.size() + " SlrParty records to process";
                for (SlrParty p : parties) {
                    current = p;
                    // Configure the entity so that it will be inserted into the database
                    p.clearOriginalValues();
                    p.setLoaded(false);
                    // Save each new slr_party record into the SOLA database
                    getRepository().saveEntity(p);
                    count++;
                }

                // Determine the parties that already exist in SOLA and flag them  
                params.remove(CommonSqlProvider.PARAM_QUERY);
                params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildMatchSlrPartySql());
                rows = getRepository().bulkUpdate(params);
                result += System.lineSeparator() + "Found " + rows + " SlrParty records that match party records "
                        + "in SOLA. The matched parties will not be uploaded to SOLA.";
            } else {
                result += System.lineSeparator() + "No SlrParty records to process";
            }
        } catch (Exception ex) {
            result += System.lineSeparator() + "Processed " + count + " records";
            if (current != null) {
                result += System.lineSeparator() + "Current SlrParty = "
                        + current.getSlrReference();
            }
            result += System.lineSeparator() + "EXCEPTION > " + FaultUtility.getStackTraceAsString(ex);
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        result += System.lineSeparator() + "SlrParty transfer completed in " + elapsed + "s";
        LogUtility.log(result);
        return result;
    }

    /**
     * Loads the slr_lease and slr_party records into the party and
     * administrative schemas. This method only inserts new records. If an
     * existing record is found, it is left unchanged.
     *
     * @return Summary messages describing the progress of the load.
     */
    @Override
    public String loadLeaseAndParty() {
        String result = "";
        long startTime = System.currentTimeMillis();
        try {
            result += System.lineSeparator() + "Load Lease and Party started: "
                    + DateUtility.getDateTimeString(DateUtility.now(), DateFormat.MEDIUM, DateFormat.LONG);

            result += System.lineSeparator() + "Load new party records into SOLA";
            Map params = new HashMap<String, Object>();
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertPartyAddressSql());
            int rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Inserted " + rows + " records into address.address";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertPartySql());
            rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Inserted " + rows + " records into party.party";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertPartyRoleSql());
            rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Inserted " + rows + " records into party.party_role";

            result += System.lineSeparator() + "Load new lease records into SOLA";
            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertBaUnitSql());
            rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Inserted " + rows + " records into administrative.ba_unit";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertRrrSql());
            rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Inserted " + rows + " records into administrative.rrr";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertNotationSql());
            rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Inserted " + rows + " records into administrative.notation";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertPartyForRrrSql());
            rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Inserted " + rows + " records into administrative.party_for_rrr";

        } catch (Exception ex) {
            result += System.lineSeparator() + "EXCEPTION > " + FaultUtility.getStackTraceAsString(ex);
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        result += System.lineSeparator() + "Load Lease and Party completed in " + elapsed + "s";
        LogUtility.log(result);
        return result;
    }

    /**
     * Loads records into the administrative.source_describes_rrr to link the
     * Rrr and Source records that have been migrated from the SLR database.
     *
     * @return Summary messages describing the progress of the load.
     */
    @Override
    public String loadRrrSourceLink() {
        String result = "";
        long startTime = System.currentTimeMillis();
        try {
            result += System.lineSeparator() + "Load Rrr Source Link started: "
                    + DateUtility.getDateTimeString(DateUtility.now(), DateFormat.MEDIUM, DateFormat.LONG);

            Map params = new HashMap<String, Object>();
            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertSourceRrrSql());
            int rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Loaded " + rows + " records into administrative.source_describes_rrr";

        } catch (Exception ex) {
            result += System.lineSeparator() + "EXCEPTION > " + FaultUtility.getStackTraceAsString(ex);
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        result += System.lineSeparator() + "Load Rrr Source Link completed in " + elapsed + "s";
        LogUtility.log(result);
        return result;
    }
}
