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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.sola.common.DateUtility;
import org.sola.common.logging.LogUtility;
import org.sola.services.common.ejbs.AbstractEJB;
import org.sola.services.common.faults.FaultUtility;
import org.sola.services.common.repository.CommonRepository;
import org.sola.services.common.repository.CommonRepositoryImpl;
import org.sola.services.common.repository.CommonSqlProvider;
import org.sola.services.common.repository.DatabaseConnectionManager;
import org.sola.services.ejb.slrmigration.repository.SlrMigrationSqlProvider;
import org.sola.services.ejb.slrmigration.repository.entities.SlrSource;

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
     * Transfers a summary of the documents in the Lesotho (SLR) database into
     * the SOLA SLR schema.
     *
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
    public String transferSlrSource(boolean registeredOnly, Date fromDate, Date toDate) {
        String result = "";
        int count = 0;
        SlrSource current = null;
        long startTime = System.currentTimeMillis();
        try {
            result += System.lineSeparator() + "Retrieving SlrSource with parameters;"
                    + " Registered Only = " + registeredOnly
                    + " fromDate = " + (fromDate == null ? "null" : DateUtility.getMediumDateString(fromDate, false))
                    + " toDate = " + (toDate == null ? "null" : DateUtility.getMediumDateString(toDate, false));
            result += System.lineSeparator() + "SlrSource prepare started: "
                    + DateUtility.getDateTimeString(DateUtility.now(), DateFormat.MEDIUM, DateFormat.LONG);

            Map params = new HashMap<String, Object>();
            // Remove all records from the slr.slr_source table first
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildDeleteSlrSourceSql());
            int rows = getRepository().bulkUpdate(params);
            result += System.lineSeparator() + "Deleted " + rows + " rows from slr.slr_source";

            params.remove(CommonSqlProvider.PARAM_QUERY);
            params.put(CommonSqlProvider.PARAM_QUERY, SlrMigrationSqlProvider.buildGetSlrSourceSql(registeredOnly, fromDate, toDate));
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
}
