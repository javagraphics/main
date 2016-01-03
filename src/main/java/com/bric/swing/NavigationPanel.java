/*
 * @(#)NavigationPanel.java
 *
 * $Date: 2015-12-26 18:42:16 +0100 (Sa, 26 Dez 2015) $
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
package com.bric.swing;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;

import com.bric.plaf.NavigationPanelUI;

public class NavigationPanel extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private static final String uiClassID = "NavigationPanelUI";
	
	public static final String KEY_NAVIGATION_MODEL = NavigationPanel.class.getName()+".model";

	public NavigationPanel() {
		setModel(new NavigationModel());
		updateUI();
	}

	public NavigationPanel(NavigationModel model) {
		setModel(model);
		updateUI();
	}

	public NavigationPanel(int currentIndex,int size) {
		setModel(new NavigationModel("Page", currentIndex, size));
		updateUI();
	}

    @Override
	public String getUIClassID() {
        return uiClassID;
    }
	
    @Override
	public void updateUI() {
    	if(UIManager.getDefaults().get(uiClassID)==null) {
    		UIManager.getDefaults().put(uiClassID, "com.bric.plaf.NavigationPanelUI");
    	}
    	setUI((NavigationPanelUI)UIManager.getUI(this));
    }
	
	public void setUI(NavigationPanelUI ui) {
        super.setUI(ui);
	}
	
	public NavigationPanelUI getUI() {
		return (NavigationPanelUI)ui;
	}
	
	public NavigationModel getModel() {
		return (NavigationModel)getClientProperty(KEY_NAVIGATION_MODEL);
	}
	
	public void setModel(NavigationModel model) {
		if(model==null)
			throw new NullPointerException();
		putClientProperty(KEY_NAVIGATION_MODEL, model);
	}

	public void setElementIndex(int i) {
		getModel().setElementIndex(i);
	}

	public void setElementCount(int size) {
		getModel().setElementCount(size);
	}

	public int getElementIndex() {
		return getModel().getElementIndex();
	}

	public int getElementCount() {
		return getModel().getElementCount();
	}
	
	public JButton getPrevButton() {
		return getUI().getContext(this).getPrevButton();
	}
	
	public JButton getNextButton() {
		return getUI().getContext(this).getNextButton();
	}
	
	public JButton getFirstButton() {
		return getUI().getContext(this).getFirstButton();
	}
	
	public JButton getLastButton() {
		return getUI().getContext(this).getLastButton();
	}
}
