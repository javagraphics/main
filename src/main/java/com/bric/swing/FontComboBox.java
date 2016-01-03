/*
 * @(#)FontComboBox.java
 *
 * $Date: 2014-11-27 08:09:41 +0100 (Do, 27 Nov 2014) $
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
package com.bric.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.SystemColor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/** A JComboBox for selecting Fonts that includes a renderer to preview the fonts.
 *
 */
public class FontComboBox extends JComboBox<Font> {
	private static final long serialVersionUID = 1L;
	
	/** A factory that produces the Fonts for a FontComboBox.
	 * <p>The default implementation consults the local GraphicsEnvironment
	 * for available fonts, but it is easy to load Fonts from files or zip archives as well.
	 */
	public static class FontFactory {

		/** Create the Fonts this combobox should display, in the order they'll be listed.
		 * Subclasses may override this to provide their own special set of Fonts.
		 */
		protected List<Font> createFonts() {
			List<Font> fonts = new ArrayList<Font>();

			//first, populate the fonts on this computer:
			GraphicsEnvironment ge = GraphicsEnvironment.
		              getLocalGraphicsEnvironment();
			String[] s = ge.getAvailableFontFamilyNames();
			for(String fontName : s) {
				fonts.add(new Font(fontName, 0, 16));
			}
			
			Collections.sort(fonts, new Comparator<Font>() {

				@Override
				public int compare(Font o1, Font o2) {
					String n1 = o1.getName();
					String n2 = o2.getName();
					int i = n1.compareTo(n2);
					if(i!=0) return i;
					int style1 = o1.getStyle();
					int style2 = o2.getStyle();
					if(style1-style2!=0) return style1-style2;
					int size1 = (int)(o1.getSize2D()*100);
					int size2 = (int)(o2.getSize2D()*100);
					if(size1-size2!=0) return size1-size2;
					return 0;
				}});
			return fonts;
		}
	}

	/** Create a FontComboBox that uses the default FontFactory.
	 */
	public FontComboBox() {
		this(new FontFactory());
	}

	public FontComboBox(FontFactory fontFactory) {
		setRenderer(new ListCellRenderer<Font>() {
			JLabel label = new JLabel();
			
			@Override
			public Component getListCellRendererComponent(
					JList<? extends Font> list, Font font, int row,
					boolean isSelected, boolean hasFocus) {
				if(font!=null) {
					label.setText(font.getName());
					label.setFont(font);
				} else {
					label.setText(" ");
				}
				if(isSelected) {
					label.setBackground(SystemColor.textHighlight);
					label.setForeground(SystemColor.textHighlightText);
				} else {
					label.setBackground(SystemColor.text);
					label.setForeground(SystemColor.textText);
				}
				if(row<0) {
					label.setBackground(new Color(255,255,255,0));
					label.setOpaque(false);
				} else {
					label.setOpaque(list!=null);
				}
				return label;
			}
			
		});
		
		List<Font> fonts = fontFactory.createFonts();
		for(Font font : fonts) {
			addItem(font);
		}
	}
	
	/** Select a Font based on its font name, or set the selected font to null
	 * to this font is not found.
	 * 
	 * @param fontName the name of the Font to select.
	 * @return true if this Font was successfully selected.
	 */
	public boolean selectFont(String fontName) {
		int fontComboBoxIndex = -1;
		for(int a = 0; a<getItemCount() && fontComboBoxIndex==-1; a++) {
			Font f = (Font)getItemAt(a);
			if(f.getName().equals(fontName)) {
				fontComboBoxIndex = a;
				break;
			}
		}
		setSelectedIndex(fontComboBoxIndex);
		
		return fontComboBoxIndex!=-1;
	}
}
