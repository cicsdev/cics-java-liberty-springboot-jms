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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class is to write a Rest Endpoint and use the JmsTemplate class to send
 * the JMS messages.
 * 
 * @RestController: build a Restful controller
 * @Autowired: drive Dependency Injection
 * @RequestMapping: write a Request URI method
 */

@RestController
public class SendJMSController {

	@Autowired
	private JmsTemplate jmsTemplate;

	@RequestMapping("/send")
	public String send(@RequestParam(value = "data") String data) {

		jmsTemplate.convertAndSend("BROWNAD.REQUEST.QUEUE", data);
		return data;

	}

}