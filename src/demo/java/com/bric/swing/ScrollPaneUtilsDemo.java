/*
 * @(#)ScrollPaneUtilsDemo.java
 *
 * $Date: 2015-02-28 21:59:45 +0100 (Sa, 28 Feb 2015) $
 *
 * Copyright (c) 2012 by Jeremy Wood.
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicScrollPaneUI;

import com.bric.blog.Blurb;
import com.bric.plaf.FilledButtonUI;
import com.bric.plaf.GradientButtonUI;

/** A demo app for {@link ScrollPaneUtils}.
 *
 */
@Blurb (
filename = "ScrollPaneUtils",
title = "Animating Scrolling",
releaseDate = "TBA",
summary = "It's great that a swipe gesture (or mouse scroll wheel) is now "+
	"used to fluidly scroll through a JScrollPane with some sense of inertia: "+
	"but what if you want to programmatically pan somewhere? This class implements "+
	"an animated pan with acceleration and deceleration.",
scrapped = "That's all it does.",
sandboxDemo = true
)
public class ScrollPaneUtilsDemo extends BricApplet {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		JFrame frame = new JFrame("ScrollPaneUtils Demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new ScrollPaneUtilsDemo());
		frame.pack();
		frame.setVisible(true);
	}
	
	static int CELL_WIDTH = 80;
	static int CELL_HEIGHT = 80;
	
	class Cell extends JPanel {
		private static final long serialVersionUID = 1L;
		JLabel label = new JLabel();
		Cell(int index) {
			Random random = new Random(index*1000);
			setBackground( createColor(random) );
			setOpaque( true );
			
			label.setText(Integer.toString(index+1));
			label.setHorizontalAlignment(SwingConstants.CENTER);
			
			add( label );
			setPreferredSize( new Dimension( CELL_WIDTH, CELL_HEIGHT ) );
			setBorder(new PartialLineBorder(Color.gray, new Insets(0,0,1,1)));
		}
		
		AbstractButton createButton() {
			JButton button = new JButton(label.getText());
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ScrollPaneUtils.animateScroll(scrollPane, Cell.this.getLocation(), 1);
				}
			});
			return button;
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
		}
	}
	
	JPanel contents = new JPanel(new GridBagLayout());
	JPanel buttonGrid = new JPanel(new GridBagLayout());
	JScrollPane scrollPane = new JScrollPane( contents, 
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
			JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
	Cell[][] cells = new Cell[10][10];
	AbstractButton[][] buttons = new AbstractButton[cells.length][cells[0].length];

	public ScrollPaneUtilsDemo() {
		contents.setBackground(Color.white);
		contents.setOpaque(true);
		
		int ctr = 0;
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		for(int y = 0; y<cells.length; y++) {
			for(int x = 0; x<cells[y].length; x++) {
				cells[y][x] = new Cell(ctr);
				buttons[y][x] = cells[y][x].createButton();
				ctr++;
				c.gridx = x; c.gridy = y;
				contents.add(cells[y][x], c);
				buttonGrid.add(buttons[y][x], c);
				
				String hPos = FilledButtonUI.MIDDLE;
				if(x==0) hPos = FilledButtonUI.LEFT;
				if(x==9) hPos = FilledButtonUI.RIGHT;

				String vPos = FilledButtonUI.MIDDLE;
				if(y==0) vPos = FilledButtonUI.TOP;
				if(y==9) vPos = FilledButtonUI.BOTTOM;
				
				buttons[y][x].putClientProperty( FilledButtonUI.HORIZONTAL_POSITION, hPos);
				buttons[y][x].putClientProperty( FilledButtonUI.VERTICAL_POSITION, vPos);
				buttons[y][x].setUI(new GradientButtonUI());
				buttons[y][x].setForeground(Color.darkGray);
			}
		}
		
		getContentPane().setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		getContentPane().add(scrollPane, c);
		c.gridy++;
		getContentPane().add(buttonGrid, c);
		
		scrollPane.setUI(new BasicScrollPaneUI());
		
		scrollPane.setPreferredSize(new Dimension(CELL_WIDTH*2, CELL_WIDTH*2));
	}
	
	private static Color createColor(Random r) {
		float[] hsb = new float[] { r.nextFloat(), r.nextFloat()*.1f + .20f, .95f };
		return new Color( Color.HSBtoRGB( hsb[0], hsb[1], hsb[2]) );
	}

}
