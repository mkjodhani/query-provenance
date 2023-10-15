package query;

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
    private Table projectionA, projectionB;
    private HashMap<String, Row.TYPE> columnTypes;

    public Union(Table tableA, Table tableB) {
        this.projectionA = tableA;
        this.projectionB = tableB;
        this.rows = new ArrayList<>();
        this.rows.addAll(this.projectionA.jointRows);
        this.rows.addAll(this.projectionB.jointRows);
        this.columnTypes = this.projectionA.jointColumnTypes;
    }
    public void show() {
        for (Row row : this.rows) {
            System.out.println(row);
        }
    }

}

