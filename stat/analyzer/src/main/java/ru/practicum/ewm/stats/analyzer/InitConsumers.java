package ru.practicum.ewm.stats.analyzer;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.analyzer.kafka.consumers.EventsSimilarityConsumer;
import ru.practicum.ewm.stats.analyzer.kafka.consumers.UserActionConsumer;

@Component
public class InitConsumers {
    public void start(ConfigurableApplicationContext context) {
        EventsSimilarityConsumer eventsSimilarityConsumer = context.getBean(EventsSimilarityConsumer.class);
        Thread eventsSimilarityThread = new Thread(eventsSimilarityConsumer);
        eventsSimilarityThread.setName("EventsSimilarityConsumer");
        eventsSimilarityThread.start();

        UserActionConsumer userActionConsumer = context.getBean(UserActionConsumer.class);
        userActionConsumer.start();
    }
}
