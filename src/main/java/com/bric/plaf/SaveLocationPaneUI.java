/*
 * @(#)SaveLocationPaneUI.java
 *
 * $Date: 2014-06-06 20:04:49 +0200 (Fr, 06 Jun 2014) $
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

import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.bric.io.location.IOLocation;
import com.bric.swing.io.LocationPane;
import com.bric.swing.io.SaveLocationPane;

public abstract class SaveLocationPaneUI extends LocationPaneUI {

	protected final JTextField saveField = new JTextField(20);
	
	public SaveLocationPaneUI(LocationPane locationPane) {
		super(locationPane);
	}

	/** This returns the IOLocation that data should be saved to.
	 * This should only be called after the commit button is pressed.
	 * @throws IOException if an IO problem occurs.
	 */
	public IOLocation getSaveLocation() throws IOException {
		String name = ((SaveLocationPane)locationPane).getSaveName();
		if(name==null || name.length()==0)
			return null;
		IOLocation newChild = locationPane.getLocationHistory().getLocation().getChild(name);
		return newChild;
	}
	
	public abstract String getNewFileName();
	
	public abstract void setNewFileName(String fileName);
	
	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		selectName(true);
	}

	/** Selects the text in the text field.
	 * 
	 * @param invokeLater if true, then this task is wrapped in a
	 * runnable passed to <code>SwingUtilities.invokeLater()</code>.
	 */
	public void selectName(boolean invokeLater) {
		Runnable runnable = new Runnable() {
			public void run() {
				saveField.requestFocus();
				String s = saveField.getText();
				int i = s.lastIndexOf('.');
				if(i==-1) {
					saveField.select(0, s.length());
				} else {
					saveField.select(0,i);
				}
			}
		};
		if(SwingUtilities.isEventDispatchThread()) {
			if(invokeLater) {
				SwingUtilities.invokeLater(runnable);
			} else {
				runnable.run();
			}
		} else {
			SwingUtilities.invokeLater(runnable);
		}
	}
}
