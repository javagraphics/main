/*
 * @(#)ValidateDeliverablesJob.java
 *
 * $Date$
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
package com.bric.blog;

import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.Parser;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

import sun.net.www.protocol.file.FileURLConnection;

import com.bric.job.Job;
import com.bric.swing.BasicConsole;

public class ValidateDeliverablesJob extends Job {
	
	enum Type { ERROR, WARNING, PASS }

	class Record {
		Set<URL> referrents = new HashSet<URL>();
		String message;
		Type type;
		
		Record(URL url) {
			try {
				URLConnection uCon = url.openConnection();
				if(uCon instanceof HttpURLConnection) {
					HttpURLConnection con = (HttpURLConnection) uCon;
					con.setRequestMethod("HEAD");
					int code = con.getResponseCode();
					if(code==HttpURLConnection.HTTP_MOVED_TEMP || code==HttpURLConnection.HTTP_MOVED_PERM) {
						type = Type.WARNING;
						message = "Redirect ("+code+") to: "+con.getHeaderField("Location");
					} else if(code!= HttpURLConnection.HTTP_OK) {
						type = Type.ERROR;
						message = "Invalid response code: "+code;
					} else {
						type = Type.PASS;
						message = "Valid response code: "+code;
					}
				} else if(uCon instanceof FileURLConnection) {
					String s = uCon.getURL().toString().substring("file:".length()).replace("/", File.separator);
					File f = new File(s);
					if(f.exists()) {
						type = Type.PASS;
						message = "This resource exists.";
					} else {
						type = Type.ERROR;
						message = "This resource does not exists.";
					}
				}
			} catch(Exception e) {
				type = Type.ERROR;
				message = e.getClass().getName()+": "+e.getLocalizedMessage();
			}
		}
		
		void addReferrent(URL url) {
			referrents.add(url);
		}
	}
	
	WorkspaceContext context;
	
	/** A queue of URLs to process. */
	LinkedList<URL> queue = new LinkedList<URL>();
	Map<URL, Record> records = new HashMap<URL, Record>();

	public ValidateDeliverablesJob(WorkspaceContext context) {
		setName("Validate Deliverables");
		setDescription("Validating links and jars...");
		setNote("This may take several minutes...");
		this.context = context;
	}

	@Override
	protected void runJob() throws Exception {
		queue.clear();
		records.clear();
		
		HttpURLConnection.setFollowRedirects(false);
		long start = System.currentTimeMillis();
		try {
			URL landingPage = new File(context.getWorkspaceSubdirectory("www"), "index.html").toURL();
			processURL(landingPage);
			while(queue.size()>0) {
				URL url = queue.remove(0);
				processURL(url);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			final BasicConsole console = BasicConsole.create("Validation Results", false, true, false);
			
			PrintStream pass = console.createPrintStream(false);
			PrintStream warning = console.createPrintStream(new Color(0x44ffff00, true));
			PrintStream error = console.createPrintStream(new Color(0x44ff0000, true));

			long time = System.currentTimeMillis() - start;
			pass.println("Validating took "+(time/1000)+" seconds.\n");
			pass.println("URL\tMessage");
			
			Type[] order = Type.values();
			for(Type currentType : order) {
				for(URL url : records.keySet()) {
					Record record = records.get(url);
					if(record.type==currentType) {
						PrintStream ps = pass;
						if(record.type==Type.WARNING) ps = warning;
						if(record.type==Type.ERROR) ps = error;
						ps.println(url+"\t"+record.message);
						for(URL r : record.referrents) {
							ps.println("* "+r);
						}
						ps.println();
					}
				}
			}
		}
	}

	private void processURL(URL url) throws Exception {
		//trim our incoming URL to ignore parameters and anchors:
		url = trim(url);
		if(url==null) return;
		
		
		Set<URL> links = getLinks(url);
		for(URL newURL : links) {
			newURL = trim(newURL);

			if(newURL.toString().startsWith("http://javagraphics.") || newURL.toString().startsWith("https://javagraphics.")) {
				if(!records.containsKey(newURL))
					queue.add(newURL);
			}

			Record record = records.get(newURL);
			if(record==null) {
				record = new Record(newURL);
				records.put(newURL, record);
			}
			record.addReferrent(url);
		}
	}
	
	private URL trim(URL url) throws MalformedURLException {
		String k = url.toString();
		
		if(k.contains("javascript:"))
			return null;
		
		if(k.startsWith("https://javagraphics.java.net/")) {
			String g = context.getWorkspaceSubdirectory("www").toURL().toString();
			k = g+k.substring("https://javagraphics.java.net/".length());
		} else if(k.startsWith("http://javagraphics.java.net/")) {
			String g = context.getWorkspaceSubdirectory("www").toURL().toString();
			k = g+k.substring("http://javagraphics.java.net/".length());
		}
		int i = k.lastIndexOf('&');
		if(i!=-1) {
			k = k.substring(0,i);
		}
		i = k.lastIndexOf('#');
		if(i!=-1) {
			k = k.substring(0,i);
		}
		i = k.lastIndexOf('?');
		if(i!=-1) {
			k = k.substring(0,i);
		}
		return new URL(k);
	}
	
	private Set<URL> getLinks(final URL url) throws Exception {
		final Set<URL> returnValue = new HashSet<URL>();
        Class<?> c = Class.forName("javax.swing.text.html.parser.ParserDelegator");
        Parser defaultParser = (Parser) c.newInstance();
        ParserCallback receiver = new ParserCallback() {

			@Override
			public void handleStartTag(Tag t, MutableAttributeSet a, int pos) {
				if(Tag.A.equals(t)) {
					try {
						String href = (String)a.getAttribute( HTML.Attribute.HREF );
						
						if(href==null) {
							System.currentTimeMillis();
						} else if(href.toLowerCase().startsWith("http://") || href.toLowerCase().startsWith("https://")) {
							returnValue.add(new URL(href));
						} else if(href.toLowerCase().startsWith("mailto") || href.toLowerCase().startsWith("javascript") ){
							//ignore these
						} else if(!href.toLowerCase().contains(":/")) {
							returnValue.add(new URL(url, href));
						} else {
							System.err.println("ValidateDeliverablesJob Warning: unrecognized link: "+url);
						}
					} catch(MalformedURLException e) {
						throw new RuntimeException(e);
					}
				}
			}
        	
        };
        try(InputStream in = url.openStream()) {
	        defaultParser.parse(new InputStreamReader( in ), receiver, true);
	        receiver.flush();
	        return returnValue;
        }
	}
}
