package aldor.build.facet;

import aldor.sdk.AxiomInstalledSdkType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import org.jetbrains.annotations.Nullable;

public final class ModuleModifyingFacetUtil {
    private ModuleModifyingFacetUtil() {}

    public static String getFacetLibraryName(AxiomInstalledSdkType type, String name) {
        return name + type.librarySuffix();
    }

    public static void updateLibrary(Module module, Sdk sdk, AxiomInstalledSdkType sdkType) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
            final ModifiableRootModel model = rootManager.getModifiableModel();
            boolean modelChanged = false;
            try {
                // Just remove all old facet libraries except one, that is necessary
                final @Nullable String name;
                if (sdk != null) {
                    name = getFacetLibraryName(sdkType, sdk.getName());
                } else {
                    name = null;
                }
                boolean librarySeen = false;
                for (OrderEntry entry : model.getOrderEntries()) {
                    if (entry instanceof LibraryOrderEntry) {
                        final String libraryName = ((LibraryOrderEntry) entry).getLibraryName();
                        if ((name != null) && name.equals(libraryName)) {
                            librarySeen = true;
                            continue;
                        }
                        if ((libraryName != null) && libraryName.endsWith(sdkType.librarySuffix())) {
                            model.removeOrderEntry(entry);
                            modelChanged = true;
                        }
                    }
                }
                if (name != null) {
                    final ModifiableModelsProvider provider = ModifiableModelsProvider.getInstance();
                    final LibraryTable.ModifiableModel libraryTableModifiableModel = provider.getLibraryTableModifiableModel();
                    Library library = libraryTableModifiableModel.getLibraryByName(name);
                    provider.disposeLibraryTableModifiableModel(libraryTableModifiableModel);
                    if (library == null) {
                        // we just create new project library
                        library = AldorSdkTableListener.addLibrary(sdk);
                    }
                    if (!librarySeen) {
                        model.addLibraryEntry(library);
                        modelChanged = true;
                    }
                }
            } finally {
                if (modelChanged) {
                    model.commit();
                } else {
                    model.dispose();
                }
            }
        });
    }

    public static void removeLibrary(AxiomInstalledSdkType sdkType, Module module) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
            final ModifiableRootModel model = rootManager.getModifiableModel();
            boolean changed = false;
            // Just remove all old facet libraries
            for (OrderEntry entry : model.getOrderEntries()) {
                if (entry instanceof LibraryOrderEntry) {
                    final Library library = ((LibraryOrderEntry) entry).getLibrary();
                    if (library != null) {
                        final String libraryName = library.getName();
                        if ((libraryName != null) && libraryName.endsWith(sdkType.librarySuffix())) {
                            model.removeOrderEntry(entry);
                            changed = true;
                        }
                    }
                }
            }
            if (changed) {
                model.commit();
            }
            else {
                model.dispose();
            }

        });
    }

}
