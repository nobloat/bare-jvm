package org.nobloat.bare;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AggregateBareDecoder extends PrimitiveBareDecoder {

    public static final List<String> INTEGER_TYPES = List.of(new String[]{"java.lang.Long", "java.lang.Integer", "java.lang.BigInteger", "java.lang.Short"});
    public static final List<String> PRIMITIVE_TYPES = List.of(new String[]{"java.lang.String", "java.lang.Boolean", "java.lang.Byte", "java.lang.Float", "java.lang.Double"});

    public AggregateBareDecoder(InputStream inputStream) {
        super(inputStream);
    }


}
