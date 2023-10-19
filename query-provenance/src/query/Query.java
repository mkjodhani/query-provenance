package query;

import database.DatabaseConnection;
import query.clause.ComplexClause;
import query.clause.SimpleClause;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mkjodhani
 * @project Query Provenance
 * @since 15/10/23
 * @version 1
 */

public class Query {
    private String queryString;
    private List<String> relations;
    private Map<String, Set<String>> schema;
    private List<SimpleClause> clauses;
    private Table projection;
    private List<String> selectedColumns;
    private long executionTime ;

    public Query(String queryString) {
        this.queryString = queryString;
        this.schema = new HashMap<>();
        this.clauses = new ArrayList<>();
        this.relations = new ArrayList<>();
        this.selectedColumns = new ArrayList<>();
        this.generateSchema();
        this.generateProjections();
        this.filterClauses();
        if(this.relations.size() > 0){
            Table a = new Table((String) relations.toArray()[0]);
            Table b = new Table((String) relations.toArray()[1]);
            long startTime = System.currentTimeMillis(); // or System.nanoTime()
            if(this.relations.size() > 1){
                this.projection = a.join(b,this.clauses);
            }
            this.projection.filterProjection(this.selectedColumns);
            this.projection.aggregate();
            executionTime = System.currentTimeMillis() - startTime;
        }
    }

    private void generateProjections(){
        String selectPattern = "SELECT\\s+(.*?)\\s+FROM";
        Pattern selectRegex = Pattern.compile(selectPattern, Pattern.CASE_INSENSITIVE);
        Matcher selectMatcher = selectRegex.matcher(this.queryString);
        if (selectMatcher.find()) {
            String selectClause = selectMatcher.group(1);
            String[] columns = selectClause.split(",");
            this.selectedColumns.addAll(Arrays.asList(columns));
        }
    }
    private void generateSchema()  {
        try {
            String fromPattern = this.queryString.toLowerCase().contains("where") ? "\\sFROM\\s+(.*?)\\s+WHERE" : "\\sFROM\\s+(.*?)\\s*$";
            Pattern fromRegex = Pattern.compile(fromPattern,Pattern.CASE_INSENSITIVE);
            Matcher fromMatcher = fromRegex.matcher(this.queryString);
            if ( fromMatcher.find()) {
                String tableName = fromMatcher.group(1);
                for (String table: tableName.split(",")){
                    this.relations.add(table.trim());
                    Set<String> columns = new HashSet<>();
                    Statement statement = null;
                    statement = DatabaseConnection.getConnection().createStatement();
                    ResultSet resultSet =  statement.executeQuery("select * from "+table);

                    // Get the metadata of the result set, which contains information about the columns
                    ResultSetMetaData metaData = resultSet.getMetaData();

                    // Get the number of columns
                    int columnCount = metaData.getColumnCount();

                    // Iterate through the columns and print their names
                    for (int i = 1; i <= columnCount; i++) {
                        columns.add(metaData.getColumnName(i));
                    }
                    schema.put(table,columns);
                }
            } else {
                System.out.println("Invalid SQL query format.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    private void filterClauses(){
        String wherePattern = "WHERE\\s+(.*)$";
        Pattern whereRegex = Pattern.compile(wherePattern, Pattern.CASE_INSENSITIVE);
        Matcher whereMatcher = whereRegex.matcher(this.queryString);
        if (whereMatcher.find()) {
            String whereClause = whereMatcher.group(1);
            for (String clause: whereClause.split(" (and)?(AND) ")){
                for(String semiClause: clause.split(" (or)?(OR) ")){
                    String operator = findComparator(semiClause);
                    String[] operands = semiClause.split(operator);
                    String left = getColumnNameWithTable(operands[0]) , right = getColumnNameWithTable(operands[1]);
                    SimpleClause clauseObj = new SimpleClause(operator,left,right);
                    String conjuction = findConjuction(whereClause);
                    if(conjuction != null){
                        clauseObj.setConjunction(conjuction.toLowerCase().contains("and") == true ? ComplexClause.CONJUNCTION.AND : ComplexClause.CONJUNCTION.OR);
                    }
                    this.clauses.add(clauseObj);
                }
            }
        }
    }
    private String findConjuction(String query){
        Matcher matcher = SimpleClause.conjucationPattern.matcher(query);
        if(matcher.find()){
            return matcher.group(0);
        }else{
            return null;
        }
    }
    private String findComparator(String condition){
        Matcher matcher = SimpleClause.comparatorPattern.matcher(condition);
        if(matcher.find()){
            return matcher.group(0);
        }else{
            return null;
        }
    }
    public String getColumnNameWithTable(String col){
        String relations = "";
        for(String table: this.relations){
            relations += "[" +table +"]";
        }
        Pattern pattern = Pattern.compile("^("+relations+"\\.)");
        Matcher matcher = pattern.matcher(col);
        if(matcher.find()){
            return col;
        }else{
            return  col;
//            if(this.projection.columnTypesA.getOrDefault(col,null) != null){
//                return this.projection.tableNameA + "." + col;
//            }else if(this.projection.columnTypesB.getOrDefault(col,null) != null){
//                return this.projection.tableNameB + "." + col;
//            }else{
//                return col;
//            }
        }
    }
    public Table getProjection() {
        return projection;
    }
    public long getExecutionTime() {
        return executionTime;
    }
}
