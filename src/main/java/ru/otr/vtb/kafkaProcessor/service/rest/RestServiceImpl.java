package ru.otr.vtb.kafkaProcessor.service.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.otr.vtb.kafkaProcessor.model.TestDao;

import java.net.URI;

@Service
public class RestServiceImpl extends AbstractRestService implements RestService {


    private final URI sendEventUri;

    @Autowired
    public RestServiceImpl(RestTemplateBuilder restTemplateBuilder,
                           @Value("${spring.data.api.rootSzpUri}") String rootUri,
                           @Value("${spring.data.api.sendEventUri}") String sendEventUri) {
        super(restTemplateBuilder, sendEventUri);
        this.sendEventUri = URI.create(rootUri + sendEventUri);
    }

    @Override
    public void sendEvent(TestDao message) throws Exception {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(message), headers);
            restTemplate.postForLocation(sendEventUri, request);
        } catch (RestClientException e) {
            log.error("Error while calling importFromQueueMessageFiles REST service", e);
            throw e;
        }
    }
}
