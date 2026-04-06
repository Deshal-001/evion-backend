const request = require("supertest");
const app = require("../../src/app");

describe("health endpoint", () => {
  test("GET /health responds with status ok", async () => {
    const response = await request(app).get("/health");
    expect(response.status).toBe(200);
    expect(response.body).toEqual({ status: "ok" });
  });
});
