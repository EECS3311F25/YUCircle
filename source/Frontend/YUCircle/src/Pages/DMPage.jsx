import DMChat from "../Components/DMChat";
import { connect, sendDM } from "../chatService.js";
import { useState, useEffect } from "react";

export default function DMPage() {
  const currentUser = localStorage.getItem("username");

  const [receiverCallback, setReceiverCallback] = useState(null);

  useEffect(() => {
    connect(currentUser, (msg) => {
      if (receiverCallback) receiverCallback(msg);
    });
  }, []);     // connect ONCE when component loads

  return (
    <DMChat
  onSendMessage={(msg) => 
    sendDM(msg.sender, msg.receiver, msg.content)
}

  onReceiveMessage={setReceiverCallback}
/>

  );
}
