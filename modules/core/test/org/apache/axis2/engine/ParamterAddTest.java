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

package org.apache.axis2.engine;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterImpl;

import javax.xml.namespace.QName;
/**
 * To chcek locked is working corrcetly
 */

public class ParamterAddTest extends TestCase {

    private AxisConfiguration reg = new AxisConfiguration();
    public void testAddParamterServiceLockedAtAxisConfig(){
        try {
            Parameter para = new ParameterImpl();
            para.setValue(null);
            para.setName("PARA_NAME");
            para.setLocked(true);
            reg.addParameter(para);

            AxisService service = new AxisService(new QName("Service1"));
            reg.addService(service);
            service.addParameter(para);
            fail("This should fails with Parmter is locked can not overide");
        } catch (AxisFault axisFault) {

        }
    }

     public void testAddParamterModuleLockedAtAxisConfig(){
        try {
            Parameter para = new ParameterImpl();
            para.setValue(null);
            para.setName("PARA_NAME");
            para.setLocked(true);
            reg.addParameter(para);
            ModuleDescription module = new ModuleDescription(new QName("Service1"));
            module.setParent(reg);
            module.addParameter(para);
            fail("This should fails with Parmter is locked can not overide");
        } catch (AxisFault axisFault) {

        }
    }

     public void testAddParamterOperationlockedByAxisConfig(){
        try {
            Parameter para = new ParameterImpl();
            para.setValue(null);
            para.setName("PARA_NAME");
            para.setLocked(true);
            reg.addParameter(para);

            AxisService service = new AxisService(new QName("Service1"));
            reg.addService(service);

            AxisOperation opertion = new InOutAxisOperation();
            opertion.setParent(service);
            opertion.addParameter(para);
            fail("This should fails with Parmter is locked can not overide");


        } catch (AxisFault axisFault) {

        }
    }

     public void testAddParamterOperationLockebyService(){
        try {
            Parameter para = new ParameterImpl();
            para.setValue(null);
            para.setName("PARA_NAME");
            para.setLocked(true);

            AxisService service = new AxisService(new QName("Service1"));
            reg.addService(service);
            service.addParameter(para);

            AxisOperation opertion = new InOutAxisOperation();
            opertion.setParent(service);
            opertion.addParameter(para);
            fail("This should fails with Parmter is locked can not overide");
        } catch (AxisFault axisFault) {

        }
    }


}
