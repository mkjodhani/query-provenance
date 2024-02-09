package query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Union class represents the union operation on two tables, combining their rows into a new table.
 * It provides methods to perform the union operation and display the resulting table.
 *
 * @version 1
 * @since 15/10/23
 */
public class Union {
    /**
     * List of rows containing the result of the union operation.
     * Each row represents a combined entry from both input tables.
     */
    private List<Row> rows;
    /**
     * Column types of the resulting table after the union operation.
     * Defines the data types of each column in the resulting table.
     */
    private HashMap<String, Row.TYPE> columnTypes;
    /**
     * The resulting table after the union operation.
     * It contains the combined rows and column information from both input tables.
     */
    private Table table;
    /**
     * Constructs a Union object by performing the union operation on two tables.
     *
     * @param tableA The first table to union.
     * @param tableB The second table to union.
     * @throws SQLException If there is an issue with SQL operations during the union.
     */
    public Union(Table tableA, Table tableB) throws SQLException {
        this.rows = new ArrayList<>();
        this.rows.addAll(tableA.getRows());
        this.rows.addAll(tableB.getRows());
        this.columnTypes = tableA.getColumnTypes();
        this.table = new Table(this.columnTypes,this.rows, String.format(tableA+":"+ tableB));
        table.aggregate();
    }
    /**
     * Displays the contents of the resulting table after the union operation.
     * The resulting table includes combined rows from both input tables.
     */
    public void show() {
        this.table.show();
    }

}

