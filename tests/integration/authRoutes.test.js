jest.mock("../../src/services/authService", () => ({
  register: jest.fn(),
  login: jest.fn(),
  getUserByEmail: jest.fn(),
  getAllUsers: jest.fn(),
  updateUser: jest.fn(),
  deleteUser: jest.fn(),
  changePassword: jest.fn(),
  resetPassword: jest.fn()
}));

const request = require("supertest");
const app = require("../../src/app");
const authService = require("../../src/services/authService");

beforeEach(() => {
  jest.clearAllMocks();
});

describe("/api/auth routes", () => {
  test("POST /api/auth/register returns token", async () => {
    authService.register.mockResolvedValue("jwt-token");

    const response = await request(app)
      .post("/api/auth/register")
      .send({ email: "test@evion.dev", password: "secret" });

    expect(response.status).toBe(200);
    expect(response.body).toBe("jwt-token");
    expect(authService.register).toHaveBeenCalledWith({
      email: "test@evion.dev",
      password: "secret"
    });
  });

  test("POST /api/auth/login returns 500 when service throws", async () => {
    authService.login.mockRejectedValue(new Error("boom"));

    const response = await request(app)
      .post("/api/auth/login")
      .send({ email: "test@evion.dev", password: "wrong" });

    expect(response.status).toBe(500);
    expect(response.body).toEqual({ message: "boom" });
  });

  test("GET /api/auth/users returns list", async () => {
    authService.getAllUsers.mockResolvedValue([{ id: 1, email: "a@b.com" }]);

    const response = await request(app).get("/api/auth/users");

    expect(response.status).toBe(200);
    expect(response.body).toEqual([{ id: 1, email: "a@b.com" }]);
  });
});
