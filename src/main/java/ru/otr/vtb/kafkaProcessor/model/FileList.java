package ru.otr.vtb.kafkaProcessor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileList implements Serializable {

    private static final long serialVersionUID = -6237160920486156169L;

    private Integer size;

    private List<File> files;

    public FileList() {
    }

    public FileList(@JsonProperty("size") Integer size,
                    @JsonProperty("files") List<File> files) {
        this.size = size;
        this.files = files;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public List<File> getFiles() {
        if (CollectionUtils.isEmpty(files)) {
            return new ArrayList<>();
        }
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileList)) return false;
        FileList fileList = (FileList) o;
        return getSize().equals(fileList.getSize()) &&
                getFiles().equals(fileList.getFiles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSize(), getFiles());
    }
}
