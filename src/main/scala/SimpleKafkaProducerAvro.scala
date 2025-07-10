import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.avro.specific.SpecificDatumWriter
import org.apache.avro.io.{BinaryEncoder, EncoderFactory}
import java.io.ByteArrayOutputStream

object SimpleKafkaProducerAvro {

  def main(args: Array[String]): Unit = {
    val topic = "demo_sub_topic_4"

    val schema: Schema = new Schema.Parser().parse(
      """
        |{
        |  "namespace": "student.avro",
        |  "type": "record",
        |  "name": "Student",
        |  "fields": [
        |    {"name": "name", "type": "string"},
        |    {"name": "age", "type": "int"}
        |  ]
        |}
        |""".stripMargin)

    val producer: KafkaProducer[String, Array[Byte]] = getKafkaProducer(topic)

    // Create and send 4 different messages
    val students = List(
      ("John Wick", 123),
      ("Alice Smith", 25),
      ("Bob Johnson", 30),
      ("Emma Wilson", 22)
    )

    students.zipWithIndex.foreach { case ((name, age), index) =>
      // Create a generic record for each student
      // index starts at 0, so:
      // index = 0 → s"key${index + 1}" = s"key${0 + 1}" = "key1"
      // index = 1 → s"key${index + 1}" = s"key${1 + 1}" = "key2"  
      // index = 2 → s"key${index + 1}" = s"key${2 + 1}" = "key3"
      // index = 3 → s"key${index + 1}" = s"key${3 + 1}" = "key4"
      val genericUser: GenericRecord = new GenericData.Record(schema)
      genericUser.put("name", name)
      genericUser.put("age", age)

      // Serialize the record into bytes
      val writer = new SpecificDatumWriter[GenericRecord](schema)
      val out = new ByteArrayOutputStream()
      val encoder: BinaryEncoder = EncoderFactory.get().binaryEncoder(out, null)

      writer.write(genericUser, encoder)
      encoder.flush()
      out.close()

      val serializedBytes: Array[Byte] = out.toByteArray

      // Send the message
      val record = new ProducerRecord[String, Array[Byte]](topic, s"key${index + 1}", serializedBytes)
      producer.send(record)
      3
      println(s"Sent message ${index + 1}: $name, age $age")
    } // <- This closing brace was missing

    // Cleanup
    producer.flush()
    producer.close()
    println("All 4 messages sent successfully!")
  }

  def getKafkaProducer(topic: String): KafkaProducer[String, Array[Byte]] = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer")
    props.put("partitioner.class", "CustomPartitioner")
    new KafkaProducer[String, Array[Byte]](props)
  }
}