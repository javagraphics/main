/*
 * @(#)FileCollection.java
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

import java.io.File;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;

import com.bric.io.FileTreeIterator;
import com.bric.util.ObservableSet;
import com.bric.util.ObservableSet.Listener;
import com.bric.util.ObservableSet.SetDataEvent;

public abstract class FileCollection {
	
	public static void search(File directoryToSearch,final Runnable edtRunnable,final FileCollection... collections) {
		final File searchRoot = directoryToSearch==null ? new File(System.getProperty("user.home")) : directoryToSearch;
		Thread initializePaths = new Thread("Initialize Paths") {
			@Override
			public void run() {
				FileTreeIterator iter = new FileTreeIterator(searchRoot);
				try {
					while(iter.hasNext()) {
						File file = iter.next();
						for(FileCollection c : collections) {
							c.process(file);
						}
					}
				} finally {
					SwingUtilities.invokeLater(edtRunnable);
				}
			}
		};
		initializePaths.start();
	}
	
	static class CaseInsensitiveFileComparator implements Comparator<File> {
		@Override
		public int compare(File o1, File o2) {
			String s1 = o1.getAbsolutePath().toLowerCase();
			String s2 = o2.getAbsolutePath().toLowerCase();
			int i = s1.compareTo(s2);
			if(i!=0) return i;
			return o1.compareTo(o2);
		}
	}
	
	protected final  String PREF_KEY_PREFIX;
	protected final ObservableSet<File> files = new ObservableSet<File>(File.class, new TreeSet<File>(new CaseInsensitiveFileComparator()));
	protected final Preferences preferences;
	
	/**
	 * 
	 * @param prefKey a key used to to read/write preferences. It should resemble "sourcepath-" or "keystore-".
	 */
	protected FileCollection(String prefKey) {
		PREF_KEY_PREFIX = prefKey;
		preferences = Preferences.userNodeForPackage(getClass());

		int ctr = 0;
		loadPrefs : while(true) {
			String s = preferences.get(PREF_KEY_PREFIX+ctr, null);
			if(s==null) {
				break loadPrefs;
			} else {
				File f = new File(s);
				process(f);
			}
			ctr++;
		}
		
		files.addListener(new Listener<File>() {
			
			private void resavePreferences() {
				File[] array = files.toComponentArray();
				for(int a = 0; a<array.length; a++) {
					preferences.put(PREF_KEY_PREFIX+a, array[a].getAbsolutePath());
				}
				preferences.remove(PREF_KEY_PREFIX+array.length);
			}

			@Override
			public void elementsAdded(SetDataEvent<File> event) {
				resavePreferences();
			}

			@Override
			public void elementsChanged(SetDataEvent<File> event) {
				resavePreferences();
			}

			@Override
			public void elementsRemoved(SetDataEvent<File> event) {
				resavePreferences();
			}
			
		});
	}
	
	public abstract void process(File file);

	public File[] getFiles() {
		return files.toComponentArray();
	}

	public void addChangeListener(ChangeListener workspaceListener) {
		files.addChangeListener(workspaceListener);
	}

	public void addRemoveListener(ChangeListener workspaceListener) {
		files.removeChangeListener(workspaceListener);
	}
}
