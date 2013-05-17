/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sola.services.ejb.application.repository.entities;


import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;
import javax.persistence.Table;
import javax.persistence.Column;
@Table(name = "client_type", schema = "external")
@DefaultSorter(sortString="display_value")    
public class ApplicationForm extends AbstractCodeEntity{

    @Column(name="content")
    private byte[] content;

    public ApplicationForm() {
        super();
    }
    
    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
    
    
    
}
