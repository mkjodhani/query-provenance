package query;

import query.clause.SimpleClause;

import java.util.*;

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
    private HashMap<Integer,String> values;
    private HashMap<String, Integer> columnIndexes;
    private HashMap<String,TYPE> columnTypes;
    private HashMap<String,Integer> annotations;

    private String tableName;

    public Row(HashMap<String, TYPE> columnTypes,HashMap<String, Integer> columnIndexes,String tableName) {
        this.columnTypes = columnTypes;
        this.values = new HashMap<>();
        this.tableName = tableName;
        this.annotations = new HashMap<>();
        this.columnIndexes = columnIndexes;
    }
    public HashMap<Integer, String> getValues() {
        return values;
    }

    public HashMap<String, Integer> getAnnotationsMap() {
        return annotations;
    }

    public String getAnnotation() {
        String annotation = "";
        for(String ann: this.annotations.keySet()){
            annotation +=  String.format("%s%s",this.annotations.getOrDefault(ann,0) <=1 ? "" : String.valueOf(this.annotations.getOrDefault(ann,0)),ann);
        }
        return annotation;
    }
    public String getFinalAnnotation() {
        String annotation = "";
        for(String ann: this.annotations.keySet()){
            annotation +=  String.format("%s%s",this.annotations.getOrDefault(ann,0) <=1 ? "" : String.valueOf(this.annotations.getOrDefault(ann,0)),ann) + " + ";
        }
        return annotation.replaceAll(" \\+ $","");
    }
    public void setAnnotation(String ann) {
        int updatedValue = this.annotations.getOrDefault(ann,0) + 1;
        this.annotations.put(ann,updatedValue);
    }
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
    public boolean apply(SimpleClause clause) {
        try{
            String leftValue = this.getValueByColumn(clause.getLeft()), rightValue = this.getValueByColumn(clause.getRight());
            return compare(leftValue, rightValue, clause.getOperator());
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public void copyFrom(Row row){
        for(String col: row.columnIndexes.keySet()){
            int rowIndex = row.columnIndexes.get(col);
            int index = this.columnIndexes.get(row.getTableName()+"."+col);
            this.getValues().put(index,row.getValues().get(rowIndex));
        }
    }
    public List<String> getAliasList(int index){
        List<String> strings = new ArrayList<>();
        for(String col:this.columnIndexes.keySet()){
            if(this.columnIndexes.get(col)== index){
                strings.add(col);
            }
        }
        return strings;
    }
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

    public String getValueByColumn(String col){
        Integer columnIndex = this.columnIndexes.getOrDefault(col,-1);
        if(columnIndex == -1){
            return col;
        }
        return this.values.getOrDefault(columnIndex,col);
    }
    public void setValueByColumn(String col,String value){
        Integer columnIndex = this.columnIndexes.get(col);
        this.values.put(columnIndex,value);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Row row = (Row) o;
        return Objects.equals(values, row.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values, columnTypes, getAnnotation());
    }

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
    public String getTableName() {
        return tableName;
    }
}
