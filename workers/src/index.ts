export interface Env {
  GEMINI_API_KEY: string;
  SUPADATA_API_KEY: string;
  APP_SECRET_KEY: string;
  GEMINI_MODEL?: string;
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

// --- Router ---

const routes: Record<string, { method: string; handler: (req: Request, env: Env) => Promise<Response> }> = {
  "/gemini-generate": { method: "POST", handler: handleGeminiGenerate },
  "/gemini-stream": { method: "POST", handler: handleGeminiStream },
  "/youtube-captions": { method: "GET", handler: handleYoutubeCaptions },
  "/youtube-title": { method: "GET", handler: handleYoutubeTitle },
  "/web-scrape": { method: "POST", handler: handleWebScrape },
};

export default {
  async fetch(req: Request, env: Env): Promise<Response> {
    const url = new URL(req.url);
    const route = routes[url.pathname];

    if (!route) {
      return Response.json({ error: "Not Found" }, { status: 404 });
    }

    if (req.method !== route.method) {
      return new Response("Method Not Allowed", { status: 405 });
    }

    const authError = verifyAppAuth(req, env);
    if (authError) return authError;

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
