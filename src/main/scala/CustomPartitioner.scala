import org.apache.kafka.clients.producer.Partitioner
import org.apache.kafka.common.Cluster
import java.util

class CustomPartitioner extends Partitioner {
  def partition(topic: String, key: Any, keyBytes: Array[Byte], 
                        value: Any, valueBytes: Array[Byte], cluster: Cluster): Int = {
    val partitions = cluster.partitionCountForTopic(topic)
    
    // Extract number from key (e.g., "key1" -> 1)
    val keyStr = key.toString
    val keyNumber = keyStr.replace("key", "").toInt
    
    // Distribute evenly: key1->partition0, key2->partition1, key3->partition0, key4->partition1
    (keyNumber - 1) % partitions
  }

  def close(): Unit = {}
  def configure(configs: util.Map[String, _]): Unit = {}
}
