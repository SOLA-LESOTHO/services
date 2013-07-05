package org.sola.services.ejb.search.repository.entities;

import org.sola.services.common.repository.entities.AbstractEntity;

public class CadastreObjectSearchParams extends AbstractEntity {
    private String nameFirstPart;
    private String nameLastPart;
    private String address;
    
    public CadastreObjectSearchParams(){
        super();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNameFirstPart() {
        return nameFirstPart;
    }

    public void setNameFirstPart(String nameFirstPart) {
        this.nameFirstPart = nameFirstPart;
    }

    public String getNameLastPart() {
        return nameLastPart;
    }

    public void setNameLastPart(String nameLastPart) {
        this.nameLastPart = nameLastPart;
    }
}
