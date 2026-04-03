package ai.x.grok.api.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColumnPrinter {

  private static final int COL_WIDTH = 80;

  public static void printAtCol(String leftBlock, String rightBlock) {
    List<String> col1 = wrapLines(toLines(leftBlock), COL_WIDTH);
    List<String> col2 = wrapLines(toLines(rightBlock), COL_WIDTH);
    printAtCol(col1, col2);
  }

  public static void printAtCol(List<String> col1, List<String> col2) {
    int rows = Math.max(col1.size(), col2.size());

    for (int i = 0; i < rows; i++) {
      String left = i < col1.size() ? col1.get(i) : "";
      String right = i < col2.size() ? col2.get(i) : "";

      System.out.println(padOrTrim(left, COL_WIDTH) + padOrTrim(right, COL_WIDTH));
    }
  }

  // --- Helpers -------------------------------------------------------------
  public static List<String> toLines(String text) {
    if (text == null || text.isEmpty()) {
      return List.of();
    }
    return Arrays.asList(text.split("\\R"));
  }

  public static List<String> wrapLines(List<String> lines, int width) {
    List<String> out = new ArrayList<>();
    for (String line : lines) {
      if (line == null) {
        out.add("");
        continue;
      }
      int start = 0;
      while (start < line.length()) {
        int end = Math.min(start + width, line.length());
        out.add(line.substring(start, end));
        start = end;
      }
      if (line.isEmpty()) {
        out.add("");
      }
    }
    return out;
  }

  private static String padOrTrim(String s, int width) {
    if (s.length() > width) {
      return s.substring(0, width);
    }
    if (s.length() == width) {
      return s;
    }
    StringBuilder sb = new StringBuilder(width);
    sb.append(s);
    while (sb.length() < width) {
      sb.append(' ');
    }
    return sb.toString();
  }
}
