package com.smyachenkov.jsondiff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonDiffJacksonTest {

  @Test
  public void shouldReturnEmptyMapForEqualDocument() {
    var document = toJson(
        //language=JSON
        "{"
        + "\"foo\": 1"
        + "}"
    );
    var actual = new JsonDiff().diff(document, document);
    var expected = List.of();
    assertEquals(expected, actual);
  }

  @Test
  public void shouldDetectRemovedKeys() {
    var from = toJson(
        //language=JSON
        "{"
        + "  \"foo\": 123,"
        + "  \"bar\": 1"
        + "}"
    );
    var to = toJson(
        //language=JSON
        "{"
        + "\"foo\": 123"
        + "}"
    );
    var actual = new JsonDiff().diff(from, to);
    var expected = List.of(
        new Difference(1, "/bar", Operation.REMOVED)
    );
    assertEquals(expected, actual);
  }

  @Test
  public void shouldDetectReplacementOfPrimitives() {
    var from = toJson(
        //language=JSON
        "{"
        + "\"foo\": 1"
        + "}"
    );
    var to = toJson(
        //language=JSON
        "{"
        + "\"foo\": \"bar\""
        + "}"
    );
    var actual = new JsonDiff().diff(from, to);
    var expected = List.of(
        new Difference(1, "/foo/", Operation.REMOVED),
        new Difference("bar", "/foo/", Operation.ADDED)
    );
    assertEquals(expected, actual);
  }

  @Test
  public void shouldDetectReplacementOfObjectWithPrimitive() {
    var from = toJson(
        //language=JSON
        "{"
        + "\"foo\": 1,"
        + "\"bar\": {"
        + "   \"a\": 2,"
        + "   \"b\": 3"
        + " }"
        + "}"
    );
    var to = toJson(
        //language=JSON
        "{"
        + "\"foo\": 1,"
        + "\"bar\": 2"
        + "}"
    );
    var actual = new JsonDiff().diff(from, to);
    var expected = List.of(
        new Difference(Map.of("a", 2, "b", 3), "/bar/", Operation.REMOVED),
        new Difference(2, "/bar/", Operation.ADDED)
    );
    assertEquals(expected, actual);
  }

  @Test
  public void shouldDetectReplacementInNestedObject() {
    var from = toJson(
        //language=JSON
        "{"
        + "\"foo\": {"
        + "\"bar\": {"
        + "     \"a\": 1,"
        + "     \"b\": \"b\""
        + "   },"
        + "   \"baz\": 12"
        + " }"
        + "}"
    );
    var to = toJson(
        //language=JSON
        "{"
        + " \"foo\": {"
        + " \"bar\": {"
        + "   \"a\": 2,"
        + "   \"c\": \"c\""
        + " },"
        + "   \"baz\": 12"
        + " }"
        + "}"
    );
    var actual = new JsonDiff().diff(from, to);
    var expected = List.of(
        new Difference(1, "/foo/bar/a/", Operation.REMOVED),
        new Difference(2, "/foo/bar/a/", Operation.ADDED),
        new Difference("b", "/foo/bar/b", Operation.REMOVED),
        new Difference("c", "/foo/bar/c", Operation.ADDED)
    );
    assertEquals(expected, actual);
  }


  @Test
  public void shouldDetectReplacementInPrimitiveArray() {
    var from = toJson(
        //language=JSON
        "{"
        + "\"foo\": [0, 1, 2]"
        + "}"
    );
    var to = toJson(
        //language=JSON
        "{"
        + "\"foo\": [0, 3, 2]"
        + "}"
    );
    var actual = new JsonDiff().diff(from, to);
    var expected = List.of(
        new Difference(1, "/foo/1/", Operation.REMOVED),
        new Difference(3, "/foo/1/", Operation.ADDED)
    );
    assertEquals(expected, actual);
  }


  @Test
  public void shouldDetectExtensionOfPrimitiveArray() {
    var from = toJson(
        //language=JSON
        "{"
        + "\"foo\": [0, 1, 2]"
        + "}"
    );
    var to = toJson(
        //language=JSON
        "{"
        + "\"foo\": [0, 1, 2, 3, 4]"
        + "}"
    );
    var actual = new JsonDiff().diff(from, to);
    var expected = List.of(
        new Difference(3, "/foo/3", Operation.ADDED),
        new Difference(4, "/foo/4", Operation.ADDED)
    );
    assertEquals(expected, actual);
  }

  @Test
  public void shouldDetectShrinkOfPrimitiveArray() {
    var from = toJson(
        //language=JSON
        "{"
        + "\"foo\": [0, 1, 2, 3, 4]"
        + "}"
    );
    var to = toJson(
        //language=JSON
        "{"
        + "\"foo\": [0, 1, 2]"
        + "}"
    );
    var actual = new JsonDiff().diff(from, to);
    var expected = List.of(
        new Difference(3, "/foo/3", Operation.REMOVED),
        new Difference(4, "/foo/4", Operation.REMOVED)
    );
    assertEquals(expected, actual);
  }

  @Test
  public void shouldDetectInsertionOfANewObjectToArray() {
    var from = toJson(
        //language=JSON
        "{"
        + "\"foo\": []"
        + "}"
    );

    var to = toJson(
        //language=JSON
        "{"
        + "\"foo\": ["
        + " {"
        + "   \"a\": {"
        + "     \"value\": 1"
        + "   }"
        + " }"
        + "]"
        + "}"
    );
    var actual = new JsonDiff().diff(from, to);
    var expected = List.of(
        new Difference(Map.of("a", Map.of("value", 1)), "/foo/0", Operation.ADDED)
    );
    assertEquals(expected, actual);
  }

  @Test
  public void shouldDetectChangesInArrayElement() {
    var from = toJson(
        //language=JSON
        "{"
        + "\"foo\": ["
        + "{"
        + "\"a\": 1,"
        + "\"b\": 2"
        + "},"
        + "{"
        + "\"c\": 3"
        + "}"
        + "]"
        + "}"
    );
    var to = toJson(
        //language=JSON
        "{"
        + "\"foo\": ["
        + "{"
        + "\"a\": 1,"
        + "\"b\": {"
        + "\"value\": 2"
        + "}"
        + "},"
        + "{"
        + "\"c\": 3"
        + "}"
        + "]"
        + "}"
    );
    var actual = new JsonDiff().diff(from, to);
    var expected = List.of(
        new Difference(2, "/foo/0/b/", Operation.REMOVED),
        new Difference(Map.of("value", 2), "/foo/0/b/", Operation.ADDED)
    );
    assertEquals(expected, actual);
  }

  private LinkedHashMap<String, Object> toJson(String json) {
    try {
      return new ObjectMapper().readValue(json, LinkedHashMap.class);
    } catch (Exception e) {
      return null;
    }
  }

}
