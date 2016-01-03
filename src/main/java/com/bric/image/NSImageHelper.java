/*
 * @(#)NSImageHelper.java
 *
 * $Date: 2014-06-06 20:04:49 +0200 (Fr, 06 Jun 2014) $
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
package com.bric.image;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/** This encapsulates information about NSImages available in Java.
 * <p>At some point (around 2008?) Apple made <a href="https://developer.apple.com/library/mac/documentation/Cocoa/Reference/ApplicationKit/Classes/NSImage_Class/Reference/Reference.html#//apple_ref/doc/uid/20000344-BCIFACIA">NSImages</a>
 * accessible through Java through the <code>java.awt.Toolkit</code> class
 * with code such as:
 * <p><code>Image image = Toolkit.getDefaultToolkit().getImage("NSImage://NSComputer");</code>
 * <p>At the time: that method returned a <code>BufferedImage</code>. (I believe that would have
 * been using Java 1.5.)
 * <p>Now (in 2014): using OpenJDK and Java 1.7: <code>Toolkit.getImage(s)</code> works, but
 * <code>Toolkit.createImage(s)</code> does not. (Although the latter used to work in Java 1.6)
 * <p>This class provides a simple wrapper for NSImages, and a static {@link #get(String)}
 * method that returns null when the argument doesn't map to an NSImage, or when
 * the execution environment doesn't respect the "NSImage:" protocol.
 * <p>Also this includes several static fields documenting known NSImages.
 * <p>The {@link com.bric.image.NSImageHelperDemo} catalogs these images and can search for other
 * undocumented images.
 * <p>Whenever possible an NSImageHelper should be constructed with an informative
 * description regarding its usage. Apple cautions:
 * <blockquote>You should always use named images according to their intended purpose, and not 
 * according to how the image appears when loaded. The appearance of images can change 
 * between releases. If you use an image for its intended purpose (and not because of it looks), 
 * your code should look correct from release to release.</blockquote>
 * <p>I chose not to provide a sample of each icon in the documentation for legal/ethical
 * reasons. (The current implementation (and accompanying demo app) are simply cataloging what
 * is already there, but by bundling perfectly extracted pngs I would be helping developers
 * on other platforms grab Apple's artwork with great ease.)
 */
public class NSImageHelper implements Comparable<NSImageHelper> {

	/** All known NSImageHelpers. */
	private static final SortedMap<String, NSImageHelper> knownImages = new TreeMap<String, NSImageHelper>();
	
	/** A Quick Look template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper QuickLookTemplate = get("QuickLookTemplate", "A Quick Look template image. Available in OS X v10.5 and later.");
	
	/** A Bluetooth template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper BluetoothTemplate = get("BluetoothTemplate", "A Bluetooth template image. Available in OS X v10.5 and later.");
	
	/** An iChat Theater template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper IChatTheaterTemplate = get("IChatTheaterTemplate", "An iChat Theater template image. Available in OS X v10.5 and later.");
	
	/** A slideshow template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper SlideshowTemplate = get("SlideshowTemplate", "A slideshow template image. Available in OS X v10.5 and later.");
	
	/** An action menu template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper ActionTemplate = get("ActionTemplate", "An action menu template image. Available in OS X v10.5 and later.");
	
	/** A badge for a "smart" item. Available in OS X v10.5 and later. */
	public static final NSImageHelper SmartBadgeTemplate = get("SmartBadgeTemplate", "A badge for a \"smart\" item. Available in OS X v10.5 and later.");
	
	/** An icon view mode template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper IconViewTemplate = get("IconViewTemplate", "An icon view mode template image. Available in OS X v10.5 and later.");
	
	/** A list view mode template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper ListViewTemplate = get("ListViewTemplate", "A list view mode template image. Available in OS X v10.5 and later.");
	
	/** A column view mode template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper ColumnViewTemplate = get("ColumnViewTemplate", "A column view mode template image. Available in OS X v10.5 and later.");
	
	/** A cover flow view mode template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper FlowViewTemplate = get("FlowViewTemplate", "A cover flow view mode template image. Available in OS X v10.5 and later.");
	
	/** A share view template image. Available in OS X v10.8 and later. */
	public static final NSImageHelper ShareTemplate = get("ShareTemplate", "A share view template image. Available in OS X v10.8 and later.");
	
	/** A path button template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper PathTemplate = get("PathTemplate", "A path button template image. Available in OS X v10.5 and later.");
	
	/** An invalid data template image. Place this icon to the right of any fields containing invalid data. You can use this image to implement a borderless button. Available in OS X v10.5 and later. */
	public static final NSImageHelper InvalidDataFreestandingTemplate = get("InvalidDataFreestandingTemplate", "An invalid data template image. Place this icon to the right of any fields containing invalid data. You can use this image to implement a borderless button. Available in OS X v10.5 and later.");
	
	/** A locked lock template image. Use to indicate locked content. Available in OS X v10.5 and later. */
	public static final NSImageHelper LockLockedTemplate = get("LockLockedTemplate", "A locked lock template image. Use to indicate locked content. Available in OS X v10.5 and later.");
	
	/** An unlocked lock template image. Use to indicate modifiable content that can be locked. Available in OS X v10.5 and later. */
	public static final NSImageHelper LockUnlockedTemplate= get("LockUnlockedTemplate", "An unlocked lock template image. Use to indicate modifiable content that can be locked. Available in OS X v10.5 and later.");
	
	/** A \"go forward\" template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper GoRightTemplate = get("GoRightTemplate", "A \"go forward\" template image. Available in OS X v10.5 and later."); 
	
	/** A \"go back\" template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper GoLeftTemplate = get("GoLeftTemplate", "A \"go back\" template image. Available in OS X v10.5 and later.");
	
	/** A generic right-facing triangle template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper RightFacingTriangleTemplate = get("RightFacingTriangleTemplate", "A generic right-facing triangle template image. Available in OS X v10.5 and later.");
	
	/** A generic left-facing triangle template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper LeftFacingTriangleTemplate = get("LeftFacingTriangleTemplate", "A generic left-facing triangle template image. Available in OS X v10.5 and later.");
	
	/** An add item template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper AddTemplate = get("AddTemplate", "An add item template image. Available in OS X v10.5 and later.");
	
	/** A remove item template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper RemoveTemplate = get("RemoveTemplate", "A remove item template image. Available in OS X v10.5 and later.");
	
	/** A reveal contents template image. You can use this image to implement a borderless button. Available in OS X v10.5 and later. */
	public static final NSImageHelper RevealFreestandingTemplate = get("RevealFreestandingTemplate", "A reveal contents template image. You can use this image to implement a borderless button. Available in OS X v10.5 and later.");
	
	/** A link template image. You can use this image to implement a borderless button. Available in OS X v10.5 and later. */
	public static final NSImageHelper FollowLinkFreestandingTemplate = get("FollowLinkFreestandingTemplate", "A link template image. You can use this image to implement a borderless button. Available in OS X v10.5 and later.");
	
	/** An enter full-screen mode template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper EnterFullScreenTemplate = get("EnterFullScreenTemplate", "An enter full-screen mode template image. Available in OS X v10.5 and later.");
	
	/** An exit full-screen mode template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper ExitFullScreenTemplate = get("ExitFullScreenTemplate", "An exit full-screen mode template image. Available in OS X v10.5 and later.");
	
	/** A stop progress button template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper StopProgressTemplate = get("StopProgressTemplate", "A stop progress button template image. Available in OS X v10.5 and later.");
	
	/** A stop progress template image. You can use this image to implement a borderless button. Available in OS X v10.5 and later. */
	public static final NSImageHelper StopProgressFreestandingTemplate = get("StopProgressFreestandingTemplate", "A stop progress template image. You can use this image to implement a borderless button. Available in OS X v10.5 and later.");
	
	/** A refresh template image. Available in OS X v10.5 and later. */
	public static final NSImageHelper RefreshTemplate = get("RefreshTemplate", "A refresh template image. Available in OS X v10.5 and later.");
	
	/** A refresh template image. You can use this image to implement a borderless button. Available in OS X v10.5 and later. */
	public static final NSImageHelper RefreshFreestandingTemplate = get("RefreshFreestandingTemplate", "A refresh template image. You can use this image to implement a borderless button. Available in OS X v10.5 and later.");
	
	/** A Bonjour icon. Available in OS X v10.5 and later. */
	public static final NSImageHelper Bonjour = get("Bonjour", "A Bonjour icon. Available in OS X v10.5 and later.");
	
	/** A Dot Mac icon. Available in OS X v10.5 and later. */
	public static final NSImageHelper DotMac = get("DotMac", "A Dot Mac icon. Available in OS X v10.5 and later.");
	
	/** A computer icon. Available in OS X v10.5 and later. */
	public static final NSImageHelper Computer = get("Computer", "A computer icon. Available in OS X v10.5 and later.");
	
	/** A burnable folder icon. Available in OS X v10.5 and later. */
	public static final NSImageHelper FolderBurnable = get("FolderBurnable", "A burnable folder icon. Available in OS X v10.5 and later.");
	
	/** A smart folder icon. Available in OS X v10.5 and later. */
	public static final NSImageHelper FolderSmart = get("FolderSmart", "A smart folder icon. Available in OS X v10.5 and later.");
	
	/** A network icon. Available in OS X v10.5 and later. */
	public static final NSImageHelper Network = get("Network", "A network icon. Available in OS X v10.5 and later.");
	
	/** A drag image for multiple items. Available in OS X v10.5 and later. *?
	public static final NSImageHelper MultipleDocuments = get("MultipleDocuments", "A drag image for multiple items. Available in OS X v10.5 and later.");
	
	/** User account toolbar icon. Use in a preferences window only. */
	public static final NSImageHelper UserAccounts = get("UserAccounts","User account toolbar icon. Use in a preferences window only.");
	
	/** General preferences toolbar icon. Use in a preferences window only. Available in OS X v10.5 and later. */
	public static final NSImageHelper PreferencesGeneral = get("PreferencesGeneral", "General preferences toolbar icon. Use in a preferences window only. Available in OS X v10.5 and later.");
	
	/** Advanced preferences toolbar icon. Use in a preferences window only. Available in OS X v10.5 and later. */
	public static final NSImageHelper Advanced = get("Advanced", "Advanced preferences toolbar icon. Use in a preferences window only. Available in OS X v10.5 and later.");
	
	/** An information toolbar icon. Available in OS X v10.5 and later. */
	public static final NSImageHelper Info = get("Info", "An information toolbar icon. Available in OS X v10.5 and later.");
	
	/** A font panel toolbar icon. Available in OS X v10.5 and later. */
	public static final NSImageHelper FontPanel = get("FontPanel", "A font panel toolbar icon. Available in OS X v10.5 and later.");
	
	/** A color panel toolbar icon. Available in OS X v10.5 and later. */
	public static final NSImageHelper ColorPanel = get("ColorPanel", "A color panel toolbar icon. Available in OS X v10.5 and later.");
	
	/** Permissions for a single user. Available in OS X v10.5 and later. */
	public static final NSImageHelper User = get("User", "Permissions for a single user. Available in OS X v10.5 and later.");
	
	/** Permissions for a group of users. Available in OS X v10.5 and later. */
	public static final NSImageHelper UserGroup = get("UserGroup", "Permissions for a group of users. Available in OS X v10.5 and later.");
	
	/** Permissions for all users. Available in OS X v10.5 and later. */
	public static final NSImageHelper Everyone = get("Everyone", "Permissions for all users. Available in OS X v10.5 and later.");
	
	/** Permissions for guests. Available in OS X v10.6 and later. */
	public static final NSImageHelper UserGuest = get("UserGuest", "Permissions for guests. Available in OS X v10.6 and later.");
	
	/** A folder image. Available in OS X v10.6 and later. */
	public static final NSImageHelper Folder = get("Folder", "A folder image. Available in OS X v10.6 and later.");
	
	/** An image of the empty trash can. Available in OS X v10.6 and later. */
	public static final NSImageHelper TrashEmpty = get("TrashEmpty", "An image of the empty trash can. Available in OS X v10.6 and later.");
	
	/** An image of the full trash can. Available in OS X v10.6 and later. */
	public static final NSImageHelper TrashFull = get("TrashFull", "An image of the full trash can. Available in OS X v10.6 and later.");
	
	/** Home image suitable for a template. Available in OS X v10.6 and later. */
	public static final NSImageHelper HomeTemplate = get("HomeTemplate", "Home image suitable for a template. Available in OS X v10.6 and later.");
	
	/** Bookmarks image suitable for a template. Available in OS X v10.6 and later. */
	public static final NSImageHelper BookmarksTemplate = get("BookmarksTemplate", "Bookmarks image suitable for a template. Available in OS X v10.6 and later.");
	
	/** Caution Image. Available in OS X v10.6 and later. */
	public static final NSImageHelper Caution = get("Caution", "Caution Image. Available in OS X v10.6 and later.");
	
	/** Small green indicator, similar to iChat's available image. Available in OS X v10.6 and later. */
	public static final NSImageHelper StatusAvailable = get("StatusAvailable", "Small green indicator, similar to iChat's available image. Available in OS X v10.6 and later.");
	
	/** Small yellow indicator, similar to iChat's idle image. Available in OS X v10.6 and later. */
	public static final NSImageHelper StatusPartiallyAvailable = get("StatusPartiallyAvailable", "Small yellow indicator, similar to iChat's idle image. Available in OS X v10.6 and later.");
	
	/** Small red indicator, similar to iChat's unavailable image. Available in OS X v10.6 and later. */
	public static final NSImageHelper StatusUnavailable = get("StatusUnavailable", "Small red indicator, similar to iChat's unavailable image. Available in OS X v10.6 and later.");
	
	/** Small clear indicator. Available in OS X v10.6 and later. */
	public static final NSImageHelper StatusNone = get("StatusNone", "Small clear indicator. Available in OS X v10.6 and later.");
	
	/** A check mark. Drawing these outside of menus is discouraged. Available in OS X v10.6 and later. */
	public static final NSImageHelper MenuOnStateTemplate = get("MenuOnStateTemplate", "A check mark. Drawing these outside of menus is discouraged. Available in OS X v10.6 and later.");
	
	/** A horizontal dash. Drawing these outside of menus is discouraged. Available in OS X v10.6 and later. */
	public static final NSImageHelper MenuMixedStateTemplate = get("MenuMixedStateTemplate", "A horizontal dash. Drawing these outside of menus is discouraged. Available in OS X v10.6 and later.");
	
	/** MobileMe logo. Note that this is preferred to using the NSImageNameDotMac image, although that image is not expected to be deprecated. Available in OS X v10.6 and later. */
	public static final NSImageHelper MobileMe = get("MobileMe", "MobileMe logo. Note that this is preferred to using the NSImageNameDotMac image, although that image is not expected to be deprecated. Available in OS X v10.6 and later.");

	//these were all identified programmatically in NSImageHelperDemo:
	
	public static final NSImageHelper Accounts = get("Accounts");
	public static final NSImageHelper Action = get("Action");
	public static final NSImageHelper Add = get("Add");
	public static final NSImageHelper Bluetooth = get("Bluetooth");
	public static final NSImageHelper Color = get("Color");
	public static final NSImageHelper Font = get("Font");
	public static final NSImageHelper Group = get("Group");
	public static final NSImageHelper Link = get("Link");
	public static final NSImageHelper Path = get("Path");
	public static final NSImageHelper Refresh = get("Refresh");
	public static final NSImageHelper Remove = get("Remove");
	public static final NSImageHelper Slideshow = get("Slideshow");
	public static final NSImageHelper Stop = get("Stop");
	public static final NSImageHelper Actions = get("Actions");
	public static final NSImageHelper Bookmark = get("Bookmark");
	public static final NSImageHelper Bookmarks = get("Bookmarks");
	public static final NSImageHelper Bug = get("Bug");
	public static final NSImageHelper Burning = get("Burning");
	public static final NSImageHelper Cancel = get("Cancel");
	public static final NSImageHelper Disclosed = get("Disclosed");
	public static final NSImageHelper Effect = get("Effect");
	public static final NSImageHelper Erase = get("Erase");
	public static final NSImageHelper Home = get("Home");
	public static final NSImageHelper Pause = get("Pause");
	public static final NSImageHelper Person = get("Person");
	public static final NSImageHelper Photograph = get("Photograph");
	public static final NSImageHelper Play = get("Play");
	public static final NSImageHelper Print = get("Print");
	public static final NSImageHelper Reload = get("Reload");
	public static final NSImageHelper Rewind = get("Rewind");
	public static final NSImageHelper Script = get("Script");
	public static final NSImageHelper Security = get("Security");
	public static final NSImageHelper Share = get("Share");
	public static final NSImageHelper Snapback = get("Snapback");
	public static final NSImageHelper Switch = get("Switch");
	public static final NSImageHelper AccountsTemplate = get("AccountsTemplate");
	public static final NSImageHelper ActionsTemplate = get("ActionsTemplate");
	public static final NSImageHelper AddBoomark = get("AddBookmark");
	public static final NSImageHelper AdvancedPreferences = get("AdvancedPreferences");
	public static final NSImageHelper AdvancedTemplate = get("AdvancedTemplate");
	public static final NSImageHelper BonjourTemplate = get("BonjourTemplate");
	public static final NSImageHelper BookmarkLock = get("BookmarkLock");
	public static final NSImageHelper BookmarkTemplate = get("BookmarkTemplate");
	public static final NSImageHelper BugTemplate = get("BugTemplate");
	public static final NSImageHelper BurningTemplate = get("BurningTemplate");
	public static final NSImageHelper CancelTemplate = get("CancelTemplate");
	public static final NSImageHelper CautionTemplate = get("CautionTemplate");
	public static final NSImageHelper ColorTemplate = get("ColorTemplate");
	public static final NSImageHelper ColumnView = get("ColumnView");
	public static final NSImageHelper ComputerTemplate = get("ComputerTemplate");
	public static final NSImageHelper DisclosedTemplate = get("DisclosedTemplate");
	public static final NSImageHelper DisclosedAlternate = get("DisclosedAlternate");
	public static final NSImageHelper EffectTemplate = get("EffectTemplate");
	public static final NSImageHelper EraseTemplate = get("EraseTemplate");
	public static final NSImageHelper EveryoneTemplate = get("EveryoneTemplate");
	public static final NSImageHelper FlowView = get("FlowView");
	public static final NSImageHelper FolderTemplate = get("FolderTemplate");
	public static final NSImageHelper FontTemplate = get("FontTemplate");
	public static final NSImageHelper GeneralPreferences = get("GeneralPreferences");
	public static final NSImageHelper GoLeft = get("GoLeft");
	public static final NSImageHelper GoRight = get("GoRight");
	public static final NSImageHelper GoBack = get("GoBack");
	public static final NSImageHelper GoForward = get("GoForward");
	public static final NSImageHelper GroupTemplate = get("GroupTemplate");
	public static final NSImageHelper IconBurning = get("IconBurning");
	public static final NSImageHelper IconComputer = get("IconComputer");
	public static final NSImageHelper IconGroup = get("IconGroup");
	public static final NSImageHelper IconLocked = get("IconLocked");
	public static final NSImageHelper IconUnlocked = get("IconUnlocked");
	public static final NSImageHelper IconUser = get("IconUser");
	public static final NSImageHelper IconView = get("IconView");
	public static final NSImageHelper IconClipboard = get("IconClipboard");
	public static final NSImageHelper IconDesktop = get("IconDesktop");
	public static final NSImageHelper IconFinder = get("IconFinder");
	public static final NSImageHelper IconGrid = get("IconGrid");
	public static final NSImageHelper IconHelp = get("IconHelp");
	public static final NSImageHelper IconOwner = get("IconOwner");
	public static final NSImageHelper IconShortcut = get("IconShortcut");
	public static final NSImageHelper IconTrash = get("IconTrash");
	public static final NSImageHelper InfoTemplate = get("InfoTemplate");
	public static final NSImageHelper InvalidData = get("InvalidData");
	public static final NSImageHelper LinkTemplate = get("LinkTemplate");
	public static final NSImageHelper LinkButton = get("LinkButton");
	public static final NSImageHelper ListView = get("ListView");
	public static final NSImageHelper LockLocked = get("LockLocked");
	public static final NSImageHelper LockUnlocked = get("LockUnlocked");
	public static final NSImageHelper MultipleItems = get("MultipleItems");
	public static final NSImageHelper NetworkTemplate = get("NetworkTemplate");
	public static final NSImageHelper PathIndicator = get("PathIndicator");
	public static final NSImageHelper PauseTemplate = get("PauseTemplate");
	public static final NSImageHelper PersonTemplate = get("PersonTemplate");
	public static final NSImageHelper PersonAnonymous = get("PersonAnonymous");
	public static final NSImageHelper PersonUnknown = get("PersonUnknown");
	public static final NSImageHelper PhotographTemplate = get("PhotographTemplate");
	public static final NSImageHelper PlayTemplate = get("PlayTemplate");
	public static final NSImageHelper PrintTemplate = get("PrintTemplate");
	public static final NSImageHelper QuickLook = get("QuickLook");
	public static final NSImageHelper RefreshFreestanding = get("RefreshFreestanding");
	public static final NSImageHelper ReloadTemplate = get("ReloadTemplate");
	public static final NSImageHelper RevealFreestanding = get("RevealFreestanding");
	public static final NSImageHelper RewindTemplate = get("RewindTemplate");
	public static final NSImageHelper ScriptTemplate = get("ScriptTemplate");
	public static final NSImageHelper SecurityTemplate = get("SecurityTemplate");
	public static final NSImageHelper SmartBadge = get("SmartBadge");
	public static final NSImageHelper SnapbackTemplate = get("SnapbackTemplate");
	public static final NSImageHelper StopProgress = get("StopProgress");
	public static final NSImageHelper StopTemplate = get("StopTemplate");
	public static final NSImageHelper SynchronizeTemplate = get("SynchronizeTemplate");
	public static final NSImageHelper SynchronizeConflict = get("SynchronizeConflict");
	public static final NSImageHelper SynchronizeStart = get("SynchronizeStart");
	public static final NSImageHelper Synchronize = get("Synchronize");
	public static final NSImageHelper TheaterStart = get("TheaterStart");
	public static final NSImageHelper UserTemplate = get("UserTemplate");
	public static final NSImageHelper ViewList = get("ViewList");
	public static final NSImageHelper ViewColumns = get("ViewColumns");
	public static final NSImageHelper ViewGroups = get("ViewGroups");
	public static final NSImageHelper ViewIcons = get("ViewIcons");
	public static final NSImageHelper KEXT = get("KEXT");
	
	/** Return all <code>NSImageHelpers</code> identified in this session.
	 * <p>Just by initializing this class: all the static public
	 * NSImageHelper fields will guarantee that (if NSImages are supported)
	 * there are a handful of images to work with.
	 */
	public static SortedSet<NSImageHelper> getAllImages() {
		TreeSet<NSImageHelper> returnValue = new TreeSet<NSImageHelper>();
		returnValue.addAll(knownImages.values());
		return returnValue;
	}
	
	/** Return an <code>NSImageHelper</code> that uses
	 * a given name. Note the name used by NSImageHelper
	 * strips away the redundant "NSImage://NS", so "NSImage://NSComputer" 
	 * because "Computer".
	 * 
	 * @param nsImageName an NSImage name, such as "Computer".
	 */
	public static NSImageHelper get(String nsImageName) {
		return get(nsImageName, "");
	}

	/** Return an <code>NSImageHelper</code> that uses
	 * a given name, and if possible identifies something about its usage.
	 * Note the name used by NSImageHelper
	 * strips away the redundant "NSImage://NS", so "NSImage://NSComputer" 
	 * because "Computer".
	 * 
	 * @param nsImageName an NSImage name, such as "Computer".
	 * @param description an optional description, preferably based on
	 * Apple's documentation. Apple sometimes has thorough guidelines about
	 * when to use which icon(s), so this description might speak to those.
	 */
	public static NSImageHelper get(String nsImageName,String description) {
		synchronized(knownImages) {
			if(description==null) description = "";
			
			NSImageHelper h = knownImages.get(nsImageName);
			if(h==null) {
				try {
					h = new NSImageHelper(nsImageName, description);
					knownImages.put(nsImageName, h);
				} catch(Exception e) {
					return null;
				}
			}
			
			return h;
		}
	}
	
	/** Return true if the <code>NSImageHelper</code> class is
	 * supported on this platform. If false then all the public static
	 * fields in this class are probably null.
	 */
	public static boolean isSupported() {
		return knownImages.size()>0;
	}
	
	final String nsImageName;
	final String usage;
	final BufferedImage bi;

	private NSImageHelper(String nsImageName) {
		this(nsImageName, "");
	}
	
	/** Throws a NullPointerException if the image is missing, or cannot be loaded.
	 * 
	 * @param nsImageName an NSImage name, such as "Computer". (Not "NSImage://NSComputer")
	 * @param description an optional description, preferably based on
	 * Apple's documentation. Apple sometimes has thorough guidelines about
	 * when to use which icon(s), so this description might speak to those.
	 * @throws NullPointerException if the image is missing, or cannot be loaded.
	 */
	private NSImageHelper(String nsImageName,String usage) {
		if(nsImageName==null) throw new NullPointerException();
		if(usage==null) throw new NullPointerException();
		this.nsImageName = nsImageName;
		this.usage = usage;

		String n = "NSImage://NS"+nsImageName;
		Image image = Toolkit.getDefaultToolkit().getImage(n);

		bi = ImageLoader.createImage(image);
		if(bi==null)
			throw new NullPointerException();
	}
	
	/** Return a <code>BufferedImage</code> copy of this NSImage. */
	public BufferedImage getImage() {
		return bi;
	}

	/** Return the name of this NSImage. For example, this might return "Computer"
	 * (but not "NSImage://NSComputer").
	 */
	public String getName() {
		return nsImageName;
	}

	/** Return the optional description of this image. which should
	 * be based on Apple's documentation. Apple sometimes has thorough guidelines about
	 * when to use which icon(s), so this description might speak to those.
	 * <p>If missing: this will be an empty string (but not null).
	 */
	public String getUsage() {
		return usage;
	}

	@Override
	public int hashCode() {
		return nsImageName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof NSImageHelper))
			return false;
		return compareTo( (NSImageHelper)obj )==0;
	}

	@Override
	public String toString() {
		return "NSImageHelper[ \""+nsImageName+"\", \""+usage+"\"]";
	}

	public int compareTo(NSImageHelper o) {
		return nsImageName.compareTo(o.nsImageName);
	}	
}
