package ru.otr.vtb.kafkaProcessor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;


public class File implements Serializable {

    private static final long serialVersionUID = -6302466601201244230L;

    private String directory;

    private String filename;

    private String lastModifiedTime;

    private String creationTime;

    private Long size;

    public File() {
    }

    @JsonCreator
    public File(@JsonProperty("directory") String directory,
                @JsonProperty("filename") String filename,
                @JsonProperty("lastModifiedTime") String lastModifiedTime,
                @JsonProperty("creationTime") String creationTime,
                @JsonProperty("size") Long size) {
        this.directory = directory;
        this.filename = filename;
        this.lastModifiedTime = lastModifiedTime;
        this.creationTime = creationTime;
        this.size = size;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(String lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof File)) return false;
        File file = (File) o;
        return getSize().equals(file.getSize()) &&
                getDirectory().equals(file.getDirectory()) &&
                getFilename().equals(file.getFilename()) &&
                Objects.equals(getLastModifiedTime(), file.getLastModifiedTime()) &&
                Objects.equals(getCreationTime(), file.getCreationTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDirectory(), getFilename(), getLastModifiedTime(), getCreationTime(), getSize());
    }

    @Override
    public String toString() {
        return "File{" +
                "directory='" + directory + '\'' +
                ", filename='" + filename + '\'' +
                ", lastModifiedTime='" + lastModifiedTime + '\'' +
                ", creationTime='" + creationTime + '\'' +
                ", size=" + size +
                '}';
    }
}
