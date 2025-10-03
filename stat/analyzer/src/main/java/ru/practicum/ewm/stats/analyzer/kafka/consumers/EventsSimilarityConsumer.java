package ru.practicum.ewm.stats.analyzer.kafka.consumers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.analyzer.service.EventsSimilarityHandler;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.serialization.avro.deserializers.EventSimilarityDeserializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventsSimilarityConsumer implements Runnable {
    private final EventsSimilarityHandler eventsSimilarityHandler;

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    @Value("${kafka.consumers.events-similarity.attempt-timeout-ms}")
    private int consumeAttemptTimeout;

    @Value("${kafka.consumers.events-similarity.commit-every-messages}")
    private int messagesQuantityToCommit;

    @Value("${kafka.consumers.events-similarity.name}")
    private String consumerName;

    @Value("${kafka.topics.events-similarity}")
    private String topic;

    @Value("${kafka.server}")
    private String kafkaServer;

    public void start() {
        KafkaConsumer<String, EventSimilarityAvro> consumer = getConsumer();

        try {
            consumer.subscribe(List.of(topic));
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            // Цикл обработки событий
            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer.poll(consumeAttemptTimeout);
                int count = 0;

                for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
                    // обрабатываем очередную запись
                    handleRecord(record);
                    // фиксируем оффсеты обработанных записей, если нужно
                    manageOffsets(record, count, consumer);
                    count++;
                }
                // фиксируем максимальный оффсет обработанных записей
                consumer.commitAsync();
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий пользователя.", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер.");
                consumer.close();
            }
        }
    }

    private KafkaConsumer<String, EventSimilarityAvro> getConsumer() {
        Properties config = new Properties();
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerName);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerName);
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventSimilarityDeserializer.class.getName());
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new KafkaConsumer<>(config);
    }

    private void manageOffsets(ConsumerRecord<String, EventSimilarityAvro> record,
                               int count,
                               KafkaConsumer<String, EventSimilarityAvro> consumer) {
        // обновляем текущий оффсет для топика-партиции
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % messagesQuantityToCommit == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if(exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}.", offsets, exception);
                }
            });
        }
    }

    private void handleRecord(ConsumerRecord<String, EventSimilarityAvro> record) {
        log.info("топик = {}, партиция = {}, смещение = {}, значение: {}\n",
                record.topic(), record.partition(), record.offset(), record.value());
        eventsSimilarityHandler.handle(record.value());
    }

    @Override
    public void run() {
        start();
    }
}
