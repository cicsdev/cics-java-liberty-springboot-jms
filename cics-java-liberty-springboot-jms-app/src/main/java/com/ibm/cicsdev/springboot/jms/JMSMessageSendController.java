/*                                                                        */
/* (c) Copyright IBM Corp. 2020 All Rights Reserved                       */
/*                                                                        */

package com.ibm.cicsdev.springboot.jms;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 *  REST endpoint using the JmsTemplate class to send JMS messages.
 * 
 * @RestController: build a Restful controller
 * @Autowired: drive Dependency Injection
 * @RequestMapping: write a Request URI method
 */

@RestController
public class JMSMessageSendController 
{
    @Autowired
    private JmsTemplate jmsTemplate;
    
    
    /**
     * Root endpoint - returns date/time + usage information
     * 
     * @return the Usage information 
     */    
    @GetMapping("/")
    public String root() 
    {                        
        Date myDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss.SSSSSS");
        String myDateString = sdf.format(myDate);
        
        return "<h1>Spring Boot JMS REST sample usage: Date/Time: " + myDateString + "</h1>"
        + "<h3>Usage:</h3>"
        + "<b>/send/{queue}?data={input string}</b> - write input string to specified queue <br>"
        ;
    }        
    
    
    /**
     * @param inputStr, input data to be written to queue
     * @param jmsq, path variable for JMS queue name
     * @return, the JMS message to send to the MQ destination
     */
    @RequestMapping(value = "/send/{jmsq}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String send(
    		@RequestParam("data") String inputStr,
    		@PathVariable("jmsq") String jmsq)
    {       
        try 
        {
            jmsTemplate.convertAndSend(jmsq, inputStr);
        }
        catch (JmsException jre) 
        {
            return "JMSMessageSendController - ERROR on JMS send " + jre.getMessage();   

        }

        return inputStr;
    }

}