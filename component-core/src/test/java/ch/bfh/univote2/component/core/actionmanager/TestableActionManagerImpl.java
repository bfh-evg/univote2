/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.component.core.actionmanager;

import ch.bfh.univote2.component.core.actionmanager.ActionManagerImpl;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import java.util.List;
import javax.ejb.Singleton;

/**
 * Deactivates the startup of ActionManagerImpl, makes some operations visible and allows to modify and read the
 * internal states.
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
public class TestableActionManagerImpl extends ActionManagerImpl {
    
    @Override
    public void init() {
    }
    
    public void pubCheckActionState(String tenant, String section, String actionName) {
        this.checkActionState(tenant, section, actionName);
    }
    
    @Override
    public void addActionContext(ActionContext actionContext) {
        super.addActionContext(actionContext);
    }
    
    @Override
    public void addActionGraphEntry(String actionName, List<String> successors) {
        super.addActionGraphEntry(actionName, successors);
    }
    
}
