package ru.otr.vtb.kafkaProcessor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component
public class TestDao implements Serializable {

    private static final long serialVersionUID = -3561142838980678679L;

    @JsonProperty("files")
    private List<String> files;

    public TestDao() {
    }

    @JsonCreator
    public TestDao(List<String> files) {
        this.files = files;
    }

    public List<String> getFiles() {
        if (CollectionUtils.isEmpty(this.files)) {
            return new ArrayList<>();
        }
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
