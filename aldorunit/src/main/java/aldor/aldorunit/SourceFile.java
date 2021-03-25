package aldor.aldorunit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("ClassIndependentOfModule")
@Retention(RetentionPolicy.RUNTIME)
public @interface SourceFile {
    String source() default "";
}
