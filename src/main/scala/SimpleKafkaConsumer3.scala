import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.{Collections, Properties}
import scala.collection.JavaConverters._
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericDatumReader, GenericRecord}
import org.apache.avro.io.{DecoderFactory}
import java.io.ByteArrayInputStream
import java.util.concurrent.atomic.AtomicLong

object SimpleKafkaConsumerBackup extends App {
  val props = new Properties()
  props.put("bootstrap.servers", "localhost:9092")
  props.put("group.id", "backup-consumer-group")  // Different group = gets ALL messages
  props.put("client.id", "backup-consumer")
  props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer")
  props.put("enable.auto.commit", "False")
  props.put("auto.offset.reset", "latest")  // Only new messages

  val consumer = new KafkaConsumer[String, Array[Byte]](props)
  consumer.subscribe(Collections.singletonList("demo_sub_topic_4"))

  // Health monitoring
  val partition0LastSeen = new AtomicLong(System.currentTimeMillis())
  val partition1LastSeen = new AtomicLong(System.currentTimeMillis())
  val TIMEOUT_MS = 15000L  // 15 seconds timeout

  val schemaString = """
  {
    "namespace": "student.avro",
    "type": "record",
    "name": "Student",
    "fields": [
      {"name": "name", "type": "string"},
      {"name": "age", "type": "int"}
    ]
  }
  """
  
  val schema = new Schema.Parser().parse(schemaString)
  val datumReader = new GenericDatumReader[GenericRecord](schema)

  println("🛡️ Backup Consumer waiting for primary consumer failures...")

  while (true) {
    val records = consumer.poll(java.time.Duration.ofMillis(1000))
    val currentTime = System.currentTimeMillis()

    // Check if primary consumers are healthy
    val partition0Healthy = (currentTime - partition0LastSeen.get()) < TIMEOUT_MS
    val partition1Healthy = (currentTime - partition1LastSeen.get()) < TIMEOUT_MS

    for (record <- records.asScala) {
      val partition = record.partition()
      
      // Update last seen times (primary consumers are working)
      if (partition == 0) partition0LastSeen.set(currentTime)
      if (partition == 1) partition1LastSeen.set(currentTime)

      // Only process if corresponding primary consumer has failed
      val shouldProcess = (partition == 0 && !partition0Healthy) || 
                         (partition == 1 && !partition1Healthy)

      if (shouldProcess) {
        try {
          val bytes = record.value()
          val inputStream = new ByteArrayInputStream(bytes)
          val decoder = DecoderFactory.get().binaryDecoder(inputStream, null)
          val user = datumReader.read(null, decoder)
          
          val name = user.get("name").toString
          val age = user.get("age").toString
          
          println(s"BACKUP ACTIVATED - Processing: name=$name, age=$age from partition $partition")
          
        } catch {
          case e: Exception =>
            println(s"Backup Consumer - Error: ${e.getMessage}")
        }
      } else {
        // Primary consumer is healthy, just monitor
        println(s"Primary consumer for partition $partition is healthy - message skipped")
      }
    }

    // Log health status periodically
    if (currentTime % 10000 < 1000) {  // Every ~10 seconds
      println(s"Health Status - P0: ${if (partition0Healthy) "✅" else "❌"}, P1: ${if (partition1Healthy) "✅" else "❌"}")
    }
  }
}