package aldor.test_util;

import javax.swing.JComponent;
import java.awt.Component;
import java.awt.Container;
import java.util.Optional;
import java.util.function.Predicate;

public class Swings {

    public static <T  extends Container> Optional<T> findChild(Container container, Class<T> clss) {
        return findChildComponent(container, c -> clss.isAssignableFrom(c.getClass())).map(clss::cast);
    }

    public static Optional<JComponent> findChildComponent(Container component, Predicate<? super Component> predicate) {
        for (int i=0; i<component.getComponentCount(); i++) {
            Component next = component.getComponent(i);
            if (predicate.test(next)) {
                return Optional.of((JComponent) next);
            }
            else if (next instanceof Container){
                Optional<JComponent> found = findChildComponent((Container) next, predicate);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }
}


