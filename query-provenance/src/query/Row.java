package query;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author mkjodhani
 * @version 1
 * @project Query Provenance
 * @since 15/10/23
 */

public class Row {
    public enum TYPE{
        STRING,
        DIGIT
    }
    private HashMap<String,String> values;
    HashMap<String,TYPE> columnTypes;
    private String annotation;

    private String tableName;

    public Row(HashMap<String, TYPE> columnTypes,String tableName) {
        this.columnTypes = columnTypes;
        this.values = new HashMap<>();
        this.tableName = tableName;
    }

    public HashMap<String, String> getValues() {
        return values;
    }

    public void setValues(HashMap<String, String> values) {
        this.values = values;
    }

    public HashMap<String, TYPE> getColumnTypes() {
        return columnTypes;
    }

    public void setColumnTypes(HashMap<String, TYPE> columnTypes) {
        this.columnTypes = columnTypes;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }
    public void put(String key,String value){
        values.put(key,value);
    }

    @Override
    public String toString() {
        return values + String.format(" => (%s)",getAnnotation());
    }

    private boolean compare(String a, String b, String comparator) throws Exception {
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
            int compareToValue = a.toLowerCase().compareTo(b.toLowerCase());
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
    public boolean apply(Clause clause,boolean single) {
        try{
            String leftValue = values.getOrDefault(clause.getLeft(),null), rightValue = values.getOrDefault(clause.getRight(),null);
            leftValue = leftValue != null ? leftValue : clause.getLeft();
            rightValue = rightValue != null ? rightValue : clause.getRight();
            return compare(leftValue != null ? leftValue : clause.getLeft(), rightValue != null ? rightValue : clause.getRight(), clause.getOperator());
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public void copyFrom(Row row,String table){
        for(String col: row.getValues().keySet()){
            String colName = String.format("%s.%s",table,col);
            this.getValues().put(colName,row.getValues().get(col));
        }
    }

    public void filterByColumnNames(List<String> columns){
        HashMap<String,String> filteredValues = new HashMap<>();
        HashMap<String,TYPE> filteredColumnTypes = new HashMap<>();
        for (String col : columns) {
            filteredValues.put(col,this.values.get(col));
            filteredColumnTypes.put(col,this.columnTypes.get(col));
        }
        this.values = filteredValues;
        this.columnTypes = filteredColumnTypes;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Row row = (Row) o;
        return Objects.equals(values, row.values) && Objects.equals(columnTypes, row.columnTypes) && Objects.equals(annotation, row.annotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values, columnTypes, annotation);
    }
}
