/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.axis.engine;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisGlobal;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.AxisTransport;
import org.apache.axis.description.HandlerMetadata;
import org.apache.axis.description.Parameter;
import org.apache.axis.handlers.AbstractHandler;
import org.apache.axis.om.OMFactory;
import org.apache.axis.providers.AbstractProvider;
import org.apache.axis.transport.TransportSender;
import org.apache.wsdl.WSDLService;

public class EngineTest extends TestCase {
   private MessageContext mc;
   private ArrayList executedHandlers = new ArrayList();
   private EngineRegistry engineRegistry;
   private QName serviceName = new QName("NullService");

   public EngineTest() {
   }

   public EngineTest(String arg0) {
       super(arg0);
   }
   protected void setUp() throws Exception {
       engineRegistry = new EngineRegistryImpl(new AxisGlobal());

       AxisTransport transport = new AxisTransport(new QName("null"));
       transport.setSender(new NullTransportSender());
       mc = new MessageContext(engineRegistry, null, null, transport);
       mc.setServerSide(true);
       OMFactory omFac = OMFactory.newInstance();
       mc.setEnvelope(omFac.getDefaultEnvelope());
       AxisService service = new AxisService(serviceName);
       service.setProvider(new NullProvider());
       engineRegistry.addService(service);
       service.setStyle(WSDLService.STYLE_DOC);
       mc.setTo(
           new EndpointReference(
               AddressingConstants.WSA_TO,
               "http://127.0.0.1:8080/axis/services/NullService"));

   }

   public void testSend() throws Exception {
       AxisEngine engine = new AxisEngine();
       engine.receive(mc);
   }

   public class TempHandler extends AbstractHandler {
       private Integer index;
       private boolean pause = false;
       public TempHandler(int index, boolean pause) {
           this.index = new Integer(index);
           this.pause = pause;
       }
       public TempHandler(int index) {
           this.index = new Integer(index);
       }

       public void invoke(MessageContext msgContext) throws AxisFault {
           executedHandlers.add(index);
           if (pause) {
               msgContext.setPaused(true);
           }
       }

   }

   public class NullProvider extends AbstractProvider {
       public MessageContext invoke(MessageContext msgCtx) throws AxisFault {
           MessageContext newCtx =
               new MessageContext(
                   msgCtx.getGlobalContext().getRegistry(),
                   msgCtx.getProperties(),
                   msgCtx.getSessionContext(),msgCtx.getTransport());
           newCtx.setEnvelope(msgCtx.getEnvelope());
           return newCtx;
       }

   }

   public class NullTransportSender implements TransportSender {
       public void cleanup() throws AxisFault {
       }

       public QName getName() {
           return null;
       }

       public Parameter getParameter(String name) {
           return null;
       }

       public void init(HandlerMetadata handlerdesc) {
       }

       public void invoke(MessageContext msgContext) throws AxisFault {
       }

       public void revoke(MessageContext msgContext) {
       }

   }


}
