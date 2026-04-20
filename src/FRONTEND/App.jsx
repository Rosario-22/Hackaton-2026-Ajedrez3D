import React, { useState, useEffect, useRef } from "react";
import { Canvas } from "@react-three/fiber";
import { OrbitControls, Text } from "@react-three/drei";
import { createSocket } from "./socket";
import "./App.css";

const API = "https://urethane-onstage-ruckus.ngrok-free.dev";
const HEADERS = {
  "ngrok-skip-browser-warning": "true",
  "Content-Type": "application/json",
};

export default function App() {
  const [pieces, setPieces] = useState([]);
  const [gameId, setGameId] = useState(null);
  const [selected, setSelected] = useState(null);
  const [legalMoves, setLegalMoves] = useState([]);
  const socketRef = useRef(null);

  useEffect(() => {
    fetch(`${API}/api/games`, { method: "POST", headers: HEADERS })
      .then((res) => res.json())
      .then((data) => {
        setGameId(data.gameId);
        if (data.pieces) setPieces(data.pieces);
      });
  }, []);

  useEffect(() => {
    socketRef.current = createSocket((message) => {
      if (message.type === "UPDATE_BOARD") {
        setPieces(message.payload);
      }
    });
    return () => socketRef.current?.close();
  }, []);

  const handleSelectPiece = (position) => {
    if (!gameId) return;

    setSelected(position);

    fetch(
      `${API}/api/games/${gameId}/legal-moves?x=${position.x}&y=${position.y}&z=${position.z}`,
      { headers: HEADERS },
    )
      .then((res) => res.json())
      .then((data) => {
        setLegalMoves(data.moves || []);
      });
  };

  const clean = (p) => ({
    x: p.x,
    y: p.y,
    z: p.z,
  });

  const handleMove = (to) => {
    if (!selected || !gameId) return;

    const from = clean(selected);

    const payload = {
      from,
      to: clean(to),
    };

    fetch(`${API}/api/games/${gameId}/moves`, {
      method: "POST",
      headers: HEADERS,
      body: JSON.stringify(payload),
    }).then(async (res) => {
      try {
        const data = await res.json();

        if (data.pieces) {
          setPieces(data.pieces);
          return;
        }
      } catch (e) {}

      setPieces((prev) =>
        prev.map((p) =>
          p.position.x === from.x &&
          p.position.y === from.y &&
          p.position.z === from.z
            ? { ...p, position: to }
            : p,
        ),
      );
    });

    setSelected(null);
    setLegalMoves([]);
  };

  return (
    <div style={{ width: "100vw", height: "100vh", background: "#0a0a0a" }}>
      <Canvas camera={{ position: [8, 8, 8], fov: 60 }}>
        <OrbitControls makeDefault />
        <ambientLight intensity={0.7} />
        <pointLight position={[10, 10, 10]} intensity={1.5} />

        <BoardGrid legalMoves={legalMoves} onMove={handleMove} />

        {pieces.map((p) => (
          <Piece
            key={p.id}
            data={p}
            isSelected={
              selected &&
              selected.x === p.position.x &&
              selected.y === p.position.y &&
              selected.z === p.position.z
            }
            onSelect={() => handleSelectPiece(p.position)}
          />
        ))}
      </Canvas>
    </div>
  );
}

function BoardGrid({ legalMoves, onMove }) {
  const cells = [];

  for (let x = 0; x < 5; x++) {
    for (let y = 0; y < 5; y++) {
      for (let z = 0; z < 5; z++) {
        const isLegal = legalMoves.some(
          (m) => m.x === x && m.y === y && m.z === z,
        );

        const checker = (x + y + z) % 2 === 0;
        const color = isLegal ? "#00ff88" : checker ? "#ffffff" : "#000000";
        const opacity = isLegal ? 0.45 : checker ? 0.18 : 0.28;

        cells.push(
          <mesh
            key={`${x}-${y}-${z}`}
            position={[x, y, z]}
            onClick={() => isLegal && onMove({ x, y, z })}
            renderOrder={-1}
          >
            <boxGeometry args={[0.9, 0.9, 0.9]} />
            <meshStandardMaterial
              color={color}
              transparent
              opacity={opacity}
              depthWrite={false}
            />
          </mesh>,
        );
      }
    }
  }

  return <group>{cells}</group>;
}

function Piece({ data, isSelected, onSelect }) {
  const color = isSelected
    ? "#ff0066"
    : data.color === "white"
      ? "#ffffff"
      : "#4488ff";

  const label = {
    rey: "Rey",
    torre: "Torre",
    unicornio: "Unicornio",
    caballo: "Caballo",
    alfil: "Alfil",
  }[data.type];

  return (
    <group position={[data.position.x, data.position.y, data.position.z]}>
      <mesh
        onClick={(e) => {
          e.stopPropagation();
          onSelect();
        }}
      >
        {data.type === "torre" && (
          <cylinderGeometry args={[0.2, 0.2, 0.6, 16]} />
        )}
        {data.type === "alfil" && <coneGeometry args={[0.3, 0.7, 4]} />}
        {data.type === "unicornio" && <octahedronGeometry args={[0.4]} />}
        {data.type === "caballo" && (
          <torusKnotGeometry args={[0.2, 0.05, 64, 8]} />
        )}
        {data.type === "rey" && <sphereGeometry args={[0.4]} />}

        <meshStandardMaterial
          color={color}
          emissive={isSelected ? "#ff0066" : "black"}
          emissiveIntensity={0.5}
        />
      </mesh>

      <Text
        position={[0, 0.8, 0]}
        fontSize={0.3}
        color={color}
        anchorX="center"
        anchorY="middle"
      >
        {label}
      </Text>
    </group>
  );
}
