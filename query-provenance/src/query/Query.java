package query;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
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
                    .getConnection("jdbc:postgresql://localhost:5432/concordia",
                            "postgres", "6563");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
    }
    private String queryString;
    private Set<String> relations;
    private Map<String, Set<String>> schema;
    private Set<Clause> clauses;
    private Table projection;
    private List<String> selectedColumns;
    public Query(String queryString) {
        this.queryString = queryString;
        this.schema = new HashMap<>();
        this.clauses = new HashSet<>();
        this.relations = new HashSet<>();
        this.selectedColumns = new ArrayList<>();
        this.generateSchema();
        this.generateProjections();
        this.filterClauses();
        this.projection = new Table((String) relations.toArray()[0],(String) relations.toArray()[1],"product","product_id");
        this.projection.filterByClauses(this.clauses);
        this.projection.filterProjection(this.selectedColumns);
        this.projection.aggregate();
    }

    private void execute(){
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format("select * from " + String.join(",",this.relations)));

        } catch (SQLException e) {
            throw new RuntimeException(e);
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
            String fromPattern = "FROM\\s+(.*?)\\s+WHERE";
            Pattern fromRegex = Pattern.compile(fromPattern);
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
            throw new RuntimeException(e);
        }
    }
    private void filterClauses(){
        String wherePattern = "WHERE\\s+(.*)$";
        Pattern whereRegex = Pattern.compile(wherePattern, Pattern.CASE_INSENSITIVE);
        Matcher whereMatcher = whereRegex.matcher(this.queryString);
        List<String> tokens = new ArrayList<>();
        if (whereMatcher.find()) {
            String whereClause = whereMatcher.group(1);
            for (String token: whereClause.split(" ")){
                if(!"and-or".contains(token.toLowerCase())){
                    tokens.add(token);
                }
            }
        }
        for(int i=0;i<tokens.size();i+=3){
            String left = tokens.get(i) , operator = tokens.get(i +  1), right = tokens.get(i + 2);
            Clause clause = new Clause(operator,left,right);
            this.clauses.add(clause);
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public Table getProjection() {
        return projection;
    }
}
