/*
 * @(#)Expression.java
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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.bric.math.symbolic.Term.NonIntegerException;

/** This is an immutable sum of several Terms.
 * <p>This class was originally intended to represent the left-hand side of
 * a polynomial equation (where the right-hand side is equal to zero).
 * 
 * @see Fraction
 * @see Term
 */
public class Expression {
	
	/** The number of Terms at which this Expression must be completely sorted. */
	private static final int UNSORTED_THRESHOLD = 50;

	final Term[] terms;
	
	/** Create a new Expression as a series of Terms.
	 * 
	 * @param consolidateTerms When in doubt this should be true, but in cases where
	 * we are 100% certain terms will not need condensing, setting this to false may
	 * save some time.
	 * @param list
	 */
	private Expression(boolean consolidateTerms,List<Term> list) {
		this( consolidateTerms, list==null ? new Term[] {} : list.toArray(new Term[list.size()]));
	}
	
	/**
	 * 
	 * @param consolidateTerms When in doubt this should be true, but in cases where
	 * we are 100% certain terms will not need condensing, setting this to false may
	 * save some time.
	 * @param incomingTerms
	 */
	private Expression(boolean consolidateTerms,Term... incomingTerms) {
		if(!consolidateTerms) {
			terms = incomingTerms;
			return;
		}
		
		/* For small expressions: let's try to preserve
		 * the order of the incoming Terms. This may help
		 * human readability if someone is trying to make
		 * sense of data.
		 * 
		 * But for large Expressions: sort the Terms
		 * to make the processes much faster. (What are
		 * the odds someone will be picky about the order
		 * of thousands of Terms?)
		 * 
		 */
		boolean sorted;
		if(incomingTerms.length>50) {
			Arrays.sort(incomingTerms);
			sorted = true;
		} else {
			sorted = false;
		}
		int ctr = 0;
		for(int a = 0; a<incomingTerms.length; a++) {
			consolidate : for(int b = a+1; b<incomingTerms.length && incomingTerms[a]!=null; b++) {
				Term t2 = incomingTerms[b];
				if(t2!=null && incomingTerms[a].variablesMatch(t2)) {
					incomingTerms[a] = incomingTerms[a].add(t2);
					incomingTerms[b] = null;
					ctr++;
				} else {
					if(sorted)
						break consolidate;
				}
			}
		}
		for(int a = 0; a<incomingTerms.length; a++) {
			if(incomingTerms[a]!=null && incomingTerms[a].coefficient.isZero()) {
				incomingTerms[a] = null;
				ctr++;
			}
		}
		
		if(ctr==0) {
			terms = incomingTerms;
		} else {
			terms = new Term[incomingTerms.length - ctr];
			ctr = 0;
			for(int a = 0; a<incomingTerms.length; a++) {
				if(incomingTerms[a]!=null)
					terms[ctr++] = incomingTerms[a];
			}
		}
	}
	
	/** Create a new Expression as a series of Terms.
	 * Redundant terms will be consolidated. Depending on the number
	 * of Terms provided they may be sorted (and thus reordered).
	 * @param list the Terms to represent in this Expression.
	 */
	public Expression(List<Term> list) {
		this(true, list);
	}

	/** Create a new Expression as a series of Terms.
	 * Redundant terms will be consolidated. Depending on the number
	 * of Terms provided they may be sorted (and thus reordered).
	 * @param incomingTerms the Terms to represent in this Expression.
	 */
	public Expression(Term... incomingTerms) {
		this(true, incomingTerms);
	}
	
	/** Create a new Expression that represents a single number.
	 * 
	 * @param value the one element of the new Expression.
	 */
	public Expression(long value) {
		this(value, 1, new String[] {});
	}
	
	/** Create a new Expression that contains only one Term.
	 * 
	 * @param coefficient the coefficient for the Term in this Expression.
	 * @param variables the variables in the Term in this Expression. If a
	 * variable is listed n-many times then it is raised to the nth power.
	 */
	public Expression(long coefficient,String... variables) {
		this(coefficient, 1, variables);
	}
	
	/** Create a new Expression that represents a fraction.
	 * 
	 * @param numerator the numerator of the coefficient for the Term in this Expression.
	 * @param denominator the denominator of the coefficient for the Term in this Expression.
	 */
	public Expression(long numerator,long denominator) {
		this(numerator, denominator, new String[] {});
	}

	
	/** Create a new Expression that contains only one Term.
	 * 
	 * @param numerator the numerator of the coefficient for the Term in this Expression.
	 * @param denominator the denominator of the coefficient for the Term in this Expression.
	 * @param variables the variables in the Term in this Expression. If a
	 * variable is listed n-many times then it is raised to the nth power.
	 */
	public Expression(long numerator,long denominator,String... variables) {
		this(new Term(numerator, denominator, variables));
	}
	
	Term getCommonFactor() {
		Map<String, Fraction> map = new HashMap<String, Fraction>();
		Set<String> bannedVariables = new HashSet<String>();
		for(int a = 0; a<terms.length; a++) {
			Term t = terms[a];
			Set<String> termVariables = t.getVariables();

			Iterator<String> iter = termVariables.iterator();
			while(iter.hasNext()) {
				String variable = iter.next();
				Fraction incoming = t.getDegree(variable);
				if(!bannedVariables.contains(variable)) {
					Fraction current = map.get(variable);
					if(current==null) {
						bannedVariables.add(variable);
					} else {
						if( (current.compareTo(0)>0) && (incoming.compareTo(0)>0)) {
							Fraction min = Fraction.min(current, incoming);
							map.put(variable, min);
						} else if(current.compareTo(0)<0 && incoming.compareTo(0)<0) {
							Fraction max = Fraction.max(current, incoming);
							map.put(variable, max);
						} else {
							bannedVariables.add(variable);
							map.remove(variable);
						}
					}
				}
			}
			
			//what about variables that were flat out absent?
			iter = map.keySet().iterator();
			while(iter.hasNext()) {
				String variable = iter.next();
				if(termVariables.contains(variable)) {
					bannedVariables.add(variable);
					iter.remove();
				}
			}
		}
		//TODO also consider the numeric coefficient
		return new Term( Fraction.get(1), map);
	}

	/** This is shorthand for <code>this.raiseTo(2)</code>.
	 * 
	 * @return this Expression squared.
	 */
	public Expression square() {
		return raiseTo(2);
	}
	
	/** Raise this expression to a power.
	 * 
	 * @param power the power to raise this Expression to. For example, 
	 * if this is 2 then this returns the square of this expression.
	 * If 3, then this returns the cube of this expression.
	 * @return a new Expression that raises this Expression to the given power.
	 */
	public Expression raiseTo(int power) {
		if(power<=0) throw new IllegalArgumentException("power ("+power+") must be positive");
		Expression multiplier = this;
		Expression result = this;
		while(power>1) {
			result = result.multiply(multiplier);
			power--;
		}
		return result;
	}
	
	/** Negate this Expression.
	 * 
	 * @return an expression equivalent to "0-this"
	 */
	public Expression negate() {
		Term[] newTerms = new Term[terms.length];
		for(int a = 0; a<newTerms.length; a++) {
			newTerms[a] = terms[a].negate();
		}
		return new Expression(false, newTerms);
	}
	
	/** The result of a division.
	 */
	public static class DivisionResult {
		/** The result of this division. */
		public Expression quotient;
		
		/** The term divisor applied to the dividend. */
		public Term divisor;
	}
	
	/** Get a specific Term from this Expression.
	 * 
	 * @param index the index of the Term to retrieve.
	 * @return an indexed Term within this Expression.
	 */
	public Term getTerm(int index) {
		return terms[index];
	}
	
	/** Check to see if this Expression contains a Term.
	 * 
	 * @param t a Term to search for.
	 * @return true if this Expression contains a Term that exactly matches the argument.
	 */
	public boolean containsTerm(Term t) {
		for(int a = 0; a<terms.length; a++) {
			if(terms[a].equals(t)) return true;
		}
		return false;
	}
	
	/** Return the number of Terms in this Expression.
	 * 
	 * @return the number of Terms in this Expression.
	 */
	public int getTermCount() {
		return terms.length;
	}
	
	/** Identify a common fraction that can be factored from all the Terms in this
	 * Expression and remove it. This includes variables raised to negative powers.
	 * 
	 * @return a result that includes a new Expression and the Divisor used to create it.
	 */
	public DivisionResult eliminateFractions() {
		DivisionResult r = new DivisionResult();
		
		if(terms.length==0) {
			r.divisor = new Term(1);
			r.quotient = this;
			return r;
		}
		
		Map<String, Fraction> variables = new HashMap<String, Fraction>();
		Fraction commonDenominator = null;

		
		//now compare all other terms, gradually reducing our divisor as necessary:
		for(int a = 0; a<terms.length; a++) {
			Term t = terms[a];

			commonDenominator = commonDenominator==null ? 
					terms[a].coefficient.isolateDenominator() : 
					commonDenominator.isolateLCD( terms[a].coefficient.isolateDenominator() );			
			
			Iterator<String> iter = t.getVariables().iterator();
			while(iter.hasNext()) {
				String variable = iter.next();
				Fraction power = t.getDegree(variable);
				if(power.compareTo(0)<0) {
					//this is negative, but we store variables as positives:
					Fraction current = variables.get(variable);
					if(current==null || power.negate().compareTo(current)>0) {
						//but not by enough, apparently!
						variables.put(variable, power.negate());
					}
				}
			}
		}
		
		Term divisor = new Term( commonDenominator.invert(), variables);
		
		r.divisor = divisor;
		r.quotient = multiply(divisor);
		return r;
	}

	/** Add several Terms to this Expression.
	 * 
	 * @param incomingTerms a list of Terms to add to this object.
	 * @return the sum of this Expression and the operand(s).
	 */
	public Expression add(Term... incomingTerms) {
		int sum = terms.length + incomingTerms.length;

		List<Term> masterList = new ArrayList<Term>(terms.length + incomingTerms.length);
		if(sum>=UNSORTED_THRESHOLD) {
			for(Term t : terms) {
				insertTermIntoSortedList(masterList, t);
			}
			for(Term t : incomingTerms) {
				insertTermIntoSortedList(masterList, t);
			}
			return new Expression(false, masterList.toArray(new Term[masterList.size()]));
		} else {
			for(Term t : terms) {
				masterList.add(t);
			}
			for(Term t : incomingTerms) {
				masterList.add(t);
			}
			return new Expression(true, masterList.toArray(new Term[masterList.size()]));
		}
	}

	/** Evaluate this Expression.
	 * 
	 * @param variables a map that relates variable names to their numeric values.
	 * @return the numeric value of this Expression based on the variables provided.
	 * @throws MissingVariablesException if a Term requires a variable that is not provided
	 * in the argument.
	 * @see #evaluateBigInteger(Map)
	 */
	public double evaluate(Map<String, Number> variables) throws MissingVariablesException {
		
		if(terms.length==0) return 0;
		
		double[] values = new double[terms.length];
		for(int a = 0; a<values.length; a++) {
			values[a] = terms[a].evaluate(variables);
		}
		return getSum(values);
	}
	
	/** Carefully add a list of doubles.
	 * <p>This adds smaller elements first and resorts the list.
	 * The theory here is this should reduce rounding error. There are
	 * probably more efficient ways to implement this, but it gets the job
	 * done for now.
	 * 
	 * @param values a list of doubles to add.
	 * @return the sum of all the elements in the argument.
	 */
	protected static double getSum(double[] values) {
		/* This goes to elaborate pains to try to mitigate machine error.
		 * The theory is: let small values snowball into larger and larger values
		 * before adding them to a large value.
		 */
		Arrays.sort(values);
		while(values.length>1) {
			values[1] += values[0];
			double[] copy = new double[values.length-1];
			System.arraycopy(values, 1, copy, 0, copy.length);
			Arrays.sort(copy);
			values = copy;
		}
		return values[0];
	}


	/** Evaluate this Expression using high precision.
	 * 
	 * @param variables a map that relates variable names to their numeric values.
	 * @return the numeric value of this Expression based on the variables provided.
	 * @throws NonIntegerException if a Term would evaluate to a non-integer.
	 * @throws MissingVariablesException if this Expression requires a variable that is not provided
	 * in the argument.
	 * @see #evaluate(Map)
	 */
	public BigInteger evaluateBigInteger(Map<String, BigInteger> variables) throws NonIntegerException, MissingVariablesException {
		BigInteger sum = BigInteger.ZERO;
		for(int a = 0; a<terms.length; a++) {
			sum = sum.add( terms[a].evaluateBigInteger(variables) );
		}
		return sum;
	}
	
	/** Add several Expressions.
	 * 
	 * @param expressions a list of Expressions to add to this object.
	 * @return the sum of all the Expressions involved.
	 */
	public Expression add(Expression... expressions) {
		int sum = terms.length;
		for(Expression e : expressions) {
			sum += e.terms.length;
		}
		List<Term> masterList = new ArrayList<Term>();
		if(sum>=UNSORTED_THRESHOLD) {
			for(Term t : terms) {
				insertTermIntoSortedList(masterList, t);
			}
			for(Expression e : expressions) {
				for(Term t : e.terms) {
					insertTermIntoSortedList(masterList, t);
				}
			}
			return new Expression(false, masterList.toArray(new Term[masterList.size()]));
		} else {
			for(Term t : terms) {
				masterList.add(t);
			}
			for(Expression e : expressions) {
				for(Term t : e.terms) {
					masterList.add(t);
				}
			}
			return new Expression(true, masterList.toArray(new Term[masterList.size()]));
		}
	}
	
	/** Subtract an Expression from this object.
	 * 
	 * @param e the operand to subtract from this Expression.
	 * @return the difference of this value minus the operand.
	 */
	public Expression subtract(Expression e) {
		return add(e.negate());
	}
	
	/** Multiple this Expression by a constant.
	 * 
	 * @param c the operand to multiply by
	 * @return the product of this Expression and the operand.
	 */
	public Expression multiply(long c) {
		return multiply(new Term(c));
	}

	/** Multiple this Expression by one or more Terms.
	 * 
	 * @param term the operand to multiply by.
	 * @return the product of this Expression and the operand.
	 */
	public Expression multiply(Term... term) {
		Term singleProduct = term[0];
		for(int a = 1; a<term.length; a++) {
			singleProduct = singleProduct.multiply(term[a]);
		}
		if(singleProduct.coefficient.isZero())
			return new Expression(false, new Term[] {});
		
		Term[] list = new Term[terms.length];
		for(int a = 0; a<list.length; a++) {
			list[a] = terms[a].multiply(singleProduct);
		}
		return new Expression(false, list);
	}
	
	/** Divide this Expression by a Fraction.
	 * 
	 * @param fraction the divisor
	 * @return the quotient of this Expression divided by the argument.
	 */
	public Expression divideBy(Fraction fraction) {
		return divideBy(new Term(fraction));
	}

	/** Divide this Expression by a series of variables.
	 * 
	 * @param variableNames the divisor, expressed as a list of variable names.
	 * If a variable is listed n-many times then it is raised to the nth power.
	 * @return the quotient of this Expression divided by the argument.
	 */
	public Expression divideBy(String...variableNames) {
		Term divisor = new Term(variableNames);
		return divideBy(divisor);
	}

	/** Divide this Expression by a Term.
	 * 
	 * @param divisor the divisor
	 * @return the quotient of this Expression divided by the argument.
	 */
	public Expression divideBy(Term divisor) {
		Term[] newArray = new Term[terms.length];
		for(int a = 0; a<newArray.length; a++) {
			newArray[a] = terms[a].divideBy(divisor);
		}
		return new Expression(false, newArray);
	}
	
	transient Integer hashCode = null;
	
	@Override
	public int hashCode() {
		if(terms.length==0) return 0;
		if(hashCode==null) {
			SortedSet<Term> sortedTerms = new TreeSet<Term>();
			for(Term t : terms) {
				sortedTerms.add(t);
			}
			hashCode = sortedTerms.hashCode();
		}
		
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==this) return true;
		if(!(obj instanceof Expression))
			return false;
		Expression other = (Expression)obj;
		
		if(hashCode()!=other.hashCode())
			return false;

		SortedSet<Term> myTerms = new TreeSet<Term>();
		for(Term t : terms) {
			myTerms.add(t);
		}

		SortedSet<Term> otherTerms = new TreeSet<Term>();
		for(Term t : other.terms) {
			otherTerms.add(t);
		}
		
		return myTerms.equals(otherTerms);
	}

	/** This is shorthand for <code>this.raiseTo(3)</code>.
	 * 
	 * @return this Expression cubed.
	 */
	public Expression cube() {
		return raiseTo(3);
	}

	/** Substitute one variable out of this Expression.
	 * @param variableName the name of the variable to replace (the left-hand-side of an equation)
	 * @param variableDefinition the term to replace the variable with (the right-hand-side of an equation).
	 * @throws IllegalSubstitutionException if these substitutions will result in an imaginary
	 * or irrational value.
	 */
	public Expression substitute(String variableName,Term variableDefinition) throws IllegalSubstitutionException {
		return substitute(variableName, new Expression(variableDefinition));
	}
	
	private static Comparator<Term> termComparator = new Comparator<Term>() {

		@Override
		public int compare(Term o1, Term o2) {
			return o1.compareVariablesTo(o2);
		}
		
	};

	/** Substitute several variables using a list of Definitions.
	 * @param definitions a series of definitions to substitute into this Expression.
	 * @throws IllegalSubstitutionException if these substitutions will result in an imaginary
	 * or irrational value.
	 */
	public Expression substitute(Collection<Definition> definitions) throws IllegalSubstitutionException {
		return substitute( definitions.toArray(new Definition[definitions.size()]));
	}

	/** Substitute several variables using a list of Definitions.
	 * @param definitions a series of definitions to substitute into this Expression.
	 * @throws IllegalSubstitutionException if these substitutions will result in an imaginary
	 * or irrational value.
	 */
	public Expression substitute(Definition... definitions) throws IllegalSubstitutionException {
		if(definitions.length==0) return this;
		
		Map<String, Expression> defMap = new HashMap<String, Expression>();
		boolean allSingleTerms = true;
		for(Definition d : definitions) {
			defMap.put(d.variable, d.expression);
			if(d.expression.terms.length!=1)
				allSingleTerms = false;
		}
		
		if(allSingleTerms) {
			Term[] newTerms = new Term[terms.length];
			for(int i = 0; i<newTerms.length; i++) {
				Term t = terms[i];
				for(String variableName : defMap.keySet()) {
					Fraction power = t.getDegree(variableName);
					if(power==null) {
						newTerms[i] = t;
					} else {
						try {
							Term replacementTerm = defMap.get(variableName).terms[0];
							newTerms[i] = replacementTerm.raiseToPower(power).multiply(t.removeVariable(variableName));
						} catch (IrrationalException e) {
							throw new IllegalSubstitutionException(e);
						} catch (ImaginaryException e) {
							throw new IllegalSubstitutionException(e);
						}
					}
				}
			}
			return new Expression(true, newTerms);
		}
		
		Set<String> definedVariableNames = defMap.keySet();
		
		/* This is the list of terms used to create our new replacement
		 * Expression. This will be sorted according to termComparator.
		 * 
		 */
		List<Term> returnValue = new ArrayList<Term>();
		LinkedList<Term> pendingTerms = new LinkedList<Term>();
		
		for(int a = 0; a<terms.length; a++) {
			//System.out.println("processing term "+a+" of "+terms.length);
			
			pendingTerms.add(terms[a]);
			
			boolean search = true;
			while(search) {
				search = false;
				
				applySubstitution : while(pendingTerms.size()>0) {
					Term et = pendingTerms.removeFirst();
					//System.out.println("pendingTerms.size = "+pendingTerms.size());
					//System.out.println("returnValue.size = "+returnValue.size());
					//System.out.println("studying: "+et);
					for(String variableName : definedVariableNames) {
						Fraction power = et.getDegree(variableName);
						if(!power.equals(0)) {
							if(!power.isInteger()) {
								throw new IllegalSubstitutionException("The expression \""+defMap.get(variableName)+"\" cannot be raised to a fractional exponent ("+power+")");
							}
							int powerInt = power.intValue();
							if(powerInt<0) {
								throw new IllegalSubstitutionException("The expression \""+defMap.get(variableName)+"\" cannot be raised to a negative exponent ("+power+")");
							}
							
							Expression replacementExpression = defMap.get(variableName).raiseTo(powerInt);
							Term remainingTerm = et.removeVariable(variableName);
							replacementExpression = replacementExpression.multiply(remainingTerm);
							
							for(Term t : replacementExpression.terms) {
								if(containsAny(definedVariableNames, t.getVariables())) {
									pendingTerms.add(0, t);
								} else {
									insertTermIntoSortedList(returnValue, t);
								}
							}
							
							search = true;
							break applySubstitution;
						}
					}
					
					//if we reached this point, then we didn't call "break applySubsitution", which means
					//"et" has no substitutions
					insertTermIntoSortedList(returnValue, et);
				}
			}
		}
		
		return new Expression(false, returnValue.toArray(new Term[returnValue.size()]));
	}
	
	private static boolean containsAny(Collection<String> bigCollection,Collection<String> smallCollection) {
		for(String s : smallCollection) {
			if(bigCollection.contains(s)) return true;
		}
		return false;
	}
	
	private static void insertTermIntoSortedList(List<Term> sortedTermList,Term addition) {
		int index = Collections.binarySearch(sortedTermList, addition, termComparator);
		if(index<0) {
			if(!addition.coefficient.isZero())
				sortedTermList.add(-index-1, addition);
		} else {
			Term existingTerm = sortedTermList.get(index);
			Term sum = existingTerm.add(addition);
			if(sum.coefficient.isZero()) {
				sortedTermList.remove(index);
			} else {
				sortedTermList.set(index, sum);
			}
		}
	}

	/** Substitute one variable out of this Expression.
	 * @param variableName the name of the variable to replace (the left-hand-side of an equation)
	 * @param variableDefinition the expression to replace the variable with (the right-hand-side of an equation).
	 * @throws IllegalSubstitutionException if these substitutions will result in an imaginary
	 * or irrational value.
	 */
	public Expression substitute(String variableName,Expression variableDefinition) throws IllegalSubstitutionException {
		return substitute(new Definition(variableName, variableDefinition));
	}
	
	/** Return the coefficient of a term.
	 * 
	 * @param variables a series of variables multiplied together. If a variable is
	 * listed n-many times then it is raised to the nth power.
	 * @return the coefficient of the Term that relates to the argument.
	 */
	public Fraction getCoefficient(String... variables) {
		Term t = new Term(variables);
		for(int a = 0; a<terms.length; a++) {
			if(terms[a].variablesMatch(t)) {
				return terms[a].coefficient;
			}
		}
		throw new IllegalArgumentException(t+" not found");
	}
	
	/** Return the variables used in this Expression.
	 * 
	 * @return the variables used in this Expression.
	 */
	public Set<String> getVariables() {
		Set<String> set = new TreeSet<String>();
		for(int a = 0; a<terms.length; a++) {
			terms[a].getVariables(set);
		}
		return set;
	}
	
	Expression multiply(Expression e) {
		List<Term> newExpr = new ArrayList<Term>();
		
		for(int a = 0; a<terms.length; a++) {
			for(int b = 0; b<e.terms.length; b++) {
				Term newTerm = terms[a].multiply(e.terms[b]);
				insertTermIntoSortedList(newExpr, newTerm);
			}
		}
		Term[] array = newExpr.toArray(new Term[newExpr.size()]);
		return new Expression(false, array);
	}
	
	Expression add(Term newIncoming) {
		List<Term> newExpr = new ArrayList<Term>();
		
		boolean processed = false;
		for(int a = 0; a<terms.length; a++) {
			if(terms[a].variablesMatch(newIncoming)) {
				newExpr.add( terms[a].add(newIncoming) );
				processed = true;
			} else {
				newExpr.add( terms[a] );
			}
		}
		if(!processed)
			newExpr.add(newIncoming);

		Term[] array = newExpr.toArray(new Term[newExpr.size()]);
		return new Expression(false, array);
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
		return toString(null, format);
	}
	
	/** Factor this Expression by a certain variable.
	 * 
	 * @param variableName the variable to factor by.
	 * @return a map that relates the degree of the argument to the terms
	 * that are multiplied by it. For example if this Expression is "5*a*x*x+b*x*x-c*x+d"
	 * and you factor by x, then the map will relate 2 to "5*a+b", 1 to "-c", and 0 to "d".
	 */
	public SortedMap<Fraction, List<Term>> factor(String variableName) {
		SortedMap<Fraction, List<Term>> m = new TreeMap<Fraction, List<Term>>(new Comparator<Fraction>() {
			public int compare(Fraction i0, Fraction i1) {
				return -i0.compareTo(i1);
			}
		});

		for(Term t : terms) {
			Fraction power = t.getDegree(variableName);
			if(power==null)
				power = Fraction.get(0);
			List<Term> list = m.get(power);
			if(list==null) {
				list = new ArrayList<Term>();
				m.put(power, list);
			}
			
			Term newTerm = t.removeVariable(variableName);
			list.add(newTerm);
		}
		
		return m;
	}
	
	/** Write this Expression.
	 * 
	 * See {@link #factor(String)}
	 * 
	 * @param writer the destination to write this Expression to.
	 * @param variable the optional variable to factor by (this may be null).
	 * @param format the format to use
	 * @throws IOException this may be thrown by the Writer if an IO problem occurs.
	 * 
	 */
	public void write(Writer writer,String variable,OutputFormat format) throws IOException {
		if(variable==null) {
			//we're not factoring, just dump the list of terms as-is:
			if(terms.length==0) {
				writer.write("0");
			}
			for(int a = 0; a<terms.length; a++) {
				Term term = terms[a];
				String s = term.toString(format);
				
				if(s.length()>0) {
					if(a!=0) {
						if(s.charAt(0)!='-') {
							writer.write("+");
						}
					}
					writer.write(s);
				}
			}
			return;
		}
		
		SortedMap<Fraction, List<Term>> m = factor(variable);
		
		Iterator<Fraction> iter = m.keySet().iterator();
		boolean empty = true;
		while(iter.hasNext()) {
			Fraction f = iter.next();
			List<Term> list = m.get(f);
			
			String coeff = new Expression(false, list.toArray(new Term[list.size()])).toString(format);
			boolean isCoeffSingleTerm = list.size()<=1;
			
			if(!empty) {
				writer.write("+");
			}
			empty = false;
			
			if(f.isZero()) {
				if(isCoeffSingleTerm) {
					writer.write( coeff );
				} else {
					writer.write("(");
					writer.write( coeff );
					writer.write(")");
				}
			} else {
				if(OutputFormat.LATEX.equals(format)) {
					String var = variable;
					char c = variable.charAt(variable.length()-1);
					if(Character.isDigit(c) || c=='}') {
						var = var+"\\,";
					}
					
					if(f.equals(0)) {
						var = "";
					} else if(!f.equals(1)) {
						String s = f.toString();
						if(s.length()>1) {
							var = var+"^{"+s+"}";
						} else {
							var = var+"^"+s;
						}
					}
					
					if(isCoeffSingleTerm) {
						if(!coeff.equals("1")) {
							writer.write(coeff);
							writer.write("\\,");
						}
						writer.write(var);
					} else {
						writer.write(var);
						writer.write("\\,("+coeff+")");
					}
				} else {
					String var = variable;
					
					if(f.equals(0)) {
						var = "";
					} else if(!f.equals(1)) {
						String s = f.toString();
						if(!f.isInteger()) {
							var = var+"^("+s+")";
						} else {
							var = var+"^"+s;
						}
					}
					
					
					if(isCoeffSingleTerm) {
						if(!coeff.equals("1")) {
							writer.write(coeff);
							writer.write("*");
						}
						writer.write(var);
					} else {
						writer.write(var);
						writer.write("*("+coeff+")");
					}
				}
			}
		}
	}

	/** Output this expression, factoring by the argument provided.
	 * 
	 * @see #factor(String)
	 * @see #write(Writer, String, OutputFormat)
	 */
	public String toString(String variable,OutputFormat format) {
		StringWriter writer = new StringWriter();
		try {
			write(writer, variable, format);
		} catch(IOException e) {
			//what are the odds this can happen with a StringWriter?
			throw new RuntimeException(e);
		}
		return writer.toString();
	}
}
