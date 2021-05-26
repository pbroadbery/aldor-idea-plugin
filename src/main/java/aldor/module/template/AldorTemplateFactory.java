package aldor.module.template;

import aldor.build.module.AldorModuleBuilder;
import aldor.build.module.AldorModuleType;
import aldor.module.template.git.AldorGitModuleBuilder;
import aldor.module.template.git.GitModuleType;
import com.google.common.collect.Lists;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import com.intellij.platform.templates.BuilderBasedTemplate;
import icons.AldorIcons;
import org.jetbrains.annotations.NonNls;
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
    private final List<TemplateRegistry> templateRegistries = Lists.newArrayList();

    public AldorTemplateFactory() {
        /*
        templateRegistries.add(new TemplateRegistry("Aldor/Spad", Lists.newArrayList(
                new BuilderBasedTemplate(new AldorEmptyModuleBuilder())
        )));
         */
        templateRegistries.add(new TemplateRegistry("Aldor", Lists.newArrayList(
                new BuilderBasedTemplate(new AldorSimpleModuleBuilder()),
                new BuilderBasedTemplate(new AldorGitModuleBuilder(GitModuleType.Aldor)))));
        templateRegistries.add(new TemplateRegistry("FriCAS", Lists.newArrayList(
                new BuilderBasedTemplate(new FricasSimpleModuleBuilder()),
                new BuilderBasedTemplate(new AldorGitModuleBuilder(GitModuleType.Fricas)))));
    }

    @NotNull
    @Override
    public String[] getGroups() {
        List<String> groupNames = Lists.newArrayList();
        for (TemplateRegistry r: templateRegistries) {
            groupNames.add(r.name());
        }
        return groupNames.toArray(new String[templateRegistries.size()]);
    }

    @Override
    public int getGroupWeight(String group) {
        return ALDOR_GROUP_WEIGHT;
    }

    @NotNull
    @Override
    public ProjectTemplate[] createTemplates(@Nullable String group, WizardContext context) {
        Optional<TemplateRegistry> registry = templateRegistries.stream().filter(r -> r.name().equals(group)).findFirst();
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

    public static class AldorEmptyModuleBuilder extends AldorModuleBuilder {
        AldorEmptyModuleBuilder() {
            super(AldorModuleType.instance());
        }

        @Override
        public String getPresentableName() {
            return "Empty Aldor";
        }

        @Override
        public String getDescription() {
            return "Combined Aldor/Spad module - Do not use";
        }

        @Override
        @Nullable
        @NonNls
        public String getBuilderId() {
            return "Empty-Aldor--Module";
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
