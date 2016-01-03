/*
 * @(#)BlogUpdaterApp.java
 *
 * $Date: 2015-03-15 22:43:24 +0100 (So, 15 Mär 2015) $
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

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import com.bric.UserCancelledException;
import com.bric.blog.CompileJarJob.MissingJarException;
import com.bric.io.FileTreeIterator;
import com.bric.io.IOUtils;
import com.bric.jar.ClassCheckList;
import com.bric.jar.JarWriter;
import com.bric.job.Job;
import com.bric.job.JobManager;
import com.bric.job.swing.JobStatusBar;
import com.bric.plaf.DecoratedTreeUI;
import com.bric.plaf.DecoratedTreeUI.TreeDecoration;
import com.bric.plaf.RoundTextFieldUI;
import com.bric.swing.EmptyIcon;
import com.bric.swing.PaddedIcon;
import com.bric.swing.TextFieldPrompt;
import com.bric.swing.resources.CheckIcon;

/** This app compiles jars, generates blurbs, blurb graphics,
 * and updates the "www" directory of this project to keep the webpages/builds
 * up-to-date.
 * <P>When this app is launched from Eclipse: it should be automatically configured
 * to update everything with just a few clicks. Once it has run: you'll need to refresh
 * the workspace and commit your changes.
 *
 */
public class BlogUpdaterApp extends JFrame {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					String lf = UIManager.getSystemLookAndFeelClassName();
					UIManager.setLookAndFeel(lf);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				
				BlogUpdaterApp appFrame = new BlogUpdaterApp();
				appFrame.pack();
				appFrame.setLocationRelativeTo(null);
				appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				appFrame.setVisible(true);
			}
		});
	}
	
	private class ToggleSelectedActionListener implements ActionListener {
		Job job;
		ToggleSelectedActionListener(Job j) {
			job = j;
		}
		public void actionPerformed(ActionEvent e) {
			if(pendingJobs.contains(job)) {
				pendingJobs.remove(job);
			} else {
				pendingJobs.add(job);
			}
			refreshControls();
		}
	}
	
	Robot robot;
	Set<Job> pendingJobs = new HashSet<Job>();
	JTree tree = new JTree();
	JScrollPane scrollPane = new JScrollPane(tree);
	JarWriter jarWriter = new JarWriter();
	TreeDecoration checkboxDecoration = new TreeDecoration() {
		Insets i = new Insets(0,4,0,8);
		Icon checkIcon = new PaddedIcon(new CheckIcon(14, 14, Color.black), i);
		Icon selectedCheckIcon = new PaddedIcon(new CheckIcon(14, 14, Color.white), i);
		Icon indicatedCheckIcon = new PaddedIcon(new CheckIcon(14, 14, Color.lightGray), i);
		Icon emptyIcon = new EmptyIcon(checkIcon.getIconWidth(), checkIcon.getIconHeight());
		
		@Override
		public Icon getIcon(JTree tree, Object value, boolean selected,
				boolean expanded, boolean leaf, int row, boolean isRollover,
				boolean isPressed) {
			Job job = getJob(value);
			if(pendingJobs.contains(job)) {
				if(selected)
					return selectedCheckIcon;
				return checkIcon;
			} else {
				if(isRollover) {
					return indicatedCheckIcon;
				}
				return emptyIcon;
			}
		}

		@Override
		public boolean isVisible(JTree tree, Object value, boolean selected,
				boolean expanded, boolean leaf, int row, boolean hasFocus) {
			return getJob(value)!=null;
		}
		
		private Job getJob(Object node) {
			DefaultMutableTreeNode t = (DefaultMutableTreeNode)node;
			if(t.getUserObject() instanceof Job)
				return (Job)t.getUserObject();
			return null;
		}

		@Override
		public ActionListener getActionListener(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			Job job = getJob(value);
			return new ToggleSelectedActionListener( job );
		}
		
	};

	WorkspaceContext context = new WorkspaceContext();
	JobManager jobManager = new JobManager(4);
	JobStatusBar statusBar = new JobStatusBar(jobManager, true);
	JButton startButton = new JButton("Start");
	JButton selectAllButton = new JButton("Select All \u25BE");
	JButton unselectAllButton = new JButton("Unselect All");
	JPanel buttonRow = new JPanel(new GridBagLayout());
	JTextField searchField = new JTextField(18);
	JPanel controls = new JPanel(new GridBagLayout());
	
	public BlogUpdaterApp() {
		context.setJarWriter(jarWriter);
		try {
			robot = new Robot();
		} catch (AWTException e1) {
			throw new RuntimeException(e1);
		}
		installUI();
		/* Sometimes the window would just never appear?
		 * My guess is that initialization took too long and the EDT
		 * just gave up? I'm not sure. But this so far appears
		 * to resolve the problem: show the window immediately,
		 * and then set out to do the heavy lifting.
		 * Simply relying on a SwingUtilities.invokeLater() didn't
		 * work, I needed a Timer to give an extra little gap
		 * in time.
		 * 
		 */
		((DefaultTreeModel)tree.getModel()).setRoot(new DefaultMutableTreeNode("Loading..."));
		Timer timer = new Timer(50, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						jobManager.addJob(new SetupJarWriterJob(context, jarWriter));
						populateJobs();
						refreshControls();
					}
				});
			}
		});
		timer.setRepeats(false);
		timer.start();
	}

	SortedMap<String, File> allSortedFiles = new TreeMap<String, File>();
	/** This is invoked to refresh the list of jobs. */
	protected void populateJobs() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		DefaultTreeModel model = new DefaultTreeModel(root);
		
		UpdateSourceHeaderJob headerJob = new UpdateSourceHeaderJob(context);
		model.insertNodeInto(new DefaultMutableTreeNode(headerJob), root, root.getChildCount());
		
		CreateSamplesJob samplesJob = new CreateSamplesJob(context);
		model.insertNodeInto(new DefaultMutableTreeNode(samplesJob), root, root.getChildCount());
		
		JavadocJob javadocJob = new JavadocJob(context);
		model.insertNodeInto(new DefaultMutableTreeNode(javadocJob), root, root.getChildCount());
		
		Job indexJob = new IndexBlurbsJob();
		model.insertNodeInto(new DefaultMutableTreeNode(indexJob), root, root.getChildCount());
		
		Job validateJob = new ValidateDeliverablesJob(context);
		model.insertNodeInto(new DefaultMutableTreeNode(validateJob), root, root.getChildCount());
				
		if(allSortedFiles.size()==0) {
			for(int a = 0; a<context.getSourcePaths().length; a++) {
				FileTreeIterator fileIter = new FileTreeIterator(context.getSourcePaths()[a], "java");
				while(fileIter.hasNext()) {
					File javaFile = fileIter.next();
					if(	//does it have a main method?
						(ClassCheckList.containsMainMethod(javaFile)) || 
							
						//what about a blurb?
						(context.containsBlurb(javaFile))) {
						
						allSortedFiles.put(javaFile.getName(), javaFile);
					}
				}
			}
		}

		List<File> javaFiles = new ArrayList<File>();
		for(String fileName : allSortedFiles.keySet()) {
			File javaFile = allSortedFiles.get(fileName);
			try {
				CompileJarJob compileJob = new CompileJarJob(context, javaFile, jarWriter, 1.7f);
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(compileJob);
				model.insertNodeInto(newNode, root, root.getChildCount());
				
				ExecuteHelperMethodJob[] helperJobs = ExecuteHelperMethodJob.get(context, javaFile);
				for(ExecuteHelperMethodJob j : helperJobs) {
					j.setRobot(robot);
					DefaultMutableTreeNode helperNode = new DefaultMutableTreeNode(j);
					model.insertNodeInto(helperNode, newNode, newNode.getChildCount());
				}
			} catch(MissingJarException e) {
				System.err.println("No jar available for \""+javaFile.getName()+"\"");
			}
			
			javaFiles.add(javaFile);
		}

		//remove jobs that don't match the search phrase
		String searchPhrase = searchField.getText();
		if(searchPhrase.length()>0) {
			for(int a = root.getChildCount()-1; a>=0; a--) {
				if(root.getChildAt(a) instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode n = (DefaultMutableTreeNode)root.getChildAt(a);
					if(n.getUserObject() instanceof Job) {
						Job job = (Job)n.getUserObject();
						String name = job.getName();
						if(!name.toLowerCase().contains(searchPhrase.toLowerCase())) {
							model.removeNodeFromParent(n);
						}
					}
				}
			}
		}
		
		tree.setModel(model);
		tree.setRootVisible(false);
		for(int row = 0; row<tree.getRowCount(); row++) {
			tree.expandRow(row);
		}
	}

	/** This is invoked once to setup the UI. */
	private void installUI() {
		scrollPane.setPreferredSize(new Dimension(350, 400));
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		getContentPane().add(controls, c);
		getContentPane().add(statusBar, c);
		
		statusBar.addContainerListener(new ContainerListener() {
			Runnable runnable = new Runnable() {
				public void run() {
					boolean v = jobManager.isActive();

					controls.setVisible(!v);
					statusBar.setVisible(v);
				}
			};

			public void componentAdded(ContainerEvent e) {
				SwingUtilities.invokeLater(runnable);
			}

			public void componentRemoved(ContainerEvent e) {
				SwingUtilities.invokeLater(runnable);
			}
			
		});
		
		tree.setUI(new DecoratedTreeUI());
		tree.putClientProperty( DecoratedTreeUI.KEY_DECORATIONS, 
				new TreeDecoration[] { checkboxDecoration } );
		c = new GridBagConstraints();
		c.weightx = 1; c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0; c.gridy = 0;  c.insets = new Insets(5, 5, 5, 5);
		controls.add(searchField, c);
		c.gridy++; c.weighty = 1;  c.insets = new Insets(0, 0, 0, 0);
		controls.add(scrollPane, c);
		c.gridy++; c.weighty = 0; c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		controls.add(buttonRow, c);
		
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 0; c.weighty = 1;
		c.insets = new Insets(5,5,5,5);
		buttonRow.add(selectAllButton, c);
		c.gridx++;
		buttonRow.add(unselectAllButton, c);
		c.gridx++; c.weightx = 1;
		JPanel fluff = new JPanel();
		fluff.setOpaque(false);
		buttonRow.add(fluff, c);
		c.gridx++; c.weightx = 0;
		buttonRow.add(startButton, c);
		
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doStart();
			}
		});
		
		selectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSelectAll(SelectType.SHOW_POPUP);
			}
		});
		
		unselectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doUnselectAll();
			}
		});

		searchField.getDocument().addDocumentListener(new DocumentListener() {

			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			public void changedUpdate(DocumentEvent e) {
				populateJobs();
			}
			
		});

		searchField.setUI(new RoundTextFieldUI());
		searchField.putClientProperty("useSearchIcon", "true");
		@SuppressWarnings("unused")
		TextFieldPrompt prompt = new TextFieldPrompt(searchField, "Search");
		getRootPane().setDefaultButton(startButton);
		
		jobManager.addChangeListener(new ChangeListener() {
			Runnable runnable = new Runnable() {
				public void run() {
					refreshControls();
				}
			};
			public void stateChanged(ChangeEvent e) {
				SwingUtilities.invokeLater(runnable);
			}
		});
	}
	
	public enum SelectType {
		SHOW_POPUP(""), ALL("All"), ALL_JARS("All Jars"), ALL_RESOURCES("All Resources"), ALL_IMAGES("All Images"), ALL_ROBOT_RECORDINGS("All Robot Recordings");
		
		String s;
		
		SelectType(String s) {
			this.s = s;
		}
		
		private JMenuItem createMenuItem(final BlogUpdaterApp app) {
			JMenuItem menuItem = new JMenuItem(s);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					app.doSelectAll(SelectType.this);
				}
			});
			return menuItem;
		}
	}
	
	protected void doSelectAll(SelectType type) {
		if(SelectType.SHOW_POPUP.equals(type)) {
			JPopupMenu popup = new JPopupMenu();
			popup.add( SelectType.ALL.createMenuItem(this) );
			popup.add( SelectType.ALL_JARS.createMenuItem(this) );
			popup.add( SelectType.ALL_IMAGES.createMenuItem(this) );
			popup.add( SelectType.ALL_ROBOT_RECORDINGS.createMenuItem(this) );
			popup.add( SelectType.ALL_RESOURCES.createMenuItem(this) );
			popup.show(selectAllButton, 0, selectAllButton.getHeight());
		} else {
			TreeNode node = (TreeNode)tree.getModel().getRoot();
			List<Job> allJobs = getVisibleJobs(node);
			if(SelectType.ALL_RESOURCES.equals(type)) {
				for(Job j : allJobs) {
					if(j instanceof ExecuteHelperMethodJob) {
						pendingJobs.add(j);
					}
				}
			} else if(SelectType.ALL.equals(type)) {
				pendingJobs.addAll(allJobs);
			} else if(SelectType.ALL_IMAGES.equals(type)) {
				for(Job j : allJobs) {
					if(j instanceof ExecuteHelperMethodJob && 
							((ExecuteHelperMethodJob)j).getType()==ExecuteHelperMethodJob.IMAGE ) {
						pendingJobs.add(j);
					}
				}
			} else if(SelectType.ALL_ROBOT_RECORDINGS.equals(type)) {
				for(Job j : allJobs) {
					if(j instanceof ExecuteHelperMethodJob && 
							((ExecuteHelperMethodJob)j).getType()==ExecuteHelperMethodJob.FILE_WITH_ROBOT ) {
						pendingJobs.add(j);
					}
				}
			} else if(SelectType.ALL_JARS.equals(type)) {
				for(Job j : allJobs) {
					if(j instanceof CompileJarJob) {
						pendingJobs.add(j);
					}
				}
			} else {
				throw new IllegalArgumentException("unrecognized select type: "+type);
			}
			refreshControls();
		}
	}
	
	protected void doUnselectAll() {
		TreeNode node = (TreeNode)tree.getModel().getRoot();
		List<Job> allJobs = getVisibleJobs(node);
		pendingJobs.removeAll(allJobs);
		refreshControls();
	}
	
	protected void refreshControls() {
		TreeNode node = (TreeNode)tree.getModel().getRoot();
		List<Job> allJobs = getVisibleJobs(node);
		startButton.setEnabled(pendingJobs.size()>0);
		unselectAllButton.setEnabled( pendingJobs.size()>0 );
		selectAllButton.setEnabled( pendingJobs.size()<allJobs.size() );
		tree.repaint();
	}
	
	/** The "www" directory we've exported everything to should
	 * be a temp directory. This migrates everything into the
	 * workspace that has changed.
	 * <P>(If we just published everything directly to the workspace,
	 * then we'd constantly being committing wastefully/redundantly...)
	 *
	 */
	static class CopyDeliverablesJob extends BlogUpdaterJob {
		
		CopyDeliverablesJob(WorkspaceContext context) {
			super(context);
			setDescription("Transfering files...");
		}

		@Override
		protected void runJob() throws Exception {
			File dir = context.getDestinationDirectory();
			int j = dir.getAbsolutePath().length();
			try {
				FileTreeIterator iter = new FileTreeIterator(dir);
				while(iter.hasNext()) {
					if(isCancelled())
						throw new UserCancelledException();
					
					File file = iter.next();
					String relativePath = file.getAbsolutePath().substring(j);
					setNote(relativePath);
					String s = context.getWorkspaceDirectory().getAbsolutePath() + File.separator+"www"+relativePath;
					File targetFile = new File( s );
					boolean equals = file.getName().endsWith(".jar") ?
							IOUtils.zipEquals(file, targetFile, ".*\\.RSA") :
							IOUtils.equals(file, targetFile);
					if( (!file.isDirectory()) && (!equals)) {
						if( targetFile.exists() && (!targetFile.delete()))
							throw new RuntimeException("Failed to remove \""+targetFile.getAbsolutePath()+"\"");
						File parent = targetFile.getParentFile();
						if(!parent.exists()) {
							if(!parent.mkdirs()) {
								throw new RuntimeException("Failed to mkdir \""+parent.getAbsolutePath()+"\"");
							}
						}
						if(!file.renameTo(targetFile))
							throw new RuntimeException("Failed to rename \""+file.getAbsolutePath()+"\" to \""+targetFile.getAbsolutePath()+"\"");
					}
				}
			} finally {
				File[] t = dir.listFiles();
				for(File f : t) {
					IOUtils.delete( f );
				}
			}
		}
	}
	
	/** Returns a sublist of all the elements that match the type argument in class.
	 */
	protected <T> List<T> getSublist(List<?> list,Class<T> type) {
		List<T> returnValue = new LinkedList<T>();
		for(int a = 0; a<list.size(); a++) {
			if(type.isInstance( list.get(a) ))
					returnValue.add( (T)list.get(a) );
		}
		return returnValue;
	}
	
	private void createDependency(List<? extends Job> jobsToModify,List<? extends Job> jobsThatMustFinishFirst) {
		for(Job j : jobsToModify) {
			j.addDependencies(jobsThatMustFinishFirst);
		}
	}
	
	protected void doStart() {
		TreeNode node = (TreeNode)tree.getModel().getRoot();
		List<Job> allJobs = getSelectedJobs(node);
		
		//some jobs rely on other jobs, so we need to establish some dependencies
		//before these are dispatched to the JobManager:
		List<UpdateSourceHeaderJob> headerJobs = getSublist(allJobs, UpdateSourceHeaderJob.class);
		List<CreateSamplesJob> samplesJobs = getSublist(allJobs, CreateSamplesJob.class);
		List<CompileJarJob> compileJobs = getSublist(allJobs, CompileJarJob.class);
		List<ExecuteHelperMethodJob> helperJobs = getSublist(allJobs, ExecuteHelperMethodJob.class);
		List<JavadocJob> docJobs = getSublist(allJobs, JavadocJob.class);
		List<ValidateDeliverablesJob> validateJob = getSublist(allJobs, ValidateDeliverablesJob.class);
		allJobs.removeAll(validateJob);
		
		createDependency(samplesJobs, headerJobs);
		createDependency(compileJobs, samplesJobs);
		createDependency(docJobs, samplesJobs);
		
		/* FIXME: Running all jobs concurrently results in broken jars, and
		 * I'm not sure why. There is probably a deeper problem here, but I
		 * want to update jars ASAP, so set up a chain of dependencies that
		 * makes them update in a single thread
		 */
		for(int a = 0; a<compileJobs.size(); a++) {
			for(int b = a+1; b<compileJobs.size(); b++) {
				compileJobs.get(a).addDependencies(compileJobs.get(b));
			}
		}
		
		List<ExecuteHelperMethodJob> chainOfHelperJobs = new ArrayList<ExecuteHelperMethodJob>();
		for(int a = 0; a<helperJobs.size(); a++) {
			if(helperJobs.get(a).requiresRobot()) {
				//initiate this kind of job first because it can take so long:
				helperJobs.get(a).setPriority(Job.PRIORITY_HIGH);
				
				//configure some dependencies so only 1 just that uses the robot runs
				//at a time
				helperJobs.get(a).setDependencies(chainOfHelperJobs.toArray(new Job[chainOfHelperJobs.size()]));
				chainOfHelperJobs.add(helperJobs.get(a));
			}
		}
		
		Job[] allJobsArray = allJobs.toArray(new Job[allJobs.size()]);
		jobManager.addJob(allJobsArray);
		
		CopyDeliverablesJob transferJob = new CopyDeliverablesJob(context);
		transferJob.setDependencies(allJobsArray);
		jobManager.addJob(transferJob);

		if(validateJob.size()==1) {
			validateJob.get(0).setDependencies(transferJob);
			jobManager.addJob(validateJob.get(0));
		}
	}
	
	private List<Job> getSelectedJobs(TreeNode node) {
		List<Job> visibleJobs = getVisibleJobs(node);
		List<Job> returnValue = new LinkedList<Job>();
		for(Job j : visibleJobs) {
			if(pendingJobs.contains( j )) {
				returnValue.add( j );
			}
		}
		return returnValue;
	}

	
	private List<Job> getVisibleJobs(TreeNode node) {
		List<Job> returnValue = new LinkedList<Job>();
		if(node instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode d = (DefaultMutableTreeNode)node;
			Object t = d.getUserObject();
			if(t instanceof Job) {
				returnValue.add( (Job)t );
			}
		}
		for(int a = 0; a<node.getChildCount(); a++) {
			returnValue.addAll( getVisibleJobs( node.getChildAt(a) ) );
		}
		return returnValue;
	}
	
}
