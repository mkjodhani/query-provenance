package query.clause;

/**
 * The ComplexClause class represents a complex condition in a query, allowing the use of conjunctions (AND, OR)
 * to combine multiple conditions.
 * <p>
 * It is a part of the query package and provides an enum for specifying the conjunction type (AND or OR).
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * {@code
 * // Creating a ComplexClause with AND conjunction:
 * ComplexClause complexAndClause = new ComplexClause(CONJUNCTION.AND);
 *
 * // Creating a ComplexClause with OR conjunction:
 * ComplexClause complexOrClause = new ComplexClause(CONJUNCTION.OR);
 * }
 * </pre>
 *
 * <p><b>Note:</b> The class is intended to be used in conjunction with other conditions to build complex queries.</p>
 *
 * @author mkjodhani
 * @version 1.0
 * @since 17/10/23
 */
public class ComplexClause {
    /**
     * Enumeration representing the conjunction type (AND or OR) for combining conditions.
     */
    public enum CONJUNCTION {
        AND,
        OR
    }
}
