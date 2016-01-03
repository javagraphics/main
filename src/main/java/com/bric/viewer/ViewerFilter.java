/*
 * @(#)ViewerFilter.java
 *
 * $Date: 2014-03-13 09:15:48 +0100 (Do, 13 Mär 2014) $
 *
 * Copyright (c) 2014 by Jeremy Wood.
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
package com.bric.viewer;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.bric.io.location.IOLocation;
import com.bric.io.location.IOLocationFilter;
import com.bric.plaf.ButtonCluster;
import com.bric.plaf.RoundRectButtonUI;
import com.bric.swing.JFancyBox;
import com.bric.swing.resources.MinusIcon;
import com.bric.swing.resources.PlusIcon;
import com.bric.swing.resources.TriangleIcon;
import com.bric.viewer.reader.FileReader;
import com.bric.viewer.reader.ImageReader;
import com.bric.viewer.reader.TextReader;

/** This manages the filters that determine what files are made visible in the UI.
 */
public class ViewerFilter {
	
	private static class SimpleFilter extends IOLocationFilter {
		final Pattern[] goodPatterns, badPatterns;
		
		SimpleFilter(PatternList goodPatternList,PatternList badPatternList) {
			goodPatterns = createPatterns(goodPatternList);
			badPatterns = createPatterns(badPatternList);
		}

		private Pattern[] createPatterns(PatternList list) {
			Pattern[] array = new Pattern[list.size()];
			for(int a = 0; a<list.size(); a++) {
				/** The javadoc for Patterns mentions:
				 * "It is an error to use a backslash prior to any alphabetic character 
				 * that does not denote an escaped construct; these are reserved for 
				 * future extensions to the regular-expression language. A backslash 
				 * may be used prior to a non-alphabetic character regardless of whether 
				 * that character is part of an unescaped construct."
				 * 
				 * ... so with that in mind, this is a reliable/simple but over-the-top
				 * approach:
				 */
				String srcText = list.getPattern(a).toLowerCase();
				StringBuffer regexText = new StringBuffer();
				for(int b = 0; b<srcText.length(); b++) {
					char c = srcText.charAt(b);
					if(Character.isLetter(c)) {
						regexText.append(c);
					} else if(c=='*') {
						regexText.append(".*");
					} else {
						regexText.append("\\"+c);
					}
				}
				array[a] = Pattern.compile(regexText.toString());
			}
			return array;
		}

		@Override
		public IOLocation filter(IOLocation loc) {
			if(loc.isNavigable())
				return loc;
			String name = loc.getName().toLowerCase();
			boolean anyGoodPatternMatches = false;
			for(int a = 0; a<goodPatterns.length; a++) {
				if(goodPatterns[a].matcher(name).matches()) {
					anyGoodPatternMatches = true;
				}
			}
			if(!anyGoodPatternMatches)
				return null;

			for(int a = 0; a<badPatterns.length; a++) {
				if(badPatterns[a].matcher(name).matches()) {
					return null;
				}
			}
			
			return loc;
		}
		
	}
	
	FileReader[] readers = new FileReader[] { new ImageReader(), new TextReader() };
	Map<FileReader, JCheckBoxMenuItem> map = new Hashtable<FileReader, JCheckBoxMenuItem>();
	JCheckBoxMenuItem customMenuItem = new JCheckBoxMenuItem("Custom...");
	
	static class PatternList {
		String name;
		
		List<String> patterns = new ArrayList<String>();
		List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
		Map<Integer, List<ChangeListener>> specificListeners = new Hashtable<Integer, List<ChangeListener>>();
		
		PatternList(String name) {
			this.name = name;
			loadFromPrefs();
			addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					saveToPrefs();
				}
			});
		}
		
		protected void loadFromPrefs() {
			Preferences prefs = Preferences.userNodeForPackage(PatternList.class);
			int ctr = 0;
			while(true) {
				String p = prefs.get(name+".pattern"+ctr, null);
				if(p!=null) {
					patterns.add(p);
				} else {
					return;
				}
				ctr++;
			}
		}
		
		protected void saveToPrefs() {
			Preferences prefs = Preferences.userNodeForPackage(PatternList.class);
			for(int a = 0; a<patterns.size(); a++) {
				prefs.put(name+".pattern"+a, patterns.get(a));
			}
			prefs.remove(name+".pattern"+patterns.size());
		}
		
		public int size() {
			return patterns.size();
		}
		
		/** Add a listener to be notified when the number of patterns changes, or
		 * when a specific pattern changes. */
		public void addChangeListener(ChangeListener l) {
			changeListeners.add(l);
		}
		
		public void removeChangeListener(ChangeListener l) {
			changeListeners.remove(l);
		}
		
		/** Add a listener to be notified when a specific pattern changes. */
		public void addChangeListener(ChangeListener l,int patternIndex) {
			List<ChangeListener> list = specificListeners.get(patternIndex);
			if(list==null) {
				list = new ArrayList<ChangeListener>();
				specificListeners.put(patternIndex, list);
			}
			list.add(l);
		}
		
		public void removeChangeListener(ChangeListener l,int patternIndex) {
			List<ChangeListener> list = specificListeners.get(patternIndex);
			if(list!=null) {
				list.remove(l);
			}
		}
		
		protected void fireChangeListeners(List<ChangeListener> listeners) {
			if(listeners==null) return;
			
			for(ChangeListener l : listeners) {
				l.stateChanged(new ChangeEvent(this));
			}
		}
		
		public void addPattern(String text,int index) {
			patterns.add(index, text);
			fireChangeListeners(changeListeners);
		}
		
		public void addPattern(String text) {
			addPattern(text, patterns.size());
		}
		
		public boolean containsPattern(String s) {
			return indexOfPattern(s)!=-1;
		}
		
		public int indexOfPattern(String s) {
			for(int a = 0; a<patterns.size(); a++) {
				if(patterns.get(a).equals(s))
					return a;
			}
			return -1;
		}
		
		public void removePattern(int index) {
			patterns.remove(index);
			fireChangeListeners(changeListeners);
		}
		
		public String getPattern(int index) {
			return patterns.get(index);
		}
		
		public boolean setPattern(int index,String text) {
			String oldPattern = patterns.get(index);
			if(oldPattern.equals(text))
				return false;
			patterns.set(index, text);
			fireChangeListeners(specificListeners.get(index));
			fireChangeListeners(changeListeners);
			return true;
		}
	}
	
	PatternList passingPatterns = new PatternList("passing");
	PatternList failingPatterns = new PatternList("failing");
	
	/** The button that displays the filterMenu.
	 * I tried simply making this a JMenu, but
	 * despite extending JButtons: Swing does not
	 * like plopping a JMenu in your UI unless it is
	 * in a menubar (I saw at least one looping/locking bug),
	 * so I'll stick to elements that feel more familiar.
	 */
	JButton filterMenuTrigger = new JButton();
	
	JPopupMenu filterMenu = new JPopupMenu();
	
	Runnable updateLaterRunnable = new Runnable() {
		public void run() {
			updateControls();
		}
	};
	
	ActionListener menuItemListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(updateLaterRunnable);
		}
	};
	
	class FileReaderCheckboxItem extends JCheckBoxMenuItem {
		private static final long serialVersionUID = 1L;
		
		FileReader reader;
		FileReaderCheckboxItem(FileReader reader) {
			super(reader.getDescription());
			this.reader = reader;
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String[] ext = FileReaderCheckboxItem.this.reader.getExtensions();
					if(isSelected()) {
						for(String s : ext) {
							if(!passingPatterns.containsPattern("*."+s)) {
								passingPatterns.addPattern("*."+s);
							}
						}
					} else {
						for(String s : ext) {
							int index = passingPatterns.indexOfPattern("*."+s);
							if(index!=-1)
								passingPatterns.removePattern(index);
						}
					}
				}
			});
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateState();
				}
			});
		}
		
		protected void updateState() {
			String[] ext = FileReaderCheckboxItem.this.reader.getExtensions();
			boolean active = ext.length>0;
			for(String s : ext) {
				if(!passingPatterns.containsPattern("*."+s)) {
					active = false;
				}
			}
			setSelected(active);
		}
	}

	CustomPanel customPanel = new CustomPanel();
	
	public ViewerFilter() {
		filterMenuTrigger.setUI( ViewerApplet.buttonUI );
		
		for(int a = 0; a<readers.length; a++) {
			FileReaderCheckboxItem item = new FileReaderCheckboxItem( readers[a] );
			item.addActionListener(menuItemListener);
			map.put(readers[a], item);
			filterMenu.add(item);
		}
		filterMenu.addSeparator();
		filterMenu.add(customMenuItem);
		filterMenuTrigger.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(filterMenu.isShowing()) {
					filterMenu.setVisible(false);
				} else {
					filterMenu.show(filterMenuTrigger, 0, filterMenuTrigger.getHeight());
				}
			}
		});
		
		filterMenuTrigger.setIcon(new TriangleIcon(SwingConstants.SOUTH, 8, 4));
		filterMenuTrigger.setIconTextGap(8);
		filterMenuTrigger.setHorizontalTextPosition(SwingConstants.LEFT);
		MouseListener mouseListener = new MouseAdapter() {

			private void setActive(boolean b) {
				filterMenuTrigger.setContentAreaFilled(b);
				filterMenuTrigger.setBorderPainted(b);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				setActive(true);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setActive(false);
			}
		};
		filterMenuTrigger.addMouseListener(mouseListener);
		mouseListener.mouseExited(null);
		
		customMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showCustomOptions();
			}
		});
		
		SwingUtilities.invokeLater(updateLaterRunnable);
		
		ChangeListener locationUpdateListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setFilter(new SimpleFilter( passingPatterns, failingPatterns));
			}
		};
		passingPatterns.addChangeListener(locationUpdateListener);
		failingPatterns.addChangeListener(locationUpdateListener);
		locationUpdateListener.stateChanged(null);
	}
	
	List<ChangeListener> filterListeners = new ArrayList<ChangeListener>();
	IOLocationFilter currentFilter;
	
	public IOLocationFilter getFilter() {
		return currentFilter;
	}
	
	protected boolean setFilter(IOLocationFilter filter) {
		if(Objects.equals(filter, currentFilter))
			return false;
		currentFilter = filter;
		fireFilterListeners();
		return true;
	}
	
	protected void fireFilterListeners() {
		for(ChangeListener l : filterListeners) {
			l.stateChanged(new ChangeEvent(this));
		}
	}
	
	/** This listener is notified when <code>getFilter()</code> is updated. */
	public void addChangeListener(ChangeListener l) {
		filterListeners.add(l);
	}
	
	public void removeChangeListener(ChangeListener l) {
		filterListeners.remove(l);
	}
	
	protected void showCustomOptions() {
		RootPaneContainer rpc = null;
		Component c = filterMenuTrigger;
		while(c!=null && rpc==null) {
			if(c instanceof RootPaneContainer) {
				rpc = (RootPaneContainer)c;
			} else {
				c = c.getParent();
			}
		}
		JFancyBox fb = new JFancyBox(rpc, customPanel);
		fb.setVisible(true);
	}
	
	protected void updateControls() {
		Set<FileReader> active = new HashSet<FileReader>();
		for(FileReader r : readers) {
			JCheckBoxMenuItem c = map.get(r);
			if(c.isSelected()) {
				active.add(r);
			}
		}
		if(active.size()==readers.length) {
			filterMenuTrigger.setText("All Files");
		} else if(active.size()==1) {
			FileReader singular = active.iterator().next();
			filterMenuTrigger.setText(singular.getDescription());
		} else {
			filterMenuTrigger.setText("Custom");
		}
		customPanel.updateControls();
	}
	
	class PatternEditor extends JPanel {
		private static final long serialVersionUID = 1L;

		JTextField textField = new JTextField(10);
		JLabel label = new JLabel("Empty");
		JButton addButton = new JButton(new PlusIcon(10, 10));
		JButton removeButton = new JButton(new MinusIcon(10, 10));
		PatternList patternList;
		int index;
		
		public PatternEditor(PatternList patternList, int index) {
			super(new GridBagLayout());
			setOpaque(false);
			this.patternList = patternList;
			this.index = index;
			if(index>=0)
				textField.setText(patternList.getPattern(index));
			
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			add(textField, c);
			add(label, c);
			c.gridx++; c.weightx = 0;
			add(addButton, c);
			c.gridx++;
			add(removeButton, c);
			
			ButtonCluster.install( new AbstractButton[] { addButton, removeButton}, new RoundRectButtonUI(), false );
			
			label.setVisible(index==-1);
			removeButton.setVisible(index!=-1);
			textField.setVisible(index!=-1);
			patternList.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							String s = PatternEditor.this.patternList.getPattern(
									PatternEditor.this.index
									);
							String oldText = textField.getText();
							if(!oldText.equals(s))
								textField.setText( s );
						}
					});
				}
			}, index);
			
			textField.getDocument().addDocumentListener(new DocumentListener() {

				public void insertUpdate(DocumentEvent e) {
					changedUpdate(e);
				}

				public void removeUpdate(DocumentEvent e) {
					changedUpdate(e);
				}

				public void changedUpdate(DocumentEvent e) {
					PatternEditor.this.patternList.setPattern(
							PatternEditor.this.index, 
							textField.getText());
				}
			});
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PatternEditor.this.patternList.addPattern("*.*", 
							PatternEditor.this.index+1);
				}
			});
			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PatternEditor.this.patternList.removePattern(PatternEditor.this.index);
				}
			});
		}
		
	}
	
	class CustomPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		public CustomPanel() {
			setLayout(new GridBagLayout());
			setOpaque(false);

			addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					e.consume();
				}

				@Override
				public void mousePressed(MouseEvent e) {
					e.consume();
				}
				
			});
			
			ChangeListener patternListener = new ChangeListener() {
				int lastPassingSize = -1;
				int lastFailingSize = -1;
				public void stateChanged(ChangeEvent e) {
					int passingSize = passingPatterns.size();
					int failingSize = failingPatterns.size();
					if(lastPassingSize!=passingSize || lastFailingSize!=failingSize) {
						refreshUI();
						lastPassingSize = passingSize;
						lastFailingSize = failingSize;
					}
				}
			};
			passingPatterns.addChangeListener(patternListener);
			failingPatterns.addChangeListener(patternListener);
			
			refreshUI();
		}
		
		protected void refreshUI() {
			removeAll();
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0;
			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(3,3,3,3);
			c.fill = GridBagConstraints.NONE;
			add(new JLabel("Accept all of these files:"), c);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(3,20,3,3);
			if(passingPatterns.size()==0) {
				c.gridy++;
				add(new PatternEditor(passingPatterns, -1), c);
			} else {
				for(int a = 0; a<passingPatterns.size(); a++) {
					c.gridy++;
					add(new PatternEditor(passingPatterns, a), c);
				}
			}
			
			c.gridy++;
			c.fill = GridBagConstraints.NONE;
			c.insets = new Insets(3,3,3,3);
			add(new JLabel("Reject all of these files:"), c);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(3,20,3,3);
			if(failingPatterns.size()==0) {
				c.gridy++;
				add(new PatternEditor(failingPatterns, -1), c);
			} else {
				for(int a = 0; a<failingPatterns.size(); a++) {
					c.gridy++;
					add(new PatternEditor(failingPatterns, a), c);
				}
			}
			
			JPanel fluff = new JPanel();
			fluff.setOpaque(false);
			c.weighty = 1;
			c.gridy++;
			add(fluff, c);
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					revalidate();
					repaint();
				}
			});
		}
		
		protected void updateControls() {
			
		}
	}
}
