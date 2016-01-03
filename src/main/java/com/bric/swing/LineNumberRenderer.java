/*
 * @(#)LineNumberRenderer.java
 *
 * $Date: 2015-12-27 07:22:12 +0100 (So, 27 Dez 2015) $
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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position.Bias;

import com.bric.util.JVM;

/**
 * This abstract renderer paints line numbers that relate to a JTextComponent.
 * <p>
 * Originally this was conceived as a panel that would sit immediately to the left
 * of the text component, but that created problems if the text component was
 * in a scrollpane (as is often the case) that scrolled horizontally: the line numbers
 * quickly were out-of-view and useless.
 * <p>
 * To resolve this problem: this logic was abstract into its own class (to be applied
 * in multiple possible usages), and the {@link LineNumberBorder} class is recommended
 * instead to make a {@code java.swing.border} that joins against a JScrollPane to achieve
 * the correct effect.
 */
public abstract class LineNumberRenderer {

	protected int[] lineBreaks = new int[] {};
	protected JTextComponent jtc;
	protected Color[] rowColors = new Color[] { new Color(0xf5f5f5), new Color(0xffffff) };
	protected Font font = new Font("Monospaced", 0, JVM.isMac ? 11 : 12);
	protected DocumentListener docListener = new DocumentListener() {

		@Override
		public void insertUpdate(DocumentEvent e) {
			String text = LineNumberRenderer.this.jtc.getText();
			final String replacement = text.replace("\r\n", "\n");
			if(text.equals(replacement)) {
				refreshLineBreaks();
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						LineNumberRenderer.this.jtc.setText(replacement);
					}
				});
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			insertUpdate(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			repaint();
		}
		
	};
	
	public LineNumberRenderer(JTextComponent jtc) {
		setTextComponent(jtc);
	}
	
	public void setTextComponent(JTextComponent jtc) {
		if(this.jtc!=null) {
			this.jtc.getDocument().removeDocumentListener(docListener);
		}
		this.jtc = jtc;
		this.jtc.getDocument().addDocumentListener(docListener);
		refreshLineBreaks();
	}

	protected void refreshLineBreaks() {
		if(jtc==null) {
			lineBreaks = new int[] {};
		} else {
			String text = jtc.getText();
			List<Integer> list = new ArrayList<>();
			StringBuffer line = new StringBuffer();
			for(int a = 0; a<text.length(); a++) {
				char ch = text.charAt(a);
				if(ch=='\n' || ch=='\r') {
					list.add(a);
					line = new StringBuffer();
				} else {
					line.append(ch);
				}
			}
			if(lineBreaks.length!=list.size())
				lineBreaks = new int[list.size()];
			for(int a = 0; a<list.size(); a++) {
				lineBreaks[a] = list.get(a);
			}
			
			String highestLineNumber = (list.size()+1)+"";
			String str = "";
			for(int a = 0; a<highestLineNumber.length(); a++) {
				str += "X";
			}
			Rectangle2D bounds = font.getStringBounds(str, new FontRenderContext(new AffineTransform(), true, true));
			//the height will be stretched, so it doesn't matter. Only the width matters:
			setMaximumWidth((int)(bounds.getWidth()+1) + 12);
		}
		repaint();
	}
	
	protected boolean isAntialiased() {
		return JVM.isMac;
	}

	protected abstract void setMaximumWidth(int maxWidth);

	public void paintComponent(Graphics2D g) {
		g = (Graphics2D)g.create();
		g.setFont(font);
		if(isAntialiased()) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		String src = jtc.getText();
		FontRenderContext frc = g.getFontRenderContext();
		int lastY = 0;
		for(int lineNumber = 0; lineNumber<=lineBreaks.length; lineNumber++) {
			try {
				int pos = lineNumber<lineBreaks.length ? (lineBreaks[lineNumber]) : src.length();
				Rectangle r = jtc.getUI().modelToView(jtc, pos, Bias.Forward);
				if(g.hitClip(2, r.y, 2, r.height)) {
					g.setColor( rowColors[lineNumber%rowColors.length] );
					g.fillRect(0, lastY, (int)(getWidth()+.5f), r.y + r.height - lastY);
					g.setColor(Color.gray);
					String str = (lineNumber+1)+"";
					Rectangle2D bounds = font.getStringBounds(str, frc);
					g.drawString( str, (float)(getWidth() - bounds.getWidth() - 8), (float)(r.y + r.height/2 - bounds.getHeight()/2 + g.getFont().getLineMetrics(str, frc).getAscent() ) );
				}
				lastY = r.y + r.height;
			} catch (BadLocationException e) {
				throw new RuntimeException(e);
			}
		}
		g.dispose();
	}
	
	protected abstract float getWidth();

	protected abstract void repaint();
}
