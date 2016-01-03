/*
 * @(#)NavigationPanelUI.java
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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;

import com.bric.swing.NavigationButtons;
import com.bric.swing.NavigationPanel;

public class NavigationPanelUI extends ComponentUI {

	private static final String KEY_CONTEXT = NavigationPanelUI.class.getName()+".context";
	
    public static ComponentUI createUI(JComponent c) {
        return new LargeNavigationPanelUI();
    }

    public class Context {
		protected JButton prevButton = NavigationButtons.createPrev();
		protected JButton nextButton = NavigationButtons.createNext();
		protected JButton firstButton = new JButton();
		protected JButton lastButton = new JButton();
		protected JSlider slider = new JSlider(0,1000);
		protected JLabel label = new JLabel();
		protected int sliderAdjusting = 0;
		protected final NavigationPanel navPanel;


		private class ButtonTimerManager implements ChangeListener {
			
			boolean wasPressed = false;
			JButton button;
			Timer timer;
			
			private ButtonTimerManager(JButton button) {
				this.button = button;
			}

			@Override
			public void stateChanged(ChangeEvent e) {
				boolean isPressed = button.getModel().isPressed();
				if(wasPressed==false && isPressed) {
					int delay = navPanel.getModel().getButtonDelay();
					timer = new Timer(delay, new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							boolean wasArmed = button.getModel().isArmed();
							boolean wasPressed = button.getModel().isPressed();
							button.removeChangeListener(ButtonTimerManager.this);
							try {
								button.doClick();
								timer.setDelay( navPanel.getModel().getButtonCycle() );
							} finally {
								button.getModel().setArmed(wasArmed);
								button.getModel().setPressed(wasPressed);
								button.addChangeListener(ButtonTimerManager.this);
							}
						}
					});
					timer.start();
				} else if(wasPressed && (!isPressed)) {
					if(timer!=null)
						timer.stop();
				}
				wasPressed = isPressed;
			}	
		}
		
		MouseInputListener dragListener = new MouseInputAdapter() {
			Point lastPoint;
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(navPanel.getModel().isDraggable())
					lastPoint = e.getPoint();
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				lastPoint = null;
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if(lastPoint!=null) {
					Point currentPoint = e.getPoint();
					Rectangle r = navPanel.getBounds();
					int dx = currentPoint.x - lastPoint.x;
					int dy = currentPoint.y - lastPoint.y;
					r.x += dx;
					r.y += dy;
					navPanel.setBounds(r);
					
					lastPoint = currentPoint;
					lastPoint.x -= dx;
					lastPoint.y -= dy;
				}
			}

		};

		
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int newIndex;
				if(e.getSource()==firstButton) {
					newIndex = 0;
				} else if(e.getSource()==prevButton) {
					newIndex  = navPanel.getElementIndex()-1;
				} else if(e.getSource()==nextButton) {
					newIndex = navPanel.getElementIndex()+1;
				} else if(e.getSource()==lastButton) {
					newIndex = navPanel.getElementCount()-1;
				} else {
					throw new RuntimeException("unexpected source: "+e.getSource());
				}
				newIndex = Math.max( 0, Math.min(navPanel.getElementCount()-1, newIndex) );
				navPanel.setElementIndex(newIndex);
			}
		};
		
		Context(NavigationPanel navPanel) {
			this.navPanel = navPanel;
			
			nextButton.addChangeListener(new ButtonTimerManager(nextButton));
			prevButton.addChangeListener(new ButtonTimerManager(prevButton));
			
			firstButton.addActionListener(actionListener);
			nextButton.addActionListener(actionListener);
			prevButton.addActionListener(actionListener);
			lastButton.addActionListener(actionListener);
			
			
			navPanel.addMouseListener(dragListener);
			navPanel.addMouseMotionListener(dragListener);

		}
		
		public JButton getNextButton() {
			return nextButton;
		}
		
		public JButton getPrevButton() {
			return prevButton;
		}
		
		public JButton getFirstButton() {
			return firstButton;
		}
		
		public JButton getLastButton() {
			return lastButton;
		}
    }
    
    public Context getContext(NavigationPanel navPanel) {
    	Context context = (Context)navPanel.getClientProperty(KEY_CONTEXT);
    	if(context==null) {
    		context = new Context(navPanel);
    		navPanel.putClientProperty(KEY_CONTEXT, context);
    	}
    	return context;
    }
	
	protected void updateControls(NavigationPanel navPanel) {
		int i = navPanel.getElementIndex();
		int size = navPanel.getElementCount();
		
		Context context = getContext(navPanel);
		context.firstButton.setEnabled(i!=0 && navPanel.isEnabled());
		context.lastButton.setEnabled(i!=size-1 && navPanel.isEnabled());
		context.prevButton.setEnabled(i!=0 && navPanel.isEnabled());
		context.nextButton.setEnabled(i!=size-1 && navPanel.isEnabled());
		context.slider.setEnabled(navPanel.isEnabled() && size>0 && i!=-1);
		try {
			Callable<String> descriptor = navPanel.getModel().getDescriptor();
			context.label.setVisible(descriptor!=null);
			if(descriptor!=null) {
				context.label.setText(descriptor.call());
			}
		} catch(Exception e) {
			context.label.setText(e.getMessage());
		}
		
		context.sliderAdjusting++;
		try {
			if(size!=context.slider.getMaximum() && size>0) {
				context.slider.setMaximum(size);
			}
			int sliderRange = context.slider.getMaximum()-context.slider.getMinimum();
			if(size!=0) {
				int sliderValue = i * sliderRange / size;
				context.slider.setValue(sliderValue);
			}
		} finally {
			context.sliderAdjusting--;
		}
	}

	@Override
	public void installUI(JComponent c) {
		
		final NavigationPanel navPanel = (NavigationPanel)c;
		final Context context = getContext(navPanel);		
		
		PropertyChangeListener pcl = new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateControls(navPanel);
			}
			
		};
		navPanel.getModel().addPropertyChangeListener(pcl);
		navPanel.addPropertyChangeListener("enabled", pcl);
		
		context.slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(context.sliderAdjusting>0)
					return;
				int sliderRange = context.slider.getMaximum()-context.slider.getMinimum();
				int myValue = Math.min(context.slider.getValue() * navPanel.getElementCount() / sliderRange, navPanel.getElementCount()-1 );
				navPanel.setElementIndex(myValue);
			}
		});
		updateControls(navPanel);
	}

	@Override
	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);
	}
}
