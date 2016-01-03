/*
 * @(#)ZipArchiveLocation.java
 *
 * $Date: 2014-03-13 09:15:48 +0100 (Do, 13 Mär 2014) $
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
package com.bric.io.location;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.bric.util.Receiver;

public class ZipArchiveLocation extends ArchiveLocation<ZipEntry> {

	public ZipArchiveLocation(IOLocation archive) {
		super(archive);
		setParent(archive.getParent());
	}
	
	private ZipArchiveLocation(ArchiveLocation<ZipEntry> root,ZipEntry zipEntry) {
		super(root, zipEntry);
	}
	
	@Override
	public String getArchivePath() {
		ZipEntry entry = getArchiveEntry();
		if(entry==null) return null;
		
		return entry.getName();
	}

	@Override
	protected char getSeparatorChar() {
		return '/';
	}

	@Override
	protected void listArchiveEntries(Receiver<ZipEntry> receiver) throws IOException {
		InputStream in = null;
		try {
			in = archive.createInputStream();
			ZipInputStream zipIn = null;
			try {
				zipIn = new ZipInputStream(in);
				ZipEntry entry = zipIn.getNextEntry();
				
				while(entry!=null) {
					receiver.add(entry);
					
					entry = zipIn.getNextEntry();
				}
			} finally {
				if(zipIn!=null) {
					try {
						zipIn.close();
					} catch(Throwable t) {
						t.printStackTrace();
					}
				}
			}
		} finally {
			if(in!=null) {
				try {
					in.close();
				} catch(Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}
	
	@Override
	protected ZipEntry createDirectory(String path) {
		ZipEntry e = new ZipEntry(path);
		return e;
	}

	@Override
	protected ArchiveLocation<ZipEntry> createArchiveLocation(ArchiveLocation<ZipEntry> root,ZipEntry t) {
		return new ZipArchiveLocation(root, t);
	}

	@Override
	public boolean isDirectory(ZipEntry entry) {
		return entry.isDirectory();
	}

	@Override
	public long getModificationDate(ZipEntry entry) {
		return entry.getTime();
	}

	@Override
	public boolean isHidden(ZipEntry entry) {
		return false;
	}

	@Override
	public long length(ZipEntry entry) {
		return entry.getSize();
	}

	/** Create an InputStream for a ZipEntry.
	 * <P>TODO: This is optimized for java.io.Files, but if we parse the table of contents
	 * manually then we might be able to similarly optimize it for non-files.
	 */
	@Override
	public InputStream createInputStream(ZipEntry entry) throws IOException {
		if(this.root.archive instanceof FileLocation) {
			File file = ((FileLocation)root.archive).getFile();
			final ZipFile zipFile = new ZipFile(file);
			InputStream in = zipFile.getInputStream(entry);
			
			//A little extra insurance that we'll close the resource we opened:
			FilterInputStream wrapper = new FilterInputStream( in ) {

				@Override
				public void close() throws IOException {
					super.close();
					closeZipFile();
				}

				@Override
				protected void finalize() throws Throwable {
					super.finalize();
					closeZipFile();
				}
				
				private void closeZipFile() throws IOException {
					zipFile.close();
				}
			};
			return wrapper;
		}

		final InputStream in = archive.createInputStream();
		final ZipInputStream zipIn = new ZipInputStream(in);
		ZipEntry e = zipIn.getNextEntry();
		
		while(e!=null) {
			if(e.getName().equals(entry.getName())) {

				FilterInputStream wrapper = new FilterInputStream( zipIn ) {

					@Override
					public void close() throws IOException {
						super.close();
						closeZipFile();
					}

					@Override
					protected void finalize() throws Throwable {
						super.finalize();
						closeZipFile();
					}
					
					private void closeZipFile() throws IOException {
						zipIn.close();
						in.close();
					}
				};
				
				return wrapper;
			}
			
			e = zipIn.getNextEntry();
		}
		if(zipIn!=null) {
			try {
				zipIn.close();
			} catch(Throwable t) {
				t.printStackTrace();
			}
		}
		if(in!=null) {
			try {
				in.close();
			} catch(Throwable t) {
				t.printStackTrace();
			}
		}
		throw new RuntimeException("the entry \""+entry.getName()+"\" was not found.");
	}
}
