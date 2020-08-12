package edu.pdx.cs;

import org.fusesource.jansi.AnsiConsole;

import java.io.Closeable;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import static org.fusesource.jansi.Ansi.Color;
import static org.fusesource.jansi.Ansi.ansi;

public class StatusBar implements Closeable {
    public final PrintStream out;

    private StatusBar(PrintStream out) {
        this.out = out;
    }

    public static StatusBar create(PrintStream out) {
        AnsiConsole.systemInstall();
        return new StatusBar(out);
    }

    //TODO: parse the user terminal to determine if it can render ANSI codes, some Windows terminals can
    // but until we know which, best to strip them all out on Windows.
    public void render(ClientState state) {
        // Space things out a bit.
        out.println();

        // Hack for Windows failure to parse ANSI codes
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            renderWindows(state);
        else
            renderOtherOS(state);
    }

    /**
     * Stripped down StatusBar formatted for Windows terminals that can't handle ANSI
     * @param state
     */
    private void renderWindows(ClientState state)
    {
        // Print top of statusbar and time.
        out.println("╔ " + LocalDateTime.now().format(
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)));

        // Print command output
        for (String line : state.getCommandOutput()) {
            out.println("║ " + line);
        }

        // Clear the printed output
        state.clearCommandOutput();

        // print the status bar
        out.println("╠ [local] " + formatStatusBar(state) + " [remote] ╗");

        // print the command prompt
        out.print(ansi().fgCyan().a("╚").reset().a(" mechaftp > ").reset());

    }

    /**
     * StatusBar output suited for non-Windows terminals that can handle ANSI formatting (prettier)
     * @param state
     */
    private void renderOtherOS(ClientState state)
    {
        // Print top of statusbar and time.
        out.println(ansi()
            .fgCyan().a("╔ ")
            .fgYellow()
            .a(LocalDateTime.now()
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT))));

        // Print command output
        for (String line : state.getCommandOutput()) {
            out.println(ansi()
                .fgCyan().a("║ ")
                .fg(Color.WHITE).a(line));
        }

        // Clear the printed output
        state.clearCommandOutput();

        // print the status bar
        out.println(ansi()
            .fg(Color.CYAN).a("╠ ")
            .fg(Color.GREEN).a("[local] ")
            .fg(Color.WHITE).a(formatStatusBar(state))
            .fg(Color.GREEN).a(" [remote] ")
            .fg(Color.CYAN).a("╗")
            .reset()
        );

        // print the command prompt
        out.print(ansi().fgCyan().a("╚").reset().a(" mechaftp > ").reset());
    }

    /**
     * Returns a formatted string cotaining the printed local and remote working
     * directories, set at 80 characters long
     * @param state Current state of the client
     * @return a string formatted for display of the working directories for the
     * local and remote machines
     */
    private String formatStatusBar(ClientState state)
    {
        int localLen = state.getLocalCwdString().length();
        int remoteLen = state.getRemoteCwdString().length();

        String localCwdString = localLen > 35 ?
            "..." + state.getLocalCwdString().substring(localLen - 32) :
            state.getLocalCwd().toString();

        String remoteCwdString = remoteLen > 33 ?
            "..." + state.getRemoteCwdString().substring(remoteLen - 30) :
            state.getRemoteCwdString();

        return String.format("%1$-38s  %2$38s", localCwdString, remoteCwdString);
    }

    /**
     * Closes out the AnsiConsole
     */
    public void close() {
        AnsiConsole.systemUninstall();
    }
}
