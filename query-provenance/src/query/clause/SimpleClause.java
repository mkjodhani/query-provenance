package query.clause;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The SimpleClause class represents a basic condition in a query, consisting of an operator, left-hand side,
 * and right-hand side. It is part of the query.clause package and is intended for building simple query conditions.
 *
 * <p>
 * The class includes patterns for recognizing comparators and conjunctions in query strings.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * {@code
 * // Creating a SimpleClause:
 * SimpleClause simpleClause = new SimpleClause("=", "column", "value");
 * }
 * </pre>
 *
 * <p><b>Note:</b> The class is designed for basic query conditions and is typically used within a ComplexClause
 * for building complex queries.</p>
 *
 * @author mkjodhani
 * @version 1
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
    /**
     * Constructs a SimpleClause with the specified operator, left-hand side, and right-hand side.
     *
     * @param operator The operator for the condition (e.g., '=', '{@literal >}', 'IN').
     * @param left     The left-hand side of the condition.
     * @param right    The right-hand side of the condition.
     */
    public SimpleClause(String operator, String left, String right) {
        this.operator = operator.replaceAll(" ", "");
        this.left = left.replaceAll("[-+^' ]*","");
        this.right = right.replaceAll("[-+^' ]*","");
    }
    /**
     * Gets the conjunction type associated with this SimpleClause.
     *
     * @return The conjunction type (AND or OR).
     */
    public ComplexClause.CONJUNCTION getConjunction() {
        return conjunction;
    }
    /**
     * Sets the conjunction type associated with this SimpleClause.
     *
     * @param conjunction The conjunction type (AND or OR).
     */
    public void setConjunction(ComplexClause.CONJUNCTION conjunction) {
        this.conjunction = conjunction;
    }
    /**
     * Returns a string representation of the SimpleClause.
     *
     * @return A string representing the SimpleClause.
     */
    @Override
    public String toString() {
        return String.format("Clause {%s %s %s}",left,operator,right);
    }
    /**
     * Checks if this SimpleClause is equal to another object.
     *
     * @param o The object to compare.
     * @return True if equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleClause clause = (SimpleClause) o;
        return Objects.equals(operator, clause.operator) && Objects.equals(left, clause.left) && Objects.equals(right, clause.right);
    }
    /**
     * Generates a hash code for the SimpleClause.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(operator, left, right);
    }
    /**
     * Gets the operator of the SimpleClause.
     *
     * @return The operator.
     */
    public String getOperator() {
        return operator;
    }
    /**
     * Gets the left-hand side of the SimpleClause.
     *
     * @return The left-hand side.
     */
    public String getLeft() {
        return left;
    }
    /**
     * Gets the right-hand side of the SimpleClause.
     *
     * @return The right-hand side.
     */
    public String getRight() {
        return right;
    }
}