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

import ch.bfh.univote2.component.core.manager.TenantManager;
import java.io.Serializable;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Named(value = "loginBean")
@SessionScoped
public class LoginBean implements Serializable {

	private boolean loggedIn = false;
	private String username;
	private String password;
	@Inject
	TenantManager tenantManager;

	/**
	 * Creates a new instance of LoginBean
	 */
	public LoginBean() {
	}

	public String doLogin() {
		if (tenantManager.checkLogin(username, password)) {
			this.loggedIn = true;
			this.password = null;
			return "/secured/welcome";
		}
		MessageFactory.error("core", "login_error");
		return "/login";
	}

	public String doLogout() {
		this.loggedIn = false;
		this.password = null;
		MessageFactory.info("core", "logout_success");
		return "/index";

	}

	public String doLock() {
		if (!this.tenantManager.lock(username, password)) {
			MessageFactory.error("core", "locking_error");
		}
		this.password = null;
		MessageFactory.info("core", "locking_success");
		return null;
	}

	public String doUnlock() {
		if (!this.tenantManager.unlock(username, password)) {
			MessageFactory.error("core", "unlocking_error");
		}
		this.password = null;
		MessageFactory.info("core", "unlocking_success");
		return null;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public boolean isLocked() {
		return this.tenantManager.isLocked(username);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
