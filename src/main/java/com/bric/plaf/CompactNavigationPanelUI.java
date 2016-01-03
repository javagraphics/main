/*
 * @(#)CompactNavigationPanelUI.java
 *
 * $Date: 2015-12-26 08:54:45 +0100 (Sa, 26 Dez 2015) $
 *
 * Copyright (c) 2015 by Jeremy Wood.
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

import javax.swing.JComponent;

import com.bric.blog.ResourceSample;
import com.bric.swing.NavigationButtons;
import com.bric.swing.NavigationPanel;

/** This includes the previous and next buttons and a label in a single row.
 * This was originally used in the <code>PrintLayoutPreviewPanel</code>.
 * 
 * <!-- ======== START OF AUTOGENERATED SAMPLES ======== -->
 * <p><img src="https://javagraphics.java.net/resources/samples/CompactNavigationPanelUI/sample.png" alt="com.bric.plaf.CompactNavigationPanelUI.createDemo()">
 * <!-- ======== END OF AUTOGENERATED SAMPLES ======== -->
 */
@ResourceSample( sample={"com.bric.plaf.CompactNavigationPanelUI.createDemo()"} )
public class CompactNavigationPanelUI extends NavigationPanelUI {
	
	/** Create a minimal demo for the javadoc. */
	public static NavigationPanel createDemo() {
		NavigationPanel navPanel = new NavigationPanel(5, 10);
		navPanel.setUI( new CompactNavigationPanelUI() );
		return navPanel;
	}

	@Override
	public void installUI(JComponent c) {
		super.installUI(c);

		NavigationPanel navPanel = (NavigationPanel)c;
		navPanel.removeAll();
		Context context = getContext(navPanel);
		
		navPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0,0,0,0);
		gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.weighty = 1; gbc.gridwidth = 1;
		navPanel.add(context.prevButton,gbc);
		gbc.gridx++;
		navPanel.add(context.nextButton,gbc);
		gbc.gridx++;
		gbc.insets = new Insets(0,8,0,0);
		navPanel.add(context.label,gbc);
		
		NavigationButtons.formatNext(context.nextButton);
		NavigationButtons.formatPrev(context.prevButton);
	}
}
