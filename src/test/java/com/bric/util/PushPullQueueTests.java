/*
 * @(#)PushPullQueueTests.java
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
package com.bric.util;

import junit.framework.TestCase;

public class PushPullQueueTests extends TestCase {
	public void testGoodPush() {
		final PushPullQueue<Integer> queue = new PushPullQueue<Integer>();
		final Integer object = new Integer(0);
		final String[] failedMessage = new String[] { null };
		Thread pullThread = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					Object pulled = queue.pull(500);
					if(pulled.equals(object)==false) {
						failedMessage[0] = "pulled: "+pulled;
					}
				} catch(InterruptedException e){
					e.printStackTrace();
					failedMessage[0] = e.toString();
				}
			}
		};
		pullThread.start();
		queue.push(object, 2000);
		try {
			pullThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}
		if(failedMessage[0]!=null)
			fail(failedMessage[0]);
	}

	public void testTimeoutPush() {
		final PushPullQueue<Integer> queue = new PushPullQueue<Integer>();
		final Integer object = new Integer(0);
		final String[] failedMessage = new String[] { null };
		Thread pullThread = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
					Object pulled = queue.pull(500);
					if(pulled.equals(object)==false) {
						failedMessage[0] = "pulled: "+pulled;
					}
				} catch(InterruptedException e){
					e.printStackTrace();
					failedMessage[0] = e.toString();
				}
			}
		};
		pullThread.start();
		try {
			queue.push(object, 1000);
			fail("push() was supposed to time out");
		} catch(RuntimeException e) {
			//this is what we expected.
		}
		try {
			pullThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}
		if(failedMessage[0]!=null)
			fail(failedMessage[0]);
	}

	public void testGoodPull() {
		final PushPullQueue<Integer> queue = new PushPullQueue<Integer>();
		final Integer object = new Integer(0);
		final String[] failedMessage = new String[] { null };
		Thread pushThread = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch(InterruptedException e){
					e.printStackTrace();
					failedMessage[0] = e.toString();
				}
				queue.push(object, 2000);

			}
		};
		pushThread.start();
		Object pullValue = queue.pull(2000);
		if(pullValue.equals(object)==false)
			fail();
		try {
			pushThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}
		if(failedMessage[0]!=null)
			fail(failedMessage[0]);
	}

	public void testTimeoutPull() {
		final PushPullQueue<Integer> queue = new PushPullQueue<Integer>();
		final Integer object = new Integer(0);
		final String[] failedMessage = new String[] { null };
		Thread pushThread = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch(InterruptedException e){
					e.printStackTrace();
					fail();
				}
				try {
					queue.push(object, 2000);
					failedMessage[0] = "push() was also supposed to time out";
				} catch(RuntimeException e) {
					//this is also expected
				}
			}
		};
		pushThread.start();
		try {
			Object pullValue = queue.pull(1000);
			fail("pull() was supposed to time out.  Instead it returned "+pullValue);
		} catch(RuntimeException e) {
			//this is what we expected
		}
		try {
			pushThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}
		if(failedMessage[0]!=null)
			fail(failedMessage[0]);
	}
}
