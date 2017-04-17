/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.stage.origin.coapserver;

import com.streamsets.pipeline.api.Stage;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class CoapReceiverServer {
  private static final Logger LOG = LoggerFactory.getLogger(CoapReceiverServer.class);
  private CoapServerConfigs coAPServerConfigs;
  private final CoapReceiver receiver;
  private final BlockingQueue<Exception> errorQueue;
  private CoapServer coapServer;
  private CoapReceiverResource coapReceiverResource;

  CoapReceiverServer(CoapServerConfigs coAPServerConfigs, CoapReceiver receiver, BlockingQueue<Exception> errorQueue) {
    this.coAPServerConfigs = coAPServerConfigs;
    this.receiver = receiver;
    this.errorQueue = errorQueue;
  }

  public List<Stage.ConfigIssue> init(Stage.Context context) {
    List<Stage.ConfigIssue> issues = new ArrayList<>();
    coapServer = new CoapServer(NetworkConfig.createStandardWithoutFile(), coAPServerConfigs.port);
    coapReceiverResource = new CoapReceiverResource(context, receiver, errorQueue);
    coapServer.add(coapReceiverResource);
    coapServer.start();
    return issues;
  }

  public void destroy() {
    LOG.debug("Shutting down, port '{}'", coAPServerConfigs.port);
    if (coapServer != null) {
      coapReceiverResource.setShuttingDown();
      coapServer.stop();
      coapServer = null;
    }
  }
}
