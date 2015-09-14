/*
 * Copyright (c) 2014 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniBoard.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package ch.bfh.univote.admin.util;

import ch.bfh.uniboard.clientlib.GetHelper;
import ch.bfh.uniboard.clientlib.UniBoardAttributesName;
import ch.bfh.uniboard.data.AlphaIdentifierDTO;
import ch.bfh.uniboard.data.ConstraintDTO;
import ch.bfh.uniboard.data.EqualDTO;
import ch.bfh.uniboard.data.OrderDTO;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.QueryDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.StringValueDTO;
import ch.bfh.univote2.common.crypto.KeyUtil;
import java.security.interfaces.DSAPublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageGet {

	private static final String BOARD_CERTIFICATE_PATH = "board-certificate.pem";
	private static final String CERTIFICATE_PATH = "ea-certificate.pem";

	private static final String UNIBOARD_ADDRESS = "http://urd.bfh.ch:10080/UniBoardService/UniBoardServiceImpl";
	private static final String UNIBOARD_SECTION = "sub-2015";
	private static final String UNIBOARD_GROUP = "electionDefinition";

	public static void main(String[] args) throws Exception {
		DSAPublicKey boardPublicKey = KeyUtil.getDSAPublicKey(BOARD_CERTIFICATE_PATH);
		DSAPublicKey posterPublicKey = KeyUtil.getDSAPublicKey(CERTIFICATE_PATH);
		GetHelper getHelper = new GetHelper(boardPublicKey, UNIBOARD_ADDRESS + "?wsdl", UNIBOARD_ADDRESS);
		QueryDTO query = createQuery(UNIBOARD_SECTION, UNIBOARD_GROUP);

		ResultContainerDTO resultContainer = getHelper.get(query);
		System.out.println("Get successful");
		for (PostDTO post : resultContainer.getResult().getPost()) {
			if (getHelper.verifyPosterSignature(post, posterPublicKey)) {
				System.out.println(new String(post.getMessage(), "UTF-8"));
			} else {
				System.out.println("Invalid signature of post " + post);
			}
		}
	}

	private static QueryDTO createQuery(String section, String group) {
		AlphaIdentifierDTO sectionIdentifier
				= new AlphaIdentifierDTO(Collections.singletonList(UniBoardAttributesName.SECTION.getName()));
		AlphaIdentifierDTO groupIdentifier
				= new AlphaIdentifierDTO(Collections.singletonList(UniBoardAttributesName.GROUP.getName()));
		List<ConstraintDTO> contraints = new ArrayList<>();
		contraints.add(new EqualDTO(sectionIdentifier, new StringValueDTO(section)));
		contraints.add(new EqualDTO(groupIdentifier, new StringValueDTO(group)));
		List<OrderDTO> orders = new ArrayList<>();
		orders.add(new OrderDTO(groupIdentifier, true));
		return new QueryDTO(contraints, orders, 0);
	}
}
