/*
 * Copyright (c) 2005-2018 Ibis Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *     
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *     
 *  o Neither the name of Ibis Kirill Grouchnikov nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package org.pushingpixels.ibis.transcoder;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.concurrent.CountDownLatch;

import org.pushingpixels.ibis.transcoder.java.JavaLanguageRenderer;
import org.pushingpixels.ibis.transcoder.kotlin.KotlinLanguageRenderer;

public class SvgBatchConverter {
    private static String getInputArgument(String[] args, String argumentName) {
        for (String arg : args) {
            String[] split = arg.split("=");
            if (split.length != 2) {
                System.out.println("Argument '" + arg + "' unsupported");
                System.out.println("Check the documentation for the parameters to pass");
                System.exit(1);
            }
            if (split[0].compareTo(argumentName) == 0) {
                return split[1];
            }
        }
        return null;
    }

    /**
     * @param args
     *            <ul>
     *            <li>sourceFolder=xyz - points to a folder with SVG images</li>
     *            <li>outputPackageName=xyz - the package name for the transcoded classes</li>
     *            <li>templateFile=xyz - the template file for creating the transcoded classes</li>
     *            <li>outputLanguage=java|kotlin - the language for the transcoded classes</li>
     *            <li>outputClassNamePrefix=xyz - optional prefix for the class name of each
     *            transcoded class</li>
     *            </ul>
     */
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Check the documentation for the parameters to pass");
            System.exit(1);
        }

        String sourceFolder = getInputArgument(args, "sourceFolder");
        if (sourceFolder == null) {
            System.out.println(
                    "Missing source folder. Check the documentation for the parameters to pass");
            System.exit(1);
        }
        String outputPackageName = getInputArgument(args, "outputPackageName");
        if (outputPackageName == null) {
            System.out.println(
                    "Missing output package name. Check the documentation for the parameters to pass");
            System.exit(1);
        }
        String templateFile = getInputArgument(args, "templateFile");
        if (templateFile == null) {
            System.out.println(
                    "Missing template file. Check the documentation for the parameters to pass");
            System.exit(1);
        }
        String outputLanguage = getInputArgument(args, "outputLanguage");
        if (outputLanguage == null) {
            System.out.println(
                    "Missing output language. Check the documentation for the parameters to pass");
            System.exit(1);
        }
        if ((outputLanguage.compareTo("java") != 0) && (outputLanguage.compareTo("kotlin") != 0)) {
            System.out.println(
                    "Output language must be either Java or Kotlin. Check the documentation for the parameters to pass");
            System.exit(1);
        }
        String outputClassNamePrefix = getInputArgument(args, "outputClassNamePrefix");
        if (outputClassNamePrefix == null) {
            outputClassNamePrefix = "";
        }

        File folder = new File(sourceFolder);
        if (!folder.exists()) {
            return;
        }

        LanguageRenderer languageRenderer = ("java".compareTo(outputLanguage) == 0)
                ? new JavaLanguageRenderer()
                : new KotlinLanguageRenderer();
        String outputFileNameExtension = ("java".compareTo(outputLanguage) == 0) ? ".java" : ".kt";
        
        System.out.println("******************************************************************************");
        System.out.println("Processing " + sourceFolder + " to " + outputPackageName + " in " + outputLanguage);
        System.out.println("******************************************************************************");

        for (File file : folder.listFiles((File dir, String name) -> name.endsWith(".svg"))) {
            String svgClassName = outputClassNamePrefix
                    + file.getName().substring(0, file.getName().length() - 4);
            svgClassName = svgClassName.replace('-', '_');
            svgClassName = svgClassName.replace(' ', '_');
            String javaClassFilename = folder + File.separator + svgClassName
                    + outputFileNameExtension;

            System.err.println("Processing " + file.getName());

            try {
                final CountDownLatch latch = new CountDownLatch(1);
                final PrintWriter pw = new PrintWriter(javaClassFilename);

                SvgTranscoder transcoder = new SvgTranscoder(file.toURI().toURL().toString(),
                        svgClassName, languageRenderer);
                transcoder.setPackageName(outputPackageName);
                transcoder.setListener(new TranscoderListener() {
                    public Writer getWriter() {
                        return pw;
                    }

                    public void finished() {
                        latch.countDown();
                    }
                });
                InputStream templateStream = SvgBatchConverter.class
                        .getResourceAsStream(templateFile);
                if (templateStream == null) {
                    System.err.println("Couldn't load " + templateFile);
                    return;
                }
                transcoder.transcode(templateStream);
                latch.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
