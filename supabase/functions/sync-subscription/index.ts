import "@supabase/functions-js/edge-runtime.d.ts";
import { verifyAppAuth } from "../_shared/auth.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.3";

interface SyncRequest {
  userId: string;
}

interface SubscriptionResponse {
  tier: "FREE" | "PRO";
  productId: string | null;
  expiresAt: string | null;
  autoRenewing: boolean;
  inGracePeriod: boolean;
}

Deno.serve(async (req) => {
  if (req.method !== "POST") {
    return new Response("Method Not Allowed", { status: 405 });
  }

  const authError = verifyAppAuth(req);
  if (authError) return authError;

  try {
    const body: SyncRequest = await req.json();

    if (!body.userId) {
      return new Response(
        JSON.stringify({ error: "Missing required field: userId" }),
        { status: 400, headers: { "Content-Type": "application/json" } }
      );
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    // Fetch current subscription from database
    const { data, error } = await supabase
      .from("user_subscriptions")
      .select("tier, product_id, expires_at, auto_renewing, grace_period_ends_at")
      .eq("user_id", body.userId)
      .single();

    if (error) {
      // User doesn't have a subscription record - create FREE tier
      const { error: insertError } = await supabase
        .from("user_subscriptions")
        .insert({
          user_id: body.userId,
          tier: "FREE",
        });

      if (insertError) {
        throw new Error(`Failed to create subscription: ${insertError.message}`);
      }

      return new Response(
        JSON.stringify({
          tier: "FREE",
          productId: null,
          expiresAt: null,
          autoRenewing: false,
          inGracePeriod: false,
        } as SubscriptionResponse),
        {
          status: 200,
          headers: { "Content-Type": "application/json" },
        }
      );
    }

    // Check if subscription has expired
    const now = new Date();
    const expiresAt = data.expires_at ? new Date(data.expires_at) : null;
    const gracePeriodEndsAt = data.grace_period_ends_at
      ? new Date(data.grace_period_ends_at)
      : null;

    let currentTier = data.tier;
    let inGracePeriod = false;

    if (data.tier === "PRO" && expiresAt) {
      if (now > expiresAt) {
        // Check if in grace period
        if (gracePeriodEndsAt && now <= gracePeriodEndsAt) {
          inGracePeriod = true;
          // Still PRO during grace period
        } else {
          // Expired and no grace period - downgrade to FREE
          currentTier = "FREE";

          // Update database
          await supabase
            .from("user_subscriptions")
            .update({
              tier: "FREE",
              updated_at: now.toISOString(),
            })
            .eq("user_id", body.userId);

          // Log expiration event
          await supabase
            .from("subscription_transactions")
            .insert({
              user_id: body.userId,
              event_type: "expiration",
              platform: data.platform || "android",
              product_id: data.product_id || "",
              purchase_token: data.purchase_token || "",
              receipt_data: {},
              transaction_date: now.toISOString(),
              expires_at: data.expires_at,
            });
        }
      }
    }

    const response: SubscriptionResponse = {
      tier: currentTier as "FREE" | "PRO",
      productId: data.product_id,
      expiresAt: data.expires_at,
      autoRenewing: data.auto_renewing ?? false,
      inGracePeriod,
    };

    return new Response(JSON.stringify(response), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    });
  } catch (e) {
    console.error("Sync error:", e);
    return new Response(
      JSON.stringify({ error: (e as Error).message }),
      {
        status: 500,
        headers: { "Content-Type": "application/json" },
      }
    );
  }
});
