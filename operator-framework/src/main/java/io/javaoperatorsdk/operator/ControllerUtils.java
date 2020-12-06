package io.javaoperatorsdk.operator;

import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.CustomResourceDoneable;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.ResourceController;

import java.util.Map;


public class ControllerUtils {

    private static final String FINALIZER_NAME_SUFFIX = "/finalizer";
    public static final String CONTROLLERS_RESOURCE_PATH = "javaoperatorsdk/controllers";
    public static final String DONEABLES_RESOURCE_PATH = "javaoperatorsdk/doneables";
    private static Map<Class<? extends ResourceController>, Class<? extends CustomResource>> controllerToCustomResourceMappings;
    private static Map<Class<? extends CustomResource>, Class<? extends CustomResourceDoneable>> resourceToDoneableMappings;


    static {
        controllerToCustomResourceMappings = ClassMappingProvider
                .provide(CONTROLLERS_RESOURCE_PATH,
                        ResourceController.class,
                        CustomResource.class);
        resourceToDoneableMappings = ClassMappingProvider
                .provide(DONEABLES_RESOURCE_PATH,
                        CustomResource.class,
                        CustomResourceDoneable.class
                );
    }

    static String getFinalizer(ResourceController controller) {
        final String annotationFinalizerName = getAnnotation(controller).finalizerName();
        if (!Controller.NULL.equals(annotationFinalizerName)) {
            return annotationFinalizerName;
        }
        return getAnnotation(controller).crdName() + FINALIZER_NAME_SUFFIX;
    }

    static boolean getGenerationEventProcessing(ResourceController<?> controller) {
        return getAnnotation(controller).generationAwareEventProcessing();
    }

    static <R extends CustomResource> Class<R> getCustomResourceClass(ResourceController<R> controller) {
        final Class<? extends CustomResource> customResourceClass = controllerToCustomResourceMappings
                .get(controller.getClass());
        if (customResourceClass == null) {
            throw new IllegalArgumentException(
                    String.format(
                            "No custom resource has been found for controller %s",
                            controller.getClass().getCanonicalName()
                    )
            );
        }
        return (Class<R>) customResourceClass;
    }

    static String getCrdName(ResourceController controller) {
        return getAnnotation(controller).crdName();
    }

    public static <T extends CustomResource> Class<? extends CustomResourceDoneable<T>>
    getCustomResourceDoneableClass(ResourceController<T> controller) {
        final Class<T> customResourceClass = getCustomResourceClass(controller);
        final Class<? extends CustomResourceDoneable<T>> doneableClass = (Class<? extends CustomResourceDoneable<T>>) resourceToDoneableMappings.get(customResourceClass);
        if (doneableClass == null) {
            throw new RuntimeException(String.format("No matching Doneable class found for %s", customResourceClass));
        }
        return doneableClass;
    }

    private static Controller getAnnotation(ResourceController<?> controller) {
        return controller.getClass().getAnnotation(Controller.class);
    }

    public static boolean hasGivenFinalizer(CustomResource resource, String finalizer) {
        return resource.getMetadata().getFinalizers() != null && resource.getMetadata().getFinalizers().contains(finalizer);
    }
}
