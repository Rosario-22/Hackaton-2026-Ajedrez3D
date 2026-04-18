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
