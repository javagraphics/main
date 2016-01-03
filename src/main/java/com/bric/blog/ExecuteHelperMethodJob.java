/*
 * @(#)ExecuteHelpersJob.java
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
package com.bric.blog;

import java.awt.Dimension;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.bric.image.pixel.Scaling;

/** Create static resources defined by the Helper classes.
 * The {@link BlogHelper} javadoc explains the method signatures
 * this job identifies.
 */
class ExecuteHelperMethodJob extends BlogUpdaterJob {
	
	/** Create new jobs to execute the accompanying Helper class methods for
	 *  a java file, or an empty array if there is no Helper class for this
	 *  java file.
	 * @param context
	 * @param javaFile a file "Foo.java" for which there is a "FooHelper.class"
	 * with recognized static methods
	 * @return a series of jobs to execute all helper methods
	 */
	public static ExecuteHelperMethodJob[] get(WorkspaceContext context,File javaFile) {
		List<ExecuteHelperMethodJob> jobs = new ArrayList<ExecuteHelperMethodJob>();
		
		String path = javaFile.getAbsolutePath();
		
		int i = path.lastIndexOf(".java");
		if(i!=-1) {
			String helperPath = path.substring(0,i)+"Helper.java";
			File helperFile = new File(helperPath);

			if(helperFile.exists()) {
				String projectName = context.getJarName(javaFile);
				String className = context.getClassName(helperFile);
				Class<?> c;
				try {
					c = Class.forName(className);
					Method[] methods = c.getDeclaredMethods();
					for(Method method : methods) {
						if( isStatic(method) && isPublic(method) ) {
							Class<?>[] params = method.getParameterTypes();
							if(method.getReturnType().equals(File.class) && 
									params.length==2 && 
									params[0].equals(Robot.class) &&
									params[1].equals(File.class)) {
								jobs.add(new ExecuteHelperMethodJob(context, method, projectName, FILE_WITH_ROBOT));
							} else if(method.getReturnType().equals(File.class) && 
									params.length==1 && params[0].equals(File.class)) {
								jobs.add(new ExecuteHelperMethodJob(context, method, projectName, FILE));
							} else if(method.getReturnType().equals(BufferedImage.class) && 
									params.length==1 && params[0].equals(Dimension.class)) {
								jobs.add(new ExecuteHelperMethodJob(context, method, projectName, IMAGE));
							} else { 
								System.err.println("Unsupported public static method: "+method.getDeclaringClass().getName()+"#"+method.getName());
							}
						}
					}
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return jobs.toArray(new ExecuteHelperMethodJob[jobs.size()]);
	}
	
	static final int IMAGE = 0;
	static final int FILE = 1;
	static final int FILE_WITH_ROBOT = 2;
	
	final protected Method method;
	final int type;
	final String projectName;
	Robot robot = null;
	
	private ExecuteHelperMethodJob(WorkspaceContext context,Method m,String projectName,int type) {
		super(context);
		this.method = m;
		this.type = type;
		this.projectName = projectName;
		setName(m.getName());
		setDescription(m.getDeclaringClass().getName());
		setNote(m.getName());
	}
	
	public boolean requiresRobot() {
		return type==FILE_WITH_ROBOT;
	}
	
	public int getType() {
		return type;
	}
	
	public void setRobot(Robot robot) {
		this.robot = robot;
	}

	@Override
	protected void runJob() throws Exception {
		File targetDirectory;
		if(type==IMAGE) {
			targetDirectory = context.getDestinationSubdirectory("blurbs");
		} else {
			targetDirectory = context.getDestinationSubdirectory("resources");
		}
		
		if(type==IMAGE) {
			Dimension maxThumbnailSize = new Dimension(160,80);
			BufferedImage bi = (BufferedImage)method.invoke(null, new Object[] { maxThumbnailSize });
			if(bi.getWidth()>maxThumbnailSize.width || bi.getHeight()>maxThumbnailSize.height) {
				Dimension newSize = Scaling.scaleDimensionsProportionally(
						new Dimension(bi.getWidth(), bi.getHeight()), 
						maxThumbnailSize);
				bi = Scaling.scale(bi, newSize);
			}
			bi = padImage(bi, 5, 5, 5, 5);
			ImageIO.write(bi, "png", new File(targetDirectory, projectName+".png"));
		} else {
			if(type==FILE_WITH_ROBOT) {
				method.invoke(null, new Object[] {robot, targetDirectory});
			} else if(type==FILE) {
				method.invoke(null, new Object[] {targetDirectory});
			} else {
				throw new RuntimeException("unexpected type: "+type);
			}
		}
	}
	
	protected static boolean isStatic(Method m) {
		return (m.getModifiers() & Modifier.STATIC)>0;
	}
	
	protected static boolean isPublic(Method m) {
		return (m.getModifiers() & Modifier.PUBLIC)>0;
	}

}
