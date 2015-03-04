/*
 * Copyright (c) 2014 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniVote2.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package ch.bfh.univote2.component.core.action;

import ch.bfh.univote2.component.core.data.ActionContext;
import javax.ejb.Local;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Local
public abstract class NotifiableAction implements Action {

    public abstract void notifyAction(ActionContext actionContext, Object notification);

    public ActionContext prepareContext(String tenant, String section) {
        ActionContext actionContext = this.createContext(section, tenant);
        //TODO Try to retrieve the preconditions and either store them in the context or the corresponding query
        return actionContext;
    }

    protected abstract ActionContext createContext(String section, String tenant);

}
