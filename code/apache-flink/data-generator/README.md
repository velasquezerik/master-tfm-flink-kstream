mvn archetype:generate                \
  -DarchetypeGroupId=org.apache.flink   \
  -DarchetypeArtifactId=flink-quickstart-java \
  -DarchetypeVersion=1.16.0

java -classpath target/original-test-0.1.jar flink.EventGenerator

mvn clean compile package

mvn exec:java -Dexec.mainClass=flink.EventGenerator

mvn exec:java -Dexec.mainClass=flink.EventGenerator -Dexec.arguments="--topic input --bootstrap.servers broker:29092 --schema.registry http://schema-registry:8091"