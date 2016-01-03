/*
 * @(#)WorkspaceContext.java
 *
 * $Date: 2015-03-01 07:49:13 +0100 (So, 01 Mär 2015) $
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
package com.bric.blog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bric.io.FileTreeIterator;
import com.bric.io.IOUtils;
import com.bric.jar.JarWriter;

/** This provides information about the current workspace. This is
 * used by {@link BlogUpdaterJob} tasks to store/retrieve data.
 */
public class WorkspaceContext {
	
	static class FileNotFoundRuntimeException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public FileNotFoundRuntimeException(String s) {
			super(s);
		}
	}
	
	File[] sourcePaths;
	File[] jars;
	File workspaceDir;
	//TODO: can this only exist for a small window in time so we can be guaranteed to delete it properly (instead of relying on shutdown hooks)?
	File destinationWWWDir = IOUtils.getUniqueFile(new File(System.getProperty("java.io.tmpdir")), "www", false, true);
	JarWriter jarWriter;

	/**
	 */
	public WorkspaceContext() {
		this(new File(System.getProperty("user.dir")));
	}

	/**
	 */
	public WorkspaceContext(File workspaceDirectory) {
		this(workspaceDirectory, JarWriter.getSourcepaths(workspaceDirectory));
	}
	
	/**
	 */
	public WorkspaceContext(File workspaceDirectory,File[] sourcePaths) {
		if(workspaceDirectory==null) throw new NullPointerException();
		for(int a = 0; a<sourcePaths.length; a++) {
			if(sourcePaths[a]==null) throw new NullPointerException("sourcePaths["+a+"]");
		}
		
		if(!workspaceDirectory.exists()) throw new FileNotFoundRuntimeException(workspaceDirectory.getAbsolutePath());
		for(int a = 0; a<sourcePaths.length; a++) {
			if(!sourcePaths[a].exists()) throw new FileNotFoundRuntimeException(sourcePaths[a].getAbsolutePath());
		}
		
		this.workspaceDir = workspaceDirectory;
		this.sourcePaths = sourcePaths;
		
		destinationWWWDir.mkdirs();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				dispose();
			}
		});
	}

	/** Assign an optional JarWriter for this WorkspaceContext. */
	public void setJarWriter(JarWriter jarWriter) {
		this.jarWriter = jarWriter;
	}
	
	/** Return the {@link JarWriter} associated with this context. */
	public JarWriter getJarWriter() {
		return jarWriter;
	}
	
	private Map<File, Boolean> blurbbedFiles = new HashMap<File, Boolean>();
	/** Return true if a java file has a {@link Blurb} annotation.
	 */
	public boolean containsBlurb(File javaFile) {
		Boolean b = blurbbedFiles.get(javaFile);
		if(b!=null) return b;
		
		b = Boolean.FALSE;
		
		InputStream in = null;
		try {
			in = new FileInputStream(javaFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String s = br.readLine();
			while(s!=null && (!b)) {
				s = s.trim();
			
				if(s.contains("@Blurb")) {
					b = Boolean.TRUE;
				} else if(s.startsWith("public class") || s.startsWith("public interface")) {
					break;
				}
				s = br.readLine();
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(in!=null)
					in.close();
			} catch(Throwable t) {}
			blurbbedFiles.put(javaFile, b);
		}
		return b;
	}
	
	/** Return the {@link Blurb} associated with a java file, or null
	 * if it cannot be identified.
	 */
	public Blurb getBlurb(File javaFile) {
		String className = getClassName(javaFile);
		try {
			Class<?> theClass = Class.forName(className);
			Blurb blurb = theClass.getAnnotation(Blurb.class);
			return blurb;
		} catch(ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	/** Return the name to use for a new jar executing a certain
	 * java file. This first checks to see if there is a {@link Blurb},
	 * and if so it returns the filename attribute defined there. Otherwise
	 * this uses the file name (for example "ColorPalette.java" would return "ColorPalette").
	 */
	public String getJarName(File javaFile) {
		if(containsBlurb(javaFile)) {
			Blurb blurb = getBlurb(javaFile);
			if(blurb!=null) return blurb.filename();
		}
		return removeFileExtension(javaFile.getName());
	}
	
	/** Remove the file extension from a name. (If no extension is found,
	 * then no change occurs.)
	 */
	protected static String removeFileExtension(String s) {
		int i = s.lastIndexOf('.');
		if(i==-1) return s;
		return s.substring(0,i);
	}
	
	/** This will delete all the temp files this context is managing. */
	public void dispose() {
		IOUtils.delete(destinationWWWDir);
	}
	
	protected void finalize() throws Throwable {
		super.finalize();
		dispose();
	}
	
	/** This returns the temporary directory that is the new "www"
	 * directory (which contains a hierarchy of deliverables).
	 */
	public File getDestinationDirectory() {
		return destinationWWWDir;
	}
	
	/** Return the directory of the current workspace that Eclipse is using. */
	public File getWorkspaceDirectory() {
		return workspaceDir;
	}
	
	/** Return a copy of the array of paths for source code roots.
	 * This will probably include ".../src" and ".../tests"
	 */
	public File[] getSourcePaths() {
		return clone(sourcePaths);
	}
	
	/** Returns the source paths expressed as a unified string
	 * (where file paths are separated by {@code File.pathSeparator})
	 */
	public String getSourcePathString() {
		File[] f = getSourcePaths();
		StringBuffer returnValue = new StringBuffer();
		for(File file : f) {
			if(returnValue.length()>0) {
				returnValue.append( File.pathSeparator );
			}
			returnValue.append(file.getAbsolutePath());
		}
		return returnValue.toString();
	}
	
	private static File[] clone(File[] array) {
		File[] copy = new File[array.length];
		System.arraycopy(array, 0, copy, 0, copy.length);
		return copy;
	}
	
	/** Return all jars in the "workspace/lib" directory. */
	public File[] getJars() {
		if(jars==null) {
			List<File> jarList = new ArrayList<File>();
			File libJar = new File(workspaceDir, "lib");
			FileTreeIterator iter = new FileTreeIterator(libJar, "jar", "zip");
			while(iter.hasNext()) {
				jarList.add(iter.next());
			}
			jars = jarList.toArray(new File[jarList.size()]);
		}
		return clone(jars);
	}
	
	/** Return a subdirectory of the workspace.
	 * If this does not exist: it will throw an exception.
	 * <p>Note you'll need a separate path if you're consulting
	 * the analogous directory in the destination; see {@link #getDestinationSubdirectory(String...)}
	 * 
	 * @param path a path such as {"www", "resources"}
	 * @return the file.
	 */
	public File getWorkspaceSubdirectory(String... path) {
		return getSubdirectory(workspaceDir, false, path);
	}
	
	/** Return a subdirectory of the destination folder.
	 * If this does not exist: it will invoke mkdir as necessary.
	 * <p>Note the destination directory is equivalent to the workspace's "www"
	 * directory, so the path you provide here is different than the
	 * path you would provide to {@link #getWorkspaceSubdirectory(String...)}
	 * 
	 * @param path a path such as {"blurb", "resources"}
	 * @return the file.
	 */
	public File getDestinationSubdirectory(String... path) {
		return getSubdirectory( getDestinationDirectory(), true, path);
	}

	/** @return a subdirectory of a given folder.
	 * 
	 * @param root the directory to start in
	 * @param mkDirsIfMissing if true then directories will be created
	 * as required, if false then an exception is thrown if a directory is missing.
	 * @param path a series of folder names, such as {"www, "resources"}
	 */
	public File getSubdirectory(File root,boolean mkDirsIfMissing,String... path) {
		//synchronized because if two threads separate invoke mkdirs one will return false,
		//and an exception will follow
		synchronized(this) {
			File t = root;
			for(String dir : path) {
				t = new File(t, dir);
				if(!t.exists()) {
					if(mkDirsIfMissing) {
						if(!t.mkdirs())
							throw new RuntimeException("mkdirs failed for \""+t.getAbsolutePath());
					} else {
						throw new IllegalArgumentException("the path \""+t.getAbsolutePath()+"\" does not exist.");
					}
				}
			}
			return t;
		}
	}
	
	/** A convenience method for <code>JarWriter.getPackage(javaFile)</code>.
	 */
	public String getPackage(File javaFile) {
		return JarWriter.getPackage(javaFile);
	}

	/** Return the classname of a java file.
	 */
	public String getClassName(File file) {
		String filePath = file.getAbsolutePath();
		for(File f : sourcePaths) {
			String classPath = f.getAbsolutePath();
			if(filePath.startsWith(classPath)) {
				classPath = filePath.substring(classPath.length());
				
				//use correct separators:
				classPath = classPath.replace(File.separator, ".");
				
				//strip the file extension:
				int i = classPath.lastIndexOf('.');
				classPath = classPath.substring(0, i);
				
				if(classPath.startsWith("."))
					classPath = classPath.substring(1);
				
				//verify that we got it right:
				//commented out because this kicks off static fields/declarations, which became way too expensive
				//try {
				//	Class c = Class.forName(classPath);
				//} catch(Throwable t) {
				//	throw new RuntimeException(t);
				//}
				return classPath;
			}
		}
		
		String s = JarWriter.getClassName(file);
		return s;
	}
}
