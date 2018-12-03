/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package iotucm.coffeeservice;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import iotucm.coffeeservice.CapsuleConsumedReply;
import iotucm.coffeeservice.CapsuleConsumedRequest;
import iotucm.coffeeservice.CoffeeServerGrpc;
import iotucm.coffeeservice.*;

/** new methods */
import iotucm.coffeeservice.MachineStatus;
import iotucm.coffeeservice.AnalysisResults;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Coffee service implementation
 */
public class CoffeeServiceServer {
  private static final Logger logger = Logger.getLogger(CoffeeServiceServer.class.getName());

  private Server server;

  private void start(int port) throws IOException {
    /* The port on which the server should run */
    server = ServerBuilder.forPort(port)
        .addService(new CofeeServerImpl())
        .build()
        .start();
    logger.info("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        CoffeeServiceServer.this.stop();
        System.err.println("*** server shut down");
      }
    });
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  /**
   * Main launches the server from the command line.
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    final CoffeeServiceServer server = new CoffeeServiceServer();
    int port = 50051;
    if (args.length > 0) {
    	port=Integer.parseInt(args[0]);
    }
    server.start(port);
    server.blockUntilShutdown();
  }

  static class CofeeServerImpl extends CoffeeServerGrpc.CoffeeServerImplBase {
    static int counter=10;	  
	@Override
	public void consumedCapsule(CapsuleConsumedRequest request, StreamObserver<CapsuleConsumedReply> responseObserver) {
		// Never call super methods, otherwise you will get "call is closed" and "unimplemented method" errors
	
	    CapsuleConsumedReply reply = null;
	    if (counter>5) {
	    	reply=CapsuleConsumedReply.newBuilder().setExpectedRemaining(counter).setExpectedProvisionDate("No need, yet").build();	    
	    } else {
	    	reply=CapsuleConsumedReply.newBuilder().setExpectedProvisionDate("11 of november of 2019").setExpectedRemaining(counter).build();
	    	counter=10;
	    }
	    counter=counter-1;
	    
	    responseObserver.onNext(reply);
	    responseObserver.onCompleted();
	}
  
     // New method
     public void checkMachineStatus(MachineStatus mstatus, StreamObserver<AnalysisResults> responseObserver) {
		AnalysisResults result = null;
		if (mstatus.getWaterTemperature() < 85) {
		  result = AnalysisResults.newBuilder().setStatus(false).setMachineProblem("Water is too cold").setDateTechnician("tomorrow").build();
		}
		else if (mstatus.getWaterTemperature() > 100) {
		  result = AnalysisResults.newBuilder().setStatus(false).setMachineProblem("Water is too hot").setDateTechnician("tomorrow").build();
		}
		else if (mstatus.getCapsulePressure() < 9) {
		  result = AnalysisResults.newBuilder().setStatus(false).setMachineProblem("Capsule pressure is too low").setDateTechnician("tomorrow").build();
		}
		else if (mstatus.getCapsulePressure() > 19) {
		  result = AnalysisResults.newBuilder().setStatus(false).setMachineProblem("Capsule pressure is too high").setDateTechnician("tomorrow").build();
		}
		else {
		  result = AnalysisResults.newBuilder().setStatus(true).setMachineProblem("Everything ok").setDateTechnician("not yet").build();
		}
		responseObserver.onNext(result);
	     responseObserver.onCompleted();
	}
  }
}
