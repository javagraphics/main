/*
 * @(#)CompileJarJob.java
 *
 * $Date: 2015-12-13 19:57:22 +0100 (So, 13 Dez 2015) $
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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.jar.Manifest;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.bric.inspector.InspectorGridBagLayout;
import com.bric.jar.ClassCheckList;
import com.bric.jar.JarDependencyChoice;
import com.bric.jar.JarSigner;
import com.bric.jar.JarWriter;
import com.bric.swing.DialogFooter;
import com.bric.swing.QDialog;

/** This job compiles a java file with main() method into 3 jars:
 * one with .java and .class files, one with only .java files, and one
 * with only .class files.
 *
 */
class CompileJarJob extends BlogUpdaterJob {
	
	static class MissingJarException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	final File primaryJavaFile;
	final JarWriter jarWriter;
	final float compilerVersion;
	String jarName;
	
	/** The credentials used to sign jars. This is static so you
	 * only have to enter it once per session.
	 */
	static SigningCredentials credentials = null;
	
	class SigningCredentials {
		String keystorePassword;
		String alias, aliasPassword;
		boolean aborted = false;
		
		SigningCredentials() {
		}
		
		public void sign(WorkspaceContext context,File jarFile) {
			if(aborted)
				throw new RuntimeException("No password provided to sign jars.");			
			
			File keyStoreFile = new File(context.getWorkspaceDirectory(), "keystore.p12");

			JarSigner signer = new JarSigner(keyStoreFile,keystorePassword,alias,aliasPassword,"http://timestamp.digicert.com");
			signer.sign(jarFile, true);
		}

		public void showDialog() {
			Frame[] frames = Frame.getFrames();
			for(Frame f : frames) {
				if(f instanceof BlogUpdaterApp && f.isShowing()) {
					showDialog(f);
					return;
				}
			}
		}
		
		private void showDialog(Frame frame) {
			JPasswordField keystorePasswordField = new JPasswordField();
			JTextField aliasField = new JTextField();
			final JPasswordField aliasPasswordField = new JPasswordField();
			final JCheckBox aliasCheckBox = new JCheckBox("Alias Password:");
			
			JPanel innerComponent = new JPanel();
			InspectorGridBagLayout layout = new InspectorGridBagLayout(innerComponent);
			layout.addRow(new JLabel("Keystore Password:"), keystorePasswordField, true);
			layout.addRow(new JLabel("Alias:"), aliasField, true);
			layout.addRow(aliasCheckBox, aliasPasswordField, true);
			
			aliasCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					aliasPasswordField.setEnabled(aliasCheckBox.isSelected());
				}
			});
			aliasCheckBox.doClick();
			
			int option = QDialog.showDialog(frame, "Jar Signing Password", QDialog.PLAIN_MESSAGE, 
					"Please enter the credentials to sign this jar.", 
					"This jar cannot be published unless it is signed.", 
					innerComponent, 
					null, //lowerLeftComponent, 
					DialogFooter.OK_CANCEL_OPTION, 
					DialogFooter.OK_OPTION, 
					null, //dontShowKey, 
					null, //alwaysApplyKey, 
					DialogFooter.EscapeKeyBehavior.TRIGGERS_CANCEL);
			if(option==DialogFooter.OK_OPTION) {
				keystorePassword = new String(keystorePasswordField.getPassword());
				alias = aliasField.getText();
				if(aliasCheckBox.isSelected()) {
					aliasPassword = new String(aliasPasswordField.getPassword());
				}
			} else {
				aborted = true;
				keystorePassword = "aborted";
			}
		}
	}
	
	/**
	 * @throws MissingJarException if this java file has a blurb that prevents writing a jar file.
	 */
	public CompileJarJob(WorkspaceContext context,File primaryJavaFile,JarWriter jarWriter,float compilerVersion) throws MissingJarException {
		super(context);
		this.compilerVersion = compilerVersion;
		this.primaryJavaFile = primaryJavaFile;
		setDescription("Compiling \""+primaryJavaFile.getName()+"\"...");
		this.jarWriter = jarWriter;
		
		jarName = context.getJarName(primaryJavaFile);
		if(jarName.length()==0)
			throw new MissingJarException();
		
		setName(jarName+".jar");
	}

	@Override
	protected void runJob() throws Exception {
		
		synchronized(SigningCredentials.class) {
			if(credentials==null) {
				credentials = new SigningCredentials();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						credentials.showDialog();
					}
				});
			}
		}
		
		String primaryClass = context.getClassName(primaryJavaFile);
		primaryClass = primaryClass.replace('/', '.');

		File jarDirectory = context.getDestinationSubdirectory("jars");
		
		File jar = new File(jarDirectory, jarName+".jar");
		
		setNote("Writing \""+jar.getName()+"\"");
		try {
			File mainClass = ClassCheckList.containsMainMethod(primaryJavaFile) ? primaryJavaFile : null;
			Manifest manifest = JarWriter.createManifest(mainClass==null ? null : primaryClass, false);
			Blurb blurb = context.getBlurb(primaryJavaFile);

			if(mainClass!=null && blurb!=null && blurb.sandboxDemo()) {
				//add security attributes, see http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/manifest.html
				String url = "https://javagraphics.java.net";
				manifest.getMainAttributes().putValue("Codebase", url);
				manifest.getMainAttributes().putValue("Application-Name", primaryClass.substring(primaryClass.lastIndexOf('.')+1));
				manifest.getMainAttributes().putValue("Application-Library-Allowable-Codebase", url);
				manifest.getMainAttributes().putValue("Caller-Allowable-Codebase", url);
				manifest.getMainAttributes().putValue("Entry-Point", primaryClass);
				manifest.getMainAttributes().putValue("Permissions", "sandbox"); // or "all-permissions"
			}
			
			String errors = jarWriter.createJar(new String[] {primaryClass}, 
					compilerVersion, 
					manifest, 
					jar, 
					null, //filter
					new JarDependencyChoice.Preference(primaryClass)); 
			
			if(errors!=null) {
				throw new RuntimeException("Compilation errors occurred, see console for details.");
			}
			
			while(credentials.keystorePassword==null) {
				try {
					Thread.sleep(50);
				} catch(Exception e) {}
			}
			credentials.sign(context, jar);
		} catch(RuntimeException e) {
			System.err.println("primaryClass = "+primaryClass);
			System.err.println("jar = "+jar.getAbsolutePath());
			throw e;
		}
	}

}
