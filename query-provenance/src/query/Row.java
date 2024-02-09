package query;

import query.clause.SimpleClause;

import java.util.*;
/**
 * The Row class represents a row of data from a database table.
 * It encapsulates information about column values, types, and annotations.
 *
 * @author mkjodhani
 * @version 1
 * @since 15/10/23
 */

public class Row {
    /**
     * Enumeration representing the data types for columns.
     */
    public enum TYPE{
        STRING,
        DIGIT
    }
    private HashMap<Integer,String> values;
    private HashMap<String, Integer> columnIndexes;
    private HashMap<String,TYPE> columnTypes;
    private HashMap<String,Integer> annotations;
    private String tableName;
    /**
     * Constructs a Row object with the specified column types, column indexes, and table name.
     *
     * @param columnTypes    A map of column names to their corresponding data types.
     * @param columnIndexes  A map of column names to their corresponding indexes.
     * @param tableName      The name of the table to which the row belongs.
     */
    public Row(HashMap<String, TYPE> columnTypes,HashMap<String, Integer> columnIndexes,String tableName) {
        this.columnTypes = columnTypes;
        this.values = new HashMap<>();
        this.tableName = tableName;
        this.annotations = new HashMap<>();
        this.columnIndexes = columnIndexes;
    }
    /**
     * Retrieves the values of the row.
     *
     * @return A map containing column indexes and their corresponding values.
     */
    public HashMap<Integer, String> getValues() {
        return values;
    }
    /**
     * Retrieves the annotations for the row.
     *
     * @return A map containing column names and their appearance count.
     */
    public HashMap<String, Integer> getAnnotationsMap() {
        return annotations;
    }
    /**
     * Retrieves a formatted string representation of annotations.
     *
     * @return A string containing annotations in the form "count + annotation".
     */
    public String getAnnotation() {
        String annotation = "";
        for(String ann: this.annotations.keySet()){
            annotation +=  String.format("%s%s",this.annotations.getOrDefault(ann,0) <=1 ? "" : String.valueOf(this.annotations.getOrDefault(ann,0)),ann);
        }
        return annotation;
    }
    /**
     * Retrieves a formatted string representation of annotations with "+" separated.
     *
     * @return A string containing annotations in the form "count + annotation + count2 + annotation2".
     */
    public String getFinalAnnotation() {
        String annotation = "";
        for(String ann: this.annotations.keySet()){
            annotation +=  String.format("%s%s",this.annotations.getOrDefault(ann,0) <=1 ? "" : String.valueOf(this.annotations.getOrDefault(ann,0)),ann) + " + ";
        }
        return annotation.replaceAll(" \\+ $","");
    }
    /**
     * Sets an annotation for the row.
     *
     * @param ann The annotation to be set.
     */
    public void setAnnotation(String ann) {
        int updatedValue = this.annotations.getOrDefault(ann,0) + 1;
        this.annotations.put(ann,updatedValue);
    }
    /**
     * Sets annotations for the row based on a map.
     *
     * @param anns A map of annotations and their counts.
     */
    public void setAnnotation(HashMap<String,Integer> anns) {
        for(String ann: anns.keySet()){
            int updatedValue = this.annotations.getOrDefault(ann,0) + 1;
            this.annotations.put(ann,updatedValue);
        }
    }
    private boolean isContains(String values, String item){
        double d;
        try {
            d = Double.parseDouble(item.trim());
            for(String value: values.split(",")){
                double doubleValue = Double.parseDouble(value.replaceAll("^\\(|\\)$","").trim());
                if (doubleValue == d){
                    return true;
                }
            }
            return false;
        }catch (Exception e){
            List<String> possibleValues = Arrays.asList(values.replaceAll("^\\(|\\)$", "").split(","));
            return possibleValues.contains(item);
        }
    }
    private boolean compare(String a, String b, String comparator) throws Exception {
        if(comparator.toLowerCase().contains("in")){
            return isContains(b,a);
        }
        boolean result = false;
        try{
            int valueA =Integer.parseInt(a);
            int valueB =Integer.parseInt(b);
            switch(comparator){
                case "<":
                    result =  valueA < valueB;
                    break;
                case ">":
                    result =  valueA > valueB;
                    break;
                case "<=":
                    result =  valueA <= valueB;
                    break;
                case ">=":
                    result =  valueA >= valueB;
                    break;
                case "=":
                    result =  valueA == valueB;
                    break;
                default:
                    throw new Exception("Invalid Operator");
            }
        }catch (Exception e){
            int compareToValue = a.toLowerCase().trim().replaceAll("^'|'$","").compareTo(b.toLowerCase().trim().replaceAll("^'|'$",""));
            switch(comparator){
                case "<":
                    result = compareToValue < 0;
                    break;
                case ">":
                    result = compareToValue > 0;
                    break;
                case "<=":
                    result = compareToValue <= 0;
                    break;
                case ">=":
                    result = compareToValue >= 0;
                    break;
                case "=":
                    result = compareToValue == 0;
                    break;
                default:
                    throw new Exception("Invalid Operator:" + comparator);
            }
        }
        return result;
    }
    /**
     * Applies a SimpleClause to check if the row satisfies the given condition.
     *
     * @param clause The SimpleClause object representing the condition.
     * @return True if the row satisfies the condition; otherwise, false.
     */
    public boolean apply(SimpleClause clause) {
        try{
            String leftValue = this.getValueByColumn(clause.getLeft()), rightValue = this.getValueByColumn(clause.getRight());
            return compare(leftValue, rightValue, clause.getOperator());
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    // Methods for copying and manipulation...

    /**
     * Copies values from another row to the current row based on column indexes.
     *
     * @param row The source row from which values are copied.
     */
    public void copyFrom(Row row){
        for(String col: row.columnIndexes.keySet()){
            int rowIndex = row.columnIndexes.get(col);
            int index = this.columnIndexes.get(row.getTableName()+"."+col);
            this.getValues().put(index,row.getValues().get(rowIndex));
        }
    }
    /**
     * Retrieves a list of column aliases for a given index.
     *
     * @param index The column index for which aliases are retrieved.
     * @return A list of column aliases.
     */
    public List<String> getAliasList(int index){
        List<String> strings = new ArrayList<>();
        for(String col:this.columnIndexes.keySet()){
            if(this.columnIndexes.get(col)== index){
                strings.add(col);
            }
        }
        return strings;
    }
    /**
     * Filters the row based on a list of column names.
     *
     * @param columns The list of column names to retain in the row.
     */
    public void filterByColumnNames(List<String> columns){
        HashMap<Integer,String> filteredValues = new HashMap<>();
        HashMap<String,TYPE> filteredColumnTypes = new HashMap<>();
        HashMap<String,Integer> filteredColumnIndexed = new HashMap<>();
        for (String col : columns) {
            col = col.trim();
            int index = this.columnIndexes.getOrDefault(col, -1);
            List<String> cols = getAliasList(index);
            for(String selectedCol: cols){
                filteredColumnIndexed.put(selectedCol.trim(),index);
                filteredColumnTypes.put(selectedCol.trim(),this.columnTypes.get(selectedCol));
            }
            filteredValues.put(index,this.getValueByColumn(col));
        }
        this.values = filteredValues;
        this.columnTypes = filteredColumnTypes;
        this.columnIndexes = filteredColumnIndexed;
    }
    /**
     * Retrieves the value of a specified column.
     *
     * @param col The name of the column for which the value is retrieved.
     * @return The value of the specified column.
     */
    public String getValueByColumn(String col){
        Integer columnIndex = this.columnIndexes.getOrDefault(col,-1);
        if(columnIndex == -1){
            return col;
        }
        return this.values.getOrDefault(columnIndex,col);
    }
    /**
     * Sets the value of a specified column.
     *
     * @param col   The name of the column for which the value is set.
     * @param value The new value for the specified column.
     */
    public void setValueByColumn(String col,String value){
        Integer columnIndex = this.columnIndexes.get(col);
        this.values.put(columnIndex,value);
    }
    // Overridden methods...

    /**
     * Checks if two rows are equal based on their values and annotations.
     *
     * @param o The object to compare with.
     * @return True if the rows are equal; otherwise, false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Row row = (Row) o;
        return Objects.equals(values, row.values);
    }
    /**
     * Generates a hash code based on values and annotations.
     *
     * @return The hash code for the row.
     */
    @Override
    public int hashCode() {
        return Objects.hash(values, columnTypes, getAnnotation());
    }
    /**
     * Generates a string representation of the row.
     *
     * @return A string containing column names and their values, along with annotations.
     */
    @Override
    public String toString() {
        String rawString = "{ ";
        HashMap<Integer,Boolean> selectedString = new HashMap<>();
        for (String col: this.columnTypes.keySet()){
            if(!selectedString.getOrDefault(this.columnIndexes.get(col), false)){
                rawString += String.format("%s=%s, ",col,this.getValueByColumn(col));
                selectedString.put(this.columnIndexes.get(col),true);
            }
        }
        return rawString.replaceAll(", $","")+" }" + String.format(" => Appearance: %d => (%s)",this.annotations.values().size(),getFinalAnnotation());
    }
    /**
     * Retrieves the name of the table to which the row belongs.
     *
     * @return The name of the table.
     */
    public String getTableName() {
        return tableName;
    }
}
