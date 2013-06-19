package org.sola.services.ejb.administrative.repository.entities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.common.StringUtility;
import org.sola.services.common.repository.ChildEntity;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.ExternalEJB;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;
import org.sola.services.ejb.cadastre.businesslogic.CadastreEJBLocal;
import org.sola.services.ejb.cadastre.repository.entities.CadastreObject;
import org.sola.services.ejb.party.businesslogic.PartyEJBLocal;
import org.sola.services.ejb.party.repository.entities.Party;
import org.sola.services.ejb.source.businesslogic.SourceEJBLocal;
import org.sola.services.ejb.source.repository.entities.Source;

/**
 * Entity representing administrative.lease table.
 */
@Table(name = "lease", schema = "administrative")
public class Lease extends AbstractVersionedEntity {
    @Id
    String id;
    
    @Column(name = "original_id")
    String originalId;
    
    @Column(name = "lease_number")
    String leaseNumber;
    
    @Column(name = "marital_status")
    String maritalStatus;
    
    @Column(name = "lessee_address")
    String lesseeAddress;
    
    @ExternalEJB(ejbLocalClass = CadastreEJBLocal.class, 
            saveMethod="saveCadastreObject", loadMethod = "getCadastreObject")
    @ChildEntity(childIdField = "cadastreObjectId", readOnly=true)
    private CadastreObject cadastreObject;
    
    @Column(name = "cadastre_object_id")
    private String cadastreObjectId;
    
    @Column(name = "ground_rent")
    BigDecimal groundRent;
    
    @Column(name = "start_date")
    Date startDate;
    
    @Column(name = "expiration_date")
    Date expirationDate;
    
    @Column(name = "lease_term")
    String leaseTerm;
    
    @Column(name = "lease_document_id")
    private String leaseDocumentId;
    
    @ExternalEJB(ejbLocalClass = SourceEJBLocal.class, loadMethod = "getSourceById", saveMethod="saveSource")
    @ChildEntity(childIdField = "leaseDocumentId")
    private Source leaseDocument;
    
    @ExternalEJB(ejbLocalClass = PartyEJBLocal.class, loadMethod = "getParties")
    @ChildEntityList(parentIdField = "rrrId", childIdField = "partyId",
            manyToManyClass = PartyForRrr.class, readOnly = true)
    private List<Party> lessees;
    
    @ChildEntityList(parentIdField = "leaseId", cascadeDelete = true)
    private List<LeaseSpecialCondition> leaseSpecialConditionList;
    
    @Column(name = "execution_date")
    Date executionDate;
    
    @Column(name = "registration_date")
    Date registrationDate;
    
    @Column(name = "registration_fee")
    BigDecimal registrationFee;
    
    @Column(name = "stamp_duty")
    BigDecimal stampDuty;
    
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

    public Source getLeaseDocument() {
        return leaseDocument;
    }

    public void setLeaseDocument(Source leaseDocument) {
        this.leaseDocument = leaseDocument;
    }

    public String getLeaseDocumentId() {
        return leaseDocumentId;
    }

    public void setLeaseDocumentId(String leaseDocumentId) {
        this.leaseDocumentId = leaseDocumentId;
    }

    public String getLeaseNumber() {
        return leaseNumber;
    }

    public void setLeaseNumber(String leaseNumber) {
        this.leaseNumber = leaseNumber;
    }

    public String getLeaseTerm() {
        return leaseTerm;
    }

    public void setLeaseTerm(String leaseTerm) {
        this.leaseTerm = leaseTerm;
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

    public String getOriginalId() {
        return originalId;
    }

    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public BigDecimal getRegistrationFee() {
        return registrationFee;
    }

    public void setRegistrationFee(BigDecimal registrationFee) {
        this.registrationFee = registrationFee;
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
        if(StringUtility.isEmpty(getOriginalId())){
            setOriginalId(getId());
        }
    }
}
