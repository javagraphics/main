/*
 * @(#)Term.java
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.slimjars.dist.gnu.trove.map.hash.TShortObjectHashMap;
import com.slimjars.dist.gnu.trove.procedure.TShortObjectProcedure;
import com.slimjars.dist.gnu.trove.procedure.TShortProcedure;

/** An immutable cluster of variables, each raised to a unique power, with a leading coefficient.
 * Negative and fractional powers and coefficients are supported.
 * <p>Internally this maps each variable name to a static ID, and this ID uses 2-bytes. So
 * in a given session you should never use more than 65536 variables (or else undefined bad things may occur).
 * 
 * @see Fraction
 * @see Expression
 */
public class Term implements Comparable<Term> {
	private static long idCtr = 0;

	private static synchronized TShortObjectHashMap<Fraction> createVariableMap(String... variableNames) {
		final TShortObjectHashMap<Fraction> returnValue = new TShortObjectHashMap<Fraction>();
		for(int a = 0; a<variableNames.length; a++) {
			String varName = variableNames[a];
			Short id = getIDForVariable(varName);
			if(id==null) {
				Term.variableNames.add(varName);
				id = (short)(Term.variableNames.size()-1);
			}
			Fraction f = returnValue.get(id);
			if(f==null) {
				returnValue.put(id, Fraction.get(1));
			} else {
				returnValue.put(id, f.add(1));
			}
		}
		return returnValue;
	}
	
	private static synchronized TShortObjectHashMap<Fraction> createVariableMap(String variableName,Fraction power) {
		TShortObjectHashMap<Fraction> returnValue = new TShortObjectHashMap<Fraction>();
		
		Short id = getIDForVariable(variableName);
		if(id==null) {
			variableNames.add(variableName);
			id = (short)(variableNames.size()-1);
		}
		
		returnValue.put(id, power);
		return returnValue;
	}
	
	/** The coefficient for this Term. */
	final Fraction coefficient;
	
	/** A unique identifier for this Term. */
	final long id = idCtr++;
	
	/** All the variable names used in this session. The index of each entry
	 * maps to its ID.
	 */
	static List<String> variableNames = new ArrayList<String>();
	
	/** This maps variables to their exponent. This should not be altered after construction. */
	final TShortObjectHashMap<Fraction> variableMap;
	
	/** Create a Term that simply represents the argument provided with no variables.
	 * 
	 * @param coefficient the value this Term represents.
	 */
	public Term(Fraction coefficient) {
		this(coefficient, (TShortObjectHashMap<Fraction>)null);
	}

	protected Term(Fraction coefficient,TShortObjectHashMap<Fraction> incomingVariables) {
		if(coefficient==null) throw new NullPointerException();
		
		this.coefficient = coefficient;
		if(incomingVariables!=null) {
			variableMap = incomingVariables;
		} else {
			variableMap = new TShortObjectHashMap<Fraction>();
		}
		short[] keys = variableMap.keys();
		for(short id : keys) {
			Fraction power = variableMap.get(id);
			if(power.isZero()) {
				variableMap.remove(id);
			}
			String name = Term.variableNames.get(id);
			if(!Character.isLetter(name.charAt(0)))
				throw new IllegalArgumentException("the variable \""+name+"\" must start with a character");
		}
	}
	
	protected Term(long numerator,long denominator,TShortObjectHashMap<Fraction> incomingVariables) {
		this(Fraction.get(numerator, denominator), incomingVariables);
	}

	/** Create a Term.
	 * 
	 * @param numerator the numerator of the coefficient.
	 * @param denominator the denominator of the coefficient.
	 * @param strings a list of variables. If a variable is listed n-many times, then it
	 * will be recorded in this Term as x^n.
	 */
	public Term(long numerator,long denominator,String... strings) {
		this( Fraction.get(numerator, denominator), createVariableMap(strings) );
	}

	/** Create a Term.
	 * 
	 * @param coefficient the coefficient of this Term.
	 * @param strings a list of variables. If a variable is listed n-many times, then it
	 * will be recorded in this Term as x^n.
	 */
	public Term(long coefficient,String... strings) {
		this(coefficient, 1, strings);
	}
	
	/** Create a Term with a coefficient of 1.
	 * 
	 * @param strings a list of variables. If a variable is listed n-many times, then it
	 * will be recorded in this Term as x^n.
	 */
	public Term(String... strings) {
		this(1, 1, strings);
	}
	
	/** Create a Term with a coefficient of 1.
	 * 
	 * @param variable the variable name.
	 * @param power the power of the variable.
	 */
	public Term(String variable,Fraction power) {
		this(Fraction.get(1), createVariableMap(variable, power));
	}

	
	/** Create a Term with a coefficient of 1.
	 * 
	 * @param variable the variable name.
	 * @param power the power of the variable.
	 */
	public Term(String variable,int power) {
		this(Fraction.get(1), createVariableMap(variable, Fraction.get(power)));
	}
	
	/** Create a Term.
	 * 
	 * @param fraction the coefficient of this Term.
	 * @param map a map that relates variable names to exponents.
	 */
	public Term(Fraction fraction, Map<String, Fraction> map) {
		this(fraction, convertMapArgument(map));
	}
	
	private synchronized static TShortObjectHashMap<Fraction> convertMapArgument(Map<String, Fraction> inMap) {
		TShortObjectHashMap<Fraction> returnValue = new TShortObjectHashMap<Fraction>();
		for(String varName : inMap.keySet()) {
			Fraction power = inMap.get(varName);
			Short id = getIDForVariable(varName);
			if(id==null) {
				Term.variableNames.add(varName);
				id = (short)(variableNames.size()-1);
			}
			returnValue.put(id, power);
		}
		return returnValue;
	}
	
	/** Return a new Term that adds the operand to this Term.
	 * 
	 * @param operand a Term that uses the same variables as this Term.
	 * @return a new Term that adds the operand to this Term.
	 * @throws IllegalArgumentException if the two terms do not match. For example:
	 * you cannot add "a + b" and expression this as a Term. (You can represent this
	 * as an Expression, though.) But this method will let you add "5*a + 20*a".
	 */
	public Term add(Term operand) {
		if(!variablesMatch(operand))
			throw new IllegalArgumentException("cannot combine "+this+" and "+operand);
		
		return new Term(coefficient.add(operand.coefficient), variableMap);
	}
	
	/** 
	 * @see #compareTo(Term, boolean)
	 * @see #compareVariablesTo(Term)
	 */
	public int compareTo(Term other) {
		return compareTo(other, false);
	}
	
	/**
	 * 
	 * @param returnNonZero if true then this will always
	 * return a non-zero answer. (This guarantees sorting
	 * algorithms consistently rank two otherwise identical
	 * Terms the same way). If false: then if two
	 * Terms represent the same value this will return 0.
	 * 
	 * @see #compareVariablesTo(Term)
	 */
	public int compareTo(Term other,boolean returnNonZero) {
		if(this==other) return 0;
		
		int v = compareVariablesTo(other);
		if(v!=0) return v;
		
		v = coefficient.compareTo(other.coefficient);
		if(v!=0) return v;
		
		//they're equal.
		
		if(!returnNonZero) {
			return 0;
		}
		//let's still have a consistent model to order them:
		if(this.id>other.id) {
			return 1;
		}
		return -1;
	}
	
	/** Similar to compareTo(), except this only regards the variables.
	 * This will return 0 if the variables match but the coefficient is different.
	 * 
	 * @see #compareTo(Term, boolean)
	 */
	public int compareVariablesTo(Term other) {
		int s1 = variableMap.size();
		int s2 = other.variableMap.size();
		if(s1<s2) {
			return -1;
		} else if(s1>s2) {
			return 1;
		}
		
		short[] keys1 = variableMap.keys();
		short[] keys2 = other.variableMap.keys();
		
		Arrays.sort(keys1);
		Arrays.sort(keys2);
		
		int min = Math.min(keys1.length, keys2.length);
		for(int a = 0; a<min; a++) {
			if(keys1[a]<keys2[a]) {
				return -1;
			} else if(keys1[a]>keys2[a]) {
				return 1;
			}

			Fraction f1 = variableMap.get(keys1[a]);
			Fraction f2 = other.variableMap.get(keys1[a]);
			int k = f1.compareTo(f2);
			if(k!=0) return k;
		}

		return 0;
	}
	
	/** Divide this Term by another.
	 * 
	 * @param variables a series of variables. If a variable is restated n-many times then
	 * it is raised to the nth power.
	 * @return a new Term that divides this Term by the variables provided.
	 */
	public Term divideBy(String... variables) {
		return divideBy(new Term(variables));
	}

	/** Divide this Term by a long.
	 * 
	 * @param value the value to divide this Term by.
	 * @return a new Term that divides this Term by the argument.
	 */
	public Term divideBy(long value) {
		return divideBy(new Term(value));
	}

	/** Divide this Term by another.
	 * 
	 * @param value the coefficient of the divisor.
	 * @param variables a series of variables in the divisor. If a variable is restated n-many times then
	 * it is raised to the nth power.
	 * @return a new Term that divides this Term by the argument.
	 */
	public Term divideBy(long value,String... variables) {
		return divideBy(new Term(value, variables));
	}

	/** Divide this Term by another.
	 * 
	 * @param divisor the Term to divide this Term by.
	 * @return a new Term that divides this Term by the argument.
	 */
	public Term divideBy(final Term divisor) {
		final TShortObjectHashMap<Fraction> newMap = new TShortObjectHashMap<>(variableMap);
		
		divisor.variableMap.forEachEntry(new TShortObjectProcedure<Fraction>() {
			@Override
			public boolean execute(short id, Fraction f) {
				Fraction f2 = variableMap.get(id);
				if(f2==null) {
					newMap.put(id, f.negate());
				} else {
					Fraction f3 = f2.subtract(f);
					if(f3.isZero()) {
						newMap.remove(id);
					} else {
						newMap.put(id, f3);
					}
				}
				return true;
			}
		});
		
		return new Term(coefficient.multiply(divisor.coefficient.invert()), newMap);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Term)) return false;
		Term t = (Term)obj;
		return compareTo(t, false)==0;
	}
	
	/** Evaluate this Term.
	 * 
	 * @param incomingVars a map that relates variable names to their numeric values.
	 * @return the numeric value of this Term based on the variables provided.
	 * @throws MissingVariablesException if this Term requires a variable that is not provided
	 * in the argument.
	 * @see #evaluateBigDecimal(Map)
	 * @see #evaluateBigInteger(Map)
	 */
	public double evaluate(Map<String, Number> incomingVars) throws MissingVariablesException {
		double product = coefficient.doubleValue();
		short[] keys = variableMap.keys();
		for(short id : keys) {
			Fraction f = variableMap.get(id);
			String varName = variableNames.get(id);

			Number value = incomingVars.get(varName);
			double evaluation = Math.pow(value.doubleValue(), f.doubleValue());
			
			product = product*evaluation;
		}
		return product;
	}

	/** Evaluate this Term using high precision.
	 * 
	 * @param incomingVars a map that relates variable names to their numeric values.
	 * @return the numeric value of this Term based on the variables provided.
	 * @throws MissingVariablesException if this Term requires a variable that is not provided
	 * in the argument.
	 * @see #evaluate(Map)
	 * @see #evaluateBigDecimal(Map)
	 */
	public BigDecimal evaluateBigDecimal(Map<String, Number> incomingVars) throws MissingVariablesException {
		BigDecimal product = coefficient.bigDecimalValue();
		short[] keys = this.variableMap.keys();
		for(short id : keys) {
			Fraction f = variableMap.get(id);
			String varName = variableNames.get(id);

			Number value = incomingVars.get(varName);
			if(value==null) throw new MissingVariablesException("the variable \""+varName+"\" is undefined");
			BigDecimal evaluation = new BigDecimal(value.doubleValue(), MathContext.DECIMAL128).pow(f.intValue(), MathContext.DECIMAL128);
			
			product = product.multiply(evaluation);
		}
		return product;
	}

	/** An exception used when an integer was required but a noninteger value was found. */
	public static class NonIntegerException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public NonIntegerException(String s) {
			super(s);
		}
	}

	/** Evaluate this Term using high precision.
	 * 
	 * @param incomingVars a map that relates variable names to their numeric values.
	 * @return the numeric value of this Term based on the variables provided.
	 * @throws MissingVariablesException if this Term requires a variable that is not provided
	 * in the argument.
	 * @throws NonIntegerException if this Term cannot be expressed as an integer.
	 * @see #evaluate(Map)
	 * @see #evaluateBigInteger(Map)
	 */
	public BigInteger evaluateBigInteger(Map<String, BigInteger> incomingVars) throws NonIntegerException, MissingVariablesException {
		BigInteger product = coefficient.bigIntegerValue();
		if(product==null)
			throw new NonIntegerException(coefficient.toString());
		short[] keys = this.variableMap.keys();
		for(short id : keys) {
			Fraction f = variableMap.get(id);
			String varName = variableNames.get(id);
			if(!f.isInteger())
				throw new NonIntegerException(varName+"^"+f);

			Number value = incomingVars.get(varName);
			if(value==null) throw new MissingVariablesException("the variable \""+varName+"\" is undefined");
			if( Math.abs(value.doubleValue() - value.intValue())>.001) {
				throw new NonIntegerException(varName+" maps to "+value);
			}
			BigInteger evaluation = BigInteger.valueOf(value.longValue()).pow(f.intValue());
			
			product = product.multiply(evaluation);
		}
		return product;
	}
	
	/** @return all the variable names used by this Term. */
	public Set<String> getVariables() {
		return getVariables(new TreeSet<String>());
	}

	/** Retrieve all the variable names used by this Term.
	 * @param set all the variable names will be stored in this argument.
	 * @return all the variable names used by this Term.
	 * 
	 */
	public Set<String> getVariables(final Set<String> set) {
		variableMap.forEachKey(new TShortProcedure() {
			@Override
			public boolean execute(short id) {
				set.add( Term.variableNames.get(id) );
				return true;
			}
		});
		return set;
	}
	
	private static Short getIDForVariable(String variableName) {
		for(int a = 0; a<variableNames.size(); a++) {
			if(variableNames.get(a).equals(variableName))
				return (short)a;
		}
		return null;
	}
	
	/** Return the degree (exponent) of the variable name provided.
	 * 
	 * @param variableName the variable name to examine.
	 * @return the degree (exponent) of the variable name used in this term. This may return zero.
	 */
	public Fraction getDegree(String variableName) {
		Short id = getIDForVariable(variableName);
		if(id==null)
			return Fraction.get(0);
		
		Fraction f = variableMap.get(id);
		if(f==null) return Fraction.get(0);
		return f;
	}
	
	@Override
	public int hashCode() {
		return variableMap.hashCode();
	}
	
	/** Multiply two Terms together.
	 * 
	 * @param t the Term to multiply this Term by.
	 * @return a new Term that is the product of these two Terms.
	 */
	public Term multiply(Term t) {
		if(coefficient.isZero() || t.coefficient.isZero())
			return new Term(0, 1, new TShortObjectHashMap<Fraction>());

		final TShortObjectHashMap<Fraction> newVariables = new TShortObjectHashMap<>(variableMap);
		
		t.variableMap.forEachEntry(new TShortObjectProcedure<Fraction>() {
			@Override
			public boolean execute(short id, Fraction power) {
				Fraction f = newVariables.get(id);
				if(f==null) {
					newVariables.put(id, power);
				} else {
					newVariables.put(id, f.add(power));
				}
				return true;
			}
		});
		
		return new Term(coefficient.multiply(t.coefficient), newVariables);
	}
	
	/** Negate this Term.
	 * 
	 * @return a new Term that is the negation of this Term.
	 */
	public Term negate() {
		return new Term( coefficient.negate(), variableMap);
	}
	
	/**
	 * 
	 * @param t another Term to compare against.
	 * @return a new Term that expresses the variables this Term and the argument share.
	 * If the argument contains (a^2) and this Term contains (a^5), then the returned
	 * value will contain (a^2). However if the signs of exponent don't match: then that
	 * variable will not be in the return value.
	 */
	public Term getCommonElements(Term t) {
		final TShortObjectHashMap<Fraction> returnValue = new TShortObjectHashMap<Fraction>();
		t.variableMap.forEachEntry(new TShortObjectProcedure<Fraction>() {
			@Override
			public boolean execute(short id, Fraction f1) {
				Fraction f2 = Term.this.variableMap.get(id);
				if(f2!=null) {
					int c1 = f1.compareTo(0);
					int c2 = f2.compareTo(0);
					if(c1<0 && c2<0) {
						Fraction max = Fraction.max(f1, f2);
						returnValue.put(id, max);
					} else if(c1>0 && c2>0) {
						Fraction min = Fraction.min(f1, f2);
						returnValue.put(id, min);
					}
				}
				return true;
			}
		});
		
		return new Term(1, 1, returnValue);
	}
	
	/**
	 * 
	 * @param exponent the exponent to raise this Term to.
	 * @return a new Term that raises this Term to the argument provided.
	 * @throws IrrationalException Term objects cannot support irrational numbers, so if this Term's
	 * coefficient cannot be raised to the argument then an IrrationalException may be thrown.
	 * @throws ImaginaryException Term objects cannot support imaginary numbers, so if this Term's
	 * coefficient cannot be raised to the argument then an ImaginaryException may be thrown.
	 */
	public Term raiseToPower(final Fraction exponent) throws IrrationalException, ImaginaryException {
		final TShortObjectHashMap<Fraction> newVariables = new TShortObjectHashMap<Fraction>();
		
		variableMap.forEachEntry(new TShortObjectProcedure<Fraction>() {

			@Override
			public boolean execute(short id, Fraction power) {
				Fraction newPower = power.multiply(exponent);
				if(newPower.compareTo(0)!=0) {
					newVariables.put(id, newPower);
				}
				return true;
			}
			
		});

		return new Term( coefficient.raiseToPower(exponent), newVariables );
	}
	
	/** Strip a variable from this Term.
	 * 
	 * @param variableName the variable name to remove.
	 * @return a new Term that removes the argument from this object.
	 */
	public Term removeVariable(String variableName) {
		Short id = Term.getIDForVariable(variableName);
		if(id==null) return this;

		final TShortObjectHashMap<Fraction> newVariables = new TShortObjectHashMap<>(variableMap);
		newVariables.remove(id);
		
		return new Term(coefficient, newVariables);
	}
	
	/** Replace the coefficient of this Term with 1.
	 * 
	 * @return a new Term that replaces its coefficient with the value 1.
	 */
	public Term removeCoefficient() {
		return new Term( Fraction.get(1), variableMap);
	}

	@Override
	public String toString() {
		return toString(OutputFormat.PLAIN);
	}

	/** Create a String representation of this Term.
	 * 
	 * @param format the preferred output format.
	 * @return a String representation of this Term.
	 */
	public String toString(OutputFormat format) {
		if(coefficient.isZero()) return "";
		
		StringBuffer sb = new StringBuffer();
		
		StringBuffer leadingText = new StringBuffer();
		StringBuffer numeratorText = new StringBuffer();
		StringBuffer denominatorText = new StringBuffer();
		
		//processCoefficient : 
		{
			String coefficientString = coefficient.toString();
			String numeratorString, denominatorString;
			int i = coefficientString.indexOf('/');
			if(i==-1) {
				numeratorString = coefficientString;
				denominatorString = null;
			} else {
				numeratorString = coefficientString.substring(0,i);
				denominatorString = coefficientString.substring(i+1);
			}
			
			if(variableMap.size()>0) {
				if(numeratorString.equals("-1")) {
					numeratorText.append("-");
				} else if(!numeratorString.equals("1")) {
					numeratorText.append(numeratorString);
				}
			} else {
				numeratorText.append(numeratorString);
			}
			if(denominatorString!=null) {
				denominatorText.append(denominatorString);
			}
		}

		short[] keys = variableMap.keys();
		//putting variable names in a sorted set helps alphabetize our output:
		SortedSet<String> varNames = new TreeSet<String>();
		for(short id : keys) {
			varNames.add( variableNames.get(id) );
		}
		Iterator<String> iter = varNames.iterator();
		processTerms : while(iter.hasNext()) {
			String key = iter.next();
			Short b = Term.getIDForVariable(key);
			Fraction power = variableMap.get(b);

			if(power.isZero()) {
				continue processTerms;
			}
			
			if(power.compareTo(0)>0) {
				if(numeratorText.length()>0 && numeratorText.charAt(numeratorText.length()-1)!='-') {
					if(OutputFormat.LATEX.equals(format)) {
						numeratorText.append("\\,");
					} else {
						numeratorText.append('*');
					}
				}
				numeratorText.append( key );
				if(power.compareTo(1)!=0) {
					if(OutputFormat.LATEX.equals(format) && key.contains("_")) {
						numeratorText.append("\\,");
					}
					numeratorText.append('^');
					String powerString = power.toString();
					if(OutputFormat.LATEX.equals(format) && powerString.length()>1) {
						numeratorText.append("{"+powerString+"}");
					} else {
						numeratorText.append( powerString );
					}
				}
			} else if(power.compareTo(0)<0) {
				if(denominatorText.length()>0) {
					if(OutputFormat.LATEX.equals(format)) {
						denominatorText.append("\\,");
					} else {
						denominatorText.append('*');
					}
				}
				denominatorText.append( key );
				if(power.compareTo(-1)!=0) {
					if(OutputFormat.LATEX.equals(format) && key.contains("_")) {
						denominatorText.append("\\,");
					}
					denominatorText.append('^');
					String powerString = power.negate().toString();
					if(OutputFormat.LATEX.equals(format) && powerString.length()>0) {
						denominatorText.append("{"+powerString+"}");
					} else {
						denominatorText.append( ""+powerString );
					}
				}
			}
		}
		sb.append(leadingText);
		if(OutputFormat.LATEX.equals(format)) {
			if(denominatorText.length()>0) {
				sb.append("\\frac{"+numeratorText+"}{"+denominatorText+"}");
			} else {
				sb.append(numeratorText);
			}
		} else {
			sb.append(numeratorText);
			if(denominatorText.length()>0) {
				sb.append("/(");
				sb.append( denominatorText );
				sb.append(")");
			}
		}
		return sb.toString();
	}
	
	protected boolean variablesMatch(Term t) {
		return compareVariablesTo(t)==0;
	}
}
