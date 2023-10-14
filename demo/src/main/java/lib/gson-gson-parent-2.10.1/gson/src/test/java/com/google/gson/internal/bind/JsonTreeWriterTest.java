/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal.bind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.common.MoreAsserts;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

@SuppressWarnings("resource")
public final class JsonTreeWriterTest {
  @Test
  public void testArray() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.value(1);
    writer.value(2);
    writer.value(3);
    writer.endArray();
    assertEquals("[1,2,3]", writer.get().toString());
  }

  @Test
  public void testNestedArray() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.beginArray();
    writer.endArray();
    writer.beginArray();
    writer.beginArray();
    writer.endArray();
    writer.endArray();
    writer.endArray();
    assertEquals("[[],[[]]]", writer.get().toString());
  }

  @Test
  public void testObject() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.name("A").value(1);
    writer.name("B").value(2);
    writer.endObject();
    assertEquals("{\"A\":1,\"B\":2}", writer.get().toString());
  }

  @Test
  public void testNestedObject() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.name("A");
    writer.beginObject();
    writer.name("B");
    writer.beginObject();
    writer.endObject();
    writer.endObject();
    writer.name("C");
    writer.beginObject();
    writer.endObject();
    writer.endObject();
    assertEquals("{\"A\":{\"B\":{}},\"C\":{}}", writer.get().toString());
  }

  @Test
  public void testWriteAfterClose() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(true);
    writer.beginArray();
    writer.value("A");
    writer.endArray();
    writer.close();
    try {
      writer.beginArray();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testPrematureClose() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(true);
    writer.beginArray();
    try {
      writer.close();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testSerializeNullsFalse() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setSerializeNulls(false);
    writer.beginObject();
    writer.name("A");
    writer.nullValue();
    writer.endObject();
    assertEquals("{}", writer.get().toString());
  }

  @Test
  public void testSerializeNullsTrue() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setSerializeNulls(true);
    writer.beginObject();
    writer.name("A");
    writer.nullValue();
    writer.endObject();
    assertEquals("{\"A\":null}", writer.get().toString());
  }

  @Test
  public void testEmptyWriter() {
    JsonTreeWriter writer = new JsonTreeWriter();
    assertEquals(JsonNull.INSTANCE, writer.get());
  }

  @Test
  public void testBeginArray() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    assertEquals(writer, writer.beginArray());
  }

  @Test
  public void testBeginObject() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    assertEquals(writer, writer.beginObject());
  }

  @Test
  public void testValueString() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    String n = "as";
    assertEquals(writer, writer.value(n));
  }

  @Test
  public void testBoolValue() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    boolean bool = true;
    assertEquals(writer, writer.value(bool));
  }

  @Test
  public void testBoolMaisValue() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    Boolean bool = true;
    assertEquals(writer, writer.value(bool));
  }

  @Test
  public void testLenientNansAndInfinities() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(true);
    writer.beginArray();
    writer.value(Float.NaN);
    writer.value(Float.NEGATIVE_INFINITY);
    writer.value(Float.POSITIVE_INFINITY);
    writer.value(Double.NaN);
    writer.value(Double.NEGATIVE_INFINITY);
    writer.value(Double.POSITIVE_INFINITY);
    writer.endArray();
    assertEquals("[NaN,-Infinity,Infinity,NaN,-Infinity,Infinity]", writer.get().toString());
  }

  @Test
  public void testStrictNansAndInfinities() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(false);
    writer.beginArray();
    try {
      writer.value(Float.NaN);
      fail();
    } catch (IllegalArgumentException expected) {
    }
    try {
      writer.value(Float.NEGATIVE_INFINITY);
      fail();
    } catch (IllegalArgumentException expected) {
    }
    try {
      writer.value(Float.POSITIVE_INFINITY);
      fail();
    } catch (IllegalArgumentException expected) {
    }
    try {
      writer.value(Double.NaN);
      fail();
    } catch (IllegalArgumentException expected) {
    }
    try {
      writer.value(Double.NEGATIVE_INFINITY);
      fail();
    } catch (IllegalArgumentException expected) {
    }
    try {
      writer.value(Double.POSITIVE_INFINITY);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void testStrictBoxedNansAndInfinities() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(false);
    writer.beginArray();
    try {
      writer.value(Float.valueOf(Float.NaN));
      fail();
    } catch (IllegalArgumentException expected) {
    }
    try {
      writer.value(Float.valueOf(Float.NEGATIVE_INFINITY));
      fail();
    } catch (IllegalArgumentException expected) {
    }
    try {
      writer.value(Float.valueOf(Float.POSITIVE_INFINITY));
      fail();
    } catch (IllegalArgumentException expected) {
    }
    try {
      writer.value(Double.valueOf(Double.NaN));
      fail();
    } catch (IllegalArgumentException expected) {
    }
    try {
      writer.value(Double.valueOf(Double.NEGATIVE_INFINITY));
      fail();
    } catch (IllegalArgumentException expected) {
    }
    try {
      writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void testJsonValue() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    try {
      writer.jsonValue("test");
      fail();
    } catch (UnsupportedOperationException expected) {
    }
  }

  /**
   * {@link JsonTreeWriter} effectively replaces the complete writing logic of {@link JsonWriter} to
   * create a {@link JsonElement} tree instead of writing to a {@link Writer}. Therefore all relevant
   * methods of {@code JsonWriter} must be overridden.
   */
  @Test
  public void testOverrides() {
    List<String> ignoredMethods = Arrays.asList("setLenient(boolean)", "isLenient()", "setIndent(java.lang.String)",
        "setHtmlSafe(boolean)", "isHtmlSafe()", "setSerializeNulls(boolean)", "getSerializeNulls()");
    MoreAsserts.assertOverridesMethods(JsonWriter.class, JsonTreeWriter.class, ignoredMethods);
  }
}
