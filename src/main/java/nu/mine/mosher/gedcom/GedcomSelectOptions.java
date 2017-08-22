package nu.mine.mosher.gedcom;

import java.io.File;
import java.io.IOException;

public class GedcomSelectOptions extends GedcomOptions {
    public File file;
    public Expr expr;

    @Override
    public void help() {
        this.help = true;
        System.err.println("Usage: java -jar gedcom-select-all.jar [OPTION]... VALUES <in.ged");
        System.err.println("Extracts IDs from a GEDCOM file, based on tag=value list.");
        System.err.println("Options:");
        System.err.println("-w, --where          Tag path to match, ex.: .INDI.NAME");
        super.options();
    }

    public void w(final String expr) throws Expr.InvalidSyntax {
        where(expr);
    }

    public void where(final String expr) throws Expr.InvalidSyntax {
        this.expr = new Expr(expr);
    }

    public void __(final String file) throws IOException {
        this.file = new File(file);
        if (!this.file.canRead()) {
            throw new IllegalArgumentException("Cannot open file of values: " + this.file.getCanonicalPath());
        }
    }

    public GedcomSelectOptions verify() {
        if (this.help) {
            return this;
        }
        if (this.file == null) {
            throw new IllegalArgumentException("Missing required input file of values.");
        }
        if (this.expr == null) {
            throw new IllegalArgumentException("Missing required where clause.");
        }
        return this;
    }
}
