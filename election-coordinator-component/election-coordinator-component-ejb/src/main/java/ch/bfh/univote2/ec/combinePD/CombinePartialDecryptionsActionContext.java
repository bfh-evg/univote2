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
package ch.bfh.univote2.ec.combinePD;

import ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.common.message.CryptoSetting;
import ch.bfh.univote2.common.message.MixedVotes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class CombinePartialDecryptionsActionContext extends ActionContext {

	private int amount = -1;
	private final Map<String, List<String>> partialDecryptions = new HashMap<>();
	private Function[] generatorFunctions;
	private CryptoSetting cryptoSetting;
	private MixedVotes mixedVotes;

	public CombinePartialDecryptionsActionContext(ActionContextKey actionContextKey) {
		super(actionContextKey, new ArrayList<>(), false);
	}

	@Override
	protected void purgeData() {
		this.partialDecryptions.clear();
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getAmount() {
		return amount;
	}

	public Map<String, List<String>> getPartialDecryptions() {
		return partialDecryptions;
	}

	public CryptoSetting getCryptoSetting() {
		return cryptoSetting;
	}

	public void setCryptoSetting(CryptoSetting cryptoSetting) {
		this.cryptoSetting = cryptoSetting;
	}

	public Function[] getGeneratorFunctions() {
		return generatorFunctions;
	}

	public void setGeneratorFunctions(Function[] generatorFunctions) {
		this.generatorFunctions = generatorFunctions;
	}

	public MixedVotes getMixedVotes() {
		return mixedVotes;
	}

	public void setMixedVotes(MixedVotes mixedVotes) {
		this.mixedVotes = mixedVotes;
	}

}