package org.sola.services.ejbs.billing.repository.entities;

import javax.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 * This entity represents pay period reference data
 * @author nmafereka
 */
@Table(name = "pay_period", schema = "billing")
@DefaultSorter(sortString="display_value")
public class PayPeriod extends AbstractCodeEntity {
    public PayPeriod(){
        super();
    }
}