/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.component.core.manager;

import ch.bfh.univote2.component.core.services.InformationService;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;

@Singleton
@LocalBean
public class InformationServiceMock implements InformationService {

    private String actionName;
    private String tenant;
    private String section;
    private String information;

    @Override
    public void informTenant(String actionName, String tenant, String section, String information) {
        this.actionName = actionName;
        this.information = information;
        this.tenant = tenant;
        this.section = section;
    }

    public String getActionName() {
        return actionName;
    }

    public String getTenant() {
        return tenant;
    }

    public String getSection() {
        return section;
    }

    public String getInformation() {
        return information;
    }

}
