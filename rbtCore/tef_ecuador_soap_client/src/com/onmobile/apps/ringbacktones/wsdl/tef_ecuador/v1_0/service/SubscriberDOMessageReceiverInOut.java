
/**
 * SubscriberDOMessageReceiverInOut.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */
        package com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service;

        /**
        *  SubscriberDOMessageReceiverInOut message receiver
        */

        public class SubscriberDOMessageReceiverInOut extends org.apache.axis2.receivers.AbstractInOutMessageReceiver{


        public void invokeBusinessLogic(org.apache.axis2.context.MessageContext msgContext, org.apache.axis2.context.MessageContext newMsgContext)
        throws org.apache.axis2.AxisFault{

        try {

        // get the implementation class for the Web Service
        Object obj = getTheImplementationObject(msgContext);

        SubscriberDOSkeletonInterface skel = (SubscriberDOSkeletonInterface)obj;
        //Out Envelop
        org.apache.axiom.soap.SOAPEnvelope envelope = null;
        //Find the axisOperation that has been set by the Dispatch phase.
        org.apache.axis2.description.AxisOperation op = msgContext.getOperationContext().getAxisOperation();
        if (op == null) {
        throw new org.apache.axis2.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
        }

        java.lang.String methodName;
        if((op.getName() != null) && ((methodName = org.apache.axis2.util.JavaUtils.xmlNameToJavaIdentifier(op.getName().getLocalPart())) != null)){


        

            if("deactivateSubscriber".equals(methodName)){
                
                com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.SubscriberResponse subscriberResponse3 = null;
	                        com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.DeactivateSubscriber wrappedParam =
                                                             (com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.DeactivateSubscriber)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.DeactivateSubscriber.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               subscriberResponse3 =
                                                   
                                                   
                                                         skel.deactivateSubscriber(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), subscriberResponse3, false, new javax.xml.namespace.QName("http://apps.onmobile.com/ringbacktones/wsdl/tef_ecuador/V1_0/service",
                                                    "deactivateSubscriber"));
                                    
            } else {
              throw new java.lang.RuntimeException("method not found");
            }
        

        newMsgContext.setEnvelope(envelope);
        }
        }
        catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
        }
        
        //
            private  org.apache.axiom.om.OMElement  toOM(com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.DeactivateSubscriber param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.DeactivateSubscriber.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.SubscriberResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.SubscriberResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.SubscriberResponse param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.SubscriberResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.SubscriberResponse wrapdeactivateSubscriber(){
                                com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.SubscriberResponse wrappedElement = new com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.SubscriberResponse();
                                return wrappedElement;
                         }
                    


        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
        return factory.getDefaultEnvelope();
        }


        private  java.lang.Object fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{

        try {
        
                if (com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.DeactivateSubscriber.class.equals(type)){
                
                           return com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.DeactivateSubscriber.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.SubscriberResponse.class.equals(type)){
                
                           return com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.SubscriberResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
        } catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
           return null;
        }



    

        /**
        *  A utility method that copies the namepaces from the SOAPEnvelope
        */
        private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env){
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
        org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
        returnMap.put(ns.getPrefix(),ns.getNamespaceURI());
        }
        return returnMap;
        }

        private org.apache.axis2.AxisFault createAxisFault(java.lang.Exception e) {
        org.apache.axis2.AxisFault f;
        Throwable cause = e.getCause();
        if (cause != null) {
            f = new org.apache.axis2.AxisFault(e.getMessage(), cause);
        } else {
            f = new org.apache.axis2.AxisFault(e.getMessage());
        }

        return f;
    }

        }//end of class
    