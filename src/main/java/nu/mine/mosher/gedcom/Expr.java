package nu.mine.mosher.gedcom;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Expr {
    private static final Logger log = Logger.getLogger("");
    private final String orig;
    private final List<String> path = new ArrayList<>();
    private boolean rooted; // not yet implemented

    public Expr(final String expr) {
        this.orig = expr;
    }

    private static boolean next(final StreamTokenizer t) {
        try {
            return t.nextToken() != StreamTokenizer.TT_EOF;
        } catch (final Throwable cannotHappen) {
            throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return this.path.toString();
    }

    public void parse() throws InvalidSyntax {
        final int START = 0;
        final int TAG = 1;
        final int DOWN = 2;

        final StreamTokenizer token = tokenizer();

        this.rooted = false;
        int state = START;
        while (next(token)) {
            log.finest(token.toString());
            switch (state) {
                case START: {
                    if (token.ttype == '.') {
                        this.rooted = true;
                    } else {
                        token.pushBack();
                    }
                    state = TAG;
                }
                break;
                case TAG: {
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
        log.fine(this.toString());
    }

    private StreamTokenizer tokenizer() {
        final StreamTokenizer t = new StreamTokenizer(new StringReader(this.orig));

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
