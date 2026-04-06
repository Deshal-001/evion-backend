const jwt = require("jsonwebtoken");
const env = require("../config/env");

function generateToken(email, roles) {
  return jwt.sign(
    {
      roles
    },
    env.jwt.secret,
    {
      subject: email,
      expiresIn: Math.floor(env.jwt.expirationMs / 1000)
    }
  );
}

module.exports = {
  generateToken
};
