/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
package aldor.psi.elements;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.EmptyStub;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmptyStubElementType<T extends PsiElement> extends IStubElementType<EmptyStub<T>, T> {
    private final AldorStubFactory.PsiElementFactory<EmptyStub<T>, T> stubFactory;

    protected EmptyStubElementType(@NotNull @NonNls String debugName, @Nullable Language language, AldorStubFactory.PsiElementFactory<EmptyStub<T>, T> stubFactory) {
        super(debugName, language);
        this.stubFactory = stubFactory;
    }

    @Override
    public T createPsi(@NotNull EmptyStub<T> stub) {
        return stubFactory.invoke(stub, this);
    }

    @NotNull
    @Override
    public final EmptyStub<T> createStub(@NotNull T psi, @SuppressWarnings("rawtypes") StubElement parentStub) {
        return createStub(parentStub);
    }

    protected EmptyStub<T> createStub(StubElement<?> parentStub) {
        return new EmptyStub<>(parentStub, this);
    }

    @NotNull
    @Override
    public String getExternalId() {
        //noinspection StringConcatenationMissingWhitespace
        return getLanguage().getID() + toString();
    }

    @Override
    public final void serialize(@NotNull EmptyStub<T> stub, @NotNull StubOutputStream dataStream) {
    }

    @NotNull
    @Override
    public final EmptyStub<T> deserialize(@NotNull StubInputStream dataStream, @SuppressWarnings("rawtypes") StubElement parentStub) {
        return createStub(parentStub);
    }

    @Override
    public final void indexStub(@NotNull EmptyStub<T> stub, @NotNull IndexSink sink) {
    }
}
