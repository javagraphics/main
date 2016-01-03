/*
 * @(#)SoundListDemo.java
 *
 * $Date: 2015-02-28 21:59:45 +0100 (Sa, 28 Feb 2015) $
 *
 * Copyright (c) 2012 by Jeremy Wood.
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

import com.bric.audio.AudioPlayer;
import com.bric.blog.Blurb;
import com.bric.plaf.CloseDecoration;
import com.bric.plaf.DecoratedListUI;
import com.bric.plaf.DecoratedListUI.ListDecoration;
import com.bric.swing.resources.MusicIcon;

/** A simple demo for the <code>DecoratedListUI</code>, focusing on 
 * emulating Mac's playback controls for sound files.
 * 
 */
@Blurb (
filename = "SoundList",
title = "ListUIs: Adding decorations to cells",
releaseDate = "June 2012",
summary = "This demonstrates a new ListUI to add decorations to JList cells. "+
	"The demo emulates Apple's music file icons.",
instructions = "This applet demonstrates the <code>ListDecoration</code> feature by emulating how Apple's Finder "+
	"lets you play back audio files.\n"+
	"<p>(There might be a slight lag between click the play button and hearing audio; that has to do with the audio "+
	"buffering and not the list UI!)",
link = "http://javagraphics.blogspot.com/2012/06/listuis-adding-decorations-to-cells.html",
sandboxDemo = true
)
public class SoundListDemo extends BricApplet {
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
				
				final JFrame frame = new JFrame("Sound List Demo");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().add(new SoundListDemo());
				frame.pack();
				frame.setVisible(true);
			}
		});
	}
		
	static class SoundSource {
		String name;
		URL url;
		
		SoundSource(String name) {
			this.name = name;
			url = SoundListDemo.class.getResource("resources/"+name);
		}
	}
	
	class SoundUI {
		SoundSource sound;

		
		float trackSize = 0;
		float targetTrackSize = 0;
		float opacity = 1;
		float targetOpacity = 1;

		SourceDataLine line;
		float completion = 0;
		long totalFrames = 1;
		
		List<ChangeListener> completionListeners = new Vector<ChangeListener>();
		
		
		Timer timer = new Timer(50, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean active = false;

				float newCompletion = 0;
				if(line!=null) {
					newCompletion = ((float)line.getFramePosition())/((float)totalFrames);
				}
				if(setCompletion(newCompletion))
					active = true;
				
				if(trackSize<targetTrackSize) {
					if(setTrackSize(Math.min(targetTrackSize, trackSize + .25f)))
						active = true;
				} else if(trackSize>targetTrackSize) {
					if(setTrackSize(Math.max(targetTrackSize, trackSize - .25f)))
						active = true;
				}
				
				if(opacity<targetOpacity) {
					if(setOpacity(Math.min(targetOpacity, opacity + .25f)))
						active = true;
				} else if(opacity>targetOpacity) {
					if(setOpacity(Math.max(targetOpacity, opacity - .25f)))
						active = true;
				}
				
				if(!active) {
					timer.stop();
				}
			}
		});
		
		SoundUI(SoundSource sound) {
			this.sound = sound;
		}

		
		private boolean setCompletion(float f) {
			if(completion==f)
				return false;
			completion = f;
			timer.start();
			repaintListCell();
			return true;
		}
		
		public float getCompletion() {
			return completion;
		}
		
		public boolean isPlaying() {
			return line!=null && line.isOpen() && line.isActive();
		}
		
		public void play() {
			try {
				if(line!=null && line.isOpen()) {
					line.start();
				} else {
					AudioInputStream audioIn = AudioSystem.getAudioInputStream(sound.url);
					totalFrames = audioIn.getFrameLength();
					line = AudioPlayer.playAudioStream(audioIn);
					line.addLineListener(new LineListener() {
						public void update(LineEvent e) {
							updateTargetTrackSize();
						}
					});
					timer.start();
					updateTargetTrackSize();
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		private void updateTargetTrackSize() {
			setTargetTrackSize(line.isOpen() && line.isActive() ? 1 : 0);
		}
		
		public void pause() {
			if(line!=null) {
				line.stop();
			}
		}
		
		protected void setTargetTrackSize(float s) {
			if(targetTrackSize==s) return;
			targetTrackSize = s;
			timer.start();
		}
		
		protected void setTargetOpacity(float s) {
			if(targetOpacity==s) return;
			targetOpacity = s;
			timer.start();
		}
		
		public float getTrackSize() {
			return trackSize;
		}
		
		public float getOpacity() {
			return opacity;
		}
		
		private void repaintListCell() {
			int index = -1;
			for(int a = 0; a<list.getModel().getSize(); a++) {
				if(list.getModel().getElementAt(a)==sound)
					index = a;
			}
			
			Rectangle cellBounds = list.getCellBounds(index, index);
			if(cellBounds!=null)
				list.repaint( cellBounds );
		}

		private boolean setTrackSize(float f) {
			if(trackSize==f)
				return false;
			trackSize = f;
			repaintListCell();
			return true;
		}

		private boolean setOpacity(float f) {
			if(opacity==f)
				return false;
			opacity = f;
			repaintListCell();
			return true;
		}
	}
	
	static class SoundCellRenderer implements ListCellRenderer {
		JLabel label = new JLabel();
		
		SoundCellRenderer() { 
			label.setIcon(new MusicIcon(128));
			label.setVerticalTextPosition(SwingConstants.BOTTOM);
			label.setHorizontalTextPosition(SwingConstants.CENTER);
			label.setBorder(new EmptyBorder(12, 12, 12, 12));
			label.setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object soundObject,
				int row, boolean isSelected, boolean hasFocus) {
			SoundSource sound = (SoundSource)soundObject;
			label.setText(sound.name);
			
			if(isSelected) {
				label.setBackground(SystemColor.textHighlight);
				label.setForeground(SystemColor.textHighlightText);
			} else {
				label.setBackground(SystemColor.text);
				label.setForeground(SystemColor.textText);
			}
			return label;
		}
		
	}
	
	DefaultListModel listModel = new DefaultListModel();
	JList list = new JList(listModel);
	
	ListDecoration playbackDecoration = new ListDecoration() {
		MusicIcon.PlayToggleIcon icon = new MusicIcon.PlayToggleIcon( MusicIcon.PlayToggleIcon.DEFAULT_WIDTH );

		WeakHashMap<SoundSource, SoundUI> map = new WeakHashMap<SoundSource, SoundUI>();
		
		private SoundUI getSoundUI(SoundSource soundSource) {
			SoundUI soundUI = map.get(soundSource);
			if(soundUI==null) {
				soundUI = new SoundUI(soundSource);
				map.put(soundSource, soundUI);
			}
			return soundUI;
		}
		
		@Override
		public Icon getIcon(JList list, Object value, int row,
				boolean isSelected, boolean cellHasFocus, boolean isRollover,
				boolean isPressed) {
			SoundSource sound = (SoundSource)value;
			SoundUI soundUI = getSoundUI(sound);
			
			icon.setPressed(isPressed);
			if(soundUI.isPlaying()) {
				soundUI.setTargetOpacity( 1 );
				icon.setPauseIconVisible(true);
				icon.setTrackCompletion( soundUI.getCompletion() );
			} else {
				soundUI.setTargetOpacity( (isRollover || isPressed) ? 1 : 0 );
				icon.setPauseIconVisible(false);
			}
			icon.setTrackSize(soundUI.getTrackSize());
			icon.setOpacity(soundUI.getOpacity());
			return icon;
		}

		@Override
		public boolean isVisible(JList list, Object value, int row,
				boolean isSelected, boolean cellHasFocus) {
			SoundSource sound = (SoundSource)value;
			SoundUI ui = getSoundUI(sound);
			
			if(!isSelected) {
				SourceDataLine dataLine = ui.line;
				if(dataLine!=null)
					dataLine.close();
			}
			
			boolean visible = isSelected;
			if(!visible) {
				ui.setTargetOpacity(0);
			}
			if(ui.targetOpacity>0) visible = true;
			return visible;
		}
		
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = list.getLeadSelectionIndex();
				
				SoundSource sound = (SoundSource)list.getModel().getElementAt(i);
				SoundUI soundUI = getSoundUI(sound);
				if(soundUI.isPlaying()) {
					soundUI.pause();
				} else {
					soundUI.play();
				}
			}
		};

		@Override
		public ActionListener getActionListener(JList list, Object value,
				int row, boolean isSelected, boolean cellHasFocus) {
			return actionListener;
		}

		@Override
		public Point getLocation(JList list, Object value, int row,
				boolean isSelected, boolean cellHasFocus) {
			Rectangle r = list.getCellBounds(row, row);
			return new Point(r.width/2 - icon.getIconWidth()/2, 
					r.height/2 - icon.getIconHeight()/2 - 10);
		}
		
	};
	
	class RemoveActionListener implements ActionListener {
		Object element;
		
		RemoveActionListener(Object element) {
			this.element = element;
		}
		
		public void actionPerformed(ActionEvent e) {
			listModel.removeElement(element);
		}
	}
	
	CloseDecoration closeDecoration = new CloseDecoration() {

		@Override
		public ActionListener getActionListener(JList list, Object value,
				int row, boolean isSelected, boolean cellHasFocus) {
			return new RemoveActionListener(value);
		}
	};
	
	public SoundListDemo() {
		super();
		listModel.addElement(new SoundSource("Bugaboo.wav"));
		listModel.addElement(new SoundSource("Ludic.wav"));
		listModel.addElement(new SoundSource("Sphynx.wav"));
		listModel.addElement(new SoundSource("Tizzy.wav"));
		listModel.addElement(new SoundSource("Unctuous.wav"));
	
		list.setCellRenderer(new SoundCellRenderer());
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(2);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setPreferredSize(new Dimension(152*3, 172*2));
		
		list.setUI(new DecoratedListUI());
		list.putClientProperty(DecoratedListUI.KEY_DECORATIONS, new ListDecoration[] { closeDecoration, playbackDecoration });
		
		list.setBackground(Color.white);
		list.setOpaque(true);
		
		getContentPane().add(list);
	}
}
