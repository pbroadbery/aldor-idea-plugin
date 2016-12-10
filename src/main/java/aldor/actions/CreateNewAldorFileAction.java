package aldor.actions;

import aldor.file.AldorFileType;
import com.intellij.ide.actions.CreateFileAction;
import org.jetbrains.annotations.Nullable;

public class CreateNewAldorFileAction extends CreateFileAction {

    public CreateNewAldorFileAction() {
        super("Aldor File",
                "Creates a new Aldor File",
                AldorFileType.INSTANCE.getIcon());
    }

    @Nullable
    @Override
    protected String getDefaultExtension() {
        return AldorFileType.INSTANCE.getDefaultExtension();
    }
}
