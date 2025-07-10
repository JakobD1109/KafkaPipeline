name := "simple-kafka-producer"

version := "0.1"

scalaVersion := "2.12.18"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "3.5.0",
  "org.apache.avro" % "avro" % "1.11.1"
)
