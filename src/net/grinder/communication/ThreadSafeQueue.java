// Copyright (C) 2000, 2001, 2002, 2003 Philip Aston
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

import java.util.LinkedList;

import net.grinder.common.GrinderException;


/**
 * Thread-safe queue of <code>Objects</code>.
 *
 * @author Philip Aston
 * @version $Revision$
 **/
final class ThreadSafeQueue {

  private LinkedList m_messages = new LinkedList();
  private boolean m_shutdown = false;

  /**
   * Constructor.
   *
   */
  public ThreadSafeQueue() {
  }

  /**
   * Queue the given Object.
   *
   * @param item The object.
   * @exception ShutdownException If the queue has been shutdown.
   * @see #shutdown
   **/
  public void queue(Object item) throws ShutdownException {
    synchronized (getMutex()) {
      checkIfShutdown();
      m_messages.add(item);
      getMutex().notifyAll();
    }
  }

  /**
   * Dequeue an Object.
   *
   * @param block <code>true</code> => block until message is
   * available, <code>false</code => return <code>null</code> if no
   * message is available.
   * @exception ShutdownException If the queue has been shutdown.
   * @see #shutdown
   **/
  public Object dequeue(boolean block) throws ShutdownException {
    synchronized (getMutex()) {
      while (!m_shutdown && block && m_messages.size() == 0) {
        try {
          getMutex().wait();
        }
        catch (InterruptedException e) {
          shutdown();
        }
      }

      checkIfShutdown();

      if (m_messages.size() == 0) {
        return null;
      }
      else {
        getMutex().notifyAll();
        return m_messages.removeFirst();
      }
    }
  }

  /**
   * Shutdown the <code>MessageQueue</code>. Any <code>Objects</code>
   * in the queue are discarded.
   **/
  public void shutdown() {
    synchronized (getMutex()) {
      m_shutdown = true;
      m_messages.clear();
      getMutex().notifyAll();
    }
  }

  /**
   * Get the lock object which is used to control and notify changes
   * to the queue.
   *
   * @return The lock object.
   */
  public Object getMutex() {
    return m_messages;
  }

  /**
   * The size of the queue.
   *
   * @return The size of the queue.
   */
  public int getSize() {
    return m_messages.size();
  }

  private void checkIfShutdown() throws ShutdownException {
    if (m_shutdown) {
      throw new ShutdownException("MessageQueue shutdown");
    }
  }

  /**
   * Exception that indicates <code>ThreadSafeQueue</code> has been
   * shutdown. It doesn't extend {@link CommunicationException}
   * because typically callers want to propagate
   * <code>ShutdownException</code>s but handle
   * <code>CommunicationException</code>s locally.
   **/
  static final class ShutdownException extends GrinderException {
    private ShutdownException(String s) {
      super(s);
    }

    private ShutdownException(String s, Exception e) {
      super(s, e);
    }
  }
}
