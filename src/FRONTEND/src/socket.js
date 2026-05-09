import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const WS_URL = import.meta.env.VITE_WS_URL;

export const createSocket = (gameId, onMessage) => {
  const client = new Client({
    webSocketFactory: () => new SockJS(WS_URL),
    onConnect: () => {
      console.log("Conectado al backend");
      client.subscribe(`/topic/game/${gameId}`, (message) => {
        try {
          const data = JSON.parse(message.body);
          onMessage({ type: "UPDATE_BOARD", payload: data });
        } catch (error) {
          console.log("Error al analizar mensaje:", error);
        }
      });
    },
    onStompError: (error) => {
      console.error("STOMP Error:", error);
    },
  });

  client.activate();

  return {
    close: () => client.deactivate(),
  };
};