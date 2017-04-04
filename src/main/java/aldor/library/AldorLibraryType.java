package aldor.library;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.DummyLibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryType;
import com.intellij.openapi.roots.libraries.NewLibraryConfiguration;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import com.intellij.openapi.roots.libraries.ui.LibraryEditorComponent;
import com.intellij.openapi.roots.libraries.ui.LibraryPropertiesEditor;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class AldorLibraryType extends LibraryType<DummyLibraryProperties> {
    private static final PersistentLibraryKind<DummyLibraryProperties> LIBRARY_KIND = new PersistentLibraryKind<DummyLibraryProperties>("aldor") {
        @NotNull
        @Override
        public DummyLibraryProperties createDefaultProperties() {
            return DummyLibraryProperties.INSTANCE;
        }

    };

    protected AldorLibraryType() {
        super(LIBRARY_KIND);
    }

    @Nullable
    @Override
    public String getCreateActionName() {
        return "Create Aldor Library";
    }

    @Nullable
    @Override
    public NewLibraryConfiguration createNewLibrary(@NotNull JComponent parentComponent,
                                                    @Nullable VirtualFile contextDirectory,
                                                    @NotNull Project project) {
        return null;
    }

    @Nullable
    @Override
    public LibraryPropertiesEditor createPropertiesEditor(@NotNull LibraryEditorComponent<DummyLibraryProperties> editorComponent) {
        return null;
    }

}
