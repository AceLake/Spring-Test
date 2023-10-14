/*
 * Copyright (C) 2010 Google Inc.
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

package com.google.gson.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import org.junit.Test;

/**
 * @author Jesse Wilson
 */
public final class TypeTokenTest {
  // These fields are accessed using reflection by the tests below
  List<Integer> listOfInteger = null;
  List<Number> listOfNumber = null;
  List<String> listOfString = null;
  List<?> listOfUnknown = null;
  List<Set<String>> listOfSetOfString = null;
  List<Set<?>> listOfSetOfUnknown = null;

  @SuppressWarnings({"deprecation"})
  @Test
  public void testIsAssignableFromRawTypes() {
    assertTrue(TypeToken.get(Object.class).isAssignableFrom(String.class));
    assertFalse(TypeToken.get(String.class).isAssignableFrom(Object.class));
    assertTrue(TypeToken.get(RandomAccess.class).isAssignableFrom(ArrayList.class));
    assertFalse(TypeToken.get(ArrayList.class).isAssignableFrom(RandomAccess.class));
  }

  @SuppressWarnings({"deprecation"})
  @Test
  public void testIsAssignableFromWithTypeParameters() throws Exception {
    Type a = getClass().getDeclaredField("listOfInteger").getGenericType();
    Type b = getClass().getDeclaredField("listOfNumber").getGenericType();
    assertTrue(TypeToken.get(a).isAssignableFrom(a));
    assertTrue(TypeToken.get(b).isAssignableFrom(b));

    // listOfInteger = listOfNumber; // doesn't compile; must be false
    assertFalse(TypeToken.get(a).isAssignableFrom(b));
    // listOfNumber = listOfInteger; // doesn't compile; must be false
    assertFalse(TypeToken.get(b).isAssignableFrom(a));
  }

  @SuppressWarnings({"deprecation"})
  @Test
  public void testIsAssignableFromWithBasicWildcards() throws Exception {
    Type a = getClass().getDeclaredField("listOfString").getGenericType();
    Type b = getClass().getDeclaredField("listOfUnknown").getGenericType();
    assertTrue(TypeToken.get(a).isAssignableFrom(a));
    assertTrue(TypeToken.get(b).isAssignableFrom(b));

    // listOfString = listOfUnknown  // doesn't compile; must be false
    assertFalse(TypeToken.get(a).isAssignableFrom(b));
    listOfUnknown = listOfString; // compiles; must be true
    // The following assertion is too difficult to support reliably, so disabling
    // assertTrue(TypeToken.get(b).isAssignableFrom(a));
  }

  @SuppressWarnings({"deprecation"})
  @Test
  public void testIsAssignableFromWithNestedWildcards() throws Exception {
    Type a = getClass().getDeclaredField("listOfSetOfString").getGenericType();
    Type b = getClass().getDeclaredField("listOfSetOfUnknown").getGenericType();
    assertTrue(TypeToken.get(a).isAssignableFrom(a));
    assertTrue(TypeToken.get(b).isAssignableFrom(b));

    // listOfSetOfString = listOfSetOfUnknown; // doesn't compile; must be false
    assertFalse(TypeToken.get(a).isAssignableFrom(b));
    // listOfSetOfUnknown = listOfSetOfString; // doesn't compile; must be false
    assertFalse(TypeToken.get(b).isAssignableFrom(a));
  }

  @Test
  public void testArrayFactory() {
    TypeToken<?> expectedStringArray = new TypeToken<String[]>() {};
    assertEquals(expectedStringArray, TypeToken.getArray(String.class));

    TypeToken<?> expectedListOfStringArray = new TypeToken<List<String>[]>() {};
    Type listOfString = new TypeToken<List<String>>() {}.getType();
    assertEquals(expectedListOfStringArray, TypeToken.getArray(listOfString));

    try {
      TypeToken.getArray(null);
      fail();
    } catch (NullPointerException e) {
    }
  }

  @Test
  public void testParameterizedFactory() {
    TypeToken<?> expectedListOfString = new TypeToken<List<String>>() {};
    assertEquals(expectedListOfString, TypeToken.getParameterized(List.class, String.class));

    TypeToken<?> expectedMapOfStringToString = new TypeToken<Map<String, String>>() {};
    assertEquals(expectedMapOfStringToString, TypeToken.getParameterized(Map.class, String.class, String.class));

    TypeToken<?> expectedListOfListOfListOfString = new TypeToken<List<List<List<String>>>>() {};
    Type listOfString = TypeToken.getParameterized(List.class, String.class).getType();
    Type listOfListOfString = TypeToken.getParameterized(List.class, listOfString).getType();
    assertEquals(expectedListOfListOfListOfString, TypeToken.getParameterized(List.class, listOfListOfString));

    TypeToken<?> expectedWithExactArg = new TypeToken<GenericWithBound<Number>>() {};
    assertEquals(expectedWithExactArg, TypeToken.getParameterized(GenericWithBound.class, Number.class));

    TypeToken<?> expectedWithSubclassArg = new TypeToken<GenericWithBound<Integer>>() {};
    assertEquals(expectedWithSubclassArg, TypeToken.getParameterized(GenericWithBound.class, Integer.class));

    TypeToken<?> expectedSatisfyingTwoBounds = new TypeToken<GenericWithMultiBound<ClassSatisfyingBounds>>() {};
    assertEquals(expectedSatisfyingTwoBounds, TypeToken.getParameterized(GenericWithMultiBound.class, ClassSatisfyingBounds.class));
  }

  @Test
  public void testParameterizedFactory_Invalid() {
    try {
      TypeToken.getParameterized(null, new Type[0]);
      fail();
    } catch (NullPointerException e) {
    }

    GenericArrayType arrayType = (GenericArrayType) TypeToken.getArray(String.class).getType();
    try {
      TypeToken.getParameterized(arrayType, new Type[0]);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("rawType must be of type Class, but was java.lang.String[]", e.getMessage());
    }

    try {
      TypeToken.getParameterized(String.class, String.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("java.lang.String requires 0 type arguments, but got 1", e.getMessage());
    }

    try {
      TypeToken.getParameterized(List.class, new Type[0]);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("java.util.List requires 1 type arguments, but got 0", e.getMessage());
    }

    try {
      TypeToken.getParameterized(List.class, String.class, String.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("java.util.List requires 1 type arguments, but got 2", e.getMessage());
    }

    try {
      TypeToken.getParameterized(GenericWithBound.class, String.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Type argument class java.lang.String does not satisfy bounds "
          + "for type variable T declared by " + GenericWithBound.class,
          e.getMessage());
    }

    try {
      TypeToken.getParameterized(GenericWithBound.class, Object.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Type argument class java.lang.Object does not satisfy bounds "
          + "for type variable T declared by " + GenericWithBound.class,
          e.getMessage());
    }

    try {
      TypeToken.getParameterized(GenericWithMultiBound.class, Number.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Type argument class java.lang.Number does not satisfy bounds "
          + "for type variable T declared by " + GenericWithMultiBound.class,
          e.getMessage());
    }

    try {
      TypeToken.getParameterized(GenericWithMultiBound.class, CharSequence.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Type argument interface java.lang.CharSequence does not satisfy bounds "
          + "for type variable T declared by " + GenericWithMultiBound.class,
          e.getMessage());
    }

    try {
      TypeToken.getParameterized(GenericWithMultiBound.class, Object.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Type argument class java.lang.Object does not satisfy bounds "
          + "for type variable T declared by " + GenericWithMultiBound.class,
          e.getMessage());
    }
  }

  private static class CustomTypeToken extends TypeToken<String> {
  }

  @Test
  public void testTypeTokenNonAnonymousSubclass() {
    TypeToken<?> typeToken = new CustomTypeToken();
    assertEquals(String.class, typeToken.getRawType());
    assertEquals(String.class, typeToken.getType());
  }

  /**
   * User must only create direct subclasses of TypeToken, but not subclasses
   * of subclasses (...) of TypeToken.
   */
  @Test
  public void testTypeTokenSubSubClass() {
    class SubTypeToken<T> extends TypeToken<String> {}
    class SubSubTypeToken1<T> extends SubTypeToken<T> {}
    class SubSubTypeToken2 extends SubTypeToken<Integer> {}

    try {
      new SubTypeToken<Integer>() {};
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Must only create direct subclasses of TypeToken", expected.getMessage());
    }

    try {
      new SubSubTypeToken1<Integer>();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Must only create direct subclasses of TypeToken", expected.getMessage());
    }

    try {
      new SubSubTypeToken2();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Must only create direct subclasses of TypeToken", expected.getMessage());
    }
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void testTypeTokenRaw() {
    try {
      new TypeToken() {};
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("TypeToken must be created with a type argument: new TypeToken<...>() {}; "
          + "When using code shrinkers (ProGuard, R8, ...) make sure that generic signatures are preserved.",
          expected.getMessage());
    }
  }
}

// Have to declare these classes here as top-level classes because otherwise tests for
// TypeToken.getParameterized fail due to owner type mismatch
class GenericWithBound<T extends Number> {
}
class GenericWithMultiBound<T extends Number & CharSequence> {
}
@SuppressWarnings("serial")
abstract class ClassSatisfyingBounds extends Number implements CharSequence {
}
