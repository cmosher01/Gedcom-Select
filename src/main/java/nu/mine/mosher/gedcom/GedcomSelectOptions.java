package nu.mine.mosher.gedcom;

import joptsimple.OptionParser;
import joptsimple.OptionSpec;

import java.io.File;

import static java.util.Arrays.asList;

public class GedcomSelectOptions extends GedcomOptions {
    private final OptionSpec<Expr> expr;
    private final OptionSpec<File> file;

    public GedcomSelectOptions(final OptionParser parser) {
        super(parser);
        this.expr = parser.acceptsAll(asList("e","expr"),"tag path to match, ex.: .INDI.NAME").withRequiredArg().required().ofType(Expr.class).describedAs("EXPR");
        this.file = parser.nonOptions("file of values").ofType(File.class).describedAs("VALUES");
    }

    public Expr expr() {
        return this.expr.value(get());
    }

    public File file() {
        return this.file.value(get());
    }
}
