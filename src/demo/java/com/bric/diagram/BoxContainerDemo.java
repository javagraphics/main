/*
 * @(#)BoxContainerDemo.java
 *
 * $Date: 2015-09-13 20:46:53 +0200 (So, 13 Sep 2015) $
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
package com.bric.diagram;

import java.awt.Component;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bric.desktop.cache.CacheManager;
import com.bric.diagram.swing.BoxContainerPanel;
import com.bric.swing.ContextualMenuHelper;

public class BoxContainerDemo extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws IOException {
		CacheManager.initialize("BoxContainerDemo", "1.0");

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					String lf = UIManager.getSystemLookAndFeelClassName();
					UIManager.setLookAndFeel(lf);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				new BoxContainerDemo();
			}
		});
	}

	BoxContainerPanel bcp = new BoxContainerPanel();


	ContextualMenuHelper menuHelper = new ContextualMenuHelper() {

		Box clickedBox;
		JMenuItem removeBoxMenuItem = add("Remove Box", new Runnable() {
			public void run() {
				bcp.getBoxContainer().getBoxes().remove(clickedBox);
			}
		});

		private Box getBox(int x,int y) {
			BoxContainer boxContainer = bcp.getBoxContainer();
			if(boxContainer==null)
				return null;

			for(Box box : boxContainer.getBoxes()) {
				if(box.getBounds().contains(x, y))
					return box;
			}
			return null;
		}

		@Override
		protected void showPopup(Component c,int x,int y) {
			clickedBox = getBox(x,y);
			removeBoxMenuItem.setVisible(clickedBox!=null);
			super.showPopup(c, x, y);
		}

	};

	private static String CACHED_DIAGRAM_NAME = "autosaved.diagram";
	File autosaveFile;

	public BoxContainerDemo() {
		File dir = CacheManager.get().getDirectory(true);
		autosaveFile = new File(dir, CACHED_DIAGRAM_NAME);

		if(autosaveFile.exists() && autosaveFile.length()>0) {
			try(FileInputStream fileIn = new FileInputStream(autosaveFile)) {
				try(ObjectInputStream objIn = new ObjectInputStream(fileIn)) {
					BoxContainer c = (BoxContainer)objIn.readObject();
					bcp.setBoxContainer(c);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		bcp.getBoxContainer().getConnectorLogic().add(new ConnectorLogic() {

			@Override
			public BinaryRelationship getRelationship(Box box1, Box box2)
			{
				return new BinaryRelationship(Relationship.PLAIN, Relationship.PLAIN);
			}

		});

		bcp.getBoxContainer().addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				autosave();
			}

	}, false);

		menuHelper.addComponent(bcp);
		menuHelper.add("Add Box", new Runnable() {
			public void run() {
				Rectangle bounds = new Rectangle(100, 100);
				bounds.x = bcp.getWidth()/2 - 50;
				bounds.y = bcp.getHeight()/2 - 50;
				bcp.getBoxContainer().getBoxes().add(new Box(bounds));
			}
		});
		menuHelper.add("Restage", new Runnable() {
			public void run() {
				bcp.getUI().refreshConnectors(bcp);
			}
		});

		getContentPane().add(bcp);
		pack();
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private PropertyChangeListener autosavePCL = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			autosave();
		}
	};
	
	private Set<Box> processedBoxes = new HashSet<>();
	private Set<Connector> processedConnectors = new HashSet<>();
	
	private void autosave() {
		BoxContainer boxContainer = bcp.getBoxContainer();
		for(Box box : boxContainer.getBoxes()) {
			if(processedBoxes.add(box)) {
				box.addPropertyChangeListener(autosavePCL);
			}
		}
		for(Connector connector : boxContainer.getConnectors()) {
			if(processedConnectors.add(connector)) {
				connector.addPropertyChangeListener(autosavePCL);
			}
		}
		
		try(FileOutputStream fileOut = new FileOutputStream(autosaveFile)) {
			try(ObjectOutputStream objOut = new ObjectOutputStream(fileOut)) {
				objOut.writeObject(boxContainer);
			}
		} catch(Exception e2) {
			e2.printStackTrace();
		}
	}
}
