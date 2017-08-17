package nu.mine.mosher.gedcom;

import joptsimple.OptionParser;
import nu.mine.mosher.collection.TreeNode;
import nu.mine.mosher.gedcom.exception.InvalidLevel;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import static nu.mine.mosher.logging.Jul.log;

/**
 * Given a path of tags, and a list of values, extracts
 * IDs of matching records from GEDCOM file.
 * <p>
 * Note: always extracts the SUBM ID listed in the HEAD, too.
 * <p>
 * Created by Christopher Alan Mosher on 2017-08-09
 */
public class GedcomSelect implements Gedcom.Processor {
    private final GedcomSelectOptions options;
    private final Set<String> values = new HashSet<>();

    private String lastID = "";

    public static void main(final String... args) throws InvalidLevel, IOException, Expr.InvalidSyntax {
        final GedcomSelectOptions options = new GedcomSelectOptions(new OptionParser());
        options.parse(args);
        new Gedcom(options, new GedcomSelect(options)).main();
    }

    private GedcomSelect(final GedcomSelectOptions options) {
        this.options = options;
    }

    @Override
    public boolean process(final GedcomTree tree) {
        try {
            readValues();
            selectSubm(tree);
            select(tree);
        } catch (final Throwable e) {
            // TODO fix exception handling
            throw new IllegalStateException(e);
        }

        return false;
    }

    private void readValues() throws IOException {
        File file = this.options.file();
        if (file == null) {
            throw new IllegalArgumentException("Missing required VALUES file.");
        }
        final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        for (String s = in.readLine(); s != null; s = in.readLine()) {
            this.values.add(s);
        }
        in.close();
    }

    private void selectSubm(final GedcomTree tree) {
        for (final TreeNode<GedcomLine> r : tree.getRoot()) {
            final GedcomLine head = r.getObject();
            if (head.getTag().equals(GedcomTag.HEAD)) {
                for (final TreeNode<GedcomLine> c : r) {
                    final GedcomLine subm = c.getObject();
                    if (subm.getTag().equals(GedcomTag.SUBM)) {
                        final String id = subm.getPointer();
                        log().info("found " + id + ": (SUBM record always selected)");
                        System.out.println(id);
                    }
                }
            }
        }
    }

    private void select(final GedcomTree tree) throws IOException {
        processLevel(tree.getRoot(), 0);
    }

    private void processLevel(final TreeNode<GedcomLine> node, final int level) throws IOException {
        for (final TreeNode<GedcomLine> c : node) {
            final GedcomLine ln = c.getObject();
            final String tag = ln.getTagString().toLowerCase();
            if (tag.equals(this.options.expr().get(level))) {
                if (level == 0 && !ln.hasID()) {
                    throw new IOException("missing ID: " + ln);
                }
                if (ln.hasID()) {
                    this.lastID = ln.getID();
                }
                if (this.options.expr().at(level)) {
                    log().finer("checking " + this.lastID + ": " + ln);
                    if (this.values.contains(ln.getValue())) {
                        log().info("found " + this.lastID + ": " + ln);
                        System.out.println(this.lastID);
                    }
                } else {
                    log().finest("checking within " + ln);
                    processLevel(c, level + 1);
                }
            }
        }
    }
}
