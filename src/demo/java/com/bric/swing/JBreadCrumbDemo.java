/*
 * @(#)JBreadCrumbDemo.java
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.LabelUI;

import com.bric.blog.Blurb;
import com.bric.plaf.BreadCrumbUI;
import com.bric.swing.JBreadCrumb.BreadCrumbFormatter;

/** A simple demo program showing off the {@link com.bric.swing.JBreadCrumb}. 
 *
 **/
@Blurb (
filename = "JBreadCrumbDemo",
title = "Navigation: Bread Crumbs",
releaseDate = "January 2014",
summary = "A Swing implementation of collapsible bread crumbs.",
instructions = "This applet demonstrates the <code>JBreadCrumb</code> component.\n"+
		"<p>Move the mouse over the text to see it collapse and expand. Also (not shown "+
		"here) you can listen to and respond to mouse clicks.",
link = "http://javagraphics.blogspot.com/2014/01/navigation-breadcrumbs.html",
sandboxDemo = true
)
public class JBreadCrumbDemo extends BricApplet {
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
				f.getContentPane().add(new JBreadCrumbDemo());
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.pack();
				f.setVisible(true);
			}
		});
	}
	
	JBreadCrumb<String> crumbs1 = new JBreadCrumb<String>();
	JBreadCrumb<String> crumbs2 = new JBreadCrumb<String>();
	JPanel crumbs2Container = new JPanel(new GridBagLayout());

	
	/** Create a default JBreadCrumbDemo with generous insets, a small width (to demonstrate collapsing) and a simple file path. */
	public JBreadCrumbDemo() {
		this(15, true, "Macintosh HD", "Users", "Hercules", "Pictures", "Labour Selfies", "Cerberus");
	}
	
	/** Create a JBreadCrumbDemo
	 * 
	 * @param insets the insets to apply to the content area
	 * @param collapse if true the then the width of this panel is reduced so collapsing is necessary.
	 * @param strings a demo path of bread crumbs
	 */
	public JBreadCrumbDemo(int insets,boolean collapse,String... strings) {
		
		// set up crumbs1:
		crumbs1.setPath(strings);
		crumbs1.setFormatter(new BreadCrumbFormatter<String>() {

			public void format(JBreadCrumb<String> container, JLabel label,
					String pathNode, int index) {
				label.setText(pathNode);
				label.setIcon(UIManager.getIcon("Tree.openIcon"));
			}
			
		});
		crumbs1.setBorder(new EmptyBorder(insets,insets,insets,insets));
		crumbs1.setOpaque(true);
		crumbs1.setBackground(Color.white);
		if(collapse) {
			Dimension d = crumbs1.getPreferredSize();
			d.width -= 100;
			crumbs1.setPreferredSize(d);
		}
		

		// set up crumbs2:
		Icon lankySeparator = new Icon() {
			
			int separatorWidth = 5;
			int leftPadding = 3;
			int rightPadding = 5;

			public void paintIcon(Component c, Graphics g, int x, int y) {
				Graphics2D g2 = (Graphics2D)g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				int h = getIconHeight()-1;
				GeneralPath arrow = new GeneralPath();
				arrow.moveTo(x + leftPadding, y);
				arrow.lineTo(x + leftPadding + separatorWidth, y + h / 2);
				arrow.lineTo(x + leftPadding, y + h);
				g2.setStroke(new BasicStroke(2));
				g2.setColor(new Color(0,0,0,10));
				g2.draw(arrow);
				g2.setStroke(new BasicStroke(1));
				g2.setColor(new Color(0,0,0,40));
				g2.draw(arrow);
				g2.dispose();
			}

			public int getIconWidth() {
				return separatorWidth + leftPadding + rightPadding;
			}

			public int getIconHeight() {
				return 22;
			}
			
		};
		
		crumbs2.setUI(new BreadCrumbUI() {
			@Override
			public void paint(Graphics g, JComponent c) {
				Graphics2D g2 = (Graphics2D)g.create();
				GradientPaint paint = new GradientPaint(0,0, new Color(0xFFFFFF), 0, c.getHeight(), new Color(0xDDDDDD));
				g2.setPaint(paint);
				g2.fillRect(0,0,c.getWidth(),c.getHeight());
				super.paint(g2, c);
				g2.dispose();
			}
			
			@Override
			protected LabelUI getLabelUI() {
				return null; //new EmphasizedLabelUI();
			}
		});
		crumbs2.setPath(strings);
		crumbs2.setFormatter(new BreadCrumbFormatter<String>() {

			public void format(JBreadCrumb<String> container, JLabel label,
					String pathNode, int index) {
				label.setText(pathNode);
			}
			
		});
		crumbs2.putClientProperty(BreadCrumbUI.SEPARATOR_ICON_KEY, lankySeparator);
		crumbs2.setBorder(new EmptyBorder(0,5,0,0));
		crumbs2.setOpaque(true);
		crumbs2.setBackground(Color.white);
		if(collapse) {
			Dimension d = crumbs2.getPreferredSize();
			d.width -= 100;
			crumbs2.setPreferredSize(d);
		}
		crumbs2Container.setBorder(new CompoundBorder(new EmptyBorder(insets,insets,insets,insets), new LineBorder(Color.gray) ));
		crumbs2Container.setBackground(Color.white);
		

		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		crumbs2Container.add(crumbs2, c);
		getContentPane().add(crumbs1, c);
		c.gridy++;
		getContentPane().add(crumbs2Container, c);
	}
}
