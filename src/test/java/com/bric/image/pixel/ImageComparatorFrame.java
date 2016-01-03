/*
 * @(#)ScalingIteratorDemo.java
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
package com.bric.image.pixel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.bric.blog.Blurb;
import com.bric.image.ImageSize;
import com.bric.image.bmp.BmpEncoder;
import com.bric.io.FileTreeIterator;
import com.bric.io.IOUtils;
import com.bric.util.ObservableList;

/** This class demos the ScalingIterator with a little GUI by browsing local files.
 * 
 */
@Blurb (
filename = "ImageComparator",
title = "Images: Comparing Image Parsers",
releaseDate = "TBA",
summary = "This iterates over your local computer finding PNGs, and uses different "+
"parsing models to load BufferedImages. This also offers a contrast view.",
scrapped = "This is a specialized tool for debugging/testing.",
sandboxDemo = false
)
public class ImageComparatorFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	static String SOURCE_IMAGE_KEY = "ImageComparatorFrame.srcImage";
	
	public static void main(String[] args) {
		final ObservableList<File> failures = new ObservableList<File>();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ImageComparatorFrame demo = new ImageComparatorFrame(failures);
				demo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				demo.pack();
				demo.setVisible(true);
			}
		});
		testBMPEncoding(failures);
	}
	
	static TexturePaint checkers;
	
	class PreviewComponent extends JComponent {
		private static final long serialVersionUID = 1L;
		
		BufferedImage image;
		
		public PreviewComponent() {
		}
		
		public void setImage(BufferedImage bi) {
			image = bi;
			repaint();
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			if(checkers==null) {
				BufferedImage bi = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
				Graphics2D g2 = bi.createGraphics();
				g2.setColor(Color.white);
				g2.fillRect(0,0,bi.getWidth(),bi.getHeight());
				g2.setColor(Color.lightGray);
				g2.fillRect(0,0,bi.getWidth()/2,bi.getHeight()/2);
				g2.fillRect(bi.getWidth()/2,bi.getHeight()/2,bi.getWidth()/2,bi.getHeight()/2);
				g2.dispose();
				checkers = new TexturePaint(bi, new Rectangle(0,0,bi.getWidth(),bi.getHeight()));
			}
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D)g;
			g2.setPaint(checkers);
			g2.fillRect(0,0,getWidth(),getHeight());
			
			if(image!=null) {
				g.drawImage(image, 0, 0, null);
			}
		}
	}
	
	class AddFileRunnable implements Runnable {
		File file;
		DefaultMutableTreeNode parent;
		
		public AddFileRunnable(File f,DefaultMutableTreeNode parent) {
			file = f;
			this.parent = parent;
		}
		
		public void run() {
			DefaultTreeModel model = ((DefaultTreeModel)tree.getModel());
			DefaultMutableTreeNode newChild = new DefaultMutableTreeNode();
			newChild.setUserObject(file);
			model.insertNodeInto(newChild, parent, parent.getChildCount());
		}
	}
	
	PreviewComponent preview1;
	PreviewComponent preview2;
	PreviewComponent contrast;
	JTabbedPane tabs = new JTabbedPane();
	DefaultMutableTreeNode root = new DefaultMutableTreeNode();
	DefaultMutableTreeNode bmpNode = new DefaultMutableTreeNode("BMP");
	JTree tree = new JTree(root);
	JScrollPane scrollPane = new JScrollPane(tree);
	JPanel leftPanel = new JPanel();
	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, tabs);
	JButton pauseButton = new JButton("Pause");
	
	public ImageComparatorFrame(final ObservableList<File> failures) {
		super("Image Comparator");
		
		preview1 = new PreviewComponent();
		preview2 = new PreviewComponent();
		contrast = new PreviewComponent();

		DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
		model.insertNodeInto(bmpNode, root, root.getChildCount());
		tree.setRootVisible(false);
		tree.expandPath( new TreePath(new Object[] {root}));
		tree.expandPath( new TreePath(new Object[] {root, bmpNode}));
		
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 0;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.gridwidth = 1;
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		getContentPane().add(splitPane, gbc);
		tabs.setPreferredSize(new Dimension(500, 500));
		tabs.setMinimumSize(new Dimension(500, 500));
		
		leftPanel.setPreferredSize(new Dimension(200, 200));
		leftPanel.setMinimumSize(new Dimension(200, 200));
		
		leftPanel.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 0;
		gbc.weightx = 1; gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		leftPanel.add(scrollPane, gbc);
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy++; gbc.weighty = 0;
		gbc.insets = new Insets(5,5,5,5);
		leftPanel.add(pauseButton, gbc);
		
		tabs.add(preview1, "Iterator");
		tabs.add(preview2, "ImageIO");
		tabs.add(contrast, "Contrast");
		
		failures.addUnsynchronizedListener(new ListDataListener() {

			public void intervalAdded(ListDataEvent e) {
				contentsChanged(e);
			}

			public void intervalRemoved(ListDataEvent e) {
				contentsChanged(e);
			}

			private int inside = 0;
			public void contentsChanged(ListDataEvent e) {
				@SuppressWarnings("unchecked")
				ObservableList<File> list = (ObservableList<File>)e.getSource();
				synchronized( list ) {
					if(inside!=0)
						return;
					inside++;
					try {
						while(list.size()>0) {
							File file = list.remove(0);
							SwingUtilities.invokeLater(new AddFileRunnable(file, bmpNode));
						}
					} finally {
						inside--;
					}
				}
			}
		}, false, true);
		
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				TreePath path = tree.getSelectionPath();
				if(path!=null) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
					if(node.getUserObject() instanceof File) {
						File file = (File)node.getUserObject();
						loadImage( file );
					}
				}
			}
		});
		
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(pauseButton.getText().equals("Pause")) {
					pauseButton.setText("Play");
					Thread pauseThread = new Thread() {
						@Override
						public void run() {
							synchronized(failures) {
								while(pauseButton.getText().equals("Play")) {
									try {
										Thread.sleep(10);
									} catch(Exception e) {
										e.printStackTrace();
									}
								}
							}
						}
					};
					pauseThread.start();
				} else {
					pauseButton.setText("Pause");
				}
			}
		});
	}

	public void loadImage(File file) {
		BufferedImage image1 = null;
		BufferedImage image2 = null;
		BufferedImage image3 = null;
		File tempFile = null;
		try {
			image1 = BmpEncoder.read(file);			
			preview1.setImage( image1 );
		} catch(Throwable t) {
			t.printStackTrace();
		} finally {
			if(tempFile!=null)
				tempFile.delete();
		}

		try {
			image2 = ImageIO.read(file) ;
			preview2.setImage( image2 );
		} catch(Throwable t) {
			t.printStackTrace();
		}
		
		image3 = getDifference(image1, image2);
		contrast.setImage(image3);
	}
	
	private static BufferedImage getDifference(BufferedImage bi1,BufferedImage bi2) {
		if(bi1==null || bi2==null) return null;
		int w = Math.max(bi1.getWidth(), bi2.getWidth());
		int h = Math.max(bi1.getHeight(), bi2.getHeight());
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		int[] raster1 = new int[w];
		for(int y = 0; y<h; y++) {
			for(int x = 0; x<w; x++) {
				int rgb1 = bi1.getRGB(x, y);
				int rgb2 = bi2.getRGB(x, y);
				if( pixelEquals(rgb1, rgb2) ) {
					raster1[x] = 0xff0000;
				} else {
					raster1[x] = 0xffffff;
				}
			}
			img.getRaster().setDataElements(0, y, w, 1, raster1);
		}
		return img;
	}
	

	public static void testBMPEncoding(List<File> failures) {
		FileTreeIterator i = getIterator();
		int ctr = 0;
		while(i.hasNext()) {
			
			//this sync lets other agents effectively pause this query
			synchronized(failures) {}
			
			File file = i.next();
			Dimension size = ImageSize.get(file);
			System.out.println(file.getAbsolutePath()+" "+size);
			if(size==null || size.width*size.height<2000*1000) {
				BufferedImage bi1 = null;
				BufferedImage bi2 = null;
				File tempFile = null;
				try {
					bi1 = BmpEncoder.read(file);
				} catch(Exception e) {
					//e.printStackTrace();
				} finally {
					if(tempFile!=null)
						tempFile.delete();
				}
					
				try {
					bi2 = ImageIO.read(file);
				} catch(Exception e) {
					//e.printStackTrace();
					if(bi1!=null) {
						System.out.println("ImageIO failed but GIRAI succeeded: "+file.getAbsolutePath());
					}
				}
					
				if(bi1==null && bi2!=null) {
					synchronized(failures) {
						if(contains(failures, file)==false)
							failures.add(file);
					}
				} else if(bi1!=null && bi2!=null && !equals(bi1,bi2)) {
					synchronized(failures) {
						if(contains(failures, file)==false)
							failures.add(file);
					}
				}
			}
			ctr++;
		}
		System.out.println("Finished "+ctr+" images");
	}
	
	private static boolean contains(List<File> list,File file) {
		for(int a = 0; a<list.size(); a++) {
			File t = list.get(a);
			try {
				if(IOUtils.equals(t, file))
					return true;
			} catch(IOException e) {
				RuntimeException e2 = new RuntimeException();
				e2.initCause(e);
				throw e2;
			}
		}
		return false;
	}
	
	public static boolean pixelEquals(int rgb1,int rgb2) {
		int a1 = (rgb1 >> 24) & 0xff;
		int r1 = (rgb1 >> 16) & 0xff;
		int g1 = (rgb1 >> 8) & 0xff;
		int b1 = (rgb1 >> 0) & 0xff;
		int a2 = (rgb2 >> 24) & 0xff;
		int r2 = (rgb2 >> 16) & 0xff;
		int g2 = (rgb2 >> 8) & 0xff;
		int b2 = (rgb2 >> 0) & 0xff;
		r1 = r1*a1/255;
		g1 = g1*a1/255;
		b1 = b1*a1/255;
		r2 = r2*a2/255;
		g2 = g2*a2/255;
		b2 = b2*a2/255;
		
		a1 = a1-a2;
		r1 = r1-r2;
		g1 = g1-g2;
		b1 = b1-b2;
		if(a1<0) a1 = -a1;
		if(r1<0) r1 = -r1;
		if(g1<0) g1 = -g1;
		if(b1<0) b1 = -b1;
		
		if(a1>5 || r1>5 || g1>5 || b1>5)
			return false;
		return true;
	}
	
	private static boolean equals(BufferedImage bi1,BufferedImage bi2) {
		if(bi1.getWidth()!=bi2.getWidth())
			return false;
		if(bi1.getHeight()!=bi2.getHeight())
			return false;
		for(int y = 0; y<bi1.getHeight(); y+=10) {
			for(int x = 0; x<bi1.getWidth(); x+=10) {
				int rgb1 = bi1.getRGB(x, y);
				int rgb2 = bi2.getRGB(x, y);
				if(!pixelEquals(rgb1, rgb2)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static FileTreeIterator getIterator() {
		File root = new File(System.getProperty("user.home"));
		String[] suffixes = new String[] { "gif", "jpeg", "png", "jpg"};
		FileTreeIterator i = new FileTreeIterator(root, suffixes);
		return i;
	}
}
