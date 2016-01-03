/*
 * @(#)AWTMonitor.java
 *
 * $Date: 2015-11-03 14:49:56 +0100 (Di, 03 Nov 2015) $
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

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.SwingUtilities;

import com.bric.util.JVM;

/** This class monitors the event dispatch thread.
 * <P>After 2 seconds if the thread is unresponsive, a list of stack
 * traces is printed to the console.
 * <P>After 15 seconds if the thread is unresponsive, the
 * <code>panicListener</code> is notified (if it is non-null).
 * By default the <code>panicListener</code> is a {@link com.bric.debug.PanicDialogPrompt},
 * but you can nullify this value or create your own {@link com.bric.debug.AWTPanicListener}.
 * 
 */
public class AWTMonitor {
	static class AWTRunnable implements Runnable {
		boolean flag;
		
		public void run() {
			flag = true;
		}
	}
	/** The AWTPanicListeners. */
	private final static Vector<AWTPanicListener> panicListeners = new Vector<AWTPanicListener>();
	/** We keep one private copy around, but it isn't added until the install() method. */
	private final static PanicDialogPrompt defaultPrompt = new PanicDialogPrompt();
	
	/** Adds a <code>AWTPanicListener</code> to be notified when the EDT appears blocked. 
	 * @param l the listener to add.
	 */
	public static void addPanicListener(AWTPanicListener l) {
		if(panicListeners.contains(l)==true)
			return;
		panicListeners.add(l);
	}
	
	/** Removes a <code>AWTPanicListener</code>. 
	 * @param l the listener to remove.
	 */
	public static void removePanicListener(AWTPanicListener l) {
		panicListeners.remove(l);
	}
	
	private static Thread awtMonitorThread;
	
	/** This installs a thread that monitors the AWT thread.
	 * <P>If the AWT thread is unresponsive for over 2 seconds, then
	 * thread stacks are dumped to the console.  If the event dispatch
	 * thread is unresponsive for over 15 seconds, the <code>panicListener</code>
	 * (if non-null) is notified.
	 * 
	 * @param applicationName the name of the application to present to the user
	 * @param addPanicDialogPrompt if true then the <code>PanicDialogPrompt</code> is added.
	 */
	public static void installAWTListener(final String applicationName,boolean addPanicDialogPrompt) {
		installAWTListener(applicationName,2000,15000,addPanicDialogPrompt);
	}
	
	/** Installs a thread that monitors the AWT thread.
	 * 
	 * @param applicationName the name of the application to present to the user
	 * @param stackTraceDelay the delay before stack traces are printed to the console
	 * @param panicListenerDelay the delay before invoking the PanicListener.
	 * @param addPanicDialogPrompt if true then the <code>PanicDialogPrompt</code> is added.
	 */
	public synchronized static void installAWTListener(final String applicationName,final long stackTraceDelay,final long panicListenerDelay,boolean addPanicDialogPrompt) {
		if(addPanicDialogPrompt)
			addPanicListener(defaultPrompt);
		if(awtMonitorThread==null || awtMonitorThread.isAlive()==false) {
			awtMonitorThread = new Thread("AWT Listener (Debug Tool)") {
				AWTRunnable awtRunnable = new AWTRunnable();
				
				@Override
				public void run() {
					/** There are two actions that can happen here:
					 * 1.  "reporting" gives a dump stack to the console.
					 * 2.  "panicking" invokes the AWTPanicListener.
					 * 
					 * An extra half second delay is added before
					 * either action.  If a computer was put to sleep, then
					 * immediately after waking either action might
					 * be triggered: a half-second delay will give
					 * the EDT a chance to catch up and prove there's
					 * not really a problem.
					 */
					
					while(true) {
						awtRunnable.flag = false;
						try {
							SwingUtilities.invokeLater(awtRunnable);
						} catch(RuntimeException e) {
							//this can happen super early in construction:
							//we can get a NPE from the code:
							// Toolkit.getEventQueue().postEvent(
						    //       new InvocationEvent(Toolkit.getDefaultToolkit(), runnable));
							awtRunnable.flag = true;
						}
						long start = System.currentTimeMillis();
						if(!awtRunnable.flag)
						{
							boolean reportedToConsole = false;
							boolean panicked = false;
							if(awtRunnable.flag==false) {
								long reportNeeded = -1;
								long panicNeeded = -1;
								while(awtRunnable.flag==false) {
									idle();
									long current = System.currentTimeMillis();
									if(reportedToConsole==false && current-start>stackTraceDelay) {
										if(reportNeeded==-1) {
											reportNeeded = System.currentTimeMillis();
										} else if(current-reportNeeded>500) {
											dumpThreads("The AWT thread was unresponsive for "+stackTraceDelay/1000+" seconds.  Here is a stack trace from all available threads:");
											reportedToConsole = true;
										}
									}
									if(panicListeners.size()>0 && panicked==false && current-start>panicListenerDelay) {
										if(panicNeeded==-1) {
											panicNeeded = System.currentTimeMillis();
										} else if(current-panicNeeded>500) {
											panicked = true;
											Thread panicThread = new Thread("Panic Thread") {
												@Override
												public void run() {
													for(int a = 0; a<panicListeners.size(); a++) {
														AWTPanicListener panicListener = panicListeners.get(a);
														try {
															panicListener.AWTPanic(applicationName);
														} catch(Exception e) {
															e.printStackTrace();
														}
													}
												}
											};
											panicThread.start();
										}
									}
								}
							}
						}
						idle();
						
						while(System.currentTimeMillis()-start<stackTraceDelay/10) {
							idle();
						}
					}
				}
				
				private void idle() {
					try {
						Thread.sleep(200);
					} catch(Exception e) {
						Thread.yield();
					}
				}
			};
			awtMonitorThread.start();
		}
	}

	/** This effectively calls Thread.dumpStack()
	 * for every available thread.
	 * @param headerText text to print to System.err that precedes the
	 * stack traces. 
	 */
	public static void dumpThreads(String headerText) {
		try {
			Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
			Iterator<Thread> i = map.keySet().iterator();
			
			System.err.println(headerText);
			while(i.hasNext()) {
				Thread key = i.next();
				StackTraceElement[] array = map.get(key);
				String id = "";
				
				try {
					id = " (id = "+key.getId()+")";
				} catch (Throwable e) {} //we ignore this
				
				System.err.println(key.getName()+id);
				for(int a = 0; a<array.length; a++) {
					System.err.println("\t"+array[a]);
				}	
			}
		} catch (Throwable e1) {
			if(JVM.getMajorJavaVersion()>=1.5) {
				e1.printStackTrace();
			} else {
				System.err.println("Stack traces were requested, but this feature is not supported in this Java version ("+JVM.getMajorJavaVersion()+").");
			}
		}
	}
}
