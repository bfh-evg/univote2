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
package ch.bfh.univote2.component.core.data;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class ActionContext {

    private final String action;
    private final String tenant;
    private final String section;
    private final List<PreconditionQuery> preconditionQueries;

    public ActionContext(String action, String tenant, String section, List<PreconditionQuery> preconditionQueries) {
        this.action = action;
        this.tenant = tenant;
        this.section = section;
        this.preconditionQueries = preconditionQueries;
    }

    public String getAction() {
        return action;
    }

    public String getTenant() {
        return tenant;
    }

    public String getSection() {
        return section;
    }

    public List<PreconditionQuery> getPreconditionQueries() {
        return preconditionQueries;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.action);
        hash = 97 * hash + Objects.hashCode(this.tenant);
        hash = 97 * hash + Objects.hashCode(this.section);
        hash = 97 * hash + Objects.hashCode(this.preconditionQueries);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ActionContext other = (ActionContext) obj;
        if (!Objects.equals(this.action, other.action)) {
            return false;
        }
        if (!Objects.equals(this.tenant, other.tenant)) {
            return false;
        }
        if (!Objects.equals(this.section, other.section)) {
            return false;
        }
        return Objects.equals(this.preconditionQueries, other.preconditionQueries);
    }

}
