package ro.isdc.wro.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;

/**
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 1.3.7
 */
public abstract class ModelBuilder<T> {

  /** Default class hierarchy logger. */
  protected Logger log = LoggerFactory.getLogger(getClass());

  /** Model factory to build configuration from. It also will be notified
   * on model changes. */
  private final WroModelFactory modelFactory;

  /** Groups added to this builder. */
  private final Map<String, Group> groups = new HashMap<String, Group>();

  /** Determines whether default model has changed or not. */
  private boolean modified;

  /**
   * Builds the model representation.
   *
   * @return
   */
  public abstract T build();

  /**
   * Creates a new {@link ModelBuilder} and sets the related model factory.
   *
   * @param theModelFactory Model factory to build configuration from. It also
   *    will be notified. Cannot be null.
   */
  public ModelBuilder(final WroModelFactory theModelFactory) {
    Validate.notNull(theModelFactory, "The model factory cannot be null.");

    modelFactory = theModelFactory;
  }

  public boolean isModified() {
    return modified;
  }

  /**
   * Adds a new group. If the groups exists, it's replaced by the new one.
   *
   * @param group Group to add. It cannot be null.
   *
   * @return Same object to continue building the model.
   */
  public ModelBuilder<T> addGroup(final Group group) {
    Validate.notNull(group, "The group cannot be null.");

    if (!groups.containsKey(group.getName()) ||
        (groups.containsKey(group.getName()) &&
         !groups.containsValue(group))) {
      modified = true;
    }

    groups.put(group.getName(), group);

    return this;
  }

  /**
   * Returns a list of groups added by this builder. It doesn't include groups
   * that exists in the model configuration file.
   *
   * @return An immutable list of groups. Never returns null.
   */
  public List<Group> getGroups() {
    return new ArrayList<Group>(groups.values());
  }

  /**
   * Returns the model factory from which model will be read.
   *
   * @return A valid {@link WroModelFactory}, never returns <code>null</code>.
   */
  protected WroModelFactory getModelFactory() {
    return modelFactory;
  }
}
