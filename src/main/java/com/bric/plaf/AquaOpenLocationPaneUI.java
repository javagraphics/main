/*
 * @(#)AquaOpenLocationPaneUI.java
 *
 * $Date: 2014-03-13 09:15:48 +0100 (Do, 13 Mär 2014) $
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
package com.bric.plaf;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.bric.io.location.FileLocation;
import com.bric.io.location.IOLocation;
import com.bric.io.location.LocationFactory;
import com.bric.swing.DialogFooter;
import com.bric.swing.QDialog;
import com.bric.swing.io.LocationPane;
import com.bric.util.CommonFiles;

public class AquaOpenLocationPaneUI extends OpenLocationPaneUI {
	
	protected final JSplitPane splitPane;
	protected final AquaLocationSourceList sourceList;
	protected final AquaLocationPaneControls controls;
	protected final DialogFooter footer;
	protected final JScrollPane sourceListScrollPane;

	public AquaOpenLocationPaneUI(LocationPane p) {
		super(p);
		sourceList = new AquaLocationSourceList(p.getLocationHistory(), p.getGraphicCache());
		sourceListScrollPane = new JScrollPane(sourceList);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sourceListScrollPane, browser);
		controls = new AquaLocationPaneControls(this);
		browser.setPreferredSize(new Dimension(200,200));

		commitButton.setText("Open");
		cancelButton.setText("Cancel");
		
		footer = new DialogFooter(new JComponent[] { newFolderButton }, 
				new JComponent[] { commitButton, cancelButton}, false, commitButton);
		
		newFolderButton.setText("New Folder");
		newFolderButton.setVisible(false);
	}

	@Override
	public IOLocation getDefaultDirectory() {
		//this is our preferred option:
		File documents = new File(System.getProperty("user.home")+"/Documents");
		if(documents.exists())
			return LocationFactory.get().create(documents);
		
		//but it's possible either Documents doesn't exist, either because
		//this Mac is weird or this UI was constructed on a non-Mac:
		File home = new File(System.getProperty("user.home"));
		return LocationFactory.get().create(home);
	}
	
	protected static File[] combine(File[] array1,File[] array2) {
		File[] sum = new File[array1.length+array2.length];
		System.arraycopy(array1, 0, sum, 0, array1.length);
		System.arraycopy(array2, 0, sum, array1.length, array2.length);
		return sum;
	}

	@Override
	protected void installGUI(JComponent panel) {
		installGUI(panel, true);
	}
	
	protected void installGUI(JComponent panel,boolean includeFooter) {
		if(sourceList.isEmpty()) {
			File[] array1 = CommonFiles.getUserDirectories(true);
			IOLocation[] array2 = new FileLocation[array1.length];
			for(int a = 0; a<array1.length; a++) {
				array2[a] = LocationFactory.get().create(array1[a]);
			}
			sourceList.add( array2 );
		}
		
		panel.removeAll();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(4,0,4,0);
		panel.add(controls, c);
		c.gridy++; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		panel.add(splitPane,c);
		if(includeFooter) {
			c.weighty = 0;
			c.gridy++;
			panel.add(footer, c);
		}	
		sourceListScrollPane.setMinimumSize(new Dimension(100,40));
		sourceListScrollPane.setPreferredSize(new Dimension(150,40));
	}
	
	@Override
	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);
	}

	@Override
	protected String getNewFolderName() {
		Frame frame = null;
		Window parent = SwingUtilities.getWindowAncestor(locationPane);
		while(frame==null && parent!=null) {
			if(parent instanceof Frame) {
				frame = (Frame)parent;
			}
			parent = parent.getOwner();
		}
		JButton create = DialogFooter.createOKButton();
		create.setText("Create");
		JButton cancel = DialogFooter.createCancelButton(true);
		
		JTextField textField = new JTextField("untitled folder");
		
		JPanel content = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(3,3,3,3);
		content.add(new JLabel("Name of new folder:"),c);
		c.gridy++;
		content.add(textField,c);
		
		//just to give some extra width:
		JPanel fluff = new JPanel();
		fluff.setPreferredSize(new Dimension(50,5));
		fluff.setOpaque(false);
		
		DialogFooter footer = new DialogFooter(new JComponent[] {fluff}, new JComponent[] {create, cancel}, true, create);
		QDialog dialog = new QDialog(frame, 
				"New Folder", 
				null, //icon
				content, 
				footer,
				true);
		dialog.pack();
		dialog.setModal(true);
		
		Window parentWindow = SwingUtilities.getWindowAncestor(locationPane);
		if(parentWindow!=null)
			dialog.setLocationRelativeTo(parentWindow);
		dialog.setVisible(true);
		
		if(dialog.getSelectedButton()==create)
			return textField.getText();
		
		return null;
	}
	
	
}
