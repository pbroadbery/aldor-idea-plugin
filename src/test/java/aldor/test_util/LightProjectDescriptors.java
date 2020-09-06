package aldor.test_util;

import aldor.build.module.AldorModuleType;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

// @Deprecated "Use SDK Project Descriptors"
@Deprecated
public final class LightProjectDescriptors {
    public static final AldorRoundTripProjectDescriptor ALDOR_ROUND_TRIP_PROJECT_DESCRIPTOR = new AldorRoundTripProjectDescriptor();

    public static final LightProjectDescriptor ALDOR_MODULE_DESCRIPTOR = new LightProjectDescriptor() {
        @Override
        @NotNull
        public String getModuleTypeId() {
            return AldorModuleType.instance().getId();
        }
    };

}
