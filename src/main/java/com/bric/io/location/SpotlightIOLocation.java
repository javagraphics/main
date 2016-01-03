/*
 * @(#)SpotlightIOLocation.java
 *
 * $Date: 2014-03-27 08:50:51 +0100 (Do, 27 Mär 2014) $
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import com.bric.swing.BasicCancellable;
import com.bric.swing.Cancellable;
import com.bric.util.ObservableList;
import com.bric.util.Receiver;


public class SpotlightIOLocation extends CachedLocation implements SearchResults {

	Process process;
	IOLocation parent;
	String searchPhrase;
	
	ObservableList<IOLocation> myChildren = new ObservableList<IOLocation>();
	
	public SpotlightIOLocation(IOLocation parent,String searchPhrase) throws IOException {
		this.parent = parent;
		this.searchPhrase = searchPhrase;

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				process.destroy();
			}
		});
		process = Runtime.getRuntime().exec(new String[] {"mdfind", searchPhrase});
	}

	@Override
	protected String doGetPath() {
		return "spotlight://\""+searchPhrase+"\"";
	}

	@Override
	protected boolean doCanRead() {
		return true;
	}

	@Override
	protected boolean doCanWrite() {
		return false;
	}

	public String getSearchText() {
		return searchPhrase;
	}

	public void mkdir() throws IOException {
		throw new IOException("operation not supported");
	}

	public IOLocation getChild(String name) {
		IOLocation[] children = listChildren(null, null);
		for(int a = 0; a<children.length; a++) {
			if(children[a].getName().equalsIgnoreCase(name))
				return children[a];
		}
		return null;
	}

	public InputStream createInputStream() throws IOException {
		throw new IOException();
	}

	public OutputStream createOutputStream() throws IOException {
		throw new IOException();
	}

	public void delete() throws IOException {
		throw new IOException();
	}

	@Override
	protected boolean doExists() {
		return true;
	}

	@Override
	protected long doGetModificationDate() {
		return 0;
	}

	@Override
	protected String doGetName() {
		return "Search";
	}

	public IOLocation getParent() {
		return parent;
	}

	@Override
	public String getParentPath() {
		return "spotlight://";
	}

	@Override
	protected boolean doIsAlias() {
		return false;
	}

	@Override
	protected boolean doIsDirectory() {
		return true;
	}

	@Override
	protected boolean doIsHidden() {
		return false;
	}

	public boolean isSearchable() {
		return false;
	}

	@Override
	protected long doLength() {
		return 0;
	}

	@Override
	protected boolean doIsNavigable() {
		return true;
	}

	public IOLocation getSearchDirectory() {
		return null;
	}

	@Override
	protected void doListChildren(Receiver<IOLocation> receiver,
			Cancellable cancellable) {
		if(cancellable==null) cancellable = new BasicCancellable();
		ActionListener cancellableListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				process.destroy();
			}
		};
		try {
			cancellable.addCancelListener(cancellableListener);
			
			//is there really no other way to know if a process
			//is finished than to call exitValue() and see if
			//an exception is thrown?
			try {
				process.exitValue();
			} catch(IllegalThreadStateException e) {
				InputStream in = process.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
				String s;
				while( (s = br.readLine())!=null) {
					if(cancellable.isCancelled()) {
						process.destroy();
						return;
					}
					File file = new File(s);
					if(file.exists()) {
						FileLocation loc = new FileLocation(file);
						myChildren.add(loc);
					}
				}
			}
			
			receiver.add( myChildren.toArray(new FileLocation[myChildren.size()]) );
		} catch(Throwable t) {
			t.printStackTrace();
		} finally {
			cancellable.removeCancelListener(cancellableListener);
		}
	}

	public IOLocation setName(String s) throws IOException {
		throw new IOException();
	}

	public URL getURL() {
		return null;
	}	
}
