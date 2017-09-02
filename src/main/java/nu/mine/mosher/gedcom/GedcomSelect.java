package nu.mine.mosher.gedcom;

import nu.mine.mosher.collection.TreeNode;
import nu.mine.mosher.gedcom.exception.InvalidLevel;
import nu.mine.mosher.mopper.ArgParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
public class GedcomSelect {
    private final GedcomSelectOptions options;
    private final Set<String> values = new HashSet<>();
    private GedcomTree tree;

    private String lastID = "";

    public static void main(final String... args) throws InvalidLevel, IOException, GedcomDataRef.InvalidSyntax {
        log();
        new GedcomSelect(new ArgParser<>(new GedcomSelectOptions()).parse(args).verify()).main();
        System.out.flush();
        System.err.flush();
    }

    private GedcomSelect(final GedcomSelectOptions options) {
        this.options = options;
    }

    private void main() throws IOException, InvalidLevel {
        if (this.options.help) {
            return;
        }
        readGedcom();
        readValues();
        selectSubm();
        select();
    }

    private void readGedcom() throws IOException, InvalidLevel {
        tree = Gedcom.readFile(new BufferedInputStream(new FileInputStream(this.options.gedcom)));
        new GedcomConcatenator(tree).concatenate();
    }

    private void readValues() throws IOException {
        new BufferedReader(new InputStreamReader(new FileInputStream(FileDescriptor.in), StandardCharsets.UTF_8)).lines().forEach(this.values::add);
    }

    private void selectSubm() {
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

    private void select() throws IOException {
        this.options.ref.forEach(this.tree, c -> {
            if (this.values.contains(c.getObject().getValue())) {
                System.out.println(findContainingRecord(c).getObject().getID());
            }
        });
    }

    private static TreeNode<GedcomLine> findContainingRecord(final TreeNode<GedcomLine> n) {
        if (n.parent().parent() == null) {
            return n;
        }
        return findContainingRecord(n.parent());
    }
}
