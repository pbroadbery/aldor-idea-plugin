package aldor.test_util;

import javax.swing.JComponent;
import java.awt.Component;
import java.awt.Container;
import java.util.Optional;
import java.util.function.Predicate;

public class Swings {

    public static <T extends Container> Optional<T> findChild(Container container, Class<T> clss) {
        return findChildComponent(container, c -> clss.isAssignableFrom(c.getClass())).map(clss::cast);
    }

    public static Optional<JComponent> findChildComponent(Container component, Predicate<? super Component> predicate) {
        return findChildComponent(component, predicate, JComponent.class);
    }

    public static <T extends JComponent> Optional<T> findChildComponent(Container component, Predicate<? super Component> predicate, Class<T> clss) {
        for (int i=0; i<component.getComponentCount(); i++) {
            Component next = component.getComponent(i);

            if (clss.isAssignableFrom(next.getClass()) && predicate.test(next)) {
                //noinspection unchecked
                return Optional.of((T) next);
            }
            else if (next instanceof Container) {
                Optional<T> found = findChildComponent((Container) next, predicate, clss);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }

    public static Predicate<Component> byName(String name) {
        return component -> {
            if (component instanceof JComponent) {
                return name.equals(component.getName());
            } else {
                return false;
            }
        };
    }

}


