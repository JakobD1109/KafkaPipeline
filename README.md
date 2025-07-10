# Kafka Scala Avro Producer-Consumer Demo

A real-time data streaming application built with Apache Kafka, Scala, and Avro serialization. This project demonstrates producer-consumer patterns, custom partitioning, load balancing, and failover mechanisms.

## 🚀 Features

- **Avro Serialization**: Schema-based data serialization 
- **Custom Partitioner**: Message distribution across partitions
- **Load Balancing**: Multiple consumers processing messages in parallel
- **Failover Support**: Backup consumer for high availability
- **Real-time Processing**: Continuous message streaming and processing

## 🔧 Prerequisites

- **Java**: JDK 11 or higher
- **Scala**: 2.12.18
- **SBT**: 1.x
- **Apache Kafka**: 2.12-3.5.0
- **Apache Zookeeper**: (included with Kafka)

## ⚙️ Setup & Installation

### Start Kafka Infrastructure

```bash
# Start Zookeeper (Terminal 1)
cd C:\kafka_2.12-3.5.0
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties

# Start Kafka Server (Terminal 2)
cd C:\kafka_2.12-3.5.0
.\bin\windows\kafka-server-start.bat .\config\server.properties

# Create topic with 2 partitions
.\bin\windows\kafka-topics.bat --create --topic demo_sub_topic_4 --bootstrap-server localhost:9092 --partitions 2 --replication-factor 1

# Terminal 3
cd kafka-producer
sbt run
# Select: SimpleKafkaProducerAvro

### Running Consumers
### New Terminal
**Terminal 4 - Primary Consumer (Partition 0):**
```bash
sbt run
# Select: SimpleKafkaConsumer
```

**Terminal 2 - Secondary Consumer (Partition 1):**
```bash
sbt run
# Select: SimpleKafkaConsumer2
```

**Terminal 3 - Backup Consumer (Both Partitions):**
```bash
sbt run
# Select: SimpleKafkaConsumerBackup
```

## 📊 Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────────┐
│  Kafka Producer │───▶│   Kafka Topic    │───▶│   Consumer Group    │
│  (Avro Data)    │    │ demo_sub_topic_4 │    │ "load-balance-group"│
│                 │    │                  │    │                     │
│ CustomPartitioner│    │ ┌─────────────┐  │    │ ┌─────────────────┐ │
│      ↓          │    │ │ Partition 0 │  │    │ │   Consumer 1    │ │
│ key1→Partition 0│    │ │ key1, key3  │  │    │ │  (Partition 0)  │ │
│ key2→Partition 1│    │ └─────────────┘  │    │ └─────────────────┘ │
│ key3→Partition 0│    │ ┌─────────────┐  │    │ ┌─────────────────┐ │
│ key4→Partition 1│    │ │ Partition 1 │  │    │ │   Consumer 2    │ │
└─────────────────┘    │ │ key2, key4  │  │    │ │  (Partition 1)  │ │
                       │ └─────────────┘  │    │ └─────────────────┘ │
                       └──────────────────┘    └─────────────────────┘
                                                         │
                                               ┌─────────────────────┐
                                               │   Backup Consumer   │
                                               │ "backup-group"      │
                                               │ (Failover Support)  │
                                               └─────────────────────┘
```

## 🔄 Data Flow

1. **Producer** creates Student objects with Avro schema
2. **CustomPartitioner** routes messages:
   - `key1, key3` → Partition 0
   - `key2, key4` → Partition 1
3. **Consumer Group** load balances:
   - Consumer 1 processes Partition 0
   - Consumer 2 processes Partition 1
4. **Backup Consumer** monitors and activates on failure

## 🛠️ Configuration

### Producer Settings
- **Serializers**: String key, ByteArray value
- **Partitioner**: CustomPartitioner for balanced distribution
- **Topic**: demo_sub_topic_4

### Consumer Settings
- **Deserializers**: String key, ByteArray value
- **Group ID**: load-balance-group (for load balancing)
- **Auto Offset Reset**: earliest
- **Enable Auto Commit**: true

## 🌟 Future Enhancements

- [ ] Add real-time API data ingestion
- [ ] Implement Python consumer for data processing
- [ ] Add Kafka Streams for event processing
- [ ] Integrate with monitoring tools (JMX, Prometheus)

## 📞 Contact

Jakob Drews - jakobdrews97@gmail.com

Project Link: [https://github.com/JakobD1109/KafkaPipeline]
