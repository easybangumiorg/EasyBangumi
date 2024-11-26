package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.internal.artifacts.dependencies.ProjectDependencyInternal;
import org.gradle.api.internal.artifacts.DefaultProjectDependencyFactory;
import org.gradle.api.internal.artifacts.dsl.dependencies.ProjectFinder;
import org.gradle.api.internal.catalog.DelegatingProjectDependency;
import org.gradle.api.internal.catalog.TypeSafeProjectDependencyFactory;
import javax.inject.Inject;

@NonNullApi
public class EasyBangumiProjectDependency extends DelegatingProjectDependency {

    @Inject
    public EasyBangumiProjectDependency(TypeSafeProjectDependencyFactory factory, ProjectDependencyInternal delegate) {
        super(factory, delegate);
    }

    /**
     * Creates a project dependency on the project at path ":business"
     */
    public BusinessProjectDependency getBusiness() { return new BusinessProjectDependency(getFactory(), create(":business")); }

    /**
     * Creates a project dependency on the project at path ":component"
     */
    public ComponentProjectDependency getComponent() { return new ComponentProjectDependency(getFactory(), create(":component")); }

    /**
     * Creates a project dependency on the project at path ":composeApp"
     */
    public ComposeAppProjectDependency getComposeApp() { return new ComposeAppProjectDependency(getFactory(), create(":composeApp")); }

}
