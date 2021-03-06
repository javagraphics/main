/*
 * @(#)QuantizationDemo.java
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
package com.bric.image.pixel.quantize;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;

import com.bric.blog.Blurb;
import com.bric.blog.ResourceSample;
import com.bric.io.SuffixFilenameFilter;
import com.bric.swing.BricApplet;


/** A simple demo program that applies color and image quantization to
 * an image. A slider lets you configure the maximum number of colors
 * (up to 256), and a couple of other controls let you configure
 * the quantization approach used.
 *
 *
 * 
 * <!-- ======== START OF AUTOGENERATED SAMPLES ======== -->
 * <p><img src="https://javagraphics.java.net/resources/samples/QuantizationDemo/sample.png" alt="new&#160;com.bric.image.pixel.quantize.QuantizationDemo(&#160;)">
 * <!-- ======== END OF AUTOGENERATED SAMPLES ======== -->
 */
@Blurb (
filename = "QuantizationDemo",
title = "Images: Color Palette Reduction",
releaseDate = "February 2014",
summary = "This is a crash course in a pure-Java implementation of color reduction for images "+
"(aka \"image quantization\"). This provides a median cut color reduction algorithm and a few different "+
"levels of error diffusion to help reduce images from millions of colors to a few hundred.\n"+
"<p>(This is a prerequisite for writing a gif encoder.)",
instructions = "This applet demonstrates a color quantization implementation.\n"+
"<p>The image is displayed on the left, and the color palette it was reduced to is displayed on the right. Use "+
"the controls at the top of the panel to alter the color quantization.\n"+
"<p>If launched as an executable: you have the option of importing an image file to work with, but as an applet "+
"you can only work with the predefined rainbow-ish image.",
link = "http://javagraphics.blogspot.com/2014/02/images-color-palette-reduction.html",
sandboxDemo = true
)
@ResourceSample( sample="new com.bric.image.pixel.quantize.QuantizationDemo( )" )
public class QuantizationDemo extends BricApplet {
	private static final long serialVersionUID = 1L;

	/** Launch the QuantizationDemo app. 
     * @param args the application's arguments. (This is unused.)
     */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					String lf = UIManager.getSystemLookAndFeelClassName();
					UIManager.setLookAndFeel(lf);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				
				JFrame f = new JFrame("Quantization Demo");
				f.getContentPane().add(new QuantizationDemo(false));
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.pack();
				f.setVisible(true);
			}
		});
	}

	/** A map of color cell bounds to the color they represent. */
	Map<Rectangle, Color> cells = new Hashtable<Rectangle, Color>();
	
	/** The upper panel of controls. */
	JPanel controls = new JPanel(new GridBagLayout());
	
	/** The panel that shows the current reduced image. */
	JPanel contentPanel = new JPanel() {
		private static final long serialVersionUID = 1L;

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if(reducedImage!=null) {
				Graphics2D g2 = (Graphics2D)g.create();
				g2.drawImage(reducedImage, 0, 0, null);
				g2.dispose();
			}
		}
	};
	
	JScrollPane contentScrollPane = new JScrollPane(contentPanel);
	
	/** The size of each color cell. */
	int CELL_SIZE = 16;
	
	JLabel fileLabel = new JLabel("File:");
	JTextField filePath = new JTextField();
	JButton browseButton = new JButton("Browse");
	JLabel colorCountLabel = new JLabel("Color Count:");
	
	/** The panel on the right that shows the current color palette. */
	JPanel pixelPanel = new JPanel() {
		private static final long serialVersionUID = 1L;

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			for(Rectangle r : cells.keySet()) {
				Color c = cells.get(r);
				g.setColor(c);
				g.fillRect(r.x, r.y, r.width, r.height);
			}
		}
	};
	JSlider reductionSlider = new JSlider(2, 256);
	BufferedImage originalImage = null;
	BufferedImage reducedImage = null;
	ColorSet originalImageColors = null;
	ColorSet reducedImageColors = null;
	JComboBox<String> reductionType = new JComboBox<String>();
	JComboBox<ImageQuantization> quantizationType = new JComboBox<ImageQuantization>();
	JLabel quantizationLabel = new JLabel("Quantization:");


	/** Create a demo that is registered as an applet. */
	public QuantizationDemo() {
		this(true);
	}
	
	/**
	 * 
	 * @param isApplet if true then the option to browse for a file is removed.
	 */
	public QuantizationDemo(boolean isApplet) {
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0;
		c.fill = GridBagConstraints.BOTH; c.gridwidth = GridBagConstraints.REMAINDER;
		getContentPane().add(controls, c);
		c.gridy++;
		c.weighty = 1; c.gridwidth = 1;
		getContentPane().add(contentScrollPane, c);
		c.gridx++; c.weightx = 0;
		getContentPane().add(pixelPanel, c);
		
		c = new GridBagConstraints();
		c.insets = new Insets(3,3,3,3);
		c.anchor = GridBagConstraints.EAST;
		c.gridx = 0; c.gridy = 0; c.weightx = 0; c.weighty = 0;
		controls.add( fileLabel, c);
		c.anchor = GridBagConstraints.WEST;
		c.gridx++; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 10;
		controls.add(filePath, c);
		c.gridx+=c.gridwidth; c.weightx = 0; c.fill = GridBagConstraints.NONE; c.gridwidth = 1;
		controls.add(browseButton, c);
		
		fileLabel.setVisible(!isApplet);
		filePath.setVisible(!isApplet);
		browseButton.setVisible(!isApplet);
		
		c.gridy++; c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		controls.add(colorCountLabel, c);
		c.gridx++;
		c.anchor = GridBagConstraints.WEST;
		controls.add(reductionSlider, c);
		c.gridx++;
		controls.add(reductionType, c);

		c.gridy++; c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		controls.add(quantizationLabel, c);
		c.gridx++;
		c.anchor = GridBagConstraints.WEST;
		controls.add(quantizationType, c);
		
		reductionType.addItem("Biased");
		reductionType.addItem("Median Cut (Simplest)");
		
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File f = browseForFile("jpg", "jpeg", "png");
				if(f==null) return;
				setFile(f);
			}
		});
		filePath.getDocument().addDocumentListener(new DocumentListener() {

			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			public void changedUpdate(DocumentEvent e) {
				File f = new File(filePath.getText());
				if(f.exists()) {
					setFile(f);
				}
			}
			
		});
		
		reductionSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateReducedColorList();
			}
		});
		Dimension d = new Dimension(400, 400);
		contentScrollPane.setPreferredSize(d);
		pixelPanel.setPreferredSize(new Dimension(8*CELL_SIZE, 32*CELL_SIZE));
		pixelPanel.setMinimumSize(new Dimension(8*CELL_SIZE, 32*CELL_SIZE));
		
		pixelPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateCells();
			}
		});

		quantizationType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateReducedImage();
			}
		});
		
		reductionType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateReducedColorList();
			}
		});
		
		pixelPanel.addMouseMotionListener(new MouseInputAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();
				String s = null;
				for(Rectangle r : cells.keySet()) {
					if(r.contains(p)) {
						Color c = cells.get(r);
						s = c.getRed()+", "+c.getGreen()+", "+c.getBlue();
					}
				}
				pixelPanel.setToolTipText(s);
			}
		});
		
		quantizationType.addItem( ImageQuantization.MOST_DIFFUSION );
		quantizationType.addItem( ImageQuantization.MEDIUM_DIFFUSION );
		quantizationType.addItem( ImageQuantization.SIMPLEST_DIFFUSION );
		quantizationType.addItem( ImageQuantization.NEAREST_NEIGHBOR );
		
		if(isApplet) {
			setFile(null);
		}
	}
	
	/** If invoked from within a Frame: this pulls up a FileDialog to
	 * browse for a file. If this is invoked from a secure applet: then
	 * this will throw an exception.
	 * 
	 * @param ext an optional list of extensions
	 * @return a File, or null if the user declined to select anything.
	 */
	public File browseForFile(String... ext) {
		Window w = SwingUtilities.getWindowAncestor(this);
		if(!(w instanceof Frame))
			throw new IllegalStateException();
		Frame frame = (Frame)w;
		FileDialog fd = new FileDialog(frame);
		if(ext!=null && ext.length>0 && (!(ext.length==1 && ext[0]==null)))
			fd.setFilenameFilter(new SuffixFilenameFilter(ext));
		fd.pack();
		fd.setLocationRelativeTo(null);
		fd.setVisible(true);
		String d = fd.getFile();
		if(d==null) return null;
		return new File(fd.getDirectory()+fd.getFile());
	}

	private boolean dirtyColorList = false;
	private void updateReducedColorList() {
		dirtyColorList = true;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				doUpdateReducedColorList();
			}
		});
	}
	
	private void doUpdateReducedColorList() {
		if(dirtyColorList==false)
			return;
		dirtyColorList = false;
		reducedImageColors = null;
		if(originalImageColors!=null) {
			ColorQuantization cq = null;
			if(reductionType.getSelectedIndex()==0) {
				cq = new BiasedMedianCutColorQuantization(.1f);
			} else {
				cq = new MedianCutColorQuantization();
			}
			reducedImageColors = cq.createReducedSet( originalImageColors, reductionSlider.getValue(), true );
		}
		contentScrollPane.repaint();

		updateReducedImage();
		updateCells();
	}
	
	private void updateReducedImage() {
		reducedImage = null;
		contentPanel.repaint();
		if(reducedImageColors==null)
			return;
		
		IndexColorModel icm = reducedImageColors.createIndexColorModel(false, false);
		ColorLUT lut = new ColorLUT(icm);
		reducedImage = ((ImageQuantization)quantizationType.getSelectedItem()).createImage(originalImage, lut);
		contentPanel.setPreferredSize( new Dimension(reducedImage.getWidth(), reducedImage.getHeight()) );
		
	}
	
	private void updateCells() {
		int x = 0;
		int y = 0;
		cells.clear();
		if(reducedImageColors!=null) {
			Color[] color = reducedImageColors.getColors();

			/** This is a crude hard-to-explain approach to sorting
			 * colors into clusters that humans find reasonably appealing.
			 * (That is: without a 3D representation it's hard to
			 * express a series of 3D-data points in 2D space.)
			 */
			
			Comparator<Color> hueComparator = new Comparator<Color>() {

				public int compare(Color c1, Color c2) {
					float[] hsb1 = new float[3];
					float[] hsb2 = new float[3];
					Color.RGBtoHSB(c1.getRed(), c1.getGreen(), c1.getBlue(), hsb1);
					Color.RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getBlue(), hsb2);
					if(hsb1[0]<hsb2[0]) {
						return -1;
					} else if(hsb1[0]>hsb2[0]) {
						return 1;
					} else if(hsb1[1]<hsb2[1]) {
						return -1;
					} else if(hsb1[1]>hsb2[1]) {
						return 1;
					} else if(hsb1[2]<hsb2[2]) {
						return -1;
					} else if(hsb1[2]>hsb2[2]) {
						return 1;
					}
					return 0;
				}
				
			};
			Comparator<Color> brightnessComparator = new Comparator<Color>() {

				public int compare(Color c1, Color c2) {
					float[] hsb1 = new float[3];
					float[] hsb2 = new float[3];
					Color.RGBtoHSB(c1.getRed(), c1.getGreen(), c1.getBlue(), hsb1);
					Color.RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getBlue(), hsb2);
					if(hsb1[2]<hsb2[2]) {
						return -1;
					} else if(hsb1[2]>hsb2[2]) {
						return 1;
					} else if(hsb1[0]<hsb2[0]) {
						return -1;
					} else if(hsb1[0]>hsb2[0]) {
						return 1;
					} else if(hsb1[1]<hsb2[1]) {
						return -1;
					} else if(hsb1[1]>hsb2[1]) {
						return 1;
					}
					return 0;
				}
				
			};
			
			Set<Color> tier1 = new TreeSet<Color>(hueComparator);
			Set<Color> tier2 = new TreeSet<Color>(hueComparator);
			Set<Color> tier3 = new TreeSet<Color>(brightnessComparator);

			for(int a = 0; a<color.length; a++) {
				float[] hsb = new float[3];
				Color.RGBtoHSB(color[a].getRed(), color[a].getGreen(), color[a].getBlue(), hsb);
				float k = hsb[1] + hsb[2];
				if(k>1.2) {
					tier1.add(color[a]);
				} else if(k>.7) {
					tier2.add(color[a]);
				} else {
					tier3.add(color[a]);
				}
			}
			
			for(int a = 0; a<3; a++) {
				Set<Color> set = null;
				if(a==0) {
					set = tier1;
				} else if(a==1) {
					set = tier2;
				} else if(a==2) {
					set = tier3;
				}
				Color[] array = set.toArray(new Color[set.size()]);
				for(int b = 0; b<array.length; b++) {
					cells.put(new Rectangle(x, y, CELL_SIZE, CELL_SIZE), array[b]);
					
					x += CELL_SIZE;
					if(x>=pixelPanel.getWidth()) {
						x = 0;
						y += CELL_SIZE;
					}
				}
			}
		}
		pixelPanel.repaint();
	}
	
	protected void setFile(File f) {
		originalImage = null;
		originalImageColors = null;
		if(f==null) {
			originalImage = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
			int[] pixels = new int[originalImage.getWidth()];
			for(int y = 0; y<originalImage.getHeight(); y++) {
				for(int x = 0; x<pixels.length; x++) {
					float hue = ((float)x)/((float)pixels.length);
					float saturation = ((float)y)/((float)originalImage.getHeight());
					int rgb = Color.HSBtoRGB(hue, saturation,	1);
					pixels[x] = rgb;
				}
				originalImage.getRaster().setDataElements(0, y, pixels.length, 1, pixels);
			}
			updateColorList();
		} else {
			String s = f.getAbsolutePath();
			if(!filePath.getText().equals(s)) {
				filePath.setText(s);
			}
			try {
				originalImage = ImageIO.read(f);
				updateColorList();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		getContentPane().repaint();
	}
	
	private void updateColorList() {
		originalImageColors = new ColorSet();
		originalImageColors.addColors(originalImage);
		updateReducedColorList();
	}
}
