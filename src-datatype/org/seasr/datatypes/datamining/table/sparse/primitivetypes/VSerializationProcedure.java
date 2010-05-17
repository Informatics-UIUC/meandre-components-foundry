/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * All rights reserved.
 *
 * Developed by:
 *
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */

package org.seasr.datatypes.datamining.table.sparse.primitivetypes;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * vered - May 18 - this class is exactly like SerializationProcedure from
 * package gnu.trove with the different that it implements only VIntBooleanProcedure
 * and VIntShortProcedure .
 * this is an ugly (but quick fix and working) solution to the problem that
 * SerializationProcedure is not public, therefore cannot be accessed from this
 * package.
 * this is done so as to support de-serialization of VIntBooleanHashMap
 * and VIntShortHashMap.
 *
 *
 * Implementation of the variously typed procedure interfaces that supports
 * writing the arguments to the procedure out on an ObjectOutputStream.
 * In the case of two-argument procedures, the arguments are written out
 * in the order received.
 *
 * <p>
 * Any IOException is trapped here so that it can be rethrown in a writeObject
 * method.
 * </p>
 *
 * Created: Sun Jul  7 00:14:18 2002
 *
 * @author Eric D. Friedman
 * @version $Id: VSerializationProcedure.java,v 1.3 2004/07/26 15:45:07 vered Exp $
 */

class VSerializationProcedure implements

   VIntBooleanProcedure,
   VIntShortProcedure,
VIntCharProcedure{

    private final ObjectOutputStream stream;
    IOException exception;

    VSerializationProcedure (ObjectOutputStream stream) {
        this.stream = stream;
    }

    public boolean execute(int val) {
        try {
            stream.writeInt(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

  /*  public boolean execute(double val) {
        try {
            stream.writeDouble(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(long val) {
        try {
            stream.writeLong(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(float val) {
        try {
            stream.writeFloat(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(Object val) {
        try {
            stream.writeObject(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(Object key, Object val) {
        try {
            stream.writeObject(key);
            stream.writeObject(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(Object key, int val) {
        try {
            stream.writeObject(key);
            stream.writeInt(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(Object key, long val) {
        try {
            stream.writeObject(key);
            stream.writeLong(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(Object key, double val) {
        try {
            stream.writeObject(key);
            stream.writeDouble(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(Object key, float val) {
        try {
            stream.writeObject(key);
            stream.writeFloat(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(int key, Object val) {
        try {
            stream.writeInt(key);
            stream.writeObject(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(int key, int val) {
        try {
            stream.writeInt(key);
            stream.writeInt(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(int key, long val) {
        try {
            stream.writeInt(key);
            stream.writeLong(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(int key, double val) {
        try {
            stream.writeInt(key);
            stream.writeDouble(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(int key, float val) {
        try {
            stream.writeInt(key);
            stream.writeFloat(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }*/

    public boolean execute(int key, boolean val) {
        try {
            stream.writeInt(key);
            stream.writeBoolean(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(int key, short val) {
       try {
           stream.writeInt(key);
           stream.writeShort(val);
       } catch (IOException e) {
           this.exception = e;
           return false;
       }
       return true;
   }




   public boolean execute(int key, char val) {
       try {
           stream.writeInt(key);
           stream.writeChar(val);
       } catch (IOException e) {
           this.exception = e;
           return false;
       }
       return true;
   }


/*
    public boolean execute(long key, Object val) {
        try {
            stream.writeLong(key);
            stream.writeObject(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(long key, int val) {
        try {
            stream.writeLong(key);
            stream.writeInt(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(long key, long val) {
        try {
            stream.writeLong(key);
            stream.writeLong(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(long key, double val) {
        try {
            stream.writeLong(key);
            stream.writeDouble(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(long key, float val) {
        try {
            stream.writeLong(key);
            stream.writeFloat(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(double key, Object val) {
        try {
            stream.writeDouble(key);
            stream.writeObject(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(double key, int val) {
        try {
            stream.writeDouble(key);
            stream.writeInt(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(double key, long val) {
        try {
            stream.writeDouble(key);
            stream.writeLong(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(double key, double val) {
        try {
            stream.writeDouble(key);
            stream.writeDouble(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(double key, float val) {
        try {
            stream.writeDouble(key);
            stream.writeFloat(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(float key, Object val) {
        try {
            stream.writeFloat(key);
            stream.writeObject(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(float key, int val) {
        try {
            stream.writeFloat(key);
            stream.writeInt(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(float key, long val) {
        try {
            stream.writeFloat(key);
            stream.writeLong(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(float key, double val) {
        try {
            stream.writeFloat(key);
            stream.writeDouble(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }

    public boolean execute(float key, float val) {
        try {
            stream.writeFloat(key);
            stream.writeFloat(val);
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
        return true;
    }*/
}// VSerializationProcedure
