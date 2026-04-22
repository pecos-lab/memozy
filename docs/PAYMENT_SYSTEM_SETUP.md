# Payment System Setup Guide

This guide explains how to set up and configure the server-side payment system for memozy.

## Overview

The payment system validates in-app purchases (Google Play / App Store) on the server to prevent fraud and enable cross-device subscription sync.

**Architecture:**
- Client: Android app with Google Play Billing
- Server: Supabase Edge Functions for receipt validation
- Database: PostgreSQL tables for subscription state
- API: Google Play Developer API for receipt verification

## Prerequisites

1. Google Cloud Project with Google Play Developer API enabled
2. Service Account with Google Play Developer API access
3. Supabase project with Edge Functions enabled
4. Environment variables configured

## Setup Steps

### 1. Google Cloud Console Setup

#### A. Create Service Account

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project (or create one)
3. Navigate to **IAM & Admin** → **Service Accounts**
4. Click **Create Service Account**
   - Name: `memozy-billing-validator`
   - Description: `Service account for validating in-app purchases`
5. Click **Create and Continue**
6. Grant role: **Service Account User**
7. Click **Done**

#### B. Generate Service Account Key

1. Click on the service account you just created
2. Go to **Keys** tab
3. Click **Add Key** → **Create new key**
4. Select **JSON** format
5. Click **Create** - a JSON file will download
6. **⚠️ Keep this file secure! It contains credentials.**

#### C. Enable Google Play Developer API

1. Navigate to **APIs & Services** → **Library**
2. Search for "Google Play Android Developer API"
3. Click **Enable**

### 2. Google Play Console Setup

#### A. Link Service Account

1. Go to [Google Play Console](https://play.google.com/console/)
2. Select your app (memozy)
3. Navigate to **Setup** → **API access**
4. Under **Service accounts**, click **Link a service account**
5. Select the service account you created (`memozy-billing-validator`)
6. Grant permissions:
   - ✅ **View financial data**
   - ✅ **Manage orders and subscriptions**

#### B. Create Subscription Products

1. Navigate to **Monetize** → **Subscriptions**
2. Create subscription: **Pro Monthly**
   - Product ID: `pro_monthly`
   - Price: (set your price)
   - Billing period: 1 month
3. Create subscription: **Pro Yearly**
   - Product ID: `pro_yearly`
   - Price: (set your price)
   - Billing period: 1 year

#### C. Create Donation Products (Optional)

1. Navigate to **Monetize** → **Products** → **In-app products**
2. Create 5 donation tiers:
   - `donation_rice_1` - ₩1,000
   - `donation_rice_2` - ₩3,000
   - `donation_rice_3` - ₩5,000
   - `donation_rice_4` - ₩10,000
   - `donation_rice_5` - ₩30,000

### 3. Supabase Configuration

#### A. Set Environment Variables

Add to your Supabase project settings (or `.env` for local development):

```bash
# Google Play API
GOOGLE_SERVICE_ACCOUNT_JSON='<contents-of-service-account-json-file>'

# Supabase (already configured)
SUPABASE_URL='<your-supabase-url>'
SUPABASE_SERVICE_ROLE_KEY='<your-service-role-key>'

# App Secret (for API authentication)
APP_SECRET_KEY='<your-app-secret-key>'
```

**To set in Supabase Dashboard:**
1. Go to Project Settings → Edge Functions
2. Add each secret:
   ```bash
   supabase secrets set GOOGLE_SERVICE_ACCOUNT_JSON="$(cat path/to/service-account.json)"
   ```

#### B. Deploy Database Migration

```bash
cd memozy
supabase db push
```

This creates:
- `user_subscriptions` - Current subscription state per user
- `subscription_transactions` - Audit log of all subscription events

#### C. Deploy Edge Functions

```bash
supabase functions deploy validate-purchase
supabase functions deploy sync-subscription
```

### 4. Android App Configuration

The Android app is already configured to:
- ✅ Call `validate-purchase` after successful purchase
- ✅ Call `sync-subscription` on app launch
- ✅ Handle server validation failures gracefully (with local fallback)

**No additional client configuration needed.**

### 5. Testing

#### A. Test Purchases (Sandbox)

1. Add test account in Google Play Console:
   - **Setup** → **License testing**
   - Add Gmail account
2. Install app from internal testing track
3. Make test purchase (will not be charged)
4. Verify in Supabase:
   ```sql
   SELECT * FROM user_subscriptions;
   SELECT * FROM subscription_transactions;
   ```

#### B. Monitor Logs

Check Edge Function logs:
```bash
supabase functions logs validate-purchase
supabase functions logs sync-subscription
```

#### C. Test Scenarios

- ✅ New subscription purchase
- ✅ Subscription renewal (auto-renewing)
- ✅ Subscription cancellation
- ✅ Purchase restoration (cross-device)
- ✅ Expired subscription (grace period)
- ✅ Refund handling

### 6. Production Deployment

1. **Deploy to production Supabase:**
   ```bash
   supabase link --project-ref <prod-project-ref>
   supabase db push
   supabase functions deploy validate-purchase
   supabase functions deploy sync-subscription
   ```

2. **Update app build variant:**
   - Use production `WORKER_URL` in `local.properties`
   - Use production `SUPABASE_URL` and `SUPABASE_ANON_KEY`

3. **Submit app to Google Play:**
   - Internal testing → Closed testing → Open testing → Production

## Architecture Details

### Database Schema

**user_subscriptions:**
- `user_id` (PK) - Supabase auth user
- `tier` - FREE or PRO
- `product_id` - Subscription SKU
- `platform` - android or ios
- `purchase_token` - Receipt token
- `expires_at` - Subscription expiration
- `auto_renewing` - Auto-renewal status
- `grace_period_ends_at` - Payment recovery period

**subscription_transactions:**
- Audit log for all events (purchase, renewal, cancellation, refund, etc.)

### API Endpoints

**POST /validate-purchase**
- Validates purchase with Google Play Developer API
- Updates `user_subscriptions` table
- Logs transaction in `subscription_transactions`

**POST /sync-subscription**
- Returns current subscription state from server
- Handles expiration and grace period logic
- Creates FREE tier if user doesn't exist

### Security

- ✅ App → Edge Function: `x-app-key` header authentication
- ✅ Edge Function → Google Play API: Service account OAuth2
- ✅ Edge Function → Database: Service role key
- ✅ RLS policies: Users can only read their own subscriptions
- ✅ Subscriptions can only be modified by server (not clients)

## Troubleshooting

### Error: "GOOGLE_SERVICE_ACCOUNT_JSON not configured"
- Check environment variable is set in Supabase
- Verify JSON format is valid

### Error: "Google Play API error: 401"
- Service account not linked in Google Play Console
- Service account missing required permissions

### Subscription not syncing
- Check user is logged in (authRepository.currentUser)
- Verify Edge Function logs for errors
- Check database RLS policies

### Purchase validation fails but purchase succeeds
- This is expected - client sets PRO tier locally
- Server validation retries on next app launch via `syncSubscription()`

## Related Issues

- #294 - Payment system implementation (this PR)
- #279 - iOS StoreKit integration (future)
- #209 - Subscription UX and tier design
- #190 - Free tier limit adjustments

## Support

For issues with payment system:
1. Check Edge Function logs
2. Verify Google Play Console configuration
3. Test with sandbox account first
4. Open GitHub issue with logs
