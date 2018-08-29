package de.java2db.utilities;

import com.trigersoft.jaque.expression.BinaryExpression;
import com.trigersoft.jaque.expression.ConstantExpression;
import com.trigersoft.jaque.expression.Expression;
import com.trigersoft.jaque.expression.ExpressionType;
import com.trigersoft.jaque.expression.ExpressionVisitor;
import com.trigersoft.jaque.expression.InvocationExpression;
import com.trigersoft.jaque.expression.LambdaExpression;
import com.trigersoft.jaque.expression.MemberExpression;
import com.trigersoft.jaque.expression.ParameterExpression;
import com.trigersoft.jaque.expression.UnaryExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * This code is taken and modified from the
 * <a href="https://github.com/ajermakovics/lambda2sql">lambda2sql</a> GitHub repository.
 */
public class SqlConverter implements ExpressionVisitor<StringBuilder> {

	private StringBuilder sb = new StringBuilder();
	private Expression body;
	private List<ConstantExpression> parameters = new ArrayList<>();

	private static String toSqlOp(int expressionType) {
		switch (expressionType) {
			case ExpressionType.Equal:
				return "=";
			case ExpressionType.LogicalAnd:
				return "AND";
			case ExpressionType.LogicalOr:
				return "OR";
			case ExpressionType.Convert:
				return "";
		}
		return ExpressionType.toString(expressionType);
	}

	@Override
	public StringBuilder visit(BinaryExpression e) {
		var quote = e != body && e.getExpressionType() == ExpressionType.LogicalOr;

		if (quote) sb.append('(');

		e.getFirst().accept(this);
		sb.append(' ').append(toSqlOp(e.getExpressionType())).append(' ');
		e.getSecond().accept(this);

		if (quote) sb.append(')');

		return sb;
	}

	@Override
	public StringBuilder visit(ConstantExpression e) {
		if (e.getValue() instanceof String) {
			return sb.append("'").append(e.getValue().toString()).append("'");
		}
		return sb.append(e.getValue().toString());
	}

	@Override
	public StringBuilder visit(InvocationExpression e) {
		e.getArguments().stream().filter(x -> x instanceof ConstantExpression).forEach(x -> parameters.add((ConstantExpression) x));
		return e.getTarget().accept(this);
	}

	@Override
	public StringBuilder visit(LambdaExpression<?> e) {
		this.body = e.getBody();
		return body.accept(this);
	}

	@Override
	public StringBuilder visit(MemberExpression e) {
		var name = e.getMember().getName();
		name = name.replaceAll("^(get)", "");
		name = name.substring(0, 1).toLowerCase() + name.substring(1);

		return sb.append(name);
	}

	@Override
	public StringBuilder visit(ParameterExpression e) {
		parameters.get(e.getIndex()).accept(this);
		return sb;
	}

	@Override
	public StringBuilder visit(UnaryExpression e) {
		sb.append(toSqlOp(e.getExpressionType()));
		return e.getFirst().accept(this);
	}

}