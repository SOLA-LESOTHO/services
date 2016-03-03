package org.sola.services.ejbs.billing.repository.entities;

import javax.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

/**
 * This entity represents pay period reference data
 *
 * @author nmafereka
 */
@Table(name = "calculation_type", schema = "billing")
@DefaultSorter(sortString = "display_value")
public class CalculationType extends AbstractCodeEntity {

    public static final String FIXED_VALUE = "fixed";
    public static final String FLAT_RATE = "percentage";

    public CalculationType() {
        super();
    }
}
