/*
 * @(#)FactoredExpression.java
 *
 * $Date: 2015-12-27 03:42:44 +0100 (So, 27 Dez 2015) $
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** An immutable series of Expressions that are multiplied together.
 * <p>So if an Expression is something "a+b" or "c-d" then
 * this object can represent "(a+b)(c-d)". This object can be flattened
 * into a single Expression, but for complicated factors this is a much
 * more efficient way to store an expression.
 */
public class FactoredExpression {
	Map<Expression, Integer> map = new HashMap<Expression, Integer>();
	List<Expression> list = new ArrayList<Expression>();
	long coefficient;
	
	private static Comparator<Expression> expressionComparator = new Comparator<Expression>() {

		@Override
		public int compare(Expression o1, Expression o2) {
			int size1 = o1.getTermCount();
			int size2 = o2.getTermCount();
			if(size1<size2) return -1;
			if(size1>size2) return 1;
			for(int a = 0; a<size1; a++) {
				Term t1 = o1.getTerm(a);
				Term t2 = o2.getTerm(a);
				int k = t1.compareTo(t2, true);
				if(k!=0) return k;
			}
			return 0;
		}
		
	};

	/** Create a FactoredExpression that multiplies several Expressions together.
	 * 
	 * @param exp the Expressions to multiply to create this FactoredExpression.
	 */
	public FactoredExpression(Expression... exp) {
		this(1, exp);
	}

	/** Create a FactoredExpression that multiplies several Expressions together.
	 * 
	 * @param coefficient the coefficient to use with this FactoredExpression.
	 * @param exp the Expressions to multiply to create this FactoredExpression.
	 */
	public FactoredExpression(long coefficient,Expression... exp) {
		boolean containsZero = coefficient==0;
		for(Expression e : exp) {
			if(e.terms.length==0) {
				containsZero = true;
			}
		}
		
		if(!containsZero) {
			this.coefficient = coefficient;
			for(Expression e : exp) {
				list.add(e);
				Integer i = map.get(e);
				if(i==null) {
					map.put(e, 1);
				} else {
					map.put(e, i+1);
				}
			}
		} else {
			this.coefficient = 0;
		}
		Collections.sort(list, expressionComparator );
	}
	
	@Override
	public int hashCode() {
		return list.hashCode();
	}

	@Override
	public boolean equals(Object t) {
		if(!(t instanceof FactoredExpression))
			return false;
		//TODO: improve efficiency
		return this.toString().equals(t.toString());
	}

	@Override
	public String toString() {
		return toString(OutputFormat.PLAIN);
	}

	/** Create a String representation of this FactoredExpression.
	 * 
	 * @param outputFormat the preferred output format.
	 * @return a String representation of this FactoredExpression.
	 */
	public String toString(OutputFormat outputFormat) {
		StringBuffer sb = new StringBuffer();
		if(coefficient!=1) {
			sb.append( coefficient );
			if(OutputFormat.LATEX==outputFormat) {
				sb.append("\\,");
			} else {
				sb.append("*");
			}
		}
		Set<Expression> handled = new HashSet<Expression>();
		for(int a = 0; a<list.size(); a++) {
			Expression e = list.get(a);
			if(handled.add(e)) {
				if(a>0) {
					if(OutputFormat.LATEX==outputFormat) {
						sb.append("\\,");
					} else {
						sb.append("*");
					}
				}
				
				if(e.getTermCount()>1) {
					sb.append("("+e.toString(outputFormat)+")");
				} else {
					sb.append(e.toString(outputFormat));
				}
				Integer power = map.get(e);
				if(power>1) {
					if(OutputFormat.LATEX==outputFormat) {
						sb.append("^{"+power+"}");
					} else {
						sb.append("^"+power);
					}
				}
			}
		}
		return sb.toString();
	}

	/** Multipler this by another Expression.
	 * 
	 * @param expression another Expression to multiply this object by.
	 * @return the product of this FactoredExpression with the operand.
	 */
	public FactoredExpression multiply(Expression expression) {
		List<Expression> list2 = new ArrayList<Expression>();
		list2.addAll(list);
		list2.add(expression);
		return new FactoredExpression(coefficient, list2.toArray(new Expression[list2.size()]));
	}

	/** Flatten this FactoredExpression to a single Expression.
	 * 
	 * @return an Expression that includes all the multiplication represented
	 * in this object.
	 */
	public Expression toExpression() {
		Expression product = new Expression(coefficient);
		for(Expression e : list) {
			product = product.multiply(e);
		}
		return product;
	}
	
	/** Return the coefficient of this FactoredExpression.
	 * 
	 * @return the coefficient of this FactoredExpression.
	 */
	public long getCoefficient() {
		return coefficient;
	}

	/** Return a FactoredExpression that uses all the same Expressions but has a coefficient of 1.
	 * 
	 * @return a FactoredExpression that uses all the same Expressions but has a coefficient of 1.
	 */
	public FactoredExpression removeCoefficient() {
		return new FactoredExpression( list.toArray(new Expression[list.size()]) );
	}

	/** Multiply this FactoredExpression by an operand.
	 * 
	 * @param operand the operand to multiply this value's coefficient by.
	 * @return a new FactoredExpression that is the product of this object and the operand.
	 */
	public FactoredExpression multiply(long operand) {
		return new FactoredExpression( coefficient*operand, list.toArray(new Expression[list.size()]) );
	}
}
