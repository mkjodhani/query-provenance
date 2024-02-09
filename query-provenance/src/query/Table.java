package query;

import database.DatabaseConnection;
import query.clause.ComplexClause;
import query.clause.SimpleClause;

import java.sql.*;
import java.util.*;

/**
 * The Table class represents a database table, including its name, column types, column indexes,
 * and a list of rows containing data.
 *
 * @author mkjodhani
 * @version 1
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
    /**
     * Retrieves the column types of the table by querying the database.
     *
     * @return A map of column names to their respective data types.
     * @throws SQLException If there is an issue with SQL operations.
     */
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
    /**
     * Generates column indexes based on column types.
     *
     * @param columnTypes A map of column names to their corresponding data types.
     * @return A map of column names to their respective indexes.
     * @throws SQLException If there is an issue with SQL operations.
     */
    private HashMap<String, Integer> getColumnIndexesByRelation(HashMap<String, Row.TYPE> columnTypes) throws SQLException {
        HashMap<String, Integer> indexes = new HashMap<>();
        int columnIndex = 0;
        for(String col:columnTypes.keySet()){
            indexes.put(col,columnIndex);
            columnIndex++;
        }
        return indexes;
    }
    /**
     * Retrieves rows of data for the table by querying the database.
     *
     * @return A list of Row objects representing the table data.
     * @throws SQLException If there is an issue with SQL operations.
     */
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
    /**
     * Generates combined column indexes for a joint table.
     *
     * @param columnTypesA Column types of the first table.
     * @param columnTypesB Column types of the second table.
     * @param tableA       Name of the first table.
     * @param tableB       Name of the second table.
     * @return A map of column names to their respective indexes in the joint table.
     */
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
    /**
     * Generates column types for a joint table.
     *
     * @param columnTypesA Column types of the first table.
     * @param columnTypesB Column types of the second table.
     * @param tableA       Name of the first table.
     * @param tableB       Name of the second table.
     * @return A map of column names to their respective data types in the joint table.
     */
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
    // Methods for displaying, filtering, and aggregating data...

    /**
     * Displays the contents of the table by printing each row.
     */

    public void show(){
        for (Row row : this.rows) {
            System.out.println(row);
        }
    }
    /**
     * Filters rows based on a list of SimpleClause conditions.
     *
     * @param clauseList A list of SimpleClause conditions.
     */
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
    /**
     * Filters the columns of each row based on a list of column names.
     *
     * @param columns The list of column names to retain.
     */
    public void filterProjection(List<String> columns){
        for(Row row: this.rows){
            row.filterByColumnNames(columns);
        }
    }
    /**
     * Aggregates rows with the same values, combining their annotations.
     */
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
    /**
     * Performs a join operation with another table based on a list of SimpleClause conditions.
     *
     * @param table   The table to join with.
     * @param clauses A list of SimpleClause conditions for the join.
     * @return A new Table representing the result of the join operation.
     */
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
                row.setAnnotation(rowA.getAnnotation() + "*"+rowB.getAnnotation());
            jointTable.rows.add(row);
            }
        }
        jointTable.filterByClauses(clauses);
        return jointTable;
    }
    // Getter methods...

    /**
     * Retrieves the name of the table.
     *
     * @return The name of the table.
     */
    public HashMap<String, Row.TYPE> getColumnTypes() {
        return columnTypes;
    }
    /**
     * Retrieves the column indexes of the table.
     *
     * @return A map of column names to their respective indexes.
     */
    public HashMap<String, Integer> getColumnIndexes() {
        return columnIndexes;
    }
    /**
     * Retrieves the list of rows in the table.
     *
     * @return The list of rows containing data.
     */
    public List<Row> getRows() {
        return rows;
    }
}
