const jwt = require("jsonwebtoken");
const env = require("../config/env");

function authenticate(req, res, next) {
  const header = req.headers.authorization;
  if (!header || !header.startsWith("Bearer ")) {
    return res.status(401).json({ message: "Unauthorized" });
  }

  const token = header.substring(7);
  try {
    const payload = jwt.verify(token, env.jwt.secret);
    req.user = {
      email: payload.sub,
      roles: Array.isArray(payload.roles) ? payload.roles : []
    };
    return next();
  } catch (error) {
    return res.status(401).json({ message: "Invalid token" });
  }
}

module.exports = authenticate;
