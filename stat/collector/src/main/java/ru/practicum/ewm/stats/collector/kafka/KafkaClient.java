package ru.practicum.ewm.stats.collector.kafka;

import lombok.Getter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.serialization.avro.GeneralAvroSerializer;

import java.util.Properties;

@Component
public class KafkaClient {
    @Getter
    @Value("${kafka.topics.user-actions}")
    private String userActionsTopic;

    private final Producer<String, SpecificRecordBase> producer;

    @Autowired
    public KafkaClient(@Value("${kafka.server}") String kafkaServer) {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class.getName());
        producer = new KafkaProducer<>(config);
    }

    public void sendData(String key, SpecificRecordBase data, String topic) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, key, data);
        producer.send(record);
    }

    public void close() {
        producer.flush();
        producer.close();
    }
}
