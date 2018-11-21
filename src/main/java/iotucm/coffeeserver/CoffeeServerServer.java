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

package iotucm.coffeeserver;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.logging.Logger;
import iotucm.coffeeserver.*;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class CoffeeServerServer {
  private static final Logger logger = Logger.getLogger(CoffeeServerServer.class.getName());

  private Server server;

  private void start() throws IOException {
    /* The port on which the server should run */
    int port = 50051;
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
        CoffeeServerServer.this.stop();
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
    final CoffeeServerServer server = new CoffeeServerServer();
    server.start();
    server.blockUntilShutdown();
  }

  static class CofeeServerImpl extends CoffeeServerGrpc.CoffeeServerImplBase {
    static int counter=10;	  
	@Override
	public void consumedCapsule(CapsuleConsumedRequest request, StreamObserver<CapsuleConsumedReply> responseObserver) {
		super.consumedCapsule(request, responseObserver);
		
	    CapsuleConsumedReply reply = null;
	    if (counter>5) {
	    		CapsuleConsumedReply.newBuilder().setExpectedRemaining(counter-1).setExpectedProvisionDate("No need, yet").build();
	    } else {
	    	CapsuleConsumedReply.newBuilder().setExpectedProvisionDate("11 of november of 2019").setExpectedRemaining(counter).build();
	    	counter=10;
	    }
	    
	    responseObserver.onNext(reply);
	    responseObserver.onCompleted();
	}
  
  }
}