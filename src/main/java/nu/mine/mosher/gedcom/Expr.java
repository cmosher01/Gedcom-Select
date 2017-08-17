package nu.mine.mosher.gedcom;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class Expr {
    private final List<String> path;

    public Expr(final String expr) throws InvalidSyntax {
        this.path = parse(expr);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(40);

        this.path.forEach(i -> { sb.append('.'); sb.append(i); });

        return sb.toString();
    }

    private static ArrayList<String> parse(final String expr) throws InvalidSyntax {
        final ArrayList<String> path = new ArrayList<>(8);

        final int START = 0;
        final int TAG = 1;
        final int DOWN = 2;

        final StreamTokenizer token = tokenizer(expr);

        int state = START;
        while (next(token)) {
            switch (state) {
                case START: {
                    if (token.ttype == '.') {
                        //this.rooted = true;
                    } else {
                        token.pushBack();
                    }
                    state = TAG;
                }
                break;
                case TAG: {
                    if (token.ttype == '.') {
                        throw new InvalidSyntax();
                    }
                    path.add(token.sval);
                    state = DOWN;
                }
                break;
                case DOWN: {
                    if (token.ttype != '.') {
                        throw new InvalidSyntax();
                    }
                    state = TAG;
                }
                break;
            }
        }

        return path;
    }

    private static StreamTokenizer tokenizer(final String expr) {
        final StreamTokenizer t = new StreamTokenizer(new StringReader(expr));

        t.resetSyntax();

        t.slashSlashComments(false);
        t.slashStarComments(false);
        t.eolIsSignificant(false);
        t.lowerCaseMode(true);

        // valid characters for GEDCOM tag:
        t.wordChars('0', '9');
        t.wordChars('A', 'Z');
        t.wordChars('a', 'z');
        t.wordChars('_', '_');

        return t;
    }

    private static boolean next(final StreamTokenizer t) {
        try {
            return t.nextToken() != StreamTokenizer.TT_EOF;
        } catch (final Throwable cannotHappen) {
            throw new IllegalStateException();
        }
    }

    public String get(final int i) {
        if (i < 0 || this.path.size() <= i) {
            return "";
        }
        return this.path.get(i);
    }

    public boolean at(final int i) {
        return i == this.path.size() - 1;
    }

    public static class InvalidSyntax extends Exception {
    }
}
