/*
 * @(#)CacheManager.java
 *
 * $Date: 2015-12-13 19:53:39 +0100 (So, 13 Dez 2015) $
 *
 * Copyright (c) 2015 by Jeremy Wood.
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
package com.bric.desktop.cache;

import java.io.File;
import java.io.IOException;

import com.bric.io.IOUtils;
import com.bric.util.JVM;

public class CacheManager {

	/** This returns or creates a cache directory for your current user. The returned
	 * file should exist and have write privileges.
	 * @param appName this is used as part (or all) of the directory name.
	 * For best results this should be a qualified name like "com.apple.GarageBand"
	 * instead of simply "GarageBand".
	 * @return the cache directory
	 * @throws IOException if this directory couldn't be initialized correctly.
	 */
	public static File getCacheDirectory(String appName) throws IOException {
		File dir;
		if (JVM.isMac) {
			dir = new File(System.getProperty("user.home") + "/Library/Caches/"
					+ appName);
		} else if(JVM.isWindows) {
			dir = new File(System.getProperty("user.home")
					+ "\\Application Data\\" + appName);
		} else {
			dir = new File(System.getProperty("user.home") + File.separator
					+ appName);
		}
		validate(dir);
		return dir;
	}
	
	/** If a directory doesn't exist: create it, and make sure we have sufficient privileges.
	 * 
	 * @param dir the directory to create/validate.
	 * @throws IOException 
	 */
	private static final void validate(File dir) throws IOException {
		if(!dir.exists()) {
			if(!dir.mkdirs())
				throw new IOException("mkdirs failed for "+dir.getAbsolutePath());
		}
		if(!dir.canRead())
			throw new IOException("insufficient privilege for "+dir.getAbsolutePath());
		if(!dir.canWrite())
			throw new IOException("insufficient privilege for "+dir.getAbsolutePath());
	}
	
	static private  CacheManager GLOBAL;
	
	/** 
	 * 
	 * @param appName this is used as part (or all) of the directory name.
	 * For best results this should be a qualified name like "com.apple.GarageBand"
	 * instead of simply "GarageBand".
	 * @param version a String representing the version of this application.
	 * This is used to create a folder (so it should not contain slashes or colons), and it
	 * lets us differentiate and clear cached resources when the version changes.
	 * 
	 * @throws IOException 
	 */
	public static synchronized void initialize(String appName,String version) throws IOException {
		if(GLOBAL!=null)
			throw new IllegalStateException("The CacheManager has already been initialized for "+GLOBAL.appName);
		GLOBAL = new CacheManager( appName, version );
	}
	
	public static CacheManager get() {
		if(GLOBAL==null)
			throw new NullPointerException("CacheManager.initialize() has not been called.");
		return GLOBAL;
	}
	
	String appName;
	File dir;
	File commonDir;
	File versionDir;
	
	CacheManager(String appName,String version) throws IOException {
		this.appName = appName;
		dir = getCacheDirectory(appName);
		commonDir = new File(dir, "Common");
		versionDir = new File(dir, version);
		
		validate(commonDir);
		validate(versionDir);
		
		File[] file = dir.listFiles();
		for(File f : file) {
			if(f.isDirectory()) {
				if(!(f.equals(commonDir) || f.equals(versionDir))) {
					//this is probably an old version. When the user switches versions:
					//clear the old cache.
					IOUtils.delete(f);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param versioned if true then the directory this returns is going to be unique
	 * to the version this CacheManager was initialized/constructed with.
	 * If false then this directory will be static across versions.
	 * @return a directory that should be persistent across sessions. This will not
	 * be null, but it may be empty (either the user/system may choose to clear it,
	 * or it will be empty the first time the app is run).
	 */
	public File getDirectory(boolean versioned) {
		if(versioned) {
			return versionDir;
		}
		return commonDir;
	}

	/** Return true if {@link #initialize(String, String)} has been called.
	 * 
	 * @return true if {@link #initialize(String, String)} has been called.
	 */
	public static boolean isInitialized() {
		return GLOBAL!=null;
	}

}
