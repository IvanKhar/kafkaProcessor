package ru.otr.vtb.kafkaProcessor.service.rest;

import ru.otr.vtb.kafkaProcessor.model.TestDao;

public interface RestService {

    void sendEvent(TestDao message) throws Exception;
}
