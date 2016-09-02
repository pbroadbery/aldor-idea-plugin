package aldor;

import com.intellij.lexer.FlexAdapter;

import java.io.Reader;
import java.util.Arrays;

public class AldorLexerAdapter extends FlexAdapter {
    public AldorLexerAdapter() {
        super(new AldorLexer(null));
    }

    public AldorLexerAdapter(Reader reader) {
        super(new AldorLexer(reader));
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
}
