/*
 * @(#)SWFDemo.java
 *
 * $Date: 2014-05-07 08:32:48 +0200 (Mi, 07 Mai 2014) $
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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.ProgressMonitor;

import com.bric.geom.RectangularTransform;
import com.bric.image.transition.Transition;
import com.bric.image.transition.Transition2D;
import com.bric.util.ObservableList;
import com.bric.util.RandomIterator;
import com.flagstone.transform.FSBounds;
import com.flagstone.transform.FSMovie;

/** This is used to create a SWF slideshow from a folder of JPEGs 
 * 
 **/
public class SWFDemo {

    public static void run(JFrame frame,Transition[] transitions) {
        JFileChooser fcc = new JFileChooser();
        fcc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fcc.setDialogTitle("Select a Folder of JPEGs");
        fcc.showDialog(frame,"Open");
        File folder = fcc.getSelectedFile();
        if(folder==null)
            folder = fcc.getCurrentDirectory();
        if(folder==null)
            return;

        File[] files = folder.listFiles();
        Dimension bounds = new Dimension(320,240);
        int ctr = 0;
        for(int a = 0; a<files.length; a++) {
            if(files[a].isHidden() || files[a].isDirectory()) {
                files[a] = null;
            } else {
                String name = files[a].getName().toLowerCase();
                if(!(name.endsWith("jpeg") || name.endsWith("jpg"))) {
                    files[a] = null;
                }
            }
            if(files[a]!=null)
                ctr++;
        }
        ProgressMonitor monitor = new ProgressMonitor(frame,"Writing SWF","Rescaling Images",0,2*ctr);
        monitor.setMillisToDecideToPopup(0);
        monitor.setMillisToPopup(0);
        ctr = 0;
        for(int a = 0; a<files.length; a++) {
            if(monitor.isCanceled())
                return;
            monitor.setProgress(ctr);
            if(files[a]!=null) {
                try {
                    Dimension d = SWFTransitionWriter.getBounds(files[a]);
                    if(d.width!=bounds.width || d.height!=bounds.height) {
                        BufferedImage newImage = new BufferedImage(bounds.width,bounds.height,BufferedImage.TYPE_INT_RGB);
                        BufferedImage oldImage = ImageIO.read(files[a]);
                        float ratio = Math.min(
                                ((float)bounds.width)/((float)oldImage.getWidth()),
                                ((float)bounds.height)/((float)oldImage.getHeight())
                        );
                        float w = oldImage.getWidth()*ratio;
                        float h = oldImage.getHeight()*ratio;
                        Graphics2D g = newImage.createGraphics();
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setTransform(RectangularTransform.create(
                                new Rectangle(0,0,oldImage.getWidth(),oldImage.getHeight()),
                                new Rectangle2D.Float(bounds.width/2-w/2, bounds.height/2-h/2, w, h)));
                        g.drawImage(oldImage, 0, 0, null);
                        g.dispose();
                        File dest = File.createTempFile("slideImage","jpeg");
                        ImageIO.write(newImage, "jpeg", dest);
                        files[a] = dest;
                        dest.deleteOnExit();
                    }
                    ctr++;
                } catch(IOException e) {
                    files[a] = null;
                }
            }
        }
        monitor.setProgress(0);
        monitor.setNote("Writing Data");
        ctr = 0;
        
        FSMovie movie = new FSMovie();
        movie.setFrameRate(24);
        movie.setFrameSize(new FSBounds(0, 0, bounds.width, bounds.height));
        
        SWFTransitionWriter writer = new SWFTransitionWriter(movie);
        File lastImage = null;
        File firstImage = null;
        
        int jpegCount = 0;
        for(int a = 0; a<files.length; a++) {
        	if(files[a]!=null)
        		jpegCount++;
        }
        if(jpegCount<transitions.length) {
        	transitions = trimRedundantTransitions(transitions);
        }
        System.out.println("transitions.length: "+transitions.length);
        RandomIterator.randomize(files);
        
        Iterator<Transition> iter = new RandomIterator<Transition>(transitions);
        
        try {
            for(int a = 0; a<files.length; a++) {
                if(monitor.isCanceled())
                    return;
                
                monitor.setProgress(monitor.getMaximum()/2+ctr);
                
                if(files[a]!=null) {
                    File image = files[a];
                    if(lastImage!=null) {
                        writer.addFile(lastImage); //we have have to explicitly call this for the first image
                        writer.addFrame(lastImage, 2, true, null);
                        
                        Transition t = iter.next();
                        monitor.setNote(t.toString());
                        writer.addTransition(lastImage, image, t, 2, null);
                        
                        if(a!=0)
                            writer.releaseFile(lastImage);
                    }
                    lastImage = image;
                    if(firstImage==null)
                        firstImage = image;
                    
                    ctr++;
                }
            }
            if(firstImage!=null) {
            	writer.addFrame(lastImage, 2, true, null);
                writer.addTransition(lastImage,firstImage,
                        iter.next(), 2, null);
            }
            monitor.setNote("Finishing...");
            monitor.setProgress(monitor.getMaximum());
            String swfName = getUniqueFile(folder.getParentFile(),folder.getName(),".swf").getAbsolutePath();
            movie.encodeToFile(swfName);
            
            writeHTMLFile(null, new File(swfName), bounds);
            
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            monitor.close();
        }
    }
    
    protected static Transition[] trimRedundantTransitions(Transition[] transitions) {
    	Hashtable<Class<?>, Transition[]> table = new Hashtable<Class<?>, Transition[]>();
    	RandomIterator.randomize(transitions);
    	for(int a = 0; a<transitions.length; a++) {
    		Class<?> c = transitions[a].getClass();
    		Transition[] obj = table.get(c);
    		if(obj==null) {
    			table.put(c, new Transition[] { transitions[a] });
    		} else if(obj.length==1) {
    			table.put(c, new Transition[] {
    					transitions[a],
    					obj[a]
    			});
    		} else {
    			//drop it!  only support 2 of each kind.  Like Noah's ark.
    		}
    	}
    	ObservableList<Transition> v = new ObservableList<Transition>();
    	Enumeration<Class<?>> e = table.keys();
    	while(e.hasMoreElements()) {
    		Class<?> c = e.nextElement();
    		Transition[] array = table.get(c);
    		v.addAll(array);
    	}
    	return v.toArray(new Transition2D[v.size()]);
    	
    }
    
    /** Creates a unique file with the name provided.
     * <P>If a file already exists with that exact name, this method will
     * try to write "name 2.swf", "name 3.swf", etc. until we find a file
     * that does not yet exist.
     * @param parent the parent folder to write the file in
     * @param name the name of the file
     * @param suffix the suffix
     * @return a file path that does not yet exist.
     */
    protected static File getUniqueFile(File parent,String name,String suffix) {
        if(suffix.startsWith(".")==false)
            suffix = "."+suffix;
        
        File f = new File(parent,name+suffix);
        if(f.exists()==false)
            return f;
        int ctr = 2;
        while(f.exists()) {
            f = new File(parent,name+" "+ctr+suffix);
            ctr++;
        }
        return f;
    }

    /** Makes a simple, no-frills HTML wrapper for a SWF to play in.
     * 
     * @param html the HTML file to write to.  This may be null, in which case
     * the root name (minus the suffix) of the swf file will be used.
     * @param swf the SWF file to embed.
     * @param swfBounds the dimensions of the SWF file
     * @throws IOException
     */
    public static void writeHTMLFile(File html,File swf,Dimension swfBounds) throws IOException {
    	writeHTMLFile(html,swf,swfBounds,"");
    }

    /** Makes a simple, no-frills HTML wrapper for a SWF to play in.
     * 
     * @param html the HTML file to write to.  This may be null, in which case
     * the root name (minus the suffix) of the swf file will be used.
     * @param swf the SWF file to embed.
     * @param swfBounds the dimensions of the SWF file
     * @param extraHTML HTML to be added below the embedded SWF object
     * @throws IOException
     */
    public static void writeHTMLFile(File html,File swf,Dimension swfBounds,String extraHTML) throws IOException  {
        if(html==null) {
            String s = swf.getName();
            if(s.toLowerCase().endsWith(".swf")) {
                s = s.substring(0,s.length()-4);
            }
            if(s.length()==0)
                s = "Untitled";
            
            s = s+".html";
            html = new File(swf.getParent(),s);
        }
        
        OutputStream htmlOut = null;
        try {
            htmlOut = new FileOutputStream(html);
            PrintStream p = new PrintStream(htmlOut);
            int w = swfBounds.width;
            int h = swfBounds.height;
            p.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
            p.println("<html>");
            p.println("<head>");
            p.println("<title>Untitled</title>");
            p.println("</head>");
            p.println("<body leftMargin=\"0\" topMargin=\"0\" marginheight=\"0\"  marginwidth=\"0\" rightmargin=\"0\" bgcolor=\"#FFFFFF\">");
            p.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" height=\"100%\">");
            p.println("<tr>");
            p.println("<td width=\"100%\" valign=\"middle\" align=\"center\" bgcolor=\"#FFFFFF\">");
            p.println("<object CLASSID=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\" CODEBASE=\"http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,40,0\" ID=\"Untitled\" WIDTH=\""+w+"\" HEIGHT=\""+h+"\">");
            p.println("<param name=\"movie\" value=\""+swf.getName()+"\">");
            p.println("<param name=\"loop\" value=\"true\">");
            p.println("<param name=\"quality\" value=\"best\">");
            p.println("<embed NAME=\""+swf.getName()+"\" SRC=\""+swf.getName()+"\" WIDTH=\""+w+"\" HEIGHT=\""+h+"\" LOOP=\"true\" SWLIVECONNECT=\"true\" QUALITY=\"best\">");
            p.println("</object>");
            p.println("</td>");
            p.println("</tr>");
            if(extraHTML!=null && extraHTML.length()>0) {
	            p.println("<tr>");
	            p.println("<td align=\"center\" valign=\"top\">");
	            p.println(extraHTML);
	            p.println("</td>");
	            p.println("</tr>");
            }
            p.println("</table>");
            p.println("</body>");
            p.println("</html>");
            p.close();
        } finally {
            if(htmlOut!=null) {
                try {
                    htmlOut.close();
                } catch(Exception e) {}
            }
        }
    }
}
