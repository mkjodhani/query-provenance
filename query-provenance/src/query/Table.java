package query;

import database.DatabaseConnection;
import query.clause.ComplexClause;
import query.clause.SimpleClause;

import java.sql.*;
import java.util.*;

/**
 * @author mkjodhani
 * @version 1
 * @project Query Provenance
 * @since 15/10/23
 */

public class Table {
    private String name;
    public static int maxFetchRows = Integer.MAX_VALUE;
    private HashMap<String, Row.TYPE> columnTypes;
    private HashMap<String, Integer> columnIndexes;
    private List<Row> rows;
    public Table(){
        this.name = "";
        this.columnTypes = new HashMap<>();
        this.columnIndexes = new HashMap<>();
        this.rows = new ArrayList<>();
    }
    public Table(String tableName) {
        try{
            this.name = tableName;
            this.columnTypes = getColumnTypesByRelation();
            this.columnIndexes = getColumnIndexesByRelation(this.columnTypes);
            this.rows = getRowsByRelation();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public Table(HashMap<String, Row.TYPE> columnTypes, List<Row> rows, String tableName) throws SQLException {
        this.rows = rows;
        this.columnTypes = columnTypes;
        this.columnIndexes = getColumnIndexesByRelation(this.columnTypes);
        this.name = tableName;
    }

    private HashMap<String, Row.TYPE> getColumnTypesByRelation() throws SQLException {
        Connection connection  = DatabaseConnection.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT  * FROM " + this.name);
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        HashMap<String, Row.TYPE> columnTypes = new HashMap<>();
        for (int i = 1; i <= columnCount; i++) {
            columnTypes.put(metaData.getColumnName(i), Objects.equals(metaData.getColumnTypeName(i), "int4") ? Row.TYPE.DIGIT : Row.TYPE.STRING);
        }
        return columnTypes;
    }
    private HashMap<String, Integer> getColumnIndexesByRelation(HashMap<String, Row.TYPE> columnTypes) throws SQLException {
        HashMap<String, Integer> indexes = new HashMap<>();
        int columnIndex = 0;
        for(String col:columnTypes.keySet()){
            indexes.put(col,columnIndex);
            columnIndex++;
        }
        return indexes;
    }
    private List<Row> getRowsByRelation() throws SQLException {
        int fetchedRows = 0;
        Connection connection  = DatabaseConnection.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT  * FROM " + this.name);
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<Row> rows = new ArrayList<>();
        while (resultSet.next()){
            if(fetchedRows > Table.maxFetchRows){
                break;
            }
            Row row = new Row(this.columnTypes,this.columnIndexes,this.name);
            for (int i = 1; i <= columnCount; i++) {
                row.setValueByColumn(metaData.getColumnName(i),resultSet.getString(metaData.getColumnName(i)));
            }
            // hard-coded value:: ERROR need to change
            row.setAnnotation(String.format("%s",row.getValueByColumn("ann")));
            rows.add(row);
        }
        return rows;
    }
    private static HashMap<String,Integer> generateJointTableColumnIndexes(HashMap<String,Row.TYPE> columnTypesA,HashMap<String,Row.TYPE> columnTypesB,String tableA, String tableB){
        if(tableA.equals(tableB)){
            tableB=  tableB + "1";
        }
        int rowIndex= 0 ;
        HashMap<String, Integer> combinedIndexes = new HashMap<>();
        for(String col:columnTypesA.keySet()){
            String colAlias = String.format("%s.%s",tableA,col);
            if(columnTypesB.getOrDefault(col,null) == null){
                combinedIndexes.put(col,rowIndex);
            }
            combinedIndexes.put(colAlias,rowIndex);
            rowIndex++;
        }
        for(String col:columnTypesB.keySet()){
            String colAlias = String.format("%s.%s",tableB,col);
            if(columnTypesA.getOrDefault(col,null) == null){
                combinedIndexes.put(col,rowIndex);
            }
            combinedIndexes.put(colAlias,rowIndex);
            rowIndex++;
        }
        return combinedIndexes;
    }
    private static HashMap<String,Row.TYPE> generateJoinTypes(HashMap<String,Row.TYPE> columnTypesA,HashMap<String,Row.TYPE> columnTypesB,String tableA, String tableB){
        if(tableA.equals(tableB)){
            tableB=  tableB + "1";
        }
        HashMap<String,Row.TYPE> resultTypes = new HashMap<>();
        for(String col:columnTypesA.keySet()){
            String colAlias = String.format("%s.%s",tableA,col);
            if(columnTypesB.getOrDefault(col,null) == null){
                resultTypes.put(col,columnTypesA.get(col));
            }
            resultTypes.put(colAlias,columnTypesA.get(col));
        }
        for(String col:columnTypesB.keySet()){
            String colAlias = String.format("%s.%s",tableB,col);
            if(columnTypesB.getOrDefault(col,null) == null){
                resultTypes.put(col,columnTypesB.get(col));
            }
            resultTypes.put(colAlias,columnTypesB.get(col));
        }
        return resultTypes;
    }
    public void show(){
        for (Row row : this.rows) {
            System.out.println(row);
        }
    }

    public void filterByClauses(List<SimpleClause> clauseList) {

        Iterator<Row> iterator = this.rows.iterator();
        while (iterator.hasNext()){
            Row mp = iterator.next();
            if(clauseList.size() == 1){
                if(!mp.apply(clauseList.get(0))){
                    iterator.remove();
                }
            }else{
                if(clauseList.get(0).getConjunction() == ComplexClause.CONJUNCTION.AND){
                    if(!mp.apply(clauseList.get(0)) || !mp.apply(clauseList.get(1))){
                        iterator.remove();
                    }
                }else{
                    if(!mp.apply(clauseList.get(0)) && !mp.apply(clauseList.get(1))){
                        iterator.remove();
                    }
                }
            }
        }
    }
    public void filterProjection(List<String> columns){
        for(Row row: this.rows){
            row.filterByColumnNames(columns);
        }
    }

    public void aggregate() {
        HashMap<String,Row> isSelectedRow = new HashMap<>();
        List<Row> rowList= new ArrayList<>();
        for(Row row: this.rows){
            if(isSelectedRow.getOrDefault(row.getValues().toString(),null) != null){
                Row selecctedRow = isSelectedRow.get(row.getValues().toString());
                selecctedRow.setAnnotation(row.getAnnotationsMap());
            }else{
                isSelectedRow.put(row.getValues().toString(),row);
                rowList.add(row);
            }
        }
        this.rows = rowList;
    }

    public String getName() {
        return name;
    }

    public Table join(Table table, List<SimpleClause> clauses){
        Table jointTable = new Table();
        jointTable.columnTypes = generateJoinTypes(this.getColumnTypes(),table.getColumnTypes(),this.name,table.getName());
        jointTable.columnIndexes = generateJointTableColumnIndexes(this.getColumnTypes(),table.getColumnTypes(),this.name,table.getName());
        jointTable.name = this.getName() + "." + table.getName();
        for (Row rowA: this.rows){
            for (Row rowB :table.rows){
                Row row = new Row(jointTable.columnTypes,jointTable.columnIndexes, jointTable.name);
                row.copyFrom(rowA);
                row.copyFrom(rowB);
                row.setAnnotation(rowA.getAnnotation() + "·"+rowB.getAnnotation());
            jointTable.rows.add(row);
            }
        }
        jointTable.filterByClauses(clauses);
        return jointTable;
    }
    public HashMap<String, Row.TYPE> getColumnTypes() {
        return columnTypes;
    }

    public HashMap<String, Integer> getColumnIndexes() {
        return columnIndexes;
    }

    public List<Row> getRows() {
        return rows;
    }
}
