import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const WS_URL = "https://urethane-onstage-ruckus.ngrok-free.dev/xs/chess";
const GAME_ID = "1";

export const createSocket = (onMessage) => {
  const client = new Client({
    webSocketFactory: () => new SockJS(WS_URL),
    onConnect: () => {
      console.log("Conectado al Java Backend");
      client.subscribe(`/topic/game/${GAME_ID}`, (message) => {
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