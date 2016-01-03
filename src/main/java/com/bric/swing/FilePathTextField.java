/*
 * @(#)FilePathTextField.java
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;

import com.bric.blog.Blurb;
import com.bric.plaf.LabelCellRenderer;

/** A text field for file paths that autocompletes matching file names.
 * 
 */
@Blurb (
filename = "FilePathTextField",
title = "Text Field: Autocomplete File paths",
releaseDate = "TBA",
summary = "This text field helps autopopulate with valid file paths, and [when finished] "+
"should offer a popup menu of options (like an autocompleting URL field).",
scrapped = "This is unfinished, but partially functional.",
sandboxDemo = false
)
public class FilePathTextField extends AutocompleteTextField<File> {
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0;
		f.getContentPane().add(new FilePathTextField(50), c);
		c.gridy++; c.weighty = 1;
		f.getContentPane().add(new JLabel(" "), c);
		f.pack();
		f.setVisible(true);
	}
	
	public static class FileSuggestionModel implements SuggestionModel<File> {

		@Override
		public void refreshSuggestions(
				com.bric.swing.AutocompleteTextField.AutocompleteModel<File> dataModel,
				AutocompleteTextField<File> field) {

			String text = field.getText();
			List<File> v = new ArrayList<File>();
			if(text.length()==0) {
				File[] roots = File.listRoots();
				for(int a = 0; roots!=null && a<roots.length; a++) {
					v.add(roots[a]);
				}
			} else {
			
				String s = null;
				if(field.getSelectionEnd()==text.length()) {
					s = text.substring(0,field.getSelectionStart());
				}
				
				if(s!=null) {
					File file = new File(s);
					if(file.exists()) {
						v.add(file);
						if(file.isDirectory()) {
							File[] children = file.listFiles();
							String sLower = s.toLowerCase();
							for(int a = 0; children!=null && a<children.length; a++) {
								String childPath = children[a].getAbsolutePath().toLowerCase();
								if(childPath.startsWith(sLower) && children[a].isHidden()==false) {
									v.add(children[a]);
								}
							}
						}
					} else {
						int i = s.lastIndexOf(File.separator);
						if(i!=-1) {
							String parentPath = s.substring(0,i+1);
							File parent = new File(parentPath);
							if(parent.exists()) {
								File[] children = parent.listFiles();
								String sLower = s.toLowerCase();
								for(int a = 0; children!=null && a<children.length; a++) {
									String childPath = children[a].getAbsolutePath().toLowerCase();
									if(childPath.startsWith(sLower)) {
										v.add(children[a]);
									}
								}
							}
						}
					}
				}
			}
			
			File[] array = v.toArray(new File[v.size()]);
			//TODO: preserve selection & indication indices if appropriate?
			dataModel.setSuggestions(array, -1, -1);
		}
		
	}
	
	public FilePathTextField() {
		this.suggestionList.setCellRenderer(new LabelCellRenderer<File>() {

			@Override
			protected void formatLabel(File file) {
				label.setText(file.getAbsolutePath());
				label.setIcon(FileIcon.getIcon(file));
			}
			
		});
		setSuggestionModel( new FileSuggestionModel() );
	}
	
	public FilePathTextField(int columns) {
		this();
		setColumns(columns);
	}

	@Override
	protected String convertSuggestionToString(File suggestion) {
		String s = suggestion.getAbsolutePath();
		if(suggestion.isDirectory() && (!s.endsWith(File.separator)))
			s = s+File.separator;
		return s;
	}
	
}
