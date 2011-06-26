package ro.isdc.wro.examples.web;

import org.apache.wicket.Application;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

import ro.isdc.wro.examples.web.page.HomePage;
import ro.isdc.wro.examples.web.page.ProcessorsPage;


/**
 * Application object for your web application. If you want to run this application without deploying, run the Start
 * class.
 *
 * @see wicket.myproject.Start#main(String[])
 */
public class WebResourceOptimizationApplication extends WebApplication {
  /**
   * @see wicket.Application#getHomePage()
   */
  @Override
  public Class<? extends WebPage> getHomePage() {
    return HomePage.class;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  protected void init() {
    // for Google App Engine
    if (isDeploy()) {
      getResourceSettings().setResourcePollFrequency(null);
    }
    // settings
    getMarkupSettings().setStripWicketTags(true);
    getMarkupSettings().setStripComments(true);
    getMarkupSettings().setCompressWhitespace(true);
    getDebugSettings().setDevelopmentUtilitiesEnabled(true);

//    setPageManagerProvider(new DefaultPageManagerProvider(this) {
//      @Override
//      public IPageManager get(final IPageManagerContext pageManagerContext) {
//        final IDataStore dataStore = new HttpSessionDataStore(pageManagerContext, new MemorySizeEvictionStrategy(
//          Bytes.megabytes(10)));
//        final IPageStore pageStore = new DefaultPageStore(Application.get().getName(), dataStore, getCacheSize());
//        return new PersistentPageManager(Application.get().getName(), pageStore, pageManagerContext);
//      }
//    });

    // mounts
    // mount(new IndexedHybridUrlCodingStrategy("/home", HomePage.class));
    mountPage("/home", HomePage.class);
    mountPage("/processors", ProcessorsPage.class);
  }


  public static WebResourceOptimizationApplication get() {
    return (WebResourceOptimizationApplication)Application.get();
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public RuntimeConfigurationType getConfigurationType() {
    return isDeploy() ? RuntimeConfigurationType.DEPLOYMENT : RuntimeConfigurationType.DEVELOPMENT;
  }


  /**
   * @return true if application is to be deployed on GAE.
   */
  public boolean isDeploy() {
    return false;
  }
}
