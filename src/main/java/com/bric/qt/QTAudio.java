/*
 * @(#)QTAudio.java
 *
 * $Date: 2014-06-06 20:04:49 +0200 (Fr, 06 Jun 2014) $
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
package com.bric.qt;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import quicktime.QTException;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.movies.Movie;
import quicktime.std.movies.Track;
import quicktime.std.movies.media.Media;
import quicktime.std.movies.media.MediaSample;
import quicktime.std.movies.media.SoundDescription;
import quicktime.std.movies.media.SoundMedia;

import com.bric.audio.WavFileWriter;
import com.bric.audio.WavFormatChunk;

/** Some static methods for interacting with audio.
 * 
 * @deprecated
 */
public class QTAudio {
	
	/**
	 * 
	 * @param media the media to convert
	 * @param wavFile the optional destination to write to.
	 * If this is not provided, a new file in the temp directory
	 * will be created.
	 * @param downsampleRate an optional rate to reduce this audio to.
	 * If zero or negative this is ignored.
	 * @return a wav file.
	 * @throws QTException
	 * @throws IOException
	 */
	public static File extractAudioAsWaveFile(SoundMedia media,File wavFile,int downsampleRate) throws QTException, IOException {
		if(wavFile==null)
			wavFile = File.createTempFile("extracted", ".wav");
		WavFileWriter writer = new WavFileWriter(wavFile);
		MediaSample demoSample = media.getSample(1024, 0, 1);
		SoundDescription desc = media.getSoundDescription(1);
		int numChannels = desc.getNumChannels();
		int origSampleRate = (int)(desc.getSampleRate()+.5);
		int sigBitsPerSample = desc.getSampleSize();
		
		int newSampleRate = origSampleRate;
		if(downsampleRate>0 && downsampleRate<origSampleRate)
			newSampleRate = downsampleRate;
		WavFormatChunk format = new WavFormatChunk(
				WavFormatChunk.COMPRESSION_PCM,
				numChannels,
				newSampleRate,
				sigBitsPerSample
		);
		writer.writeFormat(format);

		byte[] array = new byte[4096*4];

		//if this is two's complement big endian, we need to reorder the bytes:
		// 'twos'   for 16 bit two's complement big endian
		// 'sowt'   for 16 bit two's complement little endian
		// 'in24'    for 24 bit integer format
		boolean reverse = desc.getDataFormat()==1953984371;
		int sampleSize = demoSample.size/demoSample.numberOfSamples;
		
		int a = 0;
		int lastSampleIndex = -1;
		while(a<media.getSampleCount()) {
			MediaSample mediaSample = media.getSample(array.length, a, array.length);
			mediaSample.data.copyToArray(0, array, 0, mediaSample.size);
			for(int sampleIndex = 0; sampleIndex<mediaSample.numberOfSamples; sampleIndex++) {
				int newSampleIndex = (a+sampleIndex)*newSampleRate/origSampleRate;
				if(newSampleIndex!=lastSampleIndex) {
					int arrayOffset = sampleSize*sampleIndex;
					if(reverse) {
						for(int b = 0; b<format.blockAlign/2; b++) {
							byte t = array[arrayOffset+b];
							array[arrayOffset+b] = array[arrayOffset+sampleSize-1-b];
							array[arrayOffset+sampleSize-1-b] = t;
						}
					}
					writer.writeSample(array, arrayOffset, sampleSize);
					lastSampleIndex = newSampleIndex;
				}
			}
			a += mediaSample.numberOfSamples;
		}
		writer.close();
		return wavFile;
	}
	
	public static File[] extractAudioAsWaveFiles(File qtFile) throws QTException, IOException {
		QTFile file = new QTFile(qtFile);
		Movie movie = Movie.fromFile(OpenMovieFile.asRead(file));
		
		Vector<File> wavFiles = new Vector<File>();
		for(int a = 0; a<movie.getTrackCount(); a++) {
			Track track = movie.getTrack(a+1);
			Media media = track.getMedia();
			if(media instanceof SoundMedia) {
				wavFiles.add( extractAudioAsWaveFile( (SoundMedia)media, null, 0 ) );
			}
		}
		return wavFiles.toArray(new File[wavFiles.size()]);
	}

	public static Shape getWaveform(File file) {
		try {
			int soundMediaCtr = 0;
			SoundMedia sound = null;
			QTFile qtFile = new QTFile(file);
			Movie movie = Movie.fromFile(OpenMovieFile.asRead(qtFile));
			
			for(int a = 0; a<movie.getTrackCount(); a++) {
				Track track = movie.getTrack(a+1);
				Media media = track.getMedia();
				if(media instanceof SoundMedia) {
					sound = (SoundMedia)media;
					soundMediaCtr++;
				}
			}
			if(soundMediaCtr>1) {
				System.out.println("aborting");
				return null;
			}
			int sampleSize = ((SoundDescription)sound.getSampleDescription(1)).getSampleSize();
			int channels = ((SoundDescription)sound.getSampleDescription(1)).getNumChannels();
			
			byte[] array = new byte[4];
			GeneralPath shape = new GeneralPath();
			shape.moveTo(0,0);
			int sampleCount = sound.getSampleCount();
			int incr = sampleCount/1000; //assume we're aiming for a shape of 1000 pixels
			for(int a = 0; a<sampleCount; a+=incr) {
				MediaSample sample = sound.getSample(1024, a, 1);
				sample.data.copyToArray(0, array, 0, Math.min(array.length,sample.data.getSize()));
				if(sampleSize==16) {
					int k = (array[0] +128)*256+(array[1]+128)-32767;
					shape.lineTo(a, -k);
				} else if(sampleSize==8) {
					int k = (array[0]);
					shape.lineTo(a, -k);
				} else {
					System.err.println("unsupported sample: sampleSize = "+sampleSize+", channels = "+channels);
					return null;
				}
			}
			shape.lineTo(sound.getSampleCount(), 0);
			shape.closePath();
			
			return shape;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
