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

import ch.bfh.uniboard.clientlib.AttributeHelper;
import ch.bfh.uniboard.data.AttributesDTO.AttributeDTO;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.ResultDTO;
import ch.bfh.uniboard.data.StringValueDTO;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.common.message.EncryptionKey;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.message.MixedKeys;
import ch.bfh.univote2.common.query.AlphaEnum;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.common.query.MessageFactory;
import ch.bfh.univote2.common.query.QueryFactory;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.BoardsEnum;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

/**
 *
 * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
@Stateless
public class CreateVotingDataAction extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = "CreateVotingDataAction";
	private static final Logger logger = Logger.getLogger(CreateVotingDataAction.class.getName());

	@EJB
	private ActionManager actionManager;
	@EJB
	private InformationService informationService;
	@EJB
	private UniboardService uniboardService;
	@EJB
	private TenantManager tenantManager;

	@Override
	protected ActionContext createContext(String tenant, String section) {
		ActionContextKey ack = new ActionContextKey(ACTION_NAME, tenant, section);
		return new CreateVotingDataActionContext(ack);
	}

	/**
	 * Checks whether a voting data message has already been sent to the board, and returns true if so, false otherwise.
	 *
	 * @param actionContext the context for this action
	 * @return true iff a voting data message was previously sent to the board, false otherwise
	 */
	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		try {
			ResultContainerDTO container = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForVotingData(actionContext.getSection()));
			ResultDTO result = container.getResult();
			return !result.getPost().isEmpty();
		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, "Could not request trustees certificates.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not check post condition.");
			return false;
		}
	}

	/**
	 * Try to retrieve, in order, each of the JSON components necessary to build the voting data message from the board.
	 * For each non-available JSON component, set up a future notification on the board.
	 *
	 * @param actionContext the action context associated to this action
	 */
	@Override
	protected void definePreconditions(ActionContext actionContext) {
		CreateVotingDataActionContext context = (CreateVotingDataActionContext) actionContext;
		JsonObject electionDefinition = getElectionDefinition(context);
		if (electionDefinition != null) {
			context.setElectionDefinition(electionDefinition);
		} else {
			prepareForElectionDefinitionNotification(context);
		}
		JsonObject electionDetails = getElectionDetails(context);
		if (electionDetails != null) {
			context.setElectionDetails(electionDetails);
		} else {
			prepareForElectionDetailsNotification(context);
		}
		JsonObject cryptoSetting = getCryptoSetting(context);
		if (cryptoSetting != null) {
			context.setCryptoSetting(cryptoSetting);
		} else {
			prepareForCryptoSettingNotification(context);
		}
		String encryptionKey = getEncryptionKey(context);
		if (encryptionKey != null) {
			context.setEncryptionKey(encryptionKey);
		} else {
			prepareForEncryptionKeyNotification(context);
		}
		String signatureGenerator = getSignatureGenerator(context);
		if (signatureGenerator != null) {
			context.setSignatureGenerator(signatureGenerator);
		} else {
			prepareForSignatureGeneratorNotification(context);
		}
	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		this.informationService.informTenant(actionContext.getActionContextKey(), "Running.");
		if (actionContext instanceof CreateVotingDataActionContext) {
			CreateVotingDataActionContext context = (CreateVotingDataActionContext) actionContext;
			definePreconditions(context);
			if (context.gotAllNotifications()) {
				runInternal(context); // handles action manager notifications, failures
			} else {
				if (context.getElectionDefinition() == null) {
					JsonObject electionDefinition = getElectionDefinition(context);
					if (electionDefinition != null) {
						context.setElectionDefinition(electionDefinition);
					} else {
						this.informationService.informTenant(context.getActionContextKey(),
								"Election definition not yet published");
					}
				}
				if (context.getElectionDetails() == null) {
					JsonObject electionDetails = getElectionDetails(context);
					if (electionDetails != null) {
						context.setElectionDetails(electionDetails);
					} else {
						this.informationService.informTenant(context.getActionContextKey(),
								"Election details not yet published");
					}
				}
				if (context.getCryptoSetting() == null) {
					JsonObject cryptoSetting = getCryptoSetting(context);
					if (cryptoSetting != null) {
						context.setCryptoSetting(cryptoSetting);
					} else {
						this.informationService.informTenant(context.getActionContextKey(),
								"Crypto setting not yet published");
					}
				}
				if (context.getEncryptionKey() == null) {
					String encryptionKey = getEncryptionKey(context);
					if (encryptionKey != null) {
						context.setEncryptionKey(encryptionKey);
					} else {
						this.informationService.informTenant(context.getActionContextKey(),
								"Encryption key not yet published");
					}
				}
				if (context.getSignatureGenerator() == null) {
					String signatureGenerator = getSignatureGenerator(context);
					if (signatureGenerator != null) {
						context.setSignatureGenerator(signatureGenerator);
					} else {
						this.informationService.informTenant(context.getActionContextKey(),
								"Crypto setting not yet published");
					}
				}
				if (context.gotAllNotifications()) {
					runInternal(context); // handles action manager notifications, failures
				} else {
					this.actionManager.runFinished(context, ResultStatus.RUN_FINISHED);
				}
//				// Wait for another notification
//				this.actionManager.runFinished(context, ResultStatus.RUN_FINISHED);
			}
		} else {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Unsupported context.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	/**
	 * If notified we expect one out of four different messages. See
	 * {@link #parsePostDTO(PostDTO, CreateVotingDataActionContext)} for a JSON schema mentioning the expected JSON
	 * messages.
	 *
	 * @param actionContext the action context for this action
	 * @param notification the notification containing one of the expected messages
	 */
	@Override
	@Asynchronous
	public void notifyAction(ActionContext actionContext, Object notification
	) {
		this.informationService.informTenant(actionContext.getActionContextKey(), "Notified.");
		if (actionContext instanceof CreateVotingDataActionContext) {
			CreateVotingDataActionContext context = (CreateVotingDataActionContext) actionContext;
			if (notification instanceof PostDTO) {
				PostDTO post = (PostDTO) notification;
				try {
					parsePostDTO(post, context);
					// Let's see iff we got all messages
					if (context.gotAllNotifications()) {
						runInternal(context); // handles action manager notification, failures
					} else {
						// Wait for another notification
						this.actionManager.runFinished(context, ResultStatus.RUN_FINISHED);
					}
				} catch (UnivoteException ex) {
					logger.log(Level.WARNING, "Do not understand message.", ex);
					this.informationService.informTenant(actionContext.getActionContextKey(),
							"Do not understand message.");
					this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				}
			}
		} else {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Unsupported context.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	/**
	 * Given a DTO of a JSON post, let's try to parse it in order to obtain one of its properties to be stored in the
	 * given context. The schema of the expected post is:
	 * <pre>
	 * {
	 * "$schema": "http://jsonVotingData-schema.org/draft-04/schema",
	 * "title": "UniVote2: Schema of voting data",
	 * "type": "object",
	 * "properties": {
	 * "definition": {
	 * "description": "Election definition",
	 * "$ref": "electionDefinition.jsd"
	 * },
	 * "details": {
	 * "description": "Election details",
	 * "$ref": "electionDetails.jsd"
	 * },
	 * "cryptoSetting": {
	 * "description": "Crypto setting (encryption, signature, hashing)",
	 * "$ref": "cryptoSetting.jsd"
	 * },
	 * "encryptionKey": {
	 * "description": "Encryption key (decimal notation)",
	 * "type": "string"
	 * },
	 * "signatureGenerator": {
	 * "description": "Signature generator (decimal notation)",
	 * "type": "string"
	 * }
	 * },
	 * "required": ["definition", "details", "signatureGenerator", "signatureGenerator", "signatureGenerator"],
	 * "additionalProperties": false
	 * }
	 * </pre>
	 *
	 * @param post the post to parse
	 * @param context a context to store the parsed post
	 * @throws UnivoteException if the post cannot be parsed
	 */
	private void parsePostDTO(PostDTO post, CreateVotingDataActionContext context)
			throws UnivoteException {
		AttributeDTO group = AttributeHelper.searchAttribute(post.getAlpha(), AlphaEnum.GROUP.getValue());
		if (((StringValueDTO) group.getValue()).getValue().equals(GroupEnum.ELECTION_DEFINITION.getValue())) {
			JsonObject electionDefinition = unmarshal(post.getMessage());
			context.setElectionDefinition(electionDefinition);
		} else if (((StringValueDTO) group.getValue()).getValue().equals(GroupEnum.ELECTION_DETAILS.getValue())) {
			JsonObject electionDetails = unmarshal(post.getMessage());
			context.setElectionDetails(electionDetails);
		} else if (((StringValueDTO) group.getValue()).getValue().equals(GroupEnum.CRYPTO_SETTING.getValue())) {
			JsonObject cryptoSetting = unmarshal(post.getMessage());
			context.setCryptoSetting(cryptoSetting);
		} else if (((StringValueDTO) group.getValue()).getValue().equals(GroupEnum.ENCRYPTION_KEY.getValue())) {
			EncryptionKey encryptionKey = JSONConverter.unmarshal(EncryptionKey.class, post.getMessage());
			context.setEncryptionKey(encryptionKey.getEncryptionKey());
		} else if (((StringValueDTO) group.getValue()).getValue().equals(GroupEnum.MIXED_KEYS.getValue())) {
			MixedKeys mixedKeys = JSONConverter.unmarshal(MixedKeys.class, post.getMessage());
			String signatureGenerator = mixedKeys.getGenerator();
			context.setSignatureGenerator(signatureGenerator);
		} else {
			// Fatal error: Do not understand post.
			throw new UnivoteException("Do not understand message.");
		}
	}

	/**
	 * Given a context having all required notifications, compile a voting data message and post it onto the board.
	 *
	 * @param context a context containing the required notifications
	 */
	private void runInternal(CreateVotingDataActionContext context) {
		try {
			//post ac
			PublicKey pk = this.tenantManager.getPublicKey(context.getTenant());
			byte[] arMessage = MessageFactory.createAccessRight(GroupEnum.VOTING_DATA, pk, 1);
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), context.getSection(),
					GroupEnum.ACCESS_RIGHT.getValue(), arMessage, context.getTenant());
		} catch (UnivoteException ex) {
			logger.log(Level.SEVERE, "Could not post ACCESS_RIGHT message: {0}", ex.getMessage());
			this.informationService.informTenant(context.getActionContextKey(),
					"Could not post ACCESS_RIGHT message: " + ex.getMessage());
			this.actionManager.runFinished(context, ResultStatus.FAILURE);
			return;
		}
		// TODO: Adjust table in subsectino 11.2.5
		JsonObject electionDefinition = context.getElectionDefinition();
		JsonObject electionDetails = context.getElectionDetails();
		JsonObject cryptoSetting = context.getCryptoSetting();
		String encryptionKey = context.getEncryptionKey();
		String signatureGenerator = context.getSignatureGenerator();
		// Compile a voting data JsonObject instance
		JsonObject votingData = Json.createObjectBuilder()
				.add("definition", electionDefinition)
				.add("details", electionDetails)
				.add("cryptoSetting", cryptoSetting)
				.add("encryptionKey", encryptionKey)
				.add("signatureGenerator", signatureGenerator)
				.build();
		byte[] jsonVotingData = marshal(votingData);
		logger.log(Level.INFO, new String(jsonVotingData));
		try {
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), context.getSection(),
					GroupEnum.VOTING_DATA.getValue(), jsonVotingData,
					context.getTenant());
			logger.log(Level.INFO, "Posted VOTING_DATA message.");
			this.informationService.informTenant(context.getActionContextKey(),
					"Posted VOTING_DATA message.");
			this.actionManager.runFinished(context, ResultStatus.FINISHED);
		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, "Could not post VOTING_DATA message.", ex);
			this.informationService.informTenant(context.getActionContextKey(),
					"Could not post VOTING_DATA message.");
			this.actionManager.runFinished(context, ResultStatus.FAILURE);
		}
	}

	/**
	 * Given a byte array, unmarshals the byte array and returns a JsonObject instance.
	 *
	 * @param message a JSON message as a byte array
	 * @return a JsonObject instance
	 */
	private JsonObject unmarshal(byte[] message) {
		InputStream is = new ByteArrayInputStream(message);
		JsonReader reader = Json.createReader(is);
		return reader.readObject();
	}

	/**
	 * Marshals a JsonObject instance and returns a byte array.
	 *
	 * @param object a JsonObject instance
	 * @return a marshalled JSON message as a byte array
	 * @throws Exception if the marshalling cannot be done
	 */
	private byte[] marshal(JsonObject object) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(JsonGenerator.PRETTY_PRINTING, false);
		JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
		StringWriter stringWriter = new StringWriter();
		try (JsonWriter jsonWriter = writerFactory.createWriter(stringWriter)) {
			jsonWriter.writeObject(object);
		}
		return stringWriter.toString().getBytes();
	}

	/**
	 * Given a context, tries to retrieve the election definition from the board.
	 *
	 * @param context a context
	 * @return the election definition JsonObject instance
	 */
	private JsonObject getElectionDefinition(CreateVotingDataActionContext context) {
		try {
			ResultContainerDTO container = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForElectionDefinition(context.getSection()));
			ResultDTO result = container.getResult();
			if (result != null && result.getPost() != null && result.getPost().size() == 1) {
				return unmarshal(result.getPost().get(0).getMessage());
			} else {
				// TODO Provide tenant and section information in logger message.
				logger.log(Level.INFO, "Could not retrieve Election Definition from board.");
				return null;
			}
		} catch (UnivoteException ex) {
			// TODO Provide tenant and section information in logger message.
			logger.log(Level.INFO, "Could not reach board for retrieving Election Definition.");
			return null;
		}
	}

	/**
	 * Given a context, tries to retrieve the election details from the board.
	 *
	 * @param context a context
	 * @return the election details JsonObject instance
	 */
	private JsonObject getElectionDetails(CreateVotingDataActionContext context) {
		try {
			ResultContainerDTO container = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForElectionDetails(context.getSection()));
			ResultDTO result = container.getResult();
			if (result != null && result.getPost() != null && result.getPost().size() == 1) {
				return unmarshal(result.getPost().get(0).getMessage());
			} else {
				// TODO Provide tenant and section information in logger message.
				logger.log(Level.INFO, "Could not retrieve Election Details from board.");
				return null;
			}
		} catch (UnivoteException ex) {
			// TODO Provide tenant and section information in logger message.
			logger.log(Level.INFO, "Could not reach board for retrieving Election Details.");
			return null;
		}
	}

	/**
	 * Given a context, tries to retrieve the crypto setting from the board.
	 *
	 * @param context a context
	 * @return the crypto setting JsonObject instance
	 */
	private JsonObject getCryptoSetting(CreateVotingDataActionContext context) {
		try {
			ResultContainerDTO container = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForCryptoSetting(context.getSection()));
			ResultDTO result = container.getResult();
			if (result != null && result.getPost() != null && result.getPost().size() == 1) {
				return unmarshal(result.getPost().get(0).getMessage());
			} else {
				// TODO Provide tenant and section information in logger message.
				logger.log(Level.INFO, "Could not retrieve Crypto Setting from board.");
				return null;
			}
		} catch (UnivoteException ex) {
			// TODO Provide tenant and section information in logger message.
			logger.log(Level.INFO, "Could not reach board for retrieving Crypto Setting.");
			return null;
		}
	}

	/**
	 * Given a context, tries to retrieve the encryption key from the board.
	 *
	 * @param context a context
	 * @return the encryption key JsonObject instance
	 */
	private String getEncryptionKey(CreateVotingDataActionContext context) {
		try {
			ResultContainerDTO container = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForEncryptionKey(context.getSection()));
			ResultDTO result = container.getResult();
			if (result != null && result.getPost() != null && result.getPost().size() == 1) {
				EncryptionKey encryptionKey
						= JSONConverter.unmarshal(EncryptionKey.class, result.getPost().get(0).getMessage());
				return encryptionKey.getEncryptionKey();
			} else {
				// TODO Provide tenant and section information in logger message.
				logger.log(Level.INFO, "Could not retrieve Encryption Key from board.");
				return null;
			}
		} catch (UnivoteException ex) {
			// TODO Provide tenant and section information in logger message.
			logger.log(Level.INFO, "Could not reach board for retrieving Encryption Key.");
			return null;
		}
	}

	/**
	 * Given a context, tries to retrieve the signature generator from the board.
	 *
	 * @param context a context
	 * @return the signature generator JsonObject instance
	 */
	private String getSignatureGenerator(CreateVotingDataActionContext context) {
		try {
			ResultContainerDTO container = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForMixedKeys(context.getSection()));
			ResultDTO result = container.getResult();
			if (result != null && result.getPost() != null && result.getPost().size() == 1) {
				MixedKeys mixedKeys = JSONConverter.unmarshal(MixedKeys.class, result.getPost().get(0).getMessage());
				return mixedKeys.getGenerator();
			} else {
				// TODO Provide tenant and section information in logger message.
				logger.log(Level.INFO, "Could not retrieve Signature Generator from board.");
				return null;
			}
		} catch (UnivoteException ex) {
			// TODO Provide tenant and section information in logger message.
			logger.log(Level.INFO, "Could not reach board for retrieving Signature Generator.");
			return null;
		}
	}

	/**
	 * Adds a notification query for an Election Definition notification to the given context.
	 *
	 * @param context a context
	 */
	private void prepareForElectionDefinitionNotification(CreateVotingDataActionContext context) {
		BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
				QueryFactory.getQueryForElectionDefinition(context.getSection()), BoardsEnum.UNIVOTE.getValue());
		context.getPreconditionQueries().add(bQuery);
	}

	/**
	 * Adds a notification query for an Election Details notification to the given context.
	 *
	 * @param context a context
	 */
	private void prepareForElectionDetailsNotification(CreateVotingDataActionContext context) {
		BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
				QueryFactory.getQueryForElectionDetails(context.getSection()), BoardsEnum.UNIVOTE.getValue());
		context.getPreconditionQueries().add(bQuery);
	}

	/**
	 * Adds a notification query for a Crypto Setting notification to the given context.
	 *
	 * @param context a context
	 */
	private void prepareForCryptoSettingNotification(CreateVotingDataActionContext context) {
		BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
				QueryFactory.getQueryForCryptoSetting(context.getSection()), BoardsEnum.UNIVOTE.getValue());
		context.getPreconditionQueries().add(bQuery);
	}

	/**
	 * Adds a notification query for an Encryption Key notification to the given context.
	 *
	 * @param context a context
	 */
	private void prepareForEncryptionKeyNotification(CreateVotingDataActionContext context) {
		BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
				QueryFactory.getQueryForEncryptionKey(context.getSection()), BoardsEnum.UNIVOTE.getValue());
		context.getPreconditionQueries().add(bQuery);
	}

	/**
	 * Adds a notification query for a Signature Generator notification to the given context.
	 *
	 * @param context a context
	 */
	private void prepareForSignatureGeneratorNotification(CreateVotingDataActionContext context) {
		BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
				QueryFactory.getQueryForMixedKeys(context.getSection()), BoardsEnum.UNIVOTE.getValue());
		context.getPreconditionQueries().add(bQuery);
	}
}
