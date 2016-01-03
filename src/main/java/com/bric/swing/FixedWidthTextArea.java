/*
 * @(#)FixedWidthTextArea.java
 *
 * $Date: 2015-02-28 21:59:45 +0100 (Sa, 28 Feb 2015) $
 *
 * Copyright (c) 2011 by Jeremy Wood.
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

import java.awt.Dimension;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import com.bric.blog.Blurb;


/** This is a JTextArea with a fixed width.  Its preferred height will adjust depending
 * on the text inside it.
 *
 * @deprecated see com.bric.awt.TextSize
 */
@Deprecated
@Blurb (
filename = "FixedWidthTextArea",
title = "Text: Text Height and GUI Layout",
releaseDate = "June 2008",
summary = "This walks through how to find the preferred height of a block of text, "+
		"and provides a GUI component to display text with a fixed width.",
instructions = "",
link = "http://javagraphics.blogspot.com/2008/06/text-height-gui-layout-and-text-boxes.html",
sandboxDemo = false
)
public class FixedWidthTextArea extends JTextArea {
	private static final long serialVersionUID = 1L;
	
	public static final boolean isXP = System.getProperty("os.name").toLowerCase().indexOf("xp")!=-1;
	public static final boolean isMac = System.getProperty("os.name").toLowerCase().indexOf("mac")!=-1;

	int fixedWidth = -1;
	Dimension cachedSize = null;
	private final DocumentListener docListener = new DocumentListener() {

		public void changedUpdate(DocumentEvent e)
		{
			cachedSize = null;
		}

		public void insertUpdate(DocumentEvent e)
		{
			changedUpdate(e);
		}

		public void removeUpdate(DocumentEvent e)
		{
			changedUpdate(e);
		}
		
	};
	
	public FixedWidthTextArea(String text,int fixedWidth)
	{
		super(text);
		setFixedWidth(fixedWidth);
	}

	public FixedWidthTextArea(int fixedWidth)
	{
		super();
		setFixedWidth(fixedWidth);
	}

	public FixedWidthTextArea(Document doc,int fixedWidth)
	{
		super(doc);
		setFixedWidth(fixedWidth);
	}
	
	@Override
	public void setDocument(Document d) {
		Document oldDocument = getDocument();
		if(oldDocument!=null) oldDocument.removeDocumentListener(docListener);
		super.setDocument(d);
		d.addDocumentListener(docListener);
		cachedSize = null;
	}
	
	public void setFixedWidth(int i) {
		if(i==fixedWidth) return;
		fixedWidth = i;
		cachedSize = null;
	}

	@Override
	public Dimension getPreferredSize() {
		if(cachedSize==null) {
			Hashtable<Attribute, Object> attributes = new Hashtable<Attribute, Object>();
			attributes.put( TextAttribute.FONT, getFont());
			String text = getText();
			
			/** It is crucial this be accurate!  I used to have it
			 * always true/true, and XP sometimes failed because of it.
			 */
			FontRenderContext frc = isMac ? 
					new FontRenderContext(new AffineTransform(),true,true) :
						new FontRenderContext(new AffineTransform(),false,false);
					
			String[] paragraphs = getParagraphs(text);
			int rows = 0;
			for(int a = 0; a<paragraphs.length; a++) {
				int textLength = paragraphs[a].length();
				if(isWhiteSpace(paragraphs[a])) {
					rows++;
				} else {
					AttributedString attrString = new AttributedString( paragraphs[a], attributes);
	
					LineBreakMeasurer lbm = new LineBreakMeasurer(attrString.getIterator(),frc);
				
					int pos = 0;
					while(pos<textLength) {
						pos = lbm.nextOffset(fixedWidth);
						lbm.setPosition(pos);
						rows++;
					}
				}
			}
			int extra = 0;
			if(isXP) { //allow for descents
				extra = (int)(getFont().getLineMetrics("g", frc).getDescent()+1);
			}
			cachedSize = new Dimension(fixedWidth, rows*getRowHeight()+extra);
		}
		return new Dimension(cachedSize);
	}
	
	private boolean isWhiteSpace(String s) {
		for(int a = 0; a<s.length(); a++) {
			if(Character.isWhitespace(s.charAt(a))==false)
				return false;
		}
		return true;
	}
	
	private String[] getParagraphs(String s) {
		int index = 0;
		Vector<String> list = new Vector<String>();
		while(index<s.length()) {
			int i1 = s.indexOf('\n',index);
			int i2 = s.indexOf('\r',index);
			int i;
			if(i1==-1 && i2!=-1) {
				i = i2;
			} else if(i1!=-1 && i2==-1) {
				i = i1;
			} else {
				i = Math.min(i1,i2);
			}
			if(i==-1) {
				list.add(s.substring(index));
				index = s.length();
			} else {
				list.add(s.substring(index,i));
				i++;
				index = i;
			}
		}
		return list.toArray(new String[list.size()]);
	}
	
	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}
	
	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
}
