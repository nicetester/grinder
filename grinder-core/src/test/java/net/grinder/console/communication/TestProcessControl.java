// Copyright (C) 2008 - 2012 Philip Aston
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
// COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.console.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Comparator;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.common.processidentity.ProcessReport;
import net.grinder.console.common.processidentity.StubAgentProcessReport;
import net.grinder.console.communication.ProcessControl.ProcessReports;
import net.grinder.engine.agent.StubAgentIdentity;
import net.grinder.messages.console.AgentAndCacheReport;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link ProcessControl.ProcessReportsComparator}.
 *
 * @author Philip Aston
 */
public class TestProcessControl {

  private final AgentIdentity m_agentIdentity =
      new StubAgentIdentity("my agent");

  private final AgentAndCacheReport m_agentProcessReport1 =
    new StubAgentProcessReport(m_agentIdentity, ProcessReport.State.RUNNING);
  private final AgentAndCacheReport agentProcessReport2 =
    new StubAgentProcessReport(m_agentIdentity, ProcessReport.State.FINISHED);

  @Mock private ProcessReports m_processReports1;
  @Mock private ProcessReports m_processReports2;

  @Before public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(m_processReports1.getAgentProcessReport())
      .thenReturn(m_agentProcessReport1);

    when(m_processReports2.getAgentProcessReport())
      .thenReturn(agentProcessReport2);
  }

  @Test public void testProcessReportsComparator() throws Exception {
    final Comparator<ProcessReports> comparator =
      new ProcessControl.ProcessReportsComparator();

    assertEquals(0, comparator.compare(m_processReports1, m_processReports1));
    assertEquals(0, comparator.compare(m_processReports2, m_processReports2));
    assertTrue(comparator.compare(m_processReports1, m_processReports2) < 0);
    assertTrue(comparator.compare(m_processReports2, m_processReports1) > 0);
  }
}
