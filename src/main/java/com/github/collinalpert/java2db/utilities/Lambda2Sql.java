package com.github.collinalpert.java2db.utilities;

import com.trigersoft.jaque.expression.LambdaExpression;

/**
 * A utility class for converting java lambdas to SQL.
 */
public class Lambda2Sql {

	/**
	 * Converts a predicate lambda to SQL. <br>
	 * <pre>{@code person -> person.getAge() > 50 && person.isActive() }</pre>
	 * Becomes a string:
	 * <pre>{@code "age > 50 AND active" }</pre>
	 * Supported operators: &gt;,&gt;=,&lt;,&lt;=,=,!=,&amp;&amp;,||,!
	 *
	 * @param predicate The {@link SqlPredicate} to transform.
	 * @param <T> The type of the predicate.
	 * @return A String representing the predicate as an SQL where condition.
	 */
	public static <T> String toSql(SqlPredicate<T> predicate) {
		var lambdaExpression = LambdaExpression.parse(predicate);
		return lambdaExpression.accept(new SqlConverter()).toString();
	}
}