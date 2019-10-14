package org.valkyrienskies.mod.common.command.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class VSCommandUtil {

    /**
     * The only difference between tab-complete args and proper args is that tab-complete args
     * retain an empty string element at the end if the tab complete was requesting a new arg.
     */
    public static String[] toTabCompleteArgs(String[] args) {
        List<String> properArgs = translateCommandline(String.join(" ", args));

        if (args[args.length - 1].equals("")) {
            properArgs.add("");
        }

        return properArgs.toArray(new String[0]);
    }

    public static String[] toProperArgs(String[] args) {
        return toProperArgs(String.join(" ", args));
    }

    public static String[] toProperArgs(String args) {
        return translateCommandline(args).toArray(new String[0]);
    }

    /**
     * This was pretty much copy-pasted from Apache Ant's Commandline#translateCommandLine
     *
     * @see <a href="https://bit.ly/2YYbczE">CommandLine.java</a>
     */
    private static ArrayList<String> translateCommandline(String toProcess) {
        if (toProcess == null || toProcess.length() == 0) {
            //no command? no string
            return new ArrayList<>();
        }

        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        final StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
        final ArrayList<String> result = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        boolean lastTokenHasBeenQuoted = false;

        while (tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            switch (state) {
                case inQuote:
                    if ("\'".equals(nextTok)) {
                        lastTokenHasBeenQuoted = true;
                        state = normal;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                case inDoubleQuote:
                    if ("\"".equals(nextTok)) {
                        lastTokenHasBeenQuoted = true;
                        state = normal;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                default:
                    if ("\'".equals(nextTok)) {
                        state = inQuote;
                    } else if ("\"".equals(nextTok)) {
                        state = inDoubleQuote;
                    } else if (" ".equals(nextTok)) {
                        if (lastTokenHasBeenQuoted || current.length() != 0) {
                            result.add(current.toString());
                            current.setLength(0);
                        }
                    } else {
                        current.append(nextTok);
                    }
                    lastTokenHasBeenQuoted = false;
                    break;
            }
        }
        if (lastTokenHasBeenQuoted || current.length() != 0) {
            result.add(current.toString());
        }
        if (state == inQuote || state == inDoubleQuote) {
            throw new RuntimeException("unbalanced quotes in " + toProcess);
        }
        return result;
    }

}
