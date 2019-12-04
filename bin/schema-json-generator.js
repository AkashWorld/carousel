const { buildSchema } = require("graphql");
const fs = require("fs");

const sdl = fs.readFileSync("../src/main/resources/graphql/schema.sdl").toString();

const graphqlSchemaObject = buildSchema(sdl);

const graphqlJSON = JSON.stringify(graphqlSchemaObject);

fs.writeFileSync("../src/main/kotlin/client/queries/schema.json", graphqlJSON);
