/*
 * Copyright (C) 2021 The Authors of CEL-Java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.projectnessie.cel.common.types;

import static org.projectnessie.cel.common.types.BoolT.boolOf;
import static org.projectnessie.cel.common.types.Err.newTypeConversionError;
import static org.projectnessie.cel.common.types.Err.noSuchOverload;
import static org.projectnessie.cel.common.types.Err.rangeError;
import static org.projectnessie.cel.common.types.IntT.IntType;
import static org.projectnessie.cel.common.types.IntT.intOf;
import static org.projectnessie.cel.common.types.StringT.StringType;
import static org.projectnessie.cel.common.types.StringT.stringOf;
import static org.projectnessie.cel.common.types.TypeValue.TypeType;
import static org.projectnessie.cel.common.types.UintT.UintType;
import static org.projectnessie.cel.common.types.UintT.uintOf;

import com.google.protobuf.Any;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FloatValue;
import java.util.Objects;
import org.projectnessie.cel.common.types.ref.BaseVal;
import org.projectnessie.cel.common.types.ref.Type;
import org.projectnessie.cel.common.types.ref.Val;
import org.projectnessie.cel.common.types.traits.Adder;
import org.projectnessie.cel.common.types.traits.Comparer;
import org.projectnessie.cel.common.types.traits.Divider;
import org.projectnessie.cel.common.types.traits.Multiplier;
import org.projectnessie.cel.common.types.traits.Negater;
import org.projectnessie.cel.common.types.traits.Subtractor;
import org.projectnessie.cel.common.types.traits.Trait;

/** Double type that implements ref.Val, comparison, and mathematical operations. */
public final class DoubleT extends BaseVal
    implements Adder, Comparer, Divider, Multiplier, Negater, Subtractor {
  /** DoubleType singleton. */
  public static final TypeValue DoubleType =
      TypeValue.newTypeValue(
          "double",
          Trait.AdderType,
          Trait.ComparerType,
          Trait.DividerType,
          Trait.MultiplierType,
          Trait.NegatorType,
          Trait.SubtractorType);

  private final double d;

  private DoubleT(double d) {
    this.d = d;
  }

  public static DoubleT doubleOf(double d) {
    return new DoubleT(d);
  }

  /** Add implements traits.Adder.Add. */
  @Override
  public Val add(Val other) {
    if (!(other instanceof DoubleT)) {
      return noSuchOverload(this, "add", other);
    }
    return doubleOf(d + ((DoubleT) other).d);
  }

  /** Compare implements traits.Comparer.Compare. */
  @Override
  public Val compare(Val other) {
    if (!(other instanceof DoubleT)) {
      return noSuchOverload(this, "compare", other);
    }
    return intOf(Double.compare(d, ((DoubleT) other).d));
  }

  /** ConvertToNative implements ref.Val.ConvertToNative. */
  @SuppressWarnings("unchecked")
  @Override
  public <T> T convertToNative(Class<T> typeDesc) {
    if (typeDesc == Double.class || typeDesc == double.class || typeDesc == Object.class) {
      return (T) Double.valueOf(d);
    }
    if (typeDesc == Float.class || typeDesc == float.class) {
      // TODO needs overflow check
      return (T) Float.valueOf((float) d);
    }
    if (typeDesc == Any.class) {
      return (T) Any.pack(DoubleValue.of(d));
    }
    if (typeDesc == DoubleValue.class) {
      return (T) DoubleValue.of(d);
    }
    if (typeDesc == FloatValue.class) {
      // TODO needs overflow check
      return (T) FloatValue.of((float) d);
    }
    if (typeDesc == Val.class || typeDesc == DoubleT.class) {
      return (T) this;
    }

    //		switch typeDesc.Kind() {
    //		case reflect.Ptr:
    //			switch typeDesc {
    //			case jsonValueType:
    //				// Note, there are special cases for proto3 to json conversion that
    //				// expect the floating point value to be converted to a NaN,
    //				// Infinity, or -Infinity string values, but the jsonpb string
    //				// marshaling of the protobuf.Value will handle this conversion.
    //				return structpb.NewNumberValue(float64(d)), nil
    //			}
    //			switch typeDesc.Elem().Kind() {
    //			case reflect.Float32:
    //				v := float32(d)
    //				p := reflect.New(typeDesc.Elem())
    //				p.Elem().Set(reflect.ValueOf(v).Convert(typeDesc.Elem()))
    //				return p.Interface(), nil
    //			case reflect.Float64:
    //				v := float64(d)
    //				p := reflect.New(typeDesc.Elem())
    //				p.Elem().Set(reflect.ValueOf(v).Convert(typeDesc.Elem()))
    //				return p.Interface(), nil
    //			}
    //		case reflect.Interface:
    //			dv := d.Value()
    //			if reflect.TypeOf(dv).Implements(typeDesc) {
    //				return dv, nil
    //			}
    //			if reflect.TypeOf(d).Implements(typeDesc) {
    //				return d, nil
    //			}
    //		}
    throw new RuntimeException(
        String.format(
            "native type conversion error from '%s' to '%s'", DoubleType, typeDesc.getName()));
  }

  /** ConvertToType implements ref.Val.ConvertToType. */
  @Override
  public Val convertToType(Type typeValue) {
    if (typeValue == IntType) {
      long r = Math.round(d);
      if (r == Long.MIN_VALUE || r == Long.MAX_VALUE) {
        return rangeError(d, "int");
      }
      return intOf(r);
    }
    if (typeValue == UintType) {
      long r = Math.round(d);
      if (r < 0 || r == Long.MAX_VALUE) {
        return rangeError(d, "int");
      }
      return uintOf(r);
    }
    if (typeValue == DoubleType) {
      return this;
    }
    if (typeValue == StringType) {
      return stringOf(Double.toString(d));
    }
    if (typeValue == TypeType) {
      return DoubleType;
    }
    return newTypeConversionError(DoubleType, typeValue);
  }

  /** Divide implements traits.Divider.Divide. */
  @Override
  public Val divide(Val other) {
    if (!(other instanceof DoubleT)) {
      return noSuchOverload(this, "divide", other);
    }
    return doubleOf(d / ((DoubleT) other).d);
  }

  /** Equal implements ref.Val.Equal. */
  @Override
  public Val equal(Val other) {
    if (!(other instanceof DoubleT)) {
      return noSuchOverload(this, "equal", other);
    }
    /** TODO: Handle NaNs properly. */
    return boolOf(d == ((DoubleT) other).d);
  }

  /** Multiply implements traits.Multiplier.Multiply. */
  @Override
  public Val multiply(Val other) {
    if (!(other instanceof DoubleT)) {
      return noSuchOverload(this, "multiply", other);
    }
    return doubleOf(d * ((DoubleT) other).d);
  }

  /** Negate implements traits.Negater.Negate. */
  @Override
  public Val negate() {
    return doubleOf(-d);
  }

  /** Subtract implements traits.Subtractor.Subtract. */
  @Override
  public Val subtract(Val other) {
    if (!(other instanceof DoubleT)) {
      return noSuchOverload(this, "subtract", other);
    }
    return doubleOf(d - ((DoubleT) other).d);
  }

  /** Type implements ref.Val.Type. */
  @Override
  public Type type() {
    return DoubleType;
  }

  /** Value implements ref.Val.Value. */
  @Override
  public Object value() {
    return d;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DoubleT doubleT = (DoubleT) o;
    return Double.compare(doubleT.d, d) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), d);
  }
}