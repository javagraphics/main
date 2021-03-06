/*
 * @(#)JFancyBox.java
 *
 * $Date: 2014-09-23 10:46:26 +0200 (Di, 23 Sep 2014) $
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;

import com.bric.awt.TextSize;
import com.bric.blog.ResourceSample;
import com.bric.math.MathG;

/** This component is a distant cousin of a modal dialog: it floats above
 * a RootPaneContainer with a dismissable close decoration.
 * 
 * 
 * 
 * <!-- ======== START OF AUTOGENERATED SAMPLES ======== -->
 * <p><img src="https://javagraphics.java.net/resources/samples/JFancyBox/sample.png" alt="new&#160;com.bric.swing.JFancyBox(&#160;new&#160;javax.swing.JInternalFrame(),&#160;&#34;Sample&#160;JFancyBox&#160;containing&#160;text&#34;)">
 * <!-- ======== END OF AUTOGENERATED SAMPLES ======== -->
 * 
 * @see <a href="http://fancybox.net/">fancybox.net</a>
 */
@ResourceSample( sample={ "new com.bric.swing.JFancyBox( new javax.swing.JInternalFrame(), \"Sample JFancyBox containing text\")" } )
public class JFancyBox extends JComponent {
	private static final long serialVersionUID = 1L;
	
	/** A close icon with a border and a light shadow.
	 * This is meant to be displayed at a larger size than its cousin the {@link com.bric.swing.resources.CloseIcon}.
	 */
	public static class FancyCloseIcon implements Icon {
		
		int xSize = 24;
		int shadowInset = 4;
		Color xColor = Color.white;
		Color borderColor = Color.white;
		Color backgroundColor = Color.black;
		float scaleFactor = 1;
		
		public FancyCloseIcon() {}
		
		/** Scale this FancyCloseIcon to a fixed size. */
		public FancyCloseIcon(int size) {
			int defaultSize = xSize+shadowInset*2;
			scaleFactor = ((float)size)/((float)defaultSize);
		}

		public int getIconHeight() {
			int k = xSize+shadowInset*2;
			if(scaleFactor==1)
				return k;
			return MathG.ceilInt(scaleFactor*k);
		}

		public int getIconWidth() {
			int k = xSize+shadowInset*2;
			if(scaleFactor==1)
				return k;
			return MathG.ceilInt(scaleFactor*k);
		}
		
		public void setXColor(Color c) {
			if(c==null) throw new NullPointerException();
			xColor = c;
		}
		
		public void setBorderColor(Color c) {
			if(c==null) throw new NullPointerException();
			borderColor = c;
		}
		
		public void setBackgroundColor(Color c) {
			if(c==null) throw new NullPointerException();
			backgroundColor = c;
		}

		public void paintIcon(Component c, Graphics g0, int x, int y) {
			Graphics2D g = (Graphics2D)g0.create();
			g.translate(x, y);
			g.scale(scaleFactor, scaleFactor);
			g.translate(shadowInset, shadowInset);
			
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			g.setColor(new Color(0,0,0,20));
			Ellipse2D e = new Ellipse2D.Float(2, 2, 20, 20);
			AffineTransform tx = g.getTransform();
			for(int a = 0; a<shadowInset; a++) {
				g.setStroke(new BasicStroke(2f+1.5f*a));
				g.translate(0, .5f);
				g.draw(e);
			}
			g.setTransform(tx);
			g.setColor(backgroundColor);
			g.fill(e);
			g.setColor(borderColor);
			g.setStroke(new BasicStroke(1.9f));
			g.draw(e);
			g.setStroke(new BasicStroke(2.5f));
			int k = 9;
			g.setColor(xColor);
			g.drawLine(k, k, 24-k, 24-k);
			g.drawLine(24-k, k, k, 24-k);
			g.dispose();
		}
	}
	
	/** The default JLayeredPane layer to put a box in. */
	protected static int DEFAULT_LAYER = JLayeredPane.PALETTE_LAYER;
	
	class MyLayoutManager implements LayoutManager {

		public void addLayoutComponent(String constraint, Component comp) {}

		public void layoutContainer(Container c) {
			if(c!=JFancyBox.this)
				throw new IllegalArgumentException();
			
			background.setBounds(0,0,c.getWidth(),c.getHeight());
			
			Dimension contentSize = contentContainer.getPreferredSize();
			if(contentSize.width<300) {
				contentSize.width = 300;
			}
			if(contentSize.height<300) {
				contentSize.height = 300;
			}
			if(contentSize.width > c.getWidth()-30) {
				contentSize.width = c.getWidth() - 30;
			}
			if(content instanceof JTextArea) {
				contentSize = TextSize.getPreferredSize( (JTextArea)content, contentSize.width);
				contentSize.height += 50;
			} else if(contentSize.height > c.getHeight()-30) {
				contentSize.height = c.getHeight() - 30;
			}
			contentContainer.setBounds( getWidth()/2-contentSize.width/2, 
					getHeight()/2-contentSize.height/2,
					contentSize.width, contentSize.height);
			Dimension closeButtonSize = closeButton.getPreferredSize();
			closeButton.setBounds( getWidth()/2+contentSize.width/2 - closeButtonSize.width/2,
					getHeight()/2-contentSize.height/2 - closeButtonSize.height/2,
					closeButtonSize.width,
					closeButtonSize.height);
		}

		public Dimension minimumLayoutSize(Container c) {
			if(c!=JFancyBox.this)
				throw new IllegalArgumentException();
			return new Dimension(600, 600);
		}

		public Dimension preferredLayoutSize(Container c) {
			return new Dimension(600, 600);
		}

		public void removeLayoutComponent(Component comp) {}
		
	}
	
	class Background extends BlockingPane {
		private static final long serialVersionUID = 1L;
		
		@Override
		public void mouseClicked(MouseEvent e) {
			close();
			super.mouseClicked(e);
		}

		@Override
		protected void paintComponent(Graphics g0) {
			Graphics2D g = (Graphics2D)g0;
			super.paintComponent(g);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			g.setColor(new Color(0,0,0,60));
			g.fillRect(0,0,getWidth(),getHeight());

			g.setColor(new Color(0,0,0,2));
			Rectangle r = contentContainer.getBounds();
			for(int a = 0; a<20; a++) {
				g.setStroke(new BasicStroke(a+1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g.drawRect( r.x, r.y+a/4, r.width, r.height );
			}
		}
	}
	
	RootPaneContainer rpc;
	JComponent content;
	JComponent contentContainer = new JPanel(new GridBagLayout());
	JComponent background = new Background();
	JButton closeButton = new JButton(new FancyCloseIcon());
	
	protected static JComponent createContent(String text) {
		JTextArea textArea = new JTextArea(text);
		Dimension d = TextSize.getPreferredSize(textArea, 500);
		textArea.setPreferredSize(d);
		textArea.setEditable(false);
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setOpaque(false);
		return textArea;
	}

	public JFancyBox(RootPaneContainer parent,String text,int layer) {
		this(parent, createContent(text), layer);
	}

	public JFancyBox(RootPaneContainer parent,String text) {
		this(parent, createContent(text), DEFAULT_LAYER);
	}

	public JFancyBox(RootPaneContainer parent,JComponent content) {
		this(parent, content, DEFAULT_LAYER);
	}
	
	public JFancyBox(RootPaneContainer parent,JComponent content,int layer) {
		rpc = parent;
		this.content = content;

		rpc.getLayeredPane().setLayer(this, layer);
		rpc.getLayeredPane().add(this);
		
		closeButton.setBorderPainted(false);
		closeButton.setContentAreaFilled(false);

		add(closeButton);
		add(contentContainer);
		add(background);
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		setLayout(new MyLayoutManager());
		rpc.getLayeredPane().addComponentListener(new ComponentListener() {

			public void componentHidden(ComponentEvent e) {}

			public void componentMoved(ComponentEvent e) {
				updateBounds();
			}

			public void componentResized(ComponentEvent e) {
				updateBounds();
			}

			public void componentShown(ComponentEvent arg0) {}
			
		});
		updateBounds();
		setVisible(false);

		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");		
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "close");
		getActionMap().put("close", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				if(isShowing()) {
					close();
				}
			}
		});
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		c.insets = new Insets(12, 12, 12, 12);
		c.fill = GridBagConstraints.BOTH;
		contentContainer.add(content, c);
		contentContainer.setOpaque(true);
		contentContainer.setBackground(Color.white);
	}
	
	private void updateBounds() {
		setBounds(0, 0, rpc.getLayeredPane().getWidth(), rpc.getLayeredPane().getHeight());
	}
	
	protected void close() {
		setVisible(false);
	}
}
