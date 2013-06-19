package org.sola.services.ejb.administrative.repository.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(name = "lease_special_condition", schema = "administrative")
public class LeaseSpecialCondition extends AbstractVersionedEntity {
    
    @Id
    @Column
    private String id;
    
    @Column(name="lease_id")
    private String leaseId;

    @Column(name="condition_text")
    private String conditionText;
  
    public LeaseSpecialCondition(){
        super();
    }

    public String getConditionText() {
        return conditionText;
    }

    public void setConditionText(String conditionText) {
        this.conditionText = conditionText;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(String leaseId) {
        this.leaseId = leaseId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
