/*
 * @(#)InspectorGroupLayout.java
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
package com.bric.inspector;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/** An implementation of the <code>InspectorLayout</code> that uses a
 * <code>GroupLayout</code>.
 */
public class InspectorGroupLayout implements InspectorLayout {
	GroupLayout layout;
	JPanel panel;
	GroupLayout.SequentialGroup vGroup;
	GroupLayout.ParallelGroup hGroup;
	
	GroupLayout.ParallelGroup unicolumn;
	GroupLayout.ParallelGroup leftColumn;
	GroupLayout.ParallelGroup rightColumn;
	
	/** Creates a new <code>InspectorGroupLayout</code> object.
	 * 
	 * @param panel the panel to use.
	 */
	public InspectorGroupLayout(JPanel panel) {
		this.panel = panel;
		layout = new GroupLayout(panel);
		panel.setLayout(layout);

		vGroup = layout.createSequentialGroup();
		hGroup = layout.createParallelGroup(GroupLayout.LEADING, true);
		
		layout.setVerticalGroup(vGroup);
		layout.setHorizontalGroup(hGroup);
		
		unicolumn = layout.createParallelGroup(GroupLayout.LEADING);
		leftColumn = layout.createParallelGroup(GroupLayout.TRAILING, false);
		rightColumn = layout.createParallelGroup(GroupLayout.LEADING, true);
		
		hGroup.add(unicolumn);
		
		GroupLayout.SequentialGroup twoColumns = layout.createSequentialGroup();
		hGroup.add(twoColumns);
		twoColumns.add(leftColumn);
		twoColumns.addPreferredGap(LayoutStyle.RELATED);
		twoColumns.add(rightColumn);
	}
	
	public void clear() {
		panel.removeAll();
	}
	
	public void addRow(JComponent singleComponent,int alignment,boolean stretch) {
		vGroup.add(singleComponent, GroupLayout.PREFERRED_SIZE, 
				GroupLayout.PREFERRED_SIZE,
	              GroupLayout.PREFERRED_SIZE );
		int z;
		if(alignment==SwingConstants.LEFT) {
			z = GroupLayout.LEADING;
		} else if(alignment==SwingConstants.CENTER) {
			z = GroupLayout.CENTER;
		} else if(alignment==SwingConstants.RIGHT) {
			z = GroupLayout.TRAILING;
		} else {
			throw new IllegalArgumentException("Alignment should be LEFT, RIGHT or CENTER from SwingConstants.");
		}

		if(stretch) {
			unicolumn.add(singleComponent,
				GroupLayout.PREFERRED_SIZE,
				GroupLayout.PREFERRED_SIZE,
				Short.MAX_VALUE);
		} else {
			unicolumn.add(z,singleComponent,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE);
		}
		
		vGroup.addContainerGap();
	}

	public JSeparator addSeparator() {
		JSeparator separator = new JSeparator();
		
		vGroup.add(separator, GroupLayout.PREFERRED_SIZE, 
				GroupLayout.PREFERRED_SIZE,
	              GroupLayout.PREFERRED_SIZE );
		
		unicolumn.add(separator,0,GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE);
		
		return separator;
	}
	
	public void addRow(JComponent identifier,JComponent controls,boolean stretchControlToFill) {
		//vertical stuff:
		int alignment = GroupLayout.CENTER;
		if(containsText(identifier) && containsText(controls))
			alignment = GroupLayout.BASELINE;
		GroupLayout.ParallelGroup pg = layout.createParallelGroup(alignment);
		pg.add(identifier, GroupLayout.PREFERRED_SIZE, 
				GroupLayout.PREFERRED_SIZE,
				GroupLayout.PREFERRED_SIZE );
		pg.add(controls, GroupLayout.PREFERRED_SIZE, 
				GroupLayout.PREFERRED_SIZE,
				GroupLayout.PREFERRED_SIZE );
		vGroup.add(pg);
		//vGroup.addContainerGap();
		
		//horizontal stuff:
		leftColumn.add(GroupLayout.TRAILING,
				identifier,
				GroupLayout.PREFERRED_SIZE,
				GroupLayout.PREFERRED_SIZE, 
				GroupLayout.PREFERRED_SIZE);
		if(stretchControlToFill) {
			rightColumn.add( controls,
				GroupLayout.PREFERRED_SIZE,
				GroupLayout.PREFERRED_SIZE,
				Short.MAX_VALUE);
		} else {
			rightColumn.add( GroupLayout.LEADING,
					controls,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE);
		}
	}

	/** Adds a row of controls to this layout.
	 * 
	 * @param identifier a checkbox, label, or other short, simple identifying component
	 * @param leftSide a control that appears on the left side of the second column.
	 * @param rightSide a control that appears on the right side of the second column.
	 */
	public void addRow(JComponent identifier,JComponent leftSide,boolean stretchLeftSide,JComponent rightSide) {
		//vertical stuff:
		int alignment = GroupLayout.CENTER;
		int text = 0;
		if(containsText(identifier))
			text++;
		if(containsText(leftSide))
			text++;
		if(containsText(rightSide))
			text++;
		if(text>1)
			alignment = GroupLayout.BASELINE;
		GroupLayout.ParallelGroup pg = layout.createParallelGroup(alignment);
		pg.add(identifier, GroupLayout.PREFERRED_SIZE, 
				GroupLayout.PREFERRED_SIZE,
				GroupLayout.PREFERRED_SIZE );
		pg.add(leftSide, GroupLayout.PREFERRED_SIZE, 
				GroupLayout.PREFERRED_SIZE,
				GroupLayout.PREFERRED_SIZE );
		pg.add(rightSide, GroupLayout.PREFERRED_SIZE, 
				GroupLayout.PREFERRED_SIZE,
				GroupLayout.PREFERRED_SIZE );
		vGroup.add(pg);
		//vGroup.addContainerGap();
		
		//horizontal stuff:
		leftColumn.add(GroupLayout.TRAILING,
				identifier,
				GroupLayout.PREFERRED_SIZE,
				GroupLayout.PREFERRED_SIZE, 
				GroupLayout.PREFERRED_SIZE);
		GroupLayout.SequentialGroup seq = layout.createSequentialGroup();
		if(stretchLeftSide) {
			seq.add( leftSide,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE,
					Short.MAX_VALUE);
			seq.addPreferredGap(leftSide, rightSide, LayoutStyle.RELATED);
			seq.add( rightSide,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE );
		} else {
			seq.add( leftSide,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE);
			seq.add( 0, 0, Short.MAX_VALUE);
			seq.add( rightSide,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE );
		}
		rightColumn.add(seq);
	}
	
	/** Checks to see if this component (or its children)
	 * contain components which prominently feature text.
	 */
	private static boolean containsText(JComponent c) {
		if(c instanceof JTextComponent)
			return true;
		if(c instanceof AbstractButton) {
			AbstractButton button = (AbstractButton)c;
			if(button.getText()!=null && button.getText().trim().length()>0)
				return true;
		}
		
		for(int a = 0; a<c.getComponentCount(); a++) {
			if(c.getComponent(a) instanceof JComponent) {
				boolean hasText = containsText( (JComponent)c.getComponent(a) );
				if(hasText)
					return true;
			}	
		}
		return false;
	}

	public void addGap() {
		vGroup.add(0,0,Short.MAX_VALUE);
	}
}
