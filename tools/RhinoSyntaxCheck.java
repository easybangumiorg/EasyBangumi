import org.mozilla.javascript.Context;

import java.io.FileReader;

public class RhinoSyntaxCheck {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: RhinoSyntaxCheck <file.js>");
        }
        Context context = Context.enter();
        try {
            context.setOptimizationLevel(-1);
            context.setLanguageVersion(Context.VERSION_ES6);
            try (FileReader reader = new FileReader(args[0])) {
                context.compileReader(reader, args[0], 1, null);
            }
        } finally {
            Context.exit();
        }
    }
}
