import query.Query;
import query.Union;
import java.sql.SQLException;
/**
 * @author mkjodhani
 * @version 1
 * @project Query Provenance
 * @since 15/10/23
 */
public class Main {
    public static void main(String args[]) throws SQLException {
//        String query = "SELECT products.product_type FROM products, routes WHERE routes.product = products.product_id AND products.product_type = 'ELECTRONICS'"; //scanner.nextLine();
//        String query = "SELECT supplier_name FROM suppliers, routes WHERE routes.supplier = suppliers.supplier_id AND suppliers.supplier_name = 'SUPPLIER_54' UNION SELECT supplier_name FROM suppliers, routes WHERE routes.supplier = suppliers.supplier_id AND suppliers.supplier_name = 'SUPPLIER_55'";
//        String query = "SELECT supplier_name FROM suppliers, routes WHERE routes.supplier = suppliers.supplier_id";
//        String query = "SELECT products.product_type FROM products, routes WHERE routes.product = products.product_id";
//        String query  = "SELECT A,B FROM R, R";//UNION SELECT A,B  FROM R, R WHERE R.b = R.b";
        // Report Query
//        String query = "select supplier_name from suppliers, routes where supplier = suppliers.supplier_id AND region_to = 5";
//        String query = "select region_from from suppliers, routes where supplier = suppliers.supplier_id AND region_to = 5 UNION select region_from from suppliers, routes where supplier = suppliers.supplier_id AND region_to = 1";
//        String query  = "SELECT product_type, region_name FROM products, regions";
        String[] reportQueries = new String[]{
                "select supplier_name from suppliers, routes where supplier = suppliers.supplier_id AND region_to = 5",
                "SELECT product_type, region_name FROM products, regions where origin_region = regions.region_id",
                "SELECT product_type FROM products, routes where product = products.product_id AND region_from in (1, 2)"
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
//            System.out.println("Time taken: " + queryResult.getExecutionTime() + " milliseconds"); // or " + TimeUnit.NANOSECONDS.toMillis(elapsedTime) + " milliseconds" if you used System.nanoTime()
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