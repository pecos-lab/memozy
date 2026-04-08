import "@supabase/functions-js/edge-runtime.d.ts";
import { verifyAppAuth } from "../_shared/auth.ts";

Deno.serve(async (req) => {
  if (req.method !== "GET") {
    return new Response("Method Not Allowed", { status: 405 });
  }

  const authError = verifyAppAuth(req);
  if (authError) return authError;

  try {
    const url = new URL(req.url);
    const videoId = url.searchParams.get("videoId");

    if (!videoId) {
      return new Response(
        JSON.stringify({ error: "Missing 'videoId' parameter" }),
        { status: 400, headers: { "Content-Type": "application/json" } },
      );
    }

    // YouTube oEmbed API — 공식 API, 인증 불필요, 봇 차단 없음
    const oembedUrl = `https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=${videoId}&format=json`;
    const res = await fetch(oembedUrl);

    if (!res.ok) {
      return new Response(
        JSON.stringify({ title: null }),
        { headers: { "Content-Type": "application/json" } },
      );
    }

    const data = await res.json();
    const title = data?.title ?? null;

    return new Response(JSON.stringify({ title }), {
      headers: { "Content-Type": "application/json" },
    });
  } catch (e) {
    return new Response(
      JSON.stringify({ error: (e as Error).message }),
      { status: 500, headers: { "Content-Type": "application/json" } },
    );
  }
});
