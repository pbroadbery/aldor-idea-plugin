package aldor.psi.elements;

import aldor.language.AldorLanguage;
import aldor.psi.AldorWhereBlock;
import aldor.psi.stub.AldorWhereStub;

public class AldorWhereElementType extends StubCodecElementType.NoIndexElementType<AldorWhereStub, AldorWhereBlock> {
    public AldorWhereElementType(String where, PsiStubCodec<AldorWhereStub, AldorWhereBlock> whereCodec) {
        super(where, AldorLanguage.INSTANCE, whereCodec);
    }

}
