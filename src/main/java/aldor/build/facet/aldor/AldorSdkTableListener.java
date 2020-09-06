package aldor.build.facet.aldor;

import aldor.sdk.AxiomInstalledSdk;
import aldor.sdk.aldor.AldorInstalledSdkType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import org.jetbrains.annotations.NotNull;

public class AldorSdkTableListener implements ProjectJdkTable.Listener {
  private static final Logger LOG = Logger.getInstance(AldorSdkTableListener.class);

  @Override
  public void jdkAdded(@NotNull final Sdk sdk) {
    if (sdk.getSdkType() instanceof AldorInstalledSdkType) {
      ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().runWriteAction(() -> {
        addLibrary(sdk);
      }));
    }
  }

  @Override
  public void jdkRemoved(@NotNull final Sdk sdk) {
    if (sdk.getSdkType() instanceof AxiomInstalledSdk) {
      removeLibrary(sdk);
    }
  }

  @Override
  public void jdkNameChanged(@NotNull final Sdk sdk, @NotNull final String previousName) {
    if (sdk.getSdkType() instanceof AldorInstalledSdkType) {
      renameLibrary(sdk, previousName);
    }
  }

  static Library addLibrary(Sdk sdk) {
    LOG.info("Adding library " + sdk.getName());
    if (!(sdk.getSdkType() instanceof AxiomInstalledSdk)) {
      throw new IllegalArgumentException("sdk must be an installed Aldor/Fricas Sdk");
    }
    final LibraryTable.ModifiableModel libraryTableModel = ModifiableModelsProvider.SERVICE.getInstance().getLibraryTableModifiableModel();
    final Library library = libraryTableModel.createLibrary(AldorFacetUtil.getFacetLibraryName((AxiomInstalledSdk) sdk.getSdkType(), sdk.getName()));
    final Library.ModifiableModel model = library.getModifiableModel();
    for (String url : sdk.getRootProvider().getUrls(OrderRootType.CLASSES)) {
      model.addRoot(url, OrderRootType.CLASSES);
      model.addRoot(url, OrderRootType.SOURCES);
    }
    model.commit();
    libraryTableModel.commit();
    return library;
  }

  private static void removeLibrary(final Sdk sdk) {
    ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().runWriteAction(() -> {
      AxiomInstalledSdk sdkType = (AxiomInstalledSdk) sdk.getSdkType();
      final LibraryTable.ModifiableModel libraryTableModel =
        ModifiableModelsProvider.SERVICE.getInstance().getLibraryTableModifiableModel();
      final Library library = libraryTableModel.getLibraryByName(AldorFacetUtil.getFacetLibraryName(sdkType, sdk.getName()));
      if (library != null) {
        libraryTableModel.removeLibrary(library);
      }
      libraryTableModel.commit();
    }), ModalityState.NON_MODAL);
  }

  private static void renameLibrary(final Sdk sdk, final String previousName) {
    ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().runWriteAction(() -> {
      AxiomInstalledSdk sdkType = (AxiomInstalledSdk) sdk.getSdkType();
      final LibraryTable.ModifiableModel libraryTableModel =
        ModifiableModelsProvider.SERVICE.getInstance().getLibraryTableModifiableModel();
      final Library library = libraryTableModel.getLibraryByName(AldorFacetUtil.getFacetLibraryName(sdkType, previousName));
      if (library != null) {
        final Library.ModifiableModel model = library.getModifiableModel();
        model.setName(AldorFacetUtil.getFacetLibraryName(sdkType, sdk.getName()));
        model.commit();
      }
      libraryTableModel.commit();
    }), ModalityState.NON_MODAL);
  }
}
