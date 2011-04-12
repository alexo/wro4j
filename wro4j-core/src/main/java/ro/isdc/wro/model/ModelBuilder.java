package ro.isdc.wro.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;

/**
 * This class allows to build WRO models using different representations.
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
  private final Map<String, Group> groups = new LinkedHashMap<String, Group>();

  /** Determines whether default model has changed or not. */
  private boolean modified;

  /**
   * Builds the model representation. The {@link #isModified()} field will
   * return <code>false</code> each time the model is built.
   *
   * @return An instance of T containing a valid model representation.
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

  /**
   * Determines whether the model was modified or not. A model is set as
   * modified when something changes after loading the default configuration
   * from the file.
   *
   * @return <code>true</code> if the model has changed, <code>false</code>
   *    otherwise.
   */
  public boolean isModified() {
    return modified;
  }

  /**
   * Sets whether this model has changed or not.
   *
   * @param isModified <code>true</code> if the model was modified,
   *    <code>false</code> otherwise.
   */
  protected void setModified(final boolean isModified) {
    modified = isModified;
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
   * Maps a group reference. This is the base strategy for group inheritance.
   *
   * @param groupTarget Group that will be extended. If it doesn't exist it will
   *    be put into the model via {@link #addGroup(Group)}. Cannot be null.
   * @param groupRef Group to extend from. It's added if doesn't exist in the
   *    current model. It cannot be null.
   * @return Same object to continue building the model.
   */
  public ModelBuilder<T> addGroupRef(final Group groupTarget,
      final Group groupRef) {

    Validate.notNull(groupRef, "The reference group cannot be null.");

    if (!groups.containsKey(groupRef.getName())) {
      addGroup(groupRef);
    }

    return addGroupRef(groupTarget, groupRef.getName());
  }

  /**
   * Maps a group reference. This is the base strategy for group inheritance.
   *
   * @param groupTarget Group that will be extended. If it doesn't exist it will
   *    be put into the model via {@link #addGroup(Group)}. Cannot be null.
   * @param groupRef Name of the group to extend from. It cannot be null or
   *    empty.
   * @return Same object to continue building the model.
   */
  public ModelBuilder<T> addGroupRef(final Group groupTarget,
      final String groupRef) {

    Validate.notNull(groupTarget, "The target group cannot be null.");
    Validate.notEmpty(groupRef, "The group reference cannot be null or empty.");
    Validate.isTrue(groups.containsKey(groupRef),
        "The referenced group doesn't exist.");

    if (!groups.containsKey(groupTarget.getName())) {
      addGroup(groupTarget);
    }

    Group group = groups.get(groupTarget.getName());
    Group groupSource = groups.get(groupRef);

    if (!group.getResources().containsAll(groupSource.getResources())) {
      modified = true;
    }

    group.getResources().addAll(groupSource.getResources());

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
