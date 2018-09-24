package com.github.collinalpert.java2db.functions;

import com.trigersoft.jaque.expression.LambdaExpression;

/**
 * A utility class for converting java lambdas to SQL.
 */
public class Lambda2Sql {

	/**
	 * Converts a lambda lambda expression to SQL. <br>
	 * <pre>{@code person -> person.getAge() > 50 && person.isActive() }</pre>
	 * Becomes a string:
	 * <pre>{@code "age > 50 AND active" }</pre>
	 * Supported operators: &gt;,&gt;=,&lt;,&lt;=,=,!=,&amp;&amp;,||,!
	 *
	 * @param functionalInterface The lambda to transform.
	 * @return A String representing the lambda as an SQL where condition.
	 */
	public static String toSql(SerializedFunctionalInterface functionalInterface) {
		var lambdaExpression = LambdaExpression.parse(functionalInterface);
		return lambdaExpression.accept(new SqlConverter()).toString();
	}
}