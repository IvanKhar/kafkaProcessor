package ru.otr.vtb.kafkaProcessor.service.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.otr.vtb.kafkaProcessor.model.TestDao;
import ru.otr.vtb.kafkaProcessor.model.enums.EventToDirectories;
import ru.otr.vtb.kafkaProcessor.service.rest.RestService;

import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.apache.kafka.streams.kstream.Suppressed.BufferConfig.maxRecords;

@Service
public class KafkaEventProcessor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    final private static Properties STREAM_PROPERTIES = new Properties();
    final private static Map<String, Object> EVENT_PRODUCER_PROPERTIES_MAP = new HashMap<>();

    private final KafkaTemplate<String, String> eventKafkaTemplate;

    private final String filesPath;
    private final String devTopic;

    private RestService restService;


    @Autowired
    KafkaEventProcessor(@Value("${spring.kafka.admin.properties.devFilePath}") String filesPath,
                        @Value("${spring.kafka.admin.properties.devTopic}") String devTopic,
                        @Value("${spring.kafka.admin.properties.windMinDuration}") String windMinDuration,
                        @Value("${spring.kafka.streams.application-id}") String applicationId,
                        @Value("${spring.kafka.bootstrap-servers}") String bootstrapServer,
                        @Value("${spring.kafka.consumer.auto-commit-interval}") String commitInterval,
                        RestService restService) {

        this.restService = restService;

        this.filesPath = filesPath;
        this.devTopic = devTopic;

        STREAM_PROPERTIES.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        STREAM_PROPERTIES.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        STREAM_PROPERTIES.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        STREAM_PROPERTIES.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        STREAM_PROPERTIES.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, commitInterval);

        EVENT_PRODUCER_PROPERTIES_MAP.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        EVENT_PRODUCER_PROPERTIES_MAP.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        EVENT_PRODUCER_PROPERTIES_MAP.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);


        ProducerFactory<String, String> eventProducerFactory = new DefaultKafkaProducerFactory<>(EVENT_PRODUCER_PROPERTIES_MAP);

        this.eventKafkaTemplate = new KafkaTemplate<>(eventProducerFactory);


        new Thread(this::monitorForFiles).start();

        StreamsBuilder builder = new StreamsBuilder();


        KStream<String,String> inputEventTopicStream = builder.stream(devTopic, Consumed.with(Serdes.String(), Serdes.String()));

        inputEventTopicStream
                .peek((k, v) -> System.out.println(String.join(" ","Stream got record", "time:", LocalDateTime.now().format(DateTimeFormatter.ISO_TIME))))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.of(Duration.ofMinutes(Long.parseLong(windMinDuration))).grace(Duration.ofSeconds(5)).advanceBy(Duration.ofMinutes(Long.parseLong(windMinDuration))))
                .aggregate(String::new, (aggKey, newValue, aggValue) -> aggValue + "," + newValue)
                .suppress(Suppressed.untilWindowCloses(maxRecords(70).withNoBound()))
                .toStream()
                .foreach((k, v) -> sendEvent(StringUtils.commaDelimitedListToSet(v)));


        Topology topology = builder.build();

        KafkaStreams streams = new KafkaStreams(topology, STREAM_PROPERTIES);

        streams.start();
    }

    private void sendEvent(Set<String> files) {
        try {
            String filelist = "";
            for (String file : files) filelist = filelist.concat(file) + "\n";
            System.out.println("sending records \n" + filelist);
            restService.sendEvent(new TestDao(files));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }


    private String getEventCode(String filePath) {

        return EnumSet.allOf(EventToDirectories.class).stream()
                .filter(en -> filePath.contains(en.getFilePath()))
                .findFirst()
                .map(EventToDirectories::getEventCode)
                .orElse("none");

    }

    private void monitorForFiles() {
        Path filePath = Paths.get(filesPath);
        WatchService watchService;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            filePath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        while (true) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            for (WatchEvent event : key.pollEvents()) {
                String path = event.context().toString();
                eventKafkaTemplate.send(devTopic, 0, getEventCode(path), path);
            }
            key.reset();
        }
    }
}