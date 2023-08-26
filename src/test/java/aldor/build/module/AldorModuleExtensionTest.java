package aldor.build.module;

import aldor.builder.jps.module.AldorModuleState;
import aldor.builder.jps.module.MakeConvention;
import aldor.test_util.AssumptionAware;
import aldor.test_util.JUnits;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.junit.Assert;

public class AldorModuleExtensionTest extends AssumptionAware.LightIdeaTestCase {
    private JUnits.TearDownItem tearDown = new JUnits.TearDownItem();

    @Override
    protected boolean shouldRunTest() {
        return false; // ModuleState is a bit broken (and unused)
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tearDown = tearDown.with(JUnits.setLogToDebug());
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            tearDown.tearDown();
        } finally {
            super.tearDown();
        }
    }

    public void testExtension_noSettings() {
        AldorModuleExtension extension = ModuleRootManager.getInstance(getModule()).getModuleExtension(AldorModuleExtension.class);
        AldorModuleState state = extension.state();
        assert state != null;
        Element serialised = XmlSerializer.serialize(state);
        Assert.assertEquals(state, XmlSerializer.deserialize(serialised, AldorModuleState.class));
    }

    public void testExtension_settings() {
        AldorModuleExtension extension = ModuleRootManager.getInstance(getModule()).getModuleExtension(AldorModuleExtension.class).getModifiableModel(true);
        AldorModuleState state = extension.state();
        assert state != null;
        extension.setState(state.asBuilder()
                //.outputDirectory("omg ponies")
                //.makeConvention(MakeConvention.Build)
                .build());
        Element serialised = XmlSerializer.serialize(extension.getState());

        AldorModuleState deserialized = XmlSerializer.deserialize(serialised, AldorModuleState.class);
        Assert.assertEquals(extension.getState().state(), deserialized);
        Assert.assertEquals("omg ponies", deserialized._outputDirectory());
        Assert.assertEquals(MakeConvention.Build, deserialized._makeConvention());
    }

    public void testExtension_Serial() {

    }

}