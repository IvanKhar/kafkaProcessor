package ru.otr.vtb.kafkaProcessor.service.rest;

import ru.otr.vtb.kafkaProcessor.model.FileEvent;

public interface RestService {

    void sendEvent(FileEvent fileEvent) throws Exception;
}
