package hash;

import common.MainChecker;
import common.Triple;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class HashChecker extends MainChecker {
    public HashChecker(final String className) {
        super(className);
    }

    /** Test triple layout: <code>(file, contents, hash code)</code>. */
    @SafeVarargs
    protected final void test(final String input, final Triple<String, String, String>... tests) throws IOException {
        testArray(input, tests);
    }

    protected void testArray(final String input, final Triple<String, String, String>[] tests) throws IOException {
        writeFiles(tests);
        write(input, Arrays.stream(tests).map(Triple::first).collect(Collectors.joining("\n")));
        checkEquals(Arrays.stream(tests).map(Triple::third).collect(Collectors.toList()), run(input));
    }

    protected void writeFiles(final Triple<String, String, String>[] tests) throws IOException {
        for (final Triple<String, String, String> test : tests) {
            write(test.first, test.second);
        }
    }

    protected HashChecker test(
            final Triple<String, String, String> input,
            final Triple<String, String, String> hello,
            final Triple<String, String, String> empty,
            final Triple<String, String, String> hash,
            final Triple<String, String, String> spaces
    ) throws IOException {
        test("input.txt", input);
        test("input.txt", hello);
        test("input.txt", empty);
        test("input.txt", hash);
        test("input.txt", spaces);
        test("input.txt", hello, empty, hash, spaces);
        test("input.txt", empty, empty, empty);
        test("__test__input", hello, empty, hash, empty, spaces);
        return this;
    }
}
