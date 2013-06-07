package org.sola.services.ejb.application.repository.entities;

import javax.persistence.Column;
import javax.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

@Table(name = "application_form", schema = "application")
@DefaultSorter(sortString="display_value")  
public class ApplicationFormWithBinary extends AbstractCodeEntity{
    @Column
    private byte[] content;
    @Column
    private String extension;
    
    public ApplicationFormWithBinary(){
        super();
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
