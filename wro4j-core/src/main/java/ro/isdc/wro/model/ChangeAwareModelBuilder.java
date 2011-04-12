package ro.isdc.wro.model;

import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;

/**
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 1.3.7
 */
public abstract class ChangeAwareModelBuilder<T> extends ModelBuilder<T> {

  /**
   * Constructs a new {@link ModelBuilder} with the ability to notify model
   * changes.
   *
   * @param factory Related model factory which will receive model changes
   *    notifications. It cannot be null.
   */
  public ChangeAwareModelBuilder(final WroModelFactory factory) {
    super(factory);
  }

  /**
   * {@inheritDoc}.
   * <p>
   * If a new group is added this method will notify to
   * {@link WroModelFactory} that this model has changed.
   * </p>
   */
  @Override
  public ModelBuilder<T> addGroup(final Group group) {
    super.addGroup(group);

    if (this.isModified()) {
      getModelFactory().onModelChanged(null);
    }

    return this;
  }
}
