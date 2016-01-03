/*
 * @(#)JavaClassSummary.java
 *
 * $Date: 2015-12-21 05:22:16 +0100 (Mo, 21 Dez 2015) $
 *
 * Copyright (c) 2015 by Jeremy Wood.
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
package com.bric.io.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.LinkedHashSet;
import java.util.Set;

import com.bric.io.Token;
import com.bric.io.java.JavaParser.DeclarationType;
import com.bric.io.java.JavaParser.JavaModifier;
import com.bric.io.java.JavaParser.WordToken;
import com.bric.util.Receiver;

/** This identifies basic crucial details about java source code.
 */
public class JavaClassSummary
{
	/** This is used to abort parsing tokens prematurely. */
	static class FinishedException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
	
	/** This interprets incoming Tokens to populate this JavaClassSummary's data */
	class MyReceiver implements Receiver<Token> {
		StringBuffer uncommittedPackageName = null;
		StringBuffer uncommittedImportStatement = null;
		
		@Override
		public void add(Token... tokens) {
			for(Token token : tokens) {
				if(token.getText().equals(";")) {
					if(uncommittedPackageName!=null) {
						packageName = uncommittedPackageName.toString().trim();
						uncommittedPackageName = null;
					} else if(uncommittedImportStatement!=null) {
						importedClasses.add( uncommittedImportStatement.toString().trim() );
						uncommittedImportStatement = null;
					}
				} else if(uncommittedPackageName!=null) {
					uncommittedPackageName.append(token.getText());
				} else if(uncommittedImportStatement!=null) {
					uncommittedImportStatement.append(token.getText());
				} else if (token.getText().equals("package")) {
					uncommittedPackageName = new StringBuffer();
				} else if(token.getText().equals("import")) {
					uncommittedImportStatement = new StringBuffer();
				} else if(token instanceof WordToken && ((WordToken)token).isModifier) {
					modifiers.add( JavaModifier.valueOf(token.getText().toUpperCase()) );
				} else if(token instanceof WordToken && ((WordToken)token).isDeclarationType) {
					declarationType = DeclarationType.valueOf(token.getText().toUpperCase());
				} else if(declarationType!=null && simpleName==null && token instanceof WordToken) {
					simpleName = token.getText();
				} else if(token.toString().equals("{")) {
					throw new FinishedException();
				}
			}
		}
		
	}
	
	protected String simpleName;
	protected String packageName;
	protected Set<String> importedClasses = new LinkedHashSet<>();
	protected Set<JavaModifier> modifiers = new LinkedHashSet<>();
	protected DeclarationType declarationType = null;
	
	/** Create a JavaClassSummary from a String
	 * 
	 * @param string the source code.
	 */
	public JavaClassSummary(String string) {
		try {
			JavaParser.parse(new StringReader(string), true, new MyReceiver());
		} catch(FinishedException e) {
			//do nothing, this is how we prematurely exit
		} catch (IOException e) {
			//this shouldn't happen for a StringReader
			throw new RuntimeException(e);
		}
	}
	
	/** Create a JavaClassSummary from an InputStream that refers to java source code.
	 * 
	 * @param in an InputStream to read the source code through.
	 * @param charset the character set to parse the input stream with.
	 * @throws IOException
	 */
	public JavaClassSummary(InputStream in,Charset charset) throws IOException {
		try {
			JavaParser.parse(new InputStreamReader(in, charset), true, new MyReceiver());
		} catch(FinishedException e) {
			//do nothing, this is how we prematurely exit
		}
	}
	
	/** Return the package name of this class.
	 * 
	 * @return the package name of this class. Such as "javax.swing".
	 */
	public String getPackageName() {
		return packageName;
	}
	
	/** Return the simple name of this class/interface/enum. For example
	 * if source code starts with "public class Foo" then this will return "Foo".
	 * 
	 * @return the simple name of this class/interface/enum.
	 */
	public String getSimpleName() {
		return simpleName;
	}
	
	/** Return the fully qualified name of this class/interface/enum. For example
	 * if type declaration starts with "package org.apache; public class Foo" then this will return "org.apache.Foo".
	 *
	 * 
	 * @return the fully qualified name of this class/interface/enum.
	 */
	public String getCanonicalName() {
		if(packageName.length()>0) {
			return packageName+"."+simpleName;
		}
		return simpleName;
	}

	/** Return the package name in a java file. */
	public static String getPackageName(String sourceCode) {
		JavaClassSummary summary = new JavaClassSummary(sourceCode);
		return summary.getPackageName();
	}

	/** Return the package name in a java file. */
	public static String getPackageName(File sourceCodeFile) throws IOException {
		try(FileInputStream in = new FileInputStream(sourceCodeFile)) {
			JavaClassSummary summary = new JavaClassSummary(in, Charset.forName("ISO-8859-1"));
			return summary.getPackageName();
		}
	}
}
