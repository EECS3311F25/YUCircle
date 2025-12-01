import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

let stompClient = null;
let connected = false;

/**
 * Connect the current user to the WebSocket
 */
export function connect(username, onMessageReceived) {
  if (connected) {
    console.log("Already connected.");
    return;
  }

  console.log("Connecting WebSocket for:", username);

  // Create SockJS wrapper
  const socket = new SockJS("http://localhost:8080/ws");

  // Create STOMP client
  stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000, // auto-reconnect
    debug: (msg) => console.log(msg),

    onConnect: () => {
      connected = true;
      console.log("STOMP CONNECTED as:", username);

      // Subscribe to personal DM inbox
      stompClient.subscribe(`/user/${username}/queue/messages`, (frame) => {
        const msg = JSON.parse(frame.body);
        console.log("📥 INCOMING DM:", msg);
        onMessageReceived(msg);
      });
    },

    onStompError: (frame) => {
      console.error("STOMP ERROR:", frame);
    },

    onWebSocketClose: () => {
      console.warn("WebSocket connection closed.");
      connected = false;
    },
  });

  stompClient.activate();
}

/**
 * Send a direct message to another user
 */
export function sendDM(sender, receiver, content) {
  if (!stompClient || !connected) {
    console.error("❌ Cannot send DM — STOMP client not connected.");
    return;
  }

  const message = { sender, receiver, content };

  console.log("📤 SENDING DM:", message);

  stompClient.publish({
    destination: "/app/dm",
    body: JSON.stringify(message),
  });
}
