/*
 * @(#)JPEGMetaDataTest.java
 *
 * $Date: 2014-06-06 20:04:49 +0200 (Fr, 06 Jun 2014) $
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
package com.bric.image.jpeg;

import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;

import junit.framework.TestCase;

import com.bric.io.FileTreeIterator;
import com.bric.io.SuffixFilenameFilter;

public class JPEGMetaDataTest extends TestCase {
	static class Failure {
		File file;
		Throwable throwable;
		
		Failure(File f,Throwable t) {
			file = f;
			throwable = t;
		}
	}
	
	static class MyJPEGMetaData extends JPEGMetaData {
		int app0Thumbs, app1Thumbs, app2Thumbs, app13Thumbs;
		int app1_xmp, app1_exif, app1_unknown;

		public MyJPEGMetaData(File file, boolean fetchThumbnail)
				throws IOException {
			super(file, fetchThumbnail);
		}

		@Override
		protected void processAPP0(APP0Data data) {
			if(data.getThumbnail()!=null)
				app0Thumbs++;
			super.processAPP0(data);
		}

		@Override
		protected void processAPP2(APP2Data data) {
			if(data.getThumbnail()!=null)
				app2Thumbs++;
			super.processAPP2(data);
		}

		@Override
		protected void processAPP13(APP13Data data) {
			if(data.getThumbnail()!=null)
				app13Thumbs++;
			super.processAPP13(data);
		}

		@Override
		protected void processAPP1(APP1Data data) {
			if(data.getThumbnail()!=null)
				app1Thumbs++;
			if(data.type==APP1Data.TYPE_XMP) {
				app1_xmp++;
			} else if(data.type==APP1Data.TYPE_EXIF) {
				app1_exif++;
			} else {
				app1_unknown++;
			}
			super.processAPP1(data);
		}

		@Override
		protected void processComment(String comment) {
			super.processComment(comment);
		}

	}
	
	private File getJPEG(String name) {
		FileTreeIterator i = getJPEGIterator();
		while(i.hasNext()) {
			File f = i.next();
			if(f.getName().equals(name))
				return f;
		}
		return null;
	}
	
	private FileTreeIterator getJPEGIterator() {
		SuffixFilenameFilter filter = new SuffixFilenameFilter("jpg", "jpeg");
		char s = File.separatorChar;
		
		//for fun/thoroughness: try switching this
		//boolean to false to widen the net of images
		//we test against:
		boolean limitedTestPool = true;
		
		File base = limitedTestPool ? 
				new File(System.getProperty("user.dir")+
						s+"tests"+s+"com"+s+"bric"+s+"image"+s+"resources"+s) :
				new File(System.getProperty("user.home"));
		FileTreeIterator i = new FileTreeIterator(
				base,
				filter);
		return i;
	}

	/** This test searches for a JPEG with an APP0 segment
	 * that includes a thumbnail.  It <i>fails</i>
	 * if it finds such a segment: mostly to draw
	 * attention to this situation.
	 * 
	 * @throws Exception if an error occurs.
	 */
	public void testAPP0ThumbnailPresence() throws Exception {
		FileTreeIterator i = getJPEGIterator();
		int ctr = 0;
		final boolean[] failed = new boolean[] { false };
		while(i.hasNext()) {
			File file = i.next();
			@SuppressWarnings("unused")
			JPEGMetaData md = new JPEGMetaData(file, true) {
				@Override
				protected void processAPP0(APP0Data data) {
					if(data.getThumbnail()!=null) {
						failed[0] = true;
					}
				}
			};
			assertFalse("Good news!  This file has an APP0 with a thumbnail: "+file.getAbsolutePath(), failed[0] );
			ctr++;
		}
		System.out.println(ctr+" JPEGs searched: no APP0 thumbnail found");
	}
	

	private static boolean compareAgainst(MyJPEGMetaData meta,File f) throws IOException {
		FileInputStream fileIn = null;
		StringBuffer sb = new StringBuffer();
		boolean returnValue = false;
		try {
			fileIn = new FileInputStream(f);
			JPEGMarkerInputStream in = new JPEGMarkerInputStream(fileIn);
			while(in.getNextMarker()!=null) {
				String marker = in.getCurrentMarker();
				if("FFDA".equals(marker))
					break;

				BufferedImage image = null;
				try {
					image = GenericDataWithThumbnail.readJPEG(in);
				} catch(Exception e) {
					e.printStackTrace();
					image = null;
				}
				if(marker.equals(JPEGMarkerInputStream.APP0_MARKER)) {
					if( (image!=null && meta.app0Thumbs>0) || (image==null && meta.app0Thumbs==0)) {
						//nothing to report
					} else if(image==null && meta.app0Thumbs>0) {
						//sb.append("*APP0: Failed to observe image, but parsed successfully.\n");
					} else if(image!=null && meta.app0Thumbs==0) {
						sb.append("APP0: Observed image, but failed to parse.\n");
						returnValue = true;
					} else {
						sb.append("APP0: thumbs = "+meta.app0Thumbs+" "+(image==null)+"\n");
						returnValue = true;
					}
				} else if(marker.equals(JPEGMarkerInputStream.APP1_MARKER)) {
					if( (image!=null && meta.app1Thumbs>0) || (image==null && meta.app1Thumbs==0)) {
						//nothing to report
					} else if(image==null && meta.app1Thumbs>0) {
						//sb.append("*APP1: Failed to observe image, but parsed successfully.\n");
					} else if(image!=null && meta.app1Thumbs==0) {
						sb.append("APP1: Observed image, but failed to parse.\n");
						returnValue = true;
					} else {
						sb.append("APP1: thumbs = "+meta.app1Thumbs+" "+(image==null)+"\n");
						returnValue = true;
					}
				} else if(marker.equals(JPEGMarkerInputStream.APP2_MARKER)) {
					if( (image!=null && meta.app2Thumbs>0) || (image==null && meta.app2Thumbs==0)) {
						//nothing to report
					} else if(image==null && meta.app2Thumbs>0) {
						//sb.append("*APP2: Failed to observe image, but parsed successfully.\n");
					} else if(image!=null && meta.app2Thumbs==0) {
						sb.append("APP2: Observed image, but failed to parse.\n");
						returnValue = true;
					} else {
						sb.append("APP2: thumbs = "+meta.app2Thumbs+" "+(image==null)+"\n");
						returnValue = true;
					}
				} else if(marker.equals(JPEGMarkerInputStream.APP13_MARKER)) {
					if( (image!=null && meta.app13Thumbs>0) || (image==null && meta.app13Thumbs==0)) {
						//nothing to report
					} else if(image==null && meta.app13Thumbs>0) {
						//sb.append("*APP13: Failed to observe image, but parsed successfully.\n");
					} else if(image!=null && meta.app13Thumbs==0) {
						sb.append("APP13: Observed image, but failed to parse.\n");
						returnValue = true;
					} else {
						sb.append("APP13: thumbs = "+meta.app13Thumbs+" "+(image==null)+"\n");
						returnValue = true;
					}
				} else if(image!=null) {
					sb.append("Observed image for "+marker+"\n");
					returnValue = true;
				}
			}
			if(sb.length()>0) {
				System.out.println("\t"+f.getAbsolutePath()+"\n"+sb.toString().trim());
			}
		} catch(EOFException e) {
			//this is expected
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(fileIn!=null)
				fileIn.close();
		}
		return returnValue;
	}
	
	/** Verifies that we identified all 0xFFD8 markers in this file.
	 * This test will fail if 0xFFD8 occurs in a JPEG and we didn't
	 * detect another thumbnail.  This would indicate that we're not parsing
	 * a marker correctly.
	 * 
	 * @throws Exception if an error occurred parsing certain data.
	 */
	public void testIdentifyEmbeddedJPEGs() throws Exception {
		FileTreeIterator i = getJPEGIterator();
		int ctr = 0;
		int failed = 0;
		while(i.hasNext()) {
			File f = i.next();
			try {
				MyJPEGMetaData jmd = new MyJPEGMetaData(f, true);
				if(compareAgainst(jmd, f))
					failed++;
			} catch(Exception e) {
				System.err.println(f.getAbsolutePath());
				e.printStackTrace();
				failed++;
			}
			ctr++;
		}
		assertTrue("see the console for details. failed = "+failed, failed==0);
		System.out.println("The embedded thumbnails in "+ctr+" jpegs were all detected.");
		
	}
	
	/** Verifies that certain known comments were parsed correctly.
	 * 
	 * @throws Exception if an error occurs.
	 */
	public void testComments() throws Exception {
		File jpeg1 = getJPEG("3588772325_30533dd2d2_o.jpg");
		File jpeg2 = getJPEG("5268130207_30c91895e0_o.jpg");
		File jpeg3 = getJPEG("5344858574_79318e2c80_o.jpg");
		
		JPEGMetaData md1 = new JPEGMetaData(jpeg1, false);
		JPEGMetaData md2 = new JPEGMetaData(jpeg2, false);
		JPEGMetaData md3 = new JPEGMetaData(jpeg3, false);
		
		String[] comments1 = md1.getComments();
		String[] comments2 = md2.getComments();
		String[] comments3 = md3.getComments();
		
		String[] expected1 = new String[] {
				"LEAD Technologies Inc. V1.01\u0000",
				"2003-5001_2_23667, 29/03/2007, 15:56,  8C, 5250x3916 (0+1914), 88%, Custom,   1/8 s, R24.4, G10.1, B32.1\r"
		};
		String[] expected2 = new String[] {
				"Handmade Software, Inc. Image Alchemy v1.11\n"
		};
		String[] expected3 = new String[] {
				"AppleMark\n"
		};
		
		assertTrue( equals(comments1, expected1));
		assertTrue( equals(comments2, expected2));
		assertTrue( equals(comments3, expected3));
		
		assertTrue(md1.getThumbnail()==null);
		assertTrue(md2.getThumbnail()==null);
		assertTrue(md3.getThumbnail()==null);
		
		System.out.println("Comments in 3 files were verified.");
	}
	
	private boolean equals(String[] s1,String[] s2) {
		if(s1.length!=s2.length) return false;
		for(int a = 0; a<s1.length; a++) {
			for(int b = 0; b<s1[a].length(); b++) {
				int c1 = s1[a].charAt(b);
				int c2 = s2[a].charAt(b);
				if(c1!=c2)
					return false;
			}
		}
		return true;
	}
	
	/** This isn't really a test, it just outputs data to the console
	 * about how much different types of thumbnails were found.
	 */
	/*public void testProportions() throws Exception {
		int APP0_MASK = 1;
		int APP1_MASK = 2;
		int APP13_MASK = 4;
		
		FileTreeIterator i = getJPEGIterator();
		
		int[] thumbnailCombinations = new int[8];
		
		while(i.hasNext()) {
			File f = i.nextFile();
			MyJPEGMetaData jmd = new MyJPEGMetaData(f, true);
			int myMask = 0;
			if(jmd.app0Thumbs>0) {
				myMask += APP0_MASK;
			}
			if(jmd.app1Thumbs>0) {
				myMask += APP1_MASK;
			}
			if(jmd.app13Thumbs>0) {
				myMask += APP13_MASK;
			}
			thumbnailCombinations[myMask]++;
		}
		
		System.out.println("No Thumbnails\t"+thumbnailCombinations[0]);
		for(int a = 1; a<thumbnailCombinations.length; a++) {
			StringBuffer sb = new StringBuffer();
			if( (a & APP0_MASK) > 0) {
				sb.append("APP0 ");
			}
			if( (a & APP1_MASK) > 0) {
				sb.append("APP1 ");
			}
			if( (a & APP13_MASK) > 0) {
				sb.append("APP13 ");
			}
			System.out.println(sb.toString().trim()+"\t"+thumbnailCombinations[a]);
		}
	}*/

	/** Verifies that all JPEGs could be passed through the JPEGMetaData
	 * constructor successfully.
	 */
	public void testExceptions() {
		FileTreeIterator i = getJPEGIterator();
		Vector<Failure> failures = new Vector<Failure>();
		Vector<File> successes = new Vector<File>();
		
		while(i.hasNext()) {
			File f = i.next();
			try {
				new JPEGMetaData(f, true);
				successes.add(f);
			} catch(Throwable t) {
				failures.add(new Failure(f, t));
			}
		}
		
		System.out.println("Successes = "+successes.size());
		System.out.println("Failures = "+failures.size());
		if(failures.size()>0) {
			for(int a = 0; a<failures.size(); a++) {
				Failure failure = failures.get(a);
				System.err.println(failure.file.getAbsolutePath());
				failure.throwable.printStackTrace();
			}
			Failure failure = failures.get(0);
			fail("the image \""+failure.file.getAbsolutePath()+"\" caused JPEGMetaData to throw an exception.");
		}
		assertTrue(failures.size()==0);
	}
}
