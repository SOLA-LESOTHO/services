/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sola.services.ejb.application.repository.entities;


import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;
import javax.persistence.Table;
import javax.persistence.Column;
@Table(name = "application_form", schema = "application")
@DefaultSorter(sortString="display_value")    
public class ApplicationForm extends AbstractCodeEntity{

}
