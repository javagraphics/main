/*
 * @(#)BasicCancellable.java
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
package com.bric.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class BasicCancellable implements Cancellable {

	private boolean cancelled = false;
	private boolean finished = false;
	Vector<ActionListener> cancelListeners;
	Vector<ActionListener> finishListeners;
	
	public void cancel() {
		if(cancelled)
			return;
		cancelled = true;
		fireCancelListeners();
	}

	public void finish() {
		if(finished)
			return;
		finished = true;
		fireFinishListeners();
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public boolean isFinished() {
		return finished;
	}
	
	protected void fireFinishListeners() {
		if(finishListeners==null)
			return;
		for(int a = 0; a<finishListeners.size(); a++) {
			ActionListener l = finishListeners.get(a);
			try {
				l.actionPerformed(new ActionEvent(this,0,"finish"));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void fireCancelListeners() {
		if(cancelListeners==null)
			return;
		for(int a = 0; a<cancelListeners.size(); a++) {
			ActionListener l = cancelListeners.get(a);
			try {
				l.actionPerformed(new ActionEvent(this,0,"cancel"));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addFinishListener(ActionListener l) {
		if(finishListeners==null) finishListeners = new Vector<ActionListener>();
		if(finishListeners.contains(l))
			return;
		finishListeners.add(l);
	}
	
	public void addCancelListener(ActionListener l) {
		if(cancelListeners==null) cancelListeners = new Vector<ActionListener>();
		if(cancelListeners.contains(l))
			return;
		cancelListeners.add(l);
	}
	
	public void removeFinishListener(ActionListener l) {
		if(finishListeners==null) return;
		finishListeners.remove(l);
	}

	public void removeCancelListener(ActionListener l) {
		if(cancelListeners==null) return;
		cancelListeners.remove(l);
	}

}
