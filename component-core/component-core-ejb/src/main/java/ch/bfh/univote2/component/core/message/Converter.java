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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class Converter {

	/**
	 * Initilizes the JAXB context.
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
	 * @param <T> the Java type of the domain class the converstion takes place
	 * @param type the actual type object
	 * @param message a JSON 'ResultDTO' byte array
	 * @return the Java instance of the domain class
	 * @throws Exception if the conversion cannot be made
	 */
	public static <T> T unmarshal(Class<T> type, byte[] message) throws Exception {
		JAXBContext jaxbContext = Converter.initJAXBContext(type);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		InputStream stream = new ByteArrayInputStream(message);
		return unmarshaller.unmarshal(new StreamSource(stream), type).getValue();
	}

	public static String marshal(Object object) throws Exception {
		JAXBContext jaxbContext = Converter.initJAXBContext(object.getClass());
		StringWriter writer = new StringWriter();
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(object, writer);
		return writer.toString();
	}
}
