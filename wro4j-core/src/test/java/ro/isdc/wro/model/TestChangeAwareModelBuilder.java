package ro.isdc.wro.model;

import static junit.framework.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;

/**
 * Tests the {@link ChangeAwareModelBuilder} class.
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 1.3.7
 */
public class TestChangeAwareModelBuilder {

  private WroModelFactory factory;

  private ChangeAwareModelBuilder<List<Group>> builder;

  @Before
  public void setUp() {
    factory = Mockito.mock(WroModelFactory.class);
    builder = new ChangeAwareModelBuilder<List<Group>>(factory, false) {
      @Override
      public List<Group> build() {
        return getGroups();
      }
    };
  }

  @Test
  public void testAutoNotifications() {
    builder = new ChangeAwareModelBuilder<List<Group>>(factory, true) {
      @Override
      public List<Group> build() {
        return getGroups();
      }
    };

    builder.addGroup(new Group());
    assertTrue(builder.isModified());

    Mockito.verify(factory).onModelChanged();
  }

  @Test
  public void testAddGroup() {
    builder.addGroup(new Group());

    assertTrue(builder.notifyChanges());
    assertTrue(!builder.isModified());

    Mockito.verify(factory).onModelChanged();
  }

  @Test
  public void testAddGroupRef() {
    Group group = new Group();
    group.setName("NiceGroup");

    builder.addGroupRef(new Group(), group);

    assertTrue(builder.notifyChanges());
    assertTrue(!builder.isModified());
    Mockito.verify(factory).onModelChanged();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddGroupRefFail() {
    builder.addGroupRef(new Group(), "evil group");
  }
}
