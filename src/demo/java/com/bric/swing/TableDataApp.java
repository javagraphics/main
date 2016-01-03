/*
 * @(#)TableDataApp.java
 *
 * $Date: 2014-05-04 17:57:20 +0200 (So, 04 Mai 2014) $
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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.bric.util.EnumProperty;
import com.bric.util.JVM;

/** This completes a table of data in a separate thread and presents this as a
 * well-formatted monospaced text table (or you can right-click the table to
 * apply HTML formatting).
 */
public abstract class TableDataApp extends BricApplet {
	private static final long serialVersionUID = 1L;

	static enum Formatting { Tab, Monospace, HTML }
	
	static class TextTable {
		JTextArea textArea = new JTextArea();
		DefaultTableModel table;
		
		EnumProperty<Formatting> formatting = new EnumProperty<Formatting>("Formatting", Formatting.values(), Formatting.Monospace);
		
		Runnable updateTextArea = new Runnable() {
			public void run() {
				updateTextArea();
			}
		};
		
		public TextTable(Object[] columnNames,int rows) {
			table = new DefaultTableModel(columnNames, rows);
			ContextualMenuHelper.addPopupMenu("Formatting", textArea, formatting, new Runnable() {
				public void run() {
					updateTextArea();
				}
			});
			textArea.setEditable(false);
			textArea.setFont(new Font("monospaced", 0, 14));
			table.addTableModelListener(new TableModelListener() {
				public void tableChanged(TableModelEvent e) {
					SwingUtilities.invokeLater(updateTextArea);
				}
			});
		}
		
		protected void updateTextArea() {
			StringBuffer text = new StringBuffer();
			if(Formatting.HTML.equals(formatting.getValue())) {
				text.append("<table>\n");
				text.append("<tr>");
				for(int col = 0; col<table.getColumnCount(); col++) {
					text.append("<td>");
					Object v = table.getColumnName(col);
					if(v!=null)
						text.append(v.toString());
					text.append("</td>");
				}
				text.append("</tr>\n");
				for(int row = 0; row<table.getRowCount(); row++) {
					text.append("<tr>");
					for(int col = 0; col<table.getColumnCount(); col++) {
						text.append("<td>");
						Object v = table.getValueAt(row, col);
						if(v!=null)
							text.append(v.toString());
						text.append("</td>");
					}
					text.append("</tr>\n");
				}
				text.append("</table>");

			} else if(Formatting.Tab.equals(formatting.getValue())) {
				for(int col = 0; col<table.getColumnCount(); col++) {
					if(col>0) {
						text.append('\t');
					}
					String s = table.getColumnName(col).toString();
					text.append( s );
				}
				text.append("\n");
				for(int row = 0; row<table.getRowCount(); row++) {
					for(int col = 0; col<table.getColumnCount(); col++) {
						if(col>0) {
							text.append('\t');
						}
						Object v = table.getValueAt(row, col);
						if(v==null) v = "";
						String s = v.toString();
						
						text.append(s);
					}
					text.append("\n");
				}
			} else {
				int[] columnWidth = new int[table.getColumnCount()];
				for(int col = 0; col<columnWidth.length; col++) {
					columnWidth[col] = table.getColumnName(col).toString().length();
					for(int row = 0; row<table.getRowCount(); row++) {
						Object v = table.getValueAt(row, col);
						if(v==null) v = "";
						String s = v.toString();
						columnWidth[col] = Math.max( columnWidth[col], s.length());
					}
				}
				
				for(int col = 0; col<table.getColumnCount(); col++) {
					String s = table.getColumnName(col).toString();
					text.append( s );
					for(int r = 0; r<columnWidth[col]-s.length(); r++) {
						text.append(' ');
					}
					text.append("  ");
				}
				text.append("\n");
				for(int row = 0; row<table.getRowCount(); row++) {
					for(int col = 0; col<table.getColumnCount(); col++) {
						Object v = table.getValueAt(row, col);
						if(v==null) v = "";
						String s = v.toString();
						
						text.append(s);
						
						for(int r = 0; r<columnWidth[col]-s.length(); r++) {
							text.append(' ');
						}
						text.append("  ");
					}
					text.append("\n");
				}
			}
			textArea.setText(text.toString());
		}
	}
	
	abstract static class DataTable extends TextTable {
		public DataTable(Object[] columnNames,int rows) {
			super(columnNames, rows);
			
			Thread workerThread = new Thread() {
				public void run() {
					try {
						Thread.sleep(2500);
						for(int row = 0; row<table.getRowCount(); row++) {
							for(int col = 0; col<table.getColumnCount(); col++) {
								final Object value = createTableValue(row, col);
								final int myRow = row;
								final int myCol = col;
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										table.setValueAt(value, myRow, myCol);
									}
								});
							}
						}
					} catch(Throwable t) {
						t.printStackTrace();
					}
				}
			};
			workerThread.start();
		}
		
		protected abstract Object createTableValue(int row,int col);
	}
	
	JTextArea profileTextArea = new JTextArea(JVM.getProfile());
	DataTable dataTable;
	
	public TableDataApp(String labelText,Object[] columnNames,int rows) {
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0;
		c.fill = GridBagConstraints.BOTH; c.insets = new Insets(3,3,3,3);
		getContentPane().add(profileTextArea, c);
		c.gridy++;
		getContentPane().add(new JLabel(labelText), c);
		c.gridy++; c.weighty = 1;
		dataTable = new DataTable(columnNames, rows) {

			@Override
			protected Object createTableValue(int row, int col) {
				return TableDataApp.this.createTableValue(row, col);
			}
			
		};
		getContentPane().add(new JScrollPane(dataTable.textArea), c);
		
		setPreferredSize(new Dimension(500, 500));
		
		profileTextArea.setEditable(false);
		profileTextArea.setOpaque(false);
	}

	/** This generates the value of a table cell.
	 */
	protected abstract Object createTableValue(int row, int col);
}
