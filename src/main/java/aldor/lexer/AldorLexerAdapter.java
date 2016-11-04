package aldor.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.FlexLexer;

import java.io.Reader;
import java.util.Arrays;

import static aldor.lexer.LexMode.Aldor;

public class AldorLexerAdapter extends FlexAdapter {
    private final LexMode mode;

    public AldorLexerAdapter() {
        this(Aldor, null);
    }

    public AldorLexerAdapter(Reader reader) {
        this(Aldor, reader);
    }

    public AldorLexerAdapter(LexMode mode, Reader reader) {
        super(createLexer(mode, reader));
        this.mode = mode;
    }

    private static FlexLexer createLexer(LexMode mode, Reader reader) {
        AldorLexer lexer = new AldorLexer(reader);
        lexer.lexMode(mode);
        return lexer;
    }


    @Override
    public void advance() {
        try {
            super.advance();
        } catch (RuntimeException e) {
            //noinspection ProhibitedExceptionThrown
            throw new RuntimeException("Failed to advance: " + this.getBufferSequence(), e);
        }
    }

    @Override
    protected void locateToken() {
        try {
            super.locateToken();
        } catch (Error e) {
            System.out.println("error: " + Arrays.asList(e.getStackTrace()));
            //noinspection ProhibitedExceptionThrown
            throw e;
        }
        catch (RuntimeException e) {
            //noinspection ProhibitedExceptionThrown
            throw new RuntimeException("Locate failed: " + this.getBufferSequence(), e);
        }
    }

    public static AldorLexerAdapter createAndStart(CharSequence text) {
        AldorLexerAdapter lla = new AldorLexerAdapter(null);
        lla.start(text);
        return lla;
    }

    public LexMode mode() {
        return mode;
    }

}
