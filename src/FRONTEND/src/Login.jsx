import { useState } from "react";

const API = import.meta.env.VITE_API_URL;

export default function Login({ onLogin }) {
  const [modo, setModo] = useState("login");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);

  const handleSubmit = async () => {
    setError("");
    setCargando(true);

    const url = modo === "login"
      ? `${API}/api/auth/login`
      : `${API}/api/auth/register`;

    const body = modo === "login"
      ? { username, password }
      : { username, email, password };

    try {
      const res = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });

      const data = await res.json();

      if (!res.ok) {
        setError(data.detail || "Ocurrió un error");
        return;
      }

      onLogin(data.token, data.userId, data.username);
    } catch (e) {
      setError("No se pudo conectar con el servidor");
    } finally {
      setCargando(false);
    }
  };

  return (
    <div style={{
      width: "100vw", height: "100vh",
      background: "#0a0a0a", display: "flex",
      alignItems: "center", justifyContent: "center",
      fontFamily: "sans-serif", color: "white"
    }}>
      <div style={{
        background: "#1a1a1a", padding: "40px",
        borderRadius: "12px", width: "320px",
        border: "1px solid #333"
      }}>
        <h1 style={{ textAlign: "center", marginBottom: "8px", fontSize: "24px" }}>
          ♟ Ajedrez Al Cubo
        </h1>
        <p style={{ textAlign: "center", color: "#888", marginBottom: "32px", fontSize: "14px" }}>
          {modo === "login" ? "Iniciá sesión para jugar" : "Creá tu cuenta"}
        </p>

        <input
          placeholder="Usuario"
          value={username}
          onChange={e => setUsername(e.target.value)}
          style={inputStyle}
        />

        {modo === "registro" && (
          <input
            placeholder="Email"
            value={email}
            onChange={e => setEmail(e.target.value)}
            style={inputStyle}
          />
        )}

        <input
          placeholder="Contraseña"
          type="password"
          value={password}
          onChange={e => setPassword(e.target.value)}
          style={inputStyle}
        />

        {error && (
          <p style={{ color: "#ff4444", fontSize: "13px", marginBottom: "12px" }}>
            {error}
          </p>
        )}

        <button
          onClick={handleSubmit}
          disabled={cargando}
          style={buttonStyle}
        >
          {cargando ? "Cargando..." : modo === "login" ? "Entrar" : "Registrarse"}
        </button>

        <p
          onClick={() => { setModo(modo === "login" ? "registro" : "login"); setError(""); }}
          style={{ textAlign: "center", color: "#888", fontSize: "13px", cursor: "pointer", marginTop: "16px" }}
        >
          {modo === "login" ? "¿No tenés cuenta? Registrate" : "¿Ya tenés cuenta? Iniciá sesión"}
        </p>
      </div>
    </div>
  );
}

const inputStyle = {
  width: "100%", padding: "10px 12px",
  marginBottom: "12px", background: "#2a2a2a",
  border: "1px solid #444", borderRadius: "8px",
  color: "white", fontSize: "14px", boxSizing: "border-box"
};

const buttonStyle = {
  width: "100%", padding: "12px",
  background: "#00ff88", color: "#0a0a0a",
  border: "none", borderRadius: "8px",
  fontSize: "15px", fontWeight: "bold",
  cursor: "pointer", marginTop: "4px"
};