package query;

import java.sql.*;
import java.util.*;

/**
 * @author mkjodhani
 * @version 1
 * @project Query Provenance
 * @since 15/10/23
 */

public class Table {
    private String tableNameA, tableNameB;
    private boolean isJoinTable;
    HashMap<String, Row.TYPE> columnTypesA;
    HashMap<String, Row.TYPE> columnTypesB;
    HashMap<String, Row.TYPE> jointColumnTypes;
    List<Row> rowsA;
    List<Row> rowsB;
    String tableBCol, tableACol;
    List<Row> jointRows;
    public Table(String tableName) {
        this.tableNameA = tableName;
        this.rowsA = new ArrayList<>();
        this.rowsB = new ArrayList<>();
        this.columnTypesA = new HashMap<>();
        this.columnTypesB = new HashMap<>();
        this.isJoinTable = false;
    }

    public Table(String tableNameA, String tableNameB, String tableACol, String tableBCol) {
        this.tableNameA = tableNameA;
        this.tableNameB = tableNameB;
        this.rowsA = new ArrayList<>();
        this.rowsB = new ArrayList<>();
        this.columnTypesA = new HashMap<>();
        this.columnTypesB = new HashMap<>();
        this.isJoinTable = true;
        this.tableACol = tableACol;
        this.tableBCol = tableBCol;
        this.jointColumnTypes = new HashMap<>();
        try {
            generateRows();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private HashMap<String, Row.TYPE> getColumnTypesByRelation(String table) throws SQLException {
        Connection connection  = Query.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT  * FROM " + table);
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        HashMap<String, Row.TYPE> columnTypes = new HashMap<>();
        for (int i = 1; i <= columnCount; i++) {
            columnTypes.put(metaData.getColumnName(i), Objects.equals(metaData.getColumnTypeName(i), "int4") ? Row.TYPE.DIGIT : Row.TYPE.STRING);
        }
        return columnTypes;
    }
    private List<Row> getRowsByRelation(String table) throws SQLException {
        Connection connection  = Query.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT  * FROM " + table);
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        HashMap<String, Row.TYPE> columnTypes = new HashMap<>();
        List<Row> rows = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            columnTypes.put(metaData.getTableName(i) +"."+metaData.getColumnName(i), Objects.equals(metaData.getColumnTypeName(i), "int4") ? Row.TYPE.DIGIT : Row.TYPE.STRING);
        }
        while (resultSet.next()){
            Row row = new Row(columnTypes,table);
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i),resultSet.getString(metaData.getColumnName(i)));
            }
            row.setAnnotation(String.format("%s",row.getValues().get("ann")));
            rows.add(row);
        }
        return rows;
    }
    private void generateRows() throws SQLException {
        if(this.isJoinTable){
            this.rowsA = getRowsByRelation(this.tableNameA);
            this.rowsB = getRowsByRelation(this.tableNameB);
            this.columnTypesA = getColumnTypesByRelation(tableNameA);
            this.columnTypesA = getColumnTypesByRelation(tableNameB);
            this.jointRows = new ArrayList<>();
            for(String col:this.rowsA.get(0).columnTypes.keySet()){
                String tableCol = col.contains(tableNameA+".") ? col : String.format("%s.%s",this.tableNameA,col);
                this.jointColumnTypes.put(tableCol,this.rowsA.get(0).columnTypes.get(col));
            }
            for(String col:this.rowsB.get(0).columnTypes.keySet()){
                String tableCol = col.contains(tableNameB+".") ? col : String.format("%s.%s",this.tableNameB,col);
                this.jointColumnTypes.put(tableCol,this.rowsB.get(0).columnTypes.get(col));
            }
            for (Row rowA : this.rowsA) {
                for (Row rowB:this.rowsB) {
                    if(rowA.getValues().get(this.tableACol).equals(rowB.getValues().get(this.tableBCol))){
                        Row row = new Row(jointColumnTypes, String.format("%s.%s",this.tableNameA,this.tableNameB));  // tableName=A.B
                        row.copyFrom(rowA,this.tableNameA);
                        row.copyFrom(rowB,this.tableNameB);
                        String updatedAnnotation = String.format("%s.%s",rowA.getAnnotation(),rowB.getAnnotation());
                        row.setAnnotation(updatedAnnotation);
                        this.jointRows.add(row);
                    }
                }
            }
        }
        else{
            this.rowsA = getRowsByRelation(this.tableNameA);
            this.columnTypesA = getColumnTypesByRelation(tableNameA);
        }
    }

    public void show(){
        if(this.isJoinTable) {
            for (Row rowA : this.jointRows) {
                System.out.println(rowA);
            }
        }else{
            for (Row rowA : this.rowsA) {
                System.out.println(rowA);
            }
        }
    }

    public void filterByClauses(Set<Clause> clauses) {
        if(isJoinTable){
            for(Clause clause: clauses){
                Iterator<Row> iterator = this.jointRows.iterator();
                while (iterator.hasNext()){
                    Row mp = iterator.next();
                    if(!mp.apply(clause, true)){
                        iterator.remove();
                    }
                }
            }
        }else{
            for(Clause clause: clauses){
                Iterator<Row> iterator = this.rowsA.iterator();
                while (iterator.hasNext()){
                    Row mp = iterator.next();
                    if(!mp.apply(clause,false)){
                        iterator.remove();
                    }
                }
            }
        }
    }
    public void filterProjection(List<String> columns){
        if(this.isJoinTable){
            for(Row row: this.jointRows){
                row.filterByColumnNames(columns);
            }
        }else{
            for(Row row: this.rowsA){
                row.filterByColumnNames(columns);
            }
        }
    }

    public void aggregate() {
        HashMap<String,Row> isSelectedRow = new HashMap<>();
        for(Row row: this.jointRows){
            if(isSelectedRow.getOrDefault(row.getValues().toString(),null) != null){
                Row selecctedRow = isSelectedRow.get(row.getValues().toString());
                selecctedRow.setAnnotation(row.getAnnotationsMap());
            }else{
                isSelectedRow.put(row.getValues().toString(),row);
            }
        }
        this.jointRows = new ArrayList<>();
        for (Row row : isSelectedRow.values()) {
            this.jointRows.add(row);
        }
    }
}
