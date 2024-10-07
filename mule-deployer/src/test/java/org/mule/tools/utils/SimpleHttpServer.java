/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SimpleHttpServer {

  private static final int PORT = 8443;

  private final ExecutorService executorService;
  private final int port;
  private final int soTimeout;

  public SimpleHttpServer() {
    this(PORT, 10, 5000);
  }

  public SimpleHttpServer(int port, int threadPoolSize, int soTimeout) {
    this.port = port;
    this.soTimeout = soTimeout;
    executorService = Executors.newFixedThreadPool(Math.max(threadPoolSize, 2), new ServerThreadFactory());
  }

  public int getPort() {
    return port;
  }

  public abstract void response(String requestLine, OutputStream output);

  private int getSoTimeout() {
    return soTimeout;
  }

  protected void sendChunk(OutputStream output, String data) throws IOException {
    if (data.isEmpty()) {
      // This represents the final chunk (0-length)
      output.write("0\r\n\r\n".getBytes());
    } else {
      String chunkSize = Integer.toHexString(data.length()) + ";AAAAAAAA=a\r\n";
      output.write(chunkSize.getBytes());
      output.write(data.getBytes());
      output.write("\r\n".getBytes());
    }
  }

  protected void writeJsonResponse(OutputStream output, String data) {
    try {
      String httpResponse = String.format("HTTP/1.1 200 OK\r\n" +
          "Content-Type: application/json\r\n" +
          "Content-Length: %s\r\n" +
          "\r\n" +
          "%s", data.getBytes().length, data);
      output.write(httpResponse.getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeNotFoundResponse(OutputStream output) {
    try {
      String httpResponse = "HTTP/1.1 404 NOT FOUND\r\n\r\n";
      output.write(httpResponse.getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeJsonResponse(OutputStream output, String data, int chunkSize) {
    try {
      String headers = "HTTP/1.1 200 OK\r\n" +
          "Transfer-Encoding: chunked\r\n" +
          "Content-Type: application/json\r\n\r\n";
      output.write(headers.getBytes());
      byte[][] chunks = toChunkArray(data.getBytes(), chunkSize);
      for (byte[] chunk : chunks) {
        sendChunk(output, new String(chunk));
      }
      sendChunk(output, "");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  public void startServer() {
    try {
      try (ServerSocket serverSocket = new ServerSocket(getPort())) {
        while (true) {
          Socket socket = serverSocket.accept();
          // Handle the connection in a new thread
          executorService.execute(new ClientHandler(socket, this));
        }
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public void stopServer() {
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
    }
  }

  private static class ClientHandler implements Runnable {

    private final Socket socket;
    private final SimpleHttpServer server;

    public ClientHandler(Socket socket, SimpleHttpServer server) {
      this.socket = socket;
      this.server = server;
    }

    @Override
    public void run() {
      try {
        socket.setSoTimeout(server.getSoTimeout());
        try (InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

          String requestLine = reader.readLine();
          System.out.println("Received request: " + requestLine);
          server.response(requestLine, output);
          sleep(100);
          output.flush();
        }
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      } finally {
        if (socket != null && !socket.isClosed()) {
          try {
            socket.close();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
  }

  private static class ServerThreadFactory implements ThreadFactory {

    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final String namePrefix;

    ServerThreadFactory() {
      group = Optional.ofNullable(System.getSecurityManager()).map(SecurityManager::getThreadGroup)
          .orElseGet(() -> Thread.currentThread().getThreadGroup());
      namePrefix = "https-server-pool-" + poolNumber.getAndIncrement() + "-thread-";
    }

    public Thread newThread(Runnable runnable) {
      Thread thread = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement(), 0);

      if (thread.isDaemon()) {
        thread.setDaemon(false);
      }

      if (thread.getPriority() != Thread.NORM_PRIORITY) {
        thread.setPriority(Thread.NORM_PRIORITY);
      }

      return thread;
    }
  }

  private static void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  protected byte[][] toChunkArray(byte[] bytes, int chunkSize) {
    byte[][] chunks = new byte[(int) Math.ceil((double) bytes.length / chunkSize)][];

    for (int i = 0; i < chunks.length; i++) {
      int start = i * chunkSize;
      int length = Math.min(bytes.length - start, chunkSize);

      chunks[i] = new byte[length];
      System.arraycopy(bytes, start, chunks[i], 0, length);
    }

    return chunks;
  }
}

