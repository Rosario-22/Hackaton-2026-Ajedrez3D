# Hackaton-2026-Ajedrez3D

API REST en Spring Boot para un ajedrez 3D sobre un cubo de `5x5x5`.

## Que expone

- `POST /api/games` crea una partida
- `GET /api/games/{id}` devuelve el estado completo
- `GET /api/games/{id}/legal-moves?x=&y=&z=` devuelve movimientos legales de una pieza
- `POST /api/games/{id}/moves` ejecuta una jugada
- `GET /api/health` verifica que la API esta arriba
- `GET /api/rules` devuelve reglas, piezas y posiciones iniciales

## Piezas jugables

- `KING`
- `ROOK`
- `BISHOP`
- `UNICORN`
- `KNIGHT`

## Regla base

- El tablero es `5x5x5`.
- Cada jugador tiene `5` piezas.
- No hay peones.
- La partida termina por captura del rey, jaque mate o empate por ahogado.
- El backend expone `check`, `checkmate` y `stalemate` en las respuestas.

## Posicion inicial

- `WHITE` arranca en la esquina `(0,0,0)`.
- `BLACK` arranca en la esquina opuesta `(4,4,4)`.

## Nota tecnica

- El backend arranca con estado en memoria.
- El front puede consumir la API sin conocer la logica interna.
- Las coordenadas del tablero son de `0` a `4` en cada eje.

# Frontend
# 3D Chess Frontend

Frontend de una aplicación de ajedrez en 3D desarrollada durante un hackathon.  
La app renderiza el tablero en 3D y se comunica con un backend mediante API REST y WebSocket.

## 🚀 Tecnologías

- React
- @react-three/fiber (Three.js)
- @react-three/drei
- WebSocket (STOMP / SockJS)
- CSS

## 🎯 Funcionalidades

- Renderizado de un tablero 3D (5x5x5)
- Visualización de piezas con distintos tipos:
  - Rey
  - Torre
  - Alfil
  - Caballo
  - Unicornio
- Selección de piezas
- Highlight de movimientos legales
- Movimiento de piezas mediante interacción
- Actualización en tiempo real vía WebSocket

## 🔌 Integración con backend

Endpoints utilizados:

- `POST /api/games` — crear partida
- `GET /api/games/{id}/legal-moves` — obtener movimientos legales
- `POST /api/games/{id}/moves` — realizar movimiento

### WebSocket

- Suscripción a actualizaciones del tablero
- Tipo de mensaje: `UPDATE_BOARD`

## 🧠 Características

- Visualización 3D con celdas semitransparentes
- Diseño minimalista enfocado en claridad
- Interacción directa en la escena 3D
- Actualización optimista del estado al mover piezas

## 🛠 Ejecución del proyecto

```bash
npm install
npm run dev
```
La aplicación corre por defecto en:

http://localhost:5173

Configuración del API

En App.jsx:

const API = "https://urethane-onstage-ruckus.ngrok-free.dev"
