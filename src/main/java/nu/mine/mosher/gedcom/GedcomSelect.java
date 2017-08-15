package nu.mine.mosher.gedcom;

import nu.mine.mosher.collection.TreeNode;
import nu.mine.mosher.gedcom.exception.InvalidLevel;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Given a path of tags, and a list of values, extracts
 * IDs of matching records from GEDCOM file.
 *
 * Note: always extracts the SUBM ID listed in the HEAD, too.
 *
 * Created by Christopher Alan Mosher on 2017-08-09
 */
public class GedcomSelect {
    private static final Logger log = Logger.getLogger("");

    private final File fileGedcom;
    private final Expr expr;
    private final File fileValues;
    private final Set<String> values = new HashSet<>();

    private GedcomTree gt;
    private String lastID = "";

    private GedcomSelect(final String filenameGedcom, final String expr, final String filenameValues) {
        this.fileGedcom = new File(filenameGedcom);
        this.expr = new Expr(expr);
        this.fileValues = new File(filenameValues);
    }

    public static void main(final String... args) throws InvalidLevel, IOException, Expr.InvalidSyntax {
        if (args.length != 3) {
            throw new IllegalArgumentException("usage: java -jar gedcom-select in.ged expr values.csv");
        } else {
            new GedcomSelect(args[0], args[1], args[2]).main();
        }
    }

    private void main() throws IOException, InvalidLevel, Expr.InvalidSyntax {
        this.expr.parse();

        readValues();

        loadGedcom();

        selectSubm();
        select();

        System.err.flush();
        System.out.flush();
    }

    private void readValues() throws IOException {
        final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(this.fileValues), "UTF-8"));
        for (String s = in.readLine(); s != null; s = in.readLine()) {
            this.values.add(s);
        }
        in.close();
    }

    private void selectSubm() {
        for (final TreeNode<GedcomLine> r : this.gt.getRoot()) {
            final GedcomLine head = r.getObject();
            if (head.getTag().equals(GedcomTag.HEAD)) {
                for (final TreeNode<GedcomLine> c : r) {
                    final GedcomLine subm = c.getObject();
                    if (subm.getTag().equals(GedcomTag.SUBM)) {
                        final String id = subm.getPointer();
                        log.info("found " + id + ": (SUBM record always selected)");
                        System.out.println(id);
                    }
                }
            }
        }
    }

    private void select() throws IOException {
        processLevel(this.gt.getRoot(), 0);
    }

    private void processLevel(final TreeNode<GedcomLine> node, final int level) throws IOException {
        for (final TreeNode<GedcomLine> c : node) {
            final GedcomLine ln = c.getObject();
            final String tag = ln.getTagString().toLowerCase();
            if (tag.equals(this.expr.get(level))) {
                if (level == 0 && !ln.hasID()) {
                    throw new IOException("missing ID: " + ln);
                }
                if (ln.hasID()) {
                    this.lastID = ln.getID();
                }
                if (this.expr.at(level)) {
                    log.finer("checking " + this.lastID + ": " + ln);
                    if (this.values.contains(ln.getValue())) {
                        log.info("found " + this.lastID + ": " + ln);
                        System.out.println(this.lastID);
                    }
                } else {
                    log.finest("checking within " + ln);
                    processLevel(c, level + 1);
                }
            }
        }
    }

    private void loadGedcom() throws IOException, InvalidLevel {
        final Charset charset = Gedcom.getCharset(this.fileGedcom);
        this.gt = Gedcom.parseFile(fileGedcom, charset, false);
    }
}
