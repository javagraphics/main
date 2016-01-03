/*
 * @(#)TextEffectDemo.java
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
package com.bric.awt.text;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.bric.awt.text.writing.WritingFont;
import com.bric.blog.Blurb;
import com.bric.swing.BricApplet;

/** An applet demonstrating a few simple text effects.
 * 
 */
@Blurb (
filename = "TextEffect",
title = "Text: Effects and Block Shadows",
releaseDate = "February 2011",
summary = "Create some fun text effects using block shadows.",
instructions = "This applet demonstrates a few simple <code>TextEffects</code>.\n"+
"<p>Type text in the text field and click one of the buttons to see an animation.",
link = "http://javagraphics.blogspot.com/2011/02/text-effects-and-block-shadows.html",
sandboxDemo = true
)
public class TextEffectDemo extends BricApplet {
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame();
				f.getContentPane().add(new TextEffectDemo());
				f.pack();
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.setVisible(true);
			}
		});
	}
	
	class PreviewPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		PreviewPanel() {
			setPreferredSize(new Dimension(300,150));
		}
		
		@Override
		protected void paintComponent(Graphics g0) {
			super.paintComponent(g0);
			if(effect!=null) {
				Graphics2D g = (Graphics2D)g0;
				effect.paint(g, fraction);
			}
		}
	}
	
	JTextField textField = new JTextField("Type Text Here!");
	JButton outlineEffect = new JButton("Outline");
	JButton punchEffect = new JButton("Punch");
	JButton writeEffect = new JButton("Write");
	JButton waveEffect = new JButton("Wave");
	JButton explodeEffect = new JButton("Explode");
	
	TextEffect effect;
	float fraction;
	Timer timer = new Timer( 20, new ActionListener() {
		long startTime = -1;
		public void actionPerformed(ActionEvent e) {
			long t = System.currentTimeMillis();
			if(startTime==-1) {
				startTime = t;
				setEnabled(false);
				return;
			}
			long elapsed = t-startTime;
			long duration;
			if(effect instanceof PunchTextEffect || 
					effect instanceof ExplodeTextEffect || effect instanceof WriteTextEffect) {
				duration = textField.getText().length()*90;
			} else {
				duration = textField.getText().length()*50;
			}
			float fraction = ((float)elapsed)/((float)duration);
			if(fraction>=1) {
				startTime = -1;
				timer.stop();
				setEnabled(true);
				fraction = 1;
			}
			TextEffectDemo.this.fraction = fraction;
			preview.repaint();
		}
		
		private void setEnabled(boolean b) {
			textField.setEnabled(b);
			explodeEffect.setEnabled(b);
			outlineEffect.setEnabled(b);
			punchEffect.setEnabled(b);
			writeEffect.setEnabled(b);
			waveEffect.setEnabled(b);
		}
	} );
	
	ActionListener actionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src==outlineEffect) {
				doOutline();
			} else if(src==punchEffect) {
				doPunch();
			} else if(src==writeEffect) {
				doWrite();
			} else if(src==waveEffect) {
				doWave();
			} else if(src==explodeEffect) {
				doExplode();
			}
		}
	};
	
	private void doWrite() {
		String text = textField.getText();
		effect = new WriteTextEffect( WritingFont.COMIC_NEUE, text, preview.getWidth(), preview.getHeight() );
		timer.start();
	}
	
	private void doOutline() {
		Font font = new Font("Impact",0,55);
		String text = textField.getText();
		effect = new OutlineTextEffect( font, text, preview.getWidth(), preview.getHeight() );
		timer.start();
	}
	
	private void doPunch() {
		Font font = new Font("Impact",0,48);
		String text = textField.getText();
		effect = new PunchTextEffect( font, text, preview.getWidth(), preview.getHeight() );
		timer.start();
	}
	private void doWave() {
		Font font = new Font("Impact",0,55);
		String text = textField.getText();
		effect = new WaveTextEffect( font, text, preview.getWidth(), preview.getHeight() );
		timer.start();
	}
	private void doExplode() {
		Font font = new Font("Impact",0,55);
		String text = textField.getText();
		effect = new ExplodeTextEffect( font, text, preview.getWidth(), preview.getHeight() );
		timer.start();
	}
	
	PreviewPanel preview = new PreviewPanel();
	
	public TextEffectDemo() {
		getContentPane().setLayout(new GridBagLayout());

		JPanel flowPanel1 = new JPanel(new FlowLayout());
		JPanel flowPanel2 = new JPanel(new FlowLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0;
		c.insets = new Insets(3,3,3,3);
		getContentPane().add(flowPanel1, c);
		c.gridy++;
		getContentPane().add(flowPanel2, c);
		
		
		flowPanel1.add(new JLabel("Text:"));
		flowPanel1.add( textField );
		flowPanel2.add(outlineEffect);
		flowPanel2.add(punchEffect);
		flowPanel2.add(waveEffect);
		flowPanel2.add(explodeEffect);
		flowPanel2.add(writeEffect);
		
		c.gridy++; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		getContentPane().add(preview, c);

		outlineEffect.addActionListener(actionListener);
		punchEffect.addActionListener(actionListener);
		writeEffect.addActionListener(actionListener);
		waveEffect.addActionListener(actionListener);
		explodeEffect.addActionListener(actionListener);

		getContentPane().setBackground(Color.white);
		((JComponent)getContentPane()).setOpaque(true);
		flowPanel1.setOpaque(false);
		flowPanel2.setOpaque(false);
		preview.setOpaque(false);
		outlineEffect.setOpaque(false);
		punchEffect.setOpaque(false);
		writeEffect.setOpaque(false);
		waveEffect.setOpaque(false);
		explodeEffect.setOpaque(false);
	}
}
