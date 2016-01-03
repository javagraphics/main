/*
 * @(#)FileReader.java
 *
 * $Date: 2014-06-06 20:04:49 +0200 (Fr, 06 Jun 2014) $
 *
 * Copyright (c) 2013 by Jeremy Wood.
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
package com.bric.viewer.reader;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JComponent;

import com.bric.io.IOUtils;
import com.bric.io.location.IOLocation;
import com.bric.util.JVM;

public abstract class FileReader {
	public abstract String getDescription();
	public abstract String[] getExtensions();
	
	/** Return a component to render the contents of a file.
	 * Note this is may be called on or off the EDT.
	 * 
	 * @param loc the file to display.
	 * @param expectedSize an optional argument that gives the estimated size this component
	 * should be. This is useful for scaling content.
	 * @return a component to view the file provided.
	 * @throws IOException if an IO problem occurs.
	 */
	public abstract JComponent getComponent(IOLocation loc,Dimension expectedSize) throws IOException;

	/** Return an optional array of small controls to place in the footer from left-to-right. */
	public JComponent[] getFooterControls() {
		return new JComponent[] {};
	}
	
	/** This checks the extension of a location against the 
	 * <code>getExtensions()</code> array.
	 * 
	 * @param loc the location to check.
	 * @return true if this location is accepted.
	 */
	public boolean accepts(IOLocation loc) {
		String ext = loc.getExtension();
		String[] array = getExtensions();
		for(String s : array) {
			if(ext!=null && ext.equalsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}

	
	static Map<IOLocation, URL> urlMap = new Hashtable<IOLocation, URL>();
	static File cachedFileDir = null;
	protected URL getURL(IOLocation loc) throws IOException {
		URL url = loc.getURL();
		if(url!=null) return url;
		url = urlMap.get(loc);
		if(url==null) {
			synchronized(urlMap) {
				if(cachedFileDir==null) {
					File tmpDir = new File(System.getProperty("java.io.tmpdir"));
					cachedFileDir = IOUtils.getUniqueFile(tmpDir, "FileReaderCache", false, true);
					cachedFileDir.mkdirs();
					Runtime.getRuntime().addShutdownHook(new Thread() {
						public void run() {
							if(!cachedFileDir.delete()) {
								try {
									Process p = null;
									if(JVM.isMac) {
										p = Runtime.getRuntime().exec("rm -R "+cachedFileDir.getAbsolutePath());
									} else if(JVM.isWindows) {
						                String [] cmd1 ={"cmd","/C","rmdir",cachedFileDir.getAbsolutePath(),"/S","/Q"};
						                p = Runtime.getRuntime().exec(cmd1);
									}
									int exitStatus = p==null ? -1 : p.waitFor();
									if(exitStatus==0)
										return;
								} catch (InterruptedException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
								
								//grrr...
								File[] children = cachedFileDir.listFiles();
								for(File child : children) {
									child.delete();
								}
							}
						}
					});
				}
			}
			File dest = IOUtils.getUniqueFile(cachedFileDir, loc.getName(), false, true);
			InputStream in = null;
			try {
				in = loc.createInputStream();
				IOUtils.write(in, dest);
				url = dest.toURI().toURL();
				urlMap.put(loc, url);
			} finally {
				if(in!=null) {
					try {
						in.close();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return url;
	}
}
