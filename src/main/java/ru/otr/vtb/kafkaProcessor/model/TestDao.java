package ru.otr.vtb.kafkaProcessor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

public class TestDao implements Serializable {

    private static final long serialVersionUID = -3561142838980678679L;

    private String catalogName;

    private String eventType;

    private FileList fileList;

    public TestDao() {
    }

    public static TestDao newInstanceWithEmptyFileList(String catalogName, String eventType) {
        return new TestDao(catalogName, eventType, new FileList());
    }

    @JsonCreator
    public TestDao(@JsonProperty("catalogName") String catalogName,
                   @JsonProperty("eventType") String eventType,
                   @JsonProperty("fileList") FileList fileList) {
        this.catalogName = catalogName;
        this.eventType = eventType;
        this.fileList = fileList;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public FileList getFileList() {
        if (fileList == null) {
            return new FileList();
        }
        return fileList;
    }

    public void setFileList(FileList fileList) {
        this.fileList = fileList;
    }

    public TestDao addFile(File file) {
        this.getFileList().getFiles().add(file);
        return this;
    }

    public TestDao fullFillMetaInfoByFileList(String directory, String eventType) {
        this.setCatalogName(directory);
        this.setEventType(eventType);
        this.getFileList().setSize(this.getFileList().getFiles().size());
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestDao)) return false;
        TestDao testDao = (TestDao) o;
        return getCatalogName().equals(testDao.getCatalogName()) &&
                getEventType().equals(testDao.getEventType()) &&
                Objects.equals(getFileList(), testDao.getFileList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCatalogName(), getEventType(), getFileList());
    }

    @Override
    public String toString() {
        return "TestDao{" +
                "catalogName='" + catalogName + '\'' +
                ", eventType='" + eventType + '\'' +
                ", fileList=" + fileList +
                '}';
    }
}
