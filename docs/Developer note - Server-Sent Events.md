
## Server-Sent Events (SSE)

Server-Sent Events (SSE) is a web technology that allows servers to push real-time updates to clients over a single HTTP connection. Unlike traditional client-server communication, where the client must repeatedly request updates, SSE enables the server to send updates automatically whenever new data is available. This makes it ideal for applications like live sports scores, stock market updates, or social media notifications.

### Key Features of SSE

  -  Unidirectional connection: Data flows only from the server to the client.
  -  EventSource interface: Used on the client side to establish and manage the connection.
  -  Text/event-stream format: The server sends updates in a specific format that the client listens to and processes.

SSE operates over a unidirectional connection, meaning data flows only from the server to the client. It uses the EventSource interface on the client side to establish and manage the connection. The server sends updates in a specific format (`text/event-stream`), which the client listens to and processes.

### Client-Side Implementation

To use SSE on the client side, you create an EventSource object and specify the URL of the server endpoint that streams events. Here's an example:

```javascript
// Establish a connection to the server
const eventSource = new EventSource("https://example.com/stream");

// Listen for default message events
eventSource.onmessage = (event) => {
  console.log("Message received:", event.data);
};

// Listen for custom events
eventSource.addEventListener("customEvent", (event) => {
  console.log("Custom event received:", event.data);
});

// Handle errors
eventSource.onerror = (error) => {
  console.error("Error occurred:", error);
};

// Close the connection when needed
eventSource.close();
```

### Server-Side Implementation

On the server side, the response must have the `Content-Type` header set to `text/event-stream`. Each event is sent as a block of text, terminated by two newline characters. Here's an example in PHP:

```php
header("Content-Type: text/event-stream");
header("Cache-Control: no-cache");

while (true) {
    // Send a "ping" event with the current time
    echo "event: ping\n";
    echo "data: {\"time\": \"" . date(DATE_ISO8601) . "\"}\n\n";

    // Flush the output buffer
    ob_flush();
    flush();

    // Break the loop if the client disconnects
    if (connection_aborted()) break;

    sleep(1); // Wait for 1 second before sending the next event
}
```

### Advantages of SSE

SSE is lightweight and simple to implement compared to alternatives like WebSockets. It works seamlessly over HTTP/1.1 and HTTP/2 and supports automatic reconnection, event IDs for resuming streams, and custom event types.

### Limitations

SSE is unidirectional, meaning it cannot send data from the client to the server. It also has a limitation on the number of open connections per browser per domain, which can be mitigated by using HTTP/2.

### Use Cases

SSE is well-suited for applications requiring real-time updates, such as live dashboards, notifications, or collaborative tools. However, for bidirectional communication, WebSockets may be a better choice.

---

By leveraging SSE, developers can build efficient and responsive applications that deliver real-time updates with minimal overhead.
