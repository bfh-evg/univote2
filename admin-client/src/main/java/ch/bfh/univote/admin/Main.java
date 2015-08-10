package ch.bfh.univote.admin;

import ch.bfh.univote.admin.data.ElectionDetails;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public class Main {

	public static void main(String[] args) throws Exception {
		JAXBContext jaxbContext = initJAXBContext(ElectionDetails.class);
		ElectionDetails electionDetails = unmarshal(jaxbContext,
				ElectionDetails.class, "/json-examples/sub-2015/electionDetails.json");
		String jsonString = marshal(jaxbContext, electionDetails);
		System.out.println(jsonString);
	}

	private static <T> JAXBContext initJAXBContext(Class<T> type) throws Exception {
		Map<String, Object> properties = new HashMap();
		properties.put("eclipselink.media-type", "application/json");
		properties.put("eclipselink.json.include-root", false);
		return JAXBContext.newInstance(new Class[]{type}, properties);
	}

	private static <T> T unmarshal(JAXBContext jaxbContext, Class<T> type, String path) throws Exception {
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		InputStream stream = type.getResourceAsStream(path);
		return unmarshaller.unmarshal(new StreamSource(stream), type).getValue();
	}

	private static String marshal(JAXBContext jaxbContext, Object object) throws Exception {
		StringWriter writer = new StringWriter();
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(object, writer);
		return writer.toString();
	}
}
