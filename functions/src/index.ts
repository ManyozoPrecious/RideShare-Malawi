
import { initializeApp } from "firebase-admin/app";
import { getFirestore, FieldValue } from "firebase-admin/firestore";
import {
  onCall,
  onRequest,
  HttpsError,
} from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import * as crypto from "crypto";

initializeApp();

const db = getFirestore();

const PAYCHANGU_SECRET_KEY =
  defineSecret("PAYCHANGU_SECRET_KEY");

const PAYCHANGU_WEBHOOK_SECRET =
  defineSecret("PAYCHANGU_WEBHOOK_SECRET");

const PAYCHANGU_URL = "https://api.paychangu.com";

const OPERATORS: Record<string, string> = {
  airtel: "20be6c20-adeb-4b5b-a7ba-0769820df4fb",
  tnm: "27494cb5-ba9e-437f-a114-4e7a7686bcca",
};

function normalizePhone(phone: string): string {
  const digits = phone.replace(/\D/g, "");

  if (digits.startsWith("265") && digits.length === 12) {
    return `+${digits}`;
  }

  if (digits.startsWith("0") && digits.length === 10) {
    return `+265${digits.substring(1)}`;
  }

  if (digits.length === 9) {
    return `+265${digits}`;
  }

  throw new HttpsError(
    "invalid-argument",
    "Enter a valid Malawi mobile number."
  );
}

async function verifyWithPayChangu(chargeId: string) {
  const response = await fetch(
    `${PAYCHANGU_URL}/mobile-money/payments/${encodeURIComponent(chargeId)}/verify`,
    {
      method: "GET",
      headers: {
        Accept: "application/json",
        Authorization: `Bearer ${PAYCHANGU_SECRET_KEY.value()}`,
      },
    }
  );

  if (!response.ok) {
    throw new Error(
      `PayChangu verification failed: HTTP ${response.status}`
    );
  }

  return await response.json() as {
    status?: string;
    data?: {
      charge_id?: string;
      status?: string;
      amount?: number | string;
      currency?: string;
      ref_id?: string;
      trans_id?: string;
    };
  };
}

async function finalizeVerifiedPayment(chargeId: string) {
  const result = await verifyWithPayChangu(chargeId);
  const data = result.data;

  if (
    result.status !== "successful" ||
    data?.status !== "success" ||
    data.charge_id !== chargeId
  ) {
    return false;
  }

  const paymentRef = db.collection("payments").doc(chargeId);

  await db.runTransaction(async (transaction) => {
    const paymentSnapshot = await transaction.get(paymentRef);

    if (!paymentSnapshot.exists) {
      throw new Error("Payment record not found.");
    }

    const payment = paymentSnapshot.data()!;

    const paidAmount = Number(data.amount);
    const expectedAmount = Number(payment.amount);

    const currency = String(data.currency || "").toUpperCase();

    if (
      !Number.isFinite(paidAmount) ||
      paidAmount < expectedAmount ||
      !["MWK", "MK"].includes(currency) ||
      payment.currency !== "MWK"
    ) {
      throw new Error("Payment amount or currency mismatch.");
    }

    if (payment.status === "successful") {
      return;
    }

    transaction.update(paymentRef, {
      status: "successful",
      transactionReference: data.ref_id || null,
      transactionId: data.trans_id || null,
      verifiedAt: FieldValue.serverTimestamp(),
    });

    transaction.update(
      db.collection("rides").doc(payment.rideId),
      {
        paymentStatus: "paid",
        updatedAt: FieldValue.serverTimestamp(),
      }
    );
  });

  return true;
}

// 1. Start an Airtel Money or TNM Mpamba payment.
export const createMobileMoneyPayment = onCall(
  {
    secrets: [PAYCHANGU_SECRET_KEY],
    maxInstances: 2,
  },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError(
        "unauthenticated",
        "Please log in before making a payment."
      );
    }

    const rideId = String(request.data?.rideId || "");
    const phone = normalizePhone(
      String(request.data?.phoneNumber || "")
    );

    const operator = String(request.data?.operator || "")
      .toLowerCase();

    if (!rideId || !OPERATORS[operator]) {
      throw new HttpsError(
        "invalid-argument",
        "Invalid ride ID or mobile money operator."
      );
    }

    const rideRef = db.collection("rides").doc(rideId);
    const rideSnapshot = await rideRef.get();

    if (!rideSnapshot.exists) {
      throw new HttpsError(
        "not-found",
        "Ride was not found."
      );
    }

    const ride = rideSnapshot.data()!;

    if (ride.passengerId !== request.auth.uid) {
      throw new HttpsError(
        "permission-denied",
        "This ride does not belong to you."
      );
    }

    if (ride.paymentStatus === "paid") {
      throw new HttpsError(
        "failed-precondition",
        "This ride has already been paid."
      );
    }

    const amount = Number(ride.fare);

    if (!Number.isSafeInteger(amount) || amount <= 0) {
      throw new HttpsError(
        "failed-precondition",
        "The ride fare must be a positive whole number in MWK."
      );
    }

    const chargeId = `RSM-${crypto.randomUUID()}`;

    const paymentRef = db.collection("payments").doc(chargeId);

    await paymentRef.create({
      rideId,
      userId: request.auth.uid,
      amount,
      currency: "MWK",
      phoneNumber: phone,
      operator,
      chargeId,
      status: "initiating",
      createdAt: FieldValue.serverTimestamp(),
    });

    try {
      const response = await fetch(
        `${PAYCHANGU_URL}/mobile-money/payments/initialize`,
        {
          method: "POST",
          headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
            Authorization: `Bearer ${PAYCHANGU_SECRET_KEY.value()}`,
          },
          body: JSON.stringify({
            mobile: phone,
            mobile_money_operator_ref_id: OPERATORS[operator],
            amount: String(amount),
            charge_id: chargeId,
          }),
        }
      );

      const result = await response.json() as {
        status?: string;
        message?: string;
        data?: {
          charge_id?: string;
          ref_id?: string;
          trans_id?: string;
          status?: string;
          currency?: string;
          amount?: number | string;
        };
      };

      if (
        !response.ok ||
        result.status !== "success" ||
        result.data?.charge_id !== chargeId
      ) {
        throw new Error(
          result.message || "PayChangu could not initialize payment."
        );
      }

      await paymentRef.update({
        status: "pending",
        providerReference: result.data.ref_id || null,
        providerTransactionId: result.data.trans_id || null,
      });

      return {
        success: true,
        chargeId,
        status: "pending",
        message: "Payment request sent. Please authorize it on your phone.",
      };
    } catch (error) {
      await paymentRef.update({
        status: "failed",
        failureMessage:
          error instanceof Error ? error.message : "Payment initialization failed.",
        updatedAt: FieldValue.serverTimestamp(),
      });

      throw new HttpsError(
        "internal",
        "Could not start the mobile money payment."
      );
    }
  }
);

// 2. Check payment status from the Android app.
export const verifyMobileMoneyPayment = onCall(
  {
    secrets: [PAYCHANGU_SECRET_KEY],
    maxInstances: 2,
  },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError(
        "unauthenticated",
        "Please log in."
      );
    }

    const chargeId = String(request.data?.chargeId || "");

    if (!chargeId) {
      throw new HttpsError(
        "invalid-argument",
        "Charge ID is required."
      );
    }

    const paymentRef = db.collection("payments").doc(chargeId);
    const paymentSnapshot = await paymentRef.get();

    if (
      !paymentSnapshot.exists ||
      paymentSnapshot.data()?.userId !== request.auth.uid
    ) {
      throw new HttpsError(
        "not-found",
        "Payment was not found."
      );
    }

    const payment = paymentSnapshot.data()!;

    if (payment.status === "successful") {
      return { status: "successful" };
    }

    try {
      const verified = await finalizeVerifiedPayment(chargeId);

      if (verified) {
        return { status: "successful" };
      }

      return { status: "pending" };
    } catch {
      throw new HttpsError(
        "internal",
        "Could not verify payment. Please try again."
      );
    }
  }
);

// 3. Receive and authenticate PayChangu webhook notifications.
export const payChanguWebhook = onRequest(
  {
    secrets: [
      PAYCHANGU_SECRET_KEY,
      PAYCHANGU_WEBHOOK_SECRET,
    ],
    maxInstances: 2,
  },
  async (request, response) => {
    if (request.method !== "POST") {
      response.status(405).send("Method not allowed");
      return;
    }

    const signature = request.get("Signature");

    if (!signature) {
      response.status(401).send("Missing signature");
      return;
    }

    const expectedSignature = crypto
      .createHmac("sha256", PAYCHANGU_WEBHOOK_SECRET.value())
      .update(request.rawBody)
      .digest("hex");

    const received = Buffer.from(signature, "utf8");
    const expected = Buffer.from(expectedSignature, "utf8");

    if (
      received.length !== expected.length ||
      !crypto.timingSafeEqual(received, expected)
    ) {
      response.status(401).send("Invalid signature");
      return;
    }

    const chargeId = String(request.body?.charge_id || "");

    if (!chargeId) {
      response.status(400).send("Missing charge ID");
      return;
    }

    try {
      await finalizeVerifiedPayment(chargeId);
      response.status(200).send("Webhook received");
    } catch (error) {
      console.error("Webhook processing failed:", error);
      response.status(500).send("Verification failed");
    }
  }
);
