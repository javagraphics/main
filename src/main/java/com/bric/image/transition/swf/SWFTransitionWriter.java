/*
 * @(#)SWFTransitionWriter.java
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
package com.bric.image.transition.swf;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import com.bric.image.transition.ImageInstruction;
import com.bric.image.transition.ShapeInstruction;
import com.bric.image.transition.Transition;
import com.bric.image.transition.Transition2D;
import com.bric.image.transition.Transition2DInstruction;
import com.flagstone.transform.FSBitmapFill;
import com.flagstone.transform.FSBounds;
import com.flagstone.transform.FSColorTransform;
import com.flagstone.transform.FSCoordTransform;
import com.flagstone.transform.FSDefineObject;
import com.flagstone.transform.FSDefineShape3;
import com.flagstone.transform.FSFillStyle;
import com.flagstone.transform.FSLineStyle;
import com.flagstone.transform.FSMovie;
import com.flagstone.transform.FSMovieObject;
import com.flagstone.transform.FSPlaceObject2;
import com.flagstone.transform.FSRemoveObject2;
import com.flagstone.transform.FSShape;
import com.flagstone.transform.FSShowFrame;
import com.flagstone.transform.FSSolidFill;
import com.flagstone.transform.FSSolidLine;
import com.flagstone.transform.FSSoundStreamHead;
import com.flagstone.transform.FSSoundStreamHead2;
import com.flagstone.transform.util.FSImageConstructor;

/** This uses the Transform package by <A HREF="http://www.flagstonesoftware.com/">Flagstone Software</A>
 * to encode transitions in SWF files.
 */
public class SWFTransitionWriter {
    
    FSImageConstructor imageGenerator = new FSImageConstructor();
	
	FSMovie movie;
	Dimension movieSize;
	Hashtable<File, Integer> imageIDs = new Hashtable<File, Integer>();
	
	/** Creates a new <code>SWFTransitionWriter</code>.
	 * 
	 * @param file the SWF file to write to.
	 * @param width the width (in pixels) of the movie.
	 * @param height the height (in pixels) of the movie.
	 * @param frameRate the frame rate of the movie.
	 */
	public SWFTransitionWriter(File file,int width,int height,int frameRate) {
		this(new FSMovie());
		
        movie.setFrameSize(new FSBounds(0, 0, width, height));
        movie.setFrameRate(frameRate);

		FSBounds bounds = movie.getFrameSize();
		movieSize = new Dimension(bounds.getWidth(),bounds.getHeight());
	}
	
	/** Constructs a writer object that writes to the movie provided. */
	public SWFTransitionWriter(FSMovie movie) {
		this.movie = movie;
		FSBounds bounds = movie.getFrameSize();
		movieSize = new Dimension(bounds.getWidth(),bounds.getHeight());
	}
	
    /** If a file has been used via the <code>addTransition(File, File ...)</code> method,
     * then this method should be used to remove that image from the FSMovie.
     * <P>This method will do nothing if this object did not previously interact with
     * that file.
     * <P>Subsequent calls to <code>addTransition(File, File, ...)</code> will add the image
     * again, so you should try to only remove a file when you're done using it.
     * 
     * @param jpeg the file to release
     */
	public void releaseFile(File jpeg) {
		if(imageIDs.contains(jpeg)) {
			Number number = imageIDs.get(jpeg);
			int id = number.intValue();
			movie.add(new FSRemoveObject2(id));
		}
	}
	
	/** Shorthand for <code>getMovie().encodeToFile(path)</code>. 
	 * 
	 * @param filePath the file path to write to.
	 * @throws FileNotFoundException if the file path can't be found
	 * @throws IOException if an IO error occurs
	 */
	public void encodeToFile(String filePath) throws FileNotFoundException, IOException {
		movie.encodeToFile(filePath);
	}

	/** @return the <code>FSMovie</code> object this writer is
	 * using.
	 * 
	 */
	public FSMovie getMovie() {
		return movie;
	}

	/** This appends a transition to the movie between the two images files provided.
	 * <P>It is probably better to use a method that refers to files, because these
	 * images are going to immediately wrapped up as JPEG images.
	 * 
	 * @param img1 the first image
	 * @param img2 the second image
	 * @param transition the transition to use
	 * @param duration the duration (in seconds) of the transition
	 * @throws IOException
	 */
	public void addTransition(BufferedImage img1,BufferedImage img2,Transition2D transition,float duration,List<Iterator<FSMovieObject>> audioBlockIterators) throws IOException {
		File file1 = writeJPEG(img1,.8f);
		File file2 = writeJPEG(img2,.8f);
		addTransition(file1,file2,transition,duration,audioBlockIterators);
		releaseFile(file1);
		releaseFile(file2);
	}
	
	protected static File writeJPEG(BufferedImage bi,float quality) {
		File file = null;
		ImageOutputStream out = null;
		try {
			file = File.createTempFile("image", ".jpeg");
			file.deleteOnExit();


			BufferedImage innerImage = filterARGBImage(bi);
			
			IIOImage img = new IIOImage(innerImage, null, null);
			ImageWriter iw = ImageIO.getImageWritersByMIMEType("image/jpeg").next();
			ImageWriteParam iwParam = iw.getDefaultWriteParam();
			iwParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			if(quality<.1f) quality = .1f;
			if(quality>1) quality = 1;
			iwParam.setCompressionQuality(quality);
			out = new FileImageOutputStream(file);
			iw.setOutput(out);
			iw.write(null, img, iwParam);
			
			return file;
		} catch(IOException e) {
			try {
				file.delete();
			} catch(Exception e2) {}
			throw new RuntimeException(e);
		} finally {
			 try {
				 out.close();
			 } catch(Exception e) {}
		}
	}
	

	protected static void writeJPEG(BufferedImage bi,float quality,OutputStream out) throws IOException {
		BufferedImage innerImage = filterARGBImage(bi);
		
		IIOImage img = new IIOImage(innerImage, null, null);
		ImageWriter iw = ImageIO.getImageWritersByMIMEType("image/jpeg").next();
		ImageWriteParam iwParam = iw.getDefaultWriteParam();
		iwParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		if(quality<.1f) quality = .1f;
		if(quality>1) quality = 1;
		iwParam.setCompressionQuality(quality);
		ImageOutputStream out2 = new MemoryCacheImageOutputStream(out);
		iw.setOutput(out2);
		iw.write(null, img, iwParam);
		out2.close();
	}
	
	/** This addresses a bug with the ImageIO image encoder.
	 * <P>If the argument image if of type INT_ARGB, then the
	 * JPEG encoder will think the 4 components and CMYK.  So in this
	 * case a new image of type RGB is returned.
	 * 
	 * @param image any BufferedImage
	 * @return the original argument, unless the original argument needed to
	 * be converted into an RGB image.
	 */
	private static BufferedImage filterARGBImage(BufferedImage image) {
		if(image.getType()==BufferedImage.TYPE_INT_ARGB) {
			//ugh.  there's a bug here:
			//ImageIO will see 4 channels and think, "oh!  CMYK!  my favorite!"
			//so we have to flatten this to 3 channels
					
			BufferedImage bi2 = new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_RGB);
			Graphics2D g = bi2.createGraphics();
			g.drawImage(image,0,0,null);
			g.dispose();
			return bi2;
		}
		return image;
	}

	public void addTransition(File jpeg1,File jpeg2,Transition transition,float duration,List<Iterator<FSMovieObject>> audioBlockIterators) throws IOException {
		if(transition instanceof Transition2D) {
			addTransition2D(jpeg1,jpeg2, (Transition2D)transition, duration, audioBlockIterators);
			return;
		}
		
		addJPEGTransition(jpeg1,jpeg2,transition,.9f,.9f,duration,true, audioBlockIterators);
	}
	
	public void addJPEGTransition(File jpeg1,File jpeg2,Transition transition,float duration,float initialQuality,float finalQuality,boolean lowerQualityDuringTransition,List<Iterator<FSMovieObject>> audioBlockIterators) throws IOException {
		BufferedImage bi1 = ImageIO.read(jpeg1);
		BufferedImage bi2 = ImageIO.read(jpeg2);
		addJPEGTransition(bi1,bi2,transition,duration,initialQuality,finalQuality,lowerQualityDuringTransition, audioBlockIterators);
		bi1.flush();
		bi2.flush();
	}
	
	public void addJPEGTransition(BufferedImage img1,BufferedImage img2,Transition transition,float duration,float initialQuality,float finalQuality,boolean lowerQualityDuringTransition,List<Iterator<FSMovieObject>> audioBlockIterators) {

		try {
			int frameCount = (int)(movie.getFrameRate()*duration);
			float progress;
			   
			BufferedImage temp = new BufferedImage(img1.getWidth(),img1.getHeight(),BufferedImage.TYPE_INT_RGB);
			for(int a = 0; a<frameCount; a++) {
				progress = ((float)a)/((float)(frameCount-1));
				Graphics2D g = temp.createGraphics();
				g.clearRect(0,0,temp.getWidth(),temp.getHeight());
				transition.paint(g, img1, img2, progress);
				g.dispose();
			
				float quality = (1-progress)*initialQuality+progress*finalQuality;
				if(lowerQualityDuringTransition) {
					float curve = (progress-.5f)*(progress-.5f)*2+.5f;
					quality = quality*curve;
				}
				File tempFile = writeJPEG(temp,quality);
				
				int imgID = addFile(tempFile);
				addAudioBlocks(audioBlockIterators);
				addFrame(imgID,imgID,new ImageInstruction[] {
						new ImageInstruction(true)
				}, true);
				movie.add(new FSRemoveObject2(imgID));
				releaseFile(tempFile);
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/** This appends a transition2D to the movie between the two JPEG files provided.
	 * 
	 * @param jpeg1 the first jpeg
	 * @param jpeg2 the second jpeg
	 * @param transition the transition to use
	 * @param duration the duration (in seconds) of the transition
	 * @throws IOException
	 */
	public void addTransition2D(File jpeg1,File jpeg2,Transition2D transition,float duration,List<Iterator<FSMovieObject>> audioBlockIterators) throws IOException {
        int imageID1 = addFile(jpeg1);
        int imageID2 = addFile(jpeg2);
        
        addTransition2D(imageID1,imageID2,transition,duration, audioBlockIterators);
	}
    
    /** Adds a file to this FSMovie.
     * <P>This will be called for you automatically by the <code>addTransition(File, File, ...)</code>
     * method, but if you want to call it sooner you can.
     * <P>This will not add a jpeg that is already added, so it's safe to call this redundantly.
     * 
     * @param jpeg the jpeg file to add to this movie.
     * @return the id in the FSMovie of this image
     * @throws IOException
     */
    public int addFile(File jpeg) throws IOException {

        String name = jpeg.getName().toLowerCase();
        
        if((name.endsWith("jpg") || name.endsWith("jpeg"))==false) {
            throw new IllegalArgumentException("This method is designed to only work with JPEG files ("+name+")");
        }
        
        Number k = imageIDs.get(jpeg);
        
        if(k==null) {
            int imageID = movie.newIdentifier();
            Dimension size = getBounds(jpeg);
            byte[] bytes = readFile(jpeg);
            imageGenerator.setJPEGImage(size.width,size.height,bytes);
            FSDefineObject image = imageGenerator.defineImage(imageID);
            movie.add(image);
            imageIDs.put(jpeg, new Integer(imageID));
            return imageID;
        }
        return k.intValue();
    }
	
    /** This appends a Transition2D to the FSMovie.
     * 
     * @param img1ID the ID of the first image in the FSMovie
     * @param img2ID the ID of the second image in the FSMovie
     * @param transition the transition to use.
     * @param duration the number of seconds the transition should last.
     * (The FSMovie's framerate will determine how many frames are used to
     * make a transition long enough.)
     */
	public void addTransition2D(int img1ID, int img2ID,
			Transition2D transition,float duration,List<Iterator<FSMovieObject>> audioBlockIterators) {
		
		int frameCount = (int)(movie.getFrameRate()*duration);
		
		for(int a = 0; a<frameCount; a++) {
			addAudioBlocks(audioBlockIterators);
			float progress = ((float)a)/((float)(frameCount-1));
			Transition2DInstruction[] instr = transition.getInstructions(progress, movieSize);
			addFrame(img1ID,img2ID,instr, true);
		}
	}
	
	private void addAudioBlocks(List<Iterator<FSMovieObject>> audioBlockIterators) {
		if(audioBlockIterators==null)
			return;
		
		for(int b = 0; b<audioBlockIterators.size(); b++) {
			Iterator<FSMovieObject> iter = audioBlockIterators.get(b);
			if(iter.hasNext()) {
				FSMovieObject blockObject = iter.next();
				if((blockObject instanceof FSSoundStreamHead) || (blockObject instanceof FSSoundStreamHead2)) {
					movie.add(blockObject);
					blockObject = iter.next();
				}
				movie.add(blockObject);
			}
		}
	}
    
    /** @return the ID this writer has created for the the argument file in this FSMovie */
    public int getIDForFile(File file) {
        Number number = imageIDs.get(file);
        if(number==null) return -1;
        return number.intValue();
    }
    
    public void addFrame(File jpegFile,float duration,boolean dispose,List<Iterator<FSMovieObject>> audioBlockIterators) throws IOException {
    	int frameCount = (int)(movie.getFrameRate()*duration);
    	int id = addFile(jpegFile);
    	for(int a = 0; a<frameCount; a++) {
			addAudioBlocks(audioBlockIterators);
    		addFrame(id, id, new Transition2DInstruction[] { new ImageInstruction(true)}, dispose && a==frameCount-1);
    	}
    }

    /** This appends a single frame to an FSMovie.
     * <P>This adds all the necessary objects to show a frame, then this adds
     * a <code>FSShowFrame()</code> object, and then this removes all the
     * graphic objects it added to render this frame.
     * 
     * <P>So if you are streaming audio, for example, you'll want to add those audio
     * blocks <i>before</i> you call this method.
     * 
     * @param img1ID the ID of the first image in the FSMovie
     * @param img2ID the ID of the second image in the FSMovie
     * @param instr the graphic instructions to write to the FSMovie.
     */
	public void addFrame(int img1ID,int img2ID,Transition2DInstruction[] instr,boolean dispose) {		
		for(int b = 0; b<instr.length; b++) {
			if(instr[b] instanceof ImageInstruction) {
				ImageInstruction i2 = (ImageInstruction)instr[b];
				//everything needs a clipping, because we create FSShapes for all our images.
				if(i2.clipping==null) {
					i2 = new ImageInstruction(i2); //make a copy
					i2.clipping = new Rectangle2D.Float(0,0,movieSize.width,movieSize.height);
					if(i2.transform!=null)
						i2.clipping = i2.transform.createTransformedShape(i2.clipping);
					instr[b] = i2;
				}
			}
		}
		
		instr = Transition2DInstruction.filterVisibleInstructions(instr,movieSize);
			
		Vector<Integer> objectsToRemove = new Vector<Integer>();
		
		for(int b = 0; b<instr.length; b++) {
            boolean handled = false;
			try {
				if(instr[b].getClass()==ImageInstruction.class) {
                    handled = true;
					ImageInstruction i = (ImageInstruction)instr[b];
					int imageID = (i.isFirstFrame) ? img1ID : img2ID ;
					int shapeID = movie.newIdentifier();
					
					FSShape shape = FSUtils.createFSShape(i.clipping);
					FSBounds bounds = FSUtils.getBounds(i.clipping);
					ArrayList<FSFillStyle> fillStyleArray = new ArrayList<FSFillStyle>();
					FSCoordTransform transform = FSUtils.createFSCoordTransform(i.transform);
					fillStyleArray.add(new FSBitmapFill(FSFillStyle.Clipped, imageID, transform));
					ArrayList<FSLineStyle> lineStyleArray = new ArrayList<FSLineStyle>();
					
					FSDefineShape3 defineShape = new FSDefineShape3(shapeID, bounds, fillStyleArray, lineStyleArray, shape);
					movie.add(defineShape);
					FSPlaceObject2 placeObject = new FSPlaceObject2(shapeID, b+1, 0, 0);
					if(i.opacity!=1) {
						placeObject.setColorTransform(new FSColorTransform(1f,1f,1f,i.opacity));
					}
					movie.add(placeObject);
					objectsToRemove.add(new Integer(b+1));
				} else if(instr[b].getClass()==ShapeInstruction.class) {
                    handled = true;
					ShapeInstruction i = (ShapeInstruction)instr[b];
					
					int shapeID = movie.newIdentifier();
					FSShape shape = FSUtils.createFSShape(i.shape);
					FSBounds bounds = FSUtils.getBounds(i.shape);
					ArrayList<FSFillStyle> fillStyleArray = new ArrayList<FSFillStyle>();
					if(i.fillColor!=null) {
						fillStyleArray.add(new FSSolidFill(FSUtils.createFSColor(i.fillColor)));
					}
					ArrayList<FSLineStyle> lineStyleArray = new ArrayList<FSLineStyle>();
					if(i.strokeColor!=null && i.strokeWidth>0) {
						lineStyleArray.add (new FSSolidLine ((int)(i.strokeWidth+.5f), FSUtils.createFSColor(i.strokeColor)));
					}
					FSDefineShape3 defineShape = new FSDefineShape3(shapeID, bounds, fillStyleArray, lineStyleArray, shape);
					movie.add(defineShape);
					FSPlaceObject2 placeObject = new FSPlaceObject2(shapeID, b+1, 0, 0);
					movie.add(placeObject);
					objectsToRemove.add(new Integer(b+1));	
				}
			} catch(Exception e) {
				System.err.println("instruction "+b+":");
				e.printStackTrace();
			}
            if(!handled)
                throw new RuntimeException("Unrecognized instruction: "+instr[b].getClass().getName());
		}
		movie.add(new FSShowFrame());
		if(dispose) {
			for(int b = 0; b<objectsToRemove.size(); b++) {
				Integer i = objectsToRemove.get(b);
				movie.add(new FSRemoveObject2(i.intValue()));
			}
		}
	}
	
	/** @return the bounds of the image provided.
	 * @throws UnsupportedOperationException if the file cannot be read
	 * as an image by ImageIO classes.
	 * @throws IOException if an error occurred while reading the file
	 */
	public static Dimension getBounds(File file) throws IOException {
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			ImageInputStream stream = ImageIO.createImageInputStream(in);
			Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
			ImageReader reader = iter.next();
			if(reader==null)
				throw new UnsupportedOperationException("The file \""+file.getName()+"\" is not a supported image.");
			reader.setInput(stream,false);
			int w = reader.getWidth(0);
			int h = reader.getHeight(0);
			reader.dispose();
			return new Dimension(w,h);
		} finally {
			try {
				if(in!=null)
					in.close();
			} catch(Exception e) {}
		}
	}
	
	/** This loads the contents of a file into a byte array. */
	protected static byte[] readFile(File f) throws IOException {
		byte[] data = new byte[(int)f.length()];
		FileInputStream in = new FileInputStream(f);
		int read = in.read(data,0,data.length);
		while(read!=data.length) {
			int k = in.read(data,read,data.length-read);
			if(k==-1)
				throw new IOException("Unexpected EOF");
			read+=k;
		}
		return data;
	}
}
