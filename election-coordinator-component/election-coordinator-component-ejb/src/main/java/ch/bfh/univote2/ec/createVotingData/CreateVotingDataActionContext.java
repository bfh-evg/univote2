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
package ch.bfh.univote2.ec.createVotingData;

import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.message.ElectionDefinition;
import ch.bfh.univote2.component.core.message.ElectionIssue;
import ch.bfh.univote2.component.core.message.ElectoralRoll;
import ch.bfh.univote2.component.core.message.EncryptionKey;
import java.util.ArrayList;

/**
 *
 * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
public class CreateVotingDataActionContext extends ActionContext {
	private ElectionDefinition electionDefinition;
	private EncryptionKey encryptionKey;
	private ElectionIssue electionIssue;
	private ElectoralRoll electoralRoll;

	public CreateVotingDataActionContext(ActionContextKey actionContextKey) {
		super(actionContextKey, new ArrayList<>(), false);
	}

	@Override
	protected void purgeData() {
		this.electionDefinition = null;
		this.encryptionKey = null;
		this.electionIssue = null;
		this.electoralRoll = null;
	}

	ElectionDefinition getElectionDefinition() {
		return electionDefinition;
	}

	EncryptionKey getEncryptionKey() {
		return encryptionKey;
	}

	ElectionIssue getElectionIssue() {
		return electionIssue;
	}

	ElectoralRoll getElectoralRoll() {
		return electoralRoll;
	}

	void setElectionDefinition(ElectionDefinition electionDefinition) {
		this.electionDefinition = electionDefinition;
	}

	void setEncryptionKey(EncryptionKey encryptionKey) {
		this.encryptionKey = encryptionKey;
	}

	void setElectionIssue(ElectionIssue electionIssue) {
		this.electionIssue = electionIssue;
	}

	void setElectoralRoll(ElectoralRoll electoralRoll) {
		this.electoralRoll = electoralRoll;
	}

	boolean gotAllNotifications() {
		return	this.electionDefinition != null &&
				this.encryptionKey != null &&
				this.electionIssue != null &&
				this.electoralRoll != null;
	}
}
