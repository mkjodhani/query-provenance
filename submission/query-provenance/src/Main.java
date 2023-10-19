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
 * @project Query Provenance
 * @since 15/10/23
 */
public class Main {
    static boolean timeAnalysis = false;
    public static void main(String args[]) throws SQLException {
        if(timeAnalysis){
            generateReport();
        }else{
            runQuery();
        }
    }
    public static void generateReport(){
        // firstTable => id, name, age, fav_num, city
        // secondTable => id, first_name, birthdate, fav_num, state
        String[] reportQueries = new String[]{
                "select stenthousand.birthdate from rtenthousand, stenthousand where rtenthousand.fav_num > 5 AND stenthousand.fav_num < 100000",
                "SELECT rtenthousand.name, stenthousand.birthdate,  FROM rtenthousand, stenthousand where stenthousand.fav_num > 6522",
                "SELECT rtenthousand.name, stenthousand.first_name FROM rtenthousand, stenthousand where stenthousand.fav_num in ( 646495, 397375, 956267, 680928, 341340, 638124, 595823, 372669 ) AND stenthousand.state in ( 'RhodeIsland', 'Florida', 'Kentucky', 'WestVirginia', 'Ohio', 'Iowa', 'WestVirginia' )"
        };
        List<Integer> variations = new ArrayList<>(Arrays.asList(new Integer[]{10,50,100,500,1000,2000,3000,5000,10000}));
        HashMap<Integer,HashMap<String,Long>> values = new HashMap<>();
        for(Integer variation: variations){
            System.out.println("variation::" + variation);
            int index = 0;
            Table.maxFetchRows = variation;
            System.out.println("::::::::::::::::::::::::::" + (index + 1) + "::::::::::::::::::::::::::");
            HashMap<String,Long> stringIntegerHashMap = new HashMap<>();
            for(String query: reportQueries){
                Query queryResult = new Query(query);
                stringIntegerHashMap.put("Query " + (index + 1), queryResult.getExecutionTime());
                index++;
            }
            values.put(variation,stringIntegerHashMap);
            System.out.println(values);
        }
    }
    private static void runQuery(){
        String[] reportQueries = new String[]{
            "select supplier_name from suppliers, routes where supplier = suppliers.supplier_id AND region_to = 5",
            "SELECT product_type, region_name FROM products, regions where origin_region = regions.region_id",
            "SELECT product_type FROM products, routes where product = products.product_id OR region_from in (1, 2)"
        };
        String query = reportQueries[2];
        System.out.println("Query ::");
        System.out.println(query);
        if(query.contains("UNION")){
            String[] queries = query.split("UNION");
            Query query1 = new Query(queries[0]);
            Query query2 = new Query(queries[1]);
            Union union = new Union(query1.getProjection(),query2.getProjection());
            union.show();
        }else{
            Query queryResult = new Query(query);
            if(queryResult.getProjection() != null){
                queryResult.getProjection().show();
                System.out.println("Execution Time: " + queryResult.getExecutionTime() + " milliseconds"); // or " + TimeUnit.NANOSECONDS.toMillis(elapsedTime) + " milliseconds" if you used System.nanoTime()
            }else{
                System.out.println("No results found!");
            }
        }
    }

}

//|      	| Query 1 	| Query 2 	| Query 3 	|
//|------	|---------	|---------	|---------	|
//| 10   	| 25      	| 33      	| 28      	|
//| 50   	| 74      	| 32      	| 114     	|
//| 100  	| 48      	| 69      	| 113     	|
//| 500  	| 4058    	| 1423    	| 6953    	|
//| 1000 	| 56720   	| 6699    	| 81591   	|

//10 50 100 500 1000
//25  74  48  4058  56720
//33 32 69 1423 6699
//28 114 113 6953 81591