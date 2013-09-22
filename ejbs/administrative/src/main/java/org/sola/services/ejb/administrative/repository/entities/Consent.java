package org.sola.services.ejb.administrative.repository.entities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.LocalInfo;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.ExternalEJB;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;
import org.sola.services.ejb.party.businesslogic.PartyEJBLocal;
import org.sola.services.ejb.party.repository.entities.Party;

/**
 *
 * Entity representing administrative.consent table.
 */
@Table(name = "consent", schema = "administrative")
public class Consent extends AbstractVersionedEntity {
    
    public static final String QUERY_PARAMETER_TRANSACTION_ID = "transactionId";
    public static final String QUERY_WHERE_BY_TRANSACTION_ID = "transaction_id = "
            + "#{" + QUERY_PARAMETER_TRANSACTION_ID + "}";
    
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "reg_date")
    private Date registrationDate;
    @Column(name = "transaction_id", updatable = false)
    private String transactionId;
    @Column(name = "expiration_date")
    private Date expirationDate;
    @Column(name="amount")
    private BigDecimal amount;
    @Column(name="transaction_type_code")
    private String transactionTypeCode;
    @Column(name="special_conditions")
    private String specialConditions;
    @ExternalEJB(ejbLocalClass = PartyEJBLocal.class, loadMethod = "getParties")
    @ChildEntityList(parentIdField = "consentId", childIdField = "partyId",
            manyToManyClass = PartyForConsent.class)
    private List<Party> transfereeList;
    
    public Consent(){
        super();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getSpecialConditions() {
        return specialConditions;
    }

    public void setSpecialConditions(String specialConditions) {
        this.specialConditions = specialConditions;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionTypeCode() {
        return transactionTypeCode;
    }

    public void setTransactionTypeCode(String transactionTypeCode) {
        this.transactionTypeCode = transactionTypeCode;
    }

    public List<Party> getTransfereeList() {
        return transfereeList;
    }

    public void setTransfereeList(List<Party> transfereeList) {
        this.transfereeList = transfereeList;
    }
    
    @Override
    public void preSave() {
        if (this.isNew()) {
            setTransactionId(LocalInfo.getTransactionId());
        }
        super.preSave();
    }
}
