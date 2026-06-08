package com.demo.Utils.ConvertToCSV;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JSONToCSV extends ToCSV {

    private final ObjectMapper mapper = new ObjectMapper();

    public JSONToCSV(Path filePath) {
        super(filePath);
    }

    @Override
    public void toCSV() {
        if (!isValid()) {
            System.err.println("JSON file not found: " + filePath);
            return;
        }

        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);

            // Determine JSON shape: array of objects, single object, or newline-delimited JSON
            List<Map<String, Object>> items = new ArrayList<>();

            JsonNode root = mapper.readTree(content);
            if (root.isArray()) {
                items = mapper.convertValue(root, new TypeReference<List<Map<String, Object>>>(){});
            } else if (root.isObject()) {
                // either a single object or an object containing a data array
                if (root.has("data") && root.get("data").isArray()) {
                    items = mapper.convertValue(root.get("data"), new TypeReference<List<Map<String, Object>>>(){});
                } else {
                    Map<String, Object> single = mapper.convertValue(root, new TypeReference<Map<String, Object>>(){});
                    items.add(single);
                }
            } else {
                // fallback: try to parse as newline-delimited JSON (NDJSON)
                String[] lines = content.split("\r?\n");
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    Map<String, Object> obj = mapper.readValue(line, new TypeReference<Map<String, Object>>(){});
                    items.add(obj);
                }
            }

            if (items.isEmpty()) {
                System.out.println("No JSON objects to convert in " + filePath);
                return;
            }

            // Determine header: union of keys preserving insertion order where possible
            Set<String> headerSet = new LinkedHashSet<>();
            for (Map<String, Object> m : items) headerSet.addAll(m.keySet());
            String[] header = headerSet.toArray(new String[0]);

            // Ensure CSV parent exists
            Path parent = CSVPath.getParent();
            if (parent != null && !Files.exists(parent)) Files.createDirectories(parent);

            try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(Files.newOutputStream(CSVPath), StandardCharsets.UTF_8))) {
                writer.writeNext(header);
                for (Map<String, Object> m : items) {
                    String[] row = new String[header.length];
                    for (int i = 0; i < header.length; i++) {
                        Object val = m.get(header[i]);
                        row[i] = val == null ? "" : stringify(val);
                    }
                    writer.writeNext(row);
                }
            }

            System.out.println("Wrote CSV to " + CSVPath + " (rows=" + items.size() + ")");

        } catch (IOException e) {
            System.err.println("Failed to convert JSON to CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String stringify(Object v) throws IOException {
        if (v instanceof String) return (String) v;
        if (v instanceof Number || v instanceof Boolean) return String.valueOf(v);
        // for complex values (arrays, objects), write compact JSON
        return mapper.writeValueAsString(v);
    }

}
