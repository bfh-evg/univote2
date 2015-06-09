/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.component.core.actionmanager;

import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
@LocalBean
public class SecondMockAction implements NotifiableAction {

	private final List<ActionContextKey> lastRunActionContext = new ArrayList<>();

	private final List<ActionContextKey> lastNotifyActionContext = new ArrayList<>();

	private final Map<ActionContextKey, ActionContext> actionContexts = new HashMap<>();

	@Override
	public void notifyAction(ActionContext actionContext, Object notification) {
		this.lastNotifyActionContext.add(actionContext.getActionContextKey());
	}

	@Override
	public void run(ActionContext actionContext) {
		this.lastRunActionContext.add(actionContext.getActionContextKey());
	}

	@Override
	public ActionContext prepareContext(String tenant, String section) {
		return this.actionContexts.get(new ActionContextKey("SecondMockAction", tenant, section));
	}

	public boolean containsRun(ActionContextKey ack) {
		return this.lastRunActionContext.remove(ack);
	}

	public boolean containsNotify(ActionContextKey ack) {
		return this.lastNotifyActionContext.remove(ack);
	}

	public void addActionContext(ActionContext ac) {
		this.actionContexts.put(ac.getActionContextKey(), ac);
	}

}
