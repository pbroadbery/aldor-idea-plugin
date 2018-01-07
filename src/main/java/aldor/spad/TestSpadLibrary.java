package aldor.spad;

import aldor.lexer.AldorTokenType;
import aldor.lexer.AldorTokenTypes;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.index.AldorDefineTopLevelIndex;
import aldor.syntax.Syntax;
import aldor.syntax.components.Apply;
import aldor.syntax.components.Comma;
import aldor.syntax.components.Id;
import aldor.syntax.components.SyntaxRepresentation;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static aldor.spad.TestSpadLibrary.SyntaxUtils.apply;
import static aldor.spad.TestSpadLibrary.SyntaxUtils.comma;
import static aldor.spad.TestSpadLibrary.SyntaxUtils.id;
import static aldor.spad.TestSpadLibrary.SyntaxUtils.map;


public class TestSpadLibrary implements SpadLibrary {
    private final VirtualFile path; // Probably should be a search scope - or maybe a module..
    private final Project project;
    private final Supplier<GlobalSearchScope> scope;
    private final Map<String, ParentInfo> operationsForType = new HashMap<>();
    private final ParentInfo noParentInfo = new ParentInfo(s -> Collections.emptyList(), s -> Collections.emptyList());

    TestSpadLibrary(@NotNull Project project, @Nullable Module module, VirtualFile path) {
        this.path = path;
        this.project = project;
        this.scope = () -> GlobalSearchScope.allScope(project);
        operationsForType.put("List", new ParentInfo(this::listParents, this::listOperations));
        operationsForType.put("Integer", new ParentInfo(this::integerParents, this::integerOperations));
    }

    @Override
    public List<Syntax> parentCategories(Syntax syntax) {
        return parentInfo(syntax).parentsForType(syntax);
    }

    @Override
    public List<Operation> operations(Syntax syntax) {
        return parentInfo(syntax).operationsForType(syntax);
    }

    @NotNull
    @Override
    public Syntax normalise(@NotNull Syntax syntax) {
        if (syntax.is(Id.class) && "List".equals(syntax.as(Id.class).symbol())) {
            return apply(syntax, id("#1"));
        }
        return syntax;
    }

    private ParentInfo parentInfo(Syntax syntax) {
        Syntax discriminator = syntax;
        if (syntax.is(Apply.class)) {
            discriminator = syntax.as(Apply.class).operator();
        }
        if (!discriminator.is(Id.class)) {
            return noParentInfo;
        }
        Id id = discriminator.as(Id.class);
        return operationsForType.getOrDefault(id.symbol(), noParentInfo);
    }


    List<Syntax> integerParents(Syntax unused) {
        return Arrays.asList(topLevel("BasicType"), topLevel("Ring"));
    }

    List<Syntax> listParents(Syntax arg) {
        Syntax param = (arg.is(Apply.class)) ? arg.as(Apply.class).arguments().get(0) : null;
        if (param == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(
                apply(topLevel("ListAggregate"), param),
                topLevel("BasicType")
        );
    }

    List<Operation> integerOperations(Syntax arg) {
        Syntax self = id("%");
        return Arrays.asList(
                new Operation("+", map(comma(self, self), arg), null, apply(topLevel("Ring"), arg)),
                new Operation("=", map(comma(arg, self), self), null, topLevel("BasicType"))
        );
    }

    List<Operation> listOperations(Syntax arg) {
        Syntax self = id("%");
        return Arrays.asList(
            new Operation("first", map(self, arg), null, apply(topLevel("List"), arg)),
            new Operation("rest",  map(self, self), null, apply(topLevel("List"), arg)),
            new Operation("cons",  map(comma(arg, self), self), null, apply(topLevel("List"), arg)),
            new Operation("=",     map(comma(arg, self), self), null, topLevel("BasicType"))
        );
    }

    public Id topLevel(String id) {
        return new Id(new SyntaxRepresentation<AldorIdentifier>() {
            @Nullable
            @Override
            public AldorIdentifier element() {
                Collection<AldorDefine> defs = AldorDefineTopLevelIndex.instance.get(id, project, scope.get());
                return defs.stream().findFirst().flatMap(AldorDefine::defineIdentifier).orElse(null);
            }

            @Nullable
            @Override
            public AldorTokenType tokenType() {
                return AldorTokenTypes.TK_Id;
            }

            @Override
            public String text() {
                return Optional.ofNullable(element()).map(AldorIdentifier::getText).orElse(id);
            }
        });
    }

    public static final class SyntaxUtils {
        static Syntax apply(Syntax op, Syntax... args) {
            List<Syntax> all = new ArrayList<>();
            all.add(op);
            all.addAll(Arrays.asList(args));
            return new Apply(all);
        }

        static Syntax id(String id) {
            return Id.createMissingId(AldorTokenTypes.TK_Id, id);
        }

        static Syntax comma(Syntax ... syntax) {
            return new Comma(Arrays.asList(syntax));
        }

        static Syntax map(Syntax arg, Syntax ret) {
            return new Apply(Arrays.asList(Id.createMissingId(AldorTokenTypes.KW_MArrow, "->"), arg, ret));
        }
    }

    public static class ParentInfo {
        private final Function<Syntax, List<Syntax>> parentsForType;
        private final Function<Syntax, List<Operation>> opsForType;

        ParentInfo(Function<Syntax, List<Syntax>> parentsForType, Function<Syntax, List<Operation>> opsForType) {
            this.parentsForType = parentsForType;
            this.opsForType = opsForType;
        }

        List<Syntax> parentsForType(Syntax syntax) {
            return parentsForType.apply(syntax);
        }

        List<Operation> operationsForType(Syntax syntax) {
            return opsForType.apply(syntax);
        }
    }

}
