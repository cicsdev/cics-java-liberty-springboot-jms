/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Copyright IBM Corp. 2020 All Rights Reserved   
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.cicsdev.springboot.jms;

import java.util.Random;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.cics.server.CicsException;
import com.ibm.cics.server.TSQ;

@Component
public class JMSMessageReceiver {
	private static final Random R = new Random(1);

	@Transactional
    @JmsListener(destination = "BROWNAD.REQUEST.QUEUE", containerFactory = "myFactory")
    public void receiveMessage(String data) throws CicsException {
        System.out.println("Received <" + data + ">");
        
        TSQ tsq = new TSQ();
        tsq.setName("SPRINGQ");
        tsq.writeString(data);
		
        if(R.nextBoolean()) {
        	System.out.println("Rolling back");
        	throw new RuntimeException("Expected exception");
        } else {
        	System.out.println("Committing");
        }
    }

}
