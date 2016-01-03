/*
 * @(#)JarSignerDemo.java
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
package com.bric.jar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.bric.inspector.InspectorGridBagLayout;
import com.bric.swing.jnlp.FilePanel;
import com.bric.swing.jnlp.FilePanel.FileData;
import com.bric.swing.jnlp.FilePanel.FileWrapper;

/** This is a simple UI to let you select a keystore, a jar, and enter the appropriate passwords
 * to sign it.
 */
public class JarSignerDemo extends JFrame 
{
	private static final long	serialVersionUID	= 1L;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					String lf = UIManager.getSystemLookAndFeelClassName();
					UIManager.setLookAndFeel(lf);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				
				JarSignerDemo d = new JarSignerDemo();
				d.pack();
				d.setLocationRelativeTo(null);
				d.setVisible(true);
			}
		});
	}
	
	FilePanel keystoreFilePanel = new FilePanel("Keystore:", new String[] {"p12"} , new JComponent[] {});
	FilePanel jarFilePanel = new FilePanel("Jar:", new String[] { "jar"}, new JComponent[] {});
	JLabel keystorePasswordLabel = new JLabel("Keystore Password:");
	JLabel aliasLabel = new JLabel("Alias:");
	JLabel aliasPasswordLabel = new JLabel("Alias Password:");
	JTextField aliasField = new JTextField(35);
	JPasswordField keystorePasswordField = new JPasswordField(35);
	JPasswordField aliasPasswordField = new JPasswordField(35);
	
	JButton signButton = new JButton("Sign");
	
	PropertyChangeListener filePanelListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			updateSignButton();
		}
		
	};
	
	DocumentListener docListener = new DocumentListener() {

		@Override
		public void insertUpdate(DocumentEvent e)
		{
			changedUpdate(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e)
		{
			changedUpdate(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e)
		{
			updateSignButton();
		}
		
	};
	
	public JarSignerDemo() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel);
		
		InspectorGridBagLayout layout = new InspectorGridBagLayout(panel);
		layout.addRow(keystoreFilePanel.getIDLabel(), keystoreFilePanel.getFileLabel(), true, keystoreFilePanel.getBrowseButton());
		layout.addRow(jarFilePanel.getIDLabel(), jarFilePanel.getFileLabel(), true, jarFilePanel.getBrowseButton());
		layout.addRow(keystorePasswordLabel, keystorePasswordField, true);
		layout.addRow(aliasLabel, aliasField, true);
		layout.addRow(aliasPasswordLabel, aliasPasswordField, true);
		layout.addRow(signButton, SwingConstants.CENTER, false);

		updateSignButton();
		keystoreFilePanel.addPropertyChangeListener(FilePanel.FILE_DATA_KEY, filePanelListener);
		keystorePasswordField.getDocument().addDocumentListener(docListener);
		aliasField.getDocument().addDocumentListener(docListener);
		aliasPasswordField.getDocument().addDocumentListener(docListener);
		
		signButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JarSigner signer = new JarSigner(
						((FileWrapper)keystoreFilePanel.getClientProperty(FilePanel.FILE_DATA_KEY)).getFile(),
						new String(keystorePasswordField.getPassword()),
						aliasField.getText(),
						new String(aliasPasswordField.getPassword()),
						"http://timestamp.digicert.com"
					);
				File jarFile = ((FileWrapper)jarFilePanel.getClientProperty(FilePanel.FILE_DATA_KEY)).getFile();
				signer.sign(jarFile, true);
				
			}
		});
	}
	
	protected void updateSignButton() {
		signButton.setEnabled(isSignButtonEnabled());
	}
	
	protected boolean isSignButtonEnabled() {
		FileData keystoreFileData = (FileData)keystoreFilePanel.getClientProperty(FilePanel.FILE_DATA_KEY);
		FileData jarFileData = (FileData)jarFilePanel.getClientProperty(FilePanel.FILE_DATA_KEY);
		if(keystoreFileData==null || jarFileData==null)
			return false;
		if(aliasField.getText().length()==0)
			return false;
		if(keystorePasswordField.getPassword().length==0)
			return false;
		if(aliasPasswordField.getPassword().length==0)
			return false;
		return true;
	}
}
