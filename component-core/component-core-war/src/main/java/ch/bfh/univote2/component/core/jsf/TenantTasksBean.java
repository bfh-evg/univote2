/*
 * UniVote2
 *
 *  UniVote2(tm): An Internet-based, verifiable e-voting system for student elections in Switzerland
 *  Copyright (c) 2015 Bern University of Applied Sciences (BFH),
 *  Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *  Quellgasse 21, CH-2501 Biel, Switzerland
 *
 *  Licensed under Dual License consisting of:
 *  1. GNU Affero General Public License (AGPL) v3
 *  and
 *  2. Commercial license
 *
 *
 *  1. This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  2. Licensees holding valid commercial licenses for UniVote2 may use this file in
 *   accordance with the commercial license agreement provided with the
 *   Software or, alternatively, in accordance with the terms contained in
 *   a written agreement between you and Bern University of Applied Sciences (BFH),
 *   Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *   Quellgasse 21, CH-2501 Biel, Switzerland.
 *
 *
 *   For further information contact <e-mail: univote@bfh.ch>
 *
 *
 * Redistributions of files must retain the above copyright notice.
 */
package ch.bfh.univote2.component.core.jsf;

import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.data.RunActionTask;
import ch.bfh.univote2.component.core.data.Task;
import ch.bfh.univote2.component.core.data.UserInputTask;
import ch.bfh.univote2.component.core.manager.TaskManager;
import ch.bfh.univote2.component.core.services.OutcomeRoutingService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Named(value = "tenantTasksBean")
@ViewScoped
public class TenantTasksBean implements Serializable {

	@Inject
	TaskManager taskManager;

	@Inject
	OutcomeRoutingService outcomeRoutingService;

	@Inject
	LoginBean loginBean;

	private List<UserInputTask> userInputTasks;
	private List<RunActionTask> runActionTasks;

	/**
	 * Creates a new instance of TenantTasksBean
	 */
	public TenantTasksBean() {
		this.getTasks();
	}

	public String runTask(String notificationCode) {
		try {
			this.taskManager.runAction(notificationCode);
			//TODO outcome
			return "";
		} catch (UnivoteException ex) {
			//TODO Log
			Logger.getLogger(TenantTasksBean.class.getName()).log(Level.SEVERE, null, ex);
			return "";
		}
	}

	public String goToInputForm(String inputName) {
		try {
			return this.outcomeRoutingService.getRoutingForUserInput(inputName);
		} catch (UnivoteException ex) {
			//TODO
			Logger.getLogger(TenantTasksBean.class.getName()).log(Level.SEVERE, null, ex);
			return "";
		}
	}

	public List<UserInputTask> getUserInputTasks() {
		return userInputTasks;
	}

	public List<RunActionTask> getRunActionTasks() {
		return runActionTasks;
	}

	private void getTasks() {
		this.userInputTasks = new ArrayList<>();
		this.runActionTasks = new ArrayList<>();
		for (Task t : this.taskManager.getTasks(this.loginBean.getUsername())) {
			if (t instanceof UserInputTask) {
				this.userInputTasks.add((UserInputTask) t);
			} else if (t instanceof RunActionTask) {
				this.runActionTasks.add((RunActionTask) t);
			}
		}
	}

}
