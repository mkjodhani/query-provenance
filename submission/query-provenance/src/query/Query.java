package query;

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
    private static Connection connection = null;
    static {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/postgres",
                            "postgres", "6563");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
    }
    private String queryString;
    private List<String> relations;
    private Map<String, Set<String>> schema;
    private List<SimpleClause> clauses;
    private Table projection;
    private List<String> selectedColumns;
    private long executionTime ;
    public Query(String queryString) {
//        System.out.println("------------------------------------------------------------------------------------------------------------------------------");
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
//            this.projection.filterByClauses(this.clauses);
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
                    statement = connection.createStatement();
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
    public String getRootColumnName(String col){
        String relations = "";
        for(String table: this.relations){
            relations += "[" +table +"]";
        }
        Pattern pattern = Pattern.compile("^("+relations+"\\.)");
        Matcher matcher = pattern.matcher(col);
        if(matcher.find()){
            return col.replace(matcher.group(1),"");
        }
        return col;
    }
    public static Connection getConnection() {
        return connection;
    }

    public Table getProjection() {
        return projection;
    }
    public  int getSize(){
        return this.projection.rows.size();
    }

    public long getExecutionTime() {
        return executionTime;
    }
    public void showAnalysis(){
        System.out.println("Resulted Row ::" + String.valueOf(this.getSize()));
        System.out.println("Execution Time ::" + this.getExecutionTime() + " milliseconds");
//        HashMap<String,String> values = new HashMap<>();
//        values.put("Query :",this.queryString);
//        values.put("Resulted Row",String.valueOf(this.getSize()));
//        values.put("Execution Time",String.valueOf(this.getExecutionTime()));
//        printHashMapInTable(values);
    }
    public static void printHashMapInTable(HashMap<?, ?> hashMap) {
        System.out.println("+-----------------------+");
        System.out.println("| Variation   |  Time   |");
        System.out.println("+-----------------------+");

        for (Map.Entry<?, ?> entry : hashMap.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();

            int keyPadding = 12 - key.length();
            int valuePadding = 6 - value.length();

            StringBuilder row = new StringBuilder("| ");
            row.append(key);
            for (int i = 0; i < keyPadding; i++) {
                row.append(" ");
            }
            row.append(" | ");
            row.append(value);
            for (int i = 0; i < valuePadding; i++) {
                row.append(" ");
            }
            row.append(" |");

            System.out.println(row.toString());
        }

        System.out.println("+-----------------------+");
    }

}
