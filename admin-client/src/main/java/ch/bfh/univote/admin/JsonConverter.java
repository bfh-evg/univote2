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
package ch.bfh.univote.admin;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public class JsonConverter {

	public static <T> T unmarshal(Class<T> type, String json) throws Exception {
		JAXBContext jaxbContext = createJAXBContext(type);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		Reader reader = new StringReader(json);
		return unmarshaller.unmarshal(new StreamSource(reader), type).getValue();
	}

	public static String marshal(Object object) throws Exception {
		JAXBContext jaxbContext = createJAXBContext(object.getClass());
		Marshaller marshaller = jaxbContext.createMarshaller();
		StringWriter writer = new StringWriter();
		marshaller.marshal(object, writer);
		return writer.toString();
	}

	private static <T> JAXBContext createJAXBContext(Class<T> type) throws Exception {
		Map<String, Object> properties = new HashMap();
		properties.put("eclipselink.media-type", "application/json");
		properties.put("eclipselink.json.include-root", false);
		return JAXBContext.newInstance(new Class[]{type}, properties);
	}
}
