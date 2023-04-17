package com.cb.db;

import lombok.SneakyThrows;
import org.postgresql.util.PGobject;

import java.sql.Array;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class DbUtils {

    public static TreeMap<Double, Double> doubleMapFromArray(Array array) {
        Map<String, String> rawMap = stringMapFromArray(array);
        return rawMap.entrySet().stream().collect(Collectors.toMap(e -> Double.parseDouble(e.getKey()), e -> Double.parseDouble(e.getValue()), (x, y)->y, TreeMap::new));
    }

    @SneakyThrows
    public static Map<String, String> stringMapFromArray(Array array) {
        Map<String, String> result = new LinkedHashMap<>(); // maintains order of items based on insertion order
        if (array != null) {
            ResultSet arrayRs = array.getResultSet();
            while (arrayRs.next()) {
                PGobject pgObject = (PGobject)arrayRs.getObject(2);
                String raw = pgObject.getValue().replaceAll("[()]", "");
                String[] parts = raw.split(",");
                result.put(parts[0], parts[1]);
            }
        }
        return result;
    }

}
