package aldor.build.module;

import aldor.annotations.AnnotationFileNavigatorManager;
import aldor.annotations.DefaultAnnotationFileNavigator;
import aldor.annotations.SaveActionProcessor;
import aldor.editor.navigation.DefaultNavigator;
import aldor.parser.NavigatorFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import static com.intellij.AppTopics.FILE_DOCUMENT_SYNC;

/**
 * Component that looks for aldor file updates, and if it spots them notifies the annotation manager.
 */
public class AldorApplicationComponent implements ApplicationComponent {
    private static final Logger LOG = Logger.getInstance(AldorApplicationComponent.class);
    private static final String NAME = "AldorFileWatcher";

    @Override
    public void initComponent() {
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();
        MessageBusConnection connection = bus.connect();
        connection.subscribe(FILE_DOCUMENT_SYNC, new SaveActionProcessor());

        initialiseComponents();
    }

    private void initialiseComponents() {
        NavigatorFactory.registerDefaultNavigator(new DefaultNavigator());
        AnnotationFileNavigatorManager.register(DefaultAnnotationFileNavigator::factory);
    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return NAME;
    }
}
