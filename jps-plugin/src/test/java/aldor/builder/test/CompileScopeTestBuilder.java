/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aldor.builder.test;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import gnu.trove.THashSet;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.incremental.CompileScope;
import org.jetbrains.jps.incremental.CompileScopeImpl;
import org.jetbrains.jps.incremental.TargetTypeRegistry;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author pab
 */
public final class CompileScopeTestBuilder {
    private static final Logger LOG = Logger.getInstance(CompileScopeTestBuilder.class);

    private final boolean myForceBuild;
    private final Set<BuildTargetType<?>> myTargetTypes = new HashSet<>();
    private final Set<BuildTarget<?>> myTargets = new HashSet<>();
    private final LinkedHashMap<BuildTarget<?>, Set<File>> myFiles = new LinkedHashMap<>();

    public static CompileScopeTestBuilder rebuild() {
        return new CompileScopeTestBuilder(true);
    }

    public static CompileScopeTestBuilder make() {
        return new CompileScopeTestBuilder(false);
    }


    private CompileScopeTestBuilder(boolean forceBuild) {
        myForceBuild = forceBuild;
    }

    public CompileScopeTestBuilder targetTypes(BuildTargetType<?>... targets) {
        myTargetTypes.addAll(Arrays.asList(targets));
        return this;
    }

    public CompileScopeTestBuilder file(BuildTarget<?> target, String path) {
        Set<File> files = myFiles.computeIfAbsent(target, k -> new THashSet<>(FileUtil.FILE_HASHING_STRATEGY));
        files.add(new File(path));
        return this;
    }

    public CompileScope build() {
        Collection<BuildTargetType<?>> typesToForceBuild = myForceBuild ? myTargetTypes : Collections.emptyList();
        LOG.info("Creating compile scope: types: " + myTargetTypes + " targets: " + myTargets + " files: " + myFiles);
        return new CompileScopeImpl(myTargetTypes, typesToForceBuild, myTargets, myFiles);
    }

    public CompileScopeTestBuilder all() {
        myTargetTypes.addAll(TargetTypeRegistry.getInstance().getTargetTypes());
        return this;
    }

}
