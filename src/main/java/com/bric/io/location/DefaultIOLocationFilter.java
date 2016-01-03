/*
 * @(#)DefaultIOLocationFilter.java
 *
 * $Date: 2014-03-13 09:15:48 +0100 (Do, 13 Mär 2014) $
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
package com.bric.io.location;

import java.util.Vector;

import javax.swing.JComponent;

public class DefaultIOLocationFilter extends IOLocationFilter {
	static Vector<String> badZips = new Vector<String>();
	
	JComponent component;
	
	public DefaultIOLocationFilter(JComponent jc) {
		component = jc;
	}
	
	protected boolean getBoolean(String keyName,boolean defaultValue) {
		Boolean b = (Boolean)component.getClientProperty(keyName);
		if(b==null) return defaultValue;
		return b.booleanValue();
	}
	
	@Override
	public IOLocation filter(IOLocation loc) {
		if(getBoolean("showHiddenFiles",false)==false) {
			if(loc.isHidden())
				return null;
		}
		if(getBoolean("openArchives",false)) {
			synchronized(badZips) {
				if(badZips.contains(loc.toString())==false) {
					String name = loc.getName().toLowerCase();
					if(name.endsWith(".zip") || name.endsWith(".jar")) {
						try {
							ZipArchiveLocation zipLoc = new ZipArchiveLocation(loc);
							return zipLoc;
						} catch(OutOfMemoryError e) {
							e.printStackTrace();
							System.err.println("out of memory reading "+loc);
							badZips.add(loc.toString());
						} catch(Exception e) {
							e.printStackTrace();
							System.err.println("error reading "+loc);
							badZips.add(loc.toString());
						}
					}
				}
			}
		}
		return loc;
	}
}
