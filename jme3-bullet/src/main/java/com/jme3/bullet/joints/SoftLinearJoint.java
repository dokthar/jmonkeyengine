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
package com.jme3.bullet.joints;

import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.objects.PhysicsSoftBody;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Vector3f;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dokthar
 */
public class SoftLinearJoint extends SoftPhysicsJoint {

    protected Vector3f position;

    protected SoftLinearJoint() {
    }

    /**
     * Create a new linear joint for soft-rigid bodies. The softBody
     * {@code nodeA} must contain a cluster.
     *
     * @param position
     * @param nodeA
     * @param nodeB
     * @param pivotA
     * @param pivotB
     */
    public SoftLinearJoint(Vector3f position, PhysicsSoftBody nodeA, PhysicsRigidBody nodeB, Vector3f pivotA, Vector3f pivotB) {
        super(nodeA, nodeB, pivotA, pivotB);
        this.position = position;
        createJoint();
    }

    /**
     * Create a new linear joint for soft-soft bodies. The two soft bodies
     * {@code nodeA} and {@code nodeB} must contain a cluster.
     *
     * @param position
     * @param nodeA
     * @param nodeB
     * @param pivotA
     * @param pivotB
     */
    public SoftLinearJoint(Vector3f position, PhysicsSoftBody nodeA, PhysicsSoftBody nodeB, Vector3f pivotA, Vector3f pivotB) {
        super(nodeA, nodeB, pivotA, pivotB);
        this.position = position;
        createJoint();
    }

    public void setPosition(Vector3f position) {
        this.position = position;
        setPosition(objectId, position);
    }

    private native void setPosition(long jointId, Vector3f position);

    public Vector3f getPosition() {
        return position;
    }

    private void createJoint() {
        if (isSoftRigidJoint()) {
            objectId = createJointSoftRigid(softA.getObjectId(), nodeB.getObjectId(), pivotA, pivotB,
                    errorReductionParameter, constraintForceMixing, split, position);
        } else {
            objectId = createJointSoftSoft(softA.getObjectId(), softB.getObjectId(), pivotA, pivotB,
                    errorReductionParameter, constraintForceMixing, split, position);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Created Joint {0}", Long.toHexString(objectId));
    }

    private native long createJointSoftRigid(long objectIdA, long objectIdB, Vector3f pivotA, Vector3f pivotB, float erp, float cfm, float split, Vector3f position);

    private native long createJointSoftSoft(long objectIdA, long objectIdB, Vector3f pivotA, Vector3f pivotB, float erp, float cfm, float split, Vector3f position);

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule capsule = ex.getCapsule(this);

        capsule.write(position, "position", Vector3f.ZERO);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule capsule = im.getCapsule(this);

        this.position = (Vector3f) capsule.readSavable("position", Vector3f.ZERO);

        createJoint();
    }

}
