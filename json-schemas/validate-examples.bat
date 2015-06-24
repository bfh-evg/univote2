@echo off

java -jar json-schema-validator-2.2.6-lib.jar server/electionDefinitionSchema.json examples/sub-2015/electionDefinition.json
java -jar json-schema-validator-2.2.6-lib.jar server/trusteesSchema.json examples/sub-2015/trustees.json
java -jar json-schema-validator-2.2.6-lib.jar server/securityLevelSchema.json examples/sub-2015/securityLevel.json
java -jar json-schema-validator-2.2.6-lib.jar server/electionIssuesSchema.json examples/sub-2015/electionIssues.json
java -jar json-schema-validator-2.2.6-lib.jar server/electionDataSchema.json examples/sub-2015/electionData.json

java -jar json-schema-validator-2.2.6-lib.jar server/electionDefinitionSchema.json examples/ch-2015-06-14/electionDefinition.json
java -jar json-schema-validator-2.2.6-lib.jar server/trusteesSchema.json examples/ch-2015-06-14/trustees.json
java -jar json-schema-validator-2.2.6-lib.jar server/securityLevelSchema.json examples/ch-2015-06-14/securityLevel.json
java -jar json-schema-validator-2.2.6-lib.jar server/electionIssuesSchema.json examples/ch-2015-06-14/electionIssues.json
java -jar json-schema-validator-2.2.6-lib.jar server/electionDataSchema.json examples/ch-2015-06-14/electionData.json

java -jar json-schema-validator-2.2.6-lib.jar client/electionDefinitionSchema.json examples/sub-2015/electionDefinition.json
java -jar json-schema-validator-2.2.6-lib.jar client/trusteesSchema.json examples/sub-2015/trustees.json
java -jar json-schema-validator-2.2.6-lib.jar client/securityLevelSchema.json examples/sub-2015/securityLevel.json
java -jar json-schema-validator-2.2.6-lib.jar client/electionIssuesSchema.json examples/sub-2015/electionIssues.json
java -jar json-schema-validator-2.2.6-lib.jar client/electionDataSchema.json examples/sub-2015/electionData.json

java -jar json-schema-validator-2.2.6-lib.jar client/electionDefinitionSchema.json examples/ch-2015-06-14/electionDefinition.json
java -jar json-schema-validator-2.2.6-lib.jar client/trusteesSchema.json examples/ch-2015-06-14/trustees.json
java -jar json-schema-validator-2.2.6-lib.jar client/securityLevelSchema.json examples/ch-2015-06-14/securityLevel.json
java -jar json-schema-validator-2.2.6-lib.jar client/electionIssuesSchema.json examples/ch-2015-06-14/electionIssues.json
java -jar json-schema-validator-2.2.6-lib.jar client/electionDataSchema.json examples/ch-2015-06-14/electionData.json
