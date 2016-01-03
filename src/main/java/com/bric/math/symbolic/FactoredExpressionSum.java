/*
 * @(#)FactoredExpressionSum.java
 *
 * $Date: 2014-11-27 07:55:25 +0100 (Do, 27 Nov 2014) $
 *
 * Copyright (c) 2014 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Jeremy Wood. For details see accompanying license terms.
 * 
 * This software is probably, but not necessarily, discussed here:
 * https://javagraphics.java.net/
 * 
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 */
package com.bric.math.symbolic;

import java.util.ArrayList;
import java.util.List;

/** An immutable sum of several {@link com.bric.math.symbolic.FactoredExpression} objects.
 * 
 */
public class FactoredExpressionSum {
	List<FactoredExpression> list = new ArrayList<FactoredExpression>();
	
	/** Create a FactoredExpressionSum.
	 * 
	 * @param e several FactoredExpressions to add together.
	 */
	public FactoredExpressionSum(FactoredExpression... e) {
		for(FactoredExpression t : e) {
			list.add(t);
		}
	}
	
	
	@Override
	public String toString() {
		return toString(OutputFormat.PLAIN);
	}

	/** Create a String representation of this FactoredExpressionSum.
	 * 
	 * @param format the preferred output format.
	 * @return a String representation of this FactoredExpressionSum.
	 */
	public String toString(OutputFormat format) {
		StringBuffer sb = new StringBuffer();
		for(int a = 0; a<list.size(); a++) {
			String s = list.get(a).toString(format);
			if(sb.length()>0) {
				if(!s.startsWith("-")) {
					sb.append("+");
				}
			}
			
			sb.append( s );
		}
		return sb.toString();
	}

	/** Multiply all the FactoredExpressions in this object by the operand.
	 * 
	 * @param expression the operand to multiply this by.
	 * @return a FactoredExpressionSum that is the product of this object and the argument.
	 */
	public FactoredExpressionSum multiply(Expression expression) {
		if(expression.equals(new Expression(1)))
			return this;
		
		List<FactoredExpression> list2 = new ArrayList<FactoredExpression>();
		for(FactoredExpression e : list) {
			list2.add( e.multiply(expression) );
		}
		return new FactoredExpressionSum( list2.toArray(new FactoredExpression[ list2.size() ] ) );
	}

	/** Add a FactoredExpression to this object.
	 * 
	 * @param operand the operand to add to this sum.
	 * @return a FactoredExpressionSum that is the sum of this object and the argument.
	 */
	public FactoredExpressionSum add(FactoredExpressionSum operand) {
		List<FactoredExpression> l = new ArrayList<FactoredExpression>();
		l.addAll(list);
		l.addAll(operand.list);
		return new FactoredExpressionSum( l.toArray(new FactoredExpression[l.size()] ) );
	}

	/** Flatten this FactoredExpressionSum to a single Expression.
	 * 
	 * @return an Expression that includes all the multiplication represented
	 * in this object.
	 */
	public Expression toExpression() {
		Expression sum = new Expression();
		for(FactoredExpression f : list) {
			sum = sum.add( f.toExpression() );
		}
		return sum;
	}
}
