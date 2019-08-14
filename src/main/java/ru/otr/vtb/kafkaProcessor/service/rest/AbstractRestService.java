package ru.otr.vtb.kafkaProcessor.service.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;


class AbstractRestService {
    final static ObjectMapper mapper = new ObjectMapper();
    final RestTemplate restTemplate;

    final Logger log = LoggerFactory.getLogger(getClass());

    AbstractRestService(RestTemplateBuilder restTemplateBuilder,
                        String rootUri) {
        restTemplateBuilder.rootUri(rootUri);
        this.restTemplate = restTemplateBuilder.build();
    }
}
