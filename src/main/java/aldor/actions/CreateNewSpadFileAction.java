package aldor.actions;

import aldor.file.SpadFileType;
import com.intellij.ide.actions.CreateFileAction;
import org.jetbrains.annotations.Nullable;

public class CreateNewSpadFileAction extends CreateFileAction {
    public CreateNewSpadFileAction() {
        super("SPAD File",
                "Creates a new SPAD File",
                SpadFileType.INSTANCE.getIcon());
    }

    @Nullable
    @Override
    protected String getDefaultExtension() {
        return SpadFileType.INSTANCE.getDefaultExtension();
    }
}
