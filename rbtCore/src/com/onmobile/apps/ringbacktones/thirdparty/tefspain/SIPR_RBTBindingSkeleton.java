/**
 * SIPR_RBTBindingSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.onmobile.apps.ringbacktones.thirdparty.tefspain;

public class SIPR_RBTBindingSkeleton implements SIPR_RBTPortType, org.apache.axis.wsdl.Skeleton {
    private SIPR_RBTPortType impl;
    private static java.util.Map _myOperations = new java.util.Hashtable();
    private static java.util.Collection _myOperationsList = new java.util.ArrayList();

    /**
    * Returns List of OperationDesc objects with this name
    */
    public static java.util.List getOperationDescByName(java.lang.String methodName) {
        return (java.util.List)_myOperations.get(methodName);
    }

    /**
    * Returns Collection of OperationDescs
    */
    public static java.util.Collection getOperationDescs() {
        return _myOperationsList;
    }

    static {
        org.apache.axis.description.OperationDesc _oper;
        org.apache.axis.description.FaultDesc _fault;
        org.apache.axis.description.ParameterDesc [] _params;
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "melodia"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "tipoUsuario"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "franjaHoraria"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("altaSuscriptorEmpresa", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "AltaSuscriptorEmpresa"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("altaSuscriptorEmpresa") == null) {
            _myOperations.put("altaSuscriptorEmpresa", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("altaSuscriptorEmpresa")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("consultaSuscriptorEmpresa", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://localhost/SIPR_RBT.wsdl", "ArrayOf_xsd_string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "ConsultaSuscriptorEmpresa"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("consultaSuscriptorEmpresa") == null) {
            _myOperations.put("consultaSuscriptorEmpresa", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("consultaSuscriptorEmpresa")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "melodia"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "tipoUsuario"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "franjaHoraria"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("modificaSuscriptorEmpresa", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "ModificaSuscriptorEmpresa"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("modificaSuscriptorEmpresa") == null) {
            _myOperations.put("modificaSuscriptorEmpresa", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("modificaSuscriptorEmpresa")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "melodia"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("bajaSuscriptorEmpresa", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "BajaSuscriptorEmpresa"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("bajaSuscriptorEmpresa") == null) {
            _myOperations.put("bajaSuscriptorEmpresa", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("bajaSuscriptorEmpresa")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("confirmAlta", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "ConfirmAlta"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("confirmAlta") == null) {
            _myOperations.put("confirmAlta", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("confirmAlta")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("confirmBaja", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "ConfirmBaja"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("confirmBaja") == null) {
            _myOperations.put("confirmBaja", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("confirmBaja")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "ViejoMSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "NuevoMSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("cambioMSISDN", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "CambioMSISDN"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("cambioMSISDN") == null) {
            _myOperations.put("cambioMSISDN", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("cambioMSISDN")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("bajaServicio", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "BajaServicio"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("bajaServicio") == null) {
            _myOperations.put("bajaServicio", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("bajaServicio")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("consultaEstado", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "ConsultaEstado"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("consultaEstado") == null) {
            _myOperations.put("consultaEstado", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("consultaEstado")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "resul"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("respuestaAlta", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "RespuestaAlta"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("respuestaAlta") == null) {
            _myOperations.put("respuestaAlta", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("respuestaAlta")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "resul"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("respuestaBaja", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "RespuestaBaja"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("respuestaBaja") == null) {
            _myOperations.put("respuestaBaja", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("respuestaBaja")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "id_melodia"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("altaRBTCC", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "AltaRBTCC"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("altaRBTCC") == null) {
            _myOperations.put("altaRBTCC", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("altaRBTCC")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "indEmpresa"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("bajaRBTCC", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "BajaRBTCC"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("bajaRBTCC") == null) {
            _myOperations.put("bajaRBTCC", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("bajaRBTCC")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "FecCompra"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "id_melodia"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("compraRBTCC", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "CompraRBTCC"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("compraRBTCC") == null) {
            _myOperations.put("compraRBTCC", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("compraRBTCC")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("altaRBTPubliCC", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "AltaRBTPubliCC"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("altaRBTPubliCC") == null) {
            _myOperations.put("altaRBTPubliCC", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("altaRBTPubliCC")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("bajaRBTPubliCC", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "BajaRBTPubliCC"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("bajaRBTPubliCC") == null) {
            _myOperations.put("bajaRBTPubliCC", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("bajaRBTPubliCC")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "id_melodia"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("consultaRBTPrecio", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://localhost/SIPR_RBT.wsdl", "ArrayOf_xsd_string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "ConsultaRBTPrecio"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("consultaRBTPrecio") == null) {
            _myOperations.put("consultaRBTPrecio", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("consultaRBTPrecio")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("consultaRBTDefecto", _params, new javax.xml.namespace.QName("", "id_melodia_defecto"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "ConsultaRBTDefecto"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("consultaRBTDefecto") == null) {
            _myOperations.put("consultaRBTDefecto", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("consultaRBTDefecto")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDN"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "MSISDNDEST"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("regaloYavoyCC", _params, new javax.xml.namespace.QName("", "res"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:SIPR_RBT", "RegaloYavoyCC"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("regaloYavoyCC") == null) {
            _myOperations.put("regaloYavoyCC", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("regaloYavoyCC")).add(_oper);
    }

    public SIPR_RBTBindingSkeleton() {
        this.impl = new SIPR_RBTBindingImpl();
    }

    public SIPR_RBTBindingSkeleton(SIPR_RBTPortType impl) {
        this.impl = impl;
    }
    public int altaSuscriptorEmpresa(java.lang.String MSISDN, int melodia, int tipoUsuario, java.lang.String franjaHoraria) throws java.rmi.RemoteException
    {
        int ret = impl.altaSuscriptorEmpresa(MSISDN, melodia, tipoUsuario, franjaHoraria);
        return ret;
    }

    public java.lang.String[] consultaSuscriptorEmpresa(java.lang.String MSISDN) throws java.rmi.RemoteException
    {
        java.lang.String[] ret = impl.consultaSuscriptorEmpresa(MSISDN);
        return ret;
    }

    public int modificaSuscriptorEmpresa(java.lang.String MSISDN, int melodia, int tipoUsuario, java.lang.String franjaHoraria) throws java.rmi.RemoteException
    {
        int ret = impl.modificaSuscriptorEmpresa(MSISDN, melodia, tipoUsuario, franjaHoraria);
        return ret;
    }

    public int bajaSuscriptorEmpresa(java.lang.String MSISDN, int melodia) throws java.rmi.RemoteException
    {
        int ret = impl.bajaSuscriptorEmpresa(MSISDN, melodia);
        return ret;
    }

    public int confirmAlta(java.lang.String MSISDN) throws java.rmi.RemoteException
    {
        int ret = impl.confirmAlta(MSISDN);
        return ret;
    }

    public int confirmBaja(java.lang.String MSISDN) throws java.rmi.RemoteException
    {
        int ret = impl.confirmBaja(MSISDN);
        return ret;
    }

    public int cambioMSISDN(java.lang.String viejoMSISDN, java.lang.String nuevoMSISDN) throws java.rmi.RemoteException
    {
        int ret = impl.cambioMSISDN(viejoMSISDN, nuevoMSISDN);
        return ret;
    }

    public int bajaServicio(java.lang.String MSISDN) throws java.rmi.RemoteException
    {
        int ret = impl.bajaServicio(MSISDN);
        return ret;
    }

    public int consultaEstado(java.lang.String MSISDN) throws java.rmi.RemoteException
    {
        int ret = impl.consultaEstado(MSISDN);
        return ret;
    }

    public int respuestaAlta(java.lang.String MSISDN, int resul) throws java.rmi.RemoteException
    {
        int ret = impl.respuestaAlta(MSISDN, resul);
        return ret;
    }

    public int respuestaBaja(java.lang.String MSISDN, int resul) throws java.rmi.RemoteException
    {
        int ret = impl.respuestaBaja(MSISDN, resul);
        return ret;
    }

    public int altaRBTCC(java.lang.String MSISDN, java.lang.String id_melodia) throws java.rmi.RemoteException
    {
        int ret = impl.altaRBTCC(MSISDN, id_melodia);
        return ret;
    }

    public int bajaRBTCC(java.lang.String MSISDN, java.lang.String indEmpresa) throws java.rmi.RemoteException
    {
        int ret = impl.bajaRBTCC(MSISDN, indEmpresa);
        return ret;
    }

    public int compraRBTCC(java.lang.String MSISDN, java.lang.String fecCompra, java.lang.String id_melodia) throws java.rmi.RemoteException
    {
        int ret = impl.compraRBTCC(MSISDN, fecCompra, id_melodia);
        return ret;
    }

    public int altaRBTPubliCC(java.lang.String MSISDN) throws java.rmi.RemoteException
    {
        int ret = impl.altaRBTPubliCC(MSISDN);
        return ret;
    }

    public int bajaRBTPubliCC(java.lang.String MSISDN) throws java.rmi.RemoteException
    {
        int ret = impl.bajaRBTPubliCC(MSISDN);
        return ret;
    }

    public java.lang.String[] consultaRBTPrecio(java.lang.String MSISDN, java.lang.String id_melodia) throws java.rmi.RemoteException
    {
        java.lang.String[] ret = impl.consultaRBTPrecio(MSISDN, id_melodia);
        return ret;
    }

    public java.lang.String consultaRBTDefecto(java.lang.String MSISDN) throws java.rmi.RemoteException
    {
        java.lang.String ret = impl.consultaRBTDefecto(MSISDN);
        return ret;
    }

    public int regaloYavoyCC(java.lang.String MSISDN, java.lang.String MSISDNDEST) throws java.rmi.RemoteException
    {
        int ret = impl.regaloYavoyCC(MSISDN, MSISDNDEST);
        return ret;
    }

}
