import "@supabase/functions-js/edge-runtime.d.ts";
import { verifyAppAuth } from "../_shared/auth.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.3";

const GOOGLE_PACKAGE_NAME = "me.pecos.memozy";

interface PurchaseRequest {
  userId: string;
  productId: string;
  purchaseToken: string;
  platform: "android" | "ios";
}

interface GooglePlaySubscription {
  kind: string;
  startTimeMillis: string;
  expiryTimeMillis: string;
  autoRenewing: boolean;
  priceCurrencyCode: string;
  priceAmountMicros: string;
  countryCode: string;
  paymentState?: number; // 0 = pending, 1 = received
  cancelReason?: number;
  userCancellationTimeMillis?: string;
  orderId: string;
  linkedPurchaseToken?: string;
  purchaseType?: number; // 0 = test, 1 = promo
  acknowledgementState?: number; // 0 = pending, 1 = acknowledged
  externalAccountId?: string;
}

/**
 * Get Google Play access token using service account
 */
async function getGoogleAccessToken(): Promise<string> {
  const serviceAccountJson = Deno.env.get("GOOGLE_SERVICE_ACCOUNT_JSON");
  if (!serviceAccountJson) {
    throw new Error("GOOGLE_SERVICE_ACCOUNT_JSON not configured");
  }

  const serviceAccount = JSON.parse(serviceAccountJson);
  const now = Math.floor(Date.now() / 1000);
  const expiry = now + 3600;

  // Create JWT for Google OAuth2
  const header = {
    alg: "RS256",
    typ: "JWT",
  };

  const claimSet = {
    iss: serviceAccount.client_email,
    scope: "https://www.googleapis.com/auth/androidpublisher",
    aud: "https://oauth2.googleapis.com/token",
    exp: expiry,
    iat: now,
  };

  const encoder = new TextEncoder();
  const headerB64 = btoa(JSON.stringify(header))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=/g, "");
  const claimSetB64 = btoa(JSON.stringify(claimSet))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=/g, "");

  const unsignedToken = `${headerB64}.${claimSetB64}`;

  // Import private key
  const privateKey = serviceAccount.private_key;
  const pemHeader = "-----BEGIN PRIVATE KEY-----";
  const pemFooter = "-----END PRIVATE KEY-----";
  const pemContents = privateKey.substring(
    pemHeader.length,
    privateKey.length - pemFooter.length
  ).replace(/\s/g, "");
  const binaryDer = Uint8Array.from(atob(pemContents), (c) => c.charCodeAt(0));

  const cryptoKey = await crypto.subtle.importKey(
    "pkcs8",
    binaryDer,
    {
      name: "RSASSA-PKCS1-v1_5",
      hash: "SHA-256",
    },
    false,
    ["sign"]
  );

  // Sign the token
  const signature = await crypto.subtle.sign(
    "RSASSA-PKCS1-v1_5",
    cryptoKey,
    encoder.encode(unsignedToken)
  );

  const signatureB64 = btoa(String.fromCharCode(...new Uint8Array(signature)))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=/g, "");

  const jwt = `${unsignedToken}.${signatureB64}`;

  // Exchange JWT for access token
  const tokenResponse = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: `grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=${jwt}`,
  });

  if (!tokenResponse.ok) {
    const error = await tokenResponse.text();
    throw new Error(`Failed to get access token: ${error}`);
  }

  const tokenData = await tokenResponse.json();
  return tokenData.access_token;
}

/**
 * Validate Google Play subscription purchase
 */
async function validateGooglePlaySubscription(
  productId: string,
  purchaseToken: string
): Promise<GooglePlaySubscription> {
  const accessToken = await getGoogleAccessToken();

  const url = `https://androidpublisher.googleapis.com/androidpublisher/v3/applications/${GOOGLE_PACKAGE_NAME}/purchases/subscriptions/${productId}/tokens/${purchaseToken}`;

  const response = await fetch(url, {
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  if (!response.ok) {
    const error = await response.text();
    throw new Error(`Google Play API error: ${response.status} - ${error}`);
  }

  return await response.json();
}

/**
 * Update user subscription in database
 */
async function updateUserSubscription(
  userId: string,
  productId: string,
  purchaseToken: string,
  platform: "android" | "ios",
  subscriptionData: GooglePlaySubscription
) {
  const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
  const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

  const supabase = createClient(supabaseUrl, supabaseServiceKey);

  const expiresAt = new Date(parseInt(subscriptionData.expiryTimeMillis));
  const isActive = expiresAt > new Date();

  // Calculate grace period (typically 3-7 days after expiry for payment recovery)
  let gracePeriodEndsAt = null;
  if (subscriptionData.paymentState === 0) { // Payment pending
    gracePeriodEndsAt = new Date(expiresAt.getTime() + 7 * 24 * 60 * 60 * 1000); // +7 days
  }

  // Update user subscription
  const { error: updateError } = await supabase
    .from("user_subscriptions")
    .upsert({
      user_id: userId,
      tier: isActive ? "PRO" : "FREE",
      product_id: productId,
      platform: platform,
      purchase_token: purchaseToken,
      expires_at: expiresAt.toISOString(),
      auto_renewing: subscriptionData.autoRenewing,
      grace_period_ends_at: gracePeriodEndsAt?.toISOString(),
      updated_at: new Date().toISOString(),
    }, {
      onConflict: "user_id"
    });

  if (updateError) {
    throw new Error(`Failed to update subscription: ${updateError.message}`);
  }

  // Log transaction
  let eventType = "purchase";
  if (subscriptionData.cancelReason !== undefined) {
    eventType = "cancellation";
  } else if (subscriptionData.linkedPurchaseToken) {
    eventType = "renewal";
  }

  const { error: transactionError } = await supabase
    .from("subscription_transactions")
    .insert({
      user_id: userId,
      event_type: eventType,
      platform: platform,
      product_id: productId,
      purchase_token: purchaseToken,
      receipt_data: subscriptionData,
      transaction_date: new Date(parseInt(subscriptionData.startTimeMillis)).toISOString(),
      expires_at: expiresAt.toISOString(),
      is_trial: false,
    });

  if (transactionError) {
    console.error("Failed to log transaction:", transactionError);
    // Don't throw - transaction logging is non-critical
  }

  return {
    tier: isActive ? "PRO" : "FREE",
    expiresAt: expiresAt.toISOString(),
    autoRenewing: subscriptionData.autoRenewing,
  };
}

Deno.serve(async (req) => {
  if (req.method !== "POST") {
    return new Response("Method Not Allowed", { status: 405 });
  }

  const authError = verifyAppAuth(req);
  if (authError) return authError;

  try {
    const body: PurchaseRequest = await req.json();

    if (!body.userId || !body.productId || !body.purchaseToken || !body.platform) {
      return new Response(
        JSON.stringify({ error: "Missing required fields: userId, productId, purchaseToken, platform" }),
        { status: 400, headers: { "Content-Type": "application/json" } }
      );
    }

    if (body.platform === "ios") {
      // TODO: Implement iOS StoreKit validation (Issue #279)
      return new Response(
        JSON.stringify({ error: "iOS validation not yet implemented" }),
        { status: 501, headers: { "Content-Type": "application/json" } }
      );
    }

    // Validate with Google Play
    const subscriptionData = await validateGooglePlaySubscription(
      body.productId,
      body.purchaseToken
    );

    // Update database
    const result = await updateUserSubscription(
      body.userId,
      body.productId,
      body.purchaseToken,
      body.platform,
      subscriptionData
    );

    return new Response(
      JSON.stringify({
        success: true,
        subscription: result,
      }),
      {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }
    );
  } catch (e) {
    console.error("Validation error:", e);
    return new Response(
      JSON.stringify({ error: (e as Error).message }),
      {
        status: 500,
        headers: { "Content-Type": "application/json" },
      }
    );
  }
});
