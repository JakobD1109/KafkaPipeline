import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object SimpleKafkaProducerStudent {

  def main(args: Array[String]): Unit = {
    val topic = "demo_sub_topic_1"
    val producer = getKafkaProducer(topic)

    val student: Student = new Student("Raj", 24)
    val studentString = student.toString // Convert Student to String
    val record = new ProducerRecord[String, String](topic, "key1", studentString)

    producer.send(record).get()
    println(s"Message sent: $studentString")
  }

  def getKafkaProducer(topic: String): KafkaProducer[String, String] = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    new KafkaProducer[String, String](props)
  }
}
