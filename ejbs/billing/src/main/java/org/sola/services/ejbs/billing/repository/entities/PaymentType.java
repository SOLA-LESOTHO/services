package org.sola.services.ejbs.billing.repository.entities;

import javax.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 *
 * @author nmafereka
 */
@Table(name = "payment_type", schema = "billing")
@DefaultSorter(sortString="display_value")
public class PaymentType extends AbstractCodeEntity {
    public PaymentType(){
        super();
    }
}
