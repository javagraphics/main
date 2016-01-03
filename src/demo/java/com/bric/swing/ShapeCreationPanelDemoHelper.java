/*
 * @(#)ShapeCreationPanelDemoHelper.java
 *
 * $Date: 2014-06-22 00:36:20 +0200 (So, 22 Jun 2014) $
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
package com.bric.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.bric.animation.AnimationReader;
import com.bric.blog.BlogHelper;
import com.bric.capture.ComponentPaintCapture;
import com.bric.image.gif.GifWriter;
import com.bric.image.gif.GifWriter.ColorReduction;

public class ShapeCreationPanelDemoHelper extends BlogHelper {
	
	/** Create animations in the "shape-creation" directory. 
	 * 
	 * @param robot the Robot to use to create sample files.
	 * @param dir the master directory to store all resources/subdirectories in.
	 * @return the directory titled "shape-creation"
	 * @throws Exception if an error occurred creating these demo files.
	 */
	public static File createSamples(Robot robot, File dir) throws Exception {
		dir = new File(dir, "shape-creation");
		if(!dir.mkdirs())
			throw new RuntimeException("mkdirs failed for "+dir.getAbsolutePath());

		{
			File gifFile = new File(dir, "curved-polyline.gif");
			AnimationReader reader = createCurvedPolylineAnimation(robot);
			GifWriter.write(gifFile, reader, ColorReduction.FROM_FIRST_FRAME);
		}
		
		{
			File gifFile = new File(dir, "cubic-path.gif");
			AnimationReader reader = createCubicPathAnimation(robot);
			GifWriter.write(gifFile, reader, ColorReduction.FROM_FIRST_FRAME);
		}
		
		{
			File gifFile = new File(dir, "mouse-smoothing.gif");
			AnimationReader reader = createMouseSmoothingAnimation(robot, new File(dir, "unsmoothed.png"));
			GifWriter.write(gifFile, reader, ColorReduction.FROM_FIRST_FRAME);
		}
		
		return dir;
	}
	
	private static AnimationReader createCurvedPolylineAnimation(Robot robot) throws Exception {
		final ShapeCreationPanelDemo scpd = new ShapeCreationPanelDemo();
		final JFrame frame = new JFrame();
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				scpd.uiComboBox.setSelectedIndex(1);
				frame.getContentPane().add(scpd);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
		
		Rectangle bounds = frame.getBounds();
		Insets i = frame.getInsets();
		bounds.x += i.left;
		bounds.y += i.top;
		bounds.width -= i.left + i.right;
		bounds.height -= i.top + i.bottom + 3;
		
		ComponentPaintCapture screenCapture = new ComponentPaintCapture(scpd);
		screenCapture.setTargetedRepaints(false);
		screenCapture.setPlaybackRate(2);
		
		Point[] points = new Point[] {
				new Point( 200, 200 ), 
				new Point( 300, 400 ),
				new Point( 400, 380 ),
				new Point( 60, 140 ),
				new Point( 340, 180 ),
				new Point( 40, 330 ) };

		
		//starting position:
		BlogHelper.animateMouse(robot, scpd.createButton, false, false);
		
		Thread.sleep(1000);
		screenCapture.start();
		Thread.sleep(1000);
		

		BlogHelper.animateMouse(robot, scpd.createButton, false, true);
		
		for(int a = 0; a<points.length; a++) {
			BlogHelper.animateMouse(robot, scpd.shapePanel, points[a], true, a==points.length-1 ? 2 : 1);
			Thread.sleep(1000);
		}
		
		BlogHelper.animateMouse(robot, scpd.clearButton, true, true);

		//return to start:
		BlogHelper.animateMouse(robot, scpd.createButton, true, false);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch(Throwable t) {
					t.printStackTrace();
				}
				frame.setVisible(false);
			}
		});
		
		return screenCapture.stop(true);
	}
	
	private static AnimationReader createCubicPathAnimation(Robot robot) throws Exception {
		final ShapeCreationPanelDemo scpd = new ShapeCreationPanelDemo();
		final JFrame frame = new JFrame();
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				frame.getContentPane().add(scpd);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
		
		Rectangle bounds = frame.getBounds();
		Insets i = frame.getInsets();
		bounds.x += i.left;
		bounds.y += i.top;
		bounds.width -= i.left + i.right;
		bounds.height -= i.top + i.bottom + 3;
		
		ComponentPaintCapture screenCapture = new ComponentPaintCapture(scpd);
		screenCapture.setTargetedRepaints(false);
		screenCapture.setPlaybackRate(2);
		
		Point[] points = new Point[] {
				new Point( 200, 200 ), 
				new Point( 300, 400 ),
				new Point( 400, 380 ),
				new Point( 260, 140 ),};
		
		Point2D.Float[] relativeControlPoints = new Point2D.Float[] {
				new Point2D.Float(30*2, 20*2),
				new Point2D.Float(-20*2, 5*2),
				new Point2D.Float(25*2, -40*2),
				new Point2D.Float(-40*2, 30*2)
		};

		
		//starting position:
		BlogHelper.animateMouse(robot, scpd.createButton, false, false);
		
		Thread.sleep(1000);
		screenCapture.start();
		Thread.sleep(1000);
		

		BlogHelper.animateMouse(robot, scpd.createButton, false, true);
		
		for(int a = 0; a<points.length; a++) {
			BlogHelper.animateMouse(robot, scpd.shapePanel, points[a], true, 0);
			Thread.sleep(100);
			robot.mousePress(InputEvent.BUTTON1_MASK);
			for(float f = 0; f<1; f+=.05f) {
				Point t = new Point( (int)(points[a].getX()+f*relativeControlPoints[a].getX()),
						(int)(points[a].getY()+f*relativeControlPoints[a].getY()) );
				System.out.println(f+", "+points[a]+"->"+t);
				BlogHelper.animateMouse(robot, scpd.shapePanel, t, false, 0);
				beat(120);
			}
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
			
			
			Thread.sleep(1000);
		}
		
		BlogHelper.animateMouse(robot, scpd.clearButton, true, true);

		//return to start:
		BlogHelper.animateMouse(robot, scpd.createButton, true, false);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch(Throwable t) {
					t.printStackTrace();
				}
				frame.setVisible(false);
			}
		});
		
		return screenCapture.stop(true);
	}
	
	private static AnimationReader createMouseSmoothingAnimation(final Robot robot,File stillImage) throws Exception {
		final ShapeCreationPanelDemo scpd = new ShapeCreationPanelDemo();
		final JFrame frame = new JFrame();
		
		//the mouse data used to create the face demo:
		long[][] data = new long[][] {
				{ MouseEvent.MOUSE_MOVED, 490, 283, 1395933329662L },
				{ MouseEvent.MOUSE_MOVED, 456, 293, 1395933329678L },
				{ MouseEvent.MOUSE_MOVED, 418, 309, 1395933329695L },
				{ MouseEvent.MOUSE_MOVED, 393, 319, 1395933329711L },
				{ MouseEvent.MOUSE_MOVED, 367, 330, 1395933329728L },
				{ MouseEvent.MOUSE_MOVED, 353, 335, 1395933329745L },
				{ MouseEvent.MOUSE_MOVED, 342, 338, 1395933329762L },
				{ MouseEvent.MOUSE_MOVED, 336, 338, 1395933329778L },
				{ MouseEvent.MOUSE_MOVED, 331, 336, 1395933329796L },
				{ MouseEvent.MOUSE_MOVED, 329, 336, 1395933329962L },
				{ MouseEvent.MOUSE_MOVED, 325, 338, 1395933329979L },
				{ MouseEvent.MOUSE_MOVED, 321, 340, 1395933329995L },
				{ MouseEvent.MOUSE_MOVED, 317, 341, 1395933330013L },
				{ MouseEvent.MOUSE_MOVED, 309, 343, 1395933330029L },
				{ MouseEvent.MOUSE_MOVED, 301, 346, 1395933330046L },
				{ MouseEvent.MOUSE_MOVED, 293, 349, 1395933330063L },
				{ MouseEvent.MOUSE_MOVED, 284, 355, 1395933330079L },
				{ MouseEvent.MOUSE_MOVED, 277, 362, 1395933330096L },
				{ MouseEvent.MOUSE_MOVED, 273, 367, 1395933330112L },
				{ MouseEvent.MOUSE_MOVED, 270, 370, 1395933330129L },
				{ MouseEvent.MOUSE_MOVED, 268, 370, 1395933330363L },
				{ MouseEvent.MOUSE_MOVED, 266, 370, 1395933330380L },
				{ MouseEvent.MOUSE_MOVED, 261, 370, 1395933330397L },
				{ MouseEvent.MOUSE_MOVED, 250, 373, 1395933330413L },
				{ MouseEvent.MOUSE_MOVED, 239, 374, 1395933330430L },
				{ MouseEvent.MOUSE_MOVED, 232, 375, 1395933330447L },
				{ MouseEvent.MOUSE_MOVED, 225, 376, 1395933330463L },
				{ MouseEvent.MOUSE_PRESSED, 225, 376, 1395933330464L },
				{ MouseEvent.MOUSE_DRAGGED, 221, 377, 1395933330480L },
				{ MouseEvent.MOUSE_DRAGGED, 220, 377, 1395933330597L },
				{ MouseEvent.MOUSE_DRAGGED, 218, 377, 1395933330614L },
				{ MouseEvent.MOUSE_DRAGGED, 214, 377, 1395933330631L },
				{ MouseEvent.MOUSE_DRAGGED, 209, 377, 1395933330647L },
				{ MouseEvent.MOUSE_DRAGGED, 198, 377, 1395933330664L },
				{ MouseEvent.MOUSE_DRAGGED, 187, 376, 1395933330681L },
				{ MouseEvent.MOUSE_DRAGGED, 174, 369, 1395933330697L },
				{ MouseEvent.MOUSE_DRAGGED, 159, 357, 1395933330714L },
				{ MouseEvent.MOUSE_DRAGGED, 146, 347, 1395933330731L },
				{ MouseEvent.MOUSE_DRAGGED, 135, 336, 1395933330748L },
				{ MouseEvent.MOUSE_DRAGGED, 126, 324, 1395933330765L },
				{ MouseEvent.MOUSE_DRAGGED, 119, 310, 1395933330781L },
				{ MouseEvent.MOUSE_DRAGGED, 114, 294, 1395933330798L },
				{ MouseEvent.MOUSE_DRAGGED, 111, 281, 1395933330815L },
				{ MouseEvent.MOUSE_DRAGGED, 109, 256, 1395933330831L },
				{ MouseEvent.MOUSE_DRAGGED, 109, 243, 1395933330848L },
				{ MouseEvent.MOUSE_DRAGGED, 109, 229, 1395933330864L },
				{ MouseEvent.MOUSE_DRAGGED, 110, 217, 1395933330881L },
				{ MouseEvent.MOUSE_DRAGGED, 117, 205, 1395933330898L },
				{ MouseEvent.MOUSE_DRAGGED, 130, 192, 1395933330915L },
				{ MouseEvent.MOUSE_DRAGGED, 146, 180, 1395933330931L },
				{ MouseEvent.MOUSE_DRAGGED, 176, 164, 1395933330948L },
				{ MouseEvent.MOUSE_DRAGGED, 196, 156, 1395933330965L },
				{ MouseEvent.MOUSE_DRAGGED, 225, 148, 1395933330982L },
				{ MouseEvent.MOUSE_DRAGGED, 252, 145, 1395933330998L },
				{ MouseEvent.MOUSE_DRAGGED, 270, 145, 1395933331015L },
				{ MouseEvent.MOUSE_DRAGGED, 288, 145, 1395933331032L },
				{ MouseEvent.MOUSE_DRAGGED, 306, 148, 1395933331049L },
				{ MouseEvent.MOUSE_DRAGGED, 317, 156, 1395933331065L },
				{ MouseEvent.MOUSE_DRAGGED, 327, 167, 1395933331082L },
				{ MouseEvent.MOUSE_DRAGGED, 337, 181, 1395933331098L },
				{ MouseEvent.MOUSE_DRAGGED, 347, 202, 1395933331115L },
				{ MouseEvent.MOUSE_DRAGGED, 354, 221, 1395933331132L },
				{ MouseEvent.MOUSE_DRAGGED, 359, 234, 1395933331149L },
				{ MouseEvent.MOUSE_DRAGGED, 362, 247, 1395933331165L },
				{ MouseEvent.MOUSE_DRAGGED, 364, 258, 1395933331182L },
				{ MouseEvent.MOUSE_DRAGGED, 365, 268, 1395933331199L },
				{ MouseEvent.MOUSE_DRAGGED, 365, 280, 1395933331215L },
				{ MouseEvent.MOUSE_DRAGGED, 365, 291, 1395933331233L },
				{ MouseEvent.MOUSE_DRAGGED, 363, 305, 1395933331249L },
				{ MouseEvent.MOUSE_DRAGGED, 359, 318, 1395933331266L },
				{ MouseEvent.MOUSE_DRAGGED, 354, 330, 1395933331283L },
				{ MouseEvent.MOUSE_DRAGGED, 349, 340, 1395933331299L },
				{ MouseEvent.MOUSE_DRAGGED, 343, 351, 1395933331316L },
				{ MouseEvent.MOUSE_DRAGGED, 338, 358, 1395933331333L },
				{ MouseEvent.MOUSE_DRAGGED, 332, 366, 1395933331350L },
				{ MouseEvent.MOUSE_DRAGGED, 328, 371, 1395933331366L },
				{ MouseEvent.MOUSE_DRAGGED, 321, 376, 1395933331382L },
				{ MouseEvent.MOUSE_DRAGGED, 316, 379, 1395933331399L },
				{ MouseEvent.MOUSE_DRAGGED, 309, 382, 1395933331416L },
				{ MouseEvent.MOUSE_DRAGGED, 303, 384, 1395933331433L },
				{ MouseEvent.MOUSE_DRAGGED, 295, 385, 1395933331449L },
				{ MouseEvent.MOUSE_DRAGGED, 288, 386, 1395933331466L },
				{ MouseEvent.MOUSE_DRAGGED, 281, 386, 1395933331483L },
				{ MouseEvent.MOUSE_DRAGGED, 275, 386, 1395933331500L },
				{ MouseEvent.MOUSE_DRAGGED, 271, 386, 1395933331516L },
				{ MouseEvent.MOUSE_DRAGGED, 265, 386, 1395933331533L },
				{ MouseEvent.MOUSE_DRAGGED, 262, 386, 1395933331550L },
				{ MouseEvent.MOUSE_DRAGGED, 259, 385, 1395933331566L },
				{ MouseEvent.MOUSE_DRAGGED, 257, 384, 1395933331590L },
				{ MouseEvent.MOUSE_DRAGGED, 252, 382, 1395933331616L },
				{ MouseEvent.MOUSE_DRAGGED, 250, 379, 1395933331633L },
				{ MouseEvent.MOUSE_DRAGGED, 248, 377, 1395933331650L },
				{ MouseEvent.MOUSE_DRAGGED, 245, 374, 1395933331667L },
				{ MouseEvent.MOUSE_DRAGGED, 244, 373, 1395933331683L },
				{ MouseEvent.MOUSE_RELEASED, 244, 373, 1395933331693L },
				{ MouseEvent.MOUSE_MOVED, 244, 372, 1395933331734L },
				{ MouseEvent.MOUSE_MOVED, 244, 370, 1395933331750L },
				{ MouseEvent.MOUSE_MOVED, 244, 366, 1395933331767L },
				{ MouseEvent.MOUSE_MOVED, 243, 360, 1395933331784L },
				{ MouseEvent.MOUSE_MOVED, 243, 352, 1395933331801L },
				{ MouseEvent.MOUSE_MOVED, 243, 344, 1395933331817L },
				{ MouseEvent.MOUSE_MOVED, 243, 334, 1395933331834L },
				{ MouseEvent.MOUSE_MOVED, 243, 317, 1395933331850L },
				{ MouseEvent.MOUSE_MOVED, 243, 305, 1395933331867L },
				{ MouseEvent.MOUSE_MOVED, 243, 294, 1395933331884L },
				{ MouseEvent.MOUSE_MOVED, 243, 285, 1395933331901L },
				{ MouseEvent.MOUSE_MOVED, 243, 279, 1395933331917L },
				{ MouseEvent.MOUSE_MOVED, 243, 274, 1395933331934L },
				{ MouseEvent.MOUSE_MOVED, 243, 272, 1395933331951L },
				{ MouseEvent.MOUSE_MOVED, 243, 269, 1395933331967L },
				{ MouseEvent.MOUSE_MOVED, 243, 267, 1395933331984L },
				{ MouseEvent.MOUSE_MOVED, 243, 265, 1395933332001L },
				{ MouseEvent.MOUSE_MOVED, 243, 263, 1395933332018L },
				{ MouseEvent.MOUSE_MOVED, 243, 262, 1395933332034L },
				{ MouseEvent.MOUSE_MOVED, 243, 261, 1395933332068L },
				{ MouseEvent.MOUSE_MOVED, 243, 260, 1395933332085L },
				{ MouseEvent.MOUSE_MOVED, 243, 259, 1395933332101L },
				{ MouseEvent.MOUSE_MOVED, 242, 258, 1395933332118L },
				{ MouseEvent.MOUSE_MOVED, 241, 257, 1395933332135L },
				{ MouseEvent.MOUSE_MOVED, 241, 256, 1395933332152L },
				{ MouseEvent.MOUSE_MOVED, 240, 255, 1395933332268L },
				{ MouseEvent.MOUSE_MOVED, 240, 253, 1395933332286L },
				{ MouseEvent.MOUSE_MOVED, 239, 251, 1395933332302L },
				{ MouseEvent.MOUSE_MOVED, 239, 248, 1395933332319L },
				{ MouseEvent.MOUSE_MOVED, 239, 247, 1395933332335L },
				{ MouseEvent.MOUSE_MOVED, 238, 245, 1395933332352L },
				{ MouseEvent.MOUSE_MOVED, 238, 244, 1395933332369L },
				{ MouseEvent.MOUSE_MOVED, 238, 243, 1395933332385L },
				{ MouseEvent.MOUSE_MOVED, 238, 241, 1395933332402L },
				{ MouseEvent.MOUSE_MOVED, 238, 240, 1395933332419L },
				{ MouseEvent.MOUSE_MOVED, 238, 239, 1395933332435L },
				{ MouseEvent.MOUSE_MOVED, 238, 238, 1395933332452L },
				{ MouseEvent.MOUSE_MOVED, 238, 237, 1395933332469L },
				{ MouseEvent.MOUSE_PRESSED, 238, 237, 1395933332524L },
				{ MouseEvent.MOUSE_DRAGGED, 240, 237, 1395933332603L },
				{ MouseEvent.MOUSE_DRAGGED, 241, 239, 1395933332619L },
				{ MouseEvent.MOUSE_DRAGGED, 244, 241, 1395933332636L },
				{ MouseEvent.MOUSE_DRAGGED, 249, 246, 1395933332653L },
				{ MouseEvent.MOUSE_DRAGGED, 254, 251, 1395933332669L },
				{ MouseEvent.MOUSE_DRAGGED, 259, 258, 1395933332686L },
				{ MouseEvent.MOUSE_DRAGGED, 262, 266, 1395933332703L },
				{ MouseEvent.MOUSE_DRAGGED, 264, 271, 1395933332720L },
				{ MouseEvent.MOUSE_DRAGGED, 265, 275, 1395933332736L },
				{ MouseEvent.MOUSE_DRAGGED, 267, 279, 1395933332753L },
				{ MouseEvent.MOUSE_DRAGGED, 267, 281, 1395933332770L },
				{ MouseEvent.MOUSE_DRAGGED, 267, 283, 1395933332786L },
				{ MouseEvent.MOUSE_DRAGGED, 268, 285, 1395933332803L },
				{ MouseEvent.MOUSE_DRAGGED, 268, 286, 1395933332820L },
				{ MouseEvent.MOUSE_DRAGGED, 269, 288, 1395933332837L },
				{ MouseEvent.MOUSE_DRAGGED, 269, 290, 1395933332853L },
				{ MouseEvent.MOUSE_DRAGGED, 269, 293, 1395933332870L },
				{ MouseEvent.MOUSE_DRAGGED, 269, 296, 1395933332887L },
				{ MouseEvent.MOUSE_DRAGGED, 269, 299, 1395933332903L },
				{ MouseEvent.MOUSE_DRAGGED, 269, 301, 1395933332920L },
				{ MouseEvent.MOUSE_DRAGGED, 269, 302, 1395933332937L },
				{ MouseEvent.MOUSE_DRAGGED, 266, 304, 1395933332954L },
				{ MouseEvent.MOUSE_DRAGGED, 263, 306, 1395933332970L },
				{ MouseEvent.MOUSE_DRAGGED, 260, 307, 1395933332987L },
				{ MouseEvent.MOUSE_DRAGGED, 255, 309, 1395933333004L },
				{ MouseEvent.MOUSE_DRAGGED, 251, 310, 1395933333021L },
				{ MouseEvent.MOUSE_DRAGGED, 246, 311, 1395933333037L },
				{ MouseEvent.MOUSE_DRAGGED, 243, 311, 1395933333054L },
				{ MouseEvent.MOUSE_DRAGGED, 240, 313, 1395933333070L },
				{ MouseEvent.MOUSE_DRAGGED, 238, 314, 1395933333087L },
				{ MouseEvent.MOUSE_DRAGGED, 236, 314, 1395933333104L },
				{ MouseEvent.MOUSE_DRAGGED, 235, 314, 1395933333121L },
				{ MouseEvent.MOUSE_DRAGGED, 234, 314, 1395933333137L },
				{ MouseEvent.MOUSE_DRAGGED, 233, 314, 1395933333154L },
				{ MouseEvent.MOUSE_DRAGGED, 232, 314, 1395933333171L },
				{ MouseEvent.MOUSE_DRAGGED, 231, 314, 1395933333221L },
				{ MouseEvent.MOUSE_RELEASED, 231, 314, 1395933333268L },
				{ MouseEvent.MOUSE_MOVED, 231, 312, 1395933333321L },
				{ MouseEvent.MOUSE_MOVED, 231, 307, 1395933333339L },
				{ MouseEvent.MOUSE_MOVED, 230, 300, 1395933333355L },
				{ MouseEvent.MOUSE_MOVED, 227, 292, 1395933333372L },
				{ MouseEvent.MOUSE_MOVED, 224, 284, 1395933333388L },
				{ MouseEvent.MOUSE_MOVED, 221, 278, 1395933333405L },
				{ MouseEvent.MOUSE_MOVED, 220, 275, 1395933333422L },
				{ MouseEvent.MOUSE_MOVED, 219, 272, 1395933333438L },
				{ MouseEvent.MOUSE_MOVED, 218, 270, 1395933333455L },
				{ MouseEvent.MOUSE_MOVED, 217, 269, 1395933333472L },
				{ MouseEvent.MOUSE_MOVED, 216, 269, 1395933333488L },
				{ MouseEvent.MOUSE_MOVED, 215, 268, 1395933333505L },
				{ MouseEvent.MOUSE_MOVED, 214, 267, 1395933333522L },
				{ MouseEvent.MOUSE_MOVED, 213, 267, 1395933333539L },
				{ MouseEvent.MOUSE_MOVED, 212, 266, 1395933333555L },
				{ MouseEvent.MOUSE_MOVED, 210, 266, 1395933333572L },
				{ MouseEvent.MOUSE_MOVED, 209, 265, 1395933333589L },
				{ MouseEvent.MOUSE_MOVED, 207, 265, 1395933333606L },
				{ MouseEvent.MOUSE_MOVED, 204, 265, 1395933333622L },
				{ MouseEvent.MOUSE_MOVED, 204, 265, 1395933333639L },
				{ MouseEvent.MOUSE_MOVED, 203, 265, 1395933333672L },
				{ MouseEvent.MOUSE_MOVED, 202, 265, 1395933333689L },
				{ MouseEvent.MOUSE_MOVED, 201, 265, 1395933333706L },
				{ MouseEvent.MOUSE_MOVED, 200, 265, 1395933333723L },
				{ MouseEvent.MOUSE_MOVED, 199, 266, 1395933333739L },
				{ MouseEvent.MOUSE_MOVED, 198, 266, 1395933333756L },
				{ MouseEvent.MOUSE_MOVED, 196, 266, 1395933333773L },
				{ MouseEvent.MOUSE_MOVED, 195, 266, 1395933333789L },
				{ MouseEvent.MOUSE_MOVED, 193, 265, 1395933333806L },
				{ MouseEvent.MOUSE_MOVED, 191, 264, 1395933333823L },
				{ MouseEvent.MOUSE_MOVED, 187, 263, 1395933333839L },
				{ MouseEvent.MOUSE_MOVED, 185, 262, 1395933333856L },
				{ MouseEvent.MOUSE_MOVED, 184, 262, 1395933333890L },
				{ MouseEvent.MOUSE_PRESSED, 184, 262, 1395933334110L },
				{ MouseEvent.MOUSE_DRAGGED, 184, 261, 1395933334223L },
				{ MouseEvent.MOUSE_DRAGGED, 184, 259, 1395933334240L },
				{ MouseEvent.MOUSE_DRAGGED, 184, 256, 1395933334257L },
				{ MouseEvent.MOUSE_DRAGGED, 184, 253, 1395933334274L },
				{ MouseEvent.MOUSE_DRAGGED, 185, 250, 1395933334291L },
				{ MouseEvent.MOUSE_DRAGGED, 185, 247, 1395933334307L },
				{ MouseEvent.MOUSE_DRAGGED, 186, 245, 1395933334324L },
				{ MouseEvent.MOUSE_DRAGGED, 186, 243, 1395933334341L },
				{ MouseEvent.MOUSE_DRAGGED, 187, 241, 1395933334357L },
				{ MouseEvent.MOUSE_DRAGGED, 189, 239, 1395933334374L },
				{ MouseEvent.MOUSE_DRAGGED, 190, 236, 1395933334391L },
				{ MouseEvent.MOUSE_DRAGGED, 191, 234, 1395933334407L },
				{ MouseEvent.MOUSE_DRAGGED, 192, 232, 1395933334425L },
				{ MouseEvent.MOUSE_DRAGGED, 193, 232, 1395933334441L },
				{ MouseEvent.MOUSE_DRAGGED, 194, 231, 1395933334525L },
				{ MouseEvent.MOUSE_DRAGGED, 195, 231, 1395933334558L },
				{ MouseEvent.MOUSE_DRAGGED, 196, 231, 1395933334575L },
				{ MouseEvent.MOUSE_DRAGGED, 198, 231, 1395933334591L },
				{ MouseEvent.MOUSE_DRAGGED, 201, 232, 1395933334608L },
				{ MouseEvent.MOUSE_DRAGGED, 203, 234, 1395933334625L },
				{ MouseEvent.MOUSE_DRAGGED, 206, 236, 1395933334642L },
				{ MouseEvent.MOUSE_DRAGGED, 209, 240, 1395933334658L },
				{ MouseEvent.MOUSE_DRAGGED, 211, 243, 1395933334675L },
				{ MouseEvent.MOUSE_DRAGGED, 213, 245, 1395933334692L },
				{ MouseEvent.MOUSE_DRAGGED, 215, 247, 1395933334708L },
				{ MouseEvent.MOUSE_DRAGGED, 216, 249, 1395933334725L },
				{ MouseEvent.MOUSE_DRAGGED, 217, 250, 1395933334742L },
				{ MouseEvent.MOUSE_DRAGGED, 217, 251, 1395933334776L },
				{ MouseEvent.MOUSE_RELEASED, 217, 251, 1395933334807L },
				{ MouseEvent.MOUSE_MOVED, 218, 251, 1395933334808L },
				{ MouseEvent.MOUSE_MOVED, 219, 251, 1395933334858L },
				{ MouseEvent.MOUSE_MOVED, 222, 251, 1395933334876L },
				{ MouseEvent.MOUSE_MOVED, 228, 248, 1395933334892L },
				{ MouseEvent.MOUSE_MOVED, 238, 246, 1395933334909L },
				{ MouseEvent.MOUSE_MOVED, 250, 245, 1395933334925L },
				{ MouseEvent.MOUSE_MOVED, 260, 245, 1395933334943L },
				{ MouseEvent.MOUSE_MOVED, 266, 245, 1395933334959L },
				{ MouseEvent.MOUSE_MOVED, 269, 245, 1395933334976L },
				{ MouseEvent.MOUSE_MOVED, 271, 245, 1395933334993L },
				{ MouseEvent.MOUSE_PRESSED, 271, 245, 1395933335328L },
				{ MouseEvent.MOUSE_DRAGGED, 271, 244, 1395933335444L },
				{ MouseEvent.MOUSE_DRAGGED, 271, 242, 1395933335461L },
				{ MouseEvent.MOUSE_DRAGGED, 272, 236, 1395933335477L },
				{ MouseEvent.MOUSE_DRAGGED, 272, 234, 1395933335494L },
				{ MouseEvent.MOUSE_DRAGGED, 273, 230, 1395933335510L },
				{ MouseEvent.MOUSE_DRAGGED, 273, 228, 1395933335527L },
				{ MouseEvent.MOUSE_DRAGGED, 274, 226, 1395933335544L },
				{ MouseEvent.MOUSE_DRAGGED, 275, 225, 1395933335560L },
				{ MouseEvent.MOUSE_DRAGGED, 276, 225, 1395933335577L },
				{ MouseEvent.MOUSE_DRAGGED, 276, 224, 1395933335594L },
				{ MouseEvent.MOUSE_DRAGGED, 278, 224, 1395933335611L },
				{ MouseEvent.MOUSE_DRAGGED, 279, 224, 1395933335627L },
				{ MouseEvent.MOUSE_DRAGGED, 280, 224, 1395933335644L },
				{ MouseEvent.MOUSE_DRAGGED, 281, 224, 1395933335661L },
				{ MouseEvent.MOUSE_DRAGGED, 283, 225, 1395933335678L },
				{ MouseEvent.MOUSE_DRAGGED, 284, 228, 1395933335694L },
				{ MouseEvent.MOUSE_DRAGGED, 286, 232, 1395933335711L },
				{ MouseEvent.MOUSE_DRAGGED, 288, 236, 1395933335728L },
				{ MouseEvent.MOUSE_DRAGGED, 290, 240, 1395933335745L },
				{ MouseEvent.MOUSE_DRAGGED, 292, 244, 1395933335761L },
				{ MouseEvent.MOUSE_DRAGGED, 293, 246, 1395933335778L },
				{ MouseEvent.MOUSE_DRAGGED, 293, 249, 1395933335794L },
				{ MouseEvent.MOUSE_DRAGGED, 294, 250, 1395933335811L },
				{ MouseEvent.MOUSE_DRAGGED, 294, 250, 1395933335828L },
				{ MouseEvent.MOUSE_RELEASED, 294, 250, 1395933335880L },
				{ MouseEvent.MOUSE_MOVED, 294, 251, 1395933335961L },
				{ MouseEvent.MOUSE_MOVED, 294, 253, 1395933335979L },
				{ MouseEvent.MOUSE_MOVED, 290, 263, 1395933335995L },
				{ MouseEvent.MOUSE_MOVED, 284, 277, 1395933336012L },
				{ MouseEvent.MOUSE_MOVED, 275, 297, 1395933336028L },
				{ MouseEvent.MOUSE_MOVED, 267, 321, 1395933336046L },
				{ MouseEvent.MOUSE_MOVED, 255, 342, 1395933336062L },
				{ MouseEvent.MOUSE_MOVED, 249, 355, 1395933336079L },
				{ MouseEvent.MOUSE_MOVED, 241, 368, 1395933336095L },
				{ MouseEvent.MOUSE_MOVED, 237, 373, 1395933336112L },
				{ MouseEvent.MOUSE_MOVED, 232, 378, 1395933336129L },
				{ MouseEvent.MOUSE_MOVED, 230, 380, 1395933336145L },
				{ MouseEvent.MOUSE_MOVED, 228, 380, 1395933336162L },
				{ MouseEvent.MOUSE_MOVED, 227, 380, 1395933336179L },
				{ MouseEvent.MOUSE_MOVED, 226, 380, 1395933336196L },
				{ MouseEvent.MOUSE_MOVED, 225, 380, 1395933336212L },
				{ MouseEvent.MOUSE_MOVED, 223, 379, 1395933336229L },
				{ MouseEvent.MOUSE_MOVED, 221, 378, 1395933336246L },
				{ MouseEvent.MOUSE_MOVED, 217, 376, 1395933336262L },
				{ MouseEvent.MOUSE_MOVED, 214, 374, 1395933336279L },
				{ MouseEvent.MOUSE_MOVED, 210, 372, 1395933336296L },
				{ MouseEvent.MOUSE_MOVED, 207, 369, 1395933336312L },
				{ MouseEvent.MOUSE_MOVED, 204, 366, 1395933336329L },
				{ MouseEvent.MOUSE_MOVED, 202, 362, 1395933336346L },
				{ MouseEvent.MOUSE_MOVED, 200, 359, 1395933336363L },
				{ MouseEvent.MOUSE_MOVED, 199, 356, 1395933336380L },
				{ MouseEvent.MOUSE_MOVED, 198, 354, 1395933336396L },
				{ MouseEvent.MOUSE_MOVED, 197, 352, 1395933336413L },
				{ MouseEvent.MOUSE_MOVED, 197, 350, 1395933336429L },
				{ MouseEvent.MOUSE_MOVED, 197, 349, 1395933336446L },
				{ MouseEvent.MOUSE_MOVED, 197, 347, 1395933336463L },
				{ MouseEvent.MOUSE_MOVED, 197, 346, 1395933336480L },
				{ MouseEvent.MOUSE_MOVED, 197, 346, 1395933336496L },
				{ MouseEvent.MOUSE_MOVED, 196, 345, 1395933336747L },
				{ MouseEvent.MOUSE_MOVED, 196, 344, 1395933336764L },
				{ MouseEvent.MOUSE_MOVED, 196, 340, 1395933336780L },
				{ MouseEvent.MOUSE_MOVED, 194, 337, 1395933336798L },
				{ MouseEvent.MOUSE_MOVED, 194, 333, 1395933336814L },
				{ MouseEvent.MOUSE_MOVED, 193, 330, 1395933336831L },
				{ MouseEvent.MOUSE_MOVED, 192, 328, 1395933336848L },
				{ MouseEvent.MOUSE_MOVED, 192, 326, 1395933336881L },
				{ MouseEvent.MOUSE_PRESSED, 192, 326, 1395933336946L },
				{ MouseEvent.MOUSE_DRAGGED, 193, 327, 1395933337098L },
				{ MouseEvent.MOUSE_DRAGGED, 195, 328, 1395933337115L },
				{ MouseEvent.MOUSE_DRAGGED, 199, 331, 1395933337131L },
				{ MouseEvent.MOUSE_DRAGGED, 204, 333, 1395933337148L },
				{ MouseEvent.MOUSE_DRAGGED, 208, 336, 1395933337165L },
				{ MouseEvent.MOUSE_DRAGGED, 213, 339, 1395933337182L },
				{ MouseEvent.MOUSE_DRAGGED, 217, 341, 1395933337199L },
				{ MouseEvent.MOUSE_DRAGGED, 220, 344, 1395933337216L },
				{ MouseEvent.MOUSE_DRAGGED, 224, 346, 1395933337232L },
				{ MouseEvent.MOUSE_DRAGGED, 227, 349, 1395933337249L },
				{ MouseEvent.MOUSE_DRAGGED, 231, 350, 1395933337265L },
				{ MouseEvent.MOUSE_DRAGGED, 236, 351, 1395933337282L },
				{ MouseEvent.MOUSE_DRAGGED, 240, 352, 1395933337298L },
				{ MouseEvent.MOUSE_DRAGGED, 245, 352, 1395933337315L },
				{ MouseEvent.MOUSE_DRAGGED, 249, 352, 1395933337332L },
				{ MouseEvent.MOUSE_DRAGGED, 252, 352, 1395933337349L },
				{ MouseEvent.MOUSE_DRAGGED, 255, 352, 1395933337366L },
				{ MouseEvent.MOUSE_DRAGGED, 258, 352, 1395933337382L },
				{ MouseEvent.MOUSE_DRAGGED, 261, 352, 1395933337399L },
				{ MouseEvent.MOUSE_DRAGGED, 264, 352, 1395933337416L },
				{ MouseEvent.MOUSE_DRAGGED, 270, 351, 1395933337433L },
				{ MouseEvent.MOUSE_DRAGGED, 274, 349, 1395933337449L },
				{ MouseEvent.MOUSE_DRAGGED, 278, 347, 1395933337466L },
				{ MouseEvent.MOUSE_DRAGGED, 281, 345, 1395933337483L },
				{ MouseEvent.MOUSE_DRAGGED, 285, 343, 1395933337499L },
				{ MouseEvent.MOUSE_DRAGGED, 289, 341, 1395933337516L },
				{ MouseEvent.MOUSE_DRAGGED, 293, 339, 1395933337533L },
				{ MouseEvent.MOUSE_DRAGGED, 296, 337, 1395933337549L },
				{ MouseEvent.MOUSE_DRAGGED, 298, 336, 1395933337567L },
				{ MouseEvent.MOUSE_DRAGGED, 300, 335, 1395933337583L },
				{ MouseEvent.MOUSE_DRAGGED, 301, 334, 1395933337599L },
				{ MouseEvent.MOUSE_DRAGGED, 302, 332, 1395933337616L },
				{ MouseEvent.MOUSE_DRAGGED, 303, 332, 1395933337633L },
				{ MouseEvent.MOUSE_DRAGGED, 304, 330, 1395933337650L },
				{ MouseEvent.MOUSE_RELEASED, 304, 330, 1395933337659L },
				{ MouseEvent.MOUSE_MOVED, 304, 330, 1395933337666L },
				{ MouseEvent.MOUSE_MOVED, 304, 329, 1395933337716L },
				{ MouseEvent.MOUSE_MOVED, 303, 328, 1395933337784L },
				{ MouseEvent.MOUSE_MOVED, 297, 321, 1395933337800L },
				{ MouseEvent.MOUSE_MOVED, 281, 309, 1395933337817L },
				{ MouseEvent.MOUSE_MOVED, 261, 293, 1395933337834L },
				{ MouseEvent.MOUSE_MOVED, 237, 272, 1395933337850L },
				{ MouseEvent.MOUSE_MOVED, 221, 256, 1395933337867L },
				{ MouseEvent.MOUSE_MOVED, 207, 243, 1395933337883L },
				{ MouseEvent.MOUSE_MOVED, 200, 236, 1395933337900L },
				{ MouseEvent.MOUSE_MOVED, 195, 232, 1395933337917L },
				{ MouseEvent.MOUSE_MOVED, 193, 230, 1395933337934L },
				{ MouseEvent.MOUSE_MOVED, 192, 229, 1395933337951L },
				{ MouseEvent.MOUSE_MOVED, 191, 227, 1395933337968L },
				{ MouseEvent.MOUSE_MOVED, 190, 227, 1395933338151L },
				{ MouseEvent.MOUSE_MOVED, 184, 227, 1395933338168L },
				{ MouseEvent.MOUSE_MOVED, 178, 225, 1395933338184L },
				{ MouseEvent.MOUSE_MOVED, 170, 221, 1395933338201L },
				{ MouseEvent.MOUSE_MOVED, 162, 216, 1395933338218L },
				{ MouseEvent.MOUSE_MOVED, 157, 213, 1395933338235L },
				{ MouseEvent.MOUSE_MOVED, 154, 211, 1395933338251L },
				{ MouseEvent.MOUSE_MOVED, 152, 210, 1395933338268L },
				{ MouseEvent.MOUSE_MOVED, 152, 208, 1395933338318L },
				{ MouseEvent.MOUSE_MOVED, 151, 208, 1395933338418L },
				{ MouseEvent.MOUSE_PRESSED, 151, 208, 1395933338436L },
				{ MouseEvent.MOUSE_DRAGGED, 151, 207, 1395933338519L },
				{ MouseEvent.MOUSE_DRAGGED, 151, 205, 1395933338535L },
				{ MouseEvent.MOUSE_DRAGGED, 150, 204, 1395933338552L },
				{ MouseEvent.MOUSE_DRAGGED, 149, 200, 1395933338569L },
				{ MouseEvent.MOUSE_DRAGGED, 149, 195, 1395933338585L },
				{ MouseEvent.MOUSE_DRAGGED, 149, 192, 1395933338602L },
				{ MouseEvent.MOUSE_DRAGGED, 149, 186, 1395933338619L },
				{ MouseEvent.MOUSE_DRAGGED, 149, 183, 1395933338636L },
				{ MouseEvent.MOUSE_DRAGGED, 149, 180, 1395933338652L },
				{ MouseEvent.MOUSE_DRAGGED, 149, 178, 1395933338669L },
				{ MouseEvent.MOUSE_DRAGGED, 150, 176, 1395933338686L },
				{ MouseEvent.MOUSE_DRAGGED, 153, 176, 1395933338702L },
				{ MouseEvent.MOUSE_DRAGGED, 159, 176, 1395933338719L },
				{ MouseEvent.MOUSE_DRAGGED, 166, 176, 1395933338736L },
				{ MouseEvent.MOUSE_DRAGGED, 174, 178, 1395933338752L },
				{ MouseEvent.MOUSE_DRAGGED, 181, 183, 1395933338769L },
				{ MouseEvent.MOUSE_DRAGGED, 185, 187, 1395933338786L },
				{ MouseEvent.MOUSE_DRAGGED, 188, 190, 1395933338803L },
				{ MouseEvent.MOUSE_DRAGGED, 190, 192, 1395933338820L },
				{ MouseEvent.MOUSE_DRAGGED, 191, 192, 1395933338836L },
				{ MouseEvent.MOUSE_DRAGGED, 191, 190, 1395933338870L },
				{ MouseEvent.MOUSE_DRAGGED, 191, 184, 1395933338886L },
				{ MouseEvent.MOUSE_DRAGGED, 192, 177, 1395933338903L },
				{ MouseEvent.MOUSE_DRAGGED, 193, 172, 1395933338919L },
				{ MouseEvent.MOUSE_DRAGGED, 194, 168, 1395933338937L },
				{ MouseEvent.MOUSE_DRAGGED, 195, 164, 1395933338953L },
				{ MouseEvent.MOUSE_DRAGGED, 197, 164, 1395933338970L },
				{ MouseEvent.MOUSE_DRAGGED, 198, 163, 1395933338987L },
				{ MouseEvent.MOUSE_DRAGGED, 201, 163, 1395933339003L },
				{ MouseEvent.MOUSE_DRAGGED, 205, 163, 1395933339020L },
				{ MouseEvent.MOUSE_DRAGGED, 208, 164, 1395933339037L },
				{ MouseEvent.MOUSE_DRAGGED, 212, 165, 1395933339053L },
				{ MouseEvent.MOUSE_DRAGGED, 215, 167, 1395933339087L },
				{ MouseEvent.MOUSE_DRAGGED, 216, 167, 1395933339104L },
				{ MouseEvent.MOUSE_DRAGGED, 216, 165, 1395933339121L },
				{ MouseEvent.MOUSE_DRAGGED, 217, 157, 1395933339137L },
				{ MouseEvent.MOUSE_DRAGGED, 219, 149, 1395933339154L },
				{ MouseEvent.MOUSE_DRAGGED, 220, 143, 1395933339171L },
				{ MouseEvent.MOUSE_DRAGGED, 221, 139, 1395933339188L },
				{ MouseEvent.MOUSE_DRAGGED, 222, 137, 1395933339204L },
				{ MouseEvent.MOUSE_DRAGGED, 224, 137, 1395933339237L },
				{ MouseEvent.MOUSE_DRAGGED, 227, 137, 1395933339254L },
				{ MouseEvent.MOUSE_DRAGGED, 229, 137, 1395933339271L },
				{ MouseEvent.MOUSE_DRAGGED, 232, 140, 1395933339287L },
				{ MouseEvent.MOUSE_DRAGGED, 234, 143, 1395933339304L },
				{ MouseEvent.MOUSE_DRAGGED, 236, 146, 1395933339321L },
				{ MouseEvent.MOUSE_DRAGGED, 238, 147, 1395933339338L },
				{ MouseEvent.MOUSE_DRAGGED, 240, 149, 1395933339354L },
				{ MouseEvent.MOUSE_DRAGGED, 241, 149, 1395933339371L },
				{ MouseEvent.MOUSE_DRAGGED, 242, 149, 1395933339388L },
				{ MouseEvent.MOUSE_DRAGGED, 244, 149, 1395933339405L },
				{ MouseEvent.MOUSE_DRAGGED, 246, 146, 1395933339421L },
				{ MouseEvent.MOUSE_DRAGGED, 249, 140, 1395933339437L },
				{ MouseEvent.MOUSE_DRAGGED, 251, 137, 1395933339454L },
				{ MouseEvent.MOUSE_DRAGGED, 253, 135, 1395933339471L },
				{ MouseEvent.MOUSE_DRAGGED, 254, 134, 1395933339488L },
				{ MouseEvent.MOUSE_DRAGGED, 255, 134, 1395933339505L },
				{ MouseEvent.MOUSE_DRAGGED, 257, 134, 1395933339521L },
				{ MouseEvent.MOUSE_DRAGGED, 260, 135, 1395933339538L },
				{ MouseEvent.MOUSE_DRAGGED, 263, 139, 1395933339555L },
				{ MouseEvent.MOUSE_DRAGGED, 265, 144, 1395933339572L },
				{ MouseEvent.MOUSE_DRAGGED, 267, 147, 1395933339588L },
				{ MouseEvent.MOUSE_DRAGGED, 269, 150, 1395933339605L },
				{ MouseEvent.MOUSE_DRAGGED, 270, 151, 1395933339622L },
				{ MouseEvent.MOUSE_DRAGGED, 271, 152, 1395933339638L },
				{ MouseEvent.MOUSE_DRAGGED, 272, 153, 1395933339655L },
				{ MouseEvent.MOUSE_DRAGGED, 274, 153, 1395933339672L },
				{ MouseEvent.MOUSE_DRAGGED, 277, 153, 1395933339923L },
				{ MouseEvent.MOUSE_DRAGGED, 281, 151, 1395933339939L },
				{ MouseEvent.MOUSE_DRAGGED, 284, 148, 1395933339956L },
				{ MouseEvent.MOUSE_DRAGGED, 288, 145, 1395933339972L },
				{ MouseEvent.MOUSE_DRAGGED, 294, 144, 1395933339989L },
				{ MouseEvent.MOUSE_DRAGGED, 298, 143, 1395933340006L },
				{ MouseEvent.MOUSE_DRAGGED, 301, 143, 1395933340023L },
				{ MouseEvent.MOUSE_DRAGGED, 303, 143, 1395933340039L },
				{ MouseEvent.MOUSE_DRAGGED, 303, 143, 1395933340073L },
				{ MouseEvent.MOUSE_DRAGGED, 303, 146, 1395933340089L },
				{ MouseEvent.MOUSE_DRAGGED, 303, 151, 1395933340106L },
				{ MouseEvent.MOUSE_DRAGGED, 302, 156, 1395933340123L },
				{ MouseEvent.MOUSE_DRAGGED, 300, 160, 1395933340140L },
				{ MouseEvent.MOUSE_DRAGGED, 300, 163, 1395933340156L },
				{ MouseEvent.MOUSE_DRAGGED, 300, 166, 1395933340173L },
				{ MouseEvent.MOUSE_DRAGGED, 302, 167, 1395933340223L },
				{ MouseEvent.MOUSE_DRAGGED, 306, 165, 1395933340240L },
				{ MouseEvent.MOUSE_DRAGGED, 309, 164, 1395933340257L },
				{ MouseEvent.MOUSE_DRAGGED, 313, 161, 1395933340273L },
				{ MouseEvent.MOUSE_DRAGGED, 316, 159, 1395933340290L },
				{ MouseEvent.MOUSE_DRAGGED, 319, 158, 1395933340307L },
				{ MouseEvent.MOUSE_DRAGGED, 320, 158, 1395933340324L },
				{ MouseEvent.MOUSE_DRAGGED, 321, 158, 1395933340340L },
				{ MouseEvent.MOUSE_DRAGGED, 322, 158, 1395933340374L },
				{ MouseEvent.MOUSE_DRAGGED, 322, 160, 1395933340390L },
				{ MouseEvent.MOUSE_DRAGGED, 323, 162, 1395933340407L },
				{ MouseEvent.MOUSE_DRAGGED, 324, 165, 1395933340440L },
				{ MouseEvent.MOUSE_DRAGGED, 326, 166, 1395933340457L },
				{ MouseEvent.MOUSE_DRAGGED, 328, 167, 1395933340474L },
				{ MouseEvent.MOUSE_DRAGGED, 331, 167, 1395933340491L },
				{ MouseEvent.MOUSE_DRAGGED, 334, 170, 1395933340507L },
				{ MouseEvent.MOUSE_DRAGGED, 337, 171, 1395933340524L },
				{ MouseEvent.MOUSE_DRAGGED, 339, 172, 1395933340541L },
				{ MouseEvent.MOUSE_DRAGGED, 341, 172, 1395933340557L },
				{ MouseEvent.MOUSE_DRAGGED, 341, 173, 1395933340574L },
				{ MouseEvent.MOUSE_DRAGGED, 341, 175, 1395933340591L },
				{ MouseEvent.MOUSE_DRAGGED, 341, 178, 1395933340607L },
				{ MouseEvent.MOUSE_DRAGGED, 341, 180, 1395933340624L },
				{ MouseEvent.MOUSE_DRAGGED, 341, 181, 1395933340641L },
				{ MouseEvent.MOUSE_DRAGGED, 340, 183, 1395933340658L },
				{ MouseEvent.MOUSE_DRAGGED, 340, 184, 1395933340674L },
				{ MouseEvent.MOUSE_DRAGGED, 340, 185, 1395933340691L },
				{ MouseEvent.MOUSE_DRAGGED, 341, 185, 1395933340708L },
				{ MouseEvent.MOUSE_DRAGGED, 343, 186, 1395933340724L },
				{ MouseEvent.MOUSE_DRAGGED, 344, 186, 1395933340742L },
				{ MouseEvent.MOUSE_DRAGGED, 346, 187, 1395933340758L },
				{ MouseEvent.MOUSE_DRAGGED, 346, 188, 1395933340775L },
				{ MouseEvent.MOUSE_DRAGGED, 346, 189, 1395933340791L },
				{ MouseEvent.MOUSE_DRAGGED, 347, 191, 1395933340808L },
				{ MouseEvent.MOUSE_DRAGGED, 347, 192, 1395933340825L },
				{ MouseEvent.MOUSE_DRAGGED, 347, 194, 1395933340841L },
				{ MouseEvent.MOUSE_DRAGGED, 346, 196, 1395933340858L },
				{ MouseEvent.MOUSE_DRAGGED, 345, 198, 1395933340875L },
				{ MouseEvent.MOUSE_DRAGGED, 344, 199, 1395933340891L },
				{ MouseEvent.MOUSE_DRAGGED, 343, 200, 1395933340908L },
				{ MouseEvent.MOUSE_DRAGGED, 343, 201, 1395933340925L },
				{ MouseEvent.MOUSE_DRAGGED, 343, 202, 1395933340942L },
				{ MouseEvent.MOUSE_DRAGGED, 343, 203, 1395933340959L },
				{ MouseEvent.MOUSE_DRAGGED, 343, 205, 1395933340975L },
				{ MouseEvent.MOUSE_DRAGGED, 343, 207, 1395933340992L },
				{ MouseEvent.MOUSE_DRAGGED, 343, 209, 1395933341009L },
				{ MouseEvent.MOUSE_DRAGGED, 344, 210, 1395933341025L },
				{ MouseEvent.MOUSE_DRAGGED, 344, 210, 1395933341059L },
				{ MouseEvent.MOUSE_RELEASED, 344, 210, 1395933341072L },
				{ MouseEvent.MOUSE_MOVED, 329, 210, 1395933341259L },
				{ MouseEvent.MOUSE_MOVED, 322, 210, 1395933341276L },
				{ MouseEvent.MOUSE_MOVED, 310, 210, 1395933341293L },
				{ MouseEvent.MOUSE_MOVED, 295, 208, 1395933341310L },
				{ MouseEvent.MOUSE_MOVED, 271, 205, 1395933341326L },
				{ MouseEvent.MOUSE_MOVED, 235, 197, 1395933341343L },
				{ MouseEvent.MOUSE_MOVED, 194, 184, 1395933341360L },
				{ MouseEvent.MOUSE_MOVED, 154, 165, 1395933341377L },
				{ MouseEvent.MOUSE_MOVED, 128, 147, 1395933341393L },
				{ MouseEvent.MOUSE_MOVED, 108, 130, 1395933341409L },
				{ MouseEvent.MOUSE_MOVED, 99, 121, 1395933341427L },
				{ MouseEvent.MOUSE_MOVED, 89, 112, 1395933341443L },
				{ MouseEvent.MOUSE_MOVED, 86, 107, 1395933341460L },
				{ MouseEvent.MOUSE_MOVED, 84, 103, 1395933341477L },
				{ MouseEvent.MOUSE_MOVED, 83, 106, 1395933341661L },
				{ MouseEvent.MOUSE_MOVED, 83, 108, 1395933341677L },
				{ MouseEvent.MOUSE_MOVED, 83, 110, 1395933341694L },
				{ MouseEvent.MOUSE_MOVED, 87, 119, 1395933341710L },
				{ MouseEvent.MOUSE_MOVED, 91, 128, 1395933341727L },
				{ MouseEvent.MOUSE_MOVED, 98, 144, 1395933341744L },
				{ MouseEvent.MOUSE_MOVED, 106, 163, 1395933341761L },
				{ MouseEvent.MOUSE_MOVED, 112, 175, 1395933341777L },
				{ MouseEvent.MOUSE_MOVED, 118, 187, 1395933341794L },
				{ MouseEvent.MOUSE_MOVED, 121, 195, 1395933341811L },
				{ MouseEvent.MOUSE_MOVED, 123, 200, 1395933341828L },
				{ MouseEvent.MOUSE_MOVED, 124, 204, 1395933341844L },
				{ MouseEvent.MOUSE_MOVED, 124, 207, 1395933341861L },
				{ MouseEvent.MOUSE_MOVED, 125, 207, 1395933342112L },
				{ MouseEvent.MOUSE_MOVED, 128, 207, 1395933342129L },
				{ MouseEvent.MOUSE_MOVED, 131, 207, 1395933342145L },
				{ MouseEvent.MOUSE_MOVED, 132, 207, 1395933342162L },
				{ MouseEvent.MOUSE_PRESSED, 132, 207, 1395933342169L },
				{ MouseEvent.MOUSE_DRAGGED, 132, 206, 1395933342278L },
				{ MouseEvent.MOUSE_DRAGGED, 132, 202, 1395933342296L },
				{ MouseEvent.MOUSE_DRAGGED, 132, 198, 1395933342312L },
				{ MouseEvent.MOUSE_DRAGGED, 132, 192, 1395933342329L },
				{ MouseEvent.MOUSE_DRAGGED, 132, 188, 1395933342346L },
				{ MouseEvent.MOUSE_DRAGGED, 133, 185, 1395933342362L },
				{ MouseEvent.MOUSE_DRAGGED, 134, 182, 1395933342379L },
				{ MouseEvent.MOUSE_DRAGGED, 135, 180, 1395933342396L },
				{ MouseEvent.MOUSE_DRAGGED, 135, 179, 1395933342413L },
				{ MouseEvent.MOUSE_DRAGGED, 136, 179, 1395933342429L },
				{ MouseEvent.MOUSE_DRAGGED, 138, 179, 1395933342446L },
				{ MouseEvent.MOUSE_DRAGGED, 140, 180, 1395933342463L },
				{ MouseEvent.MOUSE_DRAGGED, 143, 187, 1395933342479L },
				{ MouseEvent.MOUSE_DRAGGED, 146, 191, 1395933342496L },
				{ MouseEvent.MOUSE_DRAGGED, 147, 194, 1395933342513L },
				{ MouseEvent.MOUSE_DRAGGED, 148, 196, 1395933342529L },
				{ MouseEvent.MOUSE_DRAGGED, 148, 196, 1395933342563L },
				{ MouseEvent.MOUSE_DRAGGED, 148, 189, 1395933342579L },
				{ MouseEvent.MOUSE_DRAGGED, 148, 179, 1395933342597L },
				{ MouseEvent.MOUSE_DRAGGED, 148, 170, 1395933342613L },
				{ MouseEvent.MOUSE_DRAGGED, 148, 162, 1395933342630L },
				{ MouseEvent.MOUSE_DRAGGED, 148, 157, 1395933342647L },
				{ MouseEvent.MOUSE_DRAGGED, 148, 154, 1395933342663L },
				{ MouseEvent.MOUSE_DRAGGED, 148, 153, 1395933342680L },
				{ MouseEvent.MOUSE_DRAGGED, 149, 152, 1395933342697L },
				{ MouseEvent.MOUSE_DRAGGED, 151, 152, 1395933342713L },
				{ MouseEvent.MOUSE_DRAGGED, 154, 152, 1395933342730L },
				{ MouseEvent.MOUSE_DRAGGED, 159, 154, 1395933342747L },
				{ MouseEvent.MOUSE_DRAGGED, 163, 159, 1395933342764L },
				{ MouseEvent.MOUSE_DRAGGED, 168, 165, 1395933342780L },
				{ MouseEvent.MOUSE_DRAGGED, 171, 171, 1395933342797L },
				{ MouseEvent.MOUSE_DRAGGED, 173, 176, 1395933342814L },
				{ MouseEvent.MOUSE_DRAGGED, 174, 178, 1395933342830L },
				{ MouseEvent.MOUSE_DRAGGED, 174, 180, 1395933342847L },
				{ MouseEvent.MOUSE_DRAGGED, 175, 180, 1395933342864L },
				{ MouseEvent.MOUSE_DRAGGED, 175, 179, 1395933342914L },
				{ MouseEvent.MOUSE_DRAGGED, 175, 173, 1395933342930L },
				{ MouseEvent.MOUSE_DRAGGED, 175, 166, 1395933342947L },
				{ MouseEvent.MOUSE_DRAGGED, 175, 161, 1395933342964L },
				{ MouseEvent.MOUSE_DRAGGED, 175, 157, 1395933342981L },
				{ MouseEvent.MOUSE_DRAGGED, 176, 155, 1395933342997L },
				{ MouseEvent.MOUSE_DRAGGED, 177, 154, 1395933343014L },
				{ MouseEvent.MOUSE_DRAGGED, 178, 154, 1395933343031L },
				{ MouseEvent.MOUSE_DRAGGED, 180, 154, 1395933343047L },
				{ MouseEvent.MOUSE_DRAGGED, 183, 156, 1395933343064L },
				{ MouseEvent.MOUSE_DRAGGED, 188, 161, 1395933343081L },
				{ MouseEvent.MOUSE_DRAGGED, 193, 167, 1395933343097L },
				{ MouseEvent.MOUSE_DRAGGED, 196, 171, 1395933343115L },
				{ MouseEvent.MOUSE_DRAGGED, 198, 174, 1395933343131L },
				{ MouseEvent.MOUSE_DRAGGED, 199, 174, 1395933343148L },
				{ MouseEvent.MOUSE_DRAGGED, 200, 175, 1395933343181L },
				{ MouseEvent.MOUSE_DRAGGED, 200, 172, 1395933343198L },
				{ MouseEvent.MOUSE_DRAGGED, 200, 170, 1395933343215L },
				{ MouseEvent.MOUSE_DRAGGED, 201, 163, 1395933343232L },
				{ MouseEvent.MOUSE_DRAGGED, 202, 155, 1395933343248L },
				{ MouseEvent.MOUSE_DRAGGED, 203, 145, 1395933343265L },
				{ MouseEvent.MOUSE_DRAGGED, 204, 139, 1395933343282L },
				{ MouseEvent.MOUSE_DRAGGED, 205, 134, 1395933343299L },
				{ MouseEvent.MOUSE_DRAGGED, 205, 130, 1395933343332L },
				{ MouseEvent.MOUSE_DRAGGED, 207, 130, 1395933343365L },
				{ MouseEvent.MOUSE_DRAGGED, 209, 132, 1395933343382L },
				{ MouseEvent.MOUSE_DRAGGED, 210, 135, 1395933343398L },
				{ MouseEvent.MOUSE_DRAGGED, 213, 139, 1395933343415L },
				{ MouseEvent.MOUSE_DRAGGED, 215, 142, 1395933343432L },
				{ MouseEvent.MOUSE_DRAGGED, 218, 147, 1395933343449L },
				{ MouseEvent.MOUSE_DRAGGED, 219, 151, 1395933343465L },
				{ MouseEvent.MOUSE_DRAGGED, 220, 153, 1395933343482L },
				{ MouseEvent.MOUSE_DRAGGED, 221, 154, 1395933343498L },
				{ MouseEvent.MOUSE_DRAGGED, 221, 156, 1395933343716L },
				{ MouseEvent.MOUSE_DRAGGED, 222, 158, 1395933343733L },
				{ MouseEvent.MOUSE_DRAGGED, 224, 162, 1395933343749L },
				{ MouseEvent.MOUSE_DRAGGED, 228, 167, 1395933343766L },
				{ MouseEvent.MOUSE_DRAGGED, 233, 172, 1395933343783L },
				{ MouseEvent.MOUSE_DRAGGED, 237, 176, 1395933343799L },
				{ MouseEvent.MOUSE_DRAGGED, 241, 179, 1395933343816L },
				{ MouseEvent.MOUSE_DRAGGED, 242, 180, 1395933343833L },
				{ MouseEvent.MOUSE_DRAGGED, 242, 181, 1395933343850L },
				{ MouseEvent.MOUSE_DRAGGED, 243, 181, 1395933343866L },
				{ MouseEvent.MOUSE_DRAGGED, 243, 180, 1395933343883L },
				{ MouseEvent.MOUSE_DRAGGED, 244, 175, 1395933343899L },
				{ MouseEvent.MOUSE_DRAGGED, 245, 169, 1395933343916L },
				{ MouseEvent.MOUSE_DRAGGED, 246, 165, 1395933343933L },
				{ MouseEvent.MOUSE_DRAGGED, 248, 159, 1395933343950L },
				{ MouseEvent.MOUSE_DRAGGED, 250, 155, 1395933343967L },
				{ MouseEvent.MOUSE_DRAGGED, 251, 154, 1395933343983L },
				{ MouseEvent.MOUSE_DRAGGED, 252, 152, 1395933344000L },
				{ MouseEvent.MOUSE_DRAGGED, 254, 152, 1395933344017L },
				{ MouseEvent.MOUSE_DRAGGED, 256, 152, 1395933344033L },
				{ MouseEvent.MOUSE_DRAGGED, 259, 154, 1395933344050L },
				{ MouseEvent.MOUSE_DRAGGED, 265, 165, 1395933344067L },
				{ MouseEvent.MOUSE_DRAGGED, 268, 174, 1395933344084L },
				{ MouseEvent.MOUSE_DRAGGED, 270, 182, 1395933344102L },
				{ MouseEvent.MOUSE_DRAGGED, 273, 188, 1395933344117L },
				{ MouseEvent.MOUSE_DRAGGED, 273, 191, 1395933344134L },
				{ MouseEvent.MOUSE_DRAGGED, 273, 192, 1395933344150L },
				{ MouseEvent.MOUSE_DRAGGED, 273, 191, 1395933344184L },
				{ MouseEvent.MOUSE_DRAGGED, 274, 187, 1395933344201L },
				{ MouseEvent.MOUSE_DRAGGED, 275, 175, 1395933344217L },
				{ MouseEvent.MOUSE_DRAGGED, 278, 159, 1395933344234L },
				{ MouseEvent.MOUSE_DRAGGED, 279, 152, 1395933344251L },
				{ MouseEvent.MOUSE_DRAGGED, 283, 143, 1395933344267L },
				{ MouseEvent.MOUSE_DRAGGED, 284, 139, 1395933344284L },
				{ MouseEvent.MOUSE_DRAGGED, 284, 138, 1395933344301L },
				{ MouseEvent.MOUSE_DRAGGED, 285, 138, 1395933344318L },
				{ MouseEvent.MOUSE_DRAGGED, 286, 138, 1395933344334L },
				{ MouseEvent.MOUSE_DRAGGED, 288, 142, 1395933344351L },
				{ MouseEvent.MOUSE_DRAGGED, 291, 147, 1395933344368L },
				{ MouseEvent.MOUSE_DRAGGED, 292, 151, 1395933344384L },
				{ MouseEvent.MOUSE_DRAGGED, 294, 155, 1395933344401L },
				{ MouseEvent.MOUSE_DRAGGED, 295, 158, 1395933344418L },
				{ MouseEvent.MOUSE_DRAGGED, 297, 160, 1395933344451L },
				{ MouseEvent.MOUSE_DRAGGED, 297, 161, 1395933344468L },
				{ MouseEvent.MOUSE_DRAGGED, 299, 161, 1395933344501L },
				{ MouseEvent.MOUSE_DRAGGED, 301, 158, 1395933344518L },
				{ MouseEvent.MOUSE_DRAGGED, 302, 155, 1395933344535L },
				{ MouseEvent.MOUSE_DRAGGED, 303, 153, 1395933344552L },
				{ MouseEvent.MOUSE_DRAGGED, 304, 152, 1395933344568L },
				{ MouseEvent.MOUSE_DRAGGED, 305, 152, 1395933344585L },
				{ MouseEvent.MOUSE_DRAGGED, 306, 152, 1395933344602L },
				{ MouseEvent.MOUSE_DRAGGED, 307, 152, 1395933344618L },
				{ MouseEvent.MOUSE_DRAGGED, 308, 152, 1395933344635L },
				{ MouseEvent.MOUSE_DRAGGED, 310, 154, 1395933344652L },
				{ MouseEvent.MOUSE_DRAGGED, 312, 157, 1395933344668L },
				{ MouseEvent.MOUSE_DRAGGED, 313, 159, 1395933344685L },
				{ MouseEvent.MOUSE_DRAGGED, 315, 160, 1395933344702L },
				{ MouseEvent.MOUSE_DRAGGED, 315, 162, 1395933344719L },
				{ MouseEvent.MOUSE_DRAGGED, 316, 162, 1395933344735L },
				{ MouseEvent.MOUSE_DRAGGED, 317, 163, 1395933344836L },
				{ MouseEvent.MOUSE_DRAGGED, 318, 165, 1395933344869L },
				{ MouseEvent.MOUSE_DRAGGED, 319, 168, 1395933344886L },
				{ MouseEvent.MOUSE_DRAGGED, 322, 170, 1395933344902L },
				{ MouseEvent.MOUSE_DRAGGED, 323, 174, 1395933344919L },
				{ MouseEvent.MOUSE_DRAGGED, 324, 177, 1395933344936L },
				{ MouseEvent.MOUSE_DRAGGED, 325, 179, 1395933344952L },
				{ MouseEvent.MOUSE_DRAGGED, 325, 180, 1395933344969L },
				{ MouseEvent.MOUSE_DRAGGED, 326, 181, 1395933344987L },
				{ MouseEvent.MOUSE_DRAGGED, 327, 181, 1395933345003L },
				{ MouseEvent.MOUSE_RELEASED, 327, 181, 1395933345116L },
				{ MouseEvent.MOUSE_MOVED, 328, 181, 1395933345270L },
				{ MouseEvent.MOUSE_MOVED, 336, 179, 1395933345287L },
				{ MouseEvent.MOUSE_MOVED, 349, 175, 1395933345304L },
				{ MouseEvent.MOUSE_MOVED, 371, 170, 1395933345320L },
				{ MouseEvent.MOUSE_MOVED, 386, 168, 1395933345337L },
				{ MouseEvent.MOUSE_MOVED, 404, 165, 1395933345354L },
				{ MouseEvent.MOUSE_MOVED, 413, 165, 1395933345370L },
				{ MouseEvent.MOUSE_MOVED, 422, 165, 1395933345387L },
				{ MouseEvent.MOUSE_MOVED, 427, 164, 1395933345404L },
				{ MouseEvent.MOUSE_MOVED, 429, 164, 1395933345421L },
				{ MouseEvent.MOUSE_MOVED, 430, 163, 1395933345437L },
				{ MouseEvent.MOUSE_MOVED, 431, 163, 1395933345471L },
				{ MouseEvent.MOUSE_MOVED, 432, 160, 1395933345487L },
				{ MouseEvent.MOUSE_MOVED, 432, 157, 1395933345504L },
				{ MouseEvent.MOUSE_MOVED, 433, 151, 1395933345521L },
				{ MouseEvent.MOUSE_MOVED, 434, 143, 1395933345538L },
				{ MouseEvent.MOUSE_MOVED, 434, 136, 1395933345555L },
				{ MouseEvent.MOUSE_MOVED, 434, 123, 1395933345571L },
				{ MouseEvent.MOUSE_MOVED, 434, 111, 1395933345588L },
				{ MouseEvent.MOUSE_MOVED, 433, 100, 1395933345604L },
				{ MouseEvent.MOUSE_MOVED, 429, 91, 1395933345621L },
				{ MouseEvent.MOUSE_MOVED, 424, 86, 1395933345638L },
				{ MouseEvent.MOUSE_MOVED, 419, 84, 1395933345922L },
				{ MouseEvent.MOUSE_MOVED, 412, 77, 1395933345939L },
				{ MouseEvent.MOUSE_MOVED, 395, 58, 1395933345955L },
				{ MouseEvent.MOUSE_MOVED, 374, 31, 1395933345972L },
		};
		
		RobotMouseScript mouseScript = new RobotMouseScript(data, robot, scpd.shapePanel);
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				scpd.uiComboBox.setSelectedIndex(2);
				frame.getContentPane().add(scpd);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
		
		Rectangle bounds = frame.getBounds();
		Insets i = frame.getInsets();
		bounds.x += i.left;
		bounds.y += i.top;
		bounds.width -= i.left + i.right;
		bounds.height -= i.top + i.bottom + 3;
		
		ComponentPaintCapture screenCapture = new ComponentPaintCapture(scpd);
		screenCapture.setTargetedRepaints(false);
		
		//arbitrary number based on observation:
		screenCapture.setPlaybackRate(5);

		//return to start:
		BlogHelper.animateMouse(robot, scpd.createButton, true, true);
		
		Thread.sleep(1000);
		screenCapture.start();
		mouseScript.run();
		
		BlogHelper.animateMouse(robot, scpd.clearButton, true, true);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch(Throwable t) {
					t.printStackTrace();
				}
				frame.setVisible(false);
			}
		});
		
		AnimationReader r = screenCapture.stop(true);
		
		BufferedImage bi = new BufferedImage(scpd.shapePanel.getWidth(), scpd.shapePanel.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setPaint(Color.white);
		g.fillRect(0,0,bi.getWidth(),bi.getHeight());
		g.setColor(Color.black);
		g.setStroke(new BasicStroke(1));
		GeneralPath currentPath = null;
		for(int a = 0; a<data.length; a++) {
			if(data[a][0]==MouseEvent.MOUSE_PRESSED) {
				if(currentPath!=null) {
					g.draw(currentPath);
				}
				currentPath = new GeneralPath();
				currentPath.moveTo( (int)data[a][1], (int)data[a][2] );
			} else if(data[a][0]!=MouseEvent.MOUSE_MOVED) {
				currentPath.lineTo( (int)data[a][1], (int)data[a][2] );
			}
		}
		if(currentPath!=null) {
			g.draw(currentPath);
		}
		g.dispose();
		
		//TODO: automatically trim and pad this image. ImageUtils has a related method,
		//but it specifically deals with transparent pixels (not white pixels)
		
		ImageIO.write(bi, "png", stillImage);
		
		return r;
	}
}