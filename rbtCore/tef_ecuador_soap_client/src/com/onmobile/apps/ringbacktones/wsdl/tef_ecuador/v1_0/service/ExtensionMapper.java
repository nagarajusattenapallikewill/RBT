
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:34:40 IST)
 */

        
            package com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://apps.onmobile.com/ringbacktones/wsdl/tef_ecuador/V1_0/service".equals(namespaceURI) &&
                  "Subscriber_type0".equals(typeName)){
                   
                            return  com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.Subscriber_type0.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://apps.onmobile.com/ringbacktones/wsdl/tef_ecuador/V1_0/service".equals(namespaceURI) &&
                  "Rbt_type0".equals(typeName)){
                   
                            return  com.onmobile.apps.ringbacktones.wsdl.tef_ecuador.v1_0.service.Rbt_type0.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    