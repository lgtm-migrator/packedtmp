package app.packed.application;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.base.NamespacePath;
import app.packed.component.Assembly;
import app.packed.component.ComponentMirror;
import app.packed.component.Wirelet;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import app.packed.mirror.Mirror;
import app.packed.mirror.SetView;
import app.packed.mirror.TreeWalker;

/**
 * A mirror of an application.
 * <p>
 */
public interface ApplicationMirror extends Mirror {

    /** {@return the root (container) component in the application}. */
    ComponentMirror component();

    /** {@return the component in the application}. */
    ComponentMirror component(CharSequence path);

    TreeWalker<ComponentMirror> components();

    /** {@return the root container in the application}. */
    ContainerMirror container();

    // Er det kun componenter i den application??? Ja ville jeg mene...
    // Men saa kommer vi ud i spoergsmaalet omkring er components contextualizable...
    // app.rootContainer.children() <-- does this only include children in the same
    // application?? or any children...

    // teanker det kun er containere i samme application...
    // ellers maa man bruge container.resolve("....")
    ContainerMirror container(CharSequence path);

    /** {@return a walker containing all the containers in this application} */
    TreeWalker<ContainerMirror> containers();

    /** {@return all child applications of this application.} */
    // Det her har "store" implikationer for versions drevet applicationer...
    // Det betyder nemlig at vi totalt flatliner applikationer...
    // Det er bare applikation... Hvordan brugeren ser det er helt anderledes i forhold
    // til hvordan vi internt ser det
    /**
     * Returns all child applications deploy
     * 
     * @return
     */
    // IDK or children();
    default Collection<ApplicationMirror> deployments() {
        BiConsumer<ApplicationHostMirror, Consumer<ApplicationMirror>> bc = (m, c) -> m.installations().forEach(p -> c.accept(p));
        return hosts().stream().mapMulti(bc).toList();
    }

    /** { @return a set of all extensions that have been used by the application.} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    default Set<Class<? super Extension>> extensions() {
        Set<Class<? super Extension>> result = new HashSet<>();
        containers().forEach(c -> result.addAll((Set) c.extensions()));
        return Set.copyOf(result);
    }
    
    default Optional<ApplicationHostMirror> host() {
        throw new UnsupportedOperationException();
    }
    
    /** {@return all the application hosts defined in this application.} */
    default SetView<ApplicationHostMirror> hosts() {
        throw new UnsupportedOperationException();
    }
    
    default TaskListMirror initialization() {
        throw new UnsupportedOperationException();
    }

    default boolean isHosted() {
        return !parent().isEmpty();
    }

    /**
     * Returns whether or the application is runnable. The value is always determined by
     * {@link ApplicationDriver#hasRuntime()}.
     * 
     * @return whether or the application is runnable
     */
    boolean isRunnable();

    // Wired er parent component<->child component
    // connections er component til any component.
    /**
     * Returns whether or not this application is strongly wired to a parent application.
     * <p>
     * A root application will always return false.
     * 
     * @return {@code true} if this application is strongly wired to a parent application, otherwise {@code false}
     */
    boolean isStronglyWired();

    default boolean isUninstallable() {
        // If not-hosted???
        // true -> All root applications are trivially uninstallable
        // false -> All root applications have no uninstallation function
        return false;
    }

    /**
     * {@return the module that the application belongs to. This is typically the module of the assembly that defined the
     * root container.}
     */
    Module module();

    /** {@return the name of the application.} */
    String name();

    /** {@return the parent application of this application. Or empty if this application has no parent} */
    Optional<ApplicationMirror> parent();

    NamespacePath path();
    // Optional<ApplicationRelation> parentRelation();

    /**
     * <p>
     * A root application always returns empty
     * 
     * @return whether or not this application is a part of a versionable application
     */
    default Optional<VersionableApplicationMirror> versionable() {
        throw new UnsupportedOperationException();
    }

    default TreeWalker<ApplicationMirror> walker() {
        throw new UnsupportedOperationException();
        // app.components() <-- all component in the application
        // app.component().walker() <--- all components application or not...

        // someComponent.walker().filter(c->c.application == SomeApp)...
    }

    public static ApplicationMirror of(Assembly<?> assembly, Wirelet... wirelets) {
        return BaseMirror.of(assembly, wirelets).application();
    }

    // Relations between to different applications
    // Ret meget som ComponentRelation

    /// Maaske flyt til ApplicationMirror.relation...
    /// Der er ingen der kommer til at lave dem selv...

    // extends Relation???
    interface ParentRelation {

        ApplicationMirror child();

        // Det kan jo ogsaa ligge i en attribute. Og saa kan vi extracte det
        // Via ApplicationHostMirror.of(ApplicationMirror child)
        Object hostInfo();

        ApplicationMirror parent();
    }

    /**
     * An application relation is an unchangeable representation of a directional relationship between two applications. It
     * is typically created via {@link ComponentMirror#relationTo(ComponentMirror)}.
     */
    public interface Relation {

        ApplicationMirror from();

        ApplicationMirror to();
    }
}
