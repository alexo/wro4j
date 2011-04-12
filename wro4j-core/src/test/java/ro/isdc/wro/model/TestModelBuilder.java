package ro.isdc.wro.model;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;

/**
 * Tests the {@link ModelBuilder} class.
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 1.3.7
 */
public class TestModelBuilder {

  private WroModelFactory factory;

  private ModelBuilder<List<Group>> builder;

  @Before
  public void setUp() {
    factory = Mockito.mock(WroModelFactory.class);
    builder = new ModelBuilder<List<Group>>(factory) {
      @Override
      public List<Group> build() {
        setModified(false);
        return getGroups();
      }
    };

  }

  @Test
  public void testGetConfigResourceAsStream() throws IOException {
    String message = "Leave everything a little better than how you found it.";

    Mockito.when(factory.getConfigResourceAsStream())
      .thenReturn(new ByteArrayInputStream(
          message.getBytes()));

    InputStream input = builder.getModelFactory().getConfigResourceAsStream();
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    IOUtils.copy(input, output);

    input.close();
    output.close();

    assertEquals(message, output.toString());

    Mockito.verify(factory).getConfigResourceAsStream();
  }

  @Test
  public void testAddGroup() {
    Group group1 = createGroup("TestGroup-1");
    Group group2 = createGroup("TestGroup-2");

    builder.addGroup(group1)
      .addGroup(group2);

    assertTrue(builder.isModified());
    assertEquals(group1, builder.build().get(0));
    assertEquals(group2, builder.build().get(1));
    assertTrue(!builder.isModified());
  }

  @Test
  public void testAddGroupRef() {
    Group group1 = createGroup("TestGroup-1");
    Group group2 = createGroup("TestGroup-2");
    Group group3 = createGroup("TestGroup-3");

    builder.addGroup(group1)
      .addGroup(group2)
      .addGroupRef(group3, group1)
      .addGroupRef(group3, "TestGroup-2")
      .addGroupRef(group3, createGroup("TestGroup-4"));

    assertTrue(builder.isModified());
    assertEquals(group3, builder.build().get(2));
    assertEquals(8, builder.build().get(2).getResources().size());
    assertTrue(!builder.isModified());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddGroupRefFail() {
    Group group1 = createGroup("TestGroup-1");
    Group group2 = createGroup("TestGroup-2");

    builder.addGroup(group1)
      .addGroupRef(group2, "TestGroup-fail");
  }

  @SuppressWarnings("serial")
  private Group createGroup(final String groupName) {
    Group group = new Group();
    final long id = Math.round(Math.random() * System.currentTimeMillis());

    group.setName(groupName);
    group.setResources(new ArrayList<Resource>() {{
      add(Resource.create("/test/foo-" + id + ".js", ResourceType.JS));
      add(Resource.create("/test/foo-" + id + ".css", ResourceType.CSS));
    }});

    return group;
  }
}
