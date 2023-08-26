package aldor.module.template.detect;

import aldor.build.facet.aldor.AldorFacet;
import aldor.build.facet.cfgroot.ConfigRootFacetType;
import aldor.builder.jps.module.MakeConvention;
import aldor.file.AldorFileType;
import aldor.spad.SpadLibrary;
import aldor.spad.SpadLibraryManager;
import aldor.syntax.components.Id;
import aldor.test_util.AssumptionAware;
import aldor.test_util.ExecutablePresentRule;
import aldor.util.Streams;
import com.google.common.collect.MoreCollectors;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleSourceOrderEntry;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.TestLoggerFactory;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Assume;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class AldorRepoProjectDetectorTest extends AssumptionAware.ImportFromSourcesTestCase {
    private static final Logger LOG = Logger.getInstance(AldorRepoProjectDetectorTest.class);
    private VirtualFile vpath;

    @SuppressWarnings("LoggerInitializedWithForeignClass")
    @Override
    public void setUp() throws Exception {
        super.setUp();

       //withSafeTearDown(JUnits.setLogToInfo());
        LOG.info("Starting " + this.getClass().getCanonicalName());
        Assume.assumeTrue(ExecutablePresentRule.AldorDev.INSTANCE.shouldRunTest());
        vpath = cloneRepository(ExecutablePresentRule.AldorDev.INSTANCE.repoRoot());
    }

    @Override
    public void tearDown() throws Exception {
        try {
            TestLoggerFactory.dumpLogToStdout(this.getClass().getCanonicalName());
            EdtTestUtil.runInEdtAndWait(JavaAwareProjectJdkTableImpl::removeInternalJdkInTests);
        } finally {
            super.tearDown();
        }
    }

    public void testModuleStructure() {
        importFromSources(new File(vpath.getPath()));
        Module module = ModuleUtil.findModuleForFile(vpath.findFileByRelativePath("aldor/aldor"), getProject());
        Assert.assertNotNull(module);
        FacetManager facetManager = FacetManager.getInstance(module);
        var facet = facetManager.getFacetByType(ConfigRootFacetType.TYPE_ID);
        var state = Objects.requireNonNull(facet).getConfiguration().getState();
        Assert.assertTrue(Objects.requireNonNull(state).isDefined());

        VirtualFile @NotNull [] roots = ModuleRootManager.getInstance(module).getContentRoots();
        Assert.assertEquals(1, roots.length);
        Assert.assertEquals(Objects.requireNonNull(vpath.findFileByRelativePath("aldor/aldor")).getCanonicalFile(), roots[0].getCanonicalFile());

        Module libModule = ModuleUtilCore.findModuleForFile(Objects.requireNonNull(vpath.findFileByRelativePath("aldor/aldor/lib/algebra")), getProject());
        AldorFacet libFacet = AldorFacet.forModule(libModule);
        Assert.assertEquals("lib/algebra", libFacet.getProperties().get().relativeOutputDirectory());
        Assert.assertEquals(MakeConvention.Configured, libFacet.getProperties().get().makeConvention());
        Assert.assertTrue(libFacet.getProperties().get().buildJavaComponents());

        OrderEntry[] ents = ModuleRootManager.getInstance(module).getOrderEntries();
        Assert.assertFalse(ents.length == 0);
        ModuleSourceOrderEntry ent = Arrays.stream(ents).flatMap(Streams.filterAndCast(ModuleSourceOrderEntry.class)).collect(MoreCollectors.onlyElement());
        Assert.assertEquals("aldor", ent.getRootModel().getContentRoots()[0].getName());
    }

    public void testSpadLibrary() {
        importFromSources(new File(vpath.getPath()));

        Module libModule = ModuleUtilCore.findModuleForFile(Objects.requireNonNull(vpath.findFileByRelativePath("aldor/aldor/lib/algebra")), getProject());
        Assert.assertNotNull(libModule);
        SpadLibrary lib = SpadLibraryManager.getInstance(getProject()).forModule(libModule, AldorFileType.INSTANCE);
        Assert.assertNotNull(lib);
        Assert.assertEquals("sit_field.as", lib.definingFile(Id.createImplicitId("Field")));
    }

    private VirtualFile cloneRepository(String dirToClone) {
        LOG.info("Cloning ");
        VirtualFile vpath = getTempDir().createVirtualDir();
        Git git = Git.getInstance();
        GitLineHandler handler = new GitLineHandler(null, vpath, GitCommand.CLONE);
        handler.addLineListener( (l,type) -> LOG.info("Line: "+ type + "--> " + l));
        handler.addParameters(dirToClone);

        GitCommandResult result = git.runCommand(handler);
        LOG.info("Result " + result);
        return vpath;
    }


}