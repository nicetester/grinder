// Copyright (C) 2004 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.engine.process;

import junit.framework.TestCase;

import java.util.List;
import java.util.NoSuchElementException;

import net.grinder.common.Logger;
import net.grinder.engine.EngineException;
import net.grinder.plugininterface.GrinderPlugin;
import net.grinder.script.Grinder.ScriptContext;
import net.grinder.testutility.CallRecorder;
import net.grinder.testutility.CallRecorder.CallData;
import net.grinder.testutility.RandomStubFactory;


/**
 * Unit test case for <code>PluginRegistry</code>.
 *
 * @author Philip Aston
 * @version $Revision$
 */
public class TestPluginRegistry extends TestCase {
  public TestPluginRegistry(String name) {
    super(name);
  }

  public void testConstructorAndSingleton() throws Exception {
    final RandomStubFactory loggerStubFactory =
      new RandomStubFactory(Logger.class);
    final Logger logger = (Logger)loggerStubFactory.getStub();

    final RandomStubFactory scriptContextStubFactory =
      new RandomStubFactory(ScriptContext.class);
    final ScriptContext scriptContext =
      (ScriptContext)scriptContextStubFactory.getStub();

    final ThreadContextLocator threadContextLocator =
      new StubThreadContextLocator();

    final PluginRegistry pluginRegistry =
      new PluginRegistry(logger, scriptContext, threadContextLocator);

    PluginRegistry.setInstance(pluginRegistry);
    assertEquals(pluginRegistry, PluginRegistry.getInstance());

    PluginRegistry.setInstance(null);
    assertNull(PluginRegistry.getInstance());
  }

  public void testRegister() throws Exception {
    final RandomStubFactory loggerStubFactory =
      new RandomStubFactory(Logger.class);
    final Logger logger = (Logger)loggerStubFactory.getStub();

    final RandomStubFactory scriptContextStubFactory =
      new RandomStubFactory(ScriptContext.class);
    final ScriptContext scriptContext =
      (ScriptContext)scriptContextStubFactory.getStub();

    final ThreadContextLocator threadContextLocator =
      new StubThreadContextLocator();

    final PluginRegistry pluginRegistry =
      new PluginRegistry(logger, scriptContext, threadContextLocator);

    final RandomStubFactory grinderPluginStubFactory =
      new RandomStubFactory(GrinderPlugin.class);
    final GrinderPlugin grinderPlugin =
      (GrinderPlugin)grinderPluginStubFactory.getStub();

    pluginRegistry.register(grinderPlugin);

    final CallData callData = skipHashCodeCalls(grinderPluginStubFactory);
    assertEquals("initialize", callData.getMethodName());

    final Object[] parameters = callData.getParameters();
    assertEquals(1, parameters.length);

    final RegisteredPlugin registeredPlugin = (RegisteredPlugin)parameters[0];
    assertEquals(scriptContext, registeredPlugin.getScriptContext());

    skipHashCodeCalls(grinderPluginStubFactory);
    grinderPluginStubFactory.assertNotCalled();

    loggerStubFactory.assertSuccess("output", new Class[] { String.class });
    loggerStubFactory.assertNotCalled();

    pluginRegistry.register(grinderPlugin);

    skipHashCodeCalls(grinderPluginStubFactory);
    grinderPluginStubFactory.assertNotCalled();
    loggerStubFactory.assertNotCalled();
  }

  private static final CallData skipHashCodeCalls(CallRecorder callRecorder) {

    try {
      while (true) {
        final CallData callData = callRecorder.getCallData();

        if (!"hashCode".equals(callData.getMethodName())) {
          return callData;
        }
      }
    }
    catch (NoSuchElementException e) {
      return null;
    }
  }

  public void testGetPluginThreadListenerList() throws Exception {
    final RandomStubFactory loggerStubFactory =
      new RandomStubFactory(Logger.class);
    final Logger logger = (Logger)loggerStubFactory.getStub();

    final PluginRegistry pluginRegistry =
      new PluginRegistry(logger, null, null);

    final RandomStubFactory threadContextStubFactory =
      new RandomStubFactory(ThreadContext.class);
    final ThreadContext threadContext =
      (ThreadContext)threadContextStubFactory.getStub();

    final List list1 =
      pluginRegistry.getPluginThreadListenerList(threadContext);

    assertEquals(0, list1.size());

    final RandomStubFactory grinderPluginStubFactory1 =
      new RandomStubFactory(GrinderPlugin.class);
    final GrinderPlugin grinderPlugin1 =
      (GrinderPlugin)grinderPluginStubFactory1.getStub();

    final RandomStubFactory grinderPluginStubFactory2 =
      new RandomStubFactory(GrinderPlugin.class);
    final GrinderPlugin grinderPlugin2 =
      (GrinderPlugin)grinderPluginStubFactory2.getStub();

    pluginRegistry.register(grinderPlugin1);
    pluginRegistry.register(grinderPlugin2);

    

  }
}
