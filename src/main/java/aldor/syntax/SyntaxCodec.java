package aldor.syntax;

import aldor.lexer.AldorTokenType;
import aldor.lexer.AldorTokenTypes;
import aldor.syntax.components.AldorDeclare;
import aldor.syntax.components.Apply;
import aldor.syntax.components.Comma;
import aldor.syntax.components.Define;
import aldor.syntax.components.Id;
import aldor.syntax.components.InfixedId;
import aldor.syntax.components.OtherSx;
import aldor.syntax.components.SpadDeclare;
import aldor.syntax.components.SyntaxNode;
import aldor.util.StubCodec;
import aldor.util.sexpr.SExpression;
import com.google.common.collect.Lists;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class SyntaxCodec implements StubCodec<Syntax> {
    private final Map<Class<? extends Syntax>, StubCodec<? extends Syntax>> codecForClassMap = new HashMap<>();
    private final Map<String, StubCodec<? extends Syntax>> codecForNameMap = new HashMap<>();

    public SyntaxCodec() {
        List<StubCodec<? extends Syntax>> codecs = Lists.newArrayList();

        codecs.add(new IdCodec());
        codecs.add(new InfixedIdCodec());
        codecs.add(new SyntaxNodeCodec<>(Apply.class, Apply::new));
        codecs.add(new SyntaxNodeCodec<>(AldorDeclare.class, AldorDeclare::new));
        codecs.add(new SyntaxNodeCodec<>(SpadDeclare.class, SpadDeclare::new));
        codecs.add(new SyntaxNodeCodec<>(Define.class, Define::new));
        codecs.add(new SyntaxNodeCodec<>(Comma.class, Comma::new));

        for (StubCodec<? extends Syntax> codec: codecs) {
            codecForClassMap.put(codec.clzz(), codec);
        }

        for (StubCodec<? extends Syntax> codec: codecs) {
            codecForNameMap.put(codec.clzz().getSimpleName(), codec);
        }
    }

    @NotNull
    @Override
    public Class<Syntax> clzz() {
        return Syntax.class;
    }

    @Override
    public void encode(StubOutputStream stream, Syntax syntax) throws IOException {
        encodeSafely(stream, syntax);
    }

    private <T extends Syntax> void encodeSafely(StubOutputStream stream, T syntax) throws IOException {
        //noinspection unchecked
        StubCodec<T> codec = ((StubCodec<T>) codecForClassMap.get(syntax.getClass()));
        if (codec == null) {
            stream.writeName("Missing");
            stream.writeName(syntax.getClass().getName());
        }
        else {
            stream.writeName(codec.clzz().getSimpleName());
            codec.encode(stream, syntax);
        }

    }

    @Override
    public Syntax decode(StubInputStream stream) throws IOException {
        StringRef name = stream.readName();
        if (name == null) {
            throw new IOException("Missing name");
        }
        else if (Objects.equals(name.getString(), "Missing")) {
            StringRef classNameRef = stream.readName();
            String className = Optional.ofNullable(classNameRef).map(StringRef::getString).orElse("[Missing reference]");
            return new OtherSx(SExpression.string("Missing " + className));
        }
        else {
            StubCodec<? extends Syntax> codec = codecForNameMap.get(name.getString());
            if (codec == null) {
                throw new IOException("Missing codec: " + name);
            }
            return codec.decode(stream);
        }
    }

    private final class IdCodec implements StubCodec<Id> {

        @NotNull
        @Override
        public Class<Id> clzz() {
            return Id.class;
        }

        @Override
        public void encode(StubOutputStream stream, Id id) throws IOException {
            stream.writeName(id.symbol());
            AldorTokenType tokenType = id.tokenType();
            if (tokenType == null) {
                stream.writeBoolean(false);
            }
            else {
                stream.writeBoolean(true);
                stream.writeName(tokenType.name());
            }
        }

        @Override
        public Id decode(StubInputStream stream) throws IOException {
            StringRef ref = stream.readName();
            if (ref == null) {
                throw new IOException("Missing reference");
            }
            String name = ref.getString();
            boolean present = stream.readBoolean();
            if (!present) {
                return Id.createMissingId(null, name);
            }
            else {
                StringRef tokenTypeRef = stream.readName();
                if (tokenTypeRef == null) {
                    throw new IOException("Unknown name in file");
                }
                String tokenTypeName = tokenTypeRef.getString();

                AldorTokenType tokenType = (AldorTokenType) AldorTokenTypes.createTokenType(tokenTypeName);
                return Id.createMissingId(tokenType, name);
            }
        }
    }

    private final class InfixedIdCodec implements StubCodec<InfixedId> {

        @NotNull
        @Override
        public Class<InfixedId> clzz() {
            return InfixedId.class;
        }

        @Override
        public void encode(StubOutputStream stream, InfixedId id) throws IOException {
            stream.writeName(id.symbol());
        }

        @Override
        public InfixedId decode(StubInputStream stream) throws IOException {
            StringRef ref = stream.readName();
            if (ref == null) {
                throw new IOException("Missing reference");
            }
            String name = ref.getString();
            return InfixedId.createMissingId(name);
        }
    }


    private final class SyntaxNodeCodec<T extends SyntaxNode<?>> implements StubCodec<T> {
        private final Function<List<Syntax>, T> constructor;
        private final Class<T> clzz;

        private SyntaxNodeCodec(Class<T> clzz, Function<List<Syntax>, T> constructor) {
            this.constructor = constructor;
            this.clzz = clzz;
        }

        @NotNull
        @Override
        public Class<T> clzz() {
            return clzz;
        }

        @Override
        public void encode(StubOutputStream stream, T syntaxNode) throws IOException {
            stream.writeVarInt(syntaxNode.children().size());
            for (Syntax syntax: syntaxNode.children()) {
                SyntaxCodec.this.encode(stream, syntax);
            }
        }

        @Override
        public T decode(StubInputStream stream) throws IOException {
            int size = stream.readVarInt();
            List<Syntax> children = new ArrayList<>();
            for (int i=0; i<size; i++) {
                children.add(SyntaxCodec.this.decode(stream));
            }
            return constructor.apply(children);
        }
    }
}
