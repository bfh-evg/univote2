/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.component.core.actionmanager;

import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.component.core.action.Action;
import ch.bfh.univote2.component.core.data.NotificationData;
import ch.bfh.univote2.component.core.data.NotificationDataAccessor;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;

/**
 * Deactivates the startup of ActionManagerImpl, makes some operations visible and allows to modify and read the
 * internal states.
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
@DependsOn("ConfigurationManagerMock")
public class TestableActionManagerImpl extends ActionManagerImpl {

	@Override
	public void init() {
	}

	public void testInit() {
		super.init();
	}

	public void pubCheckActionState(String tenant, String section, String actionName) {
		this.checkActionState(tenant, section, actionName);
	}

	@Override
	public NotificationDataAccessor getNotificationDataAccessor() {
		return super.getNotificationDataAccessor();
	}

	@Override
	public void addActionContext(ActionContext actionContext) {
		super.addActionContext(actionContext);
	}

	@Override
	public void addActionGraphEntry(String actionName, List<String> successors) {
		super.addActionGraphEntry(actionName, successors);
	}

	@Override
	public Map<String, List<String>> getActionGraph() {
		return super.getActionGraph();
	}

	@Override
	public void addNotificationData(NotificationData notificationData) {
		super.addNotificationData(notificationData);
	}

	public void getActionTest(ActionContext ac) throws UnivoteException {
		Action a = this.getAction(ac.getActionContextKey().getAction());
		a.run(ac);
	}

	@Override
	public void runAction(ActionContext ac) throws UnivoteException {
		super.runAction(ac);
	}

	@Override
	public void registerAction(ActionContext actionContext) throws UnivoteException {
		super.registerAction(actionContext);
	}

	@Override
	public void unregisterAction(ActionContext actionContext) {
		super.unregisterAction(actionContext);
	}

	@Override
	public void setInitialAction(String action) {
		super.setInitialAction(action);
	}

	@Override
	protected void log(String msg, Level level) {
	}
}
