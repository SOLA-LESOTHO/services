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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.security.RolesAllowed;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.sola.common.DateUtility;
import org.sola.common.RolesConstants;
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
 * EJB to manage data in the SLR Migration processes. Singleton EJB's are
 * subject to READ and WRITE concurrency control to manage access to the EJB
 * methods. To avoid unnecessary blocking of EJB methods, BEAN concurrency is
 * used instead of the default Container Concurrency.
 */
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Singleton
@EJB(name = "java:global/SOLA/SlrMigrationEJBLocal", beanInterface = SlrMigrationEJBLocal.class)
public class SlrMigrationEJB extends AbstractEJB implements SlrMigrationEJBLocal {

    CommonRepository slrRepository;
    volatile String progressMessage;

    /**
     * Sets the entity package for the EJB to
     * SlrSource.class.getPackage().getName(). This is used to restrict the save
     * and retrieval of Code Entities.
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
     * Returns the current progressMessage.
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String getProgressMessage() {
        return progressMessage;
    }

    public void setProgressMessage(String progressMessage) {
        this.progressMessage = progressMessage;
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
    @RolesAllowed(RolesConstants.SLR_MIGRATION)
    public String transferSlrSource(String adjudicationArea, boolean registeredOnly,
            Date fromDate, Date toDate) {
        int count = 0;
        SlrSource current = null;
        progressMessage = "";
        long startTime = System.currentTimeMillis();
        try {
            progressMessage += System.lineSeparator() + "Retrieving SlrSource with parameters;"
                    + " Adjudication Area = " + (adjudicationArea == null ? "null" : adjudicationArea)
                    + ", Registered Only = " + registeredOnly
                    + ", fromDate = " + (fromDate == null ? "null" : DateUtility.getMediumDateString(fromDate, false))
                    + ", toDate = " + (toDate == null ? "null" : DateUtility.getMediumDateString(toDate, false));
            progressMessage += System.lineSeparator() + "SlrSource transfer started: "
                    + DateUtility.getDateTimeString(DateUtility.now(), DateFormat.MEDIUM, DateFormat.LONG);

            Map params = new HashMap<String, Object>();
            // Remove all records from the slr.slr_source table first
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildDeleteSlrSourceSql());
            int rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Deleted " + rows + " rows from slr.slr_source";

            progressMessage += System.lineSeparator() + "Executing query to retrieve source records from SLR database. This may take some time...";
            params.remove(CommonSqlProvider.PARAM_QUERY);
            int numAreas = prepAdjudicationAreaParams(adjudicationArea, params);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildGetSlrSourceSql(registeredOnly, numAreas, fromDate, toDate));
            params.put(SlrMigrationSqlProvider.QUERY_PARAM_FROM_DATE, fromDate);
            params.put(SlrMigrationSqlProvider.QUERY_PARAM_TO_DATE, toDate);

            List<SlrSource> sources = getSlrRepository().getEntityList(SlrSource.class, params);
            if (sources != null) {
                progressMessage += System.lineSeparator() + sources.size() + " SlrSources to transfer";
                for (SlrSource s : sources) {
                    current = s;
                    // Configure the entity so that it will be inserted into the database
                    s.clearOriginalValues();
                    s.setLoaded(false);
                    // Save each new slr_source record into the SOLA database
                    getRepository().saveEntity(s);
                    count++;
                    if (count % 1000 == 0) {
                        progressMessage += System.lineSeparator() + count + " SlrSources transferred...";
                    }
                }
            } else {
                progressMessage += System.lineSeparator() + "No SlrSources to process";
            }
        } catch (Exception ex) {
            progressMessage += System.lineSeparator() + "Processed " + count + " records";
            if (current != null) {
                progressMessage += System.lineSeparator() + "Current SlrSource = "
                        + current.getAdjudicationParcelNumber() + ", FileId: " + current.getExtArchiveId();
            }
            progressMessage += System.lineSeparator() + "EXCEPTION > " + FaultUtility.getStackTraceAsString(ex);
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        progressMessage += System.lineSeparator() + "SlrSource transfer completed in " + elapsed + "s";
        LogUtility.log(progressMessage);
        return progressMessage;
    }

    /**
     * Loads the slr_source records into the source.source table.
     *
     * @return Summary messages describing the progress of the load.
     */
    @Override
    @RolesAllowed(RolesConstants.SLR_MIGRATION)
    public String loadSource() {
        progressMessage = "";
        long startTime = System.currentTimeMillis();
        try {
            progressMessage += System.lineSeparator() + "Load Source started: "
                    + DateUtility.getDateTimeString(DateUtility.now(), DateFormat.MEDIUM, DateFormat.LONG);

            progressMessage += System.lineSeparator() + "Disable triggers on source.source";
            Map params = new HashMap<String, Object>();
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildDisableTriggerSql("source.source"));
            getRepository().bulkUpdate(params);

            progressMessage += System.lineSeparator() + "Load SLR source records into source.source";
            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildLoadSourceSql());
            int rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Loaded " + rows + " records into source.source";

            progressMessage += System.lineSeparator() + "Enable triggers on source.source";
            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildEnableTriggerSql("source.source"));
            getRepository().bulkUpdate(params);

        } catch (Exception ex) {
            progressMessage += System.lineSeparator() + "EXCEPTION > " + FaultUtility.getStackTraceAsString(ex);
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        progressMessage += System.lineSeparator() + "Load Source completed in " + elapsed + "s";
        LogUtility.log(progressMessage);
        return progressMessage;
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
    @RolesAllowed(RolesConstants.SLR_MIGRATION)
    public String transferSlrParcel(String adjudicationArea, Date fromDate, Date toDate) {
        progressMessage = "";
        int count = 0;
        SlrParcel current = null;
        long startTime = System.currentTimeMillis();
        try {
            progressMessage += System.lineSeparator() + "Retrieving SlrParcel with parameters;"
                    + " Adjudication Area = " + (adjudicationArea == null ? "null" : adjudicationArea)
                    + ", fromDate = " + (fromDate == null ? "null" : DateUtility.getMediumDateString(fromDate, false))
                    + ", toDate = " + (toDate == null ? "null" : DateUtility.getMediumDateString(toDate, false));
            progressMessage += System.lineSeparator() + "SlrParcel transfer started: "
                    + DateUtility.getDateTimeString(DateUtility.now(), DateFormat.MEDIUM, DateFormat.LONG);

            Map params = new HashMap<String, Object>();
            // Remove all records from the slr.slr_parcel table first
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildDeleteSlrParcelSql());
            int rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Deleted " + rows + " rows from slr.slr_parcel";

            progressMessage += System.lineSeparator() + "Executing query to retrieve parcel records from SLR database. This may take some time...";
            params.remove(CommonSqlProvider.PARAM_QUERY);
            int numAreas = prepAdjudicationAreaParams(adjudicationArea, params);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildGetSlrParcelSql(numAreas, fromDate, toDate));
            params.put(SlrMigrationSqlProvider.QUERY_PARAM_FROM_DATE, fromDate);
            params.put(SlrMigrationSqlProvider.QUERY_PARAM_TO_DATE, toDate);
            List<SlrParcel> parcels = getSlrRepository().getEntityList(SlrParcel.class, params);
            if (parcels != null) {
                progressMessage += System.lineSeparator() + parcels.size() + " SlrParcels to transfer";
                for (SlrParcel p : parcels) {
                    current = p;
                    // Configure the entity so that it will be inserted into the database
                    p.clearOriginalValues();
                    p.setLoaded(false);
                    // Save each new slr_parcel record into the SOLA database
                    getRepository().saveEntity(p);
                    count++;
                    if (count % 1000 == 0) {
                        progressMessage += System.lineSeparator() + count + " SlrParcels transferred...";
                    }
                }


                // Set the flags on the slr_parcel table to indicate which data attributes will be updated
                // as well has identify the new records that will be created. 
                progressMessage += System.lineSeparator() + "Transfer completed. Determining attributes to update...";
                params.remove(CommonSqlProvider.PARAM_QUERY);
                params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildUpdateSlrParcelSql());
                rows = getRepository().bulkUpdate(params);
                progressMessage += System.lineSeparator() + "Updated " + rows + " rows in slr.slr_parcel. Check the"
                        + " slr.slr_parcel table and modify as required to control that data that will be loaded"
                        + " into SOLA. ";

                // Run validations on the SlrParcel records
                params.remove(CommonSqlProvider.PARAM_QUERY);
                params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildValidateSlrParcelSql());
                List<SlrValidation> list = getRepository().getEntityList(SlrValidation.class, params);
                if (list != null && list.size() > 0) {
                    progressMessage += System.lineSeparator() + list.size() + " validation messages...";
                    for (SlrValidation v : list) {
                        if (v.getMsg().equals("MULTIPOLYGON")) {
                            // SOLA only accepts parcels with a geometry type of POLYGON
                            progressMessage += System.lineSeparator() + "Parcel has multipolygon geometry and will not be loaded into SOLA;"
                                    + " Lease Num = " + v.getLeaseNumber() + ", APN = " + v.getApn();
                        }
                    }
                }
            } else {
                progressMessage += System.lineSeparator() + "No SlrParcels to process";
            }
        } catch (Exception ex) {
            progressMessage += System.lineSeparator() + "Processed " + count + " records";
            if (current != null) {
                progressMessage += System.lineSeparator() + "Current SlrParcel = "
                        + current.getAdjudicationParcelNumber() + ", Lease: " + current.getLeaseNumber();
            }
            progressMessage += System.lineSeparator() + "EXCEPTION > " + FaultUtility.getStackTraceAsString(ex);
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        progressMessage += System.lineSeparator() + "SlrParcel transfer completed in " + elapsed + "s";
        LogUtility.log(progressMessage);
        return progressMessage;
    }

    /**
     * Loads the slr_parcel records into the cadastre schema. The records and
     * data attributes to update/modify can be controlled by setting the update
     * flags on the slr_parcel table.
     *
     * @return Summary messages describing the progress of the load.
     */
    @Override
    @RolesAllowed(RolesConstants.SLR_MIGRATION)
    public String loadParcel() {
        progressMessage = "";
        long startTime = System.currentTimeMillis();
        try {
            progressMessage += System.lineSeparator() + "Load Parcel started: "
                    + DateUtility.getDateTimeString(DateUtility.now(), DateFormat.MEDIUM, DateFormat.LONG);

            // Only disable triggers on cadastre.cadastre_object as the triggers try to insert a new 
            // spatial_unit record and set the name_firstpart and name_lastpart based on the location 
            // of the geom. It is not necessary to disable triggers on the other tables. 
            progressMessage += System.lineSeparator() + "Disable triggers on cadastre.cadastre_object";
            Map params = new HashMap<String, Object>();
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildDisableTriggerSql("cadastre.cadastre_object"));
            getRepository().bulkUpdate(params);

            progressMessage += System.lineSeparator() + "Load new parcel records into SOLA";
            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertSpatialUnitSql());
            int rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Inserted " + rows + " records into cadastre.spatial_unit";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertCadastreObjectSql());
            rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Inserted " + rows + " records into cadastre.cadastre_object";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY,
                    SlrMigrationSqlProvider.buildInsertSpatialValueAreaSql(SlrMigrationSqlProvider.OFFICIAL_AREA_TYPE));
            rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Inserted " + rows + " officalArea records into cadastre.spatial_value_area";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY,
                    SlrMigrationSqlProvider.buildInsertSpatialValueAreaSql(SlrMigrationSqlProvider.CALCULATED_AREA_TYPE));
            rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Inserted " + rows + " calculatedArea records into cadastre.spatial_value_area";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertAddressSql());
            rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Inserted " + rows + " records into address.address";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertParcelAddressSql());
            rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Inserted " + rows + " records into cadastre.spatial_unit_address";

            progressMessage += System.lineSeparator() + "Update existing parcel records in SOLA";
            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildUpdateCadastreObjectSql());
            rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Updated " + rows + " records in cadastre.cadastre_object";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY,
                    SlrMigrationSqlProvider.buildUpdateSpatialValueAreaSql(SlrMigrationSqlProvider.OFFICIAL_AREA_TYPE));
            rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Updated " + rows + " official areas in cadastre.spatial_value_area";

            // Does not update the calculated area values as these can remain unchanged. 

            progressMessage += System.lineSeparator() + "Enable triggers on cadastre.cadastre_object";
            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildEnableTriggerSql("cadastre.cadastre_object"));
            getRepository().bulkUpdate(params);

        } catch (Exception ex) {
            progressMessage += System.lineSeparator() + "EXCEPTION > " + FaultUtility.getStackTraceAsString(ex);
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        progressMessage += System.lineSeparator() + "Load Parcels completed in " + elapsed + "s";
        LogUtility.log(progressMessage);
        return progressMessage;
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
    @RolesAllowed(RolesConstants.SLR_MIGRATION)
    public String transferSlrLease(Date registrationDate, String adjudicationArea,
            boolean registeredOnly, Date fromDate, Date toDate) {
        progressMessage = "";
        registrationDate = registrationDate == null
                ? new GregorianCalendar(2013, GregorianCalendar.AUGUST, 15).getTime() : registrationDate;
        int count = 0;
        SlrLease current = null;
        long startTime = System.currentTimeMillis();
        try {
            progressMessage += System.lineSeparator() + "Retrieving SlrLeases with parameters;"
                    + " Registration Date = " + DateUtility.getMediumDateString(registrationDate, false)
                    + ", Adjudication Area = " + (adjudicationArea == null ? "null" : adjudicationArea)
                    + ", Registered Only = " + registeredOnly
                    + ", fromDate = " + (fromDate == null ? "null" : DateUtility.getMediumDateString(fromDate, false))
                    + ", toDate = " + (toDate == null ? "null" : DateUtility.getMediumDateString(toDate, false));
            progressMessage += System.lineSeparator() + "SlrLease transfer started: "
                    + DateUtility.getDateTimeString(DateUtility.now(), DateFormat.MEDIUM, DateFormat.LONG);

            Map params = new HashMap<String, Object>();
            // Remove all records from the slr.slr_lease table first
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildDeleteSlrLeaseSql());
            int rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Deleted " + rows + " rows from slr.slr_lease";

            progressMessage += System.lineSeparator() + "Executing query to retrieve lease records from SLR database. This may take some time...";
            params.remove(CommonSqlProvider.PARAM_QUERY);
            int numAreas = prepAdjudicationAreaParams(adjudicationArea, params);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildGetSlrLeaseSql(numAreas, registeredOnly, fromDate, toDate));
            params.put(SlrMigrationSqlProvider.QUERY_PARAM_FROM_DATE, fromDate);
            params.put(SlrMigrationSqlProvider.QUERY_PARAM_TO_DATE, toDate);
            List<SlrLease> leases = getSlrRepository().getEntityList(SlrLease.class, params);
            if (leases != null) {
                progressMessage += System.lineSeparator() + leases.size() + " SlrLeases to transfer";
                for (SlrLease l : leases) {
                    current = l;
                    // Configure the entity so that it will be inserted into the database
                    l.clearOriginalValues();
                    l.setLoaded(false);
                    l.setRegDate(registrationDate);
                    // Save each new slr_lease record into the SOLA database
                    getRepository().saveEntity(l);
                    count++;
                    if (count % 1000 == 0) {
                        progressMessage += System.lineSeparator() + count + " SlrLeases transferred...";
                    }
                }

                // Determine the leases that already exist in SOLA and flag them 
                progressMessage += System.lineSeparator() + count + "Transfer completed. Checking for leases that already exist in SOLA...";
                params.remove(CommonSqlProvider.PARAM_QUERY);
                params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildMatchSlrLeaseSql());
                rows = getRepository().bulkUpdate(params);
                progressMessage += System.lineSeparator() + "Found " + rows + " SlrLease records that match leases "
                        + "in SOLA. The matched leases will not be uploaded to SOLA.";

                // Determine the parcel (i.e. cadastre_object) matching the lease number  
                params.remove(CommonSqlProvider.PARAM_QUERY);
                params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildUpdateSlrLeaseParcelSql());
                rows = getRepository().bulkUpdate(params);
                progressMessage += System.lineSeparator() + "Matched " + rows + " cadastre_objects to the leases that will be uploaded.";
            } else {
                progressMessage += System.lineSeparator() + "No SlrLeases to process";
            }
        } catch (Exception ex) {
            progressMessage += System.lineSeparator() + "Processed " + count + " records";
            if (current != null) {
                progressMessage += System.lineSeparator() + "Current SlrLease = "
                        + current.getAdjudicationParcelNumber();
            }
            progressMessage += System.lineSeparator() + "EXCEPTION > " + FaultUtility.getStackTraceAsString(ex);
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        progressMessage += System.lineSeparator() + "SlrLease transfer completed in " + elapsed + "s";
        LogUtility.log(progressMessage);
        return progressMessage;
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
    @RolesAllowed(RolesConstants.SLR_MIGRATION)
    public String transferSlrParty(String adjudicationArea, boolean registeredOnly,
            Date fromDate, Date toDate) {
        progressMessage = "";
        int count = 0;
        SlrParty current = null;
        long startTime = System.currentTimeMillis();
        try {
            progressMessage += System.lineSeparator() + "Retrieving SlrParties with parameters;"
                    + " Adjudication Area = " + (adjudicationArea == null ? "null" : adjudicationArea)
                    + ", Registered Only = " + registeredOnly
                    + ", fromDate = " + (fromDate == null ? "null" : DateUtility.getMediumDateString(fromDate, false))
                    + ", toDate = " + (toDate == null ? "null" : DateUtility.getMediumDateString(toDate, false));
            progressMessage += System.lineSeparator() + "SlrParty transfer started: "
                    + DateUtility.getDateTimeString(DateUtility.now(), DateFormat.MEDIUM, DateFormat.LONG);

            Map params = new HashMap<String, Object>();
            // Remove all records from the slr.slr_party table first
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildDeleteSlrPartySql());
            int rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Deleted " + rows + " rows from slr.slr_party";

            progressMessage += System.lineSeparator() + "Executing query to retrieve party records from SLR database. This may take some time...";
            params.remove(CommonSqlProvider.PARAM_QUERY);
            int numAreas = prepAdjudicationAreaParams(adjudicationArea, params);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildGetSlrPartySql(numAreas, registeredOnly, fromDate, toDate));
            params.put(SlrMigrationSqlProvider.QUERY_PARAM_FROM_DATE, fromDate);
            params.put(SlrMigrationSqlProvider.QUERY_PARAM_TO_DATE, toDate);
            List<SlrParty> parties = getSlrRepository().getEntityList(SlrParty.class, params);
            if (parties != null) {
                progressMessage += System.lineSeparator() + parties.size() + " SlrParty records to transfer";
                for (SlrParty p : parties) {
                    current = p;
                    // Configure the entity so that it will be inserted into the database
                    p.clearOriginalValues();
                    p.setLoaded(false);
                    // Save each new slr_party record into the SOLA database
                    getRepository().saveEntity(p);
                    count++;
                    if (count % 1000 == 0) {
                        progressMessage += System.lineSeparator() + count + " SlrParties transferred...";
                    }
                }

                // Determine the parties that already exist in SOLA and flag them 
                progressMessage += System.lineSeparator() + count + "Transfer completed. Checking for parties that already exist in SOLA...";
                params.remove(CommonSqlProvider.PARAM_QUERY);
                params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildMatchSlrPartySql());
                rows = getRepository().bulkUpdate(params);
                progressMessage += System.lineSeparator() + "Found " + rows + " SlrParty records that match party records "
                        + "in SOLA. The matched parties will not be uploaded to SOLA.";
            } else {
                progressMessage += System.lineSeparator() + "No SlrParty records to process";
            }
        } catch (Exception ex) {
            progressMessage += System.lineSeparator() + "Processed " + count + " records";
            if (current != null) {
                progressMessage += System.lineSeparator() + "Current SlrParty = "
                        + current.getSlrReference();
            }
            progressMessage += System.lineSeparator() + "EXCEPTION > " + FaultUtility.getStackTraceAsString(ex);
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        progressMessage += System.lineSeparator() + "SlrParty transfer completed in " + elapsed + "s";
        LogUtility.log(progressMessage);
        return progressMessage;
    }

    /**
     * Loads the slr_lease and slr_party records into the party and
     * administrative schemas. This method only inserts new records. If an
     * existing record is found, it is left unchanged.
     *
     * @param makeCurrent The transerSlrLease method can be used to transfer
     * leases that have been submitted to LAA, but not yet marked as registered
     * in the SLR database. By default these leases are transfered with a
     * 'pending' status into SOLA. To force these leases to have a 'current'
     * status in SOLA, set makeCurrent = true. When makeCurrent is false the
     * status of the lease is determined based on its status in the SLR
     * database.
     * @return Summary messages describing the progress of the load.
     */
    @Override
    @RolesAllowed(RolesConstants.SLR_MIGRATION)
    public String loadLeaseAndParty(boolean makeCurrent) {
        progressMessage = "";
        long startTime = System.currentTimeMillis();
        try {
            progressMessage += System.lineSeparator() + "Load Lease and Party started: "
                    + DateUtility.getDateTimeString(DateUtility.now(), DateFormat.MEDIUM, DateFormat.LONG);

            Map params = new HashMap<String, Object>();
            if (makeCurrent) {
                params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildUpdateSlrLeaseStatusSql());
                int rows = getRepository().bulkUpdate(params);
                progressMessage += System.lineSeparator() + "Updated " + rows + " SlrLease records to have the 'current' status";
            }

            progressMessage += System.lineSeparator() + "Load new party records into SOLA";
            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertPartyAddressSql());
            int rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Inserted " + rows + " records into address.address";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertPartySql());
            rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Inserted " + rows + " records into party.party";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertPartyRoleSql());
            rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Inserted " + rows + " records into party.party_role";

            progressMessage += System.lineSeparator() + "Load new lease records into SOLA";
            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertBaUnitSql());
            rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Inserted " + rows + " records into administrative.ba_unit";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertRrrSql());
            rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Inserted " + rows + " records into administrative.rrr";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertNotationSql());
            rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Inserted " + rows + " records into administrative.notation";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertPartyForRrrSql());
            rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Inserted " + rows + " records into administrative.party_for_rrr";

        } catch (Exception ex) {
            progressMessage += System.lineSeparator() + "EXCEPTION > " + FaultUtility.getStackTraceAsString(ex);
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        progressMessage += System.lineSeparator() + "Load Lease and Party completed in " + elapsed + "s";
        LogUtility.log(progressMessage);
        return progressMessage;
    }

    /**
     * Loads records into the administrative.source_describes_rrr to link the
     * Rrr and Source records that have been migrated from the SLR database.
     *
     * @return Summary messages describing the progress of the load.
     */
    @Override
    @RolesAllowed(RolesConstants.SLR_MIGRATION)
    public String loadRrrSourceLink() {
        progressMessage = "";
        long startTime = System.currentTimeMillis();
        try {
            progressMessage += System.lineSeparator() + "Load Rrr Source Link started: "
                    + DateUtility.getDateTimeString(DateUtility.now(), DateFormat.MEDIUM, DateFormat.LONG);

            Map params = new HashMap<String, Object>();
            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildInsertSourceRrrSql());
            int rows = getRepository().bulkUpdate(params);
            progressMessage += System.lineSeparator() + "Loaded " + rows + " records into administrative.source_describes_rrr";

        } catch (Exception ex) {
            progressMessage += System.lineSeparator() + "EXCEPTION > " + FaultUtility.getStackTraceAsString(ex);
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        progressMessage += System.lineSeparator() + "Load Rrr Source Link completed in " + elapsed + "s";
        LogUtility.log(progressMessage);
        return progressMessage;
    }
}
