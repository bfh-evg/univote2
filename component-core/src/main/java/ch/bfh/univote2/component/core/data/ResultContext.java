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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class ResultContext {

	private ResultStatus status;
	private final String tenant;
	private final String section;
	private final String actionName;
	private final List<String> output;

	public ResultContext(String tenant, String section, String actionName) {
		this.tenant = tenant;
		this.section = section;
		this.actionName = actionName;
		this.output = new ArrayList<>();
	}

	public void setStatus(ResultStatus status) {
		this.status = status;
	}

	public void addOutput(String output) {
		this.output.add(output);
	}

	public ResultStatus getStatus() {
		return status;
	}

	public String getTenant() {
		return tenant;
	}

	public String getSection() {
		return section;
	}

	public String getActionName() {
		return actionName;
	}

	public List<String> getOutput() {
		return output;
	}

}
