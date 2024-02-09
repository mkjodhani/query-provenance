package query;

import database.DatabaseConnection;
import query.clause.ComplexClause;
import query.clause.SimpleClause;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Query class represents a module for processing and analyzing SQL queries.
 * It includes functionality for validation, schema generation, clause filtering,
 * and result projection.
 *
 * @author mkjodhani
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
    /**
     * Validates whether the provided SQL query is syntactically correct and can be executed.
     *
     * @param query The SQL query to be validated.
     * @return True if the query is valid; otherwise, false.
     */
    private static boolean isValid(String query){
        try{
            Statement statement = DatabaseConnection.getConnection().createStatement();
            statement.execute(query);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    /**
     * Constructs a Query object by processing the provided SQL query.
     *
     * @param queryString The SQL query to be processed.
     * @throws SQLException If the SQL query is invalid.
     */
    public Query(String queryString) throws SQLException {
        if(!isValid(queryString)){
            throw new SQLException("Invalid SQL query!");
        }
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
    /**
     * Parses the SELECT clause of the SQL query to extract selected columns.
     */
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
    /**
     * Parses the FROM clause of the SQL query to generate table schema information.
     *
     * @throws SQLException If an SQL exception occurs during schema generation.
     */
    private void generateSchema() throws SQLException  {
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
    }
    /**
     * Parses the WHERE clause of the SQL query to filter and create SimpleClause objects.
     */
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
    /**
     * Finds the conjunction (AND or OR) in a given WHERE clause.
     *
     * @param query The WHERE clause to search for conjunctions.
     * @return The found conjunction or null if none.
     */
    private String findConjuction(String query){
        Matcher matcher = SimpleClause.conjucationPattern.matcher(query);
        if(matcher.find()){
            return matcher.group(0);
        }else{
            return null;
        }
    }
    /**
     * Finds the comparison operator in a given condition.
     *
     * @param condition The condition to search for the comparator.
     * @return The found comparator or null if none.
     */
    private String findComparator(String condition){
        Matcher matcher = SimpleClause.comparatorPattern.matcher(condition);
        if(matcher.find()){
            return matcher.group(0);
        }else{
            return null;
        }
    }
    /**
     * Adds table names to column names to distinguish them uniquely.
     *
     * @param col The column name to be modified.
     * @return The modified column name with table names.
     */
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
        }
    }
    /**
     * Retrieves the result of the query projection.
     *
     * @return The result table after projection, represented by a Table object.
     */
    public Table getProjection() {
        return projection;
    }
    /**
     * Retrieves the execution time of the query in milliseconds.
     *
     * @return The time taken for the execution of the SQL query, measured in milliseconds.
     */
    public long getExecutionTime() {
        return executionTime;
    }
}
