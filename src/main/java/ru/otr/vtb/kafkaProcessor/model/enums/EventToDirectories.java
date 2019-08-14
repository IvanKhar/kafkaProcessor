package ru.otr.vtb.kafkaProcessor.model.enums;

public enum EventToDirectories {

    FIRST_EVENT("FIRST", "uo1/bea1"),

    SECOND_EVENT("SECOND", "uo1/bea2");

    EventToDirectories(String eventCode, String filePath){
        this.eventCode = eventCode;
        this.filePath = filePath;
    }

    public String eventCode;

    public String filePath;

    public String getEventCode() {
        return eventCode;
    }

    public String getFilePath() {
        return filePath;
    }
}
