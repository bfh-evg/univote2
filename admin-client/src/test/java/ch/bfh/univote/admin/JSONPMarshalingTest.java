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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import org.junit.Test;

public class JSONPMarshalingTest {

	private static final String ENCRYPTION_KEY = "91119287387482810725299284597751730297358595357967339129245557319362300326317603349124577759531942586879523260357064133075657405043421423278179218133399049314945190759226487413938851037528971806505834896476263628153957882527151512135458357322989509505305049487723315898812780201104384863495612707468258957878";
	private static final String SIGNATURE_GENERATOR = "107109962631870964694631290572616741684259433534913193717696669627034744183712064532843948178840692685135901742106546031184882792684386296417476646866306748317314750581351545212887046296410227653636832554555991359342552427316273176036531855263497569544312481810013296540896767718156533429912241745106756662354";

	@Test
	public void test() throws Exception {
		JsonObject electionDefinition = unmarshal("json-examples/sub-2015/electionDefinition.json");
		JsonObject electionDetails = unmarshal("json-examples/sub-2015/electionDetails.json");
		JsonObject votingData = createVotingData(electionDefinition, electionDetails);
		String jsonString = marshal(votingData);
		System.out.println(jsonString);
	}

	private JsonObject unmarshal(String path) throws Exception {
		InputStream stream = new FileInputStream(path);
		JsonReader reader = Json.createReader(stream);
		return reader.readObject();
	}

	private String marshal(JsonObject object) throws Exception {
		Map<String, Object> properties = new HashMap();
		properties.put(JsonGenerator.PRETTY_PRINTING, true);
		JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
		StringWriter stringWriter = new StringWriter();
		try (JsonWriter jsonWriter = writerFactory.createWriter(stringWriter)) {
			jsonWriter.writeObject(object);
		}
		return stringWriter.toString();
	}

	private JsonObject createVotingData(JsonObject electionDefinition, JsonObject electionDetails) {
		JsonObject cryptoSetting = Json.createObjectBuilder()
				.add("encryptionSetting", "RC2")
				.add("signatureSetting", "RC2s")
				.add("hashSetting", "H2")
				.build();
		return Json.createObjectBuilder()
				.add("definiton", electionDefinition)
				.add("details", electionDetails)
				.add("cryptoSetting", cryptoSetting)
				.add("encryptionKey", ENCRYPTION_KEY)
				.add("signatureGenerator", SIGNATURE_GENERATOR)
				.build();
	}
}
