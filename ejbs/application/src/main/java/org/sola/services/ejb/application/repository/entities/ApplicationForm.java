package org.sola.services.ejb.application.repository.entities;

import javax.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

@Table(name = "application_form", schema = "application")
@DefaultSorter(sortString="display_value")    
public class ApplicationForm extends AbstractCodeEntity{
    public ApplicationForm(){
        super();
    }
}
