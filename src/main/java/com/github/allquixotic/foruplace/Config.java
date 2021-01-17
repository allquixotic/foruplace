package com.github.allquixotic.foruplace;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.Builder;
import lombok.Data;

import java.io.FileReader;
import java.io.IOException;

@Data
@Builder
public class Config {
    private String endpoint;
    private String accessKey;
    private String secretAccessKey;
    private String bucket;
    private String directory;

    public static Config readConfig() throws IOException {
        return readConfig("config.json");
    }

    public static Config readConfig(String file) throws IOException {
        return new Gson().fromJson(new FileReader(file), new TypeToken<Config>() {}.getType());
    }
}
