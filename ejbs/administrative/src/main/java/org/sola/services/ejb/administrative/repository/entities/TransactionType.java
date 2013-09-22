package org.sola.services.ejb.administrative.repository.entities;

import javax.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 * Entity representing the administrative.transaction_type reference data table.
 */

@Table(name = "transaction_type", schema = "administrative")
@DefaultSorter(sortString = "display_value")
public class TransactionType extends AbstractCodeEntity {

    public TransactionType() {
        super();
    }
}
