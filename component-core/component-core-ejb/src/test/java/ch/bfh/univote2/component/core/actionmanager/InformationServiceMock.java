/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.component.core.actionmanager;

import ch.bfh.univote2.component.core.manager.*;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.persistence.TenantInformationEntity;
import ch.bfh.univote2.component.core.services.InformationService;
import java.util.List;
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

	@Override
	public List<TenantInformationEntity> getTenantInforationEntities(String tenant, int limit) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<TenantInformationEntity> getTenantInforationEntities(String tenant, int limit, int start) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void informTenant(ActionContextKey actionContextKey, String information) {
		this.informTenant(actionContextKey.getAction(), actionContextKey.getTenant(), actionContextKey.getSection(),
				information);
	}

}
