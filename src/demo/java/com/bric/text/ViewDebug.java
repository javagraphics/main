/*
 * @(#)ViewDebug.java
 *
 * $Date: 2014-05-06 21:08:19 +0200 (Di, 06 Mai 2014) $
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
package com.bric.text;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.lang.reflect.Field;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.View;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class ViewDebug extends JPanel {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		JFrame frame = new JFrame("View Debug");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new ViewDebug());
		frame.pack();
		frame.setVisible(true);
	}
	
	class ViewPreview extends JPanel {
		private static final long serialVersionUID = 1L;

		public ViewPreview() {
			setPreferredSize(new Dimension(200,200));
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			TreePath path = viewTree.getSelectionPath();
			if(path!=null) {
				ViewNode node = (ViewNode)path.getLastPathComponent();
				if(node.view!=null) {
					node.view.paint(g, new Rectangle(0,0,getWidth(),getHeight()));
				}
			}
		}
	}
	
	JTextField url = new JTextField();
	JTabbedPane tabs = new JTabbedPane();
	
	JEditorPane preview = new JEditorPane() {
		private static final long serialVersionUID = 1L;

		@Override
		public void addMouseListener(MouseListener l) {
			super.addMouseListener(l);
			Thread.dumpStack();
		}
	};
	JTree viewTree = new JTree(new ViewNode(null));
	JPanel inspectorPanel = new JPanel(new GridBagLayout());
	JScrollPane previewScroll = new JScrollPane(preview);
	JScrollPane inspectorScroll = new JScrollPane(inspectorPanel);
	ViewPreview viewPreview = new ViewPreview();
	
	ActionListener urlListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			try {
				preview.setPage(url.getText());
			} catch(IOException e2) {
				e2.printStackTrace();
			}
		}
	};
	
	DocumentListener documentListener = new DocumentListener() {

		public void changedUpdate(DocumentEvent e) {
			System.out.println(e);
		}

		public void insertUpdate(DocumentEvent e) {
			System.out.println(e);
		}

		public void removeUpdate(DocumentEvent e) {
			System.out.println(e);
		}
	};

	HierarchyListener hierarchyListener = new HierarchyListener() {
		public void hierarchyChanged(HierarchyEvent e) {
			refreshInspector();
		}
	};
	
	public ViewDebug() {
		tabs.add(previewScroll,"Preview");
		tabs.add(inspectorScroll,"Inspector");
		
		url.addActionListener(urlListener);
		
		preview.getDocument().addDocumentListener(documentListener);
		
		url.setText("http://www.google.com/");
		urlListener.actionPerformed(null);
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(url,c);
		c.gridy++; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		add(tabs,c);
		
		inspectorPanel.addHierarchyListener(hierarchyListener);
		
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		inspectorPanel.add(new JScrollPane(viewTree),c);
		c.gridx++;
		inspectorPanel.add(viewPreview,c);
		
		viewTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		viewTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				viewPreview.repaint();
			}
		});
	}
	
	protected void refreshInspector() {
		try {
			TextUI textUI = preview.getUI();
			View rootView = getRootView(textUI);

			
			DefaultTreeModel model = (DefaultTreeModel)viewTree.getModel();
			ViewNode root = (ViewNode)model.getRoot();
			while( model.getChildCount(root)>0 ) {
				model.removeNodeFromParent( (MutableTreeNode)model.getChild(root, 0));
			}
			model.insertNodeInto(new ViewNode(rootView), root, 0);
		} catch(Throwable t) {
			t.printStackTrace();
		}

	}
	
	protected View getRootView(Object obj) throws IllegalArgumentException, IllegalAccessException {
		Class<?> c = obj.getClass();
		while(c!=null) {
			Field[] f = c.getDeclaredFields();
			for(int a = 0; a<f.length; a++) {
				if(f[a].getName().equals("rootView")) {
					f[a].setAccessible(true);
					return (View)f[a].get(obj);
				}
			}
			 c = c.getSuperclass();
		}
		return null;
	}
}

class ViewNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;

	final View view;
	public ViewNode(View v) {
		view = v;
		if(v!=null) { //v is null for root nodes
			for(int a = 0; a<v.getViewCount(); a++) {
				ViewNode newChild = new ViewNode(v.getView(a));
				add(newChild);
				System.out.println(newChild);
			}
		}
	}
	
	@Override
	public String toString() {
		if(view==null)
			return "root";
		
		return view.getClass().getName();
	}
}
