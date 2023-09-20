mvn archetype:generate                \
  -DarchetypeGroupId=org.apache.flink   \
  -DarchetypeArtifactId=flink-quickstart-java \
  -DarchetypeVersion=1.16.0

mvn clean compile package

mvn exec:java -Dexec.mainClass=flink.SignalExcessJob


--input-topic xad.pocinv.panelevents.flink.input --output-topic xad.pocinv.panelevents.flink.output --bootstrap.servers broker:29092 --schema-registry-url http://schema-registry:8091 --threshold 2

--input-topic input --output-topic output --bootstrap.servers broker:29092 --schema-registry-url http://schema-registry:8091 --threshold 2


--input-topic xad.pocinv.panelevents.flink.input --output-topic xad.pocinv.panelevents.flink.output --bootstrap.servers ef1brkm2m01v.vsdepi.local:9093,ef1brkm2m02v.vsdepi.local:9093,ef1brkm2m03v.vsdepi.local:9093 --schema-registry-url https://ef1regm2m01v.vsdepi.local:8081 --ssl-validation --jaas.config.username client --jaas.config.password 9779a14066


datos de la máquina de test:
10.45.2.99
usuario: ssrr
pass: kkq9hoimn0yl

sudo su - alarmapp
cd /opt/SDSW/apache-jmeter-5.4.1/bin
Start Test:
sh flink-pocinv-start-test.sh
Stop Test:
sh /opt/SDSW/apache-jmeter-5.4.1/bin/stoptest.sh

RecordsOutPerSecond