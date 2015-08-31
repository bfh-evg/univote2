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

import ch.bfh.univote.admin.data.ElectionDetails;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.junit.Test;

public class JAXBMarshalingTest {

	private static final String DOCUMENT_PATH = "json-examples/sub-2015/electionDetails.json";

	@Test
	public void test() throws Exception {
		JAXBContext jaxbContext = initJAXBContext(ElectionDetails.class);
		ElectionDetails electionDetails = unmarshal(jaxbContext, ElectionDetails.class, DOCUMENT_PATH);
		String jsonString = marshal(jaxbContext, electionDetails);
		System.out.println(jsonString);
	}

	private <T> JAXBContext initJAXBContext(Class<T> type) throws Exception {
		Map<String, Object> properties = new HashMap();
		properties.put("eclipselink.media-type", "application/json");
		properties.put("eclipselink.json.include-root", false);
		return JAXBContext.newInstance(new Class[]{type}, properties);
	}

	private <T> T unmarshal(JAXBContext jaxbContext, Class<T> type, String path) throws Exception {
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		Reader reader = new InputStreamReader(new FileInputStream(path), "UTF-8");
		return unmarshaller.unmarshal(new StreamSource(reader), type).getValue();
	}

	private String marshal(JAXBContext jaxbContext, Object object) throws Exception {
		StringWriter writer = new StringWriter();
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(object, writer);
		return writer.toString();
	}
}
