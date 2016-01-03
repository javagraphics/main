/*
 * @(#)CreateSamplesJob.java
 *
 * $Date: 2015-06-23 05:34:40 +0200 (Di, 23 Jun 2015) $
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import com.bric.awt.Paintable;
import com.bric.image.ImageLoader;
import com.bric.image.thumbnail.BasicThumbnail;
import com.bric.io.HTMLEncoding;
import com.bric.io.IOUtils;
import com.bric.reflect.Reflection;

/** A Job that integrates {@link ResourceSample} resources into
 * javadoc.
 */
public class CreateSamplesJob extends FormatSourceCodeJob {

	public CreateSamplesJob(WorkspaceContext context) {
		super(context);
		setName("Create Samples");
		setDescription("Creating Samples...");
	}

	@Override
	protected boolean formatFile(File javaFile) throws IOException {
		try {
			String classname = context.getClassName(javaFile);
			Class<?> t = Class.forName(classname);
			ResourceSample rs = t.getAnnotation(ResourceSample.class);
			if(rs==null) return false;
			
			String[] resources = rs.sample();
			String[] resourceNames = rs.names();
			String[] rowNames = rs.rowNames();
			String[] columnNames = rs.columnNames();
			
			int columnCount = rs.columnCount();
			
			int i = classname.lastIndexOf('.');
			String abbrClassname = classname.substring(i+1);
			
			File dest = context.getDestinationSubdirectory("resources", "samples", abbrClassname);
			
			//we're going to remake everything, so delete anything that already exists:
			IOUtils.delete(dest);
			dest.mkdirs();
			
			//make all the sample files:
			List<File> sampleFiles = new ArrayList<File>();
			Map<File, String> sampleNames = new HashMap<File, String>();
			Map<File, String> sampleAltText = new HashMap<File, String>();
			
			Set<String> uniqueResourceNames = new HashSet<String>();
			for(int a = 0; a<resources.length; a++) {
				
				//identify a unique file name for this resource:
				String resourceName = "sample";
				if(resourceNames!=null && resourceNames.length==resources.length) {
					resourceName = resourceNames[a].replace(" ", "_");
					resourceName = resourceNames[a].replace(",", "");
				}
				if(!uniqueResourceNames.add(resourceName)) {
					int ctr = 2;
					String newName;
					identifyUniqueName : while(true) {
						newName = resourceName+ctr;
						if(uniqueResourceNames.add(newName)) {
							resourceName = newName;
							break identifyUniqueName;
						}
						ctr++;
					}
				}

				try {
					File sample = createSample(resources[a], dest, resourceName);
					sampleFiles.add(sample);
					if(resourceNames!=null && resourceNames.length==resources.length) {
						sampleNames.put(sample, resourceNames[a]);
					}
					sampleAltText.put(sample, resources[a]);
				} catch(Exception e) {
					System.err.println("CreateSamplesJob: an error occurred processing \""+resources[a]+"\":");
					e.printStackTrace();
				}
			}
			
			//now generate the javadoc we'll use to describe the sample files:
			List<String> newDoc = new ArrayList<String>();
			String urlBase = "https://javagraphics.java.net/resources/samples/"+abbrClassname+"/";
			newDoc.add(" * "+SAMPLE_JAVADOC_START);
			if(sampleFiles.size()==1) {
				File file = sampleFiles.get(0);
				String altText = HTMLEncoding.encode(sampleAltText.get(file));
				newDoc.add(" * <p><img src=\""+urlBase+file.getName()+"\" alt=\""+altText+"\">");
			} else if(sampleFiles.size()>0) {
				String summary = HTMLEncoding.encode( "Resource Samples for "+classname );
				newDoc.add(" * <p>"+rs.tableIntroduction());
				newDoc.add(" * <table summary=\""+summary+"\"><tr>");
				if(columnNames.length>0) {
					newDoc.add(" * <td></td>"); //for the row name column
					for(int col = 0; col<Math.max(columnNames.length, columnCount); col++) {
						String columnHeader = col<columnNames.length ? columnNames[col] : "";
						newDoc.add(" * <td>"+columnHeader+"</td>");
					}
					newDoc.add(" * </tr><tr>");
				}
				
				int col = 0;
				int row = 0;
				String rowName = row<rowNames.length ? rowNames[row] : "";
				newDoc.add(" * <td>"+rowName+"</td>");
				for(int a = 0; a<sampleFiles.size(); a++) {
					File file = sampleFiles.get(a);
					String altText = HTMLEncoding.encode(sampleAltText.get(file));
					newDoc.add(" * <td><img src=\""+urlBase+file.getName()+"\" alt=\""+altText+"\"></td>");
					col++;
					if(col==columnCount) {
						newDoc.add(" * </tr><tr>");
						if(sampleNames.size()>0) {
							newDoc.add(" * <td></td>"); //for the row name column
							for(int b = a - columnCount+1; b<=a; b++) {
								newDoc.add(" * <td>"+sampleNames.get(sampleFiles.get(b))+"</td>");
							}
							newDoc.add(" * </tr><tr>");
						}
						row++;
						if(a!=sampleFiles.size()-1) {
							rowName = row<rowNames.length ? rowNames[row] : "";
							newDoc.add(" * <td>"+rowName+"</td>");
						}
						col = 0;
					}
				}
				//if we didn't complete the previous row, we should do so now:
				if(col>0) {
					newDoc.add(" * </tr><tr>");
					newDoc.add(" * <td></td>"); //for the row name
					if(sampleNames.size()>0) {
						for(int b = Math.max(0, sampleFiles.size() - columnCount+2); b<sampleFiles.size(); b++) {
							newDoc.add(" * <td>"+sampleNames.get(sampleFiles.get(b))+"</td>");
						}
						newDoc.add(" * </tr><tr>");
					}
				}
				newDoc.add(" * </tr></table>");
			}
			newDoc.add(" * "+SAMPLE_JAVADOC_END);
			
			String[] lines = IOUtils.readLines(javaFile, -1);
			int lineStart = -1;
			int lineEnd = -1;
			int annotationStart = -1;
			inspectFile : for(int a = 0; a<lines.length; a++) {
				if(lines[a].contains(SAMPLE_JAVADOC_START)) {
					lineStart = a;
				} else if(lines[a].contains(SAMPLE_JAVADOC_END)) {
					lineEnd = a;
					break inspectFile;
				} else if(lines[a].contains("@ResourceSample")) {
					annotationStart = a;
				}
			}
			
			List<String> list = new ArrayList<String>();
			if(lineStart!=-1 && lineEnd!=-1) {
				//we're replacing existing documentation
				for(int a = 0; a<lineStart; a++) {
					list.add(lines[a]);
				}
				list.addAll(newDoc);
				for(int a = lineEnd+1; a<lines.length; a++) {
					list.add(lines[a]);
				}
			} else if(annotationStart!=-1) {
				int javadocEnd = -1;
				int headerEnd = -1;
				inspectFile : for(int a = annotationStart; a>=0; a--) {
					if(lines[a].endsWith("*/")) {
						javadocEnd = a;
						break inspectFile;
					} else if(lines[a].startsWith("import ") || lines[a].startsWith("package ")) {
						headerEnd = a;
						break inspectFile;
					}
				}
				
				if(javadocEnd!=-1) {
					//we need to insert the new documentation at the end of the existing javadoc
					for(int a = 0; a<javadocEnd; a++) {
						list.add(lines[a]);
					}
					//for the line of javadocEnd itself let's see what else is there:
					String s = lines[javadocEnd].substring(0, lines[javadocEnd].length()-2);
					if(s.trim().length()>0) {
						list.add(s);
					}
					list.add(" * ");
					list.addAll(newDoc);
					list.add(" */");
					for(int a = javadocEnd+1; a<lines.length; a++) {
						list.add(lines[a]);
					}
				} else if(headerEnd!=-1) {
					for(int a = 0; a<=headerEnd; a++) {
						list.add(lines[a]);
					}
					list.add("");
					list.add("/**");
					list.addAll(newDoc);
					list.add(" */");
					for(int a = headerEnd+1; a<lines.length; a++) {
						list.add(lines[a]);
					}
				} else {
					//this indicates this method needs to adapt to whatever is going on...
					throw new RuntimeException("Failed to identify where to insert documentation.");
				}
			} else {
				//this indicates this method needs to adapt to whatever is going on...
				throw new RuntimeException("Failed to identify where to insert documentation.");
			}
			
			String[] newLines = new String[list.size()];
			list.toArray(newLines);
			return IOUtils.writeLines(javaFile, newLines, true);
		} catch(ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected static final String SAMPLE_JAVADOC_START = "<!-- ======== START OF AUTOGENERATED SAMPLES ======== -->";
	protected static final String SAMPLE_JAVADOC_END = "<!-- ======== END OF AUTOGENERATED SAMPLES ======== -->";
	
	protected File createSample(final String resourceText, File destDir, String resourceName) throws IOException, InvocationTargetException, InterruptedException {
		//we probably should just run all of these on the EDT to be safe:
		final Object[] t = new Object[] { null };
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				t[0] = Reflection.parse(resourceText);
			}
		});
		
		Object obj = t[0];
		if(obj instanceof Icon) {
			Icon icon = (Icon)obj;
			BufferedImage bi = paintIcon(icon);
			File file = new File(destDir, resourceName+".png");
			ImageIO.write(bi, "png", file);
			return file;
		} else if(obj instanceof BufferedImage) {
			BufferedImage bi = (BufferedImage)obj;
			File file = new File(destDir, resourceName+".png");
			ImageIO.write(bi, "png", file);
			return file;
		} else if(obj instanceof Image) {
			BufferedImage bi = ImageLoader.createImage( (Image)obj );
			File file = new File(destDir, resourceName+".png");
			ImageIO.write(bi, "png", file);
			return file;
		} else if(obj instanceof Component) {
			Component c = (Component)obj;
			if(c instanceof RootPaneContainer)
				c = ((RootPaneContainer)c).getContentPane();
			BufferedImage bi = BlogHelper.paint( c, null);
			bi = BasicThumbnail.Aqua.create(bi, null);
			File file = new File(destDir, resourceName+".png");
			ImageIO.write(bi, "png", file);
			return file;
		} else if(obj instanceof Paintable) {
			Paintable p = (Paintable)obj;
			BufferedImage bi = new BufferedImage(p.getWidth(), p.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = bi.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			p.paint(g);
			g.dispose();
			File file = new File(destDir, resourceName+".png");
			ImageIO.write(bi, "png", file);
			return file;
		} else if(obj instanceof Border) {
			JComponent jc = new JPanel();
			jc.setOpaque(false);
			jc.setPreferredSize(new Dimension(150, 150));
			jc.setBorder( (Border)obj );

			BufferedImage bi = BlogHelper.paint( jc, null);
			File file = new File(destDir, resourceName+".png");
			ImageIO.write(bi, "png", file);
			return file;
		} else {
			throw new RuntimeException("CreateSamplesJob: Unsupported resource: "+resourceText);
		}
	}
	
	protected BufferedImage paintIcon(Icon icon) {
		BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		icon.paintIcon(null, g, 0, 0);
		g.dispose();
		return bi;
	}

}
