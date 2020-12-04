/*                                                                        */
/* (c) Copyright IBM Corp. 2020 All Rights Reserved                       */
/*                                                                        */
package com.ibm.cicsdev.springboot.jms;



import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.cics.server.TSQ;

/**
 * 
 * This message driven POJO (MDP) receives a JMS message and writes the data to a CICS TSQ. 
 * Updates are either rolled back or committed based on the value of the string read from the MDB queue.
 * 
 * @Component: denote this class as Component.
 * @Transactional: Use Spring Boot managed transactions
 * @JmsListener: defines the name of the Destination that this method should listen to,  
 *  and supplies the reference to the JmsListenerContainerFactory used to create the 
 *  underlying message listener container.
 */
@Component
public class JMSMessageReceiver 
{
    
    // JMS queue to receive messages
    private static final String MDP_QUEUE = "SPRING.QUEUE";
    
    // CICS TSQ
    private static final String TSQ_NAME = "SPRINGQ";
	
	/**
	 * @param data, the message received from the MQ destination queue
	 * @throws Exception CicsConditionException
	 */
	@Transactional (rollbackFor=Exception.class)
	@JmsListener(destination = MDP_QUEUE, containerFactory = "myFactoryBean")
	public void receiveMessage(String data) throws Exception 
	{	
		System.out.println("Received <" + data + ">");
		
        // Use JCICS API to write data to a CICS TSQ
		// If TSQ write fails, CICSConditionException will be thrown and rollback the JTA Txn
		TSQ tsq = new TSQ();
		tsq.setName(TSQ_NAME);
		tsq.writeString(data);
		
		// If the TSQ is defined as a recoverable throwing an exception will rollback updates
		if (data.equalsIgnoreCase("rollback"))
		{   
			System.out.println("Rolling back");
			throw new Exception("Expected rollback exception");
		} 
		
		// Otherwise, commit (default action of @Transactional on method)
		else 
		{
			
			System.out.println("Committing");
		}
	}
}
