package org.queryprovenance.demo;

import database.DatabaseConnection;
import query.Query;
import query.Table;
import query.Union;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author mkjodhani
 * @version 1
 * @since 15/10/23
 */
public class Main {
    static boolean timeAnalysis = false;

    public static void main(String args[]) throws SQLException {
        DatabaseConnection.getConnection();
        if (timeAnalysis) {
            generateReport();
        } else {
            runQuery();
        }
    }

    public static void generateReport() throws SQLException {
        String[] reportQueries = new String[]{
                "select female.birthdate from male, female where male.fav_num > 5 AND female.fav_num < 100000",
                "SELECT male.name, female.birthdate,  FROM male, female where female.fav_num > 6522",
                "SELECT male.name, female.first_name FROM male, female where female.fav_num in ( 646495, 397375, 956267, 680928, 341340, 638124, 595823, 372669 ) AND female.state in ( 'RhodeIsland', 'Florida', 'Kentucky', 'WestVirginia', 'Ohio', 'Iowa', 'WestVirginia' )"
        };
        List<Integer> variations = new ArrayList<>(Arrays.asList(new Integer[]{10, 50, 100, 500, 1000, 2000, 3000, 5000, 10000}));
        HashMap<Integer, HashMap<String, Long>> values = new HashMap<>();
        for (Integer variation : variations) {
            int index = 0;
            Table.maxFetchRows = variation;
            HashMap<String, Long> stringIntegerHashMap = new HashMap<>();
            for (String query : reportQueries) {
                Query queryResult = new Query(query);
                stringIntegerHashMap.put("Query " + (index + 1), queryResult.getExecutionTime());
                index++;
            }
            values.put(variation, stringIntegerHashMap);
        }
    }

    private static void runQuery() throws SQLException {
        String[] reportQueries = new String[]{
                "select supplier_name from suppliers, routes where supplier = suppliers.supplier_id AND region_to = 5",
                "SELECT product_type, region_name FROM products, regions where origin_region = regions.region_id",
                "SELECT product_type, region_name FROM products, routes where product = products.product_id AND region_from in (1, 2)"
        };
        String query = reportQueries[1];
        System.out.println("Query ::");
        System.out.println(query);
        if (query.contains("UNION")) {
            String[] queries = query.split("UNION");
            Query query1 = new Query(queries[0]);
            Query query2 = new Query(queries[1]);
            Union union = new Union(query1.getProjection(), query2.getProjection());
            union.show();
        } else {
            Query queryResult = new Query(query);
            if (queryResult.getProjection() != null) {
                queryResult.getProjection().show();
                System.out.println("Execution Time: " + queryResult.getExecutionTime() + " milliseconds");
            } else {
                System.out.println("No results found!");
            }
        }
    }
}