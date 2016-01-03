/*
 * @(#)CapsuleButtonUI.java
 *
 * $Date: 2014-06-22 18:00:49 +0200 (So, 22 Jun 2014) $
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

import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

/** <img src="https://javagraphics.java.net/resources/filledbuttonui/CapsuleButtonUI.png" alt="CapsuleButtonUI Screenshot">
 * 
 * @see com.bric.plaf.FilledButtonUIDemo
 */
public class CapsuleButtonUI extends FilledButtonUI {

	public static final SimpleButtonFill CAPSULE_FILL = new SimpleButtonFill() {
		private Color strokeColor = new Color(0,0,0,154);
		private float[] fillWeights = new float[] {0, .38f, .49999f, .5f, 1};
		private Color[] normalColors = new Color[] {
				new Color(210,210,210),
				new Color(210,210,210),
				new Color(204,204,204),
				new Color(188,188,188),
				new Color(213,213,213)
		};
		private Color[] darkerColors = new Color[] {
				new Color(170, 170, 170),
				new Color(166, 166, 166),
				new Color(157, 157, 157),
				new Color(122, 122, 122),
				new Color(185, 185, 185)
				
		};
		private Color[] rolloverColors = new Color[] {
				new Color(192, 192, 192),
				new Color(186, 186, 186),
				new Color(179, 179, 179),
				new Color(158, 158, 158),
				new Color(196, 196, 196)
		};

		@Override
		public Paint getDarkerFill(Rectangle fillRect) {
			return PlafPaintUtils.getVerticalGradient("capsuleUI.darker", 
					fillRect.height, fillRect.y,
					fillWeights, darkerColors);
		}

		@Override
		public Paint getDarkestFill(Rectangle fillRect) {
			return null;
		}

		@Override
		public Paint getNormalFill(Rectangle fillRect) {
			return PlafPaintUtils.getVerticalGradient("capsuleUI.normal", 
					fillRect.height, fillRect.y,
					fillWeights, normalColors);
		}

		@Override
		public Paint getRolloverFill(Rectangle fillRect) {
			return PlafPaintUtils.getVerticalGradient("capsuleUI.rollover", 
					fillRect.height, fillRect.y,
					fillWeights, rolloverColors);
		}

		@Override
		public Paint getStroke(AbstractButton button, Rectangle fillRect) {	
			return strokeColor;
		}
	};
	
	public static final ButtonShape CAPSULE_SHAPE = new ButtonShape(8, Short.MAX_VALUE);

	private static CapsuleButtonUI capsuleButtonUI = new CapsuleButtonUI();

	/** Create a new instance of this ButtonUI for a component.
	 * <p>This method is required if you want to make this ButtonUI the default
	 * UI by invoking:
	 * <br><code>UIManager.getDefaults().put("ButtonUI", "com.bric.plaf.CapsuleButtonUI");</code>
	 */
    public static ComponentUI createUI(JComponent c) {
        return capsuleButtonUI;
    }

	public CapsuleButtonUI() {
		super(CAPSULE_FILL, CAPSULE_SHAPE);
	}
	
	@Override
	public boolean isFillOpaque() {
		return true;
	}
}
