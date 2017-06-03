/*
 * For work developed by the HSQL Development Group:
 *
 * Copyright (c) 2001-2011, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 *
 * For work originally developed by the Hypersonic SQL Group:
 *
 * Copyright (c) 1995-2000, The Hypersonic SQL Group.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Hypersonic SQL Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HYPERSONIC SQL GROUP,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Hypersonic SQL Group.
 */


package org.hsqldb.index;

import org.hsqldb.Row;
import org.hsqldb.RowSBT;
import org.hsqldb.RowSBTDisk;
import org.hsqldb.lib.LongLookup;
import org.hsqldb.persist.CachedObject;
import org.hsqldb.persist.PersistentStore;
import org.hsqldb.rowio.RowOutputInterface;
import org.hsqldb.rowio.RowInputInterface;

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// fredt@users 20020920 - path 1.7.1 - refactoring to cut mamory footprint
// fredt@users 20021205 - path 1.7.2 - enhancements
// fredt@users 20021215 - doc 1.7.2 - javadoc comments

/**
 *  The parent for all SBT node implementations. Subclasses of Node vary
 *  in the way they hold
 *  references to other Nodes in the SBT tree, or to their Row data.<br>
 *
 *  nNext links the Node objects belonging to different indexes for each
 *  table row. It is used solely by Row to locate the node belonging to a
 *  particular index.<br>
 *
 *  New class derived from Hypersonic SQL code and enhanced in HSQLDB. <p>
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.9.0
 * @since Hypersonic SQL
 */

public class NodeSBT implements CachedObject {

    static final int NO_POS = RowSBTDisk.NO_POS;
    // ibalance here is related to size
    public int       iBalance;
    public NodeSBT   nNext;    // node of next index (nNext==null || nNext.iId=iId+1)

    //
    protected NodeSBT   nLeft;
    protected NodeSBT   nRight;
    protected NodeSBT   nParent;
    protected final Row row;

    NodeSBT() {
        row = null;
        iBalance = 0;
        nLeft    = nRight = nParent = null;
    }

    public NodeSBT(Row r) {
        row = r;
        iBalance = 0;
        nLeft    = nRight = nParent = null;
    }

    public void delete() {
        iBalance = 0;
        nLeft    = nRight = nParent = null;
    }

    NodeSBT getLeft(PersistentStore store) {
        return nLeft;
    }

    NodeSBT setLeft(PersistentStore persistentStore, NodeSBT n) {

        nLeft = n;

        return this;
    }

    public int getBalance(PersistentStore store) {
        return iBalance;
    }

    boolean isLeft(NodeSBT node) {
        return nLeft == node;
    }

    boolean isRight(NodeSBT node) {
        return nRight == node;
    }

    NodeSBT getRight(PersistentStore persistentStore) {
        return nRight;
    }

    NodeSBT setRight(PersistentStore persistentStore, NodeSBT n) {

        nRight = n;

        return this;
    }

    NodeSBT getParent(PersistentStore store) {
        return nParent;
    }

    boolean isRoot(PersistentStore store) {
        return nParent == null;
    }

    NodeSBT setParent(PersistentStore persistentStore, NodeSBT n) {

        nParent = n;

        return this;
    }

    public NodeSBT setBalance(PersistentStore store, int b) {

        iBalance = b;

        return this;
    }

    boolean isFromLeft(PersistentStore store) {

        if (nParent == null) {
            return true;
        }

        return this == nParent.nLeft;
    }

    public NodeSBT child(PersistentStore store, boolean isleft) {
        return isleft ? getLeft(store)
                      : getRight(store);
    }

    public NodeSBT set(PersistentStore store, boolean isLeft, NodeSBT n) {

        if (isLeft) {
            nLeft = n;
        } else {
            nRight = n;
        }

        if (n != null) {
            n.nParent = this;
        }

        return this;
    }

    public void replace(PersistentStore store, Index index, NodeSBT n) {

        if (nParent == null) {
            if (n != null) {
                n = n.setParent(store, null);
            }

            store.setAccessor(index, n);
        } else {
            nParent.set(store, isFromLeft(store), n);
        }
    }

    boolean equals(NodeSBT n) {
        return n == this;
    }

    public void setInMemory(boolean in) {}

    public int getDefaultCapacity() {
        return 0;
    }

    public void read(RowInputInterface in) {}

    public void write(RowOutputInterface out) {}

    public void write(RowOutputInterface out, LongLookup lookup) {}

    public long getPos() {
        return 0;
    }

    public RowSBT getRow(PersistentStore store) {
        return (RowSBT) row;
    }

    protected Object[] getData(PersistentStore store) {
        return row.getData();
    }

    public void updateAccessCount(int count) {}

    public int getAccessCount() {
        return 0;
    }

    public void setStorageSize(int size) {}

    public int getStorageSize() {
        return 0;
    }

    final public boolean isBlock() {
        return false;
    }

    public void setPos(long pos) {}

    public boolean isNew() {
        return false;
    }

    public boolean hasChanged() {
        return false;
    }

    public boolean isKeepInMemory() {
        return false;
    }

    public boolean keepInMemory(boolean keep) {
        return true;
    }

    public boolean isInMemory() {
        return false;
    }

    public void restore() {}

    public void destroy() {}

    public int getRealSize(RowOutputInterface out) {
        return 0;
    }

    public boolean isMemory() {
        return true;
    }
}