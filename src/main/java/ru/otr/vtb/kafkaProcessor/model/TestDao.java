package ru.otr.vtb.kafkaProcessor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;

@Component
public class TestDao implements Serializable {

    private static final long serialVersionUID = -3561142838980678679L;

    @JsonProperty("files")
    private Set<String> files;

    public TestDao() {
    }

    @JsonCreator
    public TestDao(Set<String> files) {

        this.files = files;
    }

    public Set<String> getFiles() {
        if (CollectionUtils.isEmpty(this.files)) {
            return new HashSet<>();
        }
        return files;
    }

    public void setFiles(Set<String> files) {
        this.files = files;
    }
}
