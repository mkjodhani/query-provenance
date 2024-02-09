package test.query;

import database.DatabaseConnection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import query.Query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author mkjodhani
 * @since 09/02/24
 */
class QueryTest {
    static Query query;
    static String queryString = "SELECT product_type, region_name FROM products, regions where origin_region = regions.region_id";
    @BeforeAll
    static void connect(){
        DatabaseConnection.connect("jdbc:postgresql://localhost:5432/postgres","postgres","6563");
        System.out.println("Connection established!");
    }
    @BeforeEach
    void executeQuery() throws SQLException {
        query = new Query(queryString);
    }
    @org.junit.jupiter.api.Test
    void getColumnNameWithTable() throws SQLException {
        assertEquals("product_type",query.getColumnNameWithTable("product_type"));
    }

    @org.junit.jupiter.api.Test
    void getProjection() throws SQLException {
        Statement statement = DatabaseConnection.getConnection().createStatement();
        ResultSet resultSet = statement.executeQuery(queryString + " GROUP BY product_type, region_name");
        int totalRows = 0;
        while (resultSet.next()){
            totalRows++;
        }
        assertEquals(totalRows,query.getProjection().getRows().size());
    }
}