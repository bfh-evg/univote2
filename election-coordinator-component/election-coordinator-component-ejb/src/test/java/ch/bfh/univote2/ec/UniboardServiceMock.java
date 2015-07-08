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
package ch.bfh.univote2.ec;

import ch.bfh.uniboard.data.AttributesDTO;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.QueryDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.ResultDTO;
import ch.bfh.uniboard.data.StringValueDTO;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.services.UniboardService;
import java.util.concurrent.ArrayBlockingQueue;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
@LocalBean
public class UniboardServiceMock implements UniboardService {

	private final ArrayBlockingQueue<ResultDTO> response = new ArrayBlockingQueue<>(10);
	private PostDTO post;

	@Override
	public ResultContainerDTO get(String board, QueryDTO query) throws UnivoteException {
		ResultContainerDTO result = new ResultContainerDTO();
		result.setResult(this.response.poll());
		return result;
	}

	@Override
	public AttributesDTO post(String board, String section, String group, byte[] message, String tennant) throws UnivoteException {
		AttributesDTO alpha = new AttributesDTO();
		alpha.getAttribute().add(new AttributesDTO.AttributeDTO("board", new StringValueDTO(board)));
		alpha.getAttribute().add(new AttributesDTO.AttributeDTO("section", new StringValueDTO(section)));
		alpha.getAttribute().add(new AttributesDTO.AttributeDTO("group", new StringValueDTO(group)));
		alpha.getAttribute().add(new AttributesDTO.AttributeDTO("tenant", new StringValueDTO(tennant)));
		this.post = new PostDTO(message, alpha, alpha);
		return alpha;
	}

	public void addResponse(ResultDTO response) throws InterruptedException {
		this.response.put(response);
	}

	public PostDTO getPost() {
		return this.post;
	}

}
