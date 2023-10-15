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
        String query = "SELECT products.product_type,routes.region_from FROM products, routes WHERE routes.product = products.product_id AND products.product_type = 'ELECTRONICS'"; //scanner.nextLine();
        if(query.contains("UNION")){
            String[] queries = query.split("UNION");
            Query query1 = new Query(queries[0]);
            Query query2 = new Query(queries[1]);
            Union union = new Union(query1.getProjection(),query2.getProjection());
            union.show();
        }else{
            Query queryResult = new Query(query);
            queryResult.getProjection().show();
        }
    }
}