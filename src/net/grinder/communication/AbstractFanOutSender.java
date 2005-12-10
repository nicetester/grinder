// Copyright (C) 2003, 2004, 2005 Philip Aston
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

package net.grinder.communication;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import net.grinder.util.thread.InterruptibleRunnable;
import net.grinder.util.thread.Kernel;


/**
 * Manages the sending of messages to many Receivers.
 *
 * @author Philip Aston
 * @version $Revision$
 */
abstract class AbstractFanOutSender extends AbstractSender {

  private final Kernel m_kernel;
  private final ResourcePool m_resourcePool;

  /**
   * Constructor.
   *
   * @param kernel Kernel to use.
   * @param resourcePool Pool of resources from which the output
   * streams can be reserved.
   */
  protected AbstractFanOutSender(Kernel kernel, ResourcePool resourcePool) {
    m_kernel = kernel;
    m_resourcePool = resourcePool;
  }

  /**
   * Send a message.
   *
   * @param message The message.
   * @exception IOException If an error occurs.
   */
  protected final void writeMessage(Message message)
    throws CommunicationException, IOException {

    try {
      // We reserve all the resources here and hand off the
      // reservations to WriteMessageToStream instances. This
      // guarantees order of messages to a given resource for this
      // AbstractFanOutSender.
      final Iterator iterator = m_resourcePool.reserveAll().iterator();

      while (iterator.hasNext()) {
        final ResourcePool.Reservation reservation =
          (ResourcePool.Reservation) iterator.next();

        final OutputStream outputStream =
          resourceToOutputStream(reservation.getResource());

        m_kernel.execute(
          new WriteMessageToStream(message, outputStream, reservation));
      }
    }
    catch (Kernel.ShutdownException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * Subclasses must implement this to return an output stream from a
   * resource.
   *
   * @param resource The resource.
   * @return The output stream.
   * @throws CommunicationException If the output stream could not be
   * obtained from the resource.
   */
  protected abstract OutputStream
    resourceToOutputStream(ResourcePool.Resource resource)
    throws CommunicationException;

  /**
   * Allow subclasses to access the resource pool.
   *
   * @return The resource pool.
   */
  protected final ResourcePool getResourcePool() {
    return m_resourcePool;
  }

  /**
   * Shut down this sender.
   */
  public void shutdown() {
    super.shutdown();

    m_kernel.gracefulShutdown();
  }

  private static final class WriteMessageToStream
    implements InterruptibleRunnable {

    private final Message m_message;
    private final OutputStream m_outputStream;
    private final ResourcePool.Reservation m_reservation;

    public WriteMessageToStream(Message message,
                                OutputStream outputStream,
                                ResourcePool.Reservation reservation) {
      m_message = message;
      m_outputStream = outputStream;
      m_reservation = reservation;
    }

    public void run() {
      try {
        writeMessageToStream(m_message, m_outputStream);
      }
      catch (IOException e) {
        // InterruptedIOExceptions take this path.
        m_reservation.close();
      }
      finally {
        m_reservation.free();
      }
    }
  }
}
