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
package ch.bfh.univote2.component.core.helper;

import ch.bfh.uniboard.UniBoardService;
import ch.bfh.uniboard.UniBoardService_Service;
import ch.bfh.uniboard.data.AttributesDTO;
import ch.bfh.uniboard.data.AttributesDTO.AttributeDTO;
import ch.bfh.uniboard.data.ByteArrayValueDTO;
import ch.bfh.uniboard.data.DateValueDTO;
import ch.bfh.uniboard.data.IntegerValueDTO;
import ch.bfh.uniboard.data.QueryDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.StringValueDTO;
import ch.bfh.unicrypt.helper.Alphabet;
import ch.bfh.unicrypt.helper.array.classes.DenseArray;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.ByteArrayMonoid;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.Z;
import ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.univote2.component.core.data.Signer;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.manager.TenantManager;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Stateless
public class UniBoardHelperImpl implements UniboardHelper {

	private static final Logger logger = Logger.getLogger(UniBoardHelperImpl.class.getName());

	String endPointUrl = "";

	@EJB
	TenantManager tenantManager;

	@Override
	public ResultContainerDTO get(QueryDTO query) throws UnivoteException {
		try {
			UniBoardService uniboard = this.getUniBoardService();
			//TODO check signature
			return uniboard.get(query);
		} catch (Exception ex) {
			//TODO Differ exceptions and do log
			throw new UnivoteException("Could not get messages from the board.", ex);
		}

	}

	@Override
	public AttributesDTO post(String section, String group, byte[] message, String tenant)
			throws UnivoteException {
		try {
			Signer signer = this.tenantManager.getSigner(tenant);
			AttributesDTO alpha = new AttributesDTO();
			alpha.getAttribute().add(new AttributeDTO("section", new StringValueDTO(section)));
			alpha.getAttribute().add(new AttributeDTO("group", new StringValueDTO(group)));
			Element signature = signer.sign(this.createMessageElement(message, null));
			alpha.getAttribute().add(new AttributeDTO("signature",
					new StringValueDTO(signature.getBigInteger().toString(10))));
			alpha.getAttribute().add(new AttributeDTO("publickey",
					new StringValueDTO(signer.getPublicKey())));
			UniBoardService uniboard = this.getUniBoardService();
			//TODO check signature
			return uniboard.post(message, alpha);
		} catch (Exception ex) {
			//TODO Differ exceptions and do log
			throw new UnivoteException("Could not post message on the board", ex);
		}
	}

	protected UniBoardService getUniBoardService() throws Exception {
		URL wsdlLocation = new URL(endPointUrl);
		QName qname = new QName("http://uniboard.bfh.ch/", "UniBoardService");
		UniBoardService_Service uniboardService = new UniBoardService_Service(wsdlLocation, qname);
		UniBoardService uniboard = uniboardService.getUniBoardServicePort();
		BindingProvider bp = (BindingProvider) uniboard;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPointUrl);
		return uniboard;

	}

	protected Element createMessageElement(byte[] message, AttributesDTO alpha) {
		StringMonoid stringSpace = StringMonoid.getInstance(Alphabet.PRINTABLE_ASCII);
		Z z = Z.getInstance();
		ByteArrayMonoid byteSpace = ByteArrayMonoid.getInstance();

		Element messageElement = byteSpace.getElement(message);

		List<Element> alphaElements = new ArrayList<>();
		//itterate over alpha until one reaches the property = signature
		for (AttributeDTO attr : alpha.getAttribute()) {
			Element tmp;
			if (attr.getValue() instanceof ByteArrayValueDTO) {
				tmp = byteSpace.getElement(((ByteArrayValueDTO) attr.getValue()).getValue());
				alphaElements.add(tmp);
			} else if (attr.getValue() instanceof DateValueDTO) {
				TimeZone timeZone = TimeZone.getTimeZone("UTC");
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				dateFormat.setTimeZone(timeZone);
				String stringDate = dateFormat.format(((DateValueDTO) attr.getValue()).getValue());
				tmp = stringSpace.getElement(stringDate);
				alphaElements.add(tmp);
			} else if (attr.getValue() instanceof IntegerValueDTO) {
				tmp = z.getElement(((IntegerValueDTO) attr.getValue()).getValue());
				alphaElements.add(tmp);
			} else if (attr.getValue() instanceof StringValueDTO) {
				tmp = stringSpace.getElement(((StringValueDTO) attr.getValue()).getValue());
				alphaElements.add(tmp);
			} else {
				logger.log(Level.SEVERE, "Unsupported Value type.");
			}

		}
		DenseArray immuElements = DenseArray.getInstance(alphaElements);
		Element alphaElement = Tuple.getInstance(immuElements);
		return Pair.getInstance(messageElement, alphaElement);
	}

}
