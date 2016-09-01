package aldor.list;

import com.intellij.lexer.FlexAdapter;

import java.io.Reader;

public class ListLexerAdapter extends FlexAdapter {
  public ListLexerAdapter() {
    super(new ListLexer(null));
  }
  public ListLexerAdapter(Reader reader) { super(new ListLexer(reader));}
}
