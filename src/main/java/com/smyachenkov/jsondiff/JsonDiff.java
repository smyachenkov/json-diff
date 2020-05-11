package com.smyachenkov.jsondiff;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonDiff {

  private static final Set<Class<?>> JSON_PRIMITIVES = Set.of(
      Integer.class,
      Long.class,
      Double.class,
      String.class
  );

  public List<Difference> diff(Map<String, Object> from,
                               Map<String, Object> to) {
    return diff(from, to, "/");
  }

  public List<Difference> diff(Map<String, Object> from,
                               Map<String, Object> to,
                               String path) {
    if (from == to) {
      return List.of();
    }
    List<Difference> differences = new ArrayList<>();
    Set<String> keys = new HashSet<>();
    keys.addAll(from.keySet());
    keys.addAll(to.keySet());
    keys.forEach(key -> {
      // key is removed
      if (!to.containsKey(key) && from.containsKey(key)) {
        differences.add(new Difference(from.get(key), path + key, Operation.REMOVED));
      // new key is added
      } else if (to.containsKey(key) && !from.containsKey(key)) {
        differences.add(new Difference(to.get(key), path + key, Operation.ADDED));
      // existing key is modified
      } else {
        differences.addAll(compare(from.get(key), to.get(key), path + key + "/"));
      }
    });
    return differences;
  }

  private List<Difference> compare(Object from, Object to, String path) {
    var differences = new ArrayList<Difference>();
    var fromClass = from.getClass();
    var toClass = to.getClass();
    if (oneIsPrimitive(fromClass, toClass)) {
      if (!from.equals(to)) {
        differences.add(new Difference(from, path, Operation.REMOVED));
        differences.add(new Difference(to, path, Operation.ADDED));
      }
    } else if (bothAreObjects(from, to)) {
      differences.addAll(diff((Map<String, Object>) from, (Map<String, Object>) to, path));
    } else if (bothAreArrays(fromClass, toClass)) {
      var fromArray = (ArrayList<Object>) from;
      var toArray = (ArrayList<Object>) to;
      var arrayDiffs = new ArrayList<Difference>();
      for (int i = 0; i < Math.min(fromArray.size(), toArray.size()); i++) {
        arrayDiffs.addAll(compare(fromArray.get(i), toArray.get(i), path + i + "/"));
      }
      // add new to fromArray
      if (toArray.size() > fromArray.size()) {
        for (int i = fromArray.size(); i < toArray.size(); i++) {
          arrayDiffs.add(new Difference(toArray.get(i), path + i, Operation.ADDED));
        }
      }
      // remove extra from fromArray
      if (toArray.size() < fromArray.size()) {
        for (int i = toArray.size(); i < fromArray.size(); i++) {
          arrayDiffs.add(new Difference(fromArray.get(i), path + i, Operation.REMOVED));
        }
      }
      differences.addAll(arrayDiffs);
    } else {
      differences.add(new Difference(from, path, Operation.REMOVED));
      differences.add(new Difference(to, path, Operation.ADDED));
    }
    return differences;
  }

  private boolean oneIsPrimitive(Class<?> from, Class<?> to) {
    return JSON_PRIMITIVES.contains(to) || JSON_PRIMITIVES.contains(from);
  }

  private boolean bothAreObjects(Object from, Object to) {
    return from instanceof Map && to instanceof Map;
  }

  private boolean bothAreArrays(Class<?> from, Class<?> to) {
    return from == ArrayList.class && to == ArrayList.class;
  }

}
