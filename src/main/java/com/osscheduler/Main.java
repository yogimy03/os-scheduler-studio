package com.osscheduler;

import com.osscheduler.cli.Cli;
import com.osscheduler.ui.Launcher;

/**
 * The single entry point for the whole program.
 *
 * <p>Run it with no arguments (or {@code gui}) to open the desktop app. Run it
 * with a command such as {@code demo}, {@code cpu} or {@code disk} to use the
 * command line tool instead. Keeping both behind one class means there is only
 * one thing to launch.</p>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("gui")) {
            String[] rest = new String[args.length - 1];
            System.arraycopy(args, 1, rest, 0, rest.length);
            Launcher.main(rest);
            return;
        }

        if (args.length == 0) {
            // No command was given, so fall back to the command line help.
            // The desktop app is best launched with: mvn javafx:run
            new Cli(System.out, System.err).run(new String[] {"help"});
            return;
        }

        int exitCode = new Cli(System.out, System.err).run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
