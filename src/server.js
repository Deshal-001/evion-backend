const app = require("./app");
const env = require("./config/env");
const { connectDatabase } = require("./models");

async function bootstrap() {
  await connectDatabase();
  app.listen(env.port, () => {
    // eslint-disable-next-line no-console
    console.log(`EVion Express API running on port ${env.port}`);
  });
}

bootstrap().catch((error) => {
  // eslint-disable-next-line no-console
  console.error("Failed to start server", error);
  process.exit(1);
});
