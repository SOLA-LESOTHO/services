package org.sola.services.ejb.administrative.repository.entities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.LocalInfo;
import org.sola.services.common.repository.ChildEntity;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.ExternalEJB;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;
import org.sola.services.ejb.cadastre.businesslogic.CadastreEJBLocal;
import org.sola.services.ejb.cadastre.repository.entities.CadastreObject;
import org.sola.services.ejb.party.businesslogic.PartyEJBLocal;
import org.sola.services.ejb.party.repository.entities.Party;

/**
 * Entity representing administrative.lease table.
 */
@Table(name = "lease", schema = "administrative")
public class Lease extends AbstractVersionedEntity {
    
    public static final String QUERY_PARAMETER_TRANSACTIONID = "transactionId";
    public static final String QUERY_WHERE_BY_TRANSACTION_ID = 
            "transaction_id = #{" + QUERY_PARAMETER_TRANSACTIONID + "}";
    
    @Id
    @Column
    private String id;
    
    @Column(name = "lease_number")
    private String leaseNumber;
    
    @Column(name = "marital_status")
    private String maritalStatus;
    
    @Column(name = "lessee_address")
    private String lesseeAddress;
    
    @ExternalEJB(ejbLocalClass = CadastreEJBLocal.class, 
            saveMethod="saveCadastreObject", loadMethod = "getCadastreObject")
    @ChildEntity(childIdField = "cadastreObjectId")
    private CadastreObject cadastreObject;
    
    @Column(name = "cadastre_object_id")
    private String cadastreObjectId;
    
    @Column(name = "ground_rent")
    private BigDecimal groundRent;
    
    @Column(name = "start_date")
    private Date startDate;
    
    @Column(name = "expiration_date")
    private Date expirationDate;
    
    @ExternalEJB(ejbLocalClass = PartyEJBLocal.class, loadMethod = "getParties", saveMethod="saveParty")
    @ChildEntityList(parentIdField = "leaseId", childIdField = "partyId", manyToManyClass = PartyForLease.class)
    private List<Party> lessees;
    
    @ChildEntityList(parentIdField = "leaseId", cascadeDelete = true)
    private List<LeaseSpecialCondition> leaseSpecialConditionList;
    
    @Column(name = "execution_date")
    private Date executionDate;
    
    @Column(name = "stamp_duty")
    private BigDecimal stampDuty;
    
    @Column(name = "status_code", insertable = false, updatable = false)
    private String statusCode;
    
    @Column(name = "transaction_id", updatable = false)
    private String transactionId;
    
    public Lease(){
        super();
    }

    public CadastreObject getCadastreObject() {
        return cadastreObject;
    }

    public void setCadastreObject(CadastreObject cadastreObject) {
        this.cadastreObject = cadastreObject;
    }

    public String getCadastreObjectId() {
        return cadastreObjectId;
    }

    public void setCadastreObjectId(String cadastreObjectId) {
        this.cadastreObjectId = cadastreObjectId;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public BigDecimal getGroundRent() {
        return groundRent;
    }

    public void setGroundRent(BigDecimal groundRent) {
        this.groundRent = groundRent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLeaseNumber() {
        return leaseNumber;
    }

    public void setLeaseNumber(String leaseNumber) {
        this.leaseNumber = leaseNumber;
    }

    public String getLesseeAddress() {
        return lesseeAddress;
    }

    public void setLesseeAddress(String lesseeAddress) {
        this.lesseeAddress = lesseeAddress;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public BigDecimal getStampDuty() {
        return stampDuty;
    }

    public void setStampDuty(BigDecimal stampDuty) {
        this.stampDuty = stampDuty;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<LeaseSpecialCondition> getLeaseSpecialConditionList() {
        return leaseSpecialConditionList;
    }

    public void setLeaseSpecialConditionList(List<LeaseSpecialCondition> leaseSpecialConditionList) {
        this.leaseSpecialConditionList = leaseSpecialConditionList;
    }

    public List<Party> getLessees() {
        return lessees;
    }

    public void setLessees(List<Party> lessees) {
        this.lessees = lessees;
    }
    
    @Override
    public void preSave(){
        if (this.isNew()) {
            setTransactionId(LocalInfo.getTransactionId());
        }
        super.preSave();
    }
}
