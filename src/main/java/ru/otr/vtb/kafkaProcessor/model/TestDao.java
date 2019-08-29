package ru.otr.vtb.kafkaProcessor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

public class TestDao implements Serializable {

    private static final long serialVersionUID = -3561142838980678679L;

    private String name;

    private String eventType;

    private FileList fileList;

    public TestDao() {
    }

    public static TestDao newInstanceWithEmptyFileList(String catalogName, String eventType) {
        return new TestDao(catalogName, eventType, new FileList());
    }

    @JsonCreator
    public TestDao(@JsonProperty("name") String name,
                   @JsonProperty("eventType") String eventType,
                   @JsonProperty("fileList") FileList fileList) {
        this.name = name;
        this.eventType = eventType;
        this.fileList = fileList;
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

    public FileList getFileList() {
        if (fileList == null) {
            fileList = new FileList();
        }
        return fileList;
    }

    public void setFileList(FileList fileList) {
        this.fileList = fileList;
    }

    public TestDao addFile(File file) {
        this.getFileList().getFile().add(file);
        return this;
    }

    public TestDao fullFillMetaInfoByFileList(String directory, String eventType) {
        this.setName(directory);
        this.setEventType(eventType);
        this.getFileList().setSize(this.getFileList().getFile().size());
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestDao)) return false;
        TestDao testDao = (TestDao) o;
        return getName().equals(testDao.getName()) &&
                getEventType().equals(testDao.getEventType()) &&
                Objects.equals(getFileList(), testDao.getFileList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getEventType(), getFileList());
    }

    @Override
    public String toString() {
        return "TestDao{" +
                "name='" + name + '\'' +
                ", eventType='" + eventType + '\'' +
                ", fileList=" + fileList +
                '}';
    }
}
