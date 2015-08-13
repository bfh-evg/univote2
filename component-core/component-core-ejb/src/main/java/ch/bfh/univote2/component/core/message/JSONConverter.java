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
package ch.bfh.univote2.component.core.message;

import ch.bfh.univote2.component.core.UnivoteException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.transform.stream.StreamSource;

/**
 * Utility class that allows to convert JSON byte[] messages to objects of domain classes
 * as well as to convert these instances to JSON message strings.
 * <p>
 * An instance of this class supports support the type query predicate {@link #isOfType(java.lang.Class, byte[])}.
 * If called with a type and a message, <code>true</code> is returned if the message matches the specified type,
 * <code>false</code> otherwise. On success, the domain object for the given message is cachen and can be retrieved
 * by calling {@link #getUnmarshalledMessage()}.
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
public class JSONConverter {

	/**
	 * Initilizes the JAXB context.
	 *
	 * @param <T> the Java type of the domain class the converstion takes place
	 * @param type the actual type object
	 * @return the JAXB context
	 * @throws Exception if the context cannot be established
	 */
	private static <T> JAXBContext initJAXBContext(Class<T> type) throws Exception {
		Map<String, Object> properties = new HashMap<>();
		properties.put("eclipselink.media-type", "application/json");
		properties.put("eclipselink.json.include-root", false);
		return JAXBContext.newInstance(new Class<?>[]{type}, properties);
	}

	/**
	 * Converst a JSON 'ResultDTO' byte array into the corresponding domain class.
	 *
	 * @param <T> the Java type of the domain class the conversion takes place
	 * @param type the actual type object
	 * @param message a JSON 'ResultDTO' byte array
	 * @return the Java instance of the domain class
	 * @throws UnivoteException if the conversion cannot be made
	 */
	public static <T> T unmarshal(Class<T> type, byte[] message) throws UnivoteException {
		try {
			JAXBContext jaxbContext = JSONConverter.initJAXBContext(type);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			InputStream stream = new ByteArrayInputStream(message);
			return unmarshaller.unmarshal(new StreamSource(stream), type).getValue();
		} catch (Exception ex) {
			throw new UnivoteException(ex.getMessage(), ex);
		}
	}

	/**
	 * Given an instance of a domain class denoting a JSON object, converts it
	 * into a JSON string.
	 * @param object an instance of a domain class denoting a JSON object
	 * @return a JSON string
	 * @throws UnivoteException if there is an error
	 */
	public static String marshal(Object object) throws UnivoteException {
		try {
			JAXBContext jaxbContext = JSONConverter.initJAXBContext(object.getClass());
			StringWriter writer = new StringWriter();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
			marshaller.marshal(object, writer);
			return writer.toString();
		} catch (Exception ex) {
			throw new UnivoteException(ex.getMessage(), ex);
		}
	}

	/**
	 * Used as a cache to hold last unmarshalled instance. Not thread-safe.
	 */
	private Object domainObject;

	/**
	 * Determines if a given JSON message is of given domain type by trying to parse it. If it succeeds then the
	 * parsed message is stored in the local cache and true is returned. If it does not succed then the cache
	 * is cleared and false is returned.
	 * @param <T> the expected type
	 * @param type the class object
	 * @param message the message
	 * @return true if message is of expected type, false otherwise
	 */
	public <T> boolean isOfType(Class<T> type, byte[] message) {
		try {
			domainObject = unmarshal(type, message);
			if (notEmptyGetters(type)) {
				return true;
			} else {
				domainObject = null;
				return false;
			}
		} catch (UnivoteException ex) { // ex expected, not used
			domainObject = null;
			return false;
		}
	}

	/**
	 * Returns the message sucessfully given to a prior {@link #isOfType(java.lang.Class, byte[])} call, or
	 * <code>null</code> otherwise.
	 * @param <T> the tyoe of the returned domain object.
	 * @return the domain object
	 */
	@SuppressWarnings("unchecked")
	public <T> T getUnmarshalledMessage() {
		return (T) domainObject;
	}

	/**
	 * Checks if the getters of the domain object are not null. If at least one
	 * required getter returns null then assume that the message does not match the
	 * given type and, therefore, this method returns false.
	 * @param <T> the type of the given type
	 * @param type the given type
	 * @return true if all required getters return non-null values
	 */
	private <T> boolean notEmptyGetters(Class<T> type) {
		Method[] getters = type.getDeclaredMethods();
		for (Method m : getters) {
			// We do not dig into the depth of the XmlElement annotation instance in order
			// to figure out whether the 'required' value is set to true. Its presence
			// suffices to check and test that the getter's return value is not null.
			if (m.getName().startsWith("get") && m.getAnnotation(XmlElement.class)!= null) {
				try {
					Object rval = m.invoke(domainObject, (Object[]) null);
					if (rval == null) {
						return false;
					}
				}catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
					// left intentionally empty -- iterate over all remaining fields
				}
			}
		}
		return true;
	}
}
