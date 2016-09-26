package aldor.psi;

import aldor.AldorFile;
import aldor.AldorFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public final class AldorElementFactory {

    @NotNull
    public static AldorFile createAldorFile(@NotNull Project project, @NonNls @NotNull CharSequence text) {
        @NonNls String filename = "dummy." + AldorFileType.INSTANCE.getDefaultExtension();
        return (AldorFile) PsiFileFactory.getInstance(project)
                .createFileFromText(filename, AldorFileType.INSTANCE, text);
    }

    @NotNull
    public static AldorIdentifier createIdentifier(@NotNull Project project, @NonNls @NotNull String text) {
        AldorFile file = createAldorFile(project, text);
        AldorId theId = PsiTreeUtil.findChildOfType(file, AldorId.class);
        if (theId == null) {
            throw new IllegalStateException("Failed to find identifier " + text);
        }
        return theId;
    }

}
