package query.clause;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author mkjodhani
 * @version 1
 * @project Query Provenance
 * @since 15/10/23
 */
public class SimpleClause {
    ComplexClause.CONJUNCTION conjunction;
    private String operator, left, right;
    public static Pattern comparatorPattern;
    public static Pattern conjucationPattern;
    static{
        comparatorPattern = Pattern.compile("( [<>=(in)(IN)]+ )");
        conjucationPattern = Pattern.compile("( (AND)?(and)?(OR)?(OR)? )");
    }

    public SimpleClause(String operator, String left, String right) {
        this.operator = operator.replaceAll(" ", "");
        this.left = left.replaceAll("[-+^' ]*","");
        this.right = right.replaceAll("[-+^' ]*","");
    }

    public ComplexClause.CONJUNCTION getConjunction() {
        return conjunction;
    }

    public void setConjunction(ComplexClause.CONJUNCTION conjunction) {
        this.conjunction = conjunction;
    }

    @Override
    public String toString() {
        return String.format("Clause {%s %s %s}",left,operator,right);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleClause clause = (SimpleClause) o;
        return Objects.equals(operator, clause.operator) && Objects.equals(left, clause.left) && Objects.equals(right, clause.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, left, right);
    }

    public String getOperator() {
        return operator;
    }

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }
}