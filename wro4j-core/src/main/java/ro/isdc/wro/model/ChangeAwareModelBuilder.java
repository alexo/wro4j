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
   * Indicates whether notifications will be automatically triggered or not.
   * If this is <code>true</code>, every time model changes the related factory
   * will be notified. Default is <code>false</code>.
   */
  private boolean autoNotification;

  /**
   * Constructs a new {@link ModelBuilder} with the ability to notify model
   * changes.
   *
   * @param factory Related model factory which will receive model changes
   *    notifications. It cannot be null.
   */
  public ChangeAwareModelBuilder(final WroModelFactory factory) {
    this(factory, false);
  }

  /**
   * Constructs a new {@link ModelBuilder} with the ability to notify model
   * changes.
   *
   * @param factory Related model factory which will receive model changes
   *    notifications. It cannot be null.
   * @param autoNotificationEnabled Indicates whether notifications will be
   *    automatically triggered or not. If this is <code>true</code>, every
   *    time model changes the related factory will be notified. Default is
   *    <code>false</code>.
   */
  public ChangeAwareModelBuilder(final WroModelFactory factory,
      final boolean autoNotificationEnabled) {
    super(factory);

    autoNotification = autoNotificationEnabled;
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

    if (isModified() && autoNotification) {
      getModelFactory().onModelChanged();
    }

    return this;
  }

  /**
   * {@inheritDoc}
   * <p>
   * If a resources are added t the target group this method will notify to
   * {@link WroModelFactory} that this model has changed.
   * </p>
   */
  @Override
  public ModelBuilder<T> addGroupRef(final Group groupTarget,
      final String groupRef) {

    super.addGroupRef(groupTarget, groupRef);

    if (isModified() && autoNotification) {
      getModelFactory().onModelChanged();
    }

    return this;
  }

  /**
   * Notifies to the related {@link WroModelFactory} if this model has changed.
   * After notifying the {@link #isModified()} field will return
   * <code>false</code>.
   *
   * @return <code>true</code> if the model changed and notification has been
   *    sent, <code>false</code> otherwise.
   */
  public boolean notifyChanges() {
    boolean modified = isModified();

    if (modified) {
      getModelFactory().onModelChanged();
      setModified(false);
    }

    return modified;
  }
}
