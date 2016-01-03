/*
 * @(#)DecoratedTreeDemoHelper.java
 *
 * $Date: 2014-05-07 08:30:26 +0200 (Mi, 07 Mai 2014) $
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
package com.bric.plaf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.bric.blog.BlogHelper;
import com.bric.image.thumbnail.BasicThumbnail;
import com.bric.swing.resources.RefreshIcon;
import com.bric.swing.resources.TriangleIcon;

public class DecoratedTreeDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) {
		/** It's surprisingly hard to just capture a snapshot of this demo,
		 * so instead let's just manufacture an image of what it should
		 * resemble.
		 */
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root Node");
		DefaultMutableTreeNode node1 = new DefaultMutableTreeNode("Playable");
		DefaultMutableTreeNode node2 = new DefaultMutableTreeNode("Warning");
		DefaultTreeModel treeModel = new DefaultTreeModel(root);
		JTree tree = new JTree(treeModel);
		treeModel.insertNodeInto(node1, root, 0);
		treeModel.insertNodeInto(node2, root, 1);
		tree.setRootVisible(true);
		tree.expandRow(0);
		
		Dimension d = tree.getPreferredSize();
		d.width += 35; //padding for decorations
		tree.setSize(d);
		Insets i = new Insets(6,6,6,6);
		BufferedImage bi = new BufferedImage(d.width+i.left+i.right, d.height+i.top+i.bottom, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
		g.translate(i.left, i.top);
		tree.paint(g);
		
		Rectangle r = tree.getRowBounds(0);
		Icon refreshIcon = new RefreshIcon(14, Color.gray);
		refreshIcon.paintIcon(tree, g, 
				bi.getWidth() - refreshIcon.getIconWidth()-6-i.right, 
				r.height/2 - refreshIcon.getIconHeight()/2);

		Icon playIcon = new TriangleIcon(SwingConstants.EAST, 10, 10, Color.gray);
		r = tree.getRowBounds(1);
		playIcon.paintIcon(tree, g, 
				bi.getWidth() - playIcon.getIconWidth()-6-i.right, 
				r.y + r.height/2 - playIcon.getIconHeight()/2);

		r = tree.getRowBounds(2);
		DecoratedTreeDemo.WARNING_ICON.paintIcon(tree, g, 
			bi.getWidth() - DecoratedTreeDemo.WARNING_ICON.getIconWidth()-6-i.right, 
			r.y + r.height/2 - DecoratedTreeDemo.WARNING_ICON.getIconHeight()/2);
		
		g.dispose();
		return BasicThumbnail.getShadow(3).create(bi, null);
	}
}
