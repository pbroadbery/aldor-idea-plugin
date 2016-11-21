package aldor.psi;

import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiNameIdentifierOwner;

/** Used to mark types which represent an id - infix operators and TK_ID.
 *
 * Note that PsiNamedIdentifierOwner is probably wrong.
 * At the moment references point from ids to the ids in the declaration of their definition.
 * Unfortunately, that won't work with like infix operators, where logically a rename of
 * '+' to 'add' should cause the definition to be re-written as 'add(a: X, b: X): X instead of
 * '(a: X) + (b: X)'.
 */
public interface AldorIdentifier extends PsiNameIdentifierOwner, Navigatable {

}
