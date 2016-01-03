/*
 * @(#)Fraction.java
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

import java.io.Serializable;
import java.lang.ref.Reference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.bric.math.MathException.NegativeException;
import com.bric.math.PrimeFactors;

/** A immutable representation of a rational fraction.
 * <p>This does not support an arbitrary level of precision, but because
 * these are expressed as a series of prime factors they can express
 * values not normally represented by Java primitives.
 */
public class Fraction implements Serializable, Comparable<Fraction> {
	private static final long serialVersionUID = 1L;

	/** Return the minimum of two Fractions.
	 */
	public static Fraction min(Fraction f1,Fraction f2) {
		return (f1.compareTo(f2)<0) ? f1 : f2;
	}
	
	/** Return the maximum of two Fractions.
	 */
	public static Fraction max(Fraction f1,Fraction f2) {
		return (f1.compareTo(f2)>0) ? f1 : f2;
	}
	
	/** An integer, either backed by a java primitive or
	 * a BigInteger. The "Z" in the name is an arbitrary
	 * naming distinction to separate this (and its subclasses)
	 * from java primitives/classes. 
	 */
	private abstract static class ZInt implements Comparable<ZInt> {
		public abstract ZInt multiply(ZInt operand);
		public abstract ZInt add(ZInt operand);
		public abstract ZInt negate();
		
		public ZInt raiseTo(long exponent) {
			if(exponent==0) return new ZLong(1);
			if(exponent<0) throw new IllegalArgumentException(""+exponent);
			ZInt k = this;
			ZInt result = this;
			while(exponent>1) {
				result = result.multiply(k);
				exponent--;
			}
			return result;
		}
		
		/** Return a Long representation of this integer, or null if that
		 * is not possible.
		 */
		
		public abstract Long longValue();
		
		/** Return a BigInteger representation of this integer.
		 */
		public abstract BigInteger bigIntegerValue();
	}
	
	/** An integer that is backed by a long primitive. */
	private static class ZLong extends ZInt {
		long l;
		
		private ZLong(long l) {
			this.l = l;
		}

		@Override
		public ZInt multiply(ZInt operand) {
			boolean returnBigInt = false;
			if(operand instanceof ZLong) {
				long l2 = ((ZLong)operand).l;
				if(l==0 || l2==0) return new ZLong(0);
				
				long maximum = java.lang.Long.signum(l) == java.lang.Long.signum(l2) ? 
						java.lang.Long.MAX_VALUE : java.lang.Long.MIN_VALUE;

				if (l2 > 0 && l2 > maximum / l ||
				    l2 < 0 && l2 < maximum / l) {
					returnBigInt = true;
				}
				
				if(returnBigInt) {
					BigInteger b = BigInteger.valueOf(l);
					b = b.multiply(BigInteger.valueOf(l2));
					return new ZBig(b);
				}
				return new ZLong(l*l2);
			} else if(operand instanceof ZBig) {
				return operand.multiply(this);
			}
			
			//we only expect longs or bigs, so what's this?
			throw new InternalError( operand.toString()+" ("+operand.getClass().getName()+")");
		}

		@Override
		public ZInt add(ZInt operand) {
			boolean returnBigInt = false;
			if(operand instanceof ZLong) {
				long l2 = ((ZLong)operand).l;
				if(l>0 && l2>0) { // both positive
					if(l>Long.MAX_VALUE-l2) {
						returnBigInt = true;
					}
				} else if(l<0 && l2<0) { //both negative
					if(l<Long.MIN_VALUE-l2) {
						returnBigInt = true;
					}
				} else {
					//we're OK: a positive + negative cannot
					//overflow
				}
			
				if(returnBigInt) {
					BigInteger b = BigInteger.valueOf(l);
					b = b.add(BigInteger.valueOf(l2));
					return new ZBig(b);
				}
				
				return new ZLong(l + l2);
			} else if(operand instanceof ZBig) {
				return operand.add(this);
			}
			
			//we only expect longs or bigs, so what's this?
			throw new InternalError( operand.toString()+" ("+operand.getClass().getName()+")");
		}

		@Override
		public ZInt negate() {
			if(l==Long.MIN_VALUE) {
				BigInteger b = BigInteger.valueOf(l);
				return new ZBig( b.negate() );
			}
			return new ZLong(-l);
		}

		public int compareTo(ZInt o) {
			if(o instanceof ZLong) {
				long l2 = ((ZLong)o).l;
				if(l<l2)
					return -1;
				if(l>l2)
					return 1;
				return 0;
			}
			return -o.compareTo(this);
		}

		@Override
		public int hashCode() {
			//copied from java.lang.Long
			return (int)(l ^ (l >>> 32));
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ZLong) {
				return ((ZLong)obj).l==l;
			} else if(obj instanceof ZInt) {
				return obj.equals(this);
			}
			return false;
		}

		@Override
		public String toString() {
			return Long.toString(l);
		}

		public Long longValue() {
			return new Long(l);
		}
		
		public BigInteger bigIntegerValue() {
			return BigInteger.valueOf(l);
		}
	}

	/** An integer that is backed by a BigInteger. */
	private static class ZBig extends ZInt {
		BigInteger i;
		
		private ZBig(BigInteger i) {
			this.i = i;
		}
		
		private BigInteger getOperand(ZInt i) {
			if(i instanceof ZBig) {
				return ((ZBig)i).i;
			} else if(i instanceof ZLong) {
				return BigInteger.valueOf( ((ZLong)i).l );
			} else {
				//we only expect longs or bigs, so what's this?
				throw new InternalError( i.toString()+" ("+i.getClass().getName()+")");
			}
		}

		@Override
		public ZInt multiply(ZInt operand) {
			BigInteger t = getOperand(operand);
			BigInteger product = i.multiply(t);
			return reduce(product);
		}

		@Override
		public ZInt add(ZInt operand) {
			BigInteger t = getOperand(operand);
			BigInteger sum = i.add(t);
			return reduce(sum);
		}

		@Override
		public ZInt negate() {
			return reduce(i.negate());
		}
		
		private ZInt reduce(BigInteger bigInt) {
			long l = bigInt.longValue();
			BigInteger c = BigInteger.valueOf(l);
			if(c.equals(bigInt)) {
				return new ZLong( l );
			}
			return new ZBig( bigInt );
		}

		public int compareTo(ZInt o) {
			BigInteger b = getOperand( (ZInt)o );
			return i.compareTo(b);
		}

		@Override
		public int hashCode() {
			return i.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ZInt) {
				BigInteger b = getOperand( (ZInt)obj );
				return i.equals(b);
			}
			return false;
		}

		@Override
		public String toString() {
			return i.toString();
		}

		public Long longValue() {
			return null;
		}
		
		public BigInteger bigIntegerValue() {
			return i;
		}
	}
	
	private boolean negative;
	
	/** This will be null when this Fraction represents zero. */
	private SortedMap<ZInt, Integer> primeFactors;

	
	private final static List<Reference<Fraction>> allFractions = new ArrayList<Reference<Fraction>>();
	
	/** 
	 * @param value the value to store as a Fraction.
	 * @return a Fraction object representing a given long value.
	 */
	public synchronized static Fraction get(long value) {
		return get(value, 1);
	}

	/** 
	 * @param numerator the numerator of the required fraction.
	 * @param denominator the denominator of the required fraction.
	 * @return a Fraction object representing a numeric fraction.
	 */
	public static Fraction get(long numerator,long denominator) {
		Fraction f = new Fraction(numerator, denominator);
		return f;
	}
	
	private static Comparator<Reference<Fraction>> refComparator = new Comparator<Reference<Fraction>>(){

		NullPointerException ex = new NullPointerException();
		
		@Override
		public int compare(Reference<Fraction> o1, Reference<Fraction> o2) {
			Fraction f1 = o1.get();
			Fraction f2 = o2.get();
			if(f1==null || f2==null) throw ex;
			return f1.compareTo(f2);
		}
		
	};

	private Fraction(long numerator,long denominator) {
		if(numerator==0) {
			negative = false;
			primeFactors = null;
			return;
		}
		if(denominator==0) throw new IllegalArgumentException("denominator cannot be zero");
		
		negative = (numerator<0) ^ (denominator<0);
		
		if(numerator<0) numerator = -numerator;
		if(denominator<0) denominator = -denominator;
		try {
			primeFactors = new TreeMap<ZInt, Integer>();
			
			long[] nPrimes = numerator==1 ? new long[] {} : PrimeFactors.get(numerator);
			for(long l : nPrimes) {
				ZInt z = new ZLong(l);
				Integer f = primeFactors.get(z);
				if(f==null) {
					primeFactors.put(z, 1);
				} else {
					primeFactors.put(z, f+1);
				}
			}

			long[] dPrimes = denominator==1 ? new long[] {} : PrimeFactors.get(denominator);
			for(long l : dPrimes) {
				ZInt z = new ZLong(l);
				Integer f = primeFactors.get(z);
				if(f==null) {
					primeFactors.put(z, -1);
				} else {
					f = f-1;
					if(f==0) {
						primeFactors.remove(z);
					} else {
						primeFactors.put(z, f);
					}
				}
			}
		} catch(NegativeException e) {
			//this shouldn't happen: we should be negative negative values
			InternalError e2 = new InternalError();
			e2.initCause(e);
			throw e2;
		}
	}
	
	private static Fraction get(boolean negative, SortedMap<ZInt, Integer> primeFactors) {
		Fraction f = new Fraction(negative, primeFactors);
		return f;
	}
	
	private Fraction(boolean negative, SortedMap<ZInt, Integer> primeFactors) {
		this.negative = negative;
		this.primeFactors = primeFactors;
	}
	
	private transient ZInt denominator;
	private synchronized ZInt getDenominator() {
		if(denominator==null) {
			ZInt product = new ZLong(1);
			if(primeFactors!=null) {
				for(ZInt primeFactor : primeFactors.keySet()) {
					Integer exp = primeFactors.get(primeFactor);
					if(exp<0) {
						product = product.multiply( primeFactor.raiseTo(-exp.longValue()) );
					}
				}
			}
			denominator = product;
		}
		return denominator;
	}
	private transient ZInt numerator;
	private synchronized ZInt getNumerator() {
		if(numerator==null) {
			ZInt product = new ZLong(1);
			if(primeFactors!=null) {
				for(ZInt primeFactor : primeFactors.keySet()) {
					Integer exp = primeFactors.get(primeFactor);
					if(exp>0) {
						product = product.multiply( primeFactor.raiseTo(exp.longValue()) );
					}
				}
			}
			numerator = product;
		}
		return numerator;
	}

	/** Return this Fraction as a long value, or null
	 * if it cannot be converted to a long.
	 * 
	 * <p>If this Fraction represents an integer and this method
	 * returns null, then you can use {@link #bigIntegerValue()}.
	 */
	public Long longValue() {
		if(isZero()) return 0L;
		if(!isInteger()) return null;

		ZInt numerator = getNumerator();
		if(negative)
			numerator = numerator.negate();
		if(numerator instanceof ZLong)
			return numerator.longValue();
		return null;
	}

	/** @return this Fraction as a BigInteger value, or null
	 * if it cannot be expressed as an integer.
	 * 
	 * <p>You can also try {@link #longValue()}, which may return
	 * null if this Fraction is too large.
	 */
	public BigInteger bigIntegerValue() {
		if(isZero()) return BigInteger.ZERO;
		if(!isInteger()) return null;

		ZInt numerator = getNumerator();
		if(negative)
			numerator = numerator.negate();
		return numerator.bigIntegerValue();
	}

	private transient Double cachedDoubleValue = null;
	
	/** @return this fraction as a double.
	 * @see #bigDecimalValue()
	 */
	public double doubleValue() {
		if(cachedDoubleValue==null) {
			cachedDoubleValue = bigDecimalValue().doubleValue();
		}
		return cachedDoubleValue;
	}

	/** @return this Fraction as an int value, or null
	 * if it cannot be converted to an int.
	 * 
	 * <p>If this Fraction represents an integer and this method
	 * returns null, then you can use {@link #bigIntegerValue()}
	 * or {@link #longValue()}.
	 */
	public Integer intValue() {
		long l = longValue();
		int i = (int)l;
		if(i!=l)
			return null;
		return i;
	}
	
	/** @return this fraction as a BigDecimal.
	 * This will never return null.
	 * @see #doubleValue()
	 */
	public BigDecimal bigDecimalValue() {
		if(isZero())
			return BigDecimal.ZERO;
		
		ZInt numerator = getNumerator();
		ZInt denominator = getDenominator();
		
		BigInteger bNum = numerator.bigIntegerValue();
		BigInteger bDen = denominator.bigIntegerValue();
		
		BigDecimal result = new BigDecimal(bNum);
		result = result.divide( new BigDecimal(bDen), MathContext.DECIMAL128 );
		if(negative)
			result = result.negate();
		return result;
	}

	/** Add two values.
	 * 
	 * @return the addition of this Fraction and the operand.
	 */
	public Fraction add(long operand) {
		return add(Fraction.get(operand));
	}
	
	/** Add two Fractions.
	 * 
	 * @return the addition of this Fraction and the operand.
	 */
	public Fraction add(Fraction operand) {
		if(operand.isZero()) return this;
		if(this.isZero()) return operand;
		if(primeFactors.equals(operand.primeFactors) && negative!=operand.negative)
			return Fraction.get(0);
		
		/* Break this down, so if we're adding (a*b)+(a*c) then we
		 * isolate a (as "commonFraction"), and only compute (b+c):
		 */
		
		SortedMap<ZInt, Integer> commonFactors = new TreeMap<ZInt, Integer>();
		for(ZInt primeFactor : primeFactors.keySet()) {
			Integer exp1 = primeFactors.get(primeFactor);
			Integer exp2 = operand.primeFactors.get(primeFactor);
			if(exp2!=null) {
				if(exp1<0 && exp2<0) {
					int max = Math.max(exp1, exp2);
					commonFactors.put(primeFactor, max);
				} else if(exp1>0 && exp2>0) {
					int min = Math.min(exp1, exp2);
					commonFactors.put(primeFactor, min);
				}
			}
		}
		Fraction commonFraction = new Fraction(false, commonFactors);
		
		Fraction reducedA = divideBy(commonFraction);
		Fraction reducedB = operand.divideBy(commonFraction);
		
		
		ZInt numeratorA = reducedA.getNumerator().multiply( reducedB.getDenominator() );
		ZInt numeratorB = reducedB.getNumerator().multiply( reducedA.getDenominator() );
		if(reducedA.negative) numeratorA = numeratorA.negate();
		if(reducedB.negative) numeratorB = numeratorB.negate();
		ZInt commonDenominator = reducedA.getDenominator().multiply( reducedB.getDenominator() );
		
		ZInt sum = numeratorA.add(numeratorB);
		
		/* Now what we effectively want to return is:
		 * 
		 * Fraction reducedSum = new Fraction(sum.longValue(), commonDenominator.longValue());
		 * return reducedSum.multiply(commonFraction);
		 * 
		 * ... however: we can reduce this even more.
		 * Suppose sum.longValue() returns null (because it can only be expressed as a BigInteger),
		 * but it is actually divisible by commonDenominator. And suppose it could effectively
		 * be reduced to a long -- which is the arbitrary line-in-the-sand we've declared support for.
		 */
		
		Long cdl = commonDenominator.longValue();
		if(cdl==null)
			throw new NullPointerException("overflow executing "+this+"+"+operand);
		
		long[] factors;
		try {
			factors = PrimeFactors.get(cdl);
		} catch (NegativeException e) {
			InternalError e2 = new InternalError("unexpected condition");
			e2.initCause(e);
			throw e2;
		}
		
		long reducedDenominator = 1;
		BigInteger reducedNumerator = sum.bigIntegerValue();
		for(int a = 0; a<factors.length; a++) {
			BigInteger[] d = reducedNumerator.divideAndRemainder( BigInteger.valueOf(factors[a]) );
			if(d[1].equals(BigInteger.ZERO)) {
				reducedNumerator = d[0];
			} else {
				reducedDenominator = reducedDenominator*factors[a];
			}
		}
		
		Long reducedNumeratorV = reducedNumerator.longValue();
		if(!BigInteger.valueOf(reducedNumeratorV).equals(reducedNumerator)) {
			throw new NullPointerException("overflow executing "+this+"+"+operand);
		}
		
		Fraction reducedSum = new Fraction(reducedNumeratorV, reducedDenominator);
		return reducedSum.multiply(commonFraction);
	}
	
	/** Subtract the operand from this Fraction.
	 * 
	 * @param operand the value to subtract from this object.
	 * @return the difference of this value minus the operand.
	 */
	public Fraction subtract(Fraction operand) {
		return add( operand.negate() );
	}

	/** Subtract the operand from this Fraction.
	 * 
	 * @param operand the value to subtract from this object.
	 * @return the difference of this value minus the operand.
	 */
	public Fraction subtract(long operand) {
		return subtract(Fraction.get(operand));
	}
	
	/** Multiply this value by the operand.
	 * 
	 * @param operand the value to multiply this by.
	 * @return the product of this Fraction and the operand.
	 */
	public Fraction multiply(Fraction operand) {
		if(this.isZero())
			return this;
		if(operand.isZero())
			return operand;
		return Fraction.get(
			negative ^ operand.negative,
			multiply(primeFactors, operand.primeFactors) );
	}
	
	/** Divide this Fraction by the operand.
	 * 
	 * @param operand The operand to divide by.
	 * @return the quotient of this Fraction divided by the operand.
	 * @throws IllegalArgumentException if the operand is zero.
	 */
	public Fraction divideBy(Fraction operand) {
		if(operand.isZero()) throw new IllegalArgumentException("cannot divide by zero");
		if(this.isZero()) return this;
		return multiply(operand.invert());
		
	}
	
	/** @return true if this is Fraction represents a constant integer.
	 * 
	 */
	public boolean isInteger() {
		if(isZero()) return true;
		ZInt denominator = getDenominator();
		return (denominator instanceof ZLong && ((ZLong)denominator).l==1);
	}
	
	/** Invert this fraction.
	 * 
	 * @return an inverted copy of this Fraction.
	 * @throws IllegalStateException if this fraction is zero.
	 */
	public Fraction invert() {
		if(isZero()) throw new IllegalStateException("cannot invert the fraction \"0/1\"");
		return Fraction.get(negative, invert(primeFactors) );
	}
	
	/** If this Fraction is (a/b), then this returns (1/b)
	 */
	public Fraction isolateDenominator() {
		if(isZero())
			throw new RuntimeException("this method cannot be invoked on 0");
		SortedMap<ZInt, Integer> newMap = new TreeMap<ZInt, Integer>();
		for(ZInt primeFactor : primeFactors.keySet()) {
			Integer power = primeFactors.get(primeFactor);
			if(power<0) {
				newMap.put(primeFactor, power);
			}
		}
		return Fraction.get(false, newMap);
	}
	
	/** If this Fraction is [a/(b*c)] and the argument is [x/(b*y)]
	 * then this returns (1/(b*c*y)).
	 * 
	 */
	public Fraction isolateLCD(Fraction other) {
		if(isZero())
			throw new RuntimeException("this method cannot be invoked on 0");
		if(other.isZero())
			throw new RuntimeException("this method cannot be invoked on 0");
		
		SortedMap<ZInt, Integer> newMap = new TreeMap<ZInt, Integer>();
		for(ZInt primeFactor : primeFactors.keySet()) {
			Integer myPower = primeFactors.get(primeFactor);
			if(myPower<0) {
				Integer otherPower = other.primeFactors.get(primeFactor);
				if(otherPower!=null && otherPower<0) {
					newMap.put(primeFactor, Math.min(myPower, otherPower));
				} else {
					newMap.put(primeFactor, myPower);
				}
			}
		}
		for(ZInt primeFactor : other.primeFactors.keySet()) {
			Integer otherPower = other.primeFactors.get(primeFactor);
			if(otherPower<0) {
				if(primeFactors.get(primeFactor)==null) {
					newMap.put(primeFactor, otherPower);
				}
			}
		}
		return Fraction.get(false, newMap);
	}

	
	/** If this Fraction is [a/(b*c)] and the argument is [x/(b*y)]
	 * then this returns (1/b).
	 * 
	 */
	public Fraction isolateCommonDenominator(Fraction other) {
		if(isZero())
			throw new RuntimeException("this method cannot be invoked on 0");
		if(other.isZero())
			throw new RuntimeException("this method cannot be invoked on 0");
		
		SortedMap<ZInt, Integer> newMap = new TreeMap<ZInt, Integer>();
		for(ZInt primeFactor : primeFactors.keySet()) {
			Integer myPower = primeFactors.get(primeFactor);
			if(myPower<0) {
				Integer otherPower = other.primeFactors.get(primeFactor);
				if(otherPower!=null && otherPower<0) {
					newMap.put(primeFactor, Math.max(myPower, otherPower));
				}
			}
		}
		return Fraction.get(false, newMap);
	}

	/** If this Fraction is (a/b), then this returns (a/1)
	 */
	public Fraction isolateNumerator() {
		if(isZero()) return this;
		
		SortedMap<ZInt, Integer> newMap = new TreeMap<ZInt, Integer>();
		for(ZInt primeFactor : primeFactors.keySet()) {
			Integer power = primeFactors.get(primeFactor);
			if(power>0) {
				newMap.put(primeFactor, power);
			}
		}
		return Fraction.get(false, newMap);
	}
	
	private static <T> SortedMap<T, Integer> invert(SortedMap<T, Integer> map) {
		if(map==null) return null;
		SortedMap<T, Integer> inverse = new TreeMap<T, Integer>();
		Iterator<T> keys = map.keySet().iterator();
		while(keys.hasNext()) {
			T key = keys.next();
			Integer exp = map.get(key);
			inverse.put(key, -exp);
		}
		return inverse;
	}
	
	private static <T> SortedMap<T, Integer> multiply(SortedMap<T, Integer> map1, SortedMap<T, Integer> map2) {
		if(map1==null) {
			return map2;
		}
		if(map2==null) return map1;
		
		SortedMap<T, Integer> product = new TreeMap<T, Integer>();
		Iterator<T> keys = map1.keySet().iterator();
		while(keys.hasNext()) {
			T key = keys.next();
			Integer exp1 = map1.get(key);
			Integer exp2 = map2.get(key);
			if(exp2==null) {
				product.put(key, exp1);
			} else {
				Integer sum = exp1+exp2;
				if(sum!=0) {
					product.put(key, sum);
				} else {
					product.remove(key);
				}
			}
		}
		keys = map2.keySet().iterator();
		while(keys.hasNext()) {
			T key = keys.next();
			if( !map1.containsKey(key) ) {
				Integer exp2 = map2.get(key);
				product.put(key, exp2);
			}
		}
		return product;
	}
	
	/** Raise this Fraction to an exponent.
	 * 
	 * @param f the exponent to raise this value to.
	 * @return a Fraction that raises this to the exponent.
	 * @throws IrrationalException if this operation will involve an irrational number.
	 * @throws ImaginaryException if this operation will involve an imaginary number.
	 */
	public Fraction raiseToPower(Fraction f) throws IrrationalException, ImaginaryException {
		ZInt d = f.getDenominator();
		ZInt n = f.getNumerator();
		return getRoot(d.longValue()).raiseToPower(n.longValue());
	}

	/** Raise this Fraction to an exponent.
	 * 
	 * @param l the exponent to raise this value to.
	 * @return a Fraction that raises this to the exponent.
	 */
	public Fraction raiseToPower(long l) {
		if(l==0) {
			return Fraction.get(1);
		} else if(l<0) {
			throw new IllegalArgumentException("l ("+l+") must not be negative; see getRoot()");
		}
		if(isZero()) return this;
		
		ZLong z = new ZLong(l);
		SortedMap<ZInt, Integer> resultMap = new TreeMap<ZInt, Integer>();
		for(ZInt primeFactor : primeFactors.keySet()) {
			Integer exponent = primeFactors.get(primeFactor);
			ZInt e = z.multiply(new ZLong(exponent));
			if(e instanceof ZLong) {
				resultMap.put(primeFactor, (int)( ((ZLong)e).l ) );
			} else {
				throw new IllegalArgumentException("overflow: ("+this+")^"+l);
			}
		}
		boolean resultNegative = (l%2==0) ? false : negative;
		return Fraction.get(resultNegative, resultMap);
	}
	
	/** 
	 * 
	 * @param l if this is 2, then this will return a square root.
	 * @return a root of this Fraction.
	 * @throws IrrationalException if this operation would introduce an irrational number.
	 * @throws ImaginaryException if this operation would introduce an imaginary number.
	 */
	public Fraction getRoot(long l) throws IrrationalException, ImaginaryException {
		if(isZero()) return this;
		if(l<0) throw new IllegalArgumentException("root must be positive: "+l);
		
		if(negative && (l%2==0))
			throw new ImaginaryException("this fraction ("+toString()+") cannot be raised to (1/"+l+")");
		SortedMap<ZInt, Integer> rootMap = new TreeMap<ZInt, Integer>();
		for(ZInt primeFactor : primeFactors.keySet()) {
			Integer exponent = primeFactors.get(primeFactor);
			if( (exponent%l)==0 ) {
				exponent = (int)(exponent/l);
				rootMap.put(primeFactor, exponent);
			} else {
				throw new IrrationalException("this fraction ("+toString()+") cannot be raised to (1/"+l+")");
			}
		}
		return Fraction.get(negative, rootMap);
	}
	
	/**
	 * Negate this Fraction.
	 * 
	 * @return a negation of this Fraction.
	 * 
	 */
	public Fraction negate() {
		if(isZero()) return this;
		return Fraction.get(!negative, primeFactors);
	}
	
	/** @return true if this Fraction is zero. */
	public boolean isZero() {
		return primeFactors==null;
	}
	
	private transient Integer hashCode;
	@Override
	public synchronized int hashCode() {
		if(hashCode==null) {
			if(primeFactors==null) {
				hashCode = -1;
			} else {
				hashCode = new Integer( primeFactors.hashCode() + (negative ? 1 : 0) );
			}
		}
		return hashCode;
	}

	/** Return true if this Fraction is equal to the argument.
	 * 
	 * @param obj a Fraction or Number (currently only ints, shorts, longs and bytes
	 * are supported).
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Fraction)) {
			if(obj instanceof Integer ||
					obj instanceof Short ||
					obj instanceof Long || 
					obj instanceof Byte) {
				Number k = (Number)obj;
				Fraction f = Fraction.get( k.longValue() );
				return equals(f);
			} else if(obj instanceof Number) {
				//TODO: when we add a constructor for floats/doubles, implement this
			}
			return false;
		}
		return compareTo( (Fraction)obj )==0;
	}

	@Override
	public String toString() {
		if(this.isZero()) return "0";
		String sign = negative ? "-" : "";
		if(isInteger()) {
			return sign + getNumerator().toString();
		}
		return sign + getNumerator()+"/"+getDenominator();
	}

	/** Compares this Fraction to the operand.
	 * 
	 * @param value the value to compare this object against.
	 * @return -1 if this object is less than the argument, 0 if it is equal, and 1 if it is greater.
	 */
	public int compareTo(long value) {
		return compareTo(Fraction.get(value));
	}
	
	@Override
	public int compareTo(Fraction f) {
		if(f==this) return 0;
		if(isZero()) {
			if(f.isZero()) return 0;
			if(f.negative) return 1;
			return -1;
		} else if(f.isZero()) {
			return -f.compareTo(this);
		}
		
		if( negative && (!f.negative))
			return -1;
		if( (!negative) && f.negative)
			return 1;
		
		if(primeFactors.equals(f.primeFactors) && negative==f.negative)
			return 0;
		
		Fraction k = this.divideBy(f);
		ZInt numerator = k.getNumerator();
		ZInt denominator = k.getDenominator();
		
		int z = numerator.compareTo(denominator);
		if(negative)
			z = -z;
		return z;
	}

	/** 
	 * 
	 * @param f the Fraction to compare against.
	 * @return the minimum overlap of prime factors between two factors.
	 */
	public Fraction getCommonElements(Fraction f) {
		final SortedMap<ZInt, Integer> returnValue = new TreeMap<ZInt, Integer>();
		for(ZInt i : f.primeFactors.keySet()) {
			Integer f1 = f.primeFactors.get(i);
			Integer f2 = primeFactors.get(i);
			if(f2!=null) {
				int c1 = f1.compareTo(0);
				int c2 = f2.compareTo(0);
				if(c1<0 && c2<0) {
					returnValue.put(i, Math.max(f1,  f2));
				} else if(c1>0 && c2>0) {
					returnValue.put(i, Math.min(f1, f2));
				}
			}
		}
		return Fraction.get(false, returnValue);
	}
}
