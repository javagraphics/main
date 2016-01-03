/*
 * @(#)JavaTextComponentHighlighter.java
 *
 * $Date: 2015-12-27 07:28:02 +0100 (So, 27 Dez 2015) $
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
package com.bric.swing;

import java.awt.Color;
import java.awt.Font;

import javax.swing.text.AttributeSet;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.bric.io.Token;
import com.bric.io.java.JavaParser.CharToken;
import com.bric.io.java.JavaParser.CommentToken;
import com.bric.io.java.JavaParser.StringToken;
import com.bric.io.java.JavaParser.SymbolCharToken;
import com.bric.io.java.JavaParser.WordToken;
import com.bric.text.TextComponentHighlighter;

/** This highlights text using a monospaced formatting scheme similar to the default
 * Eclipse formatting options.
 */
public class JavaTextComponentHighlighter extends TextComponentHighlighter {

	protected SimpleAttributeSet defaultAttributes;
	protected SimpleAttributeSet keywordAttributes;
	protected SimpleAttributeSet commentAttributes;
	protected SimpleAttributeSet stringAttributes;
	protected SimpleAttributeSet importantPunctuationAttributes;

	/** Create a new JavaTextComponentHighlighter.
	 * 
	 * @param jtc the text component to apply formatting to.
	 */
	public JavaTextComponentHighlighter(JTextComponent jtc) {
		super(jtc);

		jtc.putClientProperty("caretWidth", new Integer(3));
		jtc.getCaret().setBlinkRate(500);

		initializeAttributes();
		
		Font defaultFont = new Font(StyleConstants.getFontFamily(defaultAttributes), 0, StyleConstants.getFontSize(defaultAttributes));
		jtc.setFont( defaultFont );
	}

	/** Initialize the SimpleAttributeSets used to format text. */
	protected void initializeAttributes() {
		Color keywordColor = new Color(127, 0, 85);
		Color commentColor = new Color(0, 140, 0);
		Color stringColor = new Color(45, 0, 255);

		defaultAttributes = new SimpleAttributeSet();
		StyleConstants.setFontFamily(defaultAttributes, "Courier");
		StyleConstants.setFontSize(defaultAttributes, 14);

		keywordAttributes = new SimpleAttributeSet(defaultAttributes);
		StyleConstants.setBold(keywordAttributes, true);
		StyleConstants.setForeground(keywordAttributes, keywordColor);

		commentAttributes = new SimpleAttributeSet(defaultAttributes);
		StyleConstants.setForeground(commentAttributes, commentColor);

		stringAttributes = new SimpleAttributeSet(defaultAttributes);
		StyleConstants.setForeground(stringAttributes, stringColor);

		importantPunctuationAttributes = new SimpleAttributeSet(defaultAttributes);
		StyleConstants.setBold(importantPunctuationAttributes, true);
	}

	@Override
	protected AttributeSet getAttributes(Token[] tokens, int tokenIndex, int selectionStart, int selectionEnd)
	{
		Token token = tokens[tokenIndex];
		if (token instanceof StringToken || token instanceof CharToken)
		{
			return stringAttributes;
		}

		if (token instanceof WordToken)
		{
			WordToken word = (WordToken) token;
			if (word.isKeyword || word.isLiteral)
				return keywordAttributes;
		}

		if (token instanceof CommentToken)
		{
			return commentAttributes;
		}

		if (token instanceof SymbolCharToken)
		{
			char ch = ((SymbolCharToken) token).getChar();
			if (ch == ';' || ch == ',')
			{
				return importantPunctuationAttributes;
			}
		}
		return defaultAttributes;
	}
}
