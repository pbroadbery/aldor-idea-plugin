package aldor.psi.stub.impl;

import aldor.psi.AldorDefine;
import aldor.psi.elements.AldorDefineInfo;
import aldor.psi.stub.AldorDefineStub;
import aldor.syntax.Syntax;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.Nullable;

public class AldorDefineConcreteStub extends StubBase<AldorDefine> implements AldorDefineStub {
    @Nullable
    private final Syntax syntax;
    private final String defineId;
    private final AldorDefineInfo defineInfo;

    public AldorDefineConcreteStub(StubElement<?> parent,
                                   IStubElementType<AldorDefineStub, AldorDefine> type,
                                   String defineId, AldorDefineInfo defineInfo) {
        super(parent, type);
        syntax = null; // TODO: This one will be tricky
        this.defineId = defineId;
        this.defineInfo = defineInfo;
    }

    @Override
    public String defineId() {
        return defineId;
    }

    @Override
    public Syntax syntax() {
        return syntax;
    }

    @Override
    public AldorDefineInfo defineInfo() {
        return defineInfo;
    }

}
