/*
 * @(#)AWTMonitorDemo.java
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
package com.bric.debug;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;

import javax.swing.SwingUtilities;

import com.bric.blog.Blurb;

/**
 * This is a simple app demoing the {@link AWTMonitor}
 * 
 */
@Blurb (
filename = "AWTMonitor",
title = "Event Dispatch Thread: Responding to Deadlocks",
releaseDate = "June 2008",
summary = "What can you do when your event dispatch thread is blocked?\n"+
"<P>For starters: this article mentions how to automatically detect this situation and get great console output to help pinpoint the problem. "+
"Also this delves into a very murky partial solution.  (It has worked multiple times for me, but it uses unsafe deprecated black magic.)",
link = "http://javagraphics.blogspot.com/2008/06/event-dispatch-thread-responding-to.html",
sandboxDemo = false
)
public class AWTMonitorDemo {

	/** Runs a demo program that lets the user block the AWT thread.
	 * @param args the program arguments.
	 */
	public static void main(String[] args) {
		final java.awt.Frame f = new java.awt.Frame("Demo App");
		java.awt.Button button1 = new java.awt.Button("Lock Forever");
		java.awt.Button button2 = new java.awt.Button("Lock For 20 Seconds");
		final Runnable hangRunnable = new Runnable() {
			public void run() {
				while(true) {
					synchronized(f.getTreeLock()) {
						while(true) {
							try {
								Thread.sleep(500);
							} catch(Exception e) {
								Thread.yield();
							}
						}
					}
				}
			}
		};
		
		button1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				SwingUtilities.invokeLater(hangRunnable);
			}
		});
		button2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				long start = System.currentTimeMillis();
				while(System.currentTimeMillis()-start<20000) {
					 try {
						 Thread.sleep(100);
					 } catch(Exception e2) {
						 Thread.yield();
					 }
				}
			}
		});
		f.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
		c.insets = new Insets(5,5,5,5);
		f.add(new Label("Click these buttons to lock the AWT thread:"),c);
		c.gridy++; c.gridwidth = 1;
		f.add(button1,c);
		c.gridx++;
		f.add(button2,c);
		c.gridy++; c.gridx = 0;
		c.gridwidth = 2;
		f.add(new Label("Remember this waits 2 seconds print data to the console..."),c);
		c.gridy++;
		f.add(new Label("...and waits 15 seconds to run the panicListener."),c);
		f.pack();
		f.setVisible(true);
		
		AWTMonitor.installAWTListener("Demo App",true);
	}
}
