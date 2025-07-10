import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.{Collections, Properties}
import scala.collection.JavaConverters._
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericDatumReader, GenericRecord}
import org.apache.avro.io.{DecoderFactory}
import java.io.ByteArrayInputStream

object SimpleKafkaConsumer2 extends App {
  val props = new Properties()
  props.put("bootstrap.servers", "localhost:9092")
  props.put("group.id", "load-balance-group")
  props.put("client.id", "consumer-2")
  props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer")
  props.put("group.id", "group-2")
  props.put("enable.auto.commit", "False")

  val consumer = new KafkaConsumer[String, Array[Byte]](props)
  consumer.subscribe(Collections.singletonList("demo_sub_topic_4"))

  // Define the Avro schema
  val schemaString = """
  {
    "namespace": "student.avro",
    "type": "record",
    "name": "User",
    "fields": [
      {"name": "name", "type": "string"},
      {"name": "age", "type": "int"}
    ]
  }
  """
  
  val schema = new Schema.Parser().parse(schemaString)
  val datumReader = new GenericDatumReader[GenericRecord](schema)

  println("Consumer 2 waiting for messages...")

  while (true) {
    val records = consumer.poll(java.time.Duration.ofMillis(1000))
    for (record <- records.asScala) {
      try {
        val bytes = record.value()
        
        // Deserialize Avro bytes back to GenericRecord
        val inputStream = new ByteArrayInputStream(bytes)
        val decoder = DecoderFactory.get().binaryDecoder(inputStream, null)
        val user = datumReader.read(null, decoder)
        
        // Extract human-readable values
        val name = user.get("name").toString
        val age = user.get("age").toString
        
        println(s"Consumer 2 - Received Student: name=$name, age=$age from partition ${record.partition()}")
        
      } catch {
        case e: Exception =>
          println(s"Consumer 2 - Error: ${e.getMessage}")
          println(s"Consumer 2 - Raw bytes received: ${record.value().length} bytes from partition ${record.partition()}")
      }
    }
  }
}