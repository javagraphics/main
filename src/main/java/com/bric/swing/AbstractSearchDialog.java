/*
 * @(#)AbstractSearchDialog.java
 *
 * $Date: 2015-09-13 20:46:53 +0200 (So, 13 Sep 2015) $
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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.*;

/** A very simple search dialog with a text field and the option to search forward/backward.
 * 
 */
public abstract class AbstractSearchDialog extends QDialog {
	private static final long serialVersionUID = 1L;
	
	protected static ResourceBundle strings = ResourceBundle.getBundle("com.bric.swing.resources.TextSearchDialog");
	
	private static Frame getFrame(JComponent c) {
		Window w = SwingUtilities.getWindowAncestor(c);
		if (w instanceof Frame) {
			return ((Frame) w);
		}
		return new Frame();
	}

	protected JLabel notFound = new JLabel(strings.getString("notFound"));
	protected JButton prevButton = new JButton(strings.getString("previous"));
	protected JButton nextButton = new JButton(strings.getString("next"));
	protected JLabel findLabel = new JLabel(strings.getString("find"));

	protected JTextField textField = new JTextField(20);
	
	protected ActionListener actionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			boolean forward = e.getSource()==nextButton;
			
			boolean hit = doNextSearch(forward);
			notFound.setVisible(!hit);
			if (!hit) {
				textField.requestFocus();
			} else {
				setVisible(false);
			}
		}
	};

	/** Creates an <code>AbstractSearchDialog</code>.
	 * @param comp only used to identify the frame to bind this dialog to.
	 */
	public AbstractSearchDialog(JComponent comp) {
		super(getFrame(comp), strings.getString("dialogTitle") );
		
		Color fg = UIManager.getColor("Label.disabledForeground");
		if(fg!=null) {
			notFound.setForeground(fg);
		}
		
		JPanel notFoundContainer = new JPanel();
		notFoundContainer.add(notFound);
		Dimension preferredSize = notFoundContainer.getPreferredSize();
		notFoundContainer.setPreferredSize(preferredSize);
		notFoundContainer.setMinimumSize(preferredSize);
		notFound.setVisible(false);

		nextButton.setToolTipText(strings.getString("nextTip"));
		prevButton.setToolTipText(strings.getString("previousTip"));
		
		DialogFooter footer = new DialogFooter(new JComponent[] {notFoundContainer},
				new JComponent[] {nextButton, prevButton},
				false,
				nextButton );
		
		setFooter(footer);
		
		JPanel content = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(3,3,3,3);
		c.weightx = 0; c.weighty = 0;
		content.add(findLabel,c);
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx++;
		content.add(textField,c);
		
		setContent(content);
		
		nextButton.addActionListener(actionListener);
		prevButton.addActionListener(actionListener);
		
		setModal(false);
		setDocumentModal(false);
		setCloseable(true);
		
		pack();
	}
	
	/** This is called when the user initiates a search. The search phrase
	 * to search for textField.getText()
	 * 
	 * @return true if this search was successful, false if the end of the document was reached
	 * without identifying the search phrase.
	 */
	protected abstract boolean doNextSearch(boolean forward);
}
