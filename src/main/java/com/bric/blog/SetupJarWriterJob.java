/*
 * @(#)SetupJob.java
 *
 * $Date: 2015-03-13 02:03:57 +0100 (Fr, 13 Mär 2015) $
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
package com.bric.blog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.bric.jar.EclipseClasspathFile;
import com.bric.jar.JarWriter;


/** This job sets up the JarWriter (which requires dumping hundreds of Java source
 * files into a temp directory), and sets up the license, and anything else
 * that should be done <i>before</i> the blog updating app starts to build deliverables.
 *
 */
public class SetupJarWriterJob extends BlogUpdaterJob {
	final JarWriter jarWriter;
	public SetupJarWriterJob(WorkspaceContext context, JarWriter writer) {
		super(context);
		this.jarWriter = writer;
		setDescription("Setting up compiler...");
	}

	@Override
	protected void runJob() throws Exception {
		File licenseFile = createLicenseFile();
		jarWriter.addResource("License.html", licenseFile);
		
		File xmlFile = new File(context.getWorkspaceDirectory(), ".classpath");
		if(!xmlFile.exists())
			throw new RuntimeException("The BlogUpdaterApp needs to be executed from the Eclipse workspace used to manage this project.");
		
		//the old way:
		//for(File jarFile : context.getJars()) {
		//	setNote("Loading \""+jarFile.getName()+"\"");
		//	jarWriter.addJar(jarFile);
		//}
		//for(File sourcePath : context.getSourcePaths()) {
		//	setNote("Loading \""+sourcePath.getAbsolutePath()+"\"");
		//	jarWriter.addSourcepath(sourcePath);
		//}
		
		//the new way:
		EclipseClasspathFile ecf = new EclipseClasspathFile(xmlFile, true);
		for(File src : ecf.srcpaths) {
			jarWriter.addSourcepath(src);
		}
		for(File lib : ecf.libs) {
			jarWriter.addJar(lib);
		}
	}

	/** Creates the html-encoded modified BSD license for bric code
	 * in this project.
	 * @throws IOException if an IO problem occurs.
	 */
	public static File createLicenseFile() throws IOException {
		int year = ((new GregorianCalendar()).get(Calendar.YEAR));
		File temp = File.createTempFile("license", ".html");
		temp.deleteOnExit();
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(temp);
			PrintStream ps = new PrintStream(out);
			ps.println("<html>");
			ps.println("Source code, binaries and/or any other resources in the package labeled \"com.bric\" are copyright (c) "+year+" by Jeremy Wood.  They are available under the Modified BSD license (see below).");
			ps.println("<P>Any resources not in the \"com.bric\" package may be subject to additional license terms and restrictions.");
			ps.println("<P>If you have any questions about this jar, the relevant licenses, the source code, etc., please contact <A HREF=\"mailto:mickleness+java@gmail.com\">mickleness+java@gmail.com</A>.");
			ps.println("<P>This jar is part of the \"javagraphics\" project, discussed <A HREF=\"https://javagraphics.java.net/\">here</A>.");
			ps.println("<h3>Modified BSD License</H3>");
			ps.println("<P>Copyright (c) "+year+", Jeremy Wood.");
			ps.println("<BR>All rights reserved.");
			ps.println("<P>Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:");
			ps.println();
			ps.println("<BR> * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.");
			ps.println("<BR> * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.");
			ps.println("<BR> * The name of the contributors may not be used to endorse or promote products derived from this software without specific prior written permission.");
			ps.println("<P>THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.");
			ps.println("</html>");
			ps.flush();
			return temp;
		} finally {
			if(out!=null) {
				try {
					out.close();
				} catch(Throwable t) {}
			}
		}
	}
}
