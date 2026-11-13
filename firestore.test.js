const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_sanjuan";
const BOB_UID = "bob_laiya";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTimestamp() {
  return new Date(Date.now() - 2000);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
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

test("Unauthenticated user cannot read or write user profiles or itineraries", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("itineraries").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("savedSpots").get()
  );
});

test("Authenticated owner can create and update their own TravelerProfile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);
  const ts = pastTimestamp();

  await assertSucceeds(
    profileRef.set({
      userId: ALICE_UID,
      fullName: "Maria Santos",
      email: "maria@example.com",
      homeCity: "Lipa City, Batangas",
      travelerStyle: "Beach & Marine",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    profileRef.update({
      travelerStyle: "Eco-Hiker",
      updatedAt: pastTimestamp(),
    })
  );
});

test("Shadow update with ghost field on TravelerProfile is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastTimestamp();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Maria Santos",
      email: "maria@example.com",
      homeCity: "Lipa City",
      travelerStyle: "Beach & Marine",
      isAdmin: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("Cross-user access to another traveler's profile, savedSpots, and itineraries is denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastTimestamp();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Maria Santos",
      email: "maria@example.com",
      homeCity: "Lipa City",
      travelerStyle: "Heritage & Crafts",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("itineraries").doc("stop_1").set({
      userId: ALICE_UID,
      title: "Laiya Sunrise Swim",
      location: "Brgy. Laiya Aplaya, San Juan",
      dayNumber: 1,
      timeSlot: "Morning",
      notes: "Arrive before 7 AM for calm waters.",
      visited: false,
      source: "manual",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("itineraries").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("savedSpots").doc("laiya_beach").set({
      userId: BOB_UID,
      spotId: "laiya_beach",
      spotName: "Laiya White Beach",
      category: "Beaches",
      barangay: "Laiya Aplaya",
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("Owner can bookmark San Juan spots and toggle itinerary visited status", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastTimestamp();

  const spotRef = aliceDb.collection("users").doc(ALICE_UID).collection("savedSpots").doc("mt_daguldol");
  await assertSucceeds(
    spotRef.set({
      userId: ALICE_UID,
      spotId: "mt_daguldol",
      spotName: "Mount Daguldol Eco-Trail",
      category: "Mountains & Eco",
      barangay: "Brgy. Hugom",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  const stopRef = aliceDb.collection("users").doc(ALICE_UID).collection("itineraries").doc("daguldol_hike");
  await assertSucceeds(
    stopRef.set({
      userId: ALICE_UID,
      title: "Mt. Daguldol Trail Hike",
      location: "Brgy. Hugom, San Juan",
      dayNumber: 1,
      timeSlot: "Morning",
      notes: "Register at the barangay jump-off.",
      visited: false,
      source: "ai_planner",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    stopRef.update({
      visited: true,
      updatedAt: pastTimestamp(),
    })
  );
});
