/*
 * Copyright (c) 2005-2017 Ibis Kirill Grouchnikov. All Rights Reserved.
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
package utest.common.icon;

import java.awt.Dimension;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.fest.assertions.Assertions;
import org.fest.swing.timing.Condition;
import org.fest.swing.timing.Pause;
import org.junit.Before;
import org.junit.Test;
import org.pushingpixels.flamingo.api.common.AsynchronousLoadListener;
import org.pushingpixels.flamingo.api.common.AsynchronousLoading;
import org.pushingpixels.ibis.SvgBatikResizableIcon;

public class SvgIconResizingTestCase {
    protected SvgBatikResizableIcon icon;

    @Before
    public void setUp() throws Exception {
        URL resource = SvgIconResizingTestCase.class.getClassLoader()
                .getResource("utest/common/icon/edit-paste.svg");
        Assertions.assertThat(resource).isNotNull();
        this.icon = SvgBatikResizableIcon.getSvgIcon(resource, new Dimension(32, 32));
        Assertions.assertThat(this.icon).isNotNull();
        Pause.pause(new Condition("Waiting to load icon") {
            @Override
            public boolean test() {
                return !((AsynchronousLoading) icon).isLoading();
            }
        });
    }

    @Test
    public void noCompletedMessageOnSettingSameHeight() {
        final int[] count = new int[] { 0 };
        final CountDownLatch latch = new CountDownLatch(1);
        AsynchronousLoadListener listener = (boolean success) -> {
            count[0]++;
            latch.countDown();
        };
        this.icon.addAsynchronousLoadListener(listener);
        // verify the icon height
        Assertions.assertThat(this.icon.getIconHeight()).isEqualTo(32);
        // set the icon height to the same value
        this.icon.setDimension(new Dimension(32, 32));
        try {
            // latch.await should not return true as that would mean that
            // the asynchronous load listener was notified
            Assertions.assertThat(latch.await(3, TimeUnit.SECONDS)).isFalse();
        } catch (InterruptedException ie) {
            Assertions.assertThat(true).isFalse();
        }
        Assertions.assertThat(count[0]).isEqualTo(0);
        this.icon.removeAsynchronousLoadListener(listener);
    }

    @Test
    public void completedMessageOnSettingDifferentHeight() {
        final int[] count = new int[] { 0 };
        final CountDownLatch latch = new CountDownLatch(1);
        AsynchronousLoadListener listener = (boolean success) -> {
            if (success) {
                count[0]++;
            }
            latch.countDown();
        };
        this.icon.addAsynchronousLoadListener(listener);
        // verify the icon height
        Assertions.assertThat(this.icon.getIconHeight()).isEqualTo(32);
        // set the icon height to different value
        this.icon.setDimension(new Dimension(64, 64));
        try {
            latch.await();
        } catch (InterruptedException ie) {
            Assertions.assertThat(true).isFalse();
        }
        Assertions.assertThat(count[0]).isEqualTo(1);
        this.icon.removeAsynchronousLoadListener(listener);
    }

    @Test
    public void changedHeightOnSettingDifferentHeight() {
        final CountDownLatch latch = new CountDownLatch(1);
        AsynchronousLoadListener listener = (boolean success) -> latch.countDown();
        this.icon.addAsynchronousLoadListener(listener);
        // verify the icon height
        Assertions.assertThat(this.icon.getIconHeight()).isEqualTo(32);
        // set the icon height to different value
        this.icon.setDimension(new Dimension(64, 64));
        try {
            latch.await();
        } catch (InterruptedException ie) {
            Assertions.assertThat(true).isFalse();
        }
        Assertions.assertThat(icon.getIconHeight()).isEqualTo(64);
        this.icon.removeAsynchronousLoadListener(listener);
    }
}
