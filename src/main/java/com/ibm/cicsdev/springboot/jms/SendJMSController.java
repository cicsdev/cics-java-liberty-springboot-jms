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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Rest Endpoint using the JmsTemplate class to send JMS messages.
 * 
 * @RestController: build a Restful controller
 * @Autowired: drive Dependency Injection
 * @RequestMapping: write a Request URI method
 */

@RestController
public class SendJMSController 
{
	@Autowired
	private JmsTemplate jmsTemplate;

	
	@GetMapping("/")
	public String root() 
	{						
		return "Spring Boot JMS REST sample usage: /send";
	}	
	
	
	/**
	 * @param data, the object received from a Rest Endpoint
	 * @return, the JMS message that needs to be send to the MQ destination
	 */
	@RequestMapping("/send")
	public String send(@RequestParam(value = "data") String data) 
	{
		jmsTemplate.convertAndSend("BROWNAD.REQUEST.QUEUE", data);
		return data;
	}

}