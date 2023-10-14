/*
 * Copyright (C) 2012 Google Inc.
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

package com.google.gson.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gson.common.MoreAsserts;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.junit.Test;

public final class LinkedTreeMapTest {

  @Test
  public void testIterationOrder() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    map.put("a", "android");
    map.put("c", "cola");
    map.put("b", "bbq");
    assertIterationOrder(map.keySet(), "a", "c", "b");
    assertIterationOrder(map.values(), "android", "cola", "bbq");
  }

  @Test
  public void testRemoveRootDoesNotDoubleUnlink() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    map.put("a", "android");
    map.put("c", "cola");
    map.put("b", "bbq");
    Iterator<Map.Entry<String,String>> it = map.entrySet().iterator();
    it.next();
    it.next();
    it.next();
    it.remove();
    assertIterationOrder(map.keySet(), "a", "c");
  }

  @Test
  public void testPutNullKeyFails() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    try {
      map.put(null, "android");
      fail();
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testPutNonComparableKeyFails() {
    LinkedTreeMap<Object, String> map = new LinkedTreeMap<>();
    try {
      map.put(new Object(), "android");
      fail();
    } catch (ClassCastException expected) {}
  }

  @Test
  public void testPutNullValue() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    map.put("a", null);
    assertEquals(1, map.size());
    assertTrue(map.containsKey("a"));
    assertTrue(map.containsValue(null));
    assertNull(map.get("a"));
  }

  @Test
  public void testPutNullValue_Forbidden() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>(false);
    try {
      map.put("a", null);
      fail();
    } catch (NullPointerException e) {
      assertEquals("value == null", e.getMessage());
    }
    assertEquals(0, map.size());
    assertFalse(map.containsKey("a"));
    assertFalse(map.containsValue(null));
  }

  @Test
  public void testEntrySetValueNull() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    map.put("a", "1");
    assertEquals("1", map.get("a"));
    Entry<String, String> entry = map.entrySet().iterator().next();
    assertEquals("a", entry.getKey());
    assertEquals("1", entry.getValue());
    entry.setValue(null);
    assertNull(entry.getValue());

    assertTrue(map.containsKey("a"));
    assertTrue(map.containsValue(null));
    assertNull(map.get("a"));
  }


  @Test
  public void testEntrySetValueNull_Forbidden() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>(false);
    map.put("a", "1");
    Entry<String, String> entry = map.entrySet().iterator().next();
    try {
      entry.setValue(null);
      fail();
    } catch (NullPointerException e) {
      assertEquals("value == null", e.getMessage());
    }
    assertEquals("1", entry.getValue());
    assertEquals("1", map.get("a"));
    assertFalse(map.containsValue(null));
  }

  @Test
  public void testContainsNonComparableKeyReturnsFalse() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    map.put("a", "android");
    assertFalse(map.containsKey(new Object()));
  }

  @Test
  public void testContainsNullKeyIsAlwaysFalse() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    assertFalse(map.containsKey(null));
    map.put("a", "android");
    assertFalse(map.containsKey(null));
  }

  @Test
  public void testPutOverrides() throws Exception {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    assertNull(map.put("d", "donut"));
    assertNull(map.put("e", "eclair"));
    assertNull(map.put("f", "froyo"));
    assertEquals(3, map.size());

    assertEquals("donut", map.get("d"));
    assertEquals("donut", map.put("d", "done"));
    assertEquals(3, map.size());
  }

  @Test
  public void testEmptyStringValues() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    map.put("a", "");
    assertTrue(map.containsKey("a"));
    assertEquals("", map.get("a"));
  }

  @Test
  public void testLargeSetOfRandomKeys() throws Exception {
    Random random = new Random(1367593214724L);
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    String[] keys = new String[1000];
    for (int i = 0; i < keys.length; i++) {
      keys[i] = Integer.toString(Math.abs(random.nextInt()), 36) + "-" + i;
      map.put(keys[i], "" + i);
    }

    for (int i = 0; i < keys.length; i++) {
      String key = keys[i];
      assertTrue(map.containsKey(key));
      assertEquals("" + i, map.get(key));
    }
  }

  @Test
  public void testClear() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    map.put("a", "android");
    map.put("c", "cola");
    map.put("b", "bbq");
    map.clear();
    assertIterationOrder(map.keySet());
    assertEquals(0, map.size());
  }

  @Test
  public void testEqualsAndHashCode() throws Exception {
    LinkedTreeMap<String, Integer> map1 = new LinkedTreeMap<>();
    map1.put("A", 1);
    map1.put("B", 2);
    map1.put("C", 3);
    map1.put("D", 4);

    LinkedTreeMap<String, Integer> map2 = new LinkedTreeMap<>();
    map2.put("C", 3);
    map2.put("B", 2);
    map2.put("D", 4);
    map2.put("A", 1);

    MoreAsserts.assertEqualsAndHashCode(map1, map2);
  }

  @Test
  public void testJavaSerialization() throws IOException, ClassNotFoundException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutputStream objOut = new ObjectOutputStream(out);
    Map<String, Integer> map = new LinkedTreeMap<>();
    map.put("a", 1);
    objOut.writeObject(map);
    objOut.close();

    ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
    @SuppressWarnings("unchecked")
    Map<String, Integer> deserialized = (Map<String, Integer>) objIn.readObject();
    assertEquals(Collections.singletonMap("a", 1), deserialized);
  }

  @SuppressWarnings("varargs")
  @SafeVarargs
  private final <T> void assertIterationOrder(Iterable<T> actual, T... expected) {
    ArrayList<T> actualList = new ArrayList<>();
    for (T t : actual) {
      actualList.add(t);
    }
    assertEquals(Arrays.asList(expected), actualList);
  }
}
