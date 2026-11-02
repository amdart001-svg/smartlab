const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_admin";
const BOB_UID = "bob_user";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

test("Unauthenticated user: cannot read groups or students", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("groups").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("students").get());
});

test("Authenticated user: cannot read another user's SMARTLAB data", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("groups").doc("grp1").set({
      userId: ALICE_UID,
      name: "Group 1",
      courseName: "Clinical Chemistry",
      totalPrice: 250000,
      status: "registering",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("groups").doc("grp1").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("groups").get());
});

test("Owner: can create and read training groups", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const groupRef = aliceDb.collection("users").doc(ALICE_UID).collection("groups").doc("grp1");

  await assertSucceeds(
    groupRef.set({
      userId: ALICE_UID,
      name: "Group A",
      courseName: "Medical Lab Diagnostics",
      totalPrice: 200000,
      status: "registering",
      maxSeats: 15,
      createdAt: new Date(),
    })
  );

  const snapshot = await assertSucceeds(groupRef.get());
  assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("groups").get());
});

test("Owner: can record student and installment payments", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const studentRef = aliceDb.collection("users").doc(ALICE_UID).collection("students").doc("std1");
  const paymentRef = aliceDb.collection("users").doc(ALICE_UID).collection("payments").doc("pay1");

  await assertSucceeds(
    studentRef.set({
      userId: ALICE_UID,
      fullName: "Ali Ahmed",
      phone: "07701234567",
      groupId: "grp1",
      totalPrice: 200000,
      paidAmount: 100000,
      remainingAmount: 100000,
      adminStatus: "confirmed",
      financialStatus: "partially_paid",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    paymentRef.set({
      userId: ALICE_UID,
      studentId: "std1",
      groupId: "grp1",
      amount: 100000,
      paymentMethod: "Cash",
      createdAt: new Date(),
    })
  );
});

test("Audit logs: are append-only and cannot be updated or deleted", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const logRef = aliceDb.collection("users").doc(ALICE_UID).collection("audit_logs").doc("log1");

  await assertSucceeds(
    logRef.set({
      userId: ALICE_UID,
      action: "CREATE_STUDENT",
      entityType: "student",
      entityId: "std1",
      details: "Created Ali Ahmed",
      createdAt: new Date(),
    })
  );

  // Update must fail
  await assertFails(
    logRef.update({
      details: "Modified details",
    })
  );

  // Delete must fail
  await assertFails(logRef.delete());
});
