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
package org.pushingpixels.ibis;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.concurrent.CountDownLatch;

public class SvgBatchConverter {
    /**
     * @param args
     *            First parameter should point to a folder with SVG images. Second parameter should
     *            be the package name for the transcoded classes. Third parameter should point to
     *            the template file. Fourth parameter is optional; when it is present, it is used as
     *            a prefix for the class name of the transcoded class.
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("param 0 : dir, param 1 : pkg, param 2 : template");
            System.exit(1);
        }

        File folder = new File(args[0]);
        if (!folder.exists())
            return;
        
        String classPrefix = "";
        if (args.length == 4) {
            classPrefix = args[3];
        }

        for (File file : folder.listFiles((File dir, String name) -> name.endsWith(".svg"))) {
            String svgClassName = classPrefix + 
                    file.getName().substring(0, file.getName().length() - 4);
            svgClassName = svgClassName.replace('-', '_');
            svgClassName = svgClassName.replace(' ', '_');
            String javaClassFilename = folder + File.separator + svgClassName + ".java";

            System.err.println("Processing " + file.getName());

            try {
                final CountDownLatch latch = new CountDownLatch(1);
                final PrintWriter pw = new PrintWriter(javaClassFilename);

                SvgTranscoder transcoder = new SvgTranscoder(file.toURI().toURL().toString(),
                        svgClassName);
                transcoder.setJavaPackageName(args[1]);
                transcoder.setListener(new TranscoderListener() {
                    public Writer getWriter() {
                        return pw;
                    }

                    public void finished() {
                        latch.countDown();
                    }
                });
                InputStream templateStream = SvgBatchConverter.class.getResourceAsStream(args[2]);
                if (templateStream == null) {
                    System.err.println("Couldn't load " + args[2]);
                    return;
                }
                // InputStream templateStream = SvgTranscoder.class.getResourceAsStream(
                // "SvgTranscoderTemplateResizable.templ");
                // "SvgTranscoderTemplateSubstance.templ");
                transcoder.transcode(templateStream);
                latch.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
