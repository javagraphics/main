/*
 * @(#)IndexBlurbsDoclet.java
 *
 * $Date: 2015-03-01 02:39:03 +0100 (So, 01 Mär 2015) $
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

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.bric.image.ImageSize;
import com.bric.io.FileTreeIterator;
import com.bric.io.IOUtils;
import com.bric.jar.JarWriter;
import com.bric.job.Job;

/** This writes the "index.html" file.
 */
public class IndexBlurbsJob extends Job {
	
	public IndexBlurbsJob() {
		setName("Update index.html");
		setDescription("Updating index.html");
	}
	
	@Override
	protected void runJob() throws Exception {
		WorkspaceContext context = new WorkspaceContext();

		File[] srcPaths = context.getSourcePaths();
		for(File srcPath : srcPaths) {
			FileTreeIterator iter = new FileTreeIterator(srcPath, "java");
			while(iter.hasNext()) {
				File javaFile = iter.next();
				Blurb blurb = context.getBlurb(javaFile);
				if(blurb!=null) {
					if(blurb.sandboxDemo()) {
						updateJNLP(context, blurb, javaFile);
					}
					writeups.add(new BlurbWriteup(blurb));
				}
			}
		}

		writePageFromTemplate("index.html", "template.html", false, context);
		writePageFromTemplate("scrapped.html", "scrappedTemplate.html", true, context);
	}
	
	protected void updateJNLP(WorkspaceContext context,Blurb blurb,File javaFile) throws IOException {
		byte[] jnlpData = writeJNLP(context, blurb, javaFile);
		
		File jnlpFile = new File( context.getWorkspaceSubdirectory("www", "jnlp"), blurb.filename()+".jnlp");
		
		boolean equals;
		if(jnlpFile.exists()) {
			try(InputStream in1 = new ByteArrayInputStream(jnlpData);
				InputStream in2 = new FileInputStream(jnlpFile) ) {
				equals = IOUtils.equals(in1, in2);
			}
		} else {
			equals = false;
		}
		if(!equals) {
			try(InputStream in = new ByteArrayInputStream(jnlpData) ) {
				IOUtils.write(in, jnlpFile);
			}
		}
	}	

	private byte[] writeJNLP(WorkspaceContext context,Blurb blurb,File javaFile) {
		
		String projectName = blurb.filename();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try(PrintStream ps = new PrintStream(out)) {
			ps.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			ps.println("<jnlp");
			ps.println("  spec=\"6.0+\"");
			ps.println("  codebase=\"https://javagraphics.java.net/\"");
			ps.println("  href=\"jnlp/"+projectName+".jnlp\">");
			ps.println("  <information>");
			ps.println("    <title>"+projectName+"</title>");
			ps.println("    <vendor>Jeremy Wood</vendor>");
			ps.println("    <homepage href=\"https://javagraphics.java.net/\"/>");
			ps.println("    <description>"+projectName+"</description>");
			ps.println("    <offline-allowed/>" );
			ps.println("  </information>");
			ps.println("  <resources>");
			ps.println("    <j2se version=\"1.6+\" java-vm-args=\"-esa -Xnoclassgc\"/>");
			ps.println("    <jar href=\"jars/"+projectName+".jar\"/>");
			ps.println("  </resources>");
			
			String className = JarWriter.getClassName(javaFile);
			ps.println("  <application-desc main-class=\""+className+"\"/>");
			ps.println("</jnlp>");
		}
		return out.toByteArray();
	}
	
	class BlurbWriteup implements Comparable<BlurbWriteup> {
		/** All the key/values in the blurb annotation. */
		Map<String, String> attributes;
		
		public BlurbWriteup(Blurb blurb) {
			attributes = new HashMap<String, String>();
			attributes.put("filename", blurb.filename());
			attributes.put("title", blurb.title());
			attributes.put("link", blurb.link());
			attributes.put("title", blurb.title());
			attributes.put("summary", blurb.summary());
			attributes.put("releaseDate", blurb.releaseDate());
			attributes.put("scrapped", blurb.scrapped());
		}

		@Override
		public int compareTo(BlurbWriteup o) {
			return attributes.get("title").compareTo(o.attributes.get("title"));
		}
		
		/** Express this blurb as a snippet of HTML. */
		public String getHTML(WorkspaceContext context) {
			String name = attributes.get("filename");
			
			StringWriter stringWriter = new StringWriter();
			stringWriter.append("<table id=\""+name+"\" border=\"0\" cellspacing=\"0px\" cellpadding=\"10px\" padding=\"0px\" width=\"650px\">\n");
			stringWriter.append("\t<tr>\n");
			stringWriter.append("\t\t<td style=\"background: linear-gradient( rgb(255, 255, 255), rgb(241, 241, 241));border:1px solid rgb(220,220,220);width=100%;height=100%;box-shadow: 0px 1px 3px #888888;\">\n");
			stringWriter.append("\t\t\t<table width=\"100%\">\n");
			stringWriter.append("\t\t\t\t<tr>\n");
			
			String link = attributes.get("link");
			String title = strip(attributes.get("title"));
			String releaseDate = attributes.get("releaseDate");
			if(link==null || link.length()==0) {
				stringWriter.append("\t\t\t\t\t<td><span style=\"font-weight: bold;font-size:110%;}\">"+title+"</span>");
			} else {
				stringWriter.append("\t\t\t\t\t<td><span style=\"font-weight: bold;font-size:110%;}\"><a href=\""+link+"\">"+title+"</a></span>");
			}
			if(releaseDate!=null && releaseDate.length()>0) {
				stringWriter.append("<br>");
				stringWriter.append(releaseDate);
			}
			stringWriter.append("</td>\n");
			
			//this block writes the upper-right corner of the table which may include
			//the jar link and/or the jnlp link
			{
				//this preserves the order the links are presented in (this stores the
				//human-readable link name)
				List<String> linkText = new ArrayList<String>();
				
				//this maps the human-readable link name to the hyperlink
				Map<String, String> linkMap = new HashMap<String, String>();
				
				File jarDir = context.getWorkspaceSubdirectory("www", "jars");
				File jarFile = new File(jarDir, attributes.get("filename")+".jar");
				if(jarFile.exists()) {
					linkText.add("Jar");
					linkMap.put("Jar", "jars/"+jarFile.getName());
				}
				
				File jnlpDir = context.getWorkspaceSubdirectory("www", "jnlp");
				File jnlpFile = new File(jnlpDir, attributes.get("filename")+".jnlp");
				if(jnlpFile.exists()) {
					linkText.add("Web Start");
					linkMap.put("Web Start", "jnlp/"+jnlpFile.getName());
				}
				
				stringWriter.append("\t\t\t\t\t<td style=\"text-align:right;vertical-align:text-top;\"><span style=\"font-weight: bold;}\">");
				for(int a = 0; a<linkText.size(); a++) {
					String t = linkText.get(a);
					if(a>0) {
						stringWriter.append(" &#124; ");
					}
					stringWriter.append("<a href=\""+linkMap.get(t)+"\">"+t+"</a>");
				}
				stringWriter.append("</span></td>\n");
			}
			
			stringWriter.append("\t\t\t\t</tr>\n");
			stringWriter.append("\t\t\t</table>\n");
			stringWriter.append("\t\t</td>\n");
			stringWriter.append("\t</tr>\n");
			stringWriter.append("</table>\n");
			
			
			File blurbDir = context.getWorkspaceSubdirectory("www", "blurbs");
			File[] images = FileTreeIterator.findAll(
					new File[] { blurbDir },
					name, 
					new String[] { "png", "gif", "jpg", "jpeg" } );
			File blurbGraphicFile = images.length>0 ? images[0] : null;
			
			Dimension imageSize = null;
			if(blurbGraphicFile!=null)
				imageSize = ImageSize.get(blurbGraphicFile);
			
			if(blurbGraphicFile!=null && blurbGraphicFile.exists() && imageSize!=null) {
				stringWriter.append("<img align=\"right\" style=\"padding:8px\" src=\"blurbs/"+blurbGraphicFile.getName()+"\" width=\""+imageSize.width+"\" height=\""+imageSize.height+"\" alt=\"Demo Graphic\">");
			}
			
			stringWriter.append("<p>"+strip(attributes.get("summary"))+"\n");
			
			String scrapped = attributes.get("scrapped");
			if(scrapped!=null && scrapped.length()>0) {
				stringWriter.append("<p><i>Scrapped because:</i> "+scrapped);
			}
			
			stringWriter.append("<br clear=right/>\n");
			return stringWriter.toString();
		}
	}
	
	private String strip(String s) {
		s = s.replace("\\n", "");
		s = s.replace("\\'", "'");
		s = s.replace("\\\"", "\"");
		return s;
	}
	
	SortedSet<BlurbWriteup> writeups = new TreeSet<BlurbWriteup>();
	
	protected void writePageFromTemplate(String fileName,String templateName,boolean includeScrappedProjects,WorkspaceContext context) throws IOException {
		Iterator<BlurbWriteup> iter = writeups.iterator();
		StringBuffer blurbText = new StringBuffer();

		blurbText.append("<table width=\"650px\">\n<tr><td>");
		while(iter.hasNext()) {
			BlurbWriteup w = iter.next();
			
			String scrapped = w.attributes.get("scrapped");
			boolean hasScrapped = scrapped!=null && scrapped.length()>0;
			if(hasScrapped==includeScrappedProjects) {
				blurbText.append( w.getHTML(context) );
				blurbText.append( '\n' );
				blurbText.append( "<br>" );
			}
		}
		blurbText.append("</td></tr></table>");
		
		File templateFile = context.getWorkspaceSubdirectory("www", templateName);
		String html = IOUtils.read(templateFile);
		html = html.replace( "<!insertTable>", blurbText);
		
		File destFile = context.getWorkspaceSubdirectory("www", fileName);
		IOUtils.write(destFile, html, true);
	}
}
