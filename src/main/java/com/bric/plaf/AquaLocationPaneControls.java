/*
 * @(#)AquaLocationPaneControls.java
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
package com.bric.plaf;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import com.bric.swing.TextFieldPrompt;
import com.bric.swing.io.LocationBrowser;
import com.bric.swing.resources.ColumnIcon;
import com.bric.swing.resources.ListIcon;
import com.bric.swing.resources.TileIcon;

/** This is the row of controls that appears near the top of open dialogs:
 * a back/forward button, buttons to control the browser UI, combobox &amp; search field.
 */
class AquaLocationPaneControls extends JPanel {
	private static final long serialVersionUID = 1L;

	protected final LocationPaneUI paneUI;
	protected final JToolBar layoutButtons = new JToolBar();
	
	protected final JToggleButton tileView = new JToggleButton(new TileIcon(10,10));
	protected final JToggleButton listView = new JToggleButton(new ListIcon(10,10));
	protected final JToggleButton columnView = new JToggleButton(new ColumnIcon(10,10));
	
	private ActionListener controlListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			LocationBrowser browser = paneUI.getBrowser();
			if(src==tileView) {
				if(browser.getUI() instanceof AquaTileLocationBrowserUI)
					return;
				Preferences.userNodeForPackage(getClass()).put("view", "tile");
				browser.setUI(new AquaTileLocationBrowserUI(browser));
			} else if(src==listView) {
				if(browser.getUI() instanceof AquaListLocationBrowserUI)
					return;
				Preferences.userNodeForPackage(getClass()).put("view", "list");
				browser.setUI(new AquaListLocationBrowserUI(browser));
			}
		}
	};
	
	public AquaLocationPaneControls(LocationPaneUI paneUI) {
		this.paneUI = paneUI;
		
		layoutButtons.add(tileView);
		layoutButtons.add(listView);
		//TODO: implement a UI for the column view.
		//layoutButtons.add(columnView);
		
		ButtonGroup layoutGroup = new ButtonGroup();
		layoutGroup.add(tileView);
		layoutGroup.add(listView);
		layoutGroup.add(columnView);

		layoutButtons.setFloatable(false);
		
		tileView.addActionListener(controlListener);
		listView.addActionListener(controlListener);
		columnView.addActionListener(controlListener);
		
		String view = Preferences.userNodeForPackage(getClass()).get("view", "list");
		if(view.equals("list")) {
			listView.doClick();
		} else if(view.equals("column")) {
			columnView.doClick();
		} else {
			tileView.doClick();
		}
		
		BevelButtonUI shortBevel = new BevelButtonUI() {
			@Override
			protected int getPreferredHeight() {
				return 0;
			}
		};
		paneUI.upButton.setUI(shortBevel);
		ButtonCluster.install(layoutButtons,shortBevel,true);
		ButtonCluster.install(paneUI.navigationButtons,shortBevel,true);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(0,0,0,0);
		c.weightx = 0; c.weighty = 1;
		add(paneUI.navigationButtons,c);
		c.gridx++;
		add(layoutButtons,c);
		
		c.gridx++; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;

		c.insets = new Insets(3,0,0,0);
		add(paneUI.comboBox,c);
		c.insets = new Insets(0,0,0,0);
		c.weightx = 1;
		c.gridx++;
		add(paneUI.searchField,c);
		setBorder(new EmptyBorder(new Insets(0,5,0,5)));
		
		paneUI.nextButton.setRequestFocusEnabled(false);
		paneUI.backButton.setRequestFocusEnabled(false);

		paneUI.searchField.setUI(new RoundTextFieldUI());
		paneUI.searchField.putClientProperty("JTextField.variant", "search");
		@SuppressWarnings("unused")
		TextFieldPrompt prompt = new TextFieldPrompt(paneUI.searchField,"search");
	}
}
