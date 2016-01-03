/*
 * @(#)BasicAudioPlayerUI.java
 *
 * $Date: 2014-03-13 09:15:48 +0100 (Do, 13 Mär 2014) $
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
package com.bric.plaf;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;

import com.bric.audio.AudioPlayer.StartTime;
import com.bric.swing.AudioPlayerComponent;
import com.bric.swing.resources.PauseIcon;
import com.bric.swing.resources.TriangleIcon;

public class BasicAudioPlayerUI extends AudioPlayerUI {

	
	/** This method has to exist in order for to make this UI the button
	 * default by calling:
	 * <br><code>UIManager.getDefaults().put("ButtonUI", "com.bric.plaf.BevelButtonUI");</code>
	 */
    public static ComponentUI createUI(JComponent c) {
        return new BasicAudioPlayerUI();
    }
	
	static class URLPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		JTextField field = new JTextField(10);
		JLabel label = new JLabel("URL:");
		
		public URLPanel(String fieldText) {
			super(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0; c.gridy = 0; c.weightx = 0; c.weighty = 1;
			c.insets = new Insets(3,3,3,3);
			add(label, c);
			c.gridx++; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
			add(field, c);
			if(fieldText!=null)
				field.setText(fieldText);
		}
	}
	
	static class Fields {
		final AudioPlayerComponent apc;
		
		JButton playButton = new JButton(new TriangleIcon(SwingConstants.EAST, 12, 12));
		JButton pauseButton = new JButton(new PauseIcon(12, 12));
		//JSlider volumeSlider = new JSlider(0,100,100);
		JSlider playbackProgress = new JSlider(0,100,0);
		JButton browseButton = new JButton("Browse...");
		URLPanel urlPanel = new URLPanel(null);
	
		protected Fields(AudioPlayerComponent apc) {
			this.apc = apc;
		}
		
		protected void uninstall() {
			apc.remove(playButton);
			apc.remove(pauseButton);
			apc.remove(playbackProgress);
			apc.remove(browseButton);
			apc.remove(urlPanel);
		}
		
		protected void install() {
			apc.removeAll();
			apc.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1;
			gbc.insets = new Insets(3,3,3,3);
			gbc.fill = GridBagConstraints.NONE;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			apc.add(urlPanel, gbc);
			gbc.gridy++; gbc.weightx = 0;
			gbc.gridwidth = 1;
			apc.add(playButton, gbc);
			apc.add(pauseButton, gbc);
			gbc.gridx++; gbc.weightx = 1;
			apc.add(playbackProgress, gbc);
			gbc.gridx++; gbc.weightx = 0;
			apc.add(browseButton, gbc);
			
			playButton.setBorderPainted(false);
			pauseButton.setBorderPainted(false);
			
			browseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					apc.getUI().doBrowseForFile(apc);
				}
			});
			playButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					float startTime = 0;
					if(playbackProgress.getValue()<playbackProgress.getMaximum()) {
						startTime = (((float)(playbackProgress.getValue()-playbackProgress.getMinimum()))/
								((float)(playbackProgress.getMaximum()-playbackProgress.getMinimum())));
					}
					apc.getUI().doPlay(apc, new StartTime(startTime, true));
				}
			});
			pauseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					apc.getUI().doPause(apc);
				}
			});
			
			updateUI();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateUI();
				}
			});	
		}

		protected void updateUI() {
			if(!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						updateUI();
					}
				});
				return;
			}
			
			URL source = apc.getSource();
			String text = source==null ? "" : source.toString();
			urlPanel.field.setText(text);
			boolean playing = false;
			playButton.setVisible(!playing);
			pauseButton.setVisible(playing);
			
			Window w = SwingUtilities.getWindowAncestor(apc);
			browseButton.setEnabled(w!=null);
			if(browseButton.isEnabled()) {
				browseButton.setToolTipText("Select an audio file to play...");
			} else {
				browseButton.setToolTipText("Browsing is not supported in applets.");
			}
		}
	}
	
	private static final String FIELDS_KEY = BasicAudioPlayerUI.class.getName()+".fields";

	PropertyChangeListener updateSourceListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			JComponent src = evt.getSource() instanceof JComponent ? (JComponent)evt.getSource() : null;
			Fields fields = getFields(src);
			fields.updateUI();
		}
	};
	
	public BasicAudioPlayerUI() {}
	
	@Override
	public void installUI(JComponent c) {
		super.installUI(c);

		getFields(c).install();
		c.addPropertyChangeListener(AudioPlayerComponent.SOURCE_KEY, updateSourceListener);
	}
	
	protected Fields getFields(JComponent c) {
		if(c==null) return null;
		Fields fields = (Fields)c.getClientProperty(FIELDS_KEY);
		if(fields==null) {
			fields = new Fields( (AudioPlayerComponent)c );
			c.putClientProperty(FIELDS_KEY, fields);
		}
		return fields;
	}
	
	@Override
	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);

		getFields(c).uninstall();
		c.removePropertyChangeListener(AudioPlayerComponent.SOURCE_KEY, updateSourceListener);
	}

	@Override
	protected void notifyPlaybackStarted(AudioPlayerComponent apc) {
		Fields fields = getFields(apc);
		fields.playButton.setVisible(false);
		fields.pauseButton.setVisible(true);
	}
	
	@Override
	protected void notifyPlaybackProgress(AudioPlayerComponent apc,float timeElapsed, float timeAsFraction) {
		Fields fields = getFields(apc);
		int span = fields.playbackProgress.getMaximum()-fields.playbackProgress.getMinimum();
		int v = (int)(span*timeAsFraction + fields.playbackProgress.getMinimum());
		fields.playbackProgress.setValue( v );
	}

	@Override
	protected void notifyPlaybackStopped(AudioPlayerComponent apc,Throwable t) {
		Fields fields = getFields(apc);
		fields.playButton.setVisible(true);
		fields.pauseButton.setVisible(false);
	}
}
