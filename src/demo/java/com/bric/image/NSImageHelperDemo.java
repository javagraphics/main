/*
 * @(#)NSImageHelperDemo.java
 *
 * $Date: 2015-02-28 21:59:45 +0100 (Sa, 28 Feb 2015) $
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
package com.bric.image;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bric.blog.Blurb;
import com.bric.io.FileTreeIterator;
import com.bric.job.Job;
import com.bric.job.JobManager;
import com.bric.job.swing.JobStatusBar;
import com.bric.util.ObservableList;

/** This demonstrates all the known images/fields in the {@link NSImageHelper} class, 
 * and offers two searches to scan for more images. (As of this writing, though:
 * all possible known images have been documented.)
 * <p>The available images are displayed in a giant table, and this demo app includes
 * an option to export the results as a a screenshot. Below is the table after a few
 * hours of searching:
 * <img src="https://javagraphics.java.net/resources/NSImages.png" alt="table of NSImages">
 * 
 */
@Blurb (
filename = "NSImageHelper",
title = "Images: Accessing NSImages on Mac",
releaseDate = "June 2008",
summary = "This shows how to access <code>NSImages</code> on Mac, and enumerates dozens of such images. "+
"Some are well documented, but some were identified through a brute-force search.",
link = "http://javagraphics.blogspot.com/2008/06/nsimage-accessing-images-in-mac-105.html",
sandboxDemo = false
)
public class NSImageHelperDemo extends JFrame {
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				NSImageHelperDemo demo = new NSImageHelperDemo();
				demo.pack();
				demo.setVisible(true);
				demo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		});
	}
	
	/** An image and the set of names it might go by. */
	static class ImageCluster {
		final BufferedImage bi;
		final SortedSet<String> names = new TreeSet<String>();
		final List<ChangeListener> listeners = new ArrayList<ChangeListener>(); 
		
		ImageCluster(BufferedImage bi,String name) {
			this.bi = bi;
			names.add(name);
		}
		
		/** Add a listener to be notified when the list of
		 * names changes.
		 */
		void addChangeListener(ChangeListener l) {
			listeners.add(l);
		}

		void removeChangeListener(ChangeListener l) {
			listeners.remove(l);
		}
		
		void fireChangeListeners() {
			for(ChangeListener l : listeners) {
				l.stateChanged(new ChangeEvent(this));
			}
		}
		
		synchronized String[] getNames() {
			return names.toArray(new String[names.size()]);
		}
		
		/** Return true if these ImageClusters are the same,
		 * and all the names from the argument have been added
		 * to this cluster.
		 */
		synchronized boolean merge(ImageCluster incoming) {
			if(ImageComparison.equals(bi, incoming.bi)) {
				if(names.addAll(incoming.names)) {
					fireChangeListeners();
				}
				return true;
			}
			return false;
		}
	}
	
	/** A panel that displays an ImageCluster. */
	static class ImageClusterPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		final ImageCluster cluster;
		
		ImageClusterPanel(ImageCluster c) {
			cluster = c;
			cluster.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					refreshLabels();
				}
			});
			setOpaque(true);
			setBackground(Color.white);
			refreshLabels();
		}
		
		protected void refreshLabels() {
			removeAll();
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
			c.insets = new Insets(4,4,4,4);
			add(new JLabel(new ImageIcon(cluster.bi)), c);
			c.insets = new Insets(2,2,2,2);
			String[] names = cluster.getNames();
			for(String name : names) {
				c.gridy++;
				JLabel label = new JLabel(name);
				label.setFont(label.getFont().deriveFont(11f));
				add(label, c);
			}
			revalidate();
		}
	}
	
	ObservableList<ImageClusterPanel> panels = new ObservableList<ImageClusterPanel>();
	JPanel allClusters = new JPanel(new GridBagLayout());
	
	JButton screenshotButton = new JButton("Save Screenshot...");
	JButton searchButton = new JButton("Start Search");
	Set<NSImageHelper> allImages = NSImageHelper.getAllImages();
	JobManager jobManager = new JobManager(2);
	JobStatusBar statusBar = new JobStatusBar(jobManager, false);
	
	public NSImageHelperDemo() {
		panels.addUnsynchronizedChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				refreshPanels();
			}
		}, false);

		System.out.println("## Previously Identified NSImages:");
		Set<NSImageHelper> set = NSImageHelper.getAllImages();
		for(NSImageHelper h : set) {
			process(h);
		}
		
		//make sure all the public static NSImageHelper fields work
		/*Field[] f = NSImageHelper.class.getDeclaredFields();
		for(Field field : f) {
			if( (field.getModifiers() & Modifier.STATIC)>0 &&
					 (field.getModifiers() & Modifier.PUBLIC)>0 ) {
				try {
					Object t = field.get(null);
					if(t==null)
						System.err.println("!! missing field: "+field);
				} catch (Throwable t) {
					throw new RuntimeException(t);
				}
			}
		}*/
		
		System.out.println("## Unidentified NSImages:");
		
		JPanel buttonRow = new JPanel(new GridBagLayout());
		
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		getContentPane().add(new JScrollPane(allClusters), c);
		c.gridy++; c.weighty = 0; c.insets = new Insets(5,5,5,5);
		c.fill = GridBagConstraints.NONE;
		getContentPane().add(buttonRow, c);
		c.gridy++; c.insets = new Insets(0,0,0,0);
		c.fill = GridBagConstraints.BOTH;
		getContentPane().add(statusBar, c);

		c = new GridBagConstraints();
		c.insets = new Insets(4,4,4,4);
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		buttonRow.add(screenshotButton, c);
		c.gridx++;
		buttonRow.add(searchButton, c);
		
		allClusters.setOpaque(true);
		allClusters.setBackground(Color.white);
		
		screenshotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					doSaveScreenshot();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchButton.setEnabled(false);
				jobManager.addJob(new RandomLetterJob());
				jobManager.addJob(new RandomWordJob());
			}
		});
	}
	
	void refreshPanels() {
		int panelsPerRow = 5;
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1; c.weighty = 1;
		c.insets = new Insets(4,4,4,4);
		Object[] panels = this.panels.toArray();
		for(int a = 0; a<panels.length; a++) {
			ImageClusterPanel panel = (ImageClusterPanel)panels[a];
			if(panel.getParent()!=allClusters) {
				c.gridx = a%panelsPerRow;
				c.gridy = a/panelsPerRow;
				allClusters.add(panel, c);
			}
		}
		allClusters.revalidate();
		allClusters.repaint();
	}
	
	void doSaveScreenshot() throws IOException {
		FileDialog fd = new FileDialog(this);
		fd.setMode(FileDialog.SAVE);
		fd.pack();
		fd.setFile("NSImages.png");
		fd.setVisible(true);
		
		if(fd.getFile()==null) {
			return;
		}
		File file = new File(fd.getDirectory()+fd.getFile());
		BufferedImage bi = new BufferedImage(allClusters.getWidth(), allClusters.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		allClusters.paint(g);
		g.dispose();
		ImageIO.write(bi, "png", file);
	}
	
	void process(final NSImageHelper incomingImage) {
		if(incomingImage==null)
			throw new NullPointerException();
		if(allImages.contains(incomingImage) && jobManager.isActive())
			return;
		
		if(!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					process(incomingImage);
				}
			});
			return;
		}
		System.out.println(incomingImage);
		ImageCluster newCluster = new ImageCluster(incomingImage.getImage(), incomingImage.getName());
		Object[] panels = this.panels.toArray();
		for(int a = 0; a<panels.length; a++) {
			ImageClusterPanel panel = (ImageClusterPanel)panels[a];
			if(panel.cluster.merge(newCluster))
				return;
		}
		this.panels.add(new ImageClusterPanel(newCluster));
	}
	
	/** This tests random letter combinations to identify NSImageHelpers. */
	class RandomLetterJob extends Job {

		@Override
		protected void runJob() throws Exception {
			int ctr = 0;
			for(int charCount = 1; charCount<50; charCount++) {
				setDescription("Testing "+charCount+" characters...");
				char[] array = new char[charCount];
				Arrays.fill(array, 'a');
				boolean finished = false;
				while(!finished) {
					try {
						String name = new String(array);
						
						if( (++ctr)%1000 == 0) {
							setNote(name);
						}
						
						NSImageHelper h = NSImageHelper.get(name);
						process(h);
					} catch(Exception e) {}
					
					finished = !iterate(array);
				}
			}
		}
		
		private boolean iterate(char[] c) {
			int i = c.length-1;
			while(i>-1) {
				char ch = c[i];
				if(ch=='z') {
					c[i] = 'A';
					return true;
				} else if(ch=='Z') {
					c[i] = 'a';
					i--;
				} else {
					c[i] = (char)(c[i]+1);
					return true;
				}
			}
			return false;
		}
		
	}

	/** This tests random word combinations to identify NSImageHelpers.
	 * <p>This first searches for a "TWL06.txt" file on your computer
	 * (this is a Scrabble tournament word list) to use as a list of
	 * possible input words.
	 * <p>Also the list of possible words is sorted to first focus on words
	 * that existed in known NSImageHelper names.
	 */
	class RandomWordJob extends Job {

		@Override
		protected void runJob() throws Exception {
			List<String> allWords = getTWLWords();
			StringBuffer sb = new StringBuffer();

			for(int wordCount = 1; wordCount<5; wordCount++) {
				setDescription("Testing "+wordCount+" words...");
				List<String> wordList = reorderWordList(allWords);
				int[] wordIndices = new int[wordCount];
				Arrays.fill(wordIndices, 0);
				boolean finished = false;
				int ctr = 0;
				while(!finished) {
					sb.delete(0, sb.length());
					for(int k = 0; k<wordIndices.length; k++) {
						sb.append(wordList.get(wordIndices[k]));
					}
					try {
						String name = sb.toString();

						if( (++ctr)%1000 == 0) {
							setNote(name);
						}
						
						NSImageHelper h = NSImageHelper.get(new String(name));
						process(h);
					} catch(Exception e) {}
					
					finished = !iterate(wordIndices, wordList.size());
				}
			}
		}

		
		/** Return all words from a TWL06.txt file, capitalizing only the first
		 * letter of each word (as appears to be the custom in NSImage names).
		 */
		private List<String> getTWLWords() throws IOException {
			List<String> allWords = new ArrayList<String>();
			File file = FileTreeIterator.find(new File(System.getProperty("user.home")), "TWL06.txt");
			FileInputStream fileIn = null;
			try {
				fileIn = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(fileIn));
				String s = br.readLine();
				while(s!=null) {
					if(s.length()>0) {
						s = s.toLowerCase();
						s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
						allWords.add(s);
					}
					s = br.readLine();
				}
				br.close();
			} finally {
				try {
					if(fileIn!=null)
						fileIn.close();
				} catch(Exception e) {}
			}
			return allWords;
		}

		
		/** Sort the argument, pushing all words in currently known NSImages to the 
		 * front of the list.
		 * @return
		 */
		private List<String> reorderWordList(List<String> allWords) {
			Set<String> knownWords = getKnownWords();
			List<String> returnValue = new ArrayList<String>();
			returnValue.addAll(knownWords);
			for(int a = 0; a<allWords.size(); a++) {
				String word = allWords.get(a);
				if(!knownWords.contains(word)) {
					returnValue.add(word);
				}
			}
			return returnValue;
		}
		
		/** Return all the words of currently known NSImageHelpers.
		 */
		private Set<String> getKnownWords() {
			Set<String> returnValue = new TreeSet<String>();
			Set<NSImageHelper> allImages = NSImageHelper.getAllImages();
			for(NSImageHelper h : allImages) {
				String name = h.nsImageName;
				parseWords(name, returnValue);
			}
			return returnValue;
		}
		
		/** Parse all the words in the argument <code>text</code>
		 * and store them in <code>dest</code>.
		 */
		private void parseWords(String text,Set<String> dest) {
			StringBuffer sb = new StringBuffer();
			for(int a = 0; a<text.length(); a++) {
				char ch = text.charAt(a);
				if(Character.isUpperCase(ch)) {
					if(sb.length()>0) {
						dest.add(sb.toString());
						sb.delete(0, sb.length());
					}
					sb.append(ch);
				} else {
					sb.append(ch);
				}
			}
			if(sb.length()>0) {
				dest.add(sb.toString());
			}
		}
		
		private boolean iterate(int[] c,int max) {
			int i = c.length-1;
			while(i>-1) {
				int v = c[i];
				v++;
				if(v==max) {
					c[i] = 0;
					i--;
				} else {
					c[i] = v;
					return true;
				}
			}
			return false;
		}
		
	}
}
