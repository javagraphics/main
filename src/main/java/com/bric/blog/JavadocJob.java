/*
 * @(#)JavadocJob.java
 *
 * $Date: 2015-03-01 04:21:44 +0100 (So, 01 Mär 2015) $
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
package com.bric.blog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import com.bric.io.FileTreeIterator;
import com.bric.jar.JarWriter;
import com.bric.util.BufferedPipe;
import com.bric.util.Text;

/** This job updates the javadoc.
 *
 */
class JavadocJob extends BlogUpdaterJob {
	public static final File javadocDirectory = new File(System.getProperty("user.dir")+"/www/doc/");

	File destDir;
	File[] sourcePaths;
	Process process;

	public JavadocJob(WorkspaceContext context) {
		this(context, context.getSourcePaths(), context.getDestinationSubdirectory("doc") );
	}
	
	public JavadocJob(WorkspaceContext context,File[] srcPaths,File destDir) {
		super(context);
		if(destDir==null) throw new NullPointerException();
		for(int a = 0; a<srcPaths.length; a++) {
			if(srcPaths[a]==null) throw new NullPointerException("a = "+a);
		}
		this.sourcePaths = srcPaths;
		this.destDir = destDir;
		setName("Generate Javadoc");
		setDescription("Generating Javadocs...");
	}
	
	protected String getPackageNames() {
		Vector<String> packages = new Vector<String>();
		StringBuffer sb = new StringBuffer();
		for(int a = 0; a<sourcePaths.length; a++) {
			FileTreeIterator i = new FileTreeIterator(sourcePaths[a], "java");
			while(i.hasNext()) {
				File file = i.next();
				if(!file.isDirectory()) {
					String packageName = JarWriter.getPackage(file);
					if(packageName!=null && packages.contains(packageName)==false) {
						if(packages.size()!=0) {
							sb.append(' ');
						}
						sb.append(packageName);
						packages.add(packageName);
					}
				}
			}
		}
		return sb.toString();
	}

	@Override
	protected void runJob() throws Exception {
		
		String packageNames = getPackageNames();

		StringBuffer srcPathsString = new StringBuffer();
		for(int a = 0; a<sourcePaths.length; a++) {
			if(a!=0) {
				srcPathsString.append(File.pathSeparator);
			}
			srcPathsString.append(sourcePaths[a]);
		}
		//String tagletPath = "bin";
		//String cmd = "javadoc -J-mx140m"+
		//		" -d "+destDir+
        //        " -tagletPath "+tagletPath +
        //        " -taglet com.bric.blog.SampleTaglet" +
		//		" -classpath "+srcPathsString+File.pathSeparator+System.getProperty("java.class.path")+
		//		" "+packageNames;
		String cmd = "javadoc -J-mx140m -d "+destDir+" -classpath "+srcPathsString+File.pathSeparator+System.getProperty("java.class.path")+" "+packageNames;
		System.out.println(cmd);
		process = Runtime.getRuntime().exec( cmd );

		new BufferedPipe(process.getErrorStream(), System.err, "\t") {
			@Override
			protected void process(String s) {
				super.process(s);
				setNote(s);
			}
		};


		new BufferedPipe(process.getInputStream(), System.out, "\t") {
			@Override
			protected void process(String s) {
				super.process(s);
				setNote(s);
			}
		};
		
		int status = process.waitFor();
		if(status==0) {
			System.out.println("javadoc finished");
		} else {
			System.out.println("javadoc finished, status "+status);
		}
			
		formatHTML(destDir);
	}
	
	protected static String getClasspath(File htmlFile) {
		String path = htmlFile.getAbsolutePath();
		if(path.indexOf(".")>path.lastIndexOf(File.separator))
			path = path.substring(0, path.lastIndexOf('.'));
		if(path.indexOf(".")>path.lastIndexOf(File.separator)) {
			String lhs = path.substring(0, path.lastIndexOf(File.separator));
			String rhs = path.substring(path.lastIndexOf(File.separator));
			path = lhs + rhs.replace(".", "$");
		}
		path = Text.replace(path, File.separator, ".");
		int i = path.lastIndexOf('.');
		while(true) {
			String className = path.substring(i+1);
			try {
				Class.forName(className);
				return className;
			} catch(ClassNotFoundException e) {
				
			} catch(Throwable t) {
				t.printStackTrace();
			}
			i = path.lastIndexOf('.', i-1);
			if(i==-1) return "";
		}
	}
	
	/** Strips the date, FRAMES/NO FRAMES links, and adds the "source code" link.
	 * 
	 * @param file
	 * @throws IOException
	 */
	protected void formatHTML(File file) throws IOException {
		setNote("Formatting \""+file.getName()+"\"");
		if(file.isDirectory()) {
			File[] children = file.listFiles();
			for(int a = 0; a<children.length; a++) {
				formatHTML( children[a] );
			}
		} else if(file.getName().toLowerCase().endsWith(".html")) {
			String[] lines = getStrings(file);
			
			file.delete();
			file.createNewFile();
			FileOutputStream out = null;
			String classpath = getClasspath(file);
			int insideTopNavBar = 0;
			int insideBottomNavBar = 0;
			boolean includedLink = false;
			try {
				out = new FileOutputStream(file);
				PrintStream ps = new PrintStream(out);
				for(int a = 0; a<lines.length; a++) {
					String upper = lines[a].toUpperCase();
					
					//some lines (like the date) we want to skip, otherwise our docs are ALWAYS dirty
					boolean skip = upper.startsWith("<!-- GENERATED BY JAVADOC");
					
					if(upper.trim().indexOf("<META NAME=\"DATE\"")!=-1) {
						skip = true; //ruins diffs when syncing for files that are otherwise the same
					}

					if(upper.startsWith("<LI>") && upper.endsWith("TARGET=\"_TOP\">FRAMES</A></LI>")) {
						skip = true;
					} else if(upper.startsWith("<LI>") && upper.endsWith("TARGET=\"_TOP\">NO FRAMES</A></LI>")) {
						skip = true;
					} else if(upper.contains("========= START OF TOP NAVBAR =======")) {
						insideTopNavBar++;
					} else if(upper.contains("========= START OF BOTTOM NAVBAR =======")) {
						insideBottomNavBar++;
					}
					if(insideTopNavBar>0 || insideBottomNavBar>0) {
						skip = true;
					} else if( (!includedLink) && classpath.length()>0 && lines[a].contains(classpath)) {
						includedLink = true;
						int i2 = lines[a].indexOf(classpath)+classpath.length();
						String srcURL = getSourceURL(classpath);
						lines[a] = lines[a].substring(0, i2) + " <a href=\""+srcURL+"\">(Source Code)</a>"+lines[a].substring(i2);
					
					}

					if(!skip) {
						ps.println(lines[a]);
					}
					
					if(upper.contains("========= END OF TOP NAVBAR =========")) {
						insideTopNavBar--;
					} else if(upper.contains("========= END OF BOTTOM NAVBAR =========")) {
						insideBottomNavBar--;
					}
				}
				ps.flush();
				ps.close();
			} finally {
				try {
					if(out!=null)
						out.close();
				} catch(IOException e) {}
			}
		}
	}
	
	/**
	 * 
	 * @param classpath something like "com.bric.job.JobManager"
	 * @return the source file's URL
	 */
	protected String getSourceURL(String classpath) {
		File file = getSourceFile(classpath);
		String packageName = JarWriter.getPackage(file);
		packageName = packageName.replace(".", File.separator);
		int i = file.getAbsolutePath().indexOf(packageName);
		i--;
		int i2 = file.getAbsolutePath().lastIndexOf(File.separator,i-1);
		String s2 = file.getAbsolutePath().substring(i2+1, i);
		
		String url = "https://java.net/svn/javagraphics~svn/trunk/"+s2+"/"+packageName+"/"+file.getName();
		return url;
	}
	
	Set<File> allSourceFiles = new TreeSet<File>();
	protected File getSourceFile(String classpath) {
		if(allSourceFiles.size()==0) {
			for(int a = 0; a<sourcePaths.length; a++) {
				FileTreeIterator iter = new FileTreeIterator(sourcePaths[a], "java");
				while(iter.hasNext()) {
					allSourceFiles.add( iter.next() );
				}
			}
		}
		
		String p = classpath;
		int i = p.indexOf('$');
		if(i!=-1) {
			p = p.substring(0,i);
		}
		String filename = p.substring(p.lastIndexOf('.')+1)+".java";
		
		for(File file : allSourceFiles) {
			if(file.getName().equals(filename)) {
				return file;
			}
		}
		throw new RuntimeException("did not find \""+classpath+"\"");
	}
	
	private static String[] getStrings(File file) throws IOException {
		FileInputStream in = null;
		try {
			Vector<String> strings = new Vector<String>();
			in = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String s = br.readLine();
			while(s!=null) {
				strings.add(s);
				s = br.readLine();
			}
			return strings.toArray(new String[strings.size()]);
		} finally {
			try {
				if(in!=null)
					in.close();
			} catch(IOException e) {}
		}
	}
}
