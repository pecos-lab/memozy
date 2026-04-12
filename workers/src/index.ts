export interface Env {
  GEMINI_API_KEY: string;
  SUPADATA_API_KEY: string;
  APP_SECRET_KEY: string;
  GEMINI_MODEL?: string;
  SUPABASE_URL: string;
  SUPABASE_SERVICE_KEY: string;
  SUPABASE_JWT_SECRET: string;
}

const AI_GATEWAY_URL = "https://gateway.ai.cloudflare.com/v1/fd6859f0e7b0f6307cfa850af2324d90/memozy/google-ai-studio";
const GEMINI_BASE_URL = `${AI_GATEWAY_URL}/v1beta`;
const FALLBACK_MODELS = ["gemini-2.5-flash-lite", "gemini-1.5-flash"];

// --- Auth ---

function verifyAppAuth(req: Request, env: Env): Response | null {
  const apiKey = req.headers.get("x-app-key");
  if (!apiKey || apiKey !== env.APP_SECRET_KEY) {
    return Response.json({ error: "Unauthorized" }, { status: 401 });
  }
  return null;
}

async function verifyJwt(req: Request, env: Env): Promise<{ userId: string } | Response> {
  const authHeader = req.headers.get("Authorization");
  if (!authHeader?.startsWith("Bearer ")) {
    return Response.json({ error: "Missing Authorization header" }, { status: 401 });
  }
  try {
    // Verify token via Supabase Auth API
    const url = `${env.SUPABASE_URL}/auth/v1/user`;
    console.log("Verifying JWT via:", url);
    const userRes = await fetch(url, {
      headers: {
        "Authorization": authHeader,
        "apikey": env.SUPABASE_SERVICE_KEY,
      },
    });
    if (!userRes.ok) {
      const err = await userRes.text();
      console.log("Supabase auth failed:", userRes.status, err);
      return Response.json({ error: "Auth failed", detail: err }, { status: 401 });
    }
    const user = await userRes.json<{ id: string }>();
    console.log("Auth OK, userId:", user.id);
    return { userId: user.id };
  } catch (e) {
    console.log("JWT verify exception:", (e as Error).message);
    return Response.json({ error: "Token verify failed", detail: (e as Error).message }, { status: 401 });
  }
}

// --- Supabase REST helper ---

async function supabaseRest(
  env: Env,
  path: string,
  options: { method?: string; body?: unknown; headers?: Record<string, string> } = {}
): Promise<Response> {
  const url = `${env.SUPABASE_URL}/rest/v1/${path}`;
  return fetch(url, {
    method: options.method ?? "GET",
    headers: {
      "apikey": env.SUPABASE_SERVICE_KEY,
      "Authorization": `Bearer ${env.SUPABASE_SERVICE_KEY}`,
      "Content-Type": "application/json",
      "Prefer": options.method === "POST" ? "return=representation" : "return=minimal",
      ...options.headers,
    },
    body: options.body ? JSON.stringify(options.body) : undefined,
  });
}

// --- Gemini helpers ---

async function callGemini(
  env: Env,
  model: string,
  body: unknown
): Promise<Response> {
  const url = `${GEMINI_BASE_URL}/models/${model}:generateContent?key=${env.GEMINI_API_KEY}`;
  return await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}

async function callGeminiStream(
  env: Env,
  model: string,
  body: unknown
): Promise<Response> {
  const url = `${GEMINI_BASE_URL}/models/${model}:streamGenerateContent?alt=sse&key=${env.GEMINI_API_KEY}`;
  return await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}

// --- Route handlers ---

async function handleGeminiGenerate(req: Request, env: Env): Promise<Response> {
  const body = await req.json();
  const model = env.GEMINI_MODEL ?? "gemini-2.5-flash";

  let geminiRes = await callGemini(env, model, body);

  if (geminiRes.status === 503 || geminiRes.status === 429) {
    for (const fallback of FALLBACK_MODELS) {
      geminiRes = await callGemini(env, fallback, body);
      if (geminiRes.status !== 503 && geminiRes.status !== 429 && geminiRes.status !== 404) {
        break;
      }
    }
  }

  const data = await geminiRes.json();
  return Response.json(data, { status: geminiRes.status });
}

async function handleGeminiStream(req: Request, env: Env): Promise<Response> {
  const body = await req.json();
  const model = env.GEMINI_MODEL ?? "gemini-2.5-flash";

  let geminiRes = await callGeminiStream(env, model, body);

  if (geminiRes.status === 503 || geminiRes.status === 429) {
    for (const fallback of FALLBACK_MODELS) {
      geminiRes = await callGeminiStream(env, fallback, body);
      if (geminiRes.status !== 503 && geminiRes.status !== 429 && geminiRes.status !== 404) {
        break;
      }
    }
  }

  if (!geminiRes.ok) {
    const errorBody = await geminiRes.text();
    return new Response(errorBody, {
      status: geminiRes.status,
      headers: { "Content-Type": "application/json" },
    });
  }

  return new Response(geminiRes.body, {
    status: 200,
    headers: {
      "Content-Type": "text/event-stream",
      "Cache-Control": "no-cache",
      "Connection": "keep-alive",
    },
  });
}

async function handleYoutubeCaptions(req: Request, env: Env): Promise<Response> {
  const reqUrl = new URL(req.url);
  const videoUrl = reqUrl.searchParams.get("url");
  const lang = reqUrl.searchParams.get("lang") ?? "ko";

  if (!videoUrl) {
    return Response.json({ error: "Missing 'url' parameter" }, { status: 400 });
  }

  const supadataUrl = new URL("https://api.supadata.ai/v1/youtube/transcript");
  supadataUrl.searchParams.set("url", videoUrl);
  supadataUrl.searchParams.set("lang", lang);

  const supadataRes = await fetch(supadataUrl.toString(), {
    headers: { "x-api-key": env.SUPADATA_API_KEY },
  });

  const data = await supadataRes.text();
  return new Response(data, {
    status: supadataRes.status,
    headers: { "Content-Type": "application/json" },
  });
}

async function handleYoutubeTitle(req: Request): Promise<Response> {
  const url = new URL(req.url);
  const videoId = url.searchParams.get("videoId");

  if (!videoId) {
    return Response.json({ error: "Missing 'videoId' parameter" }, { status: 400 });
  }

  const oembedUrl = `https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=${videoId}&format=json`;
  const res = await fetch(oembedUrl);

  if (!res.ok) {
    return Response.json({ title: null });
  }

  const data: { title?: string } = await res.json();
  return Response.json({ title: data?.title ?? null });
}

async function handleWebScrape(req: Request): Promise<Response> {
  const { url } = await req.json<{ url?: string }>();

  if (!url) {
    return Response.json({ error: "Missing 'url' parameter" }, { status: 400 });
  }

  const pageRes = await fetch(url, {
    headers: {
      "User-Agent":
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
      Accept: "text/html,application/xhtml+xml",
      "Accept-Language": "ko-KR,ko;q=0.9,en;q=0.8",
    },
  });

  if (!pageRes.ok) {
    return Response.json({ error: `Failed to fetch: ${pageRes.status}` }, { status: 502 });
  }

  const html = await pageRes.text();

  const titleMatch = html.match(/<title[^>]*>([^<]*)<\/title>/i);
  const ogTitleMatch = html.match(/<meta\s+property="og:title"\s+content="([^"]*)"/i);
  const title = ogTitleMatch?.[1] || titleMatch?.[1]?.trim() || null;

  let text = html
    .replace(/<script[\s\S]*?<\/script>/gi, "")
    .replace(/<style[\s\S]*?<\/style>/gi, "")
    .replace(/<nav[\s\S]*?<\/nav>/gi, "")
    .replace(/<header[\s\S]*?<\/header>/gi, "")
    .replace(/<footer[\s\S]*?<\/footer>/gi, "")
    .replace(/<aside[\s\S]*?<\/aside>/gi, "")
    .replace(/<[^>]+>/g, " ")
    .replace(/&nbsp;/g, " ")
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/\s+/g, " ")
    .trim();

  if (text.length > 15000) {
    text = text.substring(0, 15000) + "...";
  }

  return Response.json({ title, text });
}

// --- Backup handlers ---

interface BackupRequest {
  device_name: string;
  app_version: string;
  db_version: number;
  tables: Record<string, unknown[]>;
}

async function handleBackupCreate(req: Request, env: Env): Promise<Response> {
  const auth = await verifyJwt(req, env);
  if (auth instanceof Response) {
    console.log("JWT auth failed");
    return auth;
  }
  console.log("JWT auth OK, userId:", auth.userId);

  const body = await req.json<BackupRequest>();
  console.log("Request body keys:", Object.keys(body));

  if (!body.device_name || !body.app_version || !body.tables) {
    return Response.json({ error: "Missing required fields" }, { status: 400 });
  }

  // Calculate size and memo count
  const dataStr = JSON.stringify(body.tables);
  const sizeBytes = new TextEncoder().encode(dataStr).length;

  if (sizeBytes > 10 * 1024 * 1024) {
    return Response.json({ error: "Backup too large (max 10MB)" }, { status: 413 });
  }

  const memoCount = body.tables.memos?.length ?? body.tables.memo?.length ?? 0;

  // 1. Create backup metadata
  const metaRes = await supabaseRest(env, "backups", {
    method: "POST",
    body: {
      user_id: auth.userId,
      device_name: body.device_name,
      app_version: body.app_version,
      db_version: body.db_version ?? 0,
      memo_count: memoCount,
      size_bytes: sizeBytes,
    },
  });

  if (!metaRes.ok) {
    const err = await metaRes.text();
    console.log("Supabase meta insert failed:", metaRes.status, err);
    return Response.json({ error: "Failed to create backup", detail: err }, { status: 500 });
  }
  console.log("Supabase meta insert OK");

  const [backup] = await metaRes.json<{ id: string; created_at: string }[]>();

  // 2. Insert backup data per table (skip null/empty tables)
  const dataRows = Object.entries(body.tables)
    .filter(([, rows]) => rows != null)
    .map(([tableName, rows]) => ({
      backup_id: backup.id,
      table_name: tableName,
      data: rows,
      row_count: Array.isArray(rows) ? rows.length : 0,
    }));

  const dataRes = await supabaseRest(env, "backup_data", {
    method: "POST",
    body: dataRows,
  });

  if (!dataRes.ok) {
    // Rollback: delete the backup metadata
    await supabaseRest(env, `backups?id=eq.${backup.id}`, { method: "DELETE" });
    const err = await dataRes.text();
    console.log("Backup data insert failed:", dataRes.status, err);
    return Response.json({ error: "Failed to save backup data", detail: err }, { status: 500 });
  }
  console.log("Backup data insert OK, tables:", dataRows.length);

  return Response.json(
    { id: backup.id, created_at: backup.created_at, memo_count: memoCount },
    { status: 201 }
  );
}

async function handleBackupList(req: Request, env: Env): Promise<Response> {
  const auth = await verifyJwt(req, env);
  if (auth instanceof Response) return auth;

  const res = await supabaseRest(
    env,
    `backups?user_id=eq.${auth.userId}&order=created_at.desc&select=id,device_name,app_version,db_version,memo_count,size_bytes,created_at`
  );

  if (!res.ok) {
    return Response.json({ error: "Failed to fetch backups" }, { status: 500 });
  }

  const data = await res.json();
  return Response.json(data);
}

async function handleBackupGet(req: Request, env: Env, backupId: string): Promise<Response> {
  const auth = await verifyJwt(req, env);
  if (auth instanceof Response) return auth;

  // Get metadata (RLS ensures user can only see own)
  const metaRes = await supabaseRest(
    env,
    `backups?id=eq.${backupId}&user_id=eq.${auth.userId}&select=*`
  );

  if (!metaRes.ok) {
    return Response.json({ error: "Failed to fetch backup" }, { status: 500 });
  }

  const metas = await metaRes.json<unknown[]>();
  if (!metas || metas.length === 0) {
    return Response.json({ error: "Backup not found" }, { status: 404 });
  }

  // Get backup data
  const dataRes = await supabaseRest(
    env,
    `backup_data?backup_id=eq.${backupId}&select=table_name,data,row_count`
  );

  if (!dataRes.ok) {
    return Response.json({ error: "Failed to fetch backup data" }, { status: 500 });
  }

  const dataRows = await dataRes.json<{ table_name: string; data: unknown; row_count: number }[]>();

  const tables: Record<string, unknown> = {};
  for (const row of dataRows) {
    tables[row.table_name] = row.data;
  }

  return Response.json({
    id: backupId,
    metadata: metas[0],
    tables,
  });
}

async function handleBackupDelete(req: Request, env: Env, backupId: string): Promise<Response> {
  const auth = await verifyJwt(req, env);
  if (auth instanceof Response) return auth;

  // CASCADE will delete backup_data too
  const res = await supabaseRest(
    env,
    `backups?id=eq.${backupId}&user_id=eq.${auth.userId}`,
    { method: "DELETE" }
  );

  if (!res.ok) {
    return Response.json({ error: "Failed to delete backup" }, { status: 500 });
  }

  return new Response(null, { status: 204 });
}

// --- Router ---

const routes: Record<string, { method: string; handler: (req: Request, env: Env) => Promise<Response> }> = {
  "/gemini-generate": { method: "POST", handler: handleGeminiGenerate },
  "/gemini-stream": { method: "POST", handler: handleGeminiStream },
  "/youtube-captions": { method: "GET", handler: handleYoutubeCaptions },
  "/youtube-title": { method: "GET", handler: handleYoutubeTitle },
  "/web-scrape": { method: "POST", handler: handleWebScrape },
  "/backup": { method: "POST", handler: handleBackupCreate },
  "/backups": { method: "GET", handler: handleBackupList },
};

export default {
  async fetch(req: Request, env: Env): Promise<Response> {
    const url = new URL(req.url);
    const pathname = url.pathname;

    // Handle /backup/:id routes
    const backupIdMatch = pathname.match(/^\/backup\/([0-9a-f-]+)$/);
    if (backupIdMatch) {
      const backupId = backupIdMatch[1];
      try {
        if (req.method === "GET") {
          return await handleBackupGet(req, env, backupId);
        } else if (req.method === "DELETE") {
          return await handleBackupDelete(req, env, backupId);
        }
        return new Response("Method Not Allowed", { status: 405 });
      } catch (e) {
        return Response.json({ error: (e as Error).message }, { status: 500 });
      }
    }

    const route = routes[pathname];

    if (!route) {
      return Response.json({ error: "Not Found" }, { status: 404 });
    }

    if (req.method !== route.method) {
      return new Response("Method Not Allowed", { status: 405 });
    }

    // Backup routes use JWT auth, others use app key auth
    const isBackupRoute = pathname === "/backup" || pathname === "/backups";
    if (!isBackupRoute) {
      const authError = verifyAppAuth(req, env);
      if (authError) return authError;
    }

    try {
      return await route.handler(req, env);
    } catch (e) {
      return Response.json(
        { error: (e as Error).message },
        { status: 500 }
      );
    }
  },
} satisfies ExportedHandler<Env>;
