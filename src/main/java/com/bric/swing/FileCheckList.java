/*
 * @(#)FileCheckList.java
 *
 * $Date: 2015-06-13 09:48:19 +0200 (Sa, 13 Jun 2015) $
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

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileFilter;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bric.util.ObservableSet;
import com.bric.util.SearchConstraints;

/** This is a <code>FileList</code> implemented with
 * <code>JCheckBoxes</code>.  This class adds a
 * <code>getSelection()</code> method to obtain a list of
 * which files are currently selected.  (That is: which
 * checkboxes are checked.)
 */
public class FileCheckList extends FileList {
	private static final long serialVersionUID = 1L;

	private final ObservableSet<File> selection = new ObservableSet<File>(File.class);
	
	public FileCheckList(File directory,FileFilter primaryFilter,SearchConstraints<File> constraints) {
		super(directory==null ? new File[] {} : new File[] { directory }, primaryFilter,constraints);
		
		selection.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				for(int a = 0; a<getComponentCount(); a++) {
					Component comp = getComponent(a);
					if(comp instanceof JCheckBox) {
						JCheckBox jc = (JCheckBox)comp;
						File file = (File)jc.getClientProperty(KEY_FILE);
						if(file!=null && (selection.contains(file)!=jc.isSelected()) ) {
							jc.setSelected(selection.contains(file));
						}
					}
				}
			}
		});
		
		addContextualMenus(this);
	}
	
	private void addContextualMenus(JComponent jc) {
		ContextualMenuHelper.add(jc, "Select All", new Runnable() {
			public void run() {
				selectAll();
			}
		});
		ContextualMenuHelper.add(jc, "Unselect All", new Runnable() {
			public void run() {
				unselectAll();
			}
		});
	}
	
	/** Return the set used to actively manage the selection.
	 * If you modify this object you will immediately change which checkboxes are currently selected.
	s */
	public ObservableSet<File> getSelection() {
		return selection;
	}
	
	@Override
	protected void updateFileListComponents() {
		super.updateFileListComponents();
		Vector<File> accountedFor = new Vector<File>();
		for(int a = 0; a<getComponentCount(); a++) {
			Component comp = getComponent(a);
			if(comp instanceof JComponent) {
				JComponent jc = (JComponent)comp;
				File file = (File)jc.getClientProperty(KEY_FILE);
				if(file!=null)
					accountedFor.add(file);
			}
		}
		if(selection!=null)
			selection.retainAll(accountedFor);
	}
	
	ItemListener itemListener = new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
			JCheckBox checkbox = (JCheckBox)e.getSource();
			File file = (File)checkbox.getClientProperty(KEY_FILE);
			if(checkbox.isSelected()) {
				if(selection.contains(file)==false)
					selection.add(file);
			} else {
				if(selection.contains(file))
					selection.remove(file);
			}
		}
	};
	
	public void selectAll() {
		JCheckBox[] checkboxes = getCheckBoxes();
		for(int a = 0; a<checkboxes.length; a++) {
			checkboxes[a].setSelected(true);
		}
	}
	
	public void unselectAll() {
		JCheckBox[] checkboxes = getCheckBoxes();
		for(int a = 0; a<checkboxes.length; a++) {
			checkboxes[a].setSelected(false);
		}
	}
	
	protected JCheckBox[] getCheckBoxes() {
		Vector<JCheckBox> v = new Vector<JCheckBox>();
		getCheckBoxes(this, v);
		return v.toArray(new JCheckBox[v.size()]);
	}
	
	private static void getCheckBoxes(JComponent jc,Vector<JCheckBox> list) {
		if(jc instanceof JCheckBox) {
			list.add( (JCheckBox)jc );
		}
		for(int a = 0; a<jc.getComponentCount(); a++) {
			if(jc.getComponent(a) instanceof JComponent) {
				getCheckBoxes( (JComponent)jc.getComponent(a), list);
			}
		}
	}
	
	@Override
	final protected JComponent createComponent(File file) {
		JCheckBox checkBox = new JCheckBox();
		checkBox.setText(getText(file));
		checkBox.addItemListener(itemListener);
		addContextualMenus(checkBox);
		return checkBox;
	}
	
	/** Returns the text a checkbox should display for a given File.
	 *
	 */
	protected String getText(File file) {
		return file.getName();
	}
}
