/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.gson.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;
import com.google.gson.common.MoreAsserts;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional tests for Java 5.0 enums.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class EnumTest {

  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testTopLevelEnumSerialization() throws Exception {
    String result = gson.toJson(MyEnum.VALUE1);
    assertEquals('"' + MyEnum.VALUE1.toString() + '"', result);
  }

  @Test
  public void testTopLevelEnumDeserialization() throws Exception {
    MyEnum result = gson.fromJson('"' + MyEnum.VALUE1.toString() + '"', MyEnum.class);
    assertEquals(MyEnum.VALUE1, result);
  }

  @Test
  public void testCollectionOfEnumsSerialization() {
    Type type = new TypeToken<Collection<MyEnum>>() {}.getType();
    Collection<MyEnum> target = new ArrayList<>();
    target.add(MyEnum.VALUE1);
    target.add(MyEnum.VALUE2);
    String expectedJson = "[\"VALUE1\",\"VALUE2\"]";
    String actualJson = gson.toJson(target);
    assertEquals(expectedJson, actualJson);
    actualJson = gson.toJson(target, type);
    assertEquals(expectedJson, actualJson);
  }

  @Test
  public void testCollectionOfEnumsDeserialization() {
    Type type = new TypeToken<Collection<MyEnum>>() {}.getType();
    String json = "[\"VALUE1\",\"VALUE2\"]";
    Collection<MyEnum> target = gson.fromJson(json, type);
    MoreAsserts.assertContains(target, MyEnum.VALUE1);
    MoreAsserts.assertContains(target, MyEnum.VALUE2);
  }

  @Test
  public void testClassWithEnumFieldSerialization() throws Exception {
    ClassWithEnumFields target = new ClassWithEnumFields();
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  @Test
  public void testClassWithEnumFieldDeserialization() throws Exception {
    String json = "{value1:'VALUE1',value2:'VALUE2'}";
    ClassWithEnumFields target = gson.fromJson(json, ClassWithEnumFields.class);
    assertEquals(MyEnum.VALUE1,target.value1);
    assertEquals(MyEnum.VALUE2,target.value2);
  }

  private static enum MyEnum {
    VALUE1, VALUE2
  }

  private static class ClassWithEnumFields {
    private final MyEnum value1 = MyEnum.VALUE1;
    private final MyEnum value2 = MyEnum.VALUE2;
    public String getExpectedJson() {
      return "{\"value1\":\"" + value1 + "\",\"value2\":\"" + value2 + "\"}";
    }
  }

  /**
   * Test for issue 226.
   */
  @Test
  public void testEnumSubclass() {
    assertFalse(Roshambo.class == Roshambo.ROCK.getClass());
    assertEquals("\"ROCK\"", gson.toJson(Roshambo.ROCK));
    assertEquals("[\"ROCK\",\"PAPER\",\"SCISSORS\"]", gson.toJson(EnumSet.allOf(Roshambo.class)));
    assertEquals(Roshambo.ROCK, gson.fromJson("\"ROCK\"", Roshambo.class));
    assertEquals(EnumSet.allOf(Roshambo.class),
        gson.fromJson("[\"ROCK\",\"PAPER\",\"SCISSORS\"]", new TypeToken<Set<Roshambo>>() {}.getType()));
  }

  @Test
  public void testEnumSubclassWithRegisteredTypeAdapter() {
    gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Roshambo.class, new MyEnumTypeAdapter())
        .create();
    assertFalse(Roshambo.class == Roshambo.ROCK.getClass());
    assertEquals("\"123ROCK\"", gson.toJson(Roshambo.ROCK));
    assertEquals("[\"123ROCK\",\"123PAPER\",\"123SCISSORS\"]", gson.toJson(EnumSet.allOf(Roshambo.class)));
    assertEquals(Roshambo.ROCK, gson.fromJson("\"123ROCK\"", Roshambo.class));
    assertEquals(EnumSet.allOf(Roshambo.class),
        gson.fromJson("[\"123ROCK\",\"123PAPER\",\"123SCISSORS\"]", new TypeToken<Set<Roshambo>>() {}.getType()));
  }

  @Test
  public void testEnumSubclassAsParameterizedType() {
    Collection<Roshambo> list = new ArrayList<>();
    list.add(Roshambo.ROCK);
    list.add(Roshambo.PAPER);

    String json = gson.toJson(list);
    assertEquals("[\"ROCK\",\"PAPER\"]", json);

    Type collectionType = new TypeToken<Collection<Roshambo>>() {}.getType();
    Collection<Roshambo> actualJsonList = gson.fromJson(json, collectionType);
    MoreAsserts.assertContains(actualJsonList, Roshambo.ROCK);
    MoreAsserts.assertContains(actualJsonList, Roshambo.PAPER);
  }

  @Test
  public void testEnumCaseMapping() {
    assertEquals(Gender.MALE, gson.fromJson("\"boy\"", Gender.class));
    assertEquals("\"boy\"", gson.toJson(Gender.MALE, Gender.class));
  }

  @Test
  public void testEnumSet() {
    EnumSet<Roshambo> foo = EnumSet.of(Roshambo.ROCK, Roshambo.PAPER);
    String json = gson.toJson(foo);
    assertEquals("[\"ROCK\",\"PAPER\"]", json);

    Type type = new TypeToken<EnumSet<Roshambo>>() {}.getType();
    EnumSet<Roshambo> bar = gson.fromJson(json, type);
    assertTrue(bar.contains(Roshambo.ROCK));
    assertTrue(bar.contains(Roshambo.PAPER));
    assertFalse(bar.contains(Roshambo.SCISSORS));
  }

  @Test
  public void testEnumMap() throws Exception {
    EnumMap<MyEnum, String> map = new EnumMap<>(MyEnum.class);
    map.put(MyEnum.VALUE1, "test");
    String json = gson.toJson(map);
    assertEquals("{\"VALUE1\":\"test\"}", json);

    Type type = new TypeToken<EnumMap<MyEnum, String>>() {}.getType();
    EnumMap<?, ?> actualMap = gson.fromJson("{\"VALUE1\":\"test\"}", type);
    Map<?, ?> expectedMap = Collections.singletonMap(MyEnum.VALUE1, "test");
    assertEquals(expectedMap, actualMap);
  }

  private enum Roshambo {
    ROCK {
      @Override Roshambo defeats() {
        return SCISSORS;
      }
    },
    PAPER {
      @Override Roshambo defeats() {
        return ROCK;
      }
    },
    SCISSORS {
      @Override Roshambo defeats() {
        return PAPER;
      }
    };

    abstract Roshambo defeats();
  }

  private static class MyEnumTypeAdapter
      implements JsonSerializer<Roshambo>, JsonDeserializer<Roshambo> {
    @Override public JsonElement serialize(Roshambo src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive("123" + src.name());
    }

    @Override public Roshambo deserialize(JsonElement json, Type classOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return Roshambo.valueOf(json.getAsString().substring(3));
    }
  }

  private enum Gender {
    @SerializedName("boy")
    MALE,

    @SerializedName("girl")
    FEMALE
  }

  @Test
  public void testEnumClassWithFields() {
    assertEquals("\"RED\"", gson.toJson(Color.RED));
    assertEquals("red", gson.fromJson("RED", Color.class).value);
    assertEquals(2, gson.fromJson("BLUE", Color.class).index);
  }

  private enum Color {
    RED("red", 1), BLUE("blue", 2), GREEN("green", 3);
    String value;
    int index;
    private Color(String value, int index) {
      this.value = value;
      this.index = index;
    }
  }

  @Test
  public void testEnumToStringRead() {
    // Should still be able to read constant name
    assertEquals(CustomToString.A, gson.fromJson("\"A\"", CustomToString.class));
    // Should be able to read toString() value
    assertEquals(CustomToString.A, gson.fromJson("\"test\"", CustomToString.class));

    assertNull(gson.fromJson("\"other\"", CustomToString.class));
  }

  private enum CustomToString {
    A;

    @Override
    public String toString() {
      return "test";
    }
  }

  /**
   * Test that enum constant names have higher precedence than {@code toString()}
   * result.
   */
  @Test
  public void testEnumToStringReadInterchanged() {
    assertEquals(InterchangedToString.A, gson.fromJson("\"A\"", InterchangedToString.class));
    assertEquals(InterchangedToString.B, gson.fromJson("\"B\"", InterchangedToString.class));
  }

  private enum InterchangedToString {
    A("B"),
    B("A");

    private final String toString;

    InterchangedToString(String toString) {
      this.toString = toString;
    }

    @Override
    public String toString() {
      return toString;
    }
  }
}
