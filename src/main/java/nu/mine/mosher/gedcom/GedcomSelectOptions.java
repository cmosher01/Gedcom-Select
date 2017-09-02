package nu.mine.mosher.gedcom;

import java.io.File;
import java.io.IOException;

@SuppressWarnings({"access", "WeakerAccess", "unused"})
public class GedcomSelectOptions {
    public boolean help;
    public File gedcom;
    public GedcomDataRef ref;

    public void h() {
        help();
    }

    public void help() {
        this.help = true;
        System.err.println("Usage: java -jar gedcom-select-all.jar -g in.ged -w .INDI.NAME <names.txt");
        System.err.println("Extracts IDs from a GEDCOM file, based on tag=value list.");
        System.err.println("Options:");
        System.err.println("-g, --gedcom=FILE    GEDCOM file to extract from.");
        System.err.println("-w, --where=EXPR     Tag path to match, ex.: .INDI.NAME");
    }

    public void g(final String gedcom) throws IOException {
        gedcom(gedcom);
    }

    public void gedcom(final String file) throws IOException {
        this.gedcom = new File(file);
        if (!this.gedcom.canRead()) {
            throw new IllegalArgumentException("Cannot open GEDCOM file: " + this.gedcom.getCanonicalPath());
        }
    }

    public void w(final String expr) throws GedcomDataRef.InvalidSyntax {
        where(expr);
    }

    public void where(final String expr) throws GedcomDataRef.InvalidSyntax {
        this.ref = new GedcomDataRef(expr);
    }

    public GedcomSelectOptions verify() {
        if (this.help) {
            return this;
        }
        if (this.gedcom == null) {
            throw new IllegalArgumentException("Missing required -g GEDCOM file.");
        }
        if (this.ref == null) {
            throw new IllegalArgumentException("Missing required -w tags.");
        }
        return this;
    }
}
