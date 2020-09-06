package aldor.module.template;

import aldor.build.module.AldorModuleBuilder;
import aldor.build.module.AldorModuleType;
import aldor.ui.AldorIcons;
import com.google.common.collect.Lists;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import com.intellij.platform.templates.BuilderBasedTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AldorTemplateFactory extends ProjectTemplatesFactory {
    private static final ProjectTemplate[] EMPTY_TEMPLATES = new ProjectTemplate[0];
    public static final int ALDOR_GROUP_WEIGHT = 1200;
    private final List<TemplateRegistry> templateRegisties = Lists.newArrayList();

    public AldorTemplateFactory() {
        templateRegisties.add(new TemplateRegistry("Aldor/Spad", Lists.newArrayList(
                new BuilderBasedTemplate(new AldorEmptyModuleBuilder())
        )));
        templateRegisties.add(new TemplateRegistry("Aldor", Lists.newArrayList(
                new BuilderBasedTemplate(new AldorGitModuleBuilder(GitModuleType.Aldor)),
                new BuilderBasedTemplate(new AldorSimpleModuleBuilder())
                )));


        templateRegisties.add(new TemplateRegistry("Fricas", Lists.newArrayList(
                new BuilderBasedTemplate(new AldorGitModuleBuilder(GitModuleType.Fricas)),
                new BuilderBasedTemplate(new FricasSimpleModuleBuilder())
                )));
    }

    @NotNull
    @Override
    public String[] getGroups() {
        List<String> groupNames = Lists.newArrayList();
        for (TemplateRegistry r: templateRegisties) {
            groupNames.add(r.name());
        }
        return groupNames.toArray(new String[templateRegisties.size()]);
    }

    @Override
    public int getGroupWeight(String group) {
        return ALDOR_GROUP_WEIGHT;
    }

    @NotNull
    @Override
    public ProjectTemplate[] createTemplates(@Nullable String group, WizardContext context) {
        Optional<TemplateRegistry> registry = templateRegisties.stream().filter(r -> r.name().equals(group)).findFirst();
        return registry.map(r -> r.templates().toArray(EMPTY_TEMPLATES)).orElse(EMPTY_TEMPLATES);
    }

    @Override
    public Icon getGroupIcon(String group) {
        return AldorIcons.MODULE;
    }

    @Nullable
    @Override
    public String getParentGroup(String group) {
        return null;
    }

    private static class AldorEmptyModuleBuilder extends AldorModuleBuilder {
        AldorEmptyModuleBuilder() {
            super(AldorModuleType.instance());
        }
    }

    private static class TemplateRegistry {
        private final String name;
        private final List<ProjectTemplate> templates;

        TemplateRegistry(String name, List<ProjectTemplate> templates) {
            this.name = name;
            this.templates = new ArrayList<>(templates);
        }

        public String name() {
            return name;
        }

        public List<ProjectTemplate> templates() {
            return Collections.unmodifiableList(templates);
        }
    }

}
