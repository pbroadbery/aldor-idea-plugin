package aldor.syntax.components;

import aldor.psi.AldorQuotedIds;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxVisitor;

import java.util.List;

public class EnumList extends SyntaxNode<AldorQuotedIds> {

    public EnumList(AldorQuotedIds ids, List<Syntax> arguments) {
        super(ids, arguments);
    }

    @Override
    public String name() {
        return "Enum";
    }

    @Override
    public <T> T accept(SyntaxVisitor<T> syntaxVisitor) {
        return syntaxVisitor.visitEnumList(this);
    }

}
