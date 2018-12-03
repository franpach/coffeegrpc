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

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import iotucm.coffeeservice.CapsuleConsumedReply;
import iotucm.coffeeservice.CapsuleConsumedRequest;
import iotucm.coffeeservice.CoffeeServerGrpc;
import iotucm.coffeeservice.*;

/** new methods */
import iotucm.coffeeservice.MachineStatus;
import iotucm.coffeeservice.AnalysisResults;

/**
 * A simple client that requests a greeting from the {@link CoffeeServerServer}.
 */
public class CoffeeServerClient {
  private static final Logger logger = Logger.getLogger(CoffeeServerClient.class.getName());

  private final ManagedChannel channel;
  private final CoffeeServerGrpc.CoffeeServerBlockingStub blockingStub;

  /** Construct client connecting to HelloWorld server at {@code host:port}. */
  public CoffeeServerClient(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port)
        // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
        // needing certificates.
        .usePlaintext()
        .build());
  }

  /** Construct client for accessing HelloWorld server using the existing channel. */
  CoffeeServerClient(ManagedChannel channel) {
    this.channel = channel;
    blockingStub = CoffeeServerGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  /** A capsule is consumed */
  public void consumeCapsule(String clientid, String type) {
    logger.info("Sending out the consumption of capsule by " + clientid + " of type "+type);
    CapsuleConsumedRequest request = CapsuleConsumedRequest.newBuilder().setClientid(clientid).setType(type).build();
    CapsuleConsumedReply response;
    try {
      response = blockingStub.consumedCapsule(request);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }
    if (response.getSupplyProvisioned()!=0)
      logger.info("Result: expect a new deliver by " + response.getExpectedProvisionDate());
    else 
    	logger.info("There is still coffee. Expected remaining " + response.getExpectedRemaining());
  }

  /** Checks machine status*/
  public void checkMachineStatus(float watertemperature, int timeconnected, float capsulepressure) {
	logger.info("Checking machine status if water temperature is " + watertemperature + "degrees, total time connected is " + timeconnected + " and the capsule pressure is " + capsulepressure + " bars");
	MachineStatus mstatus = MachineStatus.newBuilder().setWaterTemperature(watertemperature).setTimeConnected(timeconnected).setCapsulePressure(capsulepressure).build();
	AnalysisResults result;
	try {
	  result = blockingStub.checkMachineStatus(mstatus);
	} catch (StatusRuntimeException e) {
	  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
       return;
	}
	if (result.getStatus() == true) 
	  logger.info("Result: machine status is ok!");
	else
	  logger.info("There is a problem with the machine: " + result.getMachineProblem() + " . Expected technician delivery: "+ result.getDateTechnician());
  }

  /**
   * Coffee server code. The first argument is the client id, the second, the capsule type, the fourth the server ip, the fifth the port.
   */
  public static void main(String[] args) throws Exception {
	 String clientid = "myclientid";
      String capsuletype= "ristretto";
	 float waterTemperature = 90;
	 int timeConnected = 100;
      float capsulePressure = 19;
      int port=50051;
	 int typeOp = 1; 
      String host="localhost";
      if (args.length > 0) {
    	  clientid = args[0]; /* First argument is the clientid */
      }
      if (args.length > 1) {
	  try {
		Integer.parseInt(args[1]);
		waterTemperature = Float.parseFloat(args[1]);
		if (args.length > 2) {
    	 	 timeConnected = Integer.parseInt(args[2]); /* third argument is timeConnected */      
       	}
		if (args.length > 3) {
    	 	 capsulePressure = Float.parseFloat(args[3]); /* fourth argument is capsule pressure */      
       	}
 		if (args.length > 4) {
    	 	 port = Integer.parseInt(args[4]); /* fifth argument is the listening port */
       	}
		typeOp=1;		
	  } catch (NumberFormatException e) {
		capsuletype = args[1]; /* second argument is the capsule type */
		if (args.length > 2) {
    	  	 host = args[2]; /* third argument is the host */      
      	}
      	if (args.length > 3) {
    	  	 port = Integer.parseInt(args[3]); /* fourth argument is the listening port */
      	}
		typeOp=2;
	  } finally {
		CoffeeServerClient client = new CoffeeServerClient(host, port);
	     try {   
		  if(typeOp==2)   
			  client.consumeCapsule(clientid,capsuletype);
		  else
			  client.checkMachineStatus(waterTemperature, timeConnected, capsulePressure);
	     } finally {
		  client.shutdown();
	     }
	  }
    } 
      
  }
}
