/*
 * @(#)AudioPlayer.java
 *
 * $Date: 2014-06-06 20:04:49 +0200 (Fr, 06 Jun 2014) $
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
package com.bric.audio;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Timer;

import com.bric.swing.BasicCancellable;
import com.bric.swing.Cancellable;

/** An object that manages playing back a resource.
 * 
 * This offers the ability to play and pause, and adds listeners
 * (either in real-time or at fixed polling intervals).
 */
public class AudioPlayer {
	
	public static class StartTime {
		float time;
		boolean asFraction;
		
		public StartTime(float time,boolean asFraction) {
			this.time = time;
			this.asFraction = asFraction;
		}
	}
	
	/** A glimpse of the playback state.
	 */
	private static class Snapshot {
		long playAttempt;
		float fraction = 0;
		float elapsedTime = 0;
		boolean complete = false;
		boolean active = false;
		Throwable error = null;
		
		public Snapshot(long playAttempt,float fraction,float elapsedTime,boolean complete,boolean active,Throwable error) {
			this.playAttempt = playAttempt;
			this.fraction = fraction;
			this.elapsedTime = elapsedTime;
			this.active = active;
			this.complete = complete;
			this.error = error;
		}
		
		@Override
		public String toString() {
			return "Snapshot[ attempt="+playAttempt+", fraction="+fraction+", elapsed="+elapsedTime+", complete="+complete+", active="+active+", error="+error+" ]";
		}
		
		@Override
		public boolean equals(Object t) {
			if(t==this) return true;
			if(!(t instanceof Snapshot))
				return false;
			Snapshot s = (Snapshot)t;
			return fraction==s.fraction && 
					elapsedTime==s.elapsedTime && 
					active==s.active &&
					complete==s.complete && 
					Objects.equals(error, s.error);
		}
		
		private synchronized void resetAndIncrement() {
			playAttempt++;
			fraction = 0;
			elapsedTime = 0;
			complete = false;
			active = false;
			error = null;
		}
		
		public Snapshot clone() {
			return new Snapshot(playAttempt, fraction, elapsedTime, complete, active, error);
		}
	}
	
	public static interface Listener {
		public void playbackStarted();
		public void playbackProgress(float timeElapsed,float timeAsFraction);
		public void playbackStopped(Throwable t);
	}
	
	private static class NullListener implements Listener {
		public void playbackStarted() {}
		public void playbackProgress(float timeElapsed,float timeAsFraction) {}
		public void playbackStopped(Throwable t) {}
	}

	private static int threadNameCtr = 0;
	
	private static class PlayAudioThread extends Thread {
		AudioInputStream stream;
		SourceDataLine dataLine;
		Listener listener;
		Cancellable cancellable;
		StartTime startTime;
		
		PlayAudioThread(AudioInputStream stream,StartTime startTime, SourceDataLine dataLine,Listener listener,Cancellable cancellable) {
			super("Play Audio "+(threadNameCtr++));
			if(stream==null) throw new NullPointerException();
			if(dataLine==null) throw new NullPointerException();
			this.stream = stream;
			this.dataLine = dataLine;
			this.startTime = startTime;
			this.listener = listener==null ? new NullListener() : listener;
			this.cancellable = cancellable==null ? new BasicCancellable() : cancellable;
		}
		
		private void skipFully(InputStream in,long amount) throws IOException {
			while(amount>0) {
				long t = in.skip(amount);
				if(t<=0) return;
				amount -= t;
			}
		}
		
		/**
		 * This method was adapted from a discussion found online.
		 * 
		 * @see <a href="http://www.daniweb.com/software-development/java/threads/17484">http://www.daniweb.com/software-development/java/threads/17484</a>
		 */
		@Override
		public void run() {
			try {
				AudioFormat audioFormat = stream.getFormat();
				try {
					long bytesWritten = 0;
					long totalFrames = stream.getFrameLength();
					float totalTime = totalFrames/audioFormat.getFrameRate();
					int frameSize = audioFormat.getFrameSize();
					int bufferSize = (int) audioFormat.getSampleRate() * frameSize;
					byte [] buffer = new byte[ bufferSize ];
					
					long bytesSkipped = 0;
					float startTimeInSeconds;
					if(startTime!=null && startTime.asFraction) {
						startTimeInSeconds = startTime.time * totalTime;
					} else if(startTime!=null) {
						startTimeInSeconds = startTime.time;
					} else {
						startTimeInSeconds = 0;
					}
					bytesSkipped = (long)(startTimeInSeconds*audioFormat.getFrameRate()*audioFormat.getFrameSize());
					float framesSkipped = bytesSkipped / audioFormat.getFrameSize();
					skipFully(stream, bytesSkipped);

					// Move the data until done or there is an error.
					try {
						int bytesRead = 0;
						listener.playbackStarted();
						while ( bytesRead >= 0 ) {
							
							bytesRead = stream.read( buffer, 0, buffer.length );
							if ( bytesRead >= 0 ) {
								int ptr = 0;
								//write the data in such a way that we check constantly for cancellation:
								while(ptr<bytesRead) {
									if( (!dataLine.isOpen()) ||
											cancellable.isCancelled() ) {
										listener.playbackStopped(null);
										return;
									}
									
									int t = dataLine.write( buffer, ptr, Math.min(bytesRead-ptr, 1024) );
									ptr += t;
									bytesWritten += t;

									float newCompletion = ((float)dataLine.getFramePosition()+framesSkipped)/((float)totalFrames);
									listener.playbackProgress(totalTime*newCompletion, newCompletion);
								}
							}
						}
						listener.playbackProgress(totalTime, 1);
						listener.playbackStopped(null);
					} catch ( IOException e ) {
						listener.playbackStopped(e);
					}
			
					dataLine.drain();
				} finally {
					dataLine.close();
				}
			} catch(Exception e) {
				listener.playbackStopped(e);
			}
		}
	}

	/** Plays audio from the given audio input stream on a separate thread.
	 * @throws LineUnavailableException if a line is unavailable.
	 * @throws UnsupportedOperationException if this AudioPlayer doesn't support playing the stream argument
	 */
	public static SourceDataLine playAudioStream(AudioInputStream stream) throws UnsupportedOperationException, LineUnavailableException {
		return playAudioStream(stream, null, null, null, false);
	}
	
	/** Plays audio from the given audio input stream.
	 * 
	 * @param stream the AudioInputStream to play.
	 * @param startTime the time to skip to when playing starts.
	 * A value of zero means this plays from the beginning, 1 means it skips one second, etc.
	 * @param listener an optional Listener to update.
	 * @param cancellable an optional Cancellable to consult.
	 * @param blocking whether this call is blocking or not.
	 * @throws LineUnavailableException if a line is unavailable.
	 * @throws UnsupportedOperationException if this static method doesn't support playing the stream argument
	 **/
	public static SourceDataLine playAudioStream(AudioInputStream stream,StartTime startTime,Listener listener,Cancellable cancellable,boolean blocking) throws UnsupportedOperationException, LineUnavailableException {
		AudioFormat audioFormat = stream.getFormat();
		DataLine.Info info = new DataLine.Info( SourceDataLine.class, audioFormat );
		if ( !AudioSystem.isLineSupported( info ) ) {
			throw new UnsupportedOperationException("AudioPlayback.playAudioStream: info="+info );
		}
		
		final SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine( info );
		dataLine.open( audioFormat );
		dataLine.start();

		PlayAudioThread thread = new PlayAudioThread(stream, startTime, dataLine, listener, cancellable);
		if(blocking) {
			thread.run();
		} else {
			thread.start();
		}
		
		return dataLine;
	}
	
	List<Listener> realTimeListeners = new ArrayList<Listener>();
	Map<Integer, List<Listener>> edtListeners = new HashMap<Integer, List<Listener>>();
	private final Snapshot currentSnapshot = new Snapshot(-1, 0, 0, false, false, null);
	private Map<Integer, Timer> timers = new HashMap<Integer, Timer>();
	Map<Long, Throwable> playbackErrors = new HashMap<Long, Throwable>();
	final URL source;
	
	public AudioPlayer(URL source) {
		if(source==null) throw new NullPointerException();
		this.source = source;
		
		/** Add one listener that updates the currentSnapshot. All timed listeners
		 * rely on the snapshot.
		 */
		addListener(new Listener() {
			public void playbackProgress(float timeElapsed,float timeAsFraction) {
				currentSnapshot.elapsedTime = timeElapsed;
				currentSnapshot.fraction = timeAsFraction;
			}
			
			public void playbackStarted() {
				currentSnapshot.active = true;
			}

			public void playbackStopped(Throwable t) {
				playbackErrors.put(currentSnapshot.playAttempt, t);
				currentSnapshot.active = false;
				currentSnapshot.complete = true;
				currentSnapshot.error = t;
			}
		});
	}
	
	/** This listener will receive coalesced events at a given
	 * interval on the event dispatch thread. This will in no
	 * way impact audio playback.
	 * 
	 * @param l the listener to add.
	 * @param updateInterval the number of milliseconds between
	 * possible events.
	 */
	public void addEDTListener(Listener l,int updateInterval) {
		synchronized(edtListeners) {
			List<Listener> list = edtListeners.get(updateInterval);
			if(list==null) {
				list = new ArrayList<Listener>();
				list.add(l);
				edtListeners.put(updateInterval, list);
				setTimerActive(updateInterval, true);
			} else {
				list.add(l);
			}
		}
	}
	
	/** This listener will be updated in real-time, in the 
	 * thread that is processing the audio. If this method
	 * is expensive: sound playback will be choppy as we wait
	 * for this listener to finish.
	 */
	public void addListener(Listener l) {
		synchronized(realTimeListeners) {
			realTimeListeners.add(l);
		}
	}

	/** Remove a listener previously added by calling <code>addListener()</code>.
	 * 
	 * @param l the listener to remove.
	 * @return true if this listener was removed.
	 */
	public boolean removeListener(Listener l) {
		synchronized(realTimeListeners) {
			return realTimeListeners.remove(l);
		}
	}

	/** Remove a listener previously added by calling <code>addEDTListener()</code>.
	 * 
	 * @param l the listener to remove.
	 * @return true if this listener was removed.
	 */
	public boolean removeEDTListener(Listener l) {
		synchronized(edtListeners) {
			boolean returnValue = false;
			for(Integer updateInterval : edtListeners.keySet()) {
				List<Listener> list = edtListeners.get(updateInterval);
				synchronized(list) {
					if(list.remove(l)) {
						returnValue = true;
						if(list.size()==0) {
							setTimerActive(updateInterval, false);
						}
					}
				}
			}
			return returnValue;
		}
	}
	
	private class CoalesceUpdateActionListener implements ActionListener {
		int updateInterval;
		Snapshot lastBroadcast;
		
		CoalesceUpdateActionListener(int updateInterval) {
			this.updateInterval = updateInterval;
		}

		public void actionPerformed(ActionEvent e) {
			List<Listener> listeners;
			synchronized(edtListeners) {
				listeners = edtListeners.get(updateInterval);
			}
			if(listeners==null) return;
			
			Snapshot currentBroadcast = currentSnapshot.clone();
			if(currentBroadcast.equals(lastBroadcast))
				return;
			//TODO: also shutdown timers when appropriate
			
			synchronized(listeners) {
				for(Listener listener : listeners) {
					//if this is a whole new attempt, then notify everyone that playback stopped:
					if(lastBroadcast!=null && lastBroadcast.playAttempt!=currentBroadcast.playAttempt) {
						Throwable t = playbackErrors.get(lastBroadcast.playAttempt);
						listener.playbackStopped(t);
					}
				}

				if(lastBroadcast!=null && lastBroadcast.playAttempt!=currentBroadcast.playAttempt) {
					//destroy the previous record, we're not comparing against it anymore
					lastBroadcast = null;
				}
				
				Throwable lastError = lastBroadcast==null ? null : lastBroadcast.error;
				boolean lastActive = lastBroadcast==null ? false : lastBroadcast.active;
				boolean lastComplete = lastBroadcast==null ? false : lastBroadcast.complete;
				float lastFraction = lastBroadcast==null ? -1 : lastBroadcast.fraction;
				float lastElapsed = lastBroadcast==null ? -1 : lastBroadcast.elapsedTime;
				
				for(Listener listener : listeners) {
					//always broadcast this first
					if(lastActive!=currentBroadcast.active) {
						listener.playbackStarted();
					}
					
					//then we might broadcast any of these:
					if(!Objects.equals(currentBroadcast.error, lastError)) {
						listener.playbackProgress(currentBroadcast.elapsedTime, currentBroadcast.fraction);
						listener.playbackStopped(currentBroadcast.error);
					} else if(lastComplete!=currentBroadcast.complete) {
						listener.playbackProgress(currentBroadcast.elapsedTime, currentBroadcast.fraction);
						listener.playbackStopped(null);
					} else if(lastFraction!=currentBroadcast.fraction || lastElapsed!=currentBroadcast.elapsedTime) {
						listener.playbackProgress(currentBroadcast.elapsedTime, currentBroadcast.fraction);
					}
				}
				lastBroadcast = currentBroadcast;
			}
		}
	};
	
	private void setTimerActive(int updateInterval,boolean activeState) {
		Timer timer = timers.get(updateInterval);
		if(timer==null) {
			timer = new Timer(updateInterval, new CoalesceUpdateActionListener(updateInterval));
			timers.put(updateInterval, timer);
		}
		if(activeState) {
			timer.start();
		} else {
			timer.stop();
		}
	}

	public URL getSource() {
		return source;
	}
	
	/** This is passed to the static play method to notify all this AudioPlayer's
	 * real-time listeners.
	 */
	Listener masterListener = new Listener() {

		public void playbackStarted() {
			synchronized(realTimeListeners) {
				for(int a = 0; a<realTimeListeners.size(); a++) {
					try {
						realTimeListeners.get(a).playbackStarted();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		public void playbackProgress(float timeElapsed, float timeAsFraction) {
			synchronized(realTimeListeners) {
				for(int a = 0; a<realTimeListeners.size(); a++) {
					try {
						realTimeListeners.get(a).playbackProgress(timeElapsed, timeAsFraction);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		public void playbackStopped(Throwable t) {
			synchronized(realTimeListeners) {
				for(int a = 0; a<realTimeListeners.size(); a++) {
					try {
						realTimeListeners.get(a).playbackStopped(t);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	};
	
	public void play() throws UnsupportedOperationException, UnsupportedAudioFileException, IOException, LineUnavailableException {
		play( new StartTime(currentSnapshot.elapsedTime, false));
	}
	
	private transient Cancellable previousCancellable;
	public void play(StartTime startTime) throws UnsupportedAudioFileException, IOException, UnsupportedOperationException, LineUnavailableException {
		synchronized(this) {
			if(previousCancellable!=null)
				previousCancellable.cancel();
			
			currentSnapshot.resetAndIncrement();
			Cancellable currentCancellable = new BasicCancellable();
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(source);
			playAudioStream(audioIn, startTime, masterListener, currentCancellable, false);
			previousCancellable = currentCancellable;
		}
	}
	
	public boolean isPlaying() {
		synchronized(currentSnapshot) {
			return currentSnapshot.active;
		}
	}
	
	public float getElapsedTime() {
		synchronized(currentSnapshot) {
			return currentSnapshot.elapsedTime;
		}
	}

	public float getFractionTime() {
		synchronized(currentSnapshot) {
			return currentSnapshot.fraction;
		}
	}
	
	public boolean isComplete() {
		synchronized(currentSnapshot) {
			return currentSnapshot.complete;
		}
	}
	
	public Throwable getError() {
		synchronized(currentSnapshot) {
			return currentSnapshot.error;
		}
	}
	
	public void pause() {
		synchronized(this) {
			if(previousCancellable!=null)
				previousCancellable.cancel();
		}
	}
}
