# An example JMS sender.
#
# JMS objects are looked up and messages are created once during
# initialisation. This default JNDI names are for the WebLogic Server
# 7.0 examples domain - change accordingly.
#
# Each worker thread:
#  - Creates a queue session
#  - Sends ten messages
#  - Closes the queue session

from net.grinder.script import Test
from jarray import zeros
from java.util import Properties, Random
from javax.jms import Session
from javax.naming import Context, InitialContext
from weblogic.jndi import WLInitialContextFactory

# Look up connection factory and queue in JNDI. 
properties = Properties()
properties[Context.PROVIDER_URL] = "t3://localhost:7001"
properties[Context.INITIAL_CONTEXT_FACTORY] = WLInitialContextFactory.name

initialContext = InitialContext(properties)

connectionFactory = initialContext.lookup("weblogic.examples.jms.QueueConnectionFactory")
queue = initialContext.lookup("weblogic.examples.jms.exampleQueue")
initialContext.close()

# Create a connection.
connection = connectionFactory.createQueueConnection()
connection.start()

random = Random()

def createBytesMessage(session, size):
    bytes = zeros(size, 'b')
    random.nextBytes(bytes)
    message = session.createBytesMessage()
    message.writeBytes(bytes)
    return message

class TestRunner:
    def __call__(self):
        log = grinder.logger.output

        log("Creating queue session")
        session = connection.createQueueSession(0, Session.AUTO_ACKNOWLEDGE)

        sender = session.createSender(queue)
        instrumentedSender = Test(1, "Send a message").wrap(sender)

        message = createBytesMessage(session, 100)

        log("Sending ten messages")

        for i in range(0, 10):
            instrumentedSender.send(message)
            grinder.sleep(100)

        log("Closing queue session")
        session.close()
