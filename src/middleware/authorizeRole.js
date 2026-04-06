function authorizeRole(requiredRole) {
  return (req, res, next) => {
    const roles = req.user?.roles || [];
    if (roles.includes(requiredRole)) {
      return next();
    }
    return res.status(403).json({ message: "Forbidden" });
  };
}

module.exports = authorizeRole;
