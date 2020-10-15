package org.nobloat.bare;

import java.io.OutputStream;

public class AggregateBareEncoder extends PrimitiveBareEncoder {
    public AggregateBareEncoder(OutputStream os, boolean verifyInput) {
        super(os, verifyInput);
    }

    public AggregateBareEncoder(OutputStream os) {
        super(os);
    }
}
