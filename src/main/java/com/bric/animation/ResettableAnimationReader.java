/*
 * @(#)ResettableAnimationReader.java
 *
 * $Date: 2014-05-04 18:08:30 +0200 (So, 04 Mai 2014) $
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
package com.bric.animation;

import com.bric.util.Resettable;

/** A combination of the <code>AnimationReader</code> and <code>Resettable</code>
 * interfaces.
 */
public interface ResettableAnimationReader extends AnimationReader, Resettable {

}
