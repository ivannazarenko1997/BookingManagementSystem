package com.example.bookstore.config;

import com.example.bookstore.kafka.event.BookEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.config.ConfigException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KafkaConfigTest {

    private final ApplicationContextRunner okRunner = new ApplicationContextRunner()
            .withUserConfiguration(KafkaConfig.class)
            .withPropertyValues(
                    "spring.kafka.bootstrap-servers=localhost:9092",
                    "spring.kafka.consumer.group-id=bookstore-test",
                    "spring.kafka.consumer.auto-offset-reset=earliest",
                    "app.kafka.topics.book.events=book.events.test"
            );

    @Test
    void beansAreCreated() {
        okRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(ProducerFactory.class);
            assertThat(ctx).hasSingleBean(KafkaTemplate.class);
            assertThat(ctx).hasSingleBean(ConsumerFactory.class);
            assertThat(ctx).hasSingleBean(NewTopic.class);
            @SuppressWarnings("unchecked")
            ProducerFactory<String, BookEvent> pf = (ProducerFactory<String, BookEvent>) ctx.getBean(ProducerFactory.class);
            KafkaTemplate<String, BookEvent> kt = (KafkaTemplate<String, BookEvent>) ctx.getBean(KafkaTemplate.class);
            @SuppressWarnings("unchecked")
            ConsumerFactory<String, BookEvent> cf = (ConsumerFactory<String, BookEvent>) ctx.getBean(ConsumerFactory.class);
            NewTopic topic = ctx.getBean(NewTopic.class);
            assertThat(pf).isNotNull();
            assertThat(kt).isNotNull();
            assertThat(cf).isNotNull();
            assertThat(topic.name()).isEqualTo("book.events.test");
        });
    }

    @Test
    void producerFactoryHasCorrectConfigs() {
        okRunner.run(ctx -> {
            ProducerFactory<?, ?> pf = ctx.getBean(ProducerFactory.class);
            var cfg = pf.getConfigurationProperties();
            assertThat(cfg.get("bootstrap.servers")).isEqualTo("localhost:9092");
            assertThat(String.valueOf(cfg.get("key.serializer"))).contains("StringSerializer");
            assertThat(String.valueOf(cfg.get("value.serializer"))).contains("JsonSerializer");
        });
    }

    @Test
    void consumerFactoryHasCorrectConfigs() {
        okRunner.run(ctx -> {
            ConsumerFactory<?, ?> cf = ctx.getBean(ConsumerFactory.class);
            var cfg = cf.getConfigurationProperties();
            assertThat(cfg.get("bootstrap.servers")).isEqualTo("localhost:9092");
            assertThat(cfg.get("group.id")).isEqualTo("bookstore-test");
            assertThat(cfg.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)).isEqualTo("earliest");
        });
    }


    @Test
    void failsWithoutBootstrapServers_onSend() {
        new ApplicationContextRunner()
                .withUserConfiguration(KafkaConfig.class)
                .withPropertyValues(
                        "spring.kafka.consumer.group-id=bookstore-test",
                        "spring.kafka.consumer.auto-offset-reset=earliest",
                        "app.kafka.topics.book.events=book.events.test"
                )
                .run(ctx -> {
                    KafkaTemplate<String, BookEvent> kt = ctx.getBean(KafkaTemplate.class);
                    BookEvent evt = new BookEvent(); // assuming no-args ctor
                    assertThatThrownBy(() -> kt.send("book.events.test", "k1", evt).get(2, SECONDS))
                            .isInstanceOf(KafkaException.class)
                            .hasCauseInstanceOf(ConfigException.class);
                });
    }

}
