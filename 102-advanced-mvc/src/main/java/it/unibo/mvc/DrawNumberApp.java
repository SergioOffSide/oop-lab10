package it.unibo.mvc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * Application entry point for DrawNumber game.
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final String FILE_NAME = "config.yml";
    private static final String DELIMITER = ": ";

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * Constructor.
     *
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        int min = 0;
        int max = 100;
        int attempts = 10;
        try (
            BufferedReader config = new BufferedReader(
                new InputStreamReader(
                    Objects.requireNonNull(
                        ClassLoader.getSystemResourceAsStream(FILE_NAME),
                        "File not found"
                    ),
                    StandardCharsets.UTF_8
                )
            )
        ) {
            for (String line = config.readLine(); line != null; line = config.readLine()) {
                final StringTokenizer tokenizer = new StringTokenizer(line, DELIMITER);
                final String fieldName = tokenizer.nextToken();
                final int value = Integer.parseInt(tokenizer.nextToken());
                switch (fieldName) {
                    case "minimum":
                        min = value;
                        break;
                    case "maximum":
                        max = value;
                        break;
                    case "attempts":
                        attempts = value;
                        break;
                    default: break;
                }
            }
        } catch (final IOException e) {
            for (final DrawNumberView view : views) {
                view.displayError(e.getMessage());
            }
        }
        this.model = new DrawNumberImpl(min, max, attempts);
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (final IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    @SuppressFBWarnings(
        value = "DM_EXIT",
        justification = "Acceptable for exercising purposes."
    )
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * Application entry point.
     *
     * @throws FileNotFoundException if the configuration file cannot be fetched
     */
    public static void main() throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl());
    }

}
