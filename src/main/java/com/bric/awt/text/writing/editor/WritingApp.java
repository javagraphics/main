/*
 * @(#)WritingApp.java
 *
 * $Date: 2015-02-28 21:59:45 +0100 (Sa, 28 Feb 2015) $
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
package com.bric.awt.text.writing.editor;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.bric.animation.AnimationReader;
import com.bric.animation.writing.WritingShape;
import com.bric.awt.text.writing.WritingFont;
import com.bric.awt.text.writing.WritingTextArea;
import com.bric.awt.text.writing.WritingTextLayout;
import com.bric.blog.Blurb;
import com.bric.image.gif.GifWriter;
import com.bric.image.gif.GifWriter.ColorReduction;
import com.bric.io.SuffixFilenameFilter;
import com.bric.plaf.AquaAngleSliderUI;
import com.bric.qt.io.JPEGMovWriter;
import com.bric.swing.CollapsibleContainer;
import com.bric.swing.ContextualMenuHelper;
import com.bric.swing.DialogFooter;
import com.bric.swing.DialogFooter.EscapeKeyBehavior;
import com.bric.swing.FontComboBox;
import com.bric.swing.QDialog;
import com.bric.swing.QOptionPaneCommon;
import com.bric.swing.SectionContainer.Section;
import com.bric.swing.ShapeCreationPanel;
import com.bric.swing.animation.AnimationController;

/** This application edits .writtenfont files.
 */
@Blurb (
filename = "WritingApp",
title = "Text: Handwriting Text Effect",
releaseDate = "Nov 2014",
summary = "This visual effect draws text over time similar to how  human would. Unlike a simple wipe-left transition (which would work with any font), this requires specially formatted font-like shape data. The current implementation is modeled after the public domain font <a href=\"http://comicneue.com/\">Comic Neue</a>.",
link = "http://javagraphics.blogspot.com/2014/11/text-handwriting-text-effect.html",
sandboxDemo = false
)
public class WritingApp extends JFrame {
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
				
				JFrame f = new WritingApp();
				f.setVisible(true);
			}
		});
	}

	Settings settings = new Settings();
	JPanel fontControls = new JPanel(new GridBagLayout());
	JPanel glyphControls = new JPanel(new GridBagLayout());
	JPanel charPanel = new JPanel(new GridBagLayout());
	JPanel missingGlyphPanel = new JPanel(new GridBagLayout());
	JPanel stylePanel = new JPanel(new GridBagLayout());
	JRadioButton styleCalligraphy = new JRadioButton("Calligraphy");
	JRadioButton stylePlain = new JRadioButton("Plain");
	JTextField charField = new JTextField("", 2);
	ShapeCreationPanel glyphCreationPanel = new ShapeCreationPanel();
	JPanel filePanel = new JPanel(new GridBagLayout());
	JPanel strokePanel = new JPanel(new GridBagLayout());
	
	AnimationController animationController = new AnimationController();
	JLabel nibAngleLabel = new JLabel("Nib Angle:");
	JLabel fileLabel = new JLabel("None");
	JButton browseButton = new JButton("Browse...");
	JButton newButton = new JButton("New...");
	JSlider italicizedAngle = new JSlider(0, 360);
	JSlider nibAngle = new JSlider(0, 360);
	int adjusting = 0;
	FontComboBox onionSkin = new FontComboBox();
	JButton cloneButton = new JButton("Clone From...");
	JButton createGlyphButton = new JButton("Create Glyph");
	JButton deleteGlyphButton = new JButton("Delete Glyph");
	JButton deleteStrokeButton = new JButton("Delete Stroke");
	JButton createStrokeButton = new JButton("Create Stroke");
	JLabel strokeQualifier = new JLabel();
	JLabel strokeLabel = new JLabel("Stroke:");
	JCheckBox onionSkinCheckbox = new JCheckBox("Onion Skin:", false);
	JTextField strokeField = new JTextField(6);
	WritingTextArea sampleTextArea = new WritingTextArea(400, "the quick brown fox jumps over the lazy dog");
	
	DecimalFormat decimalFormat = new DecimalFormat("#.###");
	
	public WritingApp() {
		italicizedAngle.setUI(new AquaAngleSliderUI());
		nibAngle.setUI(new AquaAngleSliderUI());
		glyphCreationPanel.setHandleSize(8);
		glyphCreationPanel.setUI(new GlyphCreationUI(this, glyphCreationPanel, settings));

		ContextualMenuHelper.add(sampleTextArea, "Change font size...", new Runnable() {
			public void run() {
				float value = sampleTextArea.getWritingLayout().getFontSize();
				JSpinner spinner = new JSpinner(new SpinnerNumberModel(value,8,600,1));
			    int status = QDialog.showDialog(WritingApp.this,
			    		"Change Font Size", 
			    		QDialog.INFORMATION_MESSAGE,
			    		"",
			    		"Select a new font size:",
			    		spinner,
			    		null, //lowerLeftComponent,
			    		DialogFooter.OK_CANCEL_OPTION, 
			    		DialogFooter.OK_OPTION,
			    		null, //dontShowKey,
			    		null, //alwaysApplyKey,
			    		EscapeKeyBehavior.TRIGGERS_CANCEL);
			    if(status==DialogFooter.OK_OPTION) {
			    	sampleTextArea.getWritingLayout().setFontSize( ((Number)spinner.getValue()).floatValue() );
			    }
			}
		});
		
		ContextualMenuHelper.add(sampleTextArea, "Export GIF...", new Runnable() {
			public void run() {
				exportGIF();
			}
		});

		ContextualMenuHelper.add(sampleTextArea, "Export MOV...", new Runnable() {
			public void run() {
				exportMOV();
			}
		});
		

		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), 
				"save");
		
		getRootPane().getActionMap().put("save", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				File file = settings.get(Settings.FILE);
				if(file!=null)
					save(file);
			}
		});
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 0; c.weighty = 0;
		c.insets = new Insets(5,5,5,5); c.anchor = GridBagConstraints.EAST;
		fontControls.add(new JLabel("Writing Font:"), c);
		c.gridy++;
		fontControls.add(new JLabel("Italicized Angle:"), c);
		c.gridy++;
		fontControls.add(new JLabel("Pen Style:"), c);
		c.gridy++;
		fontControls.add(nibAngleLabel, c);
		c.gridx++; c.anchor = GridBagConstraints.WEST; c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
		fontControls.add( filePanel, c );
		c.gridy++; c.fill = GridBagConstraints.NONE;
		fontControls.add( italicizedAngle, c );
		c.gridy++;
		fontControls.add( stylePanel, c );
		c.gridy++;
		fontControls.add( nibAngle, c );
		
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 0; c.weighty = 0;
		c.insets = new Insets(5,5,5,5); c.anchor = GridBagConstraints.EAST;
		glyphControls.add(new JLabel("Character:"), c);
		c.gridy++; c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
		glyphControls.add(missingGlyphPanel, c);
		c.gridy++; c.gridwidth = 1; c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		glyphControls.add(strokeLabel, c);
		c.gridy++;
		glyphControls.add(onionSkinCheckbox, c);
		c.gridx++; c.anchor = GridBagConstraints.WEST; c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
		glyphControls.add( charPanel, c );
		c.gridy+=2;
		glyphControls.add( strokePanel, c );
		c.gridy++; c.fill = GridBagConstraints.NONE;
		glyphControls.add( onionSkin, c );
		c.insets = new Insets(0,0,0,0);
		c.gridy++; c.weighty = 1; c.gridx = 0; c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.BOTH;
		glyphControls.add(glyphCreationPanel, c);
		c.gridy++; c.weighty = 0;
		glyphControls.add(animationController, c);
		
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 0; c.weighty = 0; c.fill = GridBagConstraints.HORIZONTAL;
		missingGlyphPanel.add(new JLabel("This glyph does not exist."), c);
		c.gridx++; c.weightx = 1; c.fill = GridBagConstraints.NONE;
		missingGlyphPanel.add(cloneButton, c);

		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 0; c.weighty = 0;
		c.insets = new Insets(0,0,0,5);
		strokePanel.add(strokeField, c);
		c.gridx++; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
		strokePanel.add(strokeQualifier, c);
		c.gridx++; c.weightx = 0;  c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(0,3,0,0);
		strokePanel.add(createStrokeButton, c);
		c.gridx++;
		strokePanel.add(deleteStrokeButton, c);
		
		cloneButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JTextField charField = new JTextField(1);
				int k = QDialog.showDialog(
						WritingApp.this, //frame, 
						"Choose a Character", //dialogTitle, 
						QDialog.PLAIN_MESSAGE, //type, 
						null, //boldMessage
						"Type the character to clone.", //plainMessage, 
						charField, //innerComponent, 
						null, //lowerLeftComponent, 
						DialogFooter.OK_CANCEL_OPTION, //options, 
						DialogFooter.OK_OPTION, //defaultOption, 
						null, //dontShowKey, 
						null, //alwaysApplyKey, 
						DialogFooter.EscapeKeyBehavior.TRIGGERS_CANCEL); //escapeKeyBehavior);
				if(k==DialogFooter.OK_OPTION) {
					String text = charField.getText();
					if(text.length()!=1)
						throw new IllegalArgumentException("text \""+text+"\" should be 1 character");
					char ch = text.charAt(0);

					WritingFont font = settings.get(Settings.WRITING_FONT);
					WritingShape sourceGlyph = font.getGlyph(ch);

					char ch2 = settings.get(Settings.SELECTED_CHAR);
					font.setGlyph(ch2, new WritingShape(sourceGlyph));
					refreshControls();
				}
			}
		});
		
		animationController.addPropertyChangeListener(new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {
				if(AnimationController.TIME_PROPERTY.equals(evt.getPropertyName())) {
					settings.set(Settings.ANIMATION_TIME, animationController.getTime());
				} else if(AnimationController.PLAYING_PROPERTY.equals(evt.getPropertyName())) {
					settings.set(Settings.ANIMATION_ACTIVE, animationController.isPlaying());
				}
			}
			
		});
		
		DocumentListener docListener = new DocumentListener() {

			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			public void changedUpdate(DocumentEvent e) {
				if(adjusting>0) return;
				
				if(e.getDocument()==charField.getDocument()) {
					final String s = charField.getText();
					if(s.length()>0) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								settings.set(Settings.SELECTED_CHAR, s.charAt(0));
							}
						});
					}
				} else if(e.getDocument()==strokeField.getDocument()) {
					try {
						int i = Integer.parseInt(strokeField.getText());
						settings.set(Settings.SELECTED_STROKE, i);
						strokeField.setForeground(Color.black);
					} catch(Exception e2) {
						e2.printStackTrace();
						strokeField.setForeground(Color.red);
					}
				}
			}
		};
		
		charField.getDocument().addDocumentListener(docListener);
		strokeField.getDocument().addDocumentListener(docListener);
		
		settings.addListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if(Settings.SELECTED_CHAR.matches(evt)) {
					charField.setText( ""+settings.get( Settings.SELECTED_CHAR ) );
				} else if(Settings.FILE.matches(evt)) {
					File file = settings.get(Settings.FILE);
					WritingFont font;
					try {
						if(file==null) {
							font = null;
						} else if(!file.exists()) {
							font = new WritingFont();
						} else {
							font = new WritingFont(file);
						}
						settings.set( Settings.WRITING_FONT, font);
						fileLabel.setText(file.getName());
					} catch(Exception e) {
						e.printStackTrace();
					}
				} else if(Settings.WRITING_FONT.matches(evt)) {
					WritingFont font = (WritingFont)evt.getNewValue();
					sampleTextArea.getWritingLayout().setFont(font);
				}
				refreshControls();
			}
		});
		

		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 0; c.weighty = 1;
		c.insets = new Insets(0,0,0,0);
		charPanel.add(charField, c);
		c.gridx++; c.weightx = 1;
		JPanel fluff = new JPanel();
		fluff.setOpaque(false);
		charPanel.add(fluff, c);
		c.weightx = 0;
		c.gridx++;
		charPanel.add(createGlyphButton, c);
		c.gridx++;
		charPanel.add(deleteGlyphButton, c);
		
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		c.insets = new Insets(0,0,0,8); c.fill = GridBagConstraints.HORIZONTAL;
		filePanel.add(fileLabel, c);
		c.gridx++;
		c.weightx = 0;
		filePanel.add(browseButton, c);
		c.gridx++;
		filePanel.add(newButton, c);
		

		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		c.insets = new Insets(0,0,0,8); c.fill = GridBagConstraints.HORIZONTAL;
		stylePanel.add(stylePlain, c);
		c.gridx++;
		stylePanel.add(styleCalligraphy, c);
		
		stylePanel.setOpaque(false);
		charPanel.setOpaque(false);
		filePanel.setOpaque(false);
		
		getContentPane().setLayout(new GridBagLayout());
		
		CollapsibleContainer controls = new CollapsibleContainer();
		Section fontSection = controls.addSection("Font", "Font");
		Section glyphSection = controls.addSection("Glyph", "Glyph");
		Section sampleSection = controls.addSection("Sample", "Sample");
		fontSection.getBody().setLayout(new GridBagLayout());
		glyphSection.getBody().setLayout(new GridBagLayout());
		sampleSection.getBody().setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.weightx = 1; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
		c.gridx = 0; c.gridy = 0;
		fontSection.getBody().add(fontControls, c);
		glyphSection.getBody().add(glyphControls, c);
		
		sampleSection.getBody().add(new JScrollPane(sampleTextArea), c);
		
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		getContentPane().add(controls, c);
		
		
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(e.getSource()==newButton) {
					File file = doFileDialog(FileDialog.SAVE, WritingFont.FILE_EXTENSION);
					if(file!=null) {
						settings.set(Settings.FILE, file);
					}
				} else if(e.getSource()==browseButton) {
					File file = doFileDialog(FileDialog.LOAD, WritingFont.FILE_EXTENSION);
					if(file!=null) {
						settings.set(Settings.FILE, file);
					}
				} else if(e.getSource()==stylePlain) {
					WritingFont font = settings.get(Settings.WRITING_FONT);
					font.setProperty(WritingFont.STYLE, WritingFont.STYLE_PLAIN);
				} else if(e.getSource()==styleCalligraphy) {
					WritingFont font = settings.get(Settings.WRITING_FONT);
					font.setProperty(WritingFont.STYLE, WritingFont.STYLE_CALLIGRAPHY);
				} else if(e.getSource()==createGlyphButton) {
					WritingFont font = settings.get(Settings.WRITING_FONT);
					char ch = settings.get(Settings.SELECTED_CHAR);
					font.setGlyph(ch, new WritingShape());
				} else if(e.getSource()==deleteGlyphButton) {
					WritingFont font = settings.get(Settings.WRITING_FONT);
					char ch = settings.get(Settings.SELECTED_CHAR);
					font.setGlyph(ch, null);
				} else if(e.getSource()==createStrokeButton) {
					glyphCreationPanel.setMode(ShapeCreationPanel.MODE_CREATE);
				} else if(e.getSource()==deleteStrokeButton) {
					WritingFont font = settings.get(Settings.WRITING_FONT);
					char ch = settings.get(Settings.SELECTED_CHAR);
					WritingShape glyph = font.getGlyph(ch);
					int selectedStroke = settings.get(Settings.SELECTED_STROKE);
					glyph.getStrokes().remove(selectedStroke);
				} else if(e.getSource()==onionSkin) {
					Font font = (Font)onionSkin.getSelectedItem();
					settings.set(Settings.ONION_SKIN_FONT, font);
				} else if(e.getSource()==onionSkinCheckbox) {
					settings.set(Settings.ONION_SKIN_ACTIVE, onionSkinCheckbox.isSelected());
				}
				refreshControls();
			}
		};
		
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(adjusting>0) return;
				
				if(e.getSource()==italicizedAngle) {
					WritingFont font = settings.get(Settings.WRITING_FONT);
					float angle = (float)(Math.PI*2*getFraction(italicizedAngle));
					font.setProperty(WritingFont.ITALICIZED_ANGLE, angle);
				} else if(e.getSource()==nibAngle) {
					WritingFont font = settings.get(Settings.WRITING_FONT);
					float angle = (float)(Math.PI*2*getFraction(nibAngle));
					font.setProperty(WritingFont.NIB_ANGLE, angle);
				}
			}
		};
		
		nibAngle.addChangeListener(changeListener);
		italicizedAngle.addChangeListener(changeListener);


		onionSkinCheckbox.addActionListener(actionListener);
		onionSkin.addActionListener(actionListener);
		createStrokeButton.addActionListener(actionListener);
		deleteGlyphButton.addActionListener(actionListener);
		deleteStrokeButton.addActionListener(actionListener);
		createGlyphButton.addActionListener(actionListener);
		newButton.addActionListener(actionListener);
		browseButton.addActionListener(actionListener);
		stylePlain.addActionListener(actionListener);
		styleCalligraphy.addActionListener(actionListener);
		
		GraphicsEnvironment ge = GraphicsEnvironment.
				   getLocalGraphicsEnvironment();
		Font[] fonts = ge.getAllFonts();
		for(Font f : fonts) {
			onionSkin.addItem(f);
		}
		settings.set(Settings.ONION_SKIN_FONT, fonts[0]);
		
		refreshControls();

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		pack();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				File file = settings.get(Settings.FILE);
				if(file==null) return;
				String docName = file.getName();
				int result = QOptionPaneCommon.showSaveDialog(
						WritingApp.this, //Frame owner,
						"Writing App", //String appName,
						docName,
						file.getAbsolutePath(),
						false,
						QOptionPaneCommon.FILE_NORMAL,
						false);
				if(result==DialogFooter.SAVE_OPTION) {
					if(save(file)) {
						setVisible(false);
						System.exit(0);
					}
				} else if(result==DialogFooter.DONT_SAVE_OPTION) {
					setVisible(false);
				}
			}
		});
		
		settings.set(Settings.SELECTED_CHAR, 'a');
	}
	
	private void exportGIF() {
		File gifFile = doFileDialog(FileDialog.SAVE, "gif");
		WritingTextLayout wtl = sampleTextArea.getWritingLayout();
		if(gifFile!=null) {
			AnimationReader anim = wtl.createAnimation(sampleTextArea.getWidth(), false, 24, true);
			try {
				GifWriter.write(gifFile,anim,ColorReduction.FROM_ALL_FRAMES);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void exportMOV() {
		try {
			File movFile = doFileDialog(FileDialog.SAVE, "mov");
			WritingTextLayout wtl = sampleTextArea.getWritingLayout();
			if(movFile!=null) {
				AnimationReader anim = wtl.createAnimation(sampleTextArea.getWidth(), false, 24, false);
				JPEGMovWriter writer = new JPEGMovWriter(movFile);
				try {
					writer.addFrames(anim, null);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					writer.close(true);
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean save(File file) {
		WritingFont wfont = settings.get(Settings.WRITING_FONT);
		try {
			wfont.write(file);
			System.out.println("Saved: "+file.getAbsolutePath());
			return true;
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void refreshControls() {
		adjusting++;
		try {
			WritingFont font = settings.get(Settings.WRITING_FONT);
			if(font!=null) {
				String style = font.getProperty(WritingFont.STYLE);
				stylePlain.setSelected(style==null || WritingFont.STYLE_PLAIN.equals(style));
				styleCalligraphy.setSelected(WritingFont.STYLE_CALLIGRAPHY.equals(style));
				
				Number italicizedAngle = font.getProperty(WritingFont.ITALICIZED_ANGLE);
				if(italicizedAngle==null) italicizedAngle = 0;
	
				Number nibAngle = font.getProperty(WritingFont.NIB_ANGLE);
				if(nibAngle==null) nibAngle = -Math.PI/4;
				
				setFraction(this.italicizedAngle, italicizedAngle.doubleValue()/(2*Math.PI)-.25);
				setFraction(this.nibAngle, nibAngle.doubleValue()/(2*Math.PI) );
				
				this.nibAngle.setVisible( WritingFont.STYLE_CALLIGRAPHY.equals( style ) );
				nibAngleLabel.setVisible( WritingFont.STYLE_CALLIGRAPHY.equals( style ) );
				
				//glyph stuff:
				char ch = settings.get(Settings.SELECTED_CHAR);
				int selectedStroke = settings.get(Settings.SELECTED_STROKE);
				WritingShape glyph = font.getGlyph(ch);
				missingGlyphPanel.setVisible(glyph==null);
				strokeLabel.setVisible(glyph!=null);
				strokePanel.setVisible(glyph!=null);
				onionSkinCheckbox.setVisible(glyph!=null);
				onionSkin.setVisible(glyph!=null);
				createGlyphButton.setVisible(glyph==null);
				deleteGlyphButton.setVisible(glyph!=null);
				createStrokeButton.setVisible(true);
				deleteStrokeButton.setVisible(selectedStroke>=0);
				
				animationController.setEnabled(glyph!=null);
				animationController.setDuration(glyph==null ? 10 : glyph.getDuration(1, 1));
			} else {
				stylePlain.setSelected(true);
				styleCalligraphy.setSelected(false);
				nibAngle.setVisible( false );
				nibAngleLabel.setVisible( false );
				
				//glyph stuff:
				missingGlyphPanel.setVisible(false);
				strokeLabel.setVisible(false);
				strokePanel.setVisible(false);
				onionSkinCheckbox.setVisible(false);
				onionSkin.setVisible(false);
				createGlyphButton.setVisible(false);
				deleteGlyphButton.setVisible(false);
				createStrokeButton.setVisible(false);
				deleteStrokeButton.setVisible(false);
				animationController.setEnabled(false);
			}
			stylePlain.setEnabled(font!=null);
			styleCalligraphy.setEnabled(font!=null);
			nibAngle.setEnabled(font!=null);
			italicizedAngle.setEnabled(font!=null);
			charField.setEnabled(font!=null);
		} finally {
			adjusting--;
		}
	}
	
	private float getFraction(JSlider slider) {
		float v = slider.getValue() - slider.getMinimum();
		float range = slider.getMaximum() - slider.getMinimum();
		return v/range;
	}
	
	private void setFraction(JSlider slider,double fraction) {
		while(fraction>1)
			fraction--;
		while(fraction<0)
			fraction++;
		int range = slider.getMaximum() - slider.getMinimum();
		int v = (int)(fraction * range) + slider.getMinimum();
		slider.setValue(v);
	}
	
	private File doFileDialog(int mode,String ext) {
		Frame frame = (Frame)SwingUtilities.getWindowAncestor(WritingApp.this);
		FileDialog fd = new FileDialog(frame);
		fd.pack();
		fd.setLocationRelativeTo(null);
		fd.setFilenameFilter(new SuffixFilenameFilter( WritingFont.FILE_EXTENSION ));
		fd.setMode(mode);
		fd.setVisible(true);
		
		if(fd.getFile()==null) return null;
		String fileName = fd.getFile();
		if(!fileName.endsWith("."+ext))
			fileName = fileName+"."+ext;
		return new File(fd.getDirectory()+fileName);
	}
}
