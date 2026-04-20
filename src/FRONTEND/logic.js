export const isLegalMove = (type, start, end) => {
  if (!start || !end) return false;

  const dx = Math.abs(start.x - end.x);
  const dy = Math.abs(start.y - end.y);
  const dz = Math.abs(start.z - end.z);
  const dist = [dx, dy, dz].filter((v) => v > 0);

  switch (type.toLowerCase()) {
    case "torre":
      return dist.length === 1;
    case "alfil":
      return dist.length === 2 && (dx === dy || dx === dz || dy === dz);
    case "inicornio":
      return dx === dy && dy === dz && dx > 0;
    case "caballo":
      const s = [dx, dy, dz].sort((a, b) => b - a);
      return s[0] === 2 && s[1] === 1 && s[2] === 0;
    case "rey":
      return dx <= 1 && dy <= 1 && dz <= 1 && dist.length > 0;
    default:
      return false;
  }
};
