/*
 * Copyright (c) 2009-2016 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.bullet.util;

import com.jme3.math.Vector3f;
import com.jme3.scene.mesh.IndexBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dokthar
 */
public class NativeSoftBodyUtilTest {

    public NativeSoftBodyUtilTest() {
    }

    /**
     * Test of generateIndexMap method, of class NativeSoftBodyUtil.
     */
    @Test
    public void testGenerateIndexMap() {
        /*
         * Exemple :
         * mesh :   P1\P3----P4
         *            ¦¯\_   ¦
         *            ¦   ¯\_¦
         *           P2----P6\P5
         *
         * with P1==P3 and P6==P5
         * Buffers indexes        :  | 0| 1| 2| 3| 4| 5|
         * >-  JME PositionBuffer :  [P1,P2,P3,P4,P5,P6] 
         * >-  JME IndexBuffer    :  [ 0, 1, 5, 2, 4, 3]
         * <-> JME -> Bullet map  :  [ 0, 1, 0, 2, 3, 3]
         *  -> Bullet Positions   :  [P3,P2,P4,P6]       == [P1,P2,P4,P5]
         *  -> Bullet Index       :  [ 0, 1, 3, 0, 3, 2]
         */

        FloatBuffer jmePositions = FloatBuffer.wrap(
                new float[]{
                    1f, 0f, 0f, // P1
                    2f, 0f, 0f, // P2
                    1f, 0f, 0f, // P3 == P1
                    4f, 0f, 0f, // P4
                    5f, 0f, 0f, // P5
                    5f, 0f, 0f, // P6 == P5
                });

        IntBuffer jme2bulletIndexMap = NativeSoftBodyUtil.generateIndexMap(jmePositions);

        assertEquals(jmePositions.capacity() / 3, jme2bulletIndexMap.capacity());
        assertEquals(jme2bulletIndexMap.limit(), jme2bulletIndexMap.capacity());

        // bulletPositions should be generated from the mapBulletPositions method on a float buffer (aka position buffer)
        FloatBuffer bulletPositions = FloatBuffer.wrap(new float[]{
            1f, 0f, 0f, // P1 & P3
            2f, 0f, 0f, // P2
            4f, 0f, 0f, // P4
            5f, 0f, 0f, // P5 & P6
        });

        verifyIndexMap(jmePositions, jme2bulletIndexMap, bulletPositions);

        // generate bullet positions from the index mapping, test should still pass
        bulletPositions = NativeSoftBodyUtil.mapBulletPositions(jme2bulletIndexMap, jmePositions);

        verifyIndexMap(jmePositions, jme2bulletIndexMap, bulletPositions);

        // random values with some copy past for similar position
        jmePositions = FloatBuffer.wrap(
                new float[]{
                    0.62f, 0.49f, 0.68f,
                    0.44f, 0.35f, 0.89f,
                    0.30f, 0.28f, 0.80f,
                    0.18f, 0.62f, 0.72f,
                    0.17f, 0.26f, 0.60f,
                    0.65f, 0.58f, 0.31f,
                    0.50f, 0.31f, 0.20f,
                    0.69f, 0.79f, 0.33f,
                    0.75f, 0.16f, 0.49f,
                    0.11f, 0.28f, 0.62f,
                    0.46f, 0.20f, 0.61f,
                    0.12f, 0.86f, 0.41f,
                    0.86f, 0.50f, 0.44f,
                    0.29f, 0.58f, 0.87f,
                    0.73f, 0.90f, 0.38f,
                    0.70f, 0.73f, 0.64f,
                    0.69f, 0.79f, 0.33f,
                    0.75f, 0.16f, 0.49f,
                    0.11f, 0.28f, 0.62f,
                    0.21f, 0.40f, 0.70f,
                    0.73f, 0.58f, 0.92f,
                    0.30f, 0.28f, 0.80f,
                    0.18f, 0.62f, 0.72f,
                    0.17f, 0.26f, 0.60f,
                    0.58f, 0.56f, 0.42f,
                    0.78f, 0.13f, 0.74f}
        );

        jme2bulletIndexMap = NativeSoftBodyUtil.generateIndexMap(jmePositions);
        bulletPositions = NativeSoftBodyUtil.mapBulletPositions(jme2bulletIndexMap, jmePositions);

        verifyIndexMap(jmePositions, jme2bulletIndexMap, bulletPositions);
    }

    private static void verifyIndexMap(FloatBuffer jmePositions, IntBuffer jme2bulletIndexMap, FloatBuffer bulletPositions) {
        Map<Vector3f, Integer> verificationBulletPositions = new HashMap<Vector3f, Integer>();
        for (int i = 0, size = jme2bulletIndexMap.capacity(); i < size; i++) {
            int bulletIndex = jme2bulletIndexMap.get(i);
            Vector3f bulletPosition = new Vector3f(
                    bulletPositions.get(bulletIndex * 3),
                    bulletPositions.get(bulletIndex * 3 + 1),
                    bulletPositions.get(bulletIndex * 3 + 2));
            Vector3f jmePosition = new Vector3f(
                    jmePositions.get(i * 3),
                    jmePositions.get(i * 3 + 1),
                    jmePositions.get(i * 3 + 2));

            // mapped position should be equale
            assertEquals(bulletPosition, jmePosition);

            if (verificationBulletPositions.containsKey(bulletPosition)) {
                if (verificationBulletPositions.get(bulletPosition) != bulletIndex) {
                    fail("error : bulletPosition have to differents indexes for the same position");
                }
            } else {
                verificationBulletPositions.put(bulletPosition, bulletIndex);
            }
        }
    }

    /**
     * Test of mapBulletPositions method, of class NativeSoftBodyUtil.
     */
    @Test
    public void testMapBulletPositions() {
        /*
         * Exemple :
         * mesh :   P1\P3----P4
         *            ¦¯\_   ¦
         *            ¦   ¯\_¦
         *           P2----P6\P5
         *
         * with P1==P3 and P6==P5
         * Buffers indexes        :  | 0| 1| 2| 3| 4| 5|
         * >-  JME PositionBuffer :  [P1,P2,P3,P4,P5,P6] 
         * >-  JME IndexBuffer    :  [ 0, 1, 5, 2, 4, 3]
         * <-> JME -> Bullet map  :  [ 0, 1, 0, 2, 3, 3]
         *  -> Bullet Positions   :  [P3,P2,P4,P6]       == [P1,P2,P4,P5]
         *  -> Bullet Index       :  [ 0, 1, 3, 0, 3, 2]
         */
        FloatBuffer jmePositions = FloatBuffer.wrap(
                new float[]{
                    1f, 0f, 0f, // P1
                    2f, 0f, 0f, // P2
                    1f, 0f, 0f, // P3 == P1
                    4f, 0f, 0f, // P4
                    5f, 0f, 0f, // P5
                    5f, 0f, 0f, // P6 == P5
                });

        IntBuffer jme2bulletIndexMap = IntBuffer.wrap(
                new int[]{0, 1, 0, 2, 3, 3}
        );

        FloatBuffer bulletPositions = NativeSoftBodyUtil.mapBulletPositions(jme2bulletIndexMap, jmePositions);

        // verification with hand filled values 
        verifyIndexMap(jmePositions, jme2bulletIndexMap, bulletPositions);

        jme2bulletIndexMap = NativeSoftBodyUtil.generateIndexMap(jmePositions);
        bulletPositions = NativeSoftBodyUtil.mapBulletPositions(jme2bulletIndexMap, jmePositions);

        // second verification with generated jme2bullet indexMap
        verifyIndexMap(jmePositions, jme2bulletIndexMap, bulletPositions);

    }

    /**
     * Test of mapBulletIndex method, of class NativeSoftBodyUtil.
     */
    @Test
    public void testMapBulletIndex() {
        /* Exemple :
         * mesh :   P1\P3----P4
         *            ¦¯\_   ¦
         *            ¦   ¯\_¦
         *           P2----P6\P5
         *
         * with P1==P3 and P6==P5
         * Buffers indexes        :  | 0| 1| 2| 3| 4| 5|
         * >-  JME PositionBuffer :  [P1,P2,P3,P4,P5,P6] 
         * >-  JME IndexBuffer    :  [ 0, 1, 5, 2, 4, 3]
         * <-> JME -> Bullet map  :  [ 0, 1, 0, 2, 3, 3]
         *  -> Bullet Positions   :  [P3,P2,P4,P6]       == [P1,P2,P4,P5]
         *  -> Bullet Index       :  [ 0, 1, 3, 0, 3, 2]
         */
        IntBuffer jme2bulletIndexMap = IntBuffer.wrap(
                new int[]{0, 1, 0, 2, 3, 3}
        );

        IntBuffer facesIndex = IntBuffer.wrap(
                new int[]{0, 1, 5, 2, 4, 3}
        );

        IntBuffer resultIndex = IntBuffer.wrap(new int[6]);
        NativeSoftBodyUtil.mapBulletIndex(jme2bulletIndexMap, IndexBuffer.wrapIndexBuffer(facesIndex), IndexBuffer.wrapIndexBuffer(resultIndex));

        for (int i = 0, size = facesIndex.capacity(); i < size; i++) {
            assertEquals(resultIndex.get(i), jme2bulletIndexMap.get(facesIndex.get(i)));
        }

    }

}
