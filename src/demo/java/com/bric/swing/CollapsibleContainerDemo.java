/*
 * @(#)CollapsibleContainerDemo.java
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
package com.bric.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.bric.blog.Blurb;
import com.bric.swing.SectionContainer.Section;

/** A demo app for the <code>CollapsibleContainer</code> class.
 * <p>The {@link CollapsibleContainerDemoHelper} creates this animation
 * showing off this demo app:
 * <p><img src="https://javagraphics.java.net/resources/collapsiblecontainer.gif" style="border:1px solid gray" alt="CollapsibleContainer Animation">
 * 
 */
@Blurb (
filename = "CollapsibleContainerDemo",
title = "Panels: Collapsible Sections",
releaseDate = "March 2014",
summary = "A Swing implementation of a vertical series of collapsible panels. "+
"Panel heights are determined with vertical weights, and as the user toggles the "+
"visibility of panels: the UI is animated.",
instructions = "This applet demonstrates the <code>CollapsibleContainer</code>. "+
"Click each header to toggle its visibility, and observe how the UI animates to "+
"comply with your instruction.",
link = "http://javagraphics.blogspot.com/2014/03/panels-collapsible-sections.html",
sandboxDemo = true
)
public class CollapsibleContainerDemo extends BricApplet {
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
				f.getContentPane().add(new CollapsibleContainerDemo(1));
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.pack();
				f.setVisible(true);
			}
		});
	}
	
	class PopupListener extends MouseAdapter {
		JButton header;
		PopupListener(JButton header) {
			this.header = header;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			mouseClicked(e);
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.isPopupTrigger()) {
				e.consume();
				final int x = e.getX();
				final int y = e.getY();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JPopupMenu popup = new JPopupMenu();
						Boolean collapsible = (Boolean)header.getClientProperty(CollapsibleContainer.COLLAPSIBLE);
						if(collapsible==null) collapsible = Boolean.TRUE;
						JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Collapsible", collapsible);
						menuItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								Boolean collapsible = (Boolean)header.getClientProperty(CollapsibleContainer.COLLAPSIBLE);
								if(collapsible==null) collapsible = Boolean.TRUE;
								header.putClientProperty(CollapsibleContainer.COLLAPSIBLE, !collapsible);
							}
						});
						popup.add(menuItem);
						popup.show(header, x, y);
					}
				});
			}
		}
	}
	protected CollapsibleContainer container;
	protected Section section1, section2, section3;
	private TexturePaint stripes;

	public CollapsibleContainerDemo() {
		this(1);
	}
	
	/**
	 * @param slowdownFactor the factor animation is slowed down by.
	 * This is intended for debugging or demonstrations.
	 */
	public CollapsibleContainerDemo(int slowdownFactor) {
		container = new CollapsibleContainer(slowdownFactor) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g0) {
				super.paintComponent(g0);
				Graphics2D g = (Graphics2D)g0;
				g.setPaint(stripes);
				g.fillRect(0,0,getWidth(), getHeight());
			}
		};
		BufferedImage stripeImage = createStripeImage();
		stripes = new TexturePaint(stripeImage, new Rectangle(0,0,stripeImage.getWidth(),stripeImage.getHeight()));
		section1 = container.addSection("section1", "Section 1");
		install(section1.getBody(), new JLabel("... this is a label with no vertical weight."));
		section2 = container.addSection("section2", "Section 2");
		JTextPane text2 = new JTextPane();
		text2.setText("This section is given a weight of 1, so by default it should occupy 1/3 of the free space.");
		install(section2.getBody(), new JScrollPane(text2));
		section2.setProperty(CollapsibleContainer.VERTICAL_WEIGHT, 1);
		section3 = container.addSection("section3", "Section 3");
		JTextPane text3 = new JTextPane();
		text3.setText("This section is given a weight of 2, so by default it should occupy 2/3's of the free space.");
		install(section3.getBody(), new JScrollPane(text3));
		section3.setProperty(CollapsibleContainer.VERTICAL_WEIGHT, 2);
		container.getHeader(section1).addMouseListener(new PopupListener(container.getHeader(section1)));
		container.getHeader(section2).addMouseListener(new PopupListener(container.getHeader(section2)));
		container.getHeader(section3).addMouseListener(new PopupListener(container.getHeader(section3)));
		
		getContentPane().add(container);
		setPreferredSize(new Dimension(300,400));
	}
	
	private void install(JPanel container,JComponent comp) {
		container.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		if(comp instanceof JScrollPane) {
			comp.setBorder(new EmptyBorder(2,2,2,2));
		} else {
			c.insets = new Insets(4,4,4,4);
		}
		container.add(comp, c);
	}

	private BufferedImage createStripeImage() {
		BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		g.setColor(new Color(0x999999));
		g.fillRect(0,0,bi.getWidth(), bi.getHeight());
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.setColor(new Color(0x808080));
		g.setStroke(new BasicStroke(2));
		g.translate(-4, -4);
		for(int a = 0; a<=48; a+=8) {
			g.drawLine(a, 0, 0, a);
		}
		g.dispose();
		return bi;
	}
}
