package br.com.schemusic.schemusic_api.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseQueryUtil {

    @Autowired
    private DataSource dataSource;

    public List<Map<String, Object>> executarQuery(String sql) throws Exception {
        List<Map<String, Object>> resultados = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnLabel = metaData.getColumnLabel(i);
                    Object value = rs.getObject(i);
                    row.put(columnLabel, value);
                }
                resultados.add(row);
            }
        }
        
        return resultados;
    }
    
    public void imprimirResultados(String descricao, List<Map<String, Object>> resultados) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println(descricao);
        System.out.println("=".repeat(80));
        
        if (resultados.isEmpty()) {
            System.out.println("Nenhum resultado encontrado.");
            return;
        }
        
        // Print headers
        resultados.get(0).keySet().forEach(key -> System.out.print(key + " | "));
        System.out.println();
        System.out.println("-".repeat(80));
        
        // Print rows
        for (Map<String, Object> row : resultados) {
            row.values().forEach(value -> System.out.print(value + " | "));
            System.out.println();
        }
        
        System.out.println("Total de registros: " + resultados.size());
    }
}
