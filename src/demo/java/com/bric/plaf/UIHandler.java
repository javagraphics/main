/*
 * @(#)UIHandler.java
 *
 * $Date: 2014-05-07 08:22:48 +0200 (Mi, 07 Mai 2014) $
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
package com.bric.plaf;

import java.awt.Component;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.plaf.ComponentUI;

public interface UIHandler {
	public JComponent[] getControls();
	public void updateControls(JInternalFrame selectedFrame,ComponentUI ui,Vector<Component> components);
	public JPanel makeDemoPanel(ComponentUI ui);
}
