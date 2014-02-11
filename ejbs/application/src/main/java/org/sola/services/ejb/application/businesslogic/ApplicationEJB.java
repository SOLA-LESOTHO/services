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
package org.sola.services.ejb.application.businesslogic;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.sola.common.*;
import org.sola.common.messaging.ServiceMessage;
import org.sola.services.common.br.ValidationResult;
import org.sola.services.common.ejbs.AbstractEJB;
import org.sola.services.common.faults.SOLAValidationException;
import org.sola.services.common.repository.CommonSqlProvider;
import org.sola.services.ejb.administrative.businesslogic.AdministrativeEJBLocal;
import org.sola.services.ejb.application.repository.entities.*;
import org.sola.services.ejb.cadastre.businesslogic.CadastreEJBLocal;
import org.sola.services.ejb.party.repository.entities.Party;
import org.sola.services.ejb.source.businesslogic.SourceEJBLocal;
import org.sola.services.ejb.source.repository.entities.Source;
import org.sola.services.ejb.system.businesslogic.SystemEJBLocal;
import org.sola.services.ejb.system.repository.entities.BrValidation;
import org.sola.services.ejb.transaction.businesslogic.TransactionEJBLocal;
import org.sola.services.ejb.transaction.repository.entities.RegistrationStatusType;
import org.sola.services.ejb.transaction.repository.entities.TransactionBasic;

/**
 * EJB to manage data in the application schema. Supports retrieving and saving
 * Applications and Services as well as applying case management actions to
 * applications. Also provides methods for retrieving reference codes from the
 * application schema.
 */
@Stateless
@EJB(name = "java:global/SOLA/ApplicationEJBLocal", beanInterface = ApplicationEJBLocal.class)
public class ApplicationEJB extends AbstractEJB implements ApplicationEJBLocal {

    @EJB
    private SourceEJBLocal sourceEJB;
    @EJB
    private SystemEJBLocal systemEJB;
    @EJB
    private TransactionEJBLocal transactionEJB;
    @EJB
    private AdministrativeEJBLocal administrativeEJB;
    @EJB
    private CadastreEJBLocal cadastreEJB;

    /**
     * Sets the entity package for the EJB to
     * Application.class.getPackage().getName(). This is used to restrict the
     * save and retrieval of Code Entities.
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
        setEntityPackage(Application.class.getPackage().getName());
    }

    /**
     * Clears the LaNr on all new source records associated with the
     * application.
     *
     * @param application The application to check.
     */
    private void treatApplicationSources(Application application) {
        if (application.getSourceList() != null) {
            for (Source source : application.getSourceList()) {
                //Elton. New sources must be sure to have LaNr null. So it can be assigned from
                // the system.
                if (source.isNew()) {
                    source.setLaNr(null);
                }
            }
        }
    }

    /**
     * Returns an application based on the id value. <p>Requires the {@linkplain RolesConstants#APPLICATION_VIEW_APPS}
     * role.</p>
     *
     * @param id The id of the application to retrieve
     * @return The found application or null.
     */
    @Override
    @RolesAllowed(RolesConstants.APPLICATION_VIEW_APPS)
    public Application getApplication(String id) {
        Application result = null;
        if (id != null) {
            result = getRepository().getEntity(Application.class, id);
        }
        return result;
    }

    /**
     * Creates a new application record and any new child objects. Sets the
     * initial action for the application (e.g. lodged) using a business rule.
     * Also sets the lodged date and expected completion date. <p>Requires the {@linkplain RolesConstants#APPLICATION_CREATE_APPS}
     * role.</p>
     *
     * @param application The application to insert
     * @return The application after the insert.
     */
    @Override
    @RolesAllowed(RolesConstants.APPLICATION_CREATE_APPS)
    public Application createApplication(Application application) {
        if (application == null) {
            return application;
        }

        if (application.getLodgingDatetime() == null) {
            application.setLodgingDatetime(DateUtility.now());
        }
        if (application.getServiceList() != null) {
            for (Service ser : application.getServiceList()) {
                ser.setLodgingDatetime(application.getLodgingDatetime());
            }
        }

        calculateFeesAndDates(application);
        treatApplicationSources(application);
        application.getContactPerson().setTypeCode(Party.TYPE_CODE_NATURAL_PERSON);
        application = getRepository().saveEntity(application);

        return application;
    }

    /**
     * Calculates the lodgement fees as well as the expected completions dates
     * for each service as well as the application.
     *
     * @param application The application to calculate fees and set completion
     * dates.
     * @return The application with the fees and completion dates set
     * @see #calculateCompletionDates(Application) calculateCompletionDates
     * @see #calculateLodgementFees(Application) calculateLodgementFees
     */
    @Override
    public Application calculateFeesAndDates(Application application) {
        if (application == null) {
            return application;
        }
        calculateCompletionDates(application);
        calculateLodgementFees(application);
        return application;
    }

    /**
     * Determines the completion dates for each service based on the number of
     * days to complete for the service type. Also determines the application
     * complete date as the maximum of the service completion dates.
     *
     * @param application
     */
    private void calculateCompletionDates(Application application) {

        //Elton: Not important in which language the request types are asked
        List<RequestType> requestTypes = this.getRequestTypes("en");
        Date baseDate = application.getLodgingDatetime();
        if (baseDate == null) {
            baseDate = DateUtility.now();
        }
        if (application.getServiceList() != null && requestTypes != null) {
            for (Service ser : application.getServiceList()) {
                ser.setExpectedCompletionDate(
                        calculateServiceCompletionDate(requestTypes, ser, baseDate));
                application.setExpectedCompletionDate(
                        DateUtility.maxDate(ser.getExpectedCompletionDate(),
                        application.getExpectedCompletionDate()));
            }
        }
    }

    /**
     * Calculates the completion date for the service based using the days to
     * complete for the service request type
     *
     * @param requestTypes The list of request types
     * @param ser The service to calculate the completion date for
     * @param baseDate The date to use as the basis of the calculation.
     * @return The completion date for the service.
     */
    private Date calculateServiceCompletionDate(List<RequestType> requestTypes,
            Service ser, Date baseDate) {
        Date result = DateUtility.now();
        for (RequestType type : requestTypes) {
            if (ser.getRequestTypeCode().equals(type.getCode())) {
                if (type.getNrDaysToComplete() > 0) {
                    result = DateUtility.startOfDay(DateUtility.addDays(baseDate,
                            type.getNrDaysToComplete(), false));
                }
                break;
            }
        }
        return result;
    }

    /**
     * Calculates the fees applicable for lodgement based on the services that
     * have been associated with the application. Values on the application are
     * updated directly.
     *
     * @param application
     */
    private void calculateLodgementFees(Application application) {

        Money totalFee;

        Money registrationFeeTotal = new Money(BigDecimal.ZERO);

        Money servicesFeeTotal = new Money(BigDecimal.ZERO);

        Money stampDutyTotal = new Money(BigDecimal.ZERO);

        Money groundRent = new Money(BigDecimal.ZERO);

 
        // Calculate the fee for each service and the total services fee for the application.
        // Uses the money type to ensure all calculations yeild consisent results. Note that the
        // Money type applies Bankers Rounding to all calculations. 

        //Elton: Not important in this context what language the request types are asked
        List<RequestType> requestTypes = this.getRequestTypes("en");

        if (application.getServiceList() != null) {
            for (Service ser : application.getServiceList()) {
                Money baseFee = new Money(BigDecimal.ZERO);
                Money serviceFee = new Money(BigDecimal.ZERO);
                Money stampDuty = new Money(BigDecimal.ZERO);
                if (requestTypes != null) {
                    for (RequestType type : requestTypes) {
                        if (ser.getRequestTypeCode().equals(type.getCode())) {
                            if (type.getBaseFee() != null) {
                                baseFee = new Money(type.getBaseFee().abs());
                            }

                            break;
                        }
                    }
                }
                ser.setBaseFee(baseFee.getAmount());
                ser.setServiceFee(serviceFee.getAmount());
                
                servicesFeeTotal = servicesFeeTotal.plus(serviceFee);
                registrationFeeTotal = registrationFeeTotal.plus(baseFee);
                stampDutyTotal = stampDutyTotal.plus(stampDuty);
            }
        }

        // Calculate the total fee for the application.

        totalFee = servicesFeeTotal.plus(registrationFeeTotal).plus(stampDutyTotal).plus(groundRent);

        application.setServicesFee(servicesFeeTotal.getAmount());
                
        application.setTotalFee(totalFee.getAmount());

        if (application.getTotalAmountPaid() == null) {
            application.setTotalAmountPaid(BigDecimal.ZERO);
        }
    }

    /**
     * Saves changes to the application and child objects. Will also update the
     * completion dates and fees for the application if a new service as been
     * added. <p>Requires the {@linkplain RolesConstants#APPLICATION_CREATE_APPS}
     * role.</p>
     *
     * @param application
     * @return The application after the save is completed.
     */
    @Override
    @RolesAllowed({RolesConstants.APPLICATION_CREATE_APPS, RolesConstants.APPLICATION_EDIT_APPS})
    public Application saveApplication(Application application) {
        if (application == null) {
            return application;
        }
        if (application.isNew() && !isInRole(RolesConstants.APPLICATION_CREATE_APPS)) {
            throw new SOLAAccessException();
        }
        Date now = DateUtility.now();
        if (application.getServiceList() != null) {
            List<RequestType> requestTypes = this.getRequestTypes("en");
            for (Service ser : application.getServiceList()) {
                if (ser.isNew()) {
                    //ser.setStatusCode(ServiceStatusType.STATUS_LODGED);
                    //ser.setActionCode(ServiceActionType.LODGED);
                    ser.setLodgingDatetime(now);
                    ser.setExpectedCompletionDate(
                            calculateServiceCompletionDate(requestTypes, ser, now));
                }
            }
        }

        calculateLodgementFees(application);

        treatApplicationSources(application);
        application = getRepository().saveEntity(application);

        return application;
    }

    /**
     * Retrieves the data required for the lodgement view report. <p>Requires
     * the {@linkplain RolesConstants#REPORTS_VIEW} role.</p>
     *
     * @param params The date parameters for the report.
     * @return THe data for the Lodgement View report
     */
    @Override
//    @RolesAllowed(RolesConstants.REPORTS_VIEW)
    public List<LodgementView> getLodgementView(LodgementViewParams params) {

        List<LodgementView> result;
        Map queryParams = new HashMap<String, Object>();
        queryParams.put(CommonSqlProvider.PARAM_QUERY, LodgementView.QUERY_GETLODGEMENT);

        queryParams.put(LodgementView.PARAMETER_FROM,
                params.getFromDate() == null ? new GregorianCalendar(1, 1, 1).getTime() : params.getFromDate());
        queryParams.put(LodgementView.PARAMETER_TO,
                params.getToDate() == null ? new GregorianCalendar(2500, 1, 1).getTime() : params.getToDate());

        result = getRepository().executeFunction(queryParams, LodgementView.class);
        return result;
    }

    /**
     * Retrieves the data required for the lodgement timing report. <p>Requires
     * the {@linkplain RolesConstants#REPORTS_VIEW} role.</p>
     *
     * @param params The date parameters for the report.
     * @return The data for the Lodgement Timing report
     */
    @Override
//    @RolesAllowed(RolesConstants.REPORTS_VIEW)
    public List<LodgementTiming> getLodgementTiming(LodgementViewParams params) {

        List<LodgementTiming> result = null;
        Map queryParams = new HashMap<String, Object>();
        queryParams.put(CommonSqlProvider.PARAM_QUERY, LodgementTiming.QUERY_GETLODGEMENT);

        queryParams.put(LodgementTiming.PARAMETER_FROM,
                params.getFromDate() == null ? new GregorianCalendar(1, 1, 1).getTime() : params.getFromDate());
        queryParams.put(LodgementView.PARAMETER_TO,
                params.getToDate() == null ? new GregorianCalendar(2500, 1, 1).getTime() : params.getToDate());

        result = getRepository().executeFunction(queryParams, LodgementTiming.class);
        return result;
    }

    /**
     * Retrieves the application log for the specified application id. The log
     * captures details to track when specific actions are performed against the
     * application. <p>Requires the {@linkplain RolesConstants#REPORTS_VIEW}
     * role.</p>
     *
     * @param applicationId The identifier of the application to retrieve the
     * log for
     * @return The list of log entries for the application.
     */
    @Override
    @RolesAllowed(RolesConstants.REPORTS_VIEW)
    public List<ApplicationLog> getApplicationLog(String applicationId) {
        List<ApplicationLog> result = null;
        if (applicationId == null) {
            return result;
        }
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, ApplicationLog.QUERY_WHERE_BYAPPLICATIONID);
        params.put(ApplicationLog.QUERY_PARAMETER_APPLICATIONID, applicationId);
        result = getRepository().getEntityList(ApplicationLog.class, params);

        return result;
    }

    /**
     * Retrieves the actions a specific user has performed against any
     * application during a specific period.
     *
     * @param username The username of the user to query the application log
     * with
     * @param fromTime The start of the reporting period
     * @param toTime The end of the reporting period
     * @return The list of actions the user has performed against any
     * application during the reporting period.
     */
    @Override
    public List<ApplicationLog> getUserActions(String username, Date fromTime, Date toTime) {
        List<ApplicationLog> result = null;
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, ApplicationLog.QUERY_WHERE_BYTIMESPAN);
        params.put(ApplicationLog.QUERY_PARAMETER_USERNAME, username);
        params.put(ApplicationLog.QUERY_PARAMETER_FROM, fromTime);
        params.put(ApplicationLog.QUERY_PARAMETER_TO, toTime);
        result = getRepository().getEntityList(ApplicationLog.class, params);
        return result;
    }

    /**
     * Retrieves all application.request_type code values.
     *
     * @param languageCode The language code to use for localization of display
     * values.
     */
    @Override
    public List<RequestType> getRequestTypes(String languageCode) {
        return getRepository().getCodeList(RequestType.class, languageCode);
    }

    /**
     * Retrieves all application.application_status_type code values.
     *
     * @param languageCode The language code to use for localization of display
     * values.
     */
    @Override
    public List<ApplicationStatusType> getApplicationStatusTypes(String languageCode) {
        return getRepository().getCodeList(ApplicationStatusType.class, languageCode);
    }

    /**
     * Retrieves all application.application_stage_type code values.
     * 
     * @param languageCode The language code to use for localization of display
     * values.
     */
    @Override
    public List<ApplicationStageType> getApplicationStageTypes(String languageCode) {
        return getRepository().getCodeList(ApplicationStageType.class, languageCode);
    }
    
    /**
     * Retrieves all application.application_action_type code values.
     *
     * @param languageCode The language code to use for localization of display
     * values.
     */
    @Override
    public List<ApplicationActionType> getApplicationActionTypes(String languageCode) {
        return getRepository().getCodeList(ApplicationActionType.class, languageCode);
    }

    /**
     * Retrieves all application.type_action code values.
     *
     * @param languageCode The language code to use for localization of display
     * values.
     */
    @Override
    public List<TypeAction> getTypeActions(String languageCode) {
        return getRepository().getCodeList(TypeAction.class, languageCode);
    }

    /**
     * Retrieves all application.service_status_type code values.
     *
     * @param languageCode The language code to use for localization of display
     * values.
     */
    @Override
    public List<ServiceStatusType> getServiceStatusTypes(String languageCode) {
        return getRepository().getCodeList(ServiceStatusType.class, languageCode);
    }

    /**
     * Retrieves all application.service_action_type code values.
     *
     * @param languageCode The language code to use for localization of display
     * values.
     */
    @Override
    public List<ServiceActionType> getServiceActionTypes(String languageCode) {
        return getRepository().getCodeList(ServiceActionType.class, languageCode);
    }

    /**
     * Updates the status of the service to the value indicated by the
     * <code>status_to_set</code> in the
     * <code>application.service_action_type</code> table for the
     * <code>complete</code> code. (i.e. completed) <p>Requires the {@linkplain RolesConstants#APPLICATION_SERVICE_COMPLETE}
     * role.</p>
     *
     * @param serviceId The service to perform the action against
     * @param languageCode The language code to use for localization of
     * validation messages.
     * @param rowVersion The current row version of the service
     * @return The results of the validation performed as part of the service
     * action.
     * @see #takeActionAgainstService(java.lang.String, java.lang.String,
     * java.lang.String, int) takeActionAgainstService
     */
    @Override
    @RolesAllowed(RolesConstants.APPLICATION_SERVICE_COMPLETE)
    public List<ValidationResult> serviceActionComplete(
            String serviceId, String languageCode, int rowVersion) {
        return this.takeActionAgainstService(
                serviceId, ServiceActionType.COMPLETE, languageCode, rowVersion);
    }

    /**
     * Updates the status of the service to the value indicated by the
     * <code>status_to_set</code> in the
     * <code>application.service_action_type</code> table for the
     * <code>revert</code> code. (i.e. pending) <p>Requires the {@linkplain RolesConstants#APPLICATION_SERVICE_REVERT}
     * role.</p>
     *
     * @param serviceId The service to perform the action against
     * @param languageCode The language code to use for localization of
     * validation messages.
     * @param rowVersion The current row version of the service
     * @return The results of the validation performed as part of the service
     * action.
     * @see #takeActionAgainstService(java.lang.String, java.lang.String,
     * java.lang.String, int) takeActionAgainstService
     */
    @Override
    @RolesAllowed(RolesConstants.APPLICATION_SERVICE_REVERT)
    public List<ValidationResult> serviceActionRevert(
            String serviceId, String languageCode, int rowVersion) {
        return this.takeActionAgainstService(
                serviceId, ServiceActionType.REVERT, languageCode, rowVersion);
    }

    /**
     * Updates the status of the service to the value indicated by the
     * <code>status_to_set</code> in the
     * <code>application.service_action_type</code> table for the
     * <code>start</code> code. (i.e. pending) <p>Requires the {@linkplain RolesConstants#APPLICATION_SERVICE_START}
     * role.</p>
     *
     * @param serviceId The service to perform the action against
     * @param languageCode The language code to use for localization of
     * validation messages.
     * @param rowVersion The current row version of the service
     * @return The results of the validation performed as part of the service
     * action.
     * @see #takeActionAgainstService(java.lang.String, java.lang.String,
     * java.lang.String, int) takeActionAgainstService
     */
    @Override
    @RolesAllowed(RolesConstants.APPLICATION_SERVICE_START)
    public List<ValidationResult> serviceActionStart(String serviceId, String languageCode, int rowVersion) {

        RoleVerifier validRole = getRoleVerifier(serviceId);

        if (!validRole.isRoleCheck()) {
            throw new SOLAException(ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS);
        }
        return this.takeActionAgainstService(serviceId, ServiceActionType.START, languageCode, rowVersion);
    }

    /**
     * Updates the status of the service to the value indicated by the
     * <code>status_to_set</code> in the
     * <code>application.service_action_type</code> table for the
     * <code>cancel</code> code. (i.e. cancelled) <p>Requires the {@linkplain RolesConstants#APPLICATION_SERVICE_CANCEL}
     * role.</p>
     *
     * @param serviceId The service to perform the action against
     * @param languageCode The language code to use for localization of
     * validation messages.
     * @param rowVersion The current row version of the service
     * @return The results of the validation performed as part of the service
     * action.
     * @see #takeActionAgainstService(java.lang.String, java.lang.String,
     * java.lang.String, int) takeActionAgainstService
     */
    @Override
    @RolesAllowed(RolesConstants.APPLICATION_SERVICE_CANCEL)
    public List<ValidationResult> serviceActionCancel(
            String serviceId, String languageCode, int rowVersion) {
        return this.takeActionAgainstService(
                serviceId, ServiceActionType.CANCEL, languageCode, rowVersion);
    }

    /**
     * Updates the status of the application to the value indicated by the
     * <code>status_to_set</code> in the
     * <code>application.application_action_type</code> table for the
     * <code>withdraw</code> code. (i.e. anulled). Will also delete the
     * transaction records for each service that is associated with the
     * application.<p>Requires the {@linkplain RolesConstants#APPLICATION_WITHDRAW}
     * role.</p>
     *
     * @param applicationId The application to perform the action against
     * @param languageCode The language code to use for localization of
     * validation messages.
     * @param rowVersion The current row version of the service
     * @return The results of the validation performed as part of the service
     * action.
     * @see #takeActionAgainstApplication(java.lang.String, java.lang.String,
     * java.lang.String, int) takeActionAgainstApplication
     */
    @Override
    @RolesAllowed(RolesConstants.APPLICATION_WITHDRAW)
    public List<ValidationResult> applicationActionWithdraw(
            String applicationId, String languageCode, int rowVersion) {
        return this.takeActionAgainstApplication(
                applicationId, ApplicationActionType.WITHDRAW, languageCode, rowVersion);
    }

    /**
     * Updates the status of the application to the value indicated by the
     * <code>status_to_set</code> in the
     * <code>application.application_action_type</code> table for the
     * <code>cancel</code> code. (i.e. anulled). Will also delete the
     * transaction records for each service that is associated with the
     * application.<p>Requires the {@linkplain RolesConstants#APPLICATION_REJECT}
     * role.</p>
     *
     * @param applicationId The application to perform the action against
     * @param languageCode The language code to use for localization of
     * validation messages.
     * @param rowVersion The current row version of the service
     * @return The results of the validation performed as part of the service
     * action.
     * @see #takeActionAgainstApplication(java.lang.String, java.lang.String,
     * java.lang.String, int) takeActionAgainstApplication
     */
    @Override
    @RolesAllowed(RolesConstants.APPLICATION_REJECT)
    public List<ValidationResult> applicationActionCancel(
            String applicationId, String languageCode, int rowVersion) {
        return this.takeActionAgainstApplication(
                applicationId, ApplicationActionType.CANCEL, languageCode, rowVersion);
    }

    /**
     * Updates the status of the application to the value indicated by the
     * <code>status_to_set</code> in the
     * <code>application.application_action_type</code> table for the
     * <code>requisition</code> code. (i.e. requisition). <p>Requires the {@linkplain RolesConstants#APPLICATION_REQUISITE}
     * role.</p>
     *
     * @param applicationId The application to perform the action against
     * @param languageCode The language code to use for localization of
     * validation messages.
     * @param rowVersion The current row version of the service
     * @return The results of the validation performed as part of the service
     * action.
     * @see #takeActionAgainstApplication(java.lang.String, java.lang.String,
     * java.lang.String, int) takeActionAgainstApplication
     */
    @Override
    @RolesAllowed(RolesConstants.APPLICATION_REQUISITE)
    public List<ValidationResult> applicationActionRequisition(
            String applicationId, String languageCode, int rowVersion) {
        return this.takeActionAgainstApplication(
                applicationId, ApplicationActionType.REQUISITION, languageCode, rowVersion);
    }

    /**
     * Triggers the validations for the application and updates the Application
     * Action code to indicate if the validation succeed or fail. <p>Requires
     * the {@linkplain RolesConstants#APPLICATION_VALIDATE} role.</p>
     *
     * @param applicationId The application to perform the action against
     * @param languageCode The language code to use for localization of
     * validation messages.
     * @param rowVersion The current row version of the service
     * @return The results of the validation performed as part of the service
     * action.
     * @see #takeActionAgainstApplication(java.lang.String, java.lang.String,
     * java.lang.String, int) takeActionAgainstApplication
     */
    @Override
    @RolesAllowed(RolesConstants.APPLICATION_VALIDATE)
    public List<ValidationResult> applicationActionValidate(
            String applicationId, String languageCode, int rowVersion) {
        return this.takeActionAgainstApplication(
                applicationId, ApplicationActionType.VALIDATE, languageCode, rowVersion);
    }

    /**
     * Updates the status of the application to the value indicated by the
     * <code>status_to_set</code> in the
     * <code>application.application_action_type</code> table for the
     * <code>approve</code> code. (i.e. approved) if validations are successful.
     * Also updates the status of all services and BA Units and /or Cadastre
     * Objects linked to those services. <p>Requires the {@linkplain RolesConstants#APPLICATION_APPROVE}
     * role.</p>
     *
     * @param applicationId The application to perform the action against
     * @param languageCode The language code to use for localization of
     * validation messages.
     * @param rowVersion The current row version of the service
     * @return The results of the validation performed as part of the service
     * action.
     * @see #takeActionAgainstApplication(java.lang.String, java.lang.String,
     * java.lang.String, int) takeActionAgainstApplication
     */
    @Override
    @RolesAllowed(RolesConstants.APPLICATION_APPROVE)
    public List<ValidationResult> applicationActionApprove(
            String applicationId, String languageCode, int rowVersion) {
        return this.takeActionAgainstApplication(
                applicationId, ApplicationActionType.APPROVE, languageCode, rowVersion);
    }

    /**
     * Updates the status of the application to the value indicated by the
     * <code>status_to_set</code> in the
     * <code>application.application_action_type</code> table for the
     * <code>archive</code> code. (i.e. completed). <p>Requires the {@linkplain RolesConstants#APPLICATION_ARCHIVE}
     * role.</p>
     *
     * @param applicationId The application to perform the action against
     * @param languageCode The language code to use for localization of
     * validation messages.
     * @param rowVersion The current row version of the service
     * @return The results of the validation performed as part of the service
     * action.
     * @see #takeActionAgainstApplication(java.lang.String, java.lang.String,
     * java.lang.String, int) takeActionAgainstApplication
     */
    @Override
    @RolesAllowed(RolesConstants.APPLICATION_ARCHIVE)
    public List<ValidationResult> applicationActionArchive(
            String applicationId, String languageCode, int rowVersion) {
        return this.takeActionAgainstApplication(
                applicationId, ApplicationActionType.ARCHIVE, languageCode, rowVersion);
    }

    /**
     * Sets the action code on the application to <cpde>dispatch</code> to
     * indicate the application has been dispatched. <p>Requires the {@linkplain RolesConstants#APPLICATION_DISPATCH}
     * role.</p>
     *
     * @param applicationId The application to perform the action against
     * @param languageCode The language code to use for localization of
     * validation messages.
     * @param rowVersion The current row version of the service
     * @return The results of the validation performed as part of the service
     * action.
     * @see #takeActionAgainstApplication(java.lang.String, java.lang.String,
     * java.lang.String, int) takeActionAgainstApplication
     */
    @Override
    @RolesAllowed(RolesConstants.APPLICATION_DISPATCH)
    public List<ValidationResult> applicationActionDespatch(
            String applicationId, String languageCode, int rowVersion) {
        return this.takeActionAgainstApplication(
                applicationId, ApplicationActionType.DISPATCH, languageCode, rowVersion);
    }

    /**
     * Updates the status of the application to the value indicated by the
     * <code>status_to_set</code> in the
     * <code>application.application_action_type</code> table for the
     * <code>lapse</code> code. (i.e. anulled). Will also delete the transaction
     * records for each service that is associated with the
     * application.<p>Requires the {@linkplain RolesConstants#APPLICATION_LAPSE}
     * role.</p>
     *
     * @param applicationId The application to perform the action against
     * @param languageCode The language code to use for localization of
     * validation messages.
     * @param rowVersion The current row version of the service
     * @return The results of the validation performed as part of the service
     * action.
     * @see #takeActionAgainstApplication(java.lang.String, java.lang.String,
     * java.lang.String, int) takeActionAgainstApplication
     */
    @Override
    @RolesAllowed(RolesConstants.APPLICATION_LAPSE)
    public List<ValidationResult> applicationActionLapse(
            String applicationId, String languageCode, int rowVersion) {
        return this.takeActionAgainstApplication(
                applicationId, ApplicationActionType.LAPSE, languageCode, rowVersion);
    }

    /**
     * Sets the assignee id on the application to the id of the user specified
     * as well as setting the action code on the application to <cpde>assign</code>
     * to indicate the application has been assigned. <p>Requires the {@linkplain RolesConstants#APPLICATION_ASSIGN_TO_OTHERS}
     * or the {@linkplain RolesConstants#APPLICATION_ASSIGN_TO_YOURSELF}
     * role.</p>
     *
     * @param applicationId The application to perform the action against
     * @param userId Identifier of the user to assign to the application
     * @param languageCode The language code to use for localization of
     * validation messages.
     * @param rowVersion The current row version of the service
     * @return The results of the validation performed as part of the service
     * action.
     * @see
     * #takeActionAgainstApplication(org.sola.services.ejb.application.repository.entities.ApplicationActionTaker,
     * java.lang.String, java.lang.String, int) takeActionAgainstApplication
     */
    @Override
    @RolesAllowed({RolesConstants.APPLICATION_ASSIGN_TO_OTHERS, RolesConstants.APPLICATION_ASSIGN_TO_YOURSELF})
    public List<ValidationResult> applicationActionAssign(
            String applicationId, String userId, String languageCode, int rowVersion) {
        ApplicationActionTaker application =
                getRepository().getEntity(ApplicationActionTaker.class, applicationId);
        if (application == null) {
            throw new SOLAException(ServiceMessage.EJB_APPLICATION_APPLICATION_NOT_FOUND);
        }
        application.setAssigneeId(userId);
        application.setAssignedDatetime(Calendar.getInstance().getTime());
        return this.takeActionAgainstApplication(
                application, ApplicationActionType.ASSIGN, languageCode, rowVersion);
    }

    /**
     * Clears the assignee id on the application and sets the action code on the
     * application to <cpde>unAssign</code> to indicate the application has been
     * unassigned. <p>Requires the {@linkplain RolesConstants#APPLICATION_UNASSIGN_FROM_OTHERS}
     * or the {@linkplain RolesConstants#APPLICATION_UNASSIGN_FROM_YOURSELF}
     * role.</p>
     *
     * @param applicationId The application to perform the action against
     * @param languageCode The language code to use for localization of
     * validation messages.
     * @param rowVersion The current row version of the service
     * @return The results of the validation performed as part of the service
     * action.
     * @see
     * #takeActionAgainstApplication(org.sola.services.ejb.application.repository.entities.ApplicationActionTaker,
     * java.lang.String, java.lang.String, int) takeActionAgainstApplication
     */
    @Override
    @RolesAllowed({RolesConstants.APPLICATION_UNASSIGN_FROM_OTHERS, RolesConstants.APPLICATION_UNASSIGN_FROM_YOURSELF})
    public List<ValidationResult> applicationActionUnassign(
            String applicationId, String languageCode, int rowVersion) {
        ApplicationActionTaker application =
                getRepository().getEntity(ApplicationActionTaker.class, applicationId);
        if (application == null) {
            throw new SOLAException(ServiceMessage.EJB_APPLICATION_APPLICATION_NOT_FOUND);
        }
        application.setAssigneeId(null);
        application.setAssignedDatetime(null);
        return this.takeActionAgainstApplication(
                application, ApplicationActionType.UNASSIGN, languageCode, rowVersion);
    }

    /**
     * Updates the status of the application to the value indicated by the
     * <code>status_to_set</code> in the
     * <code>application.application_action_type</code> table for the
     * <code>resubmit</code> code. (i.e. lodged). <p>Requires the {@linkplain RolesConstants#APPLICATION_RESUBMIT}
     * role.</p>
     *
     * @param applicationId The application to perform the action against
     * @param languageCode The language code to use for localization of
     * validation messages.
     * @param rowVersion The current row version of the service
     * @return The results of the validation performed as part of the service
     * action.
     * @see #takeActionAgainstApplication(java.lang.String, java.lang.String,
     * java.lang.String, int) takeActionAgainstApplication
     */
    @Override
    @RolesAllowed(RolesConstants.APPLICATION_RESUBMIT)
    public List<ValidationResult> applicationActionResubmit(
            String applicationId, String languageCode, int rowVersion) {
        return this.takeActionAgainstApplication(
                applicationId, ApplicationActionType.RESUBMIT, languageCode, rowVersion);
    }

    /**
     * It registers a service of category type informationServices. If it is of
     * another kind of not specified it throws an exception. If the service
     * exists, it is only logged an action of type completed, otherwise it is
     * created.
     *
     * @param service The service to be saved
     * @param languageCode current language code. Used if business rules are
     * invoked.
     * @return The service after the save is completed
     */
    @Override
    @RolesAllowed({RolesConstants.APPLICATION_PRINT_STATUS_REPORT,
        RolesConstants.ADMINISTRATIVE_BA_UNIT_PRINT_CERT,
        RolesConstants.GIS_PRINT, RolesConstants.SOURCE_PRINT})
    public Service saveInformationService(Service service, String languageCode) {
        RequestType requestType = this.getCodeEntity(
                RequestType.class, service.getRequestTypeCode());
        if (requestType == null || !requestType.getRequestCategoryCode().equals(
                RequestCategoryType.INFORMATION_SERVICES)) {
            throw new SOLAException(
                    ServiceMessage.EJB_APPLICATION_SERVICE_REQUEST_TYPE_INFORMATION_REQUIRED);
        }
        Service existingService = this.getRepository().getEntity(Service.class, service.getId());
        if (existingService == null) {
            service.setLodgingDatetime(DateUtility.now());
            service.setExpectedCompletionDate(DateUtility.now());
            existingService = this.saveEntity(service);
        }
        this.serviceActionComplete(
                existingService.getId(), languageCode, existingService.getRowVersion());
        return existingService;
    }

    /**
     * Retrieves a cut down version of the services on the application for the
     * purpose of applying actions to the services.
     *
     * @param applicationId The identifier of the application to retrieve the
     * services for.
     * @return The list of cut down services.
     */
    private List<ServiceActionTaker> getServiceActionTakerList(String applicationId) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, ServiceActionTaker.QUERY_WHERE_BYAPPLICATIONID);
        params.put(ServiceBasic.QUERY_PARAMETER_APPLICATIONID, applicationId);
        return getRepository().getEntityList(ServiceActionTaker.class, params);
    }

    /**
     * It validates a service. For the moment, it is called from the validate
     * method. Perhaps in the future can be used directly to validate a single
     * service.
     *
     * @param service the service
     * @param languageCode the language code to translate the feedback
     * @return
     * @see #approveApplicationService(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, boolean) approveApplicationService
     */
    private List<ValidationResult> validateService(
            ServiceActionTaker service, String languageCode, ServiceActionType serviceActionType) {
        List<ValidationResult> resultList = new ArrayList<ValidationResult>();

        // Skip validation for cancelled service
        if (service.getStatusCode() != null && service.getStatusCode().equalsIgnoreCase(ServiceStatusType.STATUS_CANCELLED)) {
            return resultList;
        }

        List<BrValidation> brValidationList = this.systemEJB.getBrForValidatingService(
                serviceActionType.getCode(), service.getRequestTypeCode());

        HashMap<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("id", service.getId());
        params.put("username", getUserName());
        //Run the validation
        resultList = this.systemEJB.checkRulesGetValidation(
                brValidationList, languageCode, params);
        if (serviceActionType.getStatusToSet().equals(ServiceStatusType.STATUS_COMPLETED)) {
            resultList.addAll(this.approveApplicationService(
                    service.getId(), service.getStatusCode(),
                    service.getRequestTypeCode(), languageCode, true));
        }

        return resultList;
    }

    /**
     * Updates the service with the appropriate action code and status
     *
     * @param serviceId The identifier of the service to action
     * @param actionCode The action code to indicate the status change required
     * to the service
     * @param languageCode The language code to use for localizing any
     * validation messages
     * @param rowVersion The current rowversion for the service
     * @return The list of validation messages as a result of applying the
     * action to the service.
     */
    private List<ValidationResult> takeActionAgainstService(
            String serviceId, String actionCode, String languageCode, int rowVersion) {
        ServiceActionTaker service = getRepository().getEntity(ServiceActionTaker.class, serviceId);
        if (service == null) {
            throw new SOLAException(ServiceMessage.EJB_APPLICATION_SERVICE_NOT_FOUND);
        }

        ServiceActionType serviceActionType = getRepository().getCode(ServiceActionType.class, actionCode, languageCode);
        List<ValidationResult> validationResultList = this.validateService(service, languageCode, serviceActionType);

        if (systemEJB.validationSucceeded(validationResultList)) {
            transactionEJB.changeTransactionStatusFromService(serviceId, serviceActionType.getStatusToSet());
            service.setStatusCode(serviceActionType.getStatusToSet());
            service.setActionCode(actionCode);
            service.setRowVersion(rowVersion);
            getRepository().saveEntity(service);
        } else {
            throw new SOLAValidationException(validationResultList);
        }
        return validationResultList;
    }

    /**
     * Wrapper method that uses the applicationId to load the application object
     * before calling
     * {@linkplain #takeActionAgainstApplication(org.sola.services.ejb.application.repository.entities.ApplicationActionTaker,
     * java.lang.String, java.lang.String, int) takeActionAgainstApplication.
     *
     * @param applicationId The identifier of the application to perform the action against
     * @param actionCode The action to apply to the application
     * @param languageCode The language code to use for localization of
     * validation messages.
     * @param rowVersion The current row version of the service
     * @return The results of the validation performed as part of the service
     * action.
     */
    private List<ValidationResult> takeActionAgainstApplication(
            String applicationId, String actionCode, String languageCode, int rowVersion) {
        ApplicationActionTaker application =
                getRepository().getEntity(ApplicationActionTaker.class, applicationId);
        if (application == null) {
            throw new SOLAException(ServiceMessage.EJB_APPLICATION_APPLICATION_NOT_FOUND);
        }
        return this.takeActionAgainstApplication(application, actionCode, languageCode, rowVersion);
    }

    /**
     * Executes the specified action against the application. Typically this
     * will require changing the status of the application based on the
     * <code>status_to_set</code> value in the
     * <code>application.application_action_type</code> table as well as setting
     * the application action, but it can also involve updating the services and
     * related transactions depending on the scope of the action (e.g. Approve
     * will update the status of all services and related transactions).
     * <p>Before applying an action, the validation rules for that action are
     * executed against the application as well as the services and related
     * transactions. If the validation rules fail, the action is aborted and no
     * changes are applied.</p>
     *
     * @param application The application to perform the action against
     * @param actionCode The action to apply to the application
     * @param languageCode The language code to use for localization of
     * validation messages.
     * @param rowVersion The current row version of the service
     * @return The results of the validation performed as part of the service
     * action.
     * @see #approveApplicationService(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, boolean) approveApplicationService
     */
    private List<ValidationResult> takeActionAgainstApplication(
            ApplicationActionTaker application, String actionCode,
            String languageCode, int rowVersion) {

        List<BrValidation> brValidationList =
                this.systemEJB.getBrForValidatingApplication(actionCode);

        HashMap<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("id", application.getId());
        //Run the validation
        List<ValidationResult> resultList = this.systemEJB.checkRulesGetValidation(
                brValidationList, languageCode, params);

        boolean validationSucceeded = systemEJB.validationSucceeded(resultList);

        ApplicationActionType applicationActionType =
                getRepository().getCode(ApplicationActionType.class, actionCode, languageCode);

        // applicationActionType is null if the action is Validate. 
        String statusToSet = applicationActionType == null ? null
                : applicationActionType.getStatusToSet();

        //For each specific action type, can be done extra validations
        if (ApplicationActionType.VALIDATE.equals(actionCode)) {
            //If action lodge or validate
            List<ServiceActionTaker> serviceList =
                    this.getServiceActionTakerList(application.getId());
            ServiceActionType serviceActionType = getRepository().getCode(
                    ServiceActionType.class, ServiceActionType.COMPLETE, languageCode);
            for (ServiceActionTaker service : serviceList) {
                List<ValidationResult> serviceValidation =
                        this.validateService(service, languageCode, serviceActionType);
                validationSucceeded = validationSucceeded
                        && systemEJB.validationSucceeded(serviceValidation);
                resultList.addAll(serviceValidation);
            }
        } else if (ApplicationActionType.APPROVE.equals(actionCode)) {
            brValidationList = this.systemEJB.getBrForValidatingApplication(
                    ApplicationActionType.VALIDATE);
            List<ValidationResult> resultValidationForAppList =
                    this.systemEJB.checkRulesGetValidation(brValidationList, languageCode, params);
            validationSucceeded = validationSucceeded
                    && systemEJB.validationSucceeded(resultValidationForAppList);
            resultList.addAll(resultValidationForAppList);
            List<ServiceActionTaker> serviceList =
                    this.getServiceActionTakerList(application.getId());
            for (ServiceActionTaker service : serviceList) {
                List<ValidationResult> serviceValidation =
                        this.approveApplicationService(service.getId(), service.getStatusCode(),
                        service.getRequestTypeCode(), languageCode, !validationSucceeded);
                validationSucceeded = validationSucceeded
                        && systemEJB.validationSucceeded(serviceValidation);
                resultList.addAll(serviceValidation);
            }
        } else if (ApplicationStatusType.ANNULLED.equals(statusToSet)) {
            List<ServiceActionTaker> serviceList =
                    this.getServiceActionTakerList(application.getId());
            for (ServiceActionTaker service : serviceList) {
                transactionEJB.rejectTransaction(service.getId());
            }
        }

        if (validationSucceeded || ApplicationActionType.VALIDATE.equals(actionCode)) {
            if (statusToSet != null) {
                application.setStatusCode(statusToSet);
            }
            if (ApplicationActionType.VALIDATE.equals(actionCode)) {
                //Only record the outcome of the validation if the action is to validate the application. 
                actionCode = validationSucceeded
                        ? ApplicationActionType.VALIDATE_PASSED : ApplicationActionType.VALIDATE_FAILED;
            }
            application.setActionCode(actionCode);
            application.setRowVersion(rowVersion);
            getRepository().saveEntity(application);
        } else {
            throw new SOLAValidationException(resultList);
        }
        return resultList;
    }

    /**
     * Approves the service as well as the entities (e.g. BA Units, Sources,
     * Cadastre Objects, etc) tied to the service through a transaction. Can
     * also be used to cancel a service and reject (i.e. Delete) the transaction
     * associated with the service or simply validate the service in preparation
     * for approval.
     *
     * @param serviceId Identifier of the service to approve
     * @param serviceStatusCode The status code to update the service with.
     * Either Approve or Cancel
     * @param serviceRequestTypeCode The request type code of the service
     * @param languageCode The language code to use for localization of
     * validation messages
     * @param validationOnly Flag to indicate that the services should only be
     * validated and not updated.
     * @return The list of validation messages.
     * @see
     * org.sola.services.ejb.administrative.businesslogic.AdministrativeEJB#approveTransaction(java.lang.String,
     * java.lang.String, boolean, java.lang.String)
     * AdministrativeEJB.approveTransaction
     * @see
     * org.sola.services.ejb.source.businesslogic.SourceEJB#approveTransaction(java.lang.String,
     * java.lang.String, boolean, java.lang.String) SourceEJB.approveTransaction
     * @see
     * org.sola.services.ejb.transaction.businesslogic.TransactionEJB#approveTransaction(java.lang.String,
     * java.lang.String, java.lang.String, boolean)
     * TransactionEJB.approveTransaction
     * @see
     * org.sola.services.ejb.transaction.businesslogic.TransactionEJB#rejectTransaction(java.lang.String)
     * TransactionEJB.rejectTransaction
     */
    private List<ValidationResult> approveApplicationService(
            String serviceId, String serviceStatusCode, String serviceRequestTypeCode,
            String languageCode, boolean validationOnly) {
        List<ValidationResult> validationResultList = new ArrayList<ValidationResult>();

        if (!validationOnly && serviceStatusCode.equals(ServiceStatusType.STATUS_CANCELLED)) {
            transactionEJB.rejectTransaction(serviceId);
        } else if (serviceStatusCode == null || (serviceStatusCode != null && !serviceStatusCode.equalsIgnoreCase(ServiceStatusType.STATUS_CANCELLED))) {
            // Skip validation for cancelled service
            TransactionBasic transaction = transactionEJB.getTransactionByServiceId(serviceId, false, TransactionBasic.class);
            if (transaction != null) {
                String statusOnApproval = RegistrationStatusType.STATUS_CURRENT;
                String actionOnRequestType = getRepository().getCode(RequestType.class,
                        serviceRequestTypeCode, null).getTypeActionCode();

                if (actionOnRequestType != null
                        && actionOnRequestType.equals(TypeAction.CANCEL)) {
                    statusOnApproval = RegistrationStatusType.STATUS_HISTORIC;
                }

                List<ValidationResult> approvalResult = null;

                approvalResult = administrativeEJB.approveTransaction(
                        transaction.getId(), statusOnApproval, validationOnly, languageCode);
                validationResultList.addAll(approvalResult);
                validationOnly = validationOnly || !systemEJB.validationSucceeded(approvalResult);

                approvalResult = sourceEJB.approveTransaction(
                        transaction.getId(), statusOnApproval, validationOnly, languageCode);
                validationResultList.addAll(approvalResult);
                validationOnly = validationOnly || !systemEJB.validationSucceeded(approvalResult);

                approvalResult = transactionEJB.approveTransaction(
                        serviceRequestTypeCode, serviceId, languageCode, validationOnly);
                validationResultList.addAll(approvalResult);
            }
        }
        return validationResultList;
    }

    /**
     * Retrieves all application.request_category_type code values.
     *
     * @param languageCode The language code to use for localization of display
     * values.
     */
    @Override
    public List<RequestCategoryType> getRequestCategoryTypes(String languageCode) {
        return getRepository().getCodeList(RequestCategoryType.class, languageCode);
    }

    @Override
    public Application getApplicationByTransactionId(String transactionId) {
        Application application = null;
        TransactionBasic transaction = transactionEJB.getTransactionById(transactionId, TransactionBasic.class);
        if (transaction != null) {
            Service service = getRepository().getEntity(Service.class, transaction.getFromServiceId());
            if (service != null) {
                application = getRepository().getEntity(Application.class, service.getApplicationId());
            }
        }
        return application;
    }

    @Override
    public RoleVerifier getRoleVerifier(String serviceId) {
        if (serviceId == null) {
            serviceId = "";
        }
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, RoleVerifier.QUERY_VERIFY_SQL);
        params.put(RoleVerifier.QUERY_PARAM_SERVICE_ID, serviceId);
        params.put(RoleVerifier.QUERY_PARAM_USERNAME, getUserName());
        return getRepository().getEntity(RoleVerifier.class, params);
    }

    /**
     * Returns the list of ba_unit_id for approved systematic registration that
     * matches the specified search string.
     *
     * @param searchString The search string to use
     * @return The list of ba_unit_id for approved systematic registration
     */
    @Override
    @RolesAllowed(RolesConstants.ADMINISTRATIVE_SYSTEMATIC_REGISTRATION)
    public List<SysRegCertificates> getSysRegCertificatesByLocation(String searchString) {
        HashMap params = new HashMap();
        params.put("search_string", searchString);

        return getRepository().getEntityList(SysRegCertificates.class,
                SysRegCertificates.QUERY_WHERE_SEARCHBYPARTS, params);
    }

    /**
     * Returns the list of ba_unit_id for a specific approved systematic
     * registration that matches the specified search string.
     *
     * @param searchString The search string to use
     * @return The list of ba_unit_id for a specific approved systematic
     * registration
     */
    @Override
    @RolesAllowed(RolesConstants.ADMINISTRATIVE_SYSTEMATIC_REGISTRATION)
    public List<SysRegCertificates> getSysRegCertificatesByApplication(String searchString, String nr) {
        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, SysRegCertificates.QUERY_WHERE_BYNR);
        params.put(SysRegCertificates.QUERY_PARAMETER_NR, nr);
        params.put("search_string", searchString);


        return getRepository().getEntityList(SysRegCertificates.class,
                SysRegCertificates.QUERY_WHERE_BYNR, params);
    }

    @Override
    public List<ApplicationForm> getApplicationForms(String lang) {
        return getRepository().getCodeList(ApplicationForm.class, lang);
    }

    @Override
    public ApplicationFormWithBinary getApplicationFormWithBinary(String code, String lang) {
        return getRepository().getCode(ApplicationFormWithBinary.class, code, lang);
    }        
    /**
     * Retrieves statistics of mortgages completed, their number, their total amount of money and computed average amount.
     *
     * @param fromDate The start of the reporting period
     * @param toDate The end of the reporting period
     */
    @Override
    public List<MortgageStatsView> getMortgageStatsView(LodgementViewParams params) {
        Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put(CommonSqlProvider.PARAM_QUERY, MortgageStatsView.QUERY_GET_MORTGAGE_STATS);
        queryParams.put(MortgageStatsView.PARAMETER_FROM,
                params.getFromDate() == null ? new GregorianCalendar(1, 1, 1).getTime() : params.getFromDate());
        queryParams.put(MortgageStatsView.PARAMETER_TO, 
                params.getToDate() == null ? new GregorianCalendar(1, 1, 1).getTime() : params.getToDate());
        return getRepository().executeFunction(queryParams, MortgageStatsView.class);
    }
     
    @Override
    public List<ResponseView> getResponseView(LodgementViewParams params) {
        Map<String, Object> queryParams = new HashMap<String, Object>();
        
        if( params.getRequestCategoryCode() != null){
            queryParams.put(CommonSqlProvider.PARAM_QUERY, ResponseView.QUERY_GET_RESPONSE);
            queryParams.put(ResponseView.PARAMETER_FROM,
                    params.getFromDate() == null ? new GregorianCalendar(1, 1, 1).getTime() : params.getFromDate());
            queryParams.put(ResponseView.PARAMETER_TO, 
                    params.getToDate() == null ? new GregorianCalendar(1, 1, 1).getTime() : params.getToDate());
            queryParams.put(ResponseView.PARAMETER_CATEGORY_CODE, 
                    params.getRequestCategoryCode() == null ? new String() : params.getRequestCategoryCode());
            
        }
        else{
            queryParams.put(CommonSqlProvider.PARAM_QUERY, ResponseView.QUERY_GET_RESPONSE2);
            queryParams.put(ResponseView.PARAMETER_FROM,
                    params.getFromDate() == null ? new GregorianCalendar(1, 1, 1).getTime() : params.getFromDate());
            queryParams.put(ResponseView.PARAMETER_TO, 
                    params.getToDate() == null ? new GregorianCalendar(1, 1, 1).getTime() : params.getToDate());
        }
        return getRepository().executeFunction(queryParams, ResponseView.class);
    }

    /**
     * Retrieves a summary of the work performed during the specified reporting period.
     *
     * @param fromDate The start of the reporting period
     * @param toDate The end of the reporting period
     */
    @Override
    @RolesAllowed(RolesConstants.REPORTS_VIEW)
    public List<WorkSummary> getWorkSummary(LodgementViewParams params) {

        Map queryParams = new HashMap<String, Object>();
        
        if( params.getRequestCategoryCode() != null){
            queryParams.put(CommonSqlProvider.PARAM_FROM_PART, WorkSummary.QUERY_FROM_WORK_SUMMARY2);
            queryParams.put(WorkSummary.PARAMETER_FROM, params.getFromDate());
            queryParams.put(WorkSummary.PARAMETER_TO, params.getToDate()); 
            queryParams.put(WorkSummary.PARAMETER_CATEGORY_CODE, params.getRequestCategoryCode()); 
        }
        else{
            queryParams.put(CommonSqlProvider.PARAM_FROM_PART, WorkSummary.QUERY_FROM_WORK_SUMMARY);
            queryParams.put(WorkSummary.PARAMETER_FROM, params.getFromDate());
            queryParams.put(WorkSummary.PARAMETER_TO, params.getToDate());            
        }
      
        return getRepository().getEntityList(WorkSummary.class, queryParams);
    }
    
    @RolesAllowed(RolesConstants.REPORTS_VIEW)
    @Override
    public List<StatisticalView> getStatisticalView(LodgementViewParams params) {

        Map queryParams = new HashMap<String, Object>();
        
        if( params.getRequestCategoryCode() != null){
            queryParams.put(CommonSqlProvider.PARAM_FROM_PART, StatisticalView.QUERY_GETSTATISTICS2);
            queryParams.put(StatisticalView.PARAMETER_FROM, params.getFromDate());
            queryParams.put(StatisticalView.PARAMETER_TO, params.getToDate());
            queryParams.put(StatisticalView.PARAMETER_CATEGORY_CODE, params.getRequestCategoryCode());
        }else{
            queryParams.put(CommonSqlProvider.PARAM_FROM_PART, StatisticalView.QUERY_GETSTATISTICS);
            queryParams.put(StatisticalView.PARAMETER_FROM, params.getFromDate());
            queryParams.put(StatisticalView.PARAMETER_TO, params.getToDate());
        }
        
        return getRepository().getEntityList(StatisticalView.class, queryParams);
    }
 
    /**
     * Retrieves a summary of the transferred leases during the specified reporting period.
     *
     * @param fromDate The start of the reporting period
     * @param toDate The end of the reporting period
     */
    @Override
    public List<TransferLeaseView> getTransferLeaseView(LodgementViewParams params) {
        Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put(CommonSqlProvider.PARAM_QUERY, TransferLeaseView.QUERY_GET_TRANSFERRED_LEASES);
        queryParams.put(TransferLeaseView.PARAMETER_FROM,
                params.getFromDate() == null ? new GregorianCalendar(1, 1, 1).getTime() : params.getFromDate());
        queryParams.put(TransferLeaseView.PARAMETER_TO, 
                params.getToDate() == null ? new GregorianCalendar(1, 1, 1).getTime() : params.getToDate());
        return getRepository().executeFunction(queryParams, TransferLeaseView.class);
    }

}
