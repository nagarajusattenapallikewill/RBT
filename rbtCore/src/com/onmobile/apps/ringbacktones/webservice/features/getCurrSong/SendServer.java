package com.onmobile.apps.ringbacktones.webservice.features.getCurrSong;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportFactory;

public class SendServer {

  public static SendHandler handler;

  public static SendData.Processor processor;

  public static void main(String [] args) {
    try {
      handler = new SendHandler();
      processor = new SendData.Processor(handler);

      Runnable simple = new Runnable() {
        public void run() {
          simple(processor);
        }
      };      
     
      new Thread(simple).start();
    } catch (Exception x) {
      x.printStackTrace();
    }
  }

  public static void simple(SendData.Processor processor) {
    try {
      TServerTransport serverTransport = new TServerSocket(9081);
      //TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));
      TThreadPoolServer.Args serverArgs = new TThreadPoolServer.Args(serverTransport).processor(processor).
    	        transportFactory(new TTransportFactory()).
    	        protocolFactory(new TBinaryProtocol.Factory()).
    	        minWorkerThreads(10).
    	        maxWorkerThreads(20);
     /*TThreadPoolServer.Args serverArgs = new TThreadPoolServer.Args(serverTransport).processor(processor);*/
      
      TThreadPoolServer server = new TThreadPoolServer(serverArgs);

			System.out.println("Starting the multithreaded server...");
			server.serve();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
 
}
