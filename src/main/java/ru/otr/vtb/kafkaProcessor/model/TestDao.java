package ru.otr.vtb.kafkaProcessor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("tuple")
@Component
public class TestDao {

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
