package ru.otr.vtb.kafkaProcessor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

public class FileEvent implements Serializable {

    private static final long serialVersionUID = -3561142838980678679L;

    private String name;

    private String eventType;

    private FileList filelist;

    public FileEvent() {
    }

    public static FileEvent newInstanceWithEmptyFileList(String catalogName, String eventType) {
        return new FileEvent(catalogName, eventType, new FileList());
    }

    @JsonCreator
    public FileEvent(@JsonProperty("name") String name,
                     @JsonProperty("eventType") String eventType,
                     @JsonProperty("filelist") FileList filelist) {
        this.name = name;
        this.eventType = eventType;
        this.filelist = filelist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public FileList getFilelist() {
        if (filelist == null) {
            filelist = new FileList();
        }
        return filelist;
    }

    public void setFilelist(FileList filelist) {
        this.filelist = filelist;
    }

    public FileEvent addFile(File file) {
        this.getFilelist().getFile().add(file);
        return this;
    }

    public FileEvent fullFillMetaInfoByFileList(String directory, String eventType) {
        this.setName(directory);
        this.setEventType(eventType);
        this.getFilelist().setSize(this.getFilelist().getFile().size());
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileEvent)) return false;
        FileEvent fileEvent = (FileEvent) o;
        return getName().equals(fileEvent.getName()) &&
                getEventType().equals(fileEvent.getEventType()) &&
                Objects.equals(getFilelist(), fileEvent.getFilelist());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getEventType(), getFilelist());
    }

    @Override
    public String toString() {
        return "FileEvent{" +
                "name='" + name + '\'' +
                ", eventType='" + eventType + '\'' +
                ", filelist=" + filelist +
                '}';
    }
}
