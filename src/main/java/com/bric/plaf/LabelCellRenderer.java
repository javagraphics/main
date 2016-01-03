/*
 * @(#)LabelCellRenderer.java
 *
 * $Date: 2015-12-13 19:55:57 +0100 (So, 13 Dez 2015) $
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
package com.bric.plaf;

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;

import com.bric.util.JVM;

public class LabelCellRenderer<T> implements ListCellRenderer<T>, TreeCellRenderer, CellRendererConstants {

	protected JLabel label = new JLabel();
	protected JComboBox<?> comboBox;
	protected Collection<WeakReference<JList>> lists = new HashSet<>();
	protected boolean addKeyListener;
	
	public LabelCellRenderer() {
		this(null, false);
	}
	
	/**
	 * 
	 * @param comboBox an optional reference to the JComboBox. This may be used
	 * to render certain aspects (such as keyboard focus), and it is required
	 * for the argument "addKeyListener" to work.
	 * @param addKeyListener if true then key listeners
	 * are added both to JLists and the JComboBox argument to help select items based
	 * on the user's typing.
	 */
	public LabelCellRenderer(JComboBox<?> comboBox,boolean addKeyListener) {
		this.addKeyListener = addKeyListener;
		if(comboBox!=null) {
			this.comboBox = comboBox;
			if(addKeyListener) {
				comboBox.addKeyListener(new ListKeyListener(comboBox));
			}
		}
		label.setBorder(EMPTY_BORDER);
	}
	
	/** Adjust the text and the icon of the <code>label</code> field.
	 */
	protected void formatLabel(T value) {
		label.setText(value.toString());
		label.setIcon(null);
	}
	
	/** Return the label this renderer uses. */
	public JLabel getLabel() {
		return label;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		//TODO: this is missing the key listener
		
		formatLabel( (T)value);
		
		formatLabelColors(isSelected);
		label.setOpaque(row!=-1);
														
		if(isFocusBorderActive()) {
			if(comboBox!=null && comboBox.hasFocus() && row==-1) {
				label.setBorder(FOCUS_BORDER);
			} else {
				label.setBorder(EMPTY_BORDER);
			}
		}
		return label;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends T> list,
			T value, int index, boolean isSelected, boolean cellHasFocus) {
		if(addKeyListener && list!=null && registerList(list))
		{
			list.addKeyListener(new ListKeyListener(list));
		}
		
		formatLabel(value);
		
		formatLabelColors(isSelected);
		label.setOpaque(index!=-1);
														
		if(isFocusBorderActive()) {
			if(comboBox!=null && comboBox.hasFocus() && index==-1) {
				label.setBorder(FOCUS_BORDER);
			} else {
				label.setBorder(EMPTY_BORDER);
			}
		}
		return label;
	}
	
	private boolean registerList(JList<? extends T> list) {
		Iterator<WeakReference<JList>> iter = lists.iterator();
		while(iter.hasNext()) {
			WeakReference<JList> ref = iter.next();
			JList knownList = ref.get();
			if(knownList==null) {
				iter.remove();
			}
			if(knownList==list) {
				return false;
			}
		}
		lists.add(new WeakReference<JList>(list));
		return true;
	}

	protected void formatLabelColors(boolean isSelected) {
		if(comboBox!=null)
		{
			if(isSelected) {
				label.setBackground(UIManager.getColor("ComboBox.selectionBackground"));
				label.setForeground(UIManager.getColor("ComboBox.selectionForeground"));
			} else {
				label.setBackground(UIManager.getColor("ComboBox.background"));
				label.setForeground(UIManager.getColor("ComboBox.foreground"));
			}
		} else {
			if(isSelected) {
				label.setBackground(UIManager.getColor("Menu.selectionBackground"));
				label.setForeground(UIManager.getColor("Menu.selectionForeground"));
			} else {
				label.setBackground(UIManager.getColor("Menu.background"));
				label.setForeground(UIManager.getColor("Menu.foreground"));
			}
		}
	}

	/** If this returns true then the border of the label may be modified
	 * to depict focus.
	 * 
	 * @return if true then the border of the label may be modified to depict
	 * focus. The default implementation tries to figure out if the if
	 * UI actually renders the focus through the border.
	 */
	protected boolean isFocusBorderActive() {
		if(comboBox!=null && comboBox.getUI() instanceof FilledComboBoxUI)
			return false;
		return !JVM.isMac;
	}
	
	
}
