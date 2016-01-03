/*
 * @(#)Definition.java
 *
 * $Date: 2015-12-27 03:42:44 +0100 (So, 27 Dez 2015) $
 *
 * Copyright (c) 2013 by Jeremy Wood.
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

import java.util.List;

/** A variable definition.
 *
 * See {@link com.bric.math.symbolic.Expression#substitute(Definition...)}.
 */
public class Definition implements Comparable<Definition> {

	/** The left hand side of this equation. */
	public final String variable;

	/** The right hand side of this equation. */
	public final Expression expression;
	
	/** Create a new Definition.
	 * 
	 * @param variable the variable being defined.
	 * @param expression what this variable is equivalent to.
	 */
	public Definition(String variable,Expression expression) {
		
		//while technically this is a kosher equation, it is not
		//how the Definition class is meant to be used
		if(expression.getVariables().contains(variable))
			throw new IllegalArgumentException("this expression contains \""+variable+"\"");
		
		this.variable = variable;
		this.expression = expression;
	}
	
	public Definition(String variable,List<Term> expression) {
		this(variable, new Expression(expression));
	}

	@Override
	public String toString() {
		return toString(OutputFormat.PLAIN);
	}

	/** Create a String representation of this Expression.
	 * 
	 * @param format the preferred output format.
	 * @return a String representation of this Expression.
	 */
	public String toString(OutputFormat format) {
		if(OutputFormat.LATEX.equals(format)) {
			return "{$"+variable+"="+expression.toString(format)+"$}";
		}
		return variable+"="+expression.toString(format);
	}

	@Override
	public int compareTo(Definition o) {
		return variable.compareTo(o.variable);
	}
	
}
