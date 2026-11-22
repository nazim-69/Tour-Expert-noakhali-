const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const TOURIST_UID = "tourist_alice_01";
const OTHER_TOURIST_UID = "tourist_bob_02";
const ADMIN_UID = "super_admin_uid";
const BOOTSTRAP_EMAIL = "nazimmd74653@gmail.com";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
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

function sampleEventPayload(now = new Date(Date.now() - 2000)) {
  return {
    name: "সুন্দরবন ভ্রমণ — অক্টোবর স্পেশাল",
    destination: "সুন্দরবন",
    tourDate: "০৮ অক্টোবর",
    monthTag: "THIS_MONTH",
    category: "FOREST",
    duration: "২ দিন ১ রাত",
    price: 2199,
    advancePayment: 1000,
    shortDescription: "নোয়াখালী থেকে সরাসরি সুন্দরবন ইকো ট্যুর।",
    description: "সম্পূর্ণ প্যাকেজ: যাতায়াত, গাইড, সকালের নাস্তা ও দুপুরের খাবার।",
    imageKey: "sundarbans",
    galleryKeys: ["sundarbans", "hero_bangladesh"],
    totalSeats: 40,
    bookedSeats: ["A1"],
    holdSeats: [],
    vehicleLayout: "COACH_40",
    bookingStatus: "OPEN",
    featured: true,
    createdAt: now,
    updatedAt: now,
  };
}

function sampleBookingPayload(userId, now = new Date(Date.now() - 2000)) {
  return {
    userId,
    referenceNumber: "TEN-2026-0001",
    eventId: "event_sundarbans_oct08",
    eventName: "সুন্দরবন ভ্রমণ — অক্টোবর স্পেশাল",
    destination: "সুন্দরবন",
    tourDate: "০৮ অক্টোবর",
    touristName: "মোঃ করিম উদ্দিন",
    touristEmail: "karim.noakhali@gmail.com",
    mobileNumber: "01811223344",
    whatsappNumber: "01811223344",
    numberOfTourists: 2,
    nidOrPassport: "1995123456789",
    emergencyContact: "01711223344",
    address: "মাইজদী, নোয়াখালী",
    boardingPoint: "হাউজিং দক্ষিণ মোড়, নোয়াখালী",
    selectedSeats: ["B1", "B2"],
    totalAmount: 4398,
    paidAmount: 2000,
    paymentMethod: "বিকাশ",
    transactionId: "TRX987654321",
    paymentDate: "2026-10-01",
    specialInstructions: "জানালার পাশের সিট",
    status: "Payment Verification Pending",
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("events").get());
  await assertFails(unauthDb.collection("destinations").get());
  await assertFails(unauthDb.collection("bookings").get());
  await assertFails(unauthDb.collection("admin_accounts").get());
});

test("Bootstrap Super Admin (verified email) can create events & settings; tourists cannot create events", async () => {
  const adminDb = testEnv
    .authenticatedContext(ADMIN_UID, {
      email: BOOTSTRAP_EMAIL,
      email_verified: true,
    })
    .firestore();

  const touristDb = testEnv.authenticatedContext(TOURIST_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    adminDb.collection("events").doc("event_sundarbans_oct08").set(sampleEventPayload(now))
  );
  await assertFails(
    touristDb.collection("events").doc("event_fake").set(sampleEventPayload(now))
  );

  await assertSucceeds(
    adminDb.collection("settings").doc("agency").set({
      organizationName: "ট্যুর এক্সপার্ট নোয়াখালী",
      tagline: "ভ্রমণ যেখানে গল্প সেখানে",
      address: "হাউজিং দক্ষিণ মোড়, নোয়াখালী",
      contactPhone: "01822421919",
      bookingPhone: "01822421919",
      advancePaymentNumber: "01849958944",
      websiteUrl: "https://ais-pre-cvz5ce7s6eo4edsszl5gqh-42022883973.asia-east1.run.app",
      bookingCounter: 0,
      updatedAt: now,
    })
  );
  await assertFails(
    touristDb.collection("settings").doc("agency").update({
      websiteUrl: "https://malicious.example.com",
      updatedAt: now,
    })
  );
});

test("Email spoofing test: unverified admin email is rejected", async () => {
  const spoofedDb = testEnv
    .authenticatedContext("spoof_uid", {
      email: BOOTSTRAP_EMAIL,
      email_verified: false,
    })
    .firestore();

  await assertFails(
    spoofedDb
      .collection("events")
      .doc("event_spoof")
      .set(sampleEventPayload())
  );
});

test("Tourist can read events, hold available seats, and create own booking + lookup", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("events").doc("event_sundarbans_oct08").set(sampleEventPayload());
    await db.collection("settings").doc("agency").set({
      organizationName: "ট্যুর এক্সপার্ট নোয়াখালী",
      tagline: "ভ্রমণ যেখানে গল্প সেখানে",
      address: "হাউজিং দক্ষিণ মোড়, নোয়াখালী",
      contactPhone: "01822421919",
      bookingPhone: "01822421919",
      advancePaymentNumber: "01849958944",
      bookingCounter: 0,
      updatedAt: new Date(Date.now() - 5000),
    });
  });

  const touristDb = testEnv.authenticatedContext(TOURIST_UID).firestore();
  const now = new Date(Date.now() - 1000);

  // Read catalog
  await assertSucceeds(touristDb.collection("events").get());

  // Hold seats (cannot tamper price or remove existing holds)
  await assertSucceeds(
    touristDb
      .collection("events")
      .doc("event_sundarbans_oct08")
      .update({
        holdSeats: ["B1", "B2"],
        updatedAt: now,
      })
  );
  await assertFails(
    touristDb
      .collection("events")
      .doc("event_sundarbans_oct08")
      .update({
        price: 1,
        holdSeats: ["B1", "B2"],
        updatedAt: now,
      })
  );

  // Increment booking counter by +1
  await assertSucceeds(
    touristDb.collection("settings").doc("agency").update({
      bookingCounter: 1,
      updatedAt: now,
    })
  );
  // Cannot jump counter by +5
  await assertFails(
    touristDb.collection("settings").doc("agency").update({
      bookingCounter: 6,
      updatedAt: now,
    })
  );

  // Create booking (must be Payment Verification Pending, never self-Confirmed)
  await assertSucceeds(
    touristDb
      .collection("bookings")
      .doc("TEN-2026-0001")
      .set(sampleBookingPayload(TOURIST_UID, now))
  );
  await assertSucceeds(
    touristDb
      .collection("booking_status_lookup")
      .doc("TEN-2026-0001")
      .set({
        referenceNumber: "TEN-2026-0001",
        bookingId: "TEN-2026-0001",
        userId: TOURIST_UID,
        eventName: "সুন্দরবন ভ্রমণ — অক্টোবর স্পেশাল",
        destination: "সুন্দরবন",
        tourDate: "০৮ অক্টোবর",
        seatCount: 2,
        seatLabels: "B1, B2",
        status: "Payment Verification Pending",
        updatedAt: now,
      })
  );

  // Self-confirmation is rejected
  const selfConfirmed = sampleBookingPayload(TOURIST_UID, now);
  selfConfirmed.status = "Confirmed";
  await assertFails(
    touristDb.collection("bookings").doc("TEN-2026-0099").set(selfConfirmed)
  );

  // Missing or invalid Gmail/email is rejected
  const badEmailBooking = sampleBookingPayload(TOURIST_UID, now);
  badEmailBooking.touristEmail = "not-an-email";
  await assertFails(
    touristDb.collection("bookings").doc("TEN-2026-0098").set(badEmailBooking)
  );
});

test("PII & Multi-user isolation: another tourist cannot read/update/list someone else's booking or admin_accounts", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("events").doc("event_sundarbans_oct08").set(sampleEventPayload());
    await db
      .collection("bookings")
      .doc("TEN-2026-0001")
      .set(sampleBookingPayload(TOURIST_UID));
    await db.collection("admin_accounts").doc("admin_1").set({
      name: "আবদুল হাকিম",
      username: "abdulhakim",
      designation: "এডমিন",
      role: "ADMIN",
      passwordHash: "0123456789abcdef0123456789abcdef",
      passwordSalt: "abcdef123456",
      canManageEvents: true,
      canVerifyPayments: true,
      canManageBookings: true,
      canManageDestinations: false,
      canManageSettings: false,
      active: true,
      sortOrder: 1,
      createdAt: new Date(Date.now() - 5000),
      updatedAt: new Date(Date.now() - 5000),
    });
  });

  const aliceDb = testEnv.authenticatedContext(TOURIST_UID).firestore();
  const bobDb = testEnv.authenticatedContext(OTHER_TOURIST_UID).firestore();

  // Owner filtered query succeeds; unfiltered query fails
  await assertSucceeds(
    aliceDb.collection("bookings").where("userId", "==", TOURIST_UID).get()
  );
  await assertFails(aliceDb.collection("bookings").get());

  // Cross-user PII read/update fails
  await assertFails(bobDb.collection("bookings").doc("TEN-2026-0001").get());
  await assertFails(
    bobDb.collection("bookings").where("userId", "==", TOURIST_UID).get()
  );
  await assertFails(
    bobDb.collection("bookings").doc("TEN-2026-0001").update({
      status: "Cancelled",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Admin accounts strictly inaccessible to tourists
  await assertFails(bobDb.collection("admin_accounts").get());
  await assertFails(aliceDb.collection("admin_accounts").doc("admin_1").get());
});

test("Super Admin can verify payments and update booking status", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("events").doc("event_sundarbans_oct08").set(sampleEventPayload());
    await db
      .collection("bookings")
      .doc("TEN-2026-0001")
      .set(sampleBookingPayload(TOURIST_UID));
  });

  const adminDb = testEnv
    .authenticatedContext(ADMIN_UID, {
      email: BOOTSTRAP_EMAIL,
      email_verified: true,
    })
    .firestore();

  await assertSucceeds(adminDb.collection("bookings").get());
  await assertSucceeds(
    adminDb.collection("bookings").doc("TEN-2026-0001").update({
      status: "Confirmed",
      verifiedBy: "নাজিম উদ্দিন",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Reviews: signed-in tourist can submit, read, edit, and delete own review; invalid ratings or other-user edits fail; admin can delete", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(TOURIST_UID).firestore();
  const bobDb = testEnv.authenticatedContext(OTHER_TOURIST_UID).firestore();
  const adminDb = testEnv
    .authenticatedContext(ADMIN_UID, {
      email: BOOTSTRAP_EMAIL,
      email_verified: true,
    })
    .firestore();

  const now = new Date(Date.now() - 2000);
  const validReview = {
    userId: TOURIST_UID,
    reviewerName: "রাকিবুল হাসান",
    eventId: "event_sundarbans_oct08",
    eventName: "সুন্দরবন ভ্রমণ — অক্টোবর স্পেশাল",
    rating: 5,
    comment: "অসাধারণ ম্যানেজমেন্ট ও খুব নিরাপদ ভ্রমণ অভিজ্ঞতা ছিল!",
    createdAt: now,
    updatedAt: now,
  };

  // Unauthenticated access rejected
  await assertFails(unauthDb.collection("reviews").get());
  await assertFails(unauthDb.collection("reviews").doc("rev_1").set(validReview));

  // Alice creates her review
  await assertSucceeds(aliceDb.collection("reviews").doc("rev_1").set(validReview));

  // Invalid ratings (0 or 6) and short comment fail
  await assertFails(
    aliceDb.collection("reviews").doc("rev_bad_rating").set({
      ...validReview,
      rating: 6,
    })
  );
  await assertFails(
    aliceDb.collection("reviews").doc("rev_zero_rating").set({
      ...validReview,
      rating: 0,
    })
  );
  await assertFails(
    aliceDb.collection("reviews").doc("rev_short_comment").set({
      ...validReview,
      comment: "ক",
    })
  );

  // Bob can list/read reviews, cannot spoof userId or edit/delete Alice's review
  await assertSucceeds(bobDb.collection("reviews").get());
  await assertFails(
    bobDb.collection("reviews").doc("rev_spoof").set({
      ...validReview,
      userId: TOURIST_UID,
    })
  );
  await assertFails(
    bobDb.collection("reviews").doc("rev_1").update({
      rating: 1,
      comment: "হ্যাক করা মন্তব্য",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(bobDb.collection("reviews").doc("rev_1").delete());

  // Alice can update her own review
  await assertSucceeds(
    aliceDb.collection("reviews").doc("rev_1").update({
      ...validReview,
      rating: 4,
      comment: "খুব সুন্দর ছিল, খাবার ও গাইড দারুণ ছিল।",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Admin can delete a review, or Alice can delete her own review
  await assertSucceeds(adminDb.collection("reviews").doc("rev_1").delete());
});

test("Monthly Event Schedule: only authorized admins can create/edit/delete schedule; viewers can read and book open events", async () => {
  const now = new Date(Date.now() - 3000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("events").doc("event_sundarbans_oct08").set(sampleEventPayload(now));
    await db.collection("admins").doc("event_editor_admin").set({
      uid: "event_editor_admin",
      email: "hakim@tourexpert.com",
      adminAccountId: "admin_1",
      role: "ADMIN",
      displayName: "আবদুল হাকিম",
      canManageEvents: true,
      createdAt: now,
      updatedAt: now,
    });
    await db.collection("admins").doc("readonly_event_admin").set({
      uid: "readonly_event_admin",
      email: "masud@tourexpert.com",
      adminAccountId: "admin_2",
      role: "ADMIN",
      displayName: "মাসুদ পারভেজ",
      canManageEvents: false,
      createdAt: now,
      updatedAt: now,
    });
  });

  const superAdminDb = testEnv
    .authenticatedContext(ADMIN_UID, {
      email: BOOTSTRAP_EMAIL,
      email_verified: true,
    })
    .firestore();
  const editorAdminDb = testEnv.authenticatedContext("event_editor_admin").firestore();
  const noEventPermAdminDb = testEnv.authenticatedContext("readonly_event_admin").firestore();
  const viewerDb = testEnv.authenticatedContext(TOURIST_UID).firestore();

  const updateTime = new Date(Date.now() - 1000);

  // Authorized admin can add a new monthly event & update schedule date/price
  await assertSucceeds(
    editorAdminDb.collection("events").doc("event_nov_sajek").set({
      ...sampleEventPayload(updateTime),
      name: "নভেম্বর সাজেক ভ্যালি স্পেশাল",
      destination: "সাজেক",
      tourDate: "১৪ নভেম্বর",
      monthTag: "NEXT_MONTH",
      category: "HILL",
      price: 2599,
    })
  );
  await assertSucceeds(
    superAdminDb.collection("events").doc("event_sundarbans_oct08").update({
      tourDate: "২২ নভেম্বর",
      monthTag: "THIS_MONTH",
      price: 2399,
      updatedAt: updateTime,
    })
  );

  // Admin WITHOUT canManageEvents cannot edit/create/delete events
  await assertFails(
    noEventPermAdminDb.collection("events").doc("event_unauthorized").set(sampleEventPayload(updateTime))
  );
  await assertFails(
    noEventPermAdminDb.collection("events").doc("event_sundarbans_oct08").update({
      price: 500,
      updatedAt: updateTime,
    })
  );

  // Regular viewer cannot create, alter schedule fields, or delete events
  await assertFails(
    viewerDb.collection("events").doc("event_viewer_spoof").set(sampleEventPayload(updateTime))
  );
  await assertFails(
    viewerDb.collection("events").doc("event_sundarbans_oct08").update({
      tourDate: "৩০ ডিসেম্বর",
      price: 100,
      updatedAt: updateTime,
    })
  );
  await assertFails(
    viewerDb.collection("events").doc("event_sundarbans_oct08").delete()
  );

  // Viewer CAN read updated schedule and CANNOT hold an already-booked seat ("A1")
  await assertSucceeds(viewerDb.collection("events").get());
  await assertFails(
    viewerDb.collection("events").doc("event_sundarbans_oct08").update({
      holdSeats: ["A1"],
      updatedAt: updateTime,
    })
  );

  // Viewer CAN hold available seat ("C1") on OPEN event
  await assertSucceeds(
    viewerDb.collection("events").doc("event_sundarbans_oct08").update({
      holdSeats: ["C1"],
      updatedAt: updateTime,
    })
  );

  // Authorized admin can close an event, after which viewers cannot hold seats
  await assertSucceeds(
    editorAdminDb.collection("events").doc("event_nov_sajek").update({
      bookingStatus: "CLOSED",
      updatedAt: updateTime,
    })
  );
  await assertFails(
    viewerDb.collection("events").doc("event_nov_sajek").update({
      holdSeats: ["B1"],
      updatedAt: updateTime,
    })
  );
  await assertSucceeds(superAdminDb.collection("events").doc("event_nov_sajek").delete());
});


