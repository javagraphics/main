/*
 * @(#)Gemini.java
 *
 * $Date: 2015-09-21 07:24:13 +0200 (Mo, 21 Sep 2015) $
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
package com.bric.gemini;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.bric.desktop.DesktopApplication;
import com.bric.io.IOUtils;
import com.bric.io.location.FileLocation;
import com.bric.io.location.IOLocation;
import com.bric.io.location.LocationFactory;
import com.bric.plaf.CompactNavigationPanelUI;
import com.bric.plaf.RoundTextFieldUI;
import com.bric.swing.ContextualMenuHelper;
import com.bric.swing.DialogFooter;
import com.bric.swing.JThrobber;
import com.bric.swing.NavigationListener;
import com.bric.swing.NavigationPanel;
import com.bric.swing.QDialog;
import com.bric.swing.TextFieldPrompt;
import com.bric.swing.io.LocationBreadCrumbs;
import com.bric.text.WildcardPattern;
import com.bric.util.JVM;
import com.bric.util.ObservableList;
import com.bric.util.ObservableList.EDTMirror;
import com.bric.viewer.reader.MultiFileReader;
import com.sun.glass.events.KeyEvent;

public class Gemini extends JFrame {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws IOException {
	    DesktopApplication.initialize("com.bric.gemini", "Gemini", "1.0", "jeremy.wood@mac.com", Gemini.class);
	    
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final Gemini e = new Gemini();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						e.pack();
						e.setLocationRelativeTo(null);
						e.setVisible(true);
					}
				});
			}
		});
	}
	
	static class CrumbRowLayout implements LayoutManager {

		static class Layout {
			Map<Component, Rectangle> bounds = new HashMap<>();
			
			public Layout(Container parent) {
				int y = 0;
				Insets insets = new Insets(3,3,3,3);
				for(int a = 0; a<parent.getComponentCount(); a++) {
					y += insets.top;
					Component c = parent.getComponent(a);
					Dimension preferredSize = c.getPreferredSize();
					Rectangle r = new Rectangle(0+insets.left, y, parent.getWidth()-insets.right-insets.left, preferredSize.height);
					bounds.put(c, r);
					y += preferredSize.height;
					y += insets.bottom;
				}
			}

			public Dimension getPreferredSize() {
				int maxX = 0;
				int maxY = 0;
				for(Rectangle r : bounds.values()) {
					maxX = Math.max(maxX, r.x+r.width);
					maxY = Math.max(maxY, r.y+r.height);
				}
				return new Dimension(maxX, maxY);
			}

			public void run() {
				for(Component c : bounds.keySet()) {
					Rectangle r = bounds.get(c);
					c.setBounds(r);
				}
			}
		}
		
		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		@Override
		public void removeLayoutComponent(Component comp) {
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			Layout layout = new Layout(parent);
			return layout.getPreferredSize();
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			return preferredLayoutSize(parent);
		}

		@Override
		public void layoutContainer(Container parent) {
			Layout layout = new Layout(parent);
			layout.run();
		}
		
	}

	static LocationFactory f = new LocationFactory();


	private IOLocation[] getPath(File file) {
		
		List<IOLocation> list = new ArrayList<IOLocation>();
		while(file!=null) {
			list.add(0, f.create(file));
			file = file.getParentFile();
		}
		return list.toArray(new IOLocation[list.size()]);
	}
	
	ObservableList<Set<File>> matches = new ObservableList<>();
	WildcardPattern searchPattern = null;
	EDTMirror filteredMatches = matches.getListModelEDTMirror(new ObservableList.Filter<Set<File>>() {

		@Override
		public boolean accept(Set<File> files) {
			synchronized(matches) {
				if(searchPattern==null) return true;
				synchronized(files) {
					for(File file : files) {
						if(searchPattern.matches(file.getAbsolutePath()))
							return true;
					}
				}
				return false;
			}
		}});
	JPanel secondRow = new JPanel(new GridBagLayout());
	NavigationPanel navPanel = new NavigationPanel();
	JLabel scanningLabel = new JLabel("Scanning...");
	Semaphore pauseSemaphore = new Semaphore(1);
	JButton pauseButton = new JButton("Pause");
	JComponent indicator = new JThrobber();
	List<LocationBreadCrumbs> crumbsList = new ArrayList<>();
	JPanel crumbsContainer = new JPanel(new CrumbRowLayout());
	JPanel scrollPaneContents = new JPanel(new GridBagLayout());
	JTextField searchField = new JTextField(20);
	MultiFileReader  fileReader = MultiFileReader.ALL_FILES;
	
	Thread searchThread = new Thread() {
		public void run() {
			File home = new File(System.getProperty("user.home"));
			search(home);
			SwingUtilities.invokeLater(updateLabelRunnable);
		}
	};

	NavigationListener<IOLocation> navListener = new NavigationListener<IOLocation>() {

		public boolean elementsSelected(
				NavigationListener.ListSelectionType type,
				IOLocation... elements) {
			if(ListSelectionType.DOUBLE_CLICK.equals(type)) {
				File file = ((FileLocation)elements[elements.length-1]).getFile();
				if(!IOUtils.reveal(file)) {
					open(file);
				}
				return true;
			}
			return false;
		}
		
	};

	ContextualMenuHelper contextMenuHelper = new ContextualMenuHelper() {

		@Override
		protected void showPopup(Component c, int x, int y) {
			LocationBreadCrumbs newCrumbs = (LocationBreadCrumbs)c;
			IOLocation loc = newCrumbs.getUI().getCrumb(newCrumbs, new Point(x,y));
			clickedFile = ((FileLocation)loc).getFile();
			super.showPopup(c, x, y);
		}
		
	};

	JLabel unsupportedPlaceholder = new JLabel("These files cannot be previewed in Gemini.");
	JLabel errorPlaceholder = new JLabel("An error occurred previewing these files.");
	JLabel noSelectionPlaceholder = new JLabel("Empty selection.");
	JPanel previewContainer = new JPanel(new GridBagLayout());
	
	JScrollPane crumbsScrollPane = new JScrollPane(scrollPaneContents, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	JScrollPane previewScrollPane = new JScrollPane(previewContainer, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	File lastSampleFile = null;
	
	public Gemini() {
		super("Gemini: Identify Duplicate Files");
		searchThread.start();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
		getRootPane().getActionMap().put("right", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				navPanel.getNextButton().doClick();
			}
		});
		getRootPane().getActionMap().put("left", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				navPanel.getPrevButton().doClick();
			}
		});
		getRootPane().getActionMap().put("up", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JScrollBar jsb =crumbsScrollPane.getVerticalScrollBar();
				jsb.setValue(jsb.getValue()-jsb.getBlockIncrement(-1));
			}
		});
		getRootPane().getActionMap().put("down", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JScrollBar jsb =crumbsScrollPane.getVerticalScrollBar();
				jsb.setValue(jsb.getValue()+jsb.getBlockIncrement(1));
			}
		});
		
		if(JVM.isMac) {
			contextMenuHelper.add("Reveal", new Runnable() {
				public void run() {
					IOUtils.reveal(clickedFile);
				}
			});
		}
		contextMenuHelper.add("Open", new Runnable() {
			public void run() {
				open(clickedFile);
			}
		});
		contextMenuHelper.add("Delete", new Runnable() {
			public void run() {
				int k = QDialog.showDialog(Gemini.this, 
						"Delete", 
						QDialog.WARNING_MESSAGE, 
						"Are you sure you want to delete \""+clickedFile.getName()+"\"?", 
						"This operation is not undoable.", 
						null, //innerComponent, 
						null, //lowerLeftComponent, 
						DialogFooter.OK_CANCEL_OPTION, 
						DialogFooter.OK_OPTION, 
						null, //dontShowKey, 
						null, //alwaysApplyKey, 
						DialogFooter.EscapeKeyBehavior.TRIGGERS_CANCEL);
				if(k==DialogFooter.OK_OPTION) {
					IOUtils.delete(clickedFile);
					synchronized(matches) {
						synchronized(selectedSet) {
							selectedSet.remove(clickedFile);
						}
						if(selectedSet.size()<=1) {
							int currentIndex = selectedSet==null ? -1 : filteredMatches.indexOf(selectedSet);
							Set<File> next;
							if(currentIndex+1<filteredMatches.getSize()) {
								next = (Set<File>)filteredMatches.getElementAt(currentIndex+1);
							} else if(currentIndex-1>=0) {
								next = (Set<File>)filteredMatches.getElementAt(currentIndex-1);
							} else {
								next = null;
							}
							
							synchronized(matches) {
								matches.remove(selectedSet);
								selectedSet = next;
							}
						}
						refreshNavPanel();
					}
				}
			}
		});
		
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3,3,3,3);
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		getContentPane().add(scanningLabel, c);
		c.gridx++; c.weightx = 0;
		getContentPane().add(indicator, c);
		c.gridx++;
		getContentPane().add(pauseButton, c);

		searchField.setUI(new RoundTextFieldUI());
		
		new TextFieldPrompt(searchField,"*.jpg or *iTunes*");
		searchField.putClientProperty("useSearchIcon", Boolean.TRUE);
		
		
		c.gridx = 0;
		c.gridy++; c.weightx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(secondRow, c);
		
		c.gridx = 0;
		c.gridy++; c.fill = GridBagConstraints.BOTH; c.weighty = 1;
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, crumbsScrollPane, previewScrollPane);
		getContentPane().add(splitPane, c);
		
		scrollPaneContents.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTH;
		scrollPaneContents.add(crumbsContainer, c);
		c.gridy++; c.weighty = 1;
		scrollPaneContents.add(new JLabel(" "), c);
		
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		 c.anchor = GridBagConstraints.WEST;
		secondRow.add(navPanel, c);
		c.gridx++; c.weightx = 1; c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		secondRow.add(searchField, c);
		
		
		pauseButton.addActionListener(new ActionListener() {
			boolean paused = false;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!paused) {
					pauseButton.setText("Resume");
					pauseSemaphore.acquireUninterruptibly();
					paused = true;
				} else {
					pauseButton.setText("Pause");
					pauseSemaphore.release();
					paused = false;
				}
			}
		});
		
		navPanel.setUI(new CompactNavigationPanelUI());
		navPanel.getModel().addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				selectedSet = (Set<File>)filteredMatches.getElementAt( navPanel.getElementIndex() );
				refreshNavPanel();
			}
		});
		
		filteredMatches.addListDataListener(new ListDataListener() {

			@Override
			public void intervalAdded(ListDataEvent e) {
				refreshNavPanel();
			}

			@Override
			public void intervalRemoved(ListDataEvent e) {
				refreshNavPanel();
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
				refreshNavPanel();
			}
			
		});
		
		searchField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				refreshSearchResults();
			}
			
		});
		
		refreshNavPanel();
		setPreferredSize(new Dimension(800, 800));
	}
	
	protected void refreshSearchResults() {
		synchronized(matches) {
			WildcardPattern newPattern;
			if(searchField.getText().length()==0) {
				newPattern = null;
			} else {
				newPattern = new WildcardPattern(searchField.getText());
			}
			if(Objects.equals(searchPattern, newPattern))
				return;
			searchPattern = newPattern;
			filteredMatches.refresh(true);
			
			refreshNavPanel();
		}
	}

	private void open(File file) {
		try {
			Desktop.getDesktop().open( file );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	File clickedFile;
	Set<File> selectedSet = null;
	protected void refreshNavPanel() {
		int matchCount = filteredMatches.getSize();
		navPanel.setVisible(matchCount>0);
		crumbsContainer.setVisible(matchCount>0);
		if(matchCount>0) {
			int currentIndex = selectedSet==null ? -1 : filteredMatches.indexOf(selectedSet);
			if(currentIndex==-1) {
				selectedSet = (Set<File>)filteredMatches.getElementAt(0);
				currentIndex = 0;
			}
			
			while(crumbsList.size()<selectedSet.size()) {
				final LocationBreadCrumbs newCrumbs = new LocationBreadCrumbs();
				contextMenuHelper.addComponent(newCrumbs);
				newCrumbs.addNavigationListener(navListener);
				crumbsList.add(newCrumbs);
				GridBagConstraints c = new GridBagConstraints();
				c.gridx = 0; c.gridy = crumbsContainer.getComponentCount();
				c.insets = new Insets(3,3,3,3);
				c.fill = GridBagConstraints.HORIZONTAL;
				c.weightx = 0; c.weighty = 0; c.anchor = GridBagConstraints.NORTHWEST;
				crumbsContainer.add(newCrumbs, c);
			}
			
			File sampleFile = null;
			synchronized(selectedSet) {
				Iterator<File> iter = selectedSet.iterator();
				for(int a = 0; a<crumbsContainer.getComponentCount(); a++) {
					LocationBreadCrumbs c = (LocationBreadCrumbs)crumbsContainer.getComponent(a);
					if(iter.hasNext()) {
						c.setVisible(true);
						File file = iter.next();
						if(sampleFile==null)
							sampleFile = file;
						c.setPath(getPath(file));
					} else {
						c.setVisible(false);
					}
				}
			}
			navPanel.getModel().setElement(currentIndex, matchCount);
			
			if(sampleFile!=null) {
				if(!Objects.equals(lastSampleFile, sampleFile)) {
					try {
						JComponent preview = fileReader.getComponent( LocationFactory.get().create(sampleFile) , null);
						if(preview==null) preview = unsupportedPlaceholder;
						showPreview(preview);
					} catch(IOException e) {
						e.printStackTrace();
						showPreview(errorPlaceholder);
					}
				}
				lastSampleFile = sampleFile;
			} else {
				showPreview(noSelectionPlaceholder);
			}
		}
	}

	protected void showPreview(JComponent preview) {
		previewContainer.removeAll();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy =0 ; c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		previewContainer.add(preview, c);
		previewContainer.repaint();
	}
	
	protected boolean search(File f) {
		pauseSemaphore.acquireUninterruptibly();
		pauseSemaphore.release();
		
		try {
			if(f.isDirectory() && accept(f)) {
				currentFile = f;
				File[] files = f.listFiles();
				if(files==null) return false;
				for(File t : files) {
					search(t);
				}
				return true;
			} else {
				if(f.canRead()) {
					catalogFile(f);
					return true;
				}
				return false;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	Map<Long, Set<File>> fileSizeMap = new HashMap<Long, Set<File>>();
	File currentFile = null;
	Runnable updateLabelRunnable = new Runnable() {
		public void run() {
			if(searchThread.isAlive()) {
				if(currentFile==null) {
					scanningLabel.setText("Scanning...");
				} else {
					scanningLabel.setText("Scanning: "+currentFile.getAbsolutePath());
				}
				indicator.setVisible(true);
				pauseButton.setVisible(true);
			} else {
				scanningLabel.setText("Scan complete.");
				indicator.setVisible(false);
				pauseButton.setVisible(false);
			}
		}
	};
	
	protected boolean accept(File file) {
		if(file.getName().equals(".DS_Store"))
			return false;
		if(IOUtils.isAlias(file))
			return false;
		if(file.isHidden() || file.getName().startsWith("."))
			return false;
		return true;
	}
	
	protected void catalogFile(File file) throws IOException {
		Long size = file.length();
		if(size<1024 || !accept(file))
			return;
		
		currentFile = file;
		SwingUtilities.invokeLater(updateLabelRunnable);
		Set<File> set = fileSizeMap.get(size);
		if(set==null) {
			set = new TreeSet<File>();
			fileSizeMap.put(size, set);
		}
		if(set.size()>0) {
			for(File existingFile : set) {
				processMatch : if(IOUtils.equals(existingFile, file)) {
					for(Set<File> knownMatches : matches) {
						if(knownMatches.contains(existingFile)) {
							synchronized(knownMatches) {
								knownMatches.add(file);
							}
							break processMatch;
						}
					}
					Set<File> newSet = new HashSet<>();
					newSet.add(existingFile);
					newSet.add(file);
					matches.add(newSet);
				}
			}
		}
		set.add(file);
	}
}
