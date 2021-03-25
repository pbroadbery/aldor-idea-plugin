package aldor.runconfiguration.spad;

import aldor.runconfiguration.spad.SpadInputRunConfigurationType.SpadInputConfigurationBean;
import aldor.test_util.DirectoryPresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.Swings;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextPane;

public class SpadInputConfigurableTest extends BasePlatformTestCase {
    @Rule
    public final DirectoryPresentRule directory = new DirectoryPresentRule("/home/pab/Work/fricas/opt/lib/fricas/target/x86_64-linux-gnu");

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Assume.assumeTrue(directory.isPresent());
    }

    public void testInputConfigurationRead() {
        String text = "/home/pab/foo.input";
        SpadInputConfigurable configurable = new SpadInputConfigurable(getProject(), getModule());
        @NotNull SpadInputConfigurableForm form = configurable.form();
        JComponent component = configurable.createEditor();
        TextFieldWithBrowseButton inputFileField = Swings.findChildComponent(component, Swings.byName("myInputFile"), TextFieldWithBrowseButton.class).orElseGet(JUnits::fail);
        JCheckBox keepProcess = Swings.findChildComponent(component, Swings.byName("keepProcess"), JCheckBox.class).orElseGet(JUnits::fail);
        SpadInputConfigurationBean bean = new SpadInputConfigurationBean();

        inputFileField.setText(text);
        keepProcess.setSelected(true);

        form.updateConfiguration(bean);

        Assert.assertEquals(text, bean.inputFile);
        Assert.assertTrue(bean.keepRunning);
    }

    public void testInputConfigurationWrite() {
        String text = "/home/pab/foo.input";
        SpadInputConfigurable configurable = new SpadInputConfigurable(getProject(), getModule());
        @NotNull SpadInputConfigurableForm form = configurable.form();
        JComponent component = configurable.createEditor();
        TextFieldWithBrowseButton textField = Swings.findChildComponent(component, Swings.byName("myInputFile"), TextFieldWithBrowseButton.class).orElseGet(JUnits::fail);
        JCheckBox keepProcess = Swings.findChildComponent(component, Swings.byName("keepProcess"), JCheckBox.class).orElseGet(JUnits::fail);
        JTextPane pane = Swings.findChildComponent(component, Swings.byName("fricasCommand"), JTextPane.class).orElseGet(JUnits::fail);
        SpadInputConfigurationBean bean = new SpadInputConfigurationBean();

        bean.inputFile = text;
        bean.keepRunning = true;

        form.resetEditor(bean);

        Assert.assertEquals(text, textField.getText());
        Assert.assertTrue(keepProcess.isSelected());
        Assert.assertEquals("$FRICAS -eval \")r /home/pab/foo.input\"", pane.getText());

        textField.setText("/home/pab/foo2.input");
        Assert.assertEquals("$FRICAS -eval \")r /home/pab/foo2.input\"", pane.getText());

        bean.keepRunning = false;
        form.resetEditor(bean);
        Assert.assertEquals("$FRICAS -eval \")r /home/pab/foo.input\" -eval )q", pane.getText());
    }
}
