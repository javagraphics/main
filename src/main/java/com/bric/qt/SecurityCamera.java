/*
 * @(#)SecurityCamera.java
 *
 * $Date: 2015-02-28 21:59:45 +0100 (Sa, 28 Feb 2015) $
 *
 * Copyright (c) 2013 by Jeremy Wood.
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
package com.bric.qt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import quicktime.QTException;
import quicktime.QTSession;
import quicktime.std.StdQTException;

import com.bric.blog.Blurb;
import com.bric.qt.io.JPEGMovWriter;
import com.bric.swing.TextFieldPrompt;
import com.bric.util.Text;

/** This uses QTJ to record streaming 10-minute movies to local files.
 * 
 */
@Blurb (
filename = "SecurityCamera",
title = "QuickTime: Implementing a Security Camera Feed",
releaseDate = "May 2013",
summary = "When our HOA was having some security problems: I set up a $40 HD webcam "+
	"in my bedroom window and designed this app to continually stream movie footage to my "+
	"backup drive.",
scrapped = "QuickTime for Java is deprecated and unsupported.",
sandboxDemo = false
)
public class SecurityCamera extends Playback {
	private static final long serialVersionUID = 1L;

	static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
	static DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.FULL);
	
	public static void main(String[] args) {
		try {
			QTSession.open();
			JFrame frame = new JFrame("Security Camera");
			final SecurityCamera playbackPanel = new SecurityCamera(new Dimension(1024,768));
			GridBagConstraints c = new GridBagConstraints();
			frame.getContentPane().setLayout(new GridBagLayout());
			c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0;
			c.fill = GridBagConstraints.BOTH;
			frame.getContentPane().add(new Controls(playbackPanel), c);
			c.gridy++; c.weighty = 1;
			frame.getContentPane().add(playbackPanel, c);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					try {
						playbackPanel.finalize();
						System.exit(0);
					} catch(Throwable t) {
						t.printStackTrace();
					}
				}
			});
			
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		} catch(QTException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	static class Controls extends JPanel {
		private static final long serialVersionUID = 1L;

		JButton resumeRecording = new JButton("Resume Recording");
		JButton pauseRecording = new JButton("Pause Recording");
		JCheckBox playthrough = new JCheckBox("Playthrough Feedback");
		JCheckBox pauseAt = new JCheckBox("Pause Recording At:");
		JCheckBox resumeAt = new JCheckBox("Resume Recording At:");
		JTextField pauseField = new JTextField(12);
		JTextField resumeField = new JTextField(12);
		JButton settingsButton = new JButton("Settings...");
		JCheckBox flip = new JCheckBox("Flip Horizontal");
		SecurityCamera camera;
		
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if(e.getSource()==pauseRecording) {
						camera.isRecording = false;
					} else if(e.getSource()==resumeRecording) {
						camera.isRecording = true;
					} else if(e.getSource()==settingsButton) {
						try {
							camera.mVideo.settingsDialog();
						} catch (StdQTException e1) {
							e1.printStackTrace();
						}
					} else if(e.getSource()==playthrough) {
						camera.playthrough = playthrough.isSelected();
					} else if(e.getSource()==flip) {
						camera.qtImage.setFlipHorizontal( flip.isSelected() );
					} else if(e.getSource()==pauseAt) {
						//TODO
					} else if(e.getSource()==resumeAt) {
						//TODO
					}
				} finally {
					updateControls();
				}
			}
		};
		boolean pauseApplied = false;
		boolean resumeApplied = false;
		
		DocumentListener docListener = new DocumentListener() {

			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			public void changedUpdate(DocumentEvent e) {
				JTextField field;
				if(e.getDocument()==pauseField.getDocument()) {
					field = pauseField;
				} else {
					field = resumeField;
				}
				
				try {
					timeFormat.parse(field.getText());
					field.setForeground(Color.black);
					if(e.getDocument()==pauseField.getDocument()) {
						pauseApplied = false;
					} else {
						resumeApplied = false;
					}
				} catch (ParseException e1) {
					e1.printStackTrace();
					field.setForeground(Color.red);
				}
			}
			
		};
		
		public Controls(SecurityCamera camera) {
			JPanel fluff1 = new JPanel();
			JPanel fluff2 = new JPanel();
			fluff1.setOpaque(false);
			fluff2.setOpaque(false);
			
			this.camera = camera;
			camera.controls = this;
			GridBagConstraints c = new GridBagConstraints();
			setLayout(new GridBagLayout());
			c.gridx = 0; c.gridy = 0; c.weightx = 0; c.weighty = 0;
			c.insets = new Insets(3,3,3,3);
			add(pauseRecording, c);
			add(resumeRecording, c);
			c.gridy++;
			add(settingsButton, c);
			c.gridy++;
			add(playthrough, c);
			c.gridx++;
			c.gridy = 0; c.weightx = 1;
			add(fluff1, c);
			add(fluff2, c);
			c.gridx++; c.gridy = 0;
			c.weightx = 0;
			c.anchor = GridBagConstraints.EAST;
			add(pauseAt, c);
			c.gridy++;
			add(resumeAt, c);
			c.gridx++; c.gridy = 0;
			add(pauseField, c);
			c.gridy++;
			add(resumeField, c);
			c.gridy++;
			c.gridwidth = 2; c.gridx--;
			add(flip, c);
			
			pauseRecording.addActionListener(actionListener);
			resumeRecording.addActionListener(actionListener);
			settingsButton.addActionListener(actionListener);
			playthrough.addActionListener(actionListener);
			flip.addActionListener(actionListener);
			pauseAt.addActionListener(actionListener);
			resumeAt.addActionListener(actionListener);
			pauseField.getDocument().addDocumentListener(docListener);
			resumeField.getDocument().addDocumentListener(docListener);

			new TextFieldPrompt(pauseField, "6:30:00 AM CDT");
			new TextFieldPrompt(resumeField, "8:30:00 PM CDT");
			
			Thread maintainPauseAndResume = new Thread("Maintain Pause and Resume") {
				public void run() {
					//there has to be an easier way to do this... but this works for now:
					Date pauseTime, resumeTime;
					DateFormat format = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
					timeFormat.setLenient(true);
					Date epoch;
					try {
						epoch = format.parse("Thursday, January 1, 1970 12:00:00 AM CDT");
					} catch(Exception e) {
						e.printStackTrace();
						return;
					}
					Date lastCurrentDay = null;
					while(true) {
						Date current = new Date();
						
						String date = format.format(current);
						int i = date.indexOf(':');
						i = date.lastIndexOf(' ', i);
						date = date.substring(0, i);
						date = date + " 12:00:00 AM CDT";
						Date currentDay;
						try {
							currentDay = format.parse(date);
						} catch(Exception e) {
							e.printStackTrace();
							return;
						}
						
						if(!currentDay.equals(lastCurrentDay)) {
							pauseApplied = false;
							resumeApplied = false;
							lastCurrentDay = currentDay;
						}
						
						try {
							pauseTime = pauseAt.isSelected() ? timeFormat.parse(pauseField.getText()) : null;
							pauseTime = new Date(pauseTime.getTime() - epoch.getTime() + currentDay.getTime() );
						} catch(Exception e) {
							pauseTime = null;
						}
						
						try {
							resumeTime = resumeAt.isSelected() ? timeFormat.parse(resumeField.getText()) : null;
							resumeTime = new Date(resumeTime.getTime() - epoch.getTime() + currentDay.getTime() );
						} catch(Exception e) {
							resumeTime = null;
						}
						
						if(pauseTime!=null && current.after(pauseTime) && Controls.this.camera.isRecording) {
							if(pauseApplied==false) {
								pauseRecording.doClick();
								pauseApplied = true;
							}
						} else if(resumeTime!=null && current.after(resumeTime) && (!Controls.this.camera.isRecording)) {
							if(resumeApplied==false) {
								resumeRecording.doClick();
								resumeApplied = true;
							}
						}
						
						try {
							Thread.sleep(1000);
						} catch(Exception e) {}
					}
				}
			};
			maintainPauseAndResume.start();
			
			updateControls();
		}
		
		private void updateControls() {
			if(!camera.isRecording) {
				camera.msRepaintDelay = 2500;
			} else {
				camera.msRepaintDelay = 250;
			}
			pauseRecording.setVisible(camera.isRecording);
			resumeRecording.setVisible(!camera.isRecording);
			flip.setSelected(camera.qtImage.isFlipHorizontal());
			pauseField.setEnabled(pauseAt.isSelected());
			resumeField.setEnabled(resumeAt.isSelected());
			playthrough.setSelected(camera.playthrough);
			camera.repaint();
		}
	}
	
	Controls controls;
	boolean isRecording = true;
	File movFile;
	JPEGMovWriter movWriter;
	long movStartTime = -1;
	File rootDir;
	
	public SecurityCamera(Dimension size) throws QTException {
		super(size);
		msRepaintDelay = 250;
		FileDialog fd = new FileDialog(new Frame(), "Save Location", FileDialog.SAVE);
		fd.pack();
		fd.setVisible(true);
		rootDir = new File(fd.getDirectory());
		
		Thread recordingThread = new Thread("Recording...") {
			
			private void stopRecording() throws IOException {
				movWriter.close(false);
				System.out.println("Wrote: "+(movFile.length()/1024/1024)+" MB");
				movWriter = null;
			}
			
			public void run() {
				while(true) {
					try {
						synchronized(SecurityCamera.this) {
							if(isRecording) {
								if(movWriter==null) {
									Date date = new Date();
									String timeString = timeFormat.format(date).trim();
									String filename = timeString.replace(':', '.')+".mov";
									movStartTime = System.currentTimeMillis();
									
									if(filename.contains("PM")) {
										int k = filename.indexOf('.');
										int hour = Integer.parseInt(filename.substring(0, k));
										hour += 12;
										filename = hour + filename.substring(k);
									}
									
									filename = Text.replace(filename, " AM", "");
									filename = Text.replace(filename, " PM", "");
									
									movFile = null;
									if(rootDir.exists()) {
										String dateString = dateFormat.format(date).trim();
										dateString = dateString.replace("/", ".");
										File dayDir = new File(rootDir, dateString);
										if(dayDir.exists() || dayDir.mkdirs()) {
											movFile = new File(dayDir, filename);
										}
									}
									if(movFile==null)
										movFile = new File(filename);
									
									System.out.println("Writing to: "+movFile.getAbsolutePath());
									movWriter = new JPEGMovWriter(movFile);
								}
								
								if(System.currentTimeMillis() - movStartTime > 60*10*1000) {
									stopRecording();
								}
							} else {
								if(movWriter!=null) {
									stopRecording();
								}
							}
						}
					} catch(IOException e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(50);
					} catch(Exception e) {}
				}
			}
		};
		recordingThread.start();
	}
	
	@Override
	protected void postProcess(BufferedImage bi) {
		try {
			synchronized(SecurityCamera.this) {
				if(movWriter!=null) {
					movWriter.addFrame( (float)(msRepaintDelay)/1000f, bi, .8f);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void process(BufferedImage bi) {
		Graphics2D g = bi.createGraphics();
		Date date = new Date();
		String dateString = timeFormat.format(date).trim()+" ("+dateFormat.format(date).trim()+")";
		drawText(g, dateString, Color.white, SwingConstants.SOUTH_WEST, false);
		g.dispose();
	}
	
	@Override
	protected void paintComponent(Graphics g0) {
		Graphics2D g = (Graphics2D)g0;
		g.setColor(Color.gray);
		g.fillRect(0,0,getWidth(),getHeight());
		synchronized(SecurityCamera.this) {
			if(playthrough && bufferedImage!=null)
				g.drawImage(bufferedImage,0,0,getWidth(),getHeight(),0,0,bufferedImage.getWidth(),bufferedImage.getHeight(),null);
		}
		if(isRecording) {
			drawText(g, "Recording...", Color.red, SwingConstants.NORTH_WEST, true);
			String name = movFile==null ? null : movFile.getName();
			if(name!=null) {
				drawText(g, movFile.getName(), Color.white, SwingConstants.NORTH_EAST, true);
			}
		} else {
			drawText(g, "Not Recording", Color.white, SwingConstants.NORTH_WEST, true);
		}
		if(!playthrough) {
			drawText(g, "Playthrough Disabled", Color.orange, SwingConstants.SOUTH_EAST, true);
		}
	}
	
	protected Font font = new Font("Verdana",Font.BOLD,13);
	protected void drawText(Graphics2D g,String string,Color color,int placement,boolean relativeToPanel) {
		if(bufferedImage==null) return;
		
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setFont(font);
		FontRenderContext frc = g.getFontRenderContext();
		GlyphVector gv = g.getFont().createGlyphVector(frc, string);
		Rectangle2D r = gv.getVisualBounds();
		float x, y;
		if(placement==SwingConstants.NORTH_EAST || placement==SwingConstants.NORTH_WEST) {
			y = (float)( r.getHeight()+5 );
		} else if(placement==SwingConstants.SOUTH_EAST || placement==SwingConstants.SOUTH_WEST) {
			if(relativeToPanel) {
				y = (float)( getHeight() - r.getHeight() );
			} else {
				y = (float)( bufferedImage.getHeight() - r.getHeight() );
			}
		} else {
			throw new IllegalArgumentException("unrecognized placement: "+placement);
		}
		if(placement==SwingConstants.NORTH_EAST || placement==SwingConstants.SOUTH_EAST) {
			if(relativeToPanel) {
				x = (float)( getWidth() - r.getWidth()-5 );
			} else {
				x = (float)( bufferedImage.getWidth() - r.getWidth()-5 );
			}
		} else if(placement==SwingConstants.NORTH_WEST || placement==SwingConstants.SOUTH_WEST) {
			x = (float)( 5 );
		} else {
			throw new IllegalArgumentException("unrecognized placement: "+placement);
		}
		Shape shape = gv.getOutline(x, y);
		g.setStroke(new BasicStroke(3));
		g.setColor(Color.black);
		g.draw(shape);
		g.setColor(color);
		g.fill(shape);
	}
}
