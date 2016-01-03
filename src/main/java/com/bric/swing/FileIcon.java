/*
 * @(#)FileIcon.java
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
package com.bric.swing;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

import com.bric.swing.resources.ScaledIcon;
import com.bric.util.JVM;

/** This offers a static method to best retrieve a File's icon.
 */
public class FileIcon {
	
	/** This captures a File's icon and saves it as a PNG.
	 * This is tested on Mac; I'm not sure how it will perform
	 * on Windows. You may need to modify the icon size. As of this
	 * writing on Mac: you can pass most any Dimension object and
	 * the icon will scale upwards safely.
	 */
	public static void main(String[] args) throws IOException {
		FileDialog fd = new FileDialog(new Frame());
		fd.pack();
		fd.setVisible(true);
		File f = new File(fd.getDirectory()+fd.getFile());
		Icon icon = getIcon(f);
		icon = new ScaledIcon(icon, 24, 24);
		BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		icon.paintIcon(null, g, 0, 0);
		g.dispose();
		File f2 = new File(f.getParentFile(), f.getName()+".png");
		if(f2.exists())
			System.exit(1);
		ImageIO.write(bi, "png", f2);
		System.exit(0);;
	}
	
	/** Return the icon of a File.
	 * <p>Unfortunately Windows and Mac require different
	 * approaches.
	 * 
	 * @param file the file to get the icon of.
	 * @return an icon for this file.
	 */
	public static Icon getIcon(File file) {
		if(file==null) throw new NullPointerException();
		
		if(JVM.isWindows) {
			//on Macs this appears to only return the vanilla folder/file icons:
			FileSystemView fsv = FileSystemView.getFileSystemView();
			Icon icon = fsv.getSystemIcon(file);
			return icon;
		} else {
			//but this returns different icons for different folders/icons:
			FileView fileView = getFileView();
			return fileView.getIcon(file);
		}
	}

	private static FileView sharedFileView;
	protected static FileView getFileView() {
		if(sharedFileView==null) {
			JFileChooser chooser = new JFileChooser();
			sharedFileView = chooser.getUI().getFileView(chooser);
		}
		return sharedFileView;
	}
}
