package aldor.syntax.components;

import aldor.psi.AldorDefine;
import aldor.syntax.Syntax;

import java.util.List;

public class Define extends SyntaxNode<AldorDefine> {


    public Define(AldorDefine element, List<Syntax> arguments) {
        super(element, arguments);
    }

    @Override
    public String name() {
        return "Define";
    }
}
