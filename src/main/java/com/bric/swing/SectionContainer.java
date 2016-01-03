/*
 * @(#)SectionContainer.java
 *
 * $Date: 2014-03-13 09:15:48 +0100 (Do, 13 Mär 2014) $
 *
 * Copyright (c) 2014 by Jeremy Wood.
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import com.bric.util.ObservableList;

/** This <code>JPanel</code> presents a series of
 * <code>Sections</code>. The key distinction between
 * simply adding components to a panel and adding
 * <code>Sections</code> to <code>SectionContainer</code> is:
 * sections should be considered more abstract. Adding a section
 * may necessarily introduce other UI components (like headers,
 * tabs, close buttons, a draggable container, etc.)
 * <P>The only current implementation
 * of this is the {@link CollapsibleContainer}.
 * <p>TODO: implement a tabbed subclass. Unlike the <code>JTabbedPane</code>,
 * this subclass would make the tabs complex components with button,
 * contextual menus, etc.
 * <P>Another possible implementation of this could be similar to
 * a <code>CardLayout</code>, or a <code>CardLayout</code> with a
 * <code>JComboBox</code> or toolbar of modal icon buttons to toggle
 * the visible sections.
 * <p>Or, lastly: this could similar to widgets or "Mission Control"
 * on the Mac. (A series of draggable components in a larger space.)
 * 
 */
public class SectionContainer extends JPanel {
	private static final long serialVersionUID = 1L;
	
	/** The property (used in <code>Section.getProperty())</code> to refer
	 * to a name.
	 */
	public static final String NAME = SectionContainer.class.getName()+".name";
	
	/** The property (used in <code>Section.getProperty())</code> to refer
	 * to an icon.
	 */
	public static final String ICON = SectionContainer.class.getName()+".icon";
	
	public static class Section implements Serializable {
		private static final long serialVersionUID = 1L;
		final String id;
		
		Map<String, Object> properties = new Hashtable<String, Object>();
		List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
		JPanel body = new JPanel();
		
		protected Section(String id) {
			if(id==null) throw new NullPointerException("section ID must not be null");
			this.id = id;
		}
		
		public JPanel getBody() {
			return body;
		}
		
		public Object getProperty(String propertyName) {
			return properties.get(propertyName);
		}
		
		public String getID() {
			return id;
		}
		
		public void addPropertyChangeListener(PropertyChangeListener l) {
			listeners.add(l);
		}
		
		public void removePropertyChangeListener(PropertyChangeListener l) {
			listeners.remove(l);
		}
		
		public void setName(String newName) {
			if(newName==null) throw new NullPointerException();
			setProperty(NAME, newName);
		}
		
		public String getName() {
			return (String)properties.get(NAME);
		}
		
		public void setProperty(String propertyName,Object newValue) {
			PropertyChangeEvent evt = new PropertyChangeEvent(this, propertyName, properties.get(propertyName), newValue);
			if(newValue==null) {
				properties.remove(propertyName);
			} else {
				properties.put(propertyName, newValue);
			}
			for(PropertyChangeListener l : listeners) {
				try {
					l.propertyChange(evt);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	protected final ObservableList<Section> sections = new ObservableList<Section>();
	
	public SectionContainer() {}
	
	public synchronized Section addSection(String id,String name) {
		for(Section s : sections) {
			if(s.id.equals(id))
				throw new IllegalArgumentException("the section ID \""+id+"\" is already reserved");
		}
		Section section = new Section(id);
		section.setName(name);
		sections.add(section);
		return section;
	}
	
	public ObservableList<Section> getSections() {
		return sections;
	}
}
