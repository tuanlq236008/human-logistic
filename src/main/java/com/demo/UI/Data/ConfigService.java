package com.demo.UI.Data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;

public class ConfigService {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static boolean saveAndUpdateConfig(String keyword, String start, String end, String app) {
        try {
            // 1. Lưu input_data.json
            InputData data = new InputData(keyword, start, end, app);
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("input_data.json"), data);

            // 2. Cập nhật config.json
            File configFile = new File("config.json");
            if (configFile.exists()) {
                ObjectNode configRoot = (ObjectNode) mapper.readTree(configFile);
                ArrayNode newKeywordsArray = mapper.createArrayNode();
                newKeywordsArray.add(keyword.trim());
                configRoot.set("keywords", newKeywordsArray);
                mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, configRoot);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}