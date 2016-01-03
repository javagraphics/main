/*
 * @(#)SlideshowLocationBrowserUI.java
 *
 * $Date: 2015-09-21 07:24:13 +0200 (Mo, 21 Sep 2015) $
 *
 * Copyright (c) 2013 by Jeremy Wood.
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
package com.bric.viewer.plaf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bric.io.location.IOLocation;
import com.bric.io.location.IOLocationFilter;
import com.bric.io.location.IOLocationGroup;
import com.bric.io.location.IOLocationTreeIterator;
import com.bric.plaf.LocationBrowserUI;
import com.bric.swing.JThrobber;
import com.bric.swing.NavigationPanel;
import com.bric.swing.io.LocationBrowser;
import com.bric.util.ObservableList;
import com.bric.viewer.reader.FileReaderFilter;
import com.bric.viewer.reader.MultiFileReader;

public class SlideshowLocationBrowserUI extends LocationBrowserUI {
	JPanel container = new JPanel();

	NavigationPanel navPanel = new NavigationPanel();
	JThrobber spinner = new JThrobber();
	JLabel loadingPanel = new JLabel();
	MultiFileReader reader = MultiFileReader.ALL_FILES;
	IOLocationFilter filter = new FileReaderFilter(reader, false, false, false);
	
	GatherSlidesThread gatherSlidesThread = null;
	LoadSlideThread loadSlideThread = null;
	int selectedIndex = -1;
	IOLocation selectedContent = null;
	ObservableList<IOLocation> allSlideLocations = new ObservableList<IOLocation>();
	ObservableList<IOLocation> filteredSlideLocations = new ObservableList<IOLocation>();
	Object syncLock = allSlideLocations;
	
	/** The current directory. */
	protected IOLocation dir;

	public SlideshowLocationBrowserUI(LocationBrowser b) {
		super(b);
		
		allSlideLocations.addSynchronizedChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				refreshFilteredSlides();
			}
		});
		
		navPanel.getModel().addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				synchronized(syncLock) {
					int i = navPanel.getElementIndex();
					if(i>=0 && i<filteredSlideLocations.size()) {
						IOLocation e = filteredSlideLocations.get(i);
						setSelectedContent(i, e);
					} else if(filteredSlideLocations.size()>0) {
						setSelectedContent(0, filteredSlideLocations.get(0));
					} else {
						setSelectedContent(-1, null);
					}
				}
			}
		});
		
		loadingPanel.setOpaque(true);
		loadingPanel.setBackground(Color.black);
		loadingPanel.setForeground(Color.white);
		loadingPanel.setVisible(false);
	}
	
	private static Comparator<IOLocation> dateComparator = new Comparator<IOLocation>() {
		public int compare(IOLocation o1, IOLocation o2) {
			long t1 = -1;
			long t2 = -1;
			try {
				t1 = o1.getModificationDate();
			} catch(IOException e) {
				e.printStackTrace();
			}
			try {
				t2 = o2.getModificationDate();
			} catch(IOException e) {
				e.printStackTrace();
			}
			if(t1<t2) return -1;
			if(t1>t2) return 1;
			return 0;
		}
	};
	
	private void refreshFilteredSlides() {
		IOLocation[] allSlides;
		synchronized(syncLock) {
			allSlides = allSlideLocations.toArray(new IOLocation[allSlideLocations.size()]);
		}
		Arrays.sort(allSlides, dateComparator);
		List<IOLocation> newList = new ArrayList<IOLocation>(allSlides.length);
		for(int a = 0; a<allSlides.length; a++) {
			IOLocation c = browser.getFilter().filter(allSlides[a]);
			if(c!=null)
				newList.add(c);
		}
		
		filteredSlideLocations.setAll(newList);
		
		navPanel.setElementCount(filteredSlideLocations.size());
		int index = selectedContent==null ? -1 : filteredSlideLocations.indexOf(selectedContent);
		if(index==-1) {
			if(filteredSlideLocations.size()>0) {
				setSelectedContent(0, filteredSlideLocations.get(0));
			} else {
				setSelectedContent(-1, null);
			}
		} else {
			setSelectedContent(index, selectedContent);
		}
	}
	
	/** This should only be invoked on the EDT in a sync lock
	 * guaranteeing that slideLocations is stable.
	 * 
	 * @param index the index
	 * @param loc
	 */
	private void setSelectedContent(int index,IOLocation loc) {
		if(index!=selectedIndex) {
			if(index>=0) {
				navPanel.setElementIndex(index);
			} else {
				navPanel.setElementIndex(0);
			}
			selectedIndex = index;
		}
		
		if(!Objects.equals(loc, selectedContent)) {
			selectedContent = loc;
			
			loadingPanel.setText(loc.getName());
			if(loadSlideThread!=null)
				loadSlideThread.cancel();
			loadSlideThread = new LoadSlideThread(loc);
			loadSlideThread.start();
		}
	}
	
	private static int loadSlideCtr = 0;
	class LoadSlideThread extends Thread {
		IOLocation loc;
		boolean cancelled = false;
		
		LoadSlideThread(IOLocation loc) {
			super("LoadSlideThread-"+(loadSlideCtr++)+" ("+loc.getPath()+")");
			this.loc = loc;
		}
		
		public void cancel() {
			cancelled = true;
		}
		
		public void run() {
			System.out.println("starting "+this.getName());
			if(cancelled)
				return;
			JComponent jc;
			try {
				 jc = reader.getComponent(loc, container.getSize());
			} catch(IOException e) {
				e.printStackTrace();
				//TODO: replace with something better
				jc = new JLabel("Unable to read");
			}
			final JComponent finalRef = jc;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					synchronized(syncLock) {
						if(cancelled)
							return;
						container.removeAll();
						container.setLayout(new GridBagLayout());
						GridBagConstraints c = new GridBagConstraints();
						c.gridx = 0; c.gridy = 0;

						c.fill = GridBagConstraints.NONE;
						c.weightx = 0; c.weighty = 0;
						c.anchor = GridBagConstraints.NORTHEAST;
						container.add(spinner, c);
						
						installNavigationPanel();
						
						c.anchor = GridBagConstraints.SOUTHWEST;
						container.add(loadingPanel, c);

						c.weightx = 1; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
						if(finalRef==null) {
							//TODO replace with something better
							container.add(new JLabel("null"), c);
						} else {
							container.add(finalRef, c);
						}
						
						if(LoadSlideThread.this==loadSlideThread)
							loadingPanel.setVisible(false);
						
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								container.revalidate();
								container.repaint();
							}
						});
						System.out.println("full implemented "+LoadSlideThread.this.getName());
					}
				}
			});
			System.out.println("exiting "+this.getName());
		}
	}
	
	private static int gatherCtr = 0;
	class GatherSlidesThread extends Thread {
		IOLocation dir;
		IOLocationTreeIterator iter;
		private boolean cancelled = false;
		
		/* A short list of children we've discovered that should be
		 * appended to the master list (slideLocations) in the EDT.
		 * 
		 */
		List<IOLocation> pendingChildren = new ArrayList<IOLocation>();
		
		/* The runnable responsible for emptying pendingChildren; this
		 * should only be invoked on the EDT.  
		 */
		Runnable addPendingRunnable = new Runnable() {
			public void run() {
				synchronized(syncLock) {
					if(pendingChildren.size()==0 || cancelled)
						return;
					allSlideLocations.addAll(pendingChildren);
					pendingChildren.clear();
				}
			}
		};
		
		GatherSlidesThread(IOLocation dir) {
			super("GatherSlidesThread-"+(gatherCtr++)+" ("+dir.getPath()+")");
			this.dir = dir;
		}
		
		public void cancel() {
			synchronized(syncLock) {
				cancelled = true;
			}
			iter.cancel();
		}
		
		public void run() {
			System.out.println("starting "+this.getName());
			synchronized(syncLock) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						synchronized(syncLock) {
							if(!cancelled)
								allSlideLocations.clear();
						}
					}
				});
			}
			IOLocationGroup g = dir.getGroup();
			Object consumerKey = g==null ? null : g.addConsumer();
			try {
				FileReaderFilter iterFilter = new FileReaderFilter(reader, true, true, false);
				iter = new IOLocationTreeIterator(dir, false, iterFilter);
				System.out.println("iterating "+this.getName());
				while(iter.hasNext()) {
					IOLocation child = iter.next();
					child = filter.filter(child);
					if(child!=null) {
						System.out.println("tree iterator: "+child);
						synchronized(syncLock) {
							if(cancelled) return;
							pendingChildren.add(child);
							SwingUtilities.invokeLater(addPendingRunnable);
						}
					}
				}
			} finally {
				if(consumerKey!=null)
					g.releaseConsumer(consumerKey);
				System.out.println("exiting "+this.getName());
			}
		}
	}

	protected void refreshView() {
		if(!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					refreshView();
				}
			});
			return;
		}
		IOLocation newDir = browser.getLocationHistory().getLocation();
		if(!Objects.equals(dir, newDir)) {
			dir = newDir;
			synchronized(syncLock) {
				if(gatherSlidesThread!=null)
					gatherSlidesThread.cancel();
				gatherSlidesThread = new GatherSlidesThread(newDir);
				gatherSlidesThread.start();
			}
		}
	}

	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		
		LocationBrowser browser = (LocationBrowser)c;
		//TODO listen for filter change, then invoke refreshFilteredSlides
		
	}

	@Override
	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);
		synchronized(syncLock) {
			if(gatherSlidesThread!=null)
				gatherSlidesThread.cancel();
			if(loadSlideThread!=null)
				loadSlideThread.cancel();
			dir = null;
			selectedContent = null;
			selectedIndex = -1;
			allSlideLocations.clear();
			filteredSlideLocations.clear();
			container.removeAll();
			Container navParent = navPanel.getParent();
			if(navParent!=null) {
				navParent.remove(navPanel);
			}
			
		}
	}

	@Override
	protected void repaint(IOLocation loc, boolean thumbnail) {}

	@Override
	protected void installGUI(JComponent comp) {
		comp.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		comp.add(container, c);
	}

	@Override
	protected void synchronizeDirectoryContents() {
		refreshView();
	}

	@Override
	public int getVisibleLocationSize() {
		return 1;
	}
	
	protected void installNavigationPanel() {
		if(navPanel.isShowing())
			return;
		
		navPanel.getModel().setDraggable(true);
		RootPaneContainer rpc = null;
		Component c = container;
		while(c!=null && rpc==null) {
			if(c instanceof RootPaneContainer) {
				rpc = (RootPaneContainer)c;
			}
			c = c.getParent();
		}

		if(rpc!=null) {
			navPanel.setSize(navPanel.getPreferredSize());
			Point centerBottom = new Point(container.getWidth()/2, container.getHeight());
			SwingUtilities.convertPoint(container, centerBottom, rpc.getLayeredPane());
			rpc.getLayeredPane().add(navPanel, JLayeredPane.PALETTE_LAYER);
			navPanel.setLocation( centerBottom.x - navPanel.getSize().width/2, centerBottom.y - navPanel.getSize().height );
			
		} else {
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.weighty = 0;
			gbc.anchor = GridBagConstraints.SOUTH;
			container.add(navPanel, c);
		}
	}

}
