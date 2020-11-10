/* Licensed Materials - Property of IBM                                   */
/*                                                                        */
/* SAMPLE                                                                 */
/*                                                                        */
/* (c) Copyright IBM Corp. 2020 All Rights Reserved                       */
/*                                                                        */
/* US Government Users Restricted Rights - Use, duplication or disclosure */
/* restricted by GSA ADP Schedule Contract with IBM Corp                  */
/*                                                                        */
package com.ibm.cicsdev.springboot.jms;

import java.util.Random;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.cics.server.CicsException;
import com.ibm.cics.server.TSQ;

/**
 * 
 * This class is to receive the message and write the data to a CICS TSQ "SPRINGQ". 
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
	private static final Random R = new Random(1);

	
	/**
	 * @param data, the message received from the MQ destination
	 * @throws CicsException
	 */
	@Transactional
	@JmsListener(destination = "SPRING.QUEUE", containerFactory = "myFactoryBean")
	public void receiveMessage(String data) throws CicsException 
	{	
		System.out.println("Received <" + data + ">");
		
        // Use JCICS API to write data to a CICS TSQ "SPRINGQ"
		TSQ tsq = new TSQ();
		tsq.setName("SPRINGQ");
		tsq.writeString(data);

		if (R.nextBoolean()) 
		{		
			// If the SPRINGQ TSQ is defined as a recoverable resource in CICS, 
			// then throwing this exception will cause it to be rolledbacked
			System.out.println("Rolling back");
			throw new RuntimeException("Expected exception");
		} 
		else 
		{
			// Otherwise, commit (default action of @Transactional on method)
			System.out.println("Committing");
		}
	}
}
