package ro.isdc.wro

import org.junit.After
import org.junit.Before
import org.junit.Test

import ro.isdc.wro.config.Context

class TestWroRuntimeException {
  var victim: WroRuntimeException = null

  @Before def setUp {
    Context.set(Context.standaloneContext())
  }

  @After def tearDown = Context.unset()

  @Test def test = print("first test")

}