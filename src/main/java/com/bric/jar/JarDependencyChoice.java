/*
 * @(#)JarDependencyChoice.java
 *
 * $Date$
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

import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.*;

import com.bric.UserCancelledException;
import com.bric.inspector.InspectorGridBagLayout;
import com.bric.swing.DialogFooter;
import com.bric.swing.QDialog;
import com.bric.window.WindowList;

/** When creating a jar we have to decide what to do with external jar
 * dependencies: should we disregard them (like we should sure javax.jnlp.* classes),
 * copy the entire jar (like we should for JFreeChart), or copy only the specific
 * classes we need?
 * <p>This class helps manage this decision.
 */
public abstract class JarDependencyChoice {
	/** The three options we can choose from when dealing with external jar dependencies. */
	public enum Behavior { IGNORE, BUNDLE_ENTIRE_JAR, BUNDLE_ONLY_REQUIRED_CLASSES };
	
	/** This choice always uses one fixed <code>Behavior</code>.
	 */
	public static class Fixed extends JarDependencyChoice {

		final Behavior behavior;
		final boolean applyToNonrequiredJars;
		
		/**
		 * 
		 * @param b the fixed Behavior this choice always uses.
		 */
		public Fixed(Behavior b,boolean applyToNonrequiredJars) {
			if(b==null)
				throw new NullPointerException();
			
			this.applyToNonrequiredJars = applyToNonrequiredJars;
			this.behavior = b;
		}
		
		@Override
		public Behavior getBehavior(File file,boolean knownDependency) {
			if(knownDependency || applyToNonrequiredJars)
				return behavior;
			return Behavior.IGNORE;
		}

		/** This throws an exception if the argument b is not
		 * the Behavior passed to this object's constructor.
		 */
		@Override
		public void setBehavior(File file, Behavior b) {
			if(!behavior.equals(b))
				throw new RuntimeException("this operation is not supported for a fixed behavior");
		}

		@Override
		public Behavior guessBehavior(File jarFile) {
			return behavior;
		}
		
	}
	
	public static class Preference extends JarDependencyChoice {
		final Preferences rootPrefs;
		final Preferences prefs;
		
		/** Create a new Preferences-backed choice. 
		 * 
		 * @param name the project name, which is used to distinguish the
		 * preferences so different projects can make different choices.
		 */
		public Preference(String name) {
			if(name==null) throw new NullPointerException();
			rootPrefs = Preferences.userNodeForPackage( JarWriter.class );
			prefs = rootPrefs.node(name);
		}

		@Override
		public Behavior getBehavior(File file,boolean applyToNonrequiredJars) {
			String s = prefs.get(file.getName(), null);
			if(s==null) return null;
			return Behavior.valueOf(s);
		}
		
		@Override
		public void setBehavior(File file,Behavior b) {
			prefs.put(file.getName(), b.toString());
		}

		@Override
		public Behavior guessBehavior(File jarFile) {
			try {
				/** Catalog previous choices by frequency of hits. */
				Map<String, Integer> results = new TreeMap<String, Integer>();
				catalogPrefs(rootPrefs, jarFile, results);
				
				if(results.size()==0)
					return null;
				int max = 0;
				String bestKey = null;
				for(String choice : results.keySet()) {
					int v = results.get(choice);
					if(v>=max) {
						bestKey = choice;
						max = v;
					}
				}
				return Behavior.valueOf(bestKey);
			} catch(BackingStoreException e) {
				e.printStackTrace();
				return null;
			}
			
		}
		
		private void catalogPrefs(Preferences prefs,File jarFile,Map<String, Integer> results) throws BackingStoreException {
			for(String name : prefs.childrenNames()) {
				Preferences child = prefs.node(name);
				catalogPrefs(child, jarFile, results);
			}
			String v = prefs.get(jarFile.getName(), null);
			if(v!=null) {
				try {
					Behavior.valueOf(v);
					Integer i = results.get(v);
					if(i==null) i = 0;
					results.put(v, i+1);
				} catch(RuntimeException e) {}
			}
		}
	}


	/** This method guesses the desired behavior.
	 * <p>If <code>getBehavior()</code> has already returned
	 * <code>null</code> then we're going to show a dialog to
	 * the user to make them decide. But this method helps
	 * prepopulate the choices. If the user likes what they see,
	 * they can just hit the return key and move on.
	 * <p>This method may consult other projects and see
	 * if they have already stored a Behavior for this file.
	 * 
	 * @param jarFile the file to guess the Behavior for.
	 * @return a reasonable guess at the Behavior to associate
	 * with this File, or null.
	 */
	public abstract Behavior guessBehavior(File jarFile);
	
	/** 
	 * 
	 * @param file the file to fetch the Behavior for.
	 * @param knownDependency if true then we know for certain that this
	 * file is needed. If false then we're not sure if this file is required.
	 * @return the last Behavior assigned to this File.
	 * This may return null if no Behavior is defined.
	 */
	public abstract Behavior getBehavior(File file,boolean knownDependency);

	/**
	 * 
	 * @param file the file to assign the Behavior for.
	 * @param b the Behavior this file should use.
	 */
	public abstract void setBehavior(File file,Behavior b);

	static class BehaviorDialog extends QDialog {
		private static final long serialVersionUID = 1L;
		
		DialogFooter footer = DialogFooter.createDialogFooter(null, 
				DialogFooter.OK_CANCEL_OPTION, 
				DialogFooter.OK_OPTION, 
				DialogFooter.EscapeKeyBehavior.TRIGGERS_CANCEL);
		
		ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean passed = true;
				for(BehaviorComboBox c : allComboBoxes.values()) {
					Behavior b = (Behavior)c.getSelectedItem();
					if(b==null)
						passed = false;
				}
				footer.getButton(DialogFooter.OK_OPTION).setEnabled(passed);
			}
		};

		class BehaviorComboBox extends JComboBox<Behavior> {
			private static final long serialVersionUID = 1L;

			public BehaviorComboBox(Behavior b) {
				addItem( Behavior.BUNDLE_ENTIRE_JAR );
				addItem( Behavior.BUNDLE_ONLY_REQUIRED_CLASSES );
				addItem( Behavior.IGNORE );
				if(b==null) {
					setSelectedIndex(-1);
				} else if(Behavior.BUNDLE_ENTIRE_JAR.equals(b)) {
					setSelectedIndex(0);
				} else if(Behavior.BUNDLE_ENTIRE_JAR.equals(b)) {
					setSelectedIndex(1);
				} else if(Behavior.IGNORE.equals(b)) {
					setSelectedIndex(2);
				}
				addActionListener(actionListener);
			}
		}
		
		Map<File, Behavior> jarBehaviors;
		Map<File, BehaviorComboBox> allComboBoxes = new HashMap<File, BehaviorComboBox>();
		JCheckBox rememberCheckbox = new JCheckBox("Remember these choices next time", true);
		JarDependencyChoice choiceModel;
		
		BehaviorDialog(Frame frame,Set<File> jarFiles,Map<File, Behavior> jarBehaviors,String primaryClassName,JarDependencyChoice choiceModel) {
			super(frame, "Resolve Dependencies");
			this.jarBehaviors = jarBehaviors;
			this.choiceModel = choiceModel;
			
			JPanel panel = new JPanel(new GridBagLayout());
			InspectorGridBagLayout layout = new InspectorGridBagLayout(panel);
			for(File jarFile : jarFiles) {
				Behavior b = jarBehaviors.get(jarFile);
				if(b==null)
					b = choiceModel.guessBehavior(jarFile);
				BehaviorComboBox comboBox = new BehaviorComboBox( b );
				layout.addRow(new JLabel(jarFile.getName()+":"), comboBox, false);
				allComboBoxes.put(jarFile, comboBox);
			}
			layout.addRow(null, rememberCheckbox, false);
			
			JComponent innerComponent;
			if(jarBehaviors.size()>5) {
				innerComponent = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			} else {
				innerComponent = panel;
			}
			innerComponent = QDialog.createContentPanel(
					"These jars are required to compile \""+primaryClassName+"\".",
					"The javac task is complete, but what should I do with these jars?", 
					innerComponent, false);
			
			setIcon( QDialog.getIcon( QDialog.PLAIN_MESSAGE ) );
			setContent(innerComponent);
			actionListener.actionPerformed(null);
			setFooter(footer);
		}

		public void save() {
			for(File jarFile : allComboBoxes.keySet()) {
				BehaviorComboBox cb = allComboBoxes.get(jarFile);
				Behavior b = (Behavior)cb.getSelectedItem();
				jarBehaviors.put(jarFile, b);
				if(rememberCheckbox.isSelected())
					choiceModel.setBehavior(jarFile, b);
			}
		}
	}
	
	/** Show a modal dialog prompting the user to associate a Behavior to one or more jar Files.
	 * <p>This "OK" button on this dialog is disabled until a behavior is chosen for each file.
	 * 
	 * @param choiceModel the choice model used.
	 * @param jarBehaviors known jar behaviors. This is where the users choices will be stored, so it may be empty but it can not be null.
	 * This is also used to set up the dialog if you want to preset certain choices.
	 * @param jarFiles the set of jar files that the user will make choices for. When this dialog is dismissed it is guaranteed that the
	 * jarBehaviors map will have a non-null value associated with each of these Files.
	 * @param primaryClassName the UI needs the name of the compiled class to explain things to the user.
	 */
	public static void showDialog(JarDependencyChoice choiceModel,Map<File, Behavior> jarBehaviors,Set<File> jarFiles,String primaryClassName) {
		Frame[] frames = WindowList.getFrames(true, false, true);
		if(frames.length==0) {
			frames = new Frame[] { new JFrame() };
		}

		BehaviorDialog dialog = new BehaviorDialog(frames[frames.length-1], 
				new TreeSet<File>(jarFiles), 
				jarBehaviors, primaryClassName,
				choiceModel);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		if(dialog.footer.getLastSelectedComponent()==dialog.footer.getButton(DialogFooter.OK_OPTION)) {
			dialog.save();
		} else {
			throw new UserCancelledException();
		}
	}
}
