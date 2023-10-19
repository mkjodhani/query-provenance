package query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author mkjodhani
 * @version 1
 * @project Query Provenance
 * @since 15/10/23
 */
public class Union {
    private List<Row> rows;
    private HashMap<String, Row.TYPE> columnTypes;
    private Table table;

    public Union(Table tableA, Table tableB) throws SQLException {
        this.rows = new ArrayList<>();
        this.rows.addAll(tableA.getRows());
        this.rows.addAll(tableB.getRows());
        this.columnTypes = tableA.getColumnTypes();
        this.table = new Table(this.columnTypes,this.rows, String.format(tableA+":"+ tableB));
        table.aggregate();
    }
    public void show() {
        this.table.show();
    }

}

