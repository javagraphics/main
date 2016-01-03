/*
 * @(#)SWFProfiler.java
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
package com.bric.image.transition.swf;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import com.bric.image.transition.Transition2D;
import com.flagstone.transform.FSBounds;
import com.flagstone.transform.FSMovie;

/** This creates several webpages about
 * a series of transitions.
 * Currently its main focus is on the file size
 * of the SWF files that are created.
 * 
 */
public class SWFProfiler {
	
	File file1, file2;
	
	File directory;
	
	Hashtable<Transition2D, File> table = new Hashtable<Transition2D, File>();

	Dimension bounds;
	
	public SWFProfiler(JFrame frame,Transition2D[] transitions,File jpeg1,File jpeg2) throws IOException {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogType(JFileChooser.SAVE_DIALOG);
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		jfc.showDialog(frame, "Save");
		
		file1 = jpeg1;
		file2 = jpeg2;
		
		bounds = SWFTransitionWriter.getBounds(file1);
		
		directory = jfc.getSelectedFile();
		directory.mkdirs();
		
		
		for(int a = 0; a<transitions.length; a++) {
			File swf = build(transitions[a]);

			File html = new File(directory,transitions[a].toString()+".html");
			String prevPage = transitions[(a+transitions.length-1)%transitions.length].toString()+".html";
			String nextPage = transitions[(a+transitions.length+1)%transitions.length].toString()+".html"; 
			String extraHTML = "<FONT FACE=\"Verdana\">";
			extraHTML+="<BR>Name: "+transitions[a].toString();
			extraHTML+="<BR>";
			extraHTML+="<BR>File Size: "+getFileSize(swf);
			extraHTML+="<BR>";		
			extraHTML+="<BR><A HREF=\""+prevPage+"\">Prev</A> <A HREF=\"index.html\">Summary</A> <A HREF=\""+nextPage+"\">Next</A>";
			SWFDemo.writeHTMLFile(html, swf, bounds, extraHTML);
		}
		
		File mainPage = new File(directory,"index.html");
		mainPage.createNewFile();
		FileOutputStream fileOut = new FileOutputStream(mainPage);
		PrintStream out = new PrintStream(fileOut);
		out.println("<HTML>");
        out.println("<HEAD>");
        out.println("<TITLE>Transition Profiler</TITLE>");
        out.println("</HEAD>");
		out.println("<FONT FACE=\"VERDANA\">");
		out.println("<BR>Not all transitions are equal.  This table show file sizes for different SWF files that transition between the same images.");
		out.println("<BR><BR>I haven't made a specific study of what characteristics make a transition efficient, but here you can see for yourself what different transitions \"cost\".");
		out.println("<BR><BR>(Of course if you know what changes -- either in my code or in the <A HREF=\"http://www.flagstonesoftware.com/transform/index.html\">Flagstone encoder</A> -- can be used to reduce these file sizes, you should <A HREF=\"mailto:mickleness@dev.java.net\">contact me</A> to discuss it.");
		out.println("<BR><BR>");
		out.println("<FONT ALIGN=\"CENTER\">");
		out.println("<TABLE BORDER=\"1\" CELLPADDING=\"5\">");
		out.println("<TR>");
		out.println("<TD>");
		out.println("<H1>Transitions</H1>");
		out.println("</TD>");
		out.println("<TD>");
		out.println("<H1>SWF Size</H1>");
		out.println("</TD>");
		out.println("</TR>");
		for(int a = 0; a<transitions.length; a++) {
			out.println("<TR>");
			out.println("<TD>");
			String htmlName = transitions[a].toString()+".html";
			out.println("<A HREF=\""+htmlName+"\">"+transitions[a].toString()+"</A>");

			out.println("<TD ALIGN=\"RIGHT\">");
			File file = table.get(transitions[a]);
			out.println(getFileSize(file));
			
			out.println("</TD>");
			out.println("</TD>");
			out.println("</TR>");
		}
		out.println("</TABLE>");
		out.println("</HTML>");
	}
	
	protected String getFileSize(File file) {
		float k = file.length();
		k = k/1024f;
		k = ((int)(k*100))/100f;
		return k+" KB";
	}
	
	public File build(Transition2D transition) throws IOException {
		FSMovie movie = new FSMovie();
		movie.setFrameRate(24);
		movie.setFrameSize(new FSBounds(0,0,bounds.width,bounds.height));
		
		SWFTransitionWriter writer = new SWFTransitionWriter(movie);
		writer.addTransition(file1, file2, transition, 2, null);
		writer.addTransition(file2, file1, transition, 2, null);
		
		File dest = new File(directory,transition.toString()+".swf");
		try {
			movie.encodeToFile(dest.getAbsolutePath());
		} catch(Error e) {
			System.err.println("An error occurred while writing "+transition);
			throw e;
		}
		table.put(transition, dest);
		return dest;
	}
}
