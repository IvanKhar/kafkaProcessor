package ru.otr.vtb.kafkaProcessor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileList implements Serializable {

    private static final long serialVersionUID = -6237160920486156169L;

    private Integer size;

    private List<File> file;

    public FileList() {
    }

    public FileList(@JsonProperty("size") Integer size,
                    @JsonProperty("file") List<File> file) {
        this.size = size;
        this.file = file;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public List<File> getFile() {
        if (file == null) {
            file = new ArrayList<>();
            return file;
        }
        return file;
    }

    public void setFile(List<File> file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileList)) return false;
        FileList fileList = (FileList) o;
        return getSize().equals(fileList.getSize()) &&
                getFile().equals(fileList.getFile());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSize(), getFile());
    }

    @Override
    public String toString() {
        return "FileList{" +
                "size=" + size +
                ", file=" + file +
                '}';
    }
}
