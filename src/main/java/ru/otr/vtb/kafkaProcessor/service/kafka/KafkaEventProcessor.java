package ru.otr.vtb.kafkaProcessor.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.*;
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
import ru.otr.vtb.kafkaProcessor.model.File;
import ru.otr.vtb.kafkaProcessor.model.TestDao;
import ru.otr.vtb.kafkaProcessor.model.enums.EventToDirectories;
import ru.otr.vtb.kafkaProcessor.service.rest.RestService;

import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.apache.kafka.streams.kstream.Suppressed.BufferConfig.maxRecords;

@Service
public class KafkaEventProcessor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    final private static Properties FILE_EVENT_STREAM_PROPERTIES = new Properties();
    final private static Map<String, Object> EVENT_PRODUCER_PROPERTIES_MAP = new HashMap<>();

    final private static Map<String, Object> TESTEVENT_PRODUCER_PROPERTIES_MAP = new HashMap<>();

    private final KafkaTemplate<String, File> eventKafkaTemplate;

    private final String filesPath;
    private final String devTopic;

    private RestService restService;


    @Autowired
    KafkaEventProcessor(@Value("${spring.kafka.admin.properties.devFilePath}") String filesPath,
                        @Value("${spring.kafka.admin.properties.devTopic}") String devTopic,
                        @Value("${spring.kafka.admin.properties.devOutTopic}") String devOutTopic,
                        @Value("${spring.kafka.admin.properties.windMinDuration}") String windMinDuration,
                        @Value("${spring.kafka.streams.application-id}") String applicationId,
                        @Value("${spring.kafka.bootstrap-servers}") String bootstrapServer,
                        @Value("${spring.kafka.consumer.auto-commit-interval}") String commitInterval,
                        RestService restService) {

        this.restService = restService;

        this.filesPath = filesPath;
        this.devTopic = devTopic;

        FILE_EVENT_STREAM_PROPERTIES.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        FILE_EVENT_STREAM_PROPERTIES.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        FILE_EVENT_STREAM_PROPERTIES.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, commitInterval);

        EVENT_PRODUCER_PROPERTIES_MAP.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        EVENT_PRODUCER_PROPERTIES_MAP.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        EVENT_PRODUCER_PROPERTIES_MAP.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, new JsonPOJOSerializer<File>().getClass());

        new ObjectMapper();

        ProducerFactory<String, File> eventProducerFactory = new DefaultKafkaProducerFactory<>(EVENT_PRODUCER_PROPERTIES_MAP);

        this.eventKafkaTemplate = new KafkaTemplate<>(eventProducerFactory);

        new Thread(this::monitorForFiles).start();

        Map<String, Object> fileSerdeProps = new HashMap<>();

        final Serializer<File> fileSerializer = new JsonPOJOSerializer<>();
        fileSerdeProps.put("JsonPOJOClass", File.class);
        fileSerializer.configure(fileSerdeProps, false);

        final Deserializer<File> fileDeserializer = new JsonPOJODeserializer<>();
        fileSerdeProps.put("JsonPOJOClass", File.class);
        fileDeserializer.configure(fileSerdeProps, false);

        final Serde<File> fileSerde = Serdes.serdeFrom(fileSerializer, fileDeserializer);

        Map<String, Object> testDaoSerdeProps = new HashMap<>();

        final Serializer<TestDao> testDaoSerializer = new JsonPOJOSerializer<>();
        testDaoSerdeProps.put("JsonPOJOClass", TestDao.class);
        testDaoSerializer.configure(testDaoSerdeProps, false);

        final Deserializer<TestDao> testDaoDeserializer = new JsonPOJODeserializer<>();
        testDaoSerdeProps.put("JsonPOJOClass", TestDao.class);
        testDaoDeserializer.configure(testDaoSerdeProps, false);

        final Serde<TestDao> testDaoSerde = Serdes.serdeFrom(testDaoSerializer, testDaoDeserializer);

        StreamsBuilder fileEventStreamBuilder = new StreamsBuilder();

        KStream<String, File> inputEventTopicStream = fileEventStreamBuilder.stream(devTopic, Consumed.with(Serdes.String(), fileSerde));
        inputEventTopicStream
                .peek((fileDir, file) -> System.out.println(String.join(" ", "Stream got record", "time:", LocalDateTime.now().format(DateTimeFormatter.ISO_TIME))))
                .groupByKey(Grouped.with(Serdes.String(), fileSerde))
                .windowedBy(TimeWindows.of(Duration.ofMinutes(Long.parseLong(windMinDuration))).grace(Duration.ofSeconds(5)).advanceBy(Duration.ofMinutes(Long.parseLong(windMinDuration))))
                .aggregate(TestDao::new, (aggKey, newValue, aggValue) -> aggValue.addFile(newValue), Materialized.with(Serdes.String(), testDaoSerde))
                .suppress(Suppressed.untilWindowCloses(maxRecords(70).withNoBound()))
                .toStream()
                .map((key, value) -> KeyValue.pair(key.key(), value.fullFillMetaInfoByFileList(key.key(), getEventCode(key.key()))))
                .to(devOutTopic, Produced.with(Serdes.String(), testDaoSerde));

        KStream<String, TestDao> outputEventTopicStream = fileEventStreamBuilder.stream(devOutTopic, Consumed.with(Serdes.String(), testDaoSerde));
        outputEventTopicStream.foreach((key, value) -> sendEvent(value));


        Topology inputTopology = fileEventStreamBuilder.build();

        KafkaStreams eventStreams = new KafkaStreams(inputTopology, FILE_EVENT_STREAM_PROPERTIES);

        eventStreams.start();
    }

    private void sendEvent(TestDao files) {
        try {
            System.out.println("Sending new record" + files.toString());
            restService.sendEvent(files);
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
                Path path = Paths.get(filesPath).resolve(event.context().toString());
                System.out.println(path);
                try {
                    eventKafkaTemplate.send(
                            devTopic,
                            0,
                            filesPath,
                            new File(
                                    filesPath,
                                    path.getFileName().toString(),
                                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                                    Files.size(path)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            key.reset();
        }
    }
}