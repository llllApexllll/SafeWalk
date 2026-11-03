const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read community reports", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.collection("community_reports").get());
});

test("Authenticated user can read community reports", async () => {
  const authedDb = testEnv.authenticatedContext("user_123").firestore();
  await assertSucceeds(authedDb.collection("community_reports").get());
});

test("User can create valid community report", async () => {
  const authedDb = testEnv.authenticatedContext("user_123").firestore();
  const reportRef = authedDb.collection("community_reports").doc("rep_1");
  await assertSucceeds(
    reportRef.set({
      userId: "user_123",
      title: "Broken street lights",
      category: "BROKEN_LIGHTING",
      severity: "HIGH",
      latitude: 37.7760,
      longitude: -122.4180,
      addressName: "Elm St between 4th & 6th",
      isVerified: false,
      verificationStatus: "PENDING_REVIEW",
      upvotes: 1,
      isAnonymous: true,
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

test("User cannot read another user's emergency contacts", async () => {
  const user1Db = testEnv.authenticatedContext("user_1").firestore();
  const user2Db = testEnv.authenticatedContext("user_2").firestore();

  // User 1 creates contact
  await assertSucceeds(
    user1Db.collection("users").doc("user_1").collection("emergency_contacts").doc("contact_1").set({
      userId: "user_1",
      name: "Alice",
      phoneNumber: "+15551234567",
      relationship: "Sister",
      isPrimary: true,
      notifyOnSos: true,
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );

  // User 2 attempts to read User 1's contact - MUST FAIL
  await assertFails(
    user2Db.collection("users").doc("user_1").collection("emergency_contacts").doc("contact_1").get()
  );
});
