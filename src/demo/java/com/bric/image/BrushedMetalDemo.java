/*
 * @(#)BrushedMetalDemo.java
 *
 * $Date: 2015-03-01 08:01:26 +0100 (So, 01 Mär 2015) $
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.bric.blog.Blurb;
import com.bric.blog.ResourceSample;
import com.bric.swing.BricApplet;

/**
 *
 * 
 * <!-- ======== START OF AUTOGENERATED SAMPLES ======== -->
 * <p><img src="https://javagraphics.java.net/resources/samples/BrushedMetalDemo/sample.png" alt="new&#160;com.bric.image.BrushedMetalDemo(&#160;)">
 * <!-- ======== END OF AUTOGENERATED SAMPLES ======== -->
 */
@Blurb (
filename = "BrushedMetal",
title = "Brushed Metal",
releaseDate = "TBA",
summary = "The <a href=\"https://javagraphics.java.net/doc/com/bric/image/BrushedMetalLook.html\">BrushedMetalLook</a> class provides a few static methods to create BufferedImages "
		+ "that resemble brushed metal.",
scrapped = "This is an increasingly rare/obsolete look.",
sandboxDemo = true
)
@ResourceSample( sample="new com.bric.image.BrushedMetalDemo( )" )
public class BrushedMetalDemo extends BricApplet {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					String lf = UIManager.getSystemLookAndFeelClassName();
					UIManager.setLookAndFeel(lf);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				
				JFrame f = new JFrame();
				f.getContentPane().add(new BrushedMetalDemo());
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.pack();
				f.setVisible(true);
			}
		});
	}
	
	JLabel label = new JLabel("Color:");
	JTextField hexColorField = new JTextField(12);
	JTabbedPane tabs = new JTabbedPane();
	TexturePaint texturePaint;
	JComponent tilingComponent = new JComponent() {
		private static final long serialVersionUID = 1L;

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			if(texturePaint!=null) {
				Graphics2D g2 = (Graphics2D)g;
				g2.setPaint(texturePaint);
				g2.fillRect(0, 0, getWidth(), getHeight());
			}
		}
	};
	JLabel label1 = new JLabel();
	JLabel label2 = new JLabel();
	JLabel label3 = new JLabel();
	
	BrushedMetalDemo() {
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 0;
		c.insets = new Insets(3,3,3,3);
		c.anchor = GridBagConstraints.EAST;
		getContentPane().add(label, c);
		c.gridx++;
		c.anchor = GridBagConstraints.WEST;
		getContentPane().add(hexColorField, c);
		c.gridy++; c.gridwidth = GridBagConstraints.REMAINDER; c.gridx = 0;
		c.weighty = 1;
		c.anchor = GridBagConstraints.CENTER;
		getContentPane().add(tabs, c);
		
		hexColorField.getDocument().addDocumentListener(new DocumentListener() {

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
				String s = hexColorField.getText();
				int i = s.indexOf('x');
				s = s.substring(i+1);
				while(s.length()<6) {
					s = s+"0";
				}
				try {
					int rgb = Integer.parseInt(s, 16);
					setMetalColor(new Color(rgb));
					hexColorField.setForeground(Color.black);
				} catch(RuntimeException e2) {
					hexColorField.setForeground(Color.red);
				}
			}
			
		});
		hexColorField.setText("0x888899");
		
		tabs.add(tilingComponent, "Tiled Pattern");
		tabs.add(label1, "Ellipse");
		tabs.add(label2, "Line 1");
		tabs.add(label3, "Line 2");
	}
	
	protected void setMetalColor(Color c) {
		BufferedImage image = BrushedMetalLook.getImage(c);
		texturePaint = new TexturePaint(image, new Rectangle(0,0,image.getWidth(),image.getHeight()));
		
		Shape shape = new Ellipse2D.Float(100,100,400,400);
		image = BrushedMetalLook.paint(shape, 20, null, c, true);
		Icon icon1 = new ImageIcon(image);
		label1.setIcon(icon1);
		
		shape = new Line2D.Float(100,100,500,500);
		image = BrushedMetalLook.paint(shape, 20, null, c, true);
		ImageIcon icon2 = new ImageIcon(image);
		label2.setIcon(icon2);

		shape = new Line2D.Float(100,500,500,100);
		image = BrushedMetalLook.paint(shape, 20, null, c, true);
		ImageIcon icon3 = new ImageIcon(image);
		label3.setIcon(icon3);
		
		tabs.repaint();
	}
}
