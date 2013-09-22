package org.sola.services.ejb.administrative.repository.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

/**
 * Entity representing the administrative.party_for_consent association table.
 */
@Table(name = "consent_party", schema = "administrative")
public class PartyForConsent extends AbstractVersionedEntity {
    @Id
    @Column(name = "consent_id")
    private String consentId;
    @Id
    @Column(name = "party_id")
    private String partyId;
    
    public PartyForConsent(){
        super();
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }
}
