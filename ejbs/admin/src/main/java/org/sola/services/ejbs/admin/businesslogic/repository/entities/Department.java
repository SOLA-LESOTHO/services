/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sola.services.ejbs.admin.businesslogic.repository.entities;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractEntity;


@Table(name = "appdepartment", schema = "system")
@DefaultSorter(sortString="name")
public class Department extends AbstractEntity{
    
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    
    // @ChildEntityList(parentIdField = "departmentId", cascadeDelete=false)
    private List<User> departmentUsers;
    
    public Department(){
        super();
    }

    public List<User> getDepartmentUsers() {
        return departmentUsers;
    }

    public void setDepartmentUsers(List<User> departmentUsers) {
        this.departmentUsers = departmentUsers;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        if (id == null) {
            id = generateId();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
