import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties
import scala.io.StdIn

object SimpleKafkaProducer extends App {
  val props = new Properties()
  props.put("bootstrap.servers", "localhost:9092")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  val producer = new KafkaProducer[String, String](props)
  val topic = "test-topic"

  println("Type a message to send to Kafka (type 'exit' to quit):")

  var continue = true
  while (continue) {
    val line = StdIn.readLine("> ")
    if (line == "exit") continue = false
    else {
      val record = new ProducerRecord[String, String](topic, "key", line)
      producer.send(record)
      println(s"Sent: $line")
    }
  }

  producer.close()
}
