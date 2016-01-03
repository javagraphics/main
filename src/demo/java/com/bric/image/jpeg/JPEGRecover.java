/*
 * @(#)JPEGRecover.java
 *
 * $Date: 2015-02-28 21:59:45 +0100 (Sa, 28 Feb 2015) $
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
package com.bric.image.jpeg;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import com.bric.blog.Blurb;
import com.bric.io.GuardedInputStream;
import com.bric.io.IOUtils;
import com.bric.io.MeasuredInputStream;

/** This scans a URL and identifies all the jpeg files inside it.
 * 
 * <p>The current implementation just looks for a start-of-image marker, and then
 * looks ahead to identify a functional jpeg. This appears sufficient for my
 * current needs (restoring data from a SD card). There is a lot of room to
 * study and improve this concept, though.
 * 
 */
@Blurb (
filename = "JPEGRecover",
title = "Images: Recovering JPEG Data",
releaseDate = "TBA",
summary = "This scans a file and extracts all identifiable jpgs.\n"+
"<p>(When we needed to recover jpg data from a corrupt SD memory card: "+
"some simple software gave us bizarre 20MB+ \"files\" that actually contained "+
"dozens/hundreds of jpgs.)",
scrapped = "This seems like an obscure case that I'm not convinced many readers will be interested in.",
sandboxDemo = false
)
public class JPEGRecover {
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					Frame frame = new Frame();
					FileDialog fd = new FileDialog(frame);
					fd.pack();
					fd.setVisible(true);
					if(fd.getFile()==null) return;
					
					boolean searchWholeDirectory = true;
					
					if(searchWholeDirectory) {
						File dir = new File(fd.getDirectory());
						File destDirectory = IOUtils.getUniqueFile(dir.getParentFile(), dir.getName(), true, true);
						destDirectory.mkdir();
						
						File[] children = dir.listFiles();
						
						for(File f : children) {
							JPEGRecover r = new JPEGRecover(f.toURI().toURL());
							File[] written = r.writeToDirectory(destDirectory, f.getName());
							for(File t : written) {
								System.out.println("Wrote: "+t.getAbsolutePath());
							}
						}
					} else {
						File f = new File(fd.getDirectory()+fd.getFile());
						JPEGRecover r = new JPEGRecover(f.toURI().toURL());
						File[] written = r.writeToDirectory(f.getParentFile(), f.getName());
						for(File t : written) {
							System.out.println("Wrote: "+t.getAbsolutePath());
						}

					}
				} catch(Throwable t) {
					t.printStackTrace();
				} finally {
					System.exit(0);
				}
			}
		});
	}
	
	private static class FilePtr {
		/** Where to start reading bytes. */
		final long startPtr;
		/** The amount of bytes to read. */
		final long size;
		
		FilePtr(long startPtr,long size) {
			this.startPtr = startPtr;
			this.size = size;
		}
	}

	public final URL url;
	
	private transient List<FilePtr> validFiles = null;
	
	/** Create a JPEGRecover that inspects a URL. 
	 * @throws IOException if an IO problem occurs.
	 */
	public JPEGRecover(URL url) throws IOException {
		if(url==null) throw new NullPointerException();
		
		this.url = url;
	}
	
	private synchronized void initialize() throws IOException {
		if(validFiles!=null) return;
		
		validFiles = new ArrayList<FilePtr>();
	
		// Identify all the start-of-image markers:
		Set<Long> soiMarkers = new TreeSet<Long>();
		
		InputStream in = url.openStream();
		try {
			MeasuredInputStream measuredIn = new MeasuredInputStream(in);
			int i = measuredIn.read();
			while(i!=-1) {
				int next = measuredIn.read();
				if(i==255 && next==216) {
					soiMarkers.add(measuredIn.getReadBytes()-2);
				}
				i = next;
			}
			measuredIn.close();
		} finally {
			in.close();
		}
		
		// Now (separately): look at each marker and see if
		// can make a valid jpg from that:
		for(Long marker : soiMarkers) {
			in = url.openStream();
			try {
				IOUtils.skipFully(in, marker);
				MeasuredInputStream measuredIn = new MeasuredInputStream(in);
				//if this doesn't throw an exception, then we have a legit image:
				ImageIO.read(measuredIn);
				
				//this might be little too large if the reader grabbed things
				//in large buffered chunks, but that's OK:
				long size = measuredIn.getReadBytes();
				
				//record the successful result:
				validFiles.add(new FilePtr(marker, size));
			} catch(Throwable t) {
				//System.err.println("Failed for "+marker);
				//t.printStackTrace();
			} finally {
				in.close();
			}
		}
	}
	
	/** Count the number of valid jpgs inside this URL.
	 * @throws IOException if an IO problem occurs.
	 */
	public int getJPEGCount() throws IOException {
		initialize();
		return validFiles.size();
	}
	
	/** Get the InputStream for a specific recoverable jpg.
	 * @throws IOException if an IO problem occurs.
	 */
	public InputStream getJPEG(int index) throws IOException {
		initialize();
		InputStream in = url.openStream();
		FilePtr filePtr = validFiles.get(index);
		IOUtils.skipFully(in, filePtr.startPtr);
		GuardedInputStream guardedIn = new GuardedInputStream(in, filePtr.size, true);
		return guardedIn;
	}
	
	/** Write all the available files to a directory.
	 * 
	 * @param directory the directory to write to.
	 * @param baseName the base name to use. All files will
	 * be formatted as "baseName-001", "baseName-002", etc.
	 * (The number of digits will vary; it is not fixed at 3.)
	 * 
	 * @throws IOException
	 * @return an array of all the Files written.
	 */
	public File[] writeToDirectory(File directory,String baseName) throws IOException {
		if(baseName==null) baseName = "extracted-image.jpg";
		if(directory.exists()==false) {
			if(directory.mkdirs()==false)
				throw new IOException("could not set up the directory: "+directory.getAbsolutePath());
		} else if(directory.isDirectory()==false) {
			throw new IllegalArgumentException("The first argument must be a directory ("+directory.getAbsolutePath()+")");
		}
		
		initialize();
		
		if(baseName.contains(".")) {
			baseName = baseName.substring(0, baseName.lastIndexOf("."));
		}
		
		int count = getJPEGCount();
		List<File> returnValue = new ArrayList<File>(count);
		for(int a = 0; a<count; a++) {
			InputStream in = getJPEG(a);
			File destFile = IOUtils.getUniqueFile(directory, baseName+"-"+
					IOUtils.formatIndex((a+1), count)+
					".jpg",
					true, false);
			IOUtils.write(in, destFile);
			returnValue.add(destFile);
		}
		return returnValue.toArray(new File[returnValue.size()]);
	}
}
