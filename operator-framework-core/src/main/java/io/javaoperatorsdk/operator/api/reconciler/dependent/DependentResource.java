package io.javaoperatorsdk.operator.api.reconciler.dependent;

import java.util.Optional;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.javaoperatorsdk.operator.api.reconciler.Context;

public interface DependentResource<R, P extends HasMetadata> {

  /**
   * @param primary associated primary resource
   * @param context of the reconciliation
   * @return result of the reconciliation
   */
  ReconcileResult<R> reconcile(P primary, Context<P> context);

  /**
   * The intention with get resource is to return the actual state of the resource. Usually from a
   * local cache, what was updated after the reconciliation, or typically from the event source that
   * caches the resources.
   *
   * @param primaryResource associated
   * @return the reconciled resource
   */
  Optional<R> getResource(P primaryResource);
}
