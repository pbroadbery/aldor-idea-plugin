package aldor.editor;

import com.intellij.lang.Commenter;
import org.jetbrains.annotations.Nullable;


/*
 * Stuff about Aldor comments.. Basically, just "--"
 * nb: eventually should be CodeDocumentationAwareCommenterEx
 * but there's a way to go yet.
 */
public class AldorCommenter implements Commenter {

    @Nullable
    @Override
    public String getLineCommentPrefix() {
        return "--";
    }

    @Nullable
    @Override
    public String getBlockCommentPrefix() {
        return null;
    }

    @Nullable
    @Override
    public String getBlockCommentSuffix() {
        return null;
    }

    @Nullable
    @Override
    public String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Nullable
    @Override
    public String getCommentedBlockCommentSuffix() {
        return null;
    }
}
