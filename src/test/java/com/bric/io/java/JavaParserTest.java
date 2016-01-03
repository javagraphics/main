/*
 * @(#)JavaParserTest.java
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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.bric.io.ParserException;
import com.bric.io.Token;
import com.bric.io.java.JavaParser.CharToken;
import com.bric.io.java.JavaParser.CommentToken;
import com.bric.io.java.JavaParser.DoubleToken;
import com.bric.io.java.JavaParser.NumberToken;
import com.bric.io.java.JavaParser.StringToken;
import com.bric.io.java.JavaParser.SymbolCharToken;
import com.bric.io.java.JavaParser.WhitespaceToken;
import com.bric.io.java.JavaParser.WordToken;
import com.bric.util.BasicReceiver;

import junit.framework.TestCase;

public class JavaParserTest extends TestCase {
	
	public void testMultilineJavadoc() throws Exception {
		String[] expressions = new String[] {
				"/** This is some sample javadoc */",
				"/** This is some\n * sample javadoc */",
				"/** This is some\n * sample\n javadoc */",
				"/**\n * This is some sample javadoc\n */"
		};
		testBasicTokens(expressions, CommentToken.class, true);
		
		String[] badStrings = new String[] {
				"/** Bad javadoc"
		};
		testBadBasicTokens(badStrings);
	}


	
	public void testNumbers() throws Exception {
		String[] expressions = new String[] {
				"3",
				"123",
				"123L",
				"3.0",
				"3.0f",
				"-39",
				"-3.9",
				"-.9",
				"+.9",
				"+40",
				"+.6",
				"+.63d",
				"-.55d",
				"404D",
				"3e99",
				"123e+3",
				"123e-1245",
				"3.0e22",
				"3.0e-15",
				"-39E9",
				"-3.9E-2"
		};
		testBasicTokens(expressions, NumberToken.class, false);
		
		String[] badStrings = new String[] {
				"-.a",
				"+3.",
				"3ee99",
				"123e+",
				"123e",
				"3.0e-",
				"3.0e.4",
				"-39E3.4",
				"-3.9E-E"
		};
		testBadBasicTokens(badStrings);
	}

	
	public void testSingleLineJavadoc() throws Exception {
		String[] expressions = new String[] {
				"//single-line javadoc"
		};
		testBasicTokens(expressions, CommentToken.class, false);
	}
	
	public void testString() throws Exception {
		String[] expressions = new String[] {
				"\"hello world\"",
				"\"hello\tworld\"",
				"\"hello\t\\\"world\"",
				//real-life example:
				"\"Problems:\\n\\nThis rule cannot be used because its source code has compiler errors.\\n\\nTo learn more:\\nClick this rule in the tree on the left, and type the right arrow key to navigate to the source code. Then click the \\\"Compile\\\" button to see compiler feedback.\""
		};
		Token[] token = testBasicTokens(expressions, StringToken.class, true);
		assertEquals( "hello world", ((StringToken)token[0]).getDecodedString());
		assertEquals( "hello\tworld", ((StringToken)token[1]).getDecodedString());
		assertEquals( "hello\t\"world", ((StringToken)token[2]).getDecodedString());

		String[] badStrings = new String[] {
				"\"hello world",
				"\"hello\nworld\"",
				"\"hello\\u123world\"",
		};
		testBadBasicTokens(badStrings);
	}

	
	public void testChars() throws Exception {
		String[] expressions = new String[] {
				"'x'",
				"'\\t'",
				"'\\u0123'",
				"'\\123'",
				"'\\45'",
				"'\\4'"
		};
		Token[] tokens = testBasicTokens(expressions, CharToken.class, true);

		assertEquals( ((CharToken)tokens[0]).getDecodedChar(), 'x');
		assertEquals( ((CharToken)tokens[1]).getDecodedChar(), '\t');
		assertEquals( ((CharToken)tokens[2]).getDecodedChar(), '\u0123');
		assertEquals( ((CharToken)tokens[3]).getDecodedChar(), '\123');
		assertEquals( ((CharToken)tokens[4]).getDecodedChar(), '\45');
		assertEquals( ((CharToken)tokens[5]).getDecodedChar(), '\4');
		
		String[] badStrings = new String[] {
				"''",
				"'ha'",
				"'\\h'",
				"'\\u01234'",
				"'\\u34'",
				"'\\3456'"
		};
		testBadBasicTokens(badStrings);
	}	
	
	protected void testBadBasicTokens(String[] expressions) throws IOException {
		for(String expr : expressions) {
			BasicReceiver<Token> receiver = new BasicReceiver<>();
			JavaParser.parse(new StringReader(expr), true, receiver);
			Token[] tokens = receiver.toArray(new Token[receiver.getSize()]);
			if(tokens.length==1) {
				for(int a = 0; a<tokens.length; a++) {
					if(tokens[a].getException()==null) {
						fail(expr);
					}
				}
			} else {
				//if it parses 2 tokens, maybe that's OK.
				//for example: "-39E3.4" is not correctly formed,
				//but if the parser saw this as two consecutive numbers that's
				//an indication that it's OK.
				//(A compiler can then perk up and say: "Hey, no two consecutive numbers!")
			}
		}
	}
	
	protected Token[] testBasicTokens(String[] expressions,Class<? extends Token> tokenType,boolean testMergedTokens) throws IOException {
		StringBuffer sum = new StringBuffer();
		List<Token> tokens = new ArrayList<>();
		for(String expr : expressions) {
			BasicReceiver<Token> receiver = new BasicReceiver<>();
			JavaParser.parse(new StringReader(expr), true, receiver);
			for(int a = 0; a<receiver.getSize(); a++) {
				tokens.add(receiver.getElementAt(a));
				ParserException ex = receiver.getElementAt(a).getException();
				if(ex!=null) {
					System.err.println("This expression failed:");
					System.err.println(expr);
					throw ex;
				}
			}
			assertTrue(expr, receiver.getSize()==1 );
			assertTrue( receiver.getElementAt(0).getClass().getName(), tokenType.isAssignableFrom(receiver.getElementAt(0).getClass()) );
			assertEquals( receiver.getElementAt(0).getText(), expr);
			sum.append(expr);
		}

		if(testMergedTokens) {
			BasicReceiver<Token> receiver = new BasicReceiver<>();
			JavaParser.parse(new StringReader(sum.toString()), true, receiver);
			int pos = 0;
			assertTrue( receiver.getSize()==expressions.length );
			for(int a = 0; a<receiver.getSize(); a++) {
				assertTrue( receiver.getElementAt(a).getClass().equals(tokenType) );
				assertEquals( expressions[a], receiver.getElementAt(a).getText() );
				assertEquals( pos, receiver.getElementAt(a).getDocumentStartIndex() );
				assertEquals( pos + expressions[a].length(), receiver.getElementAt(a).getDocumentEndIndex() );
				pos += expressions[a].length();
			}
		}
		return tokens.toArray(new Token[tokens.size()]);
	}
	
	public void testSampleExpression() throws IOException {
		String expr = "methodName( 3.0, \"simpleString\", x);";
		Token[] tokens = JavaParser.parse(expr, true);
		
		assertEquals( new WordToken("methodName", 0), tokens[0]);
		assertEquals( new SymbolCharToken( '(', 10), tokens[1]);
		assertEquals( new WhitespaceToken( " ", 11), tokens[2]);
		assertEquals( new DoubleToken("3.0", 12), tokens[3]);
		assertEquals( new SymbolCharToken( ',', 15), tokens[4]);
		assertEquals( new WhitespaceToken(" ", 16), tokens[5]);
		assertEquals( new StringToken( "\"simpleString\"", "simpleString", 17), tokens[6]);
		assertEquals( new SymbolCharToken( ',', 31), tokens[7]);
		assertEquals( new WhitespaceToken( " ", 32), tokens[8]);
		assertEquals( new WordToken( "x", 33), tokens[9]);
		assertEquals( new SymbolCharToken( ')', 34), tokens[10]);
		assertEquals( new SymbolCharToken(';', 35), tokens[11]);

		tokens = JavaParser.parse(expr, false);
		assertEquals( new WordToken("methodName", 0), tokens[0]);
		assertEquals( new SymbolCharToken( '(', 10), tokens[1]);
		assertEquals( new DoubleToken("3.0", 12), tokens[2]);
		assertEquals( new SymbolCharToken( ',', 15), tokens[3]);
		assertEquals( new StringToken( "\"simpleString\"", "simpleString", 17), tokens[4]);
		assertEquals( new SymbolCharToken( ',', 31), tokens[5]);
		assertEquals( new WordToken( "x", 33), tokens[6]);
		assertEquals( new SymbolCharToken( ')', 34), tokens[7]);
		assertEquals( new SymbolCharToken(';', 35), tokens[8]);
		
		expr = "methodName( 3.0, \"simple\\\"String\\\"\", x);";
		tokens = JavaParser.parse(expr, false);
		assertEquals( new WordToken("methodName", 0), tokens[0]);
		assertEquals( new SymbolCharToken( '(', 10), tokens[1]);
		assertEquals( new DoubleToken("3.0", 12), tokens[2]);
		assertEquals( new SymbolCharToken( ',', 15), tokens[3]);
		assertEquals( new StringToken( "\"simple\\\"String\\\"\"", "simple\"String\"", 17), tokens[4]);
		assertEquals( new SymbolCharToken( ',', 35), tokens[5]);
		assertEquals( new WordToken( "x", 37), tokens[6]);
		assertEquals( new SymbolCharToken( ')', 38), tokens[7]);
		assertEquals( new SymbolCharToken(';', 39), tokens[8]);
		
		expr = "methodName( 3.0, \"simple\\u0123\", x);";
		tokens = JavaParser.parse(expr, false);
		assertEquals( new WordToken("methodName", 0), tokens[0]);
		assertEquals( new SymbolCharToken( '(', 10), tokens[1]);
		assertEquals( new DoubleToken("3.0", 12), tokens[2]);
		assertEquals( new SymbolCharToken( ',', 15), tokens[3]);
		assertEquals( new StringToken( "\"simple\\u0123\"", "simple\u0123", 17), tokens[4]);
	}
	
	public void testVariableNames() {
		String expr = "hello_world";
		Token[] tokens = JavaParser.parse(expr, true);
		assertEquals(new WordToken("hello_world", 0), tokens[0]);
		assertEquals(1, tokens.length);
	}
	
	public void testBadSampleExpression() {
		//we should fail to parse an unclosed String expression:
		{
			String expr = "methodName( 3.0, \"simpleString";
			Token[] tokens = JavaParser.parse(expr, true);
			assertTrue( tokens[6].getException()!=null );
			assertEquals(17, tokens[6].getDocumentStartIndex() );
		}
		
		//we should fail to parse a unicode escaped char with only 3 digits (instead of 4)
		{
			String expr = "methodName( 3.0, \"simple\\u012 \"";
			Token[] tokens = JavaParser.parse(expr, true);
			assertEquals(17, tokens[6].getDocumentStartIndex() );
		}
	}
}
