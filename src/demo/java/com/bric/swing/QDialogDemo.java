/*
 * @(#)QDialogDemo.java
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
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.bric.blog.Blurb;
import com.bric.swing.DialogFooter.EscapeKeyBehavior;
import com.bric.util.JVM;

/** A demo app that presents several different dialogs using <code>QDialog</code>.
 * 
 */
@Blurb (
filename = "QDialog",
title = "JOptionPane: Making an Alternative",
releaseDate = "June 2008",
summary = "I find the <code>JOptionPane</code> hard to work with sometimes.  If you want "+
"a really polished dialog -- with a help button, the right spacing, the right controls to "+
"escape, etc. -- you might just want to make a "+
"<a href=\"https://javagraphics.java.net/doc/com/bric/swing/QDialog.html\">new model</a>.\n"+
"<p>Really: check out the javadocs for this one.  There's a lot of research/explanation in there.",
link = "http://javagraphics.blogspot.com/2008/06/joptionpane-making-alternative.html",
sandboxDemo = true
)
public class QDialogDemo {

    /** A simple demo program that shows off some dialogs.
     * These dialogs were taken from Vista/Apple guidelines.
     * @param args the application's arguments. (This is unused.) 
     */
	public static void main(String[] args) {
		try {
			String lf = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(lf);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setSize(new Dimension(300,300));
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		QDialog.showSaveChangesDialog(frame,"Untitled",true);
		
		QDialog.showDialog(frame, 
				"Low Battery", 
				QDialog.WARNING_MESSAGE, 
				QDialog.createContentPanel("Change your battery or switch to outlet power immediately.", 
						"Your computer has a low battery, so you should act immediately to keep from losing your work.", null, false), 
				new JComponent[] {}, 
				new String[] {"Close"}, 
				0, 
				true, 
				null,  //dontShowKey
				null, //alwaysApplyKey
				EscapeKeyBehavior.TRIGGERS_DEFAULT); // escapeKeyBehavior 
		
		QDialog.showDialog(frame, 
				"AutoComplete Password", 
				QDialog.QUESTION_MESSAGE,
				"Do you want Internet Explorer to remember this password?",  //boldMessage
				"Internet explorer can remember this password so you don't have to type it again the next time you visit this page.",  //plainMessage
				null, //innerComponent 
				HelpComponent.createHelpComponent("http://www.despair.com/", "Learn about AutoComplete"), 
				DialogFooter.YES_NO_OPTION, 
				DialogFooter.NO_OPTION, 
				"autoCompletePassword",
				null,  //alwaysApplyKey
				EscapeKeyBehavior.TRIGGERS_DEFAULT ); //escapeKeyBehavior

		QDialog.showDialog(frame, 
				"Outlook", 
				QDialog.ERROR_MESSAGE, 
				QDialog.createContentPanel("Can't move \"Sent Items\"", 
						"You don't have permission to access this item.", null, true), 
				new JComponent[] {}, 
				new String[] {"Close"}, 
				0, 
				true, 
				null,  //dontShowKey
				null, //alwaysApplyKey
				EscapeKeyBehavior.TRIGGERS_DEFAULT); // escapeKeyBehavior 

		QDialog.showDialog(frame, 
				"Microsoft Office PowerPoint", 
				QDialog.QUESTION_MESSAGE,
				null,  //boldMessage
				"Do you want to run this software?",  //plainMessage
				null, //innerComponent 
				null, //lowerLeftComponent
				DialogFooter.YES_NO_OPTION, 
				DialogFooter.YES_OPTION, 
				null,  //dontShowKey
				null,  //alwaysApplyKey
				EscapeKeyBehavior.TRIGGERS_NONDEFAULT ); //escapeKeyBehavior

		QDialog.showDialog(frame, 
				"Save", 
				QDialog.PLAIN_MESSAGE,
				"The document \"Trip Request\" could not be saved because the disk \"Work Stuff\" is full.",  //boldMessage
				"Try deleting documents from \"Work Stuff\" or saving the document to another disk.",  //plainMessage
				null, //innerComponent 
				null, //lowerLeftComponent
				DialogFooter.OK_OPTION, 
				DialogFooter.OK_OPTION, 
				null,  //dontShowKey
				null,  //alwaysApplyKey
				EscapeKeyBehavior.TRIGGERS_DEFAULT ); //escapeKeyBehavior

		if(JVM.isMac) {
			QDialog.showDialog(frame, 
					null, 
					QDialog.PLAIN_MESSAGE, 
					"Are you sure you want to remove the items in the Trash permanently?",
					"You cannot undo this action.", 
					null, //innerComponent 
					null, //lowerLeftComponent
					DialogFooter.OK_CANCEL_OPTION, 
					DialogFooter.OK_OPTION, 
					null,  //dontShowKey
					null, //alwaysApplyKey
					EscapeKeyBehavior.TRIGGERS_CANCEL ); //escapeKeyBehavior
		} else {
			//XP dialog:
			QDialog.showDialog(frame, 
					null, 
					QDialog.QUESTION_MESSAGE, 
					null, //boldMessage
					"Are you sure you want to delete all of the items in the Recycle Bin?", 
					null, //innerComponent 
					null, //lowerLeftComponent
					DialogFooter.YES_NO_OPTION, 
					DialogFooter.YES_OPTION, 
					null,  //dontShowKey
					null,  //alwaysApplyKey
					EscapeKeyBehavior.TRIGGERS_NONDEFAULT ); //escapeKeyBehavior
		}
		
		//for the sake of this demo, let's not store do-not-show-again or always-apply-this-decision information between launches
		try {
			Preferences.userNodeForPackage(QDialog.class).clear();
		} catch(Throwable t) {
			t.printStackTrace();
		}
		System.exit(0);
	}
	
}
