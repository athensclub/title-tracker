package athensclub.android.titletracker.data;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Title {

    private String title;

    private SortedMap<Integer, SortedSet<Integer>> finishedEpisode;

    public static class RangeOrNumberParseResult {

        public final UnionIntList result;
        public final String error;
        public boolean hasError;

        public RangeOrNumberParseResult(String error) {
            hasError = true;
            this.error = error;
            result = null;
        }

        public RangeOrNumberParseResult(UnionIntList result) {
            hasError = false;
            error = null;
            this.result = result;
        }

    }

    public static class UnionIntList {

        public final int intValue;
        public final List<Integer> listValue;
        public final boolean isInt;

        public UnionIntList(int x) {
            isInt = true;
            listValue = null;
            intValue = x;
        }

        public UnionIntList(List<Integer> l) {
            isInt = false;
            listValue = l;
            intValue = 0;
        }

    }

    /**
     * Parse a range or number and return a union type of error message and result.
     *
     * @param str the string to be parsed. Could either be a number (ex. 1 2 3) or a
     *            a range (ex. 1-3 2-6)
     * @return a union type of error message and result, where error message will be returned
     * when an error occurs, and the result will be returned if there is no error.
     */
    public static RangeOrNumberParseResult parseRangeOrNumber(String str) {
        if (str.trim().isEmpty())
            return new RangeOrNumberParseResult("Please enter some number or range.");
        if (!str.contains("-")) {
            try {
                int val = Integer.parseInt(str);
                if (val <= 0)
                    return new RangeOrNumberParseResult("The number must be positive only, Please try again.");
                return new RangeOrNumberParseResult(new UnionIntList(val));
            } catch (NumberFormatException e) {
                return new RangeOrNumberParseResult("The number format is invalid, Please try again.");
            }
        } else {
            String[] parts = str.split("\\-");
            if (parts.length > 2)
                return new RangeOrNumberParseResult("Bad input ('-' symbol found more than once). Please try again.");
            try {
                int a = Integer.parseInt(parts[0]);
                int b = Integer.parseInt(parts[1]);
                if (a <= 0 || b <= 0)
                    return new RangeOrNumberParseResult("The number must be positive only, Please try again.");
                if (b < a)
                    return new RangeOrNumberParseResult("The end of range must be greater that the begin of range. Please try again.");
                return new RangeOrNumberParseResult(new UnionIntList(Arrays.asList(a, b)));
            } catch (NumberFormatException e) {
                return new RangeOrNumberParseResult("The number format is invalid, Please try again.");
            }
        }
    }

    /**
     * Group close elements into a range. Expects set to be sorted set.
     *
     * @param set the set of elements to be grouped. Expects to be sorted.
     * @return a list of union type of range or number.
     */
    public static List<UnionIntList> groupRange(Set<Integer> set) {
        List<UnionIntList> result = new ArrayList<>();
        int start = -1, end = -1;
        for (int x : set) {
            if (start == -1) {
                start = x;
                end = x;
            } else if (x - end == 1) {
                end = x;
            } else {
                if (end == start)
                    result.add(new UnionIntList(start));
                else
                    result.add(new UnionIntList(Arrays.asList(start, end)));
                start = x;
                end = start;
            }
        }
        if (start != -1) {
            if (end == start)
                result.add(new UnionIntList(start));
            else
                result.add(new UnionIntList(Arrays.asList(start, end)));
        }
        return result;
    }

    public static String makeRangeText(Set<Integer> set) {
        StringBuilder result = new StringBuilder();
        List<UnionIntList> ranges = groupRange(set);
        for (int i = 0; i < ranges.size(); i++) {
            if (i != 0)
                result.append(", ");
            UnionIntList u = ranges.get(i);
            if (u.isInt)
                result.append(u.intValue);
            else
                result.append(u.listValue.get(0)).append('-').append(u.listValue.get(1));
        }
        return result.toString();
    }

    public Title(String title) {
        this(title, new TreeMap<>());
    }

    public Title(String title, SortedMap<Integer, SortedSet<Integer>> finishedEpisode) {
        this.title = title;
        this.finishedEpisode = new TreeMap<>(finishedEpisode);
    }

    public String getTitle() {
        return title;
    }

    public String finishedSeasonText() {
        String str = makeRangeText(finishedEpisode.keySet());
        if (str.isEmpty()) return "None";
        return str;
    }

    public SortedMap<Integer, SortedSet<Integer>> getFinishedEpisode() {
        return finishedEpisode;
    }

    @NonNull
    @Override
    public Title clone() {
        Title result = new Title(title);
        result.finishedEpisode.putAll(finishedEpisode);
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String unfinishedEpisodeText() {
        String str = finishedEpisode.entrySet()
                .stream()
                .filter(e -> e.getValue().size() > 0)
                .map(e -> "Season " + e.getKey() + ": " + makeRangeText(e.getValue()))
                .collect(Collectors.joining("\n"));
        if (str.isEmpty())
            return "None";
        return str;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Title)) return false;
        Title title1 = (Title) o;
        return Objects.equals(title, title1.title);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(title);
    }

    @Override
    public String toString() {
        return "Title{" +
                "title='" + title + '\'' +
                ", finishedEpisode=" + finishedEpisode +
                '}';
    }
}
