package aldor.builder.jps.autoconf.descriptors;

import aldor.util.HasSxForm;
import aldor.util.SxForm;
import aldor.util.SxFormUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static aldor.util.SxFormUtils.list;
import static aldor.util.SxFormUtils.name;
import static aldor.util.SxFormUtils.tagged;

/**
 * Targets - we don't talk in terms of BuildTargetTypes - that leads to a confusing lot of mutual references.
 * Note that ids here are in terms of the local project... we don't need the module path, as it will be supplied
 * in the first part of the name
 */
class TargetCollection implements HasSxForm {
    private final Map<String, ScriptTargetDescriptor> targets = new HashMap<>();
    private final Map<SpecialTargetId, AbstractTargetDescriptor> specialTargets = new EnumMap<>(SpecialTargetId.class);
    private final Map<String, PhonyTargetDescriptor> phonyTargets = new HashMap<>();
    private Map<String, List<AbstractTargetDescriptor>> dependenciesForDescriptor = new HashMap<>();

    ScriptTargetDescriptor add(ScriptTargetDescriptor tgt) {
        targets.put(tgt.id(), tgt);
        return tgt;
    }

    PhonyTargetDescriptor add(PhonyTargetDescriptor tgt) {
        phonyTargets.put(tgt.id(), tgt);
        return tgt;
    }

    public Collection<ScriptTargetDescriptor> allScriptTargets() {
        // TODO: Break up by type
        return targets.values();
    }

    public Collection<PhonyTargetDescriptor> allPhonyTargets() {
        return phonyTargets.values();
    }

    @Override @NotNull
    public SxForm sxForm() {
        SxForm properties = tagged()
                .with("Targets", SxFormUtils.number(allScriptTargets().size()))
                .with("PhonyTargets", SxFormUtils.number(phonyTargets.size()));
        return list()
                .add(name("TargetCollection"))
                .add(properties);
    }

    void libTarget(AbstractTargetDescriptor tgt) {
        specialTargets.put(SpecialTargetId.ALDORLIB, tgt);
    }

    public AbstractTargetDescriptor libTarget() {
        return specialTargets.get(SpecialTargetId.ALDORLIB);
    }

    public void addDependency(AbstractTargetDescriptor tgt, AbstractTargetDescriptor dependency) {
        var list = dependenciesForDescriptor.computeIfAbsent(tgt.id(), k -> new ArrayList<>());
        list.add(dependency);
    }

    public Collection<AbstractTargetDescriptor> dependencies(AbstractTargetDescriptor descriptor) {
        return dependenciesForDescriptor.getOrDefault(descriptor.id(), Collections.emptyList());
    }

    public void runtimeTarget(AbstractTargetDescriptor phonyTgt) {
        specialTargets.put(SpecialTargetId.RUNTIME, phonyTgt);
    }

    enum SpecialTargetId {RUNTIME, ALDORLIB}
}
