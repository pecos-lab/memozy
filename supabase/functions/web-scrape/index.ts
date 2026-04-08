import "@supabase/functions-js/edge-runtime.d.ts";
import { verifyAppAuth } from "../_shared/auth.ts";

Deno.serve(async (req) => {
  if (req.method !== "POST") {
    return new Response("Method Not Allowed", { status: 405 });
  }

  const authError = verifyAppAuth(req);
  if (authError) return authError;

  try {
    const { url } = await req.json();

    if (!url) {
      return new Response(
        JSON.stringify({ error: "Missing 'url' parameter" }),
        { status: 400, headers: { "Content-Type": "application/json" } },
      );
    }

    // 웹페이지 fetch
    const pageRes = await fetch(url, {
      headers: {
        "User-Agent":
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Accept": "text/html,application/xhtml+xml",
        "Accept-Language": "ko-KR,ko;q=0.9,en;q=0.8",
      },
    });

    if (!pageRes.ok) {
      return new Response(
        JSON.stringify({ error: `Failed to fetch: ${pageRes.status}` }),
        { status: 502, headers: { "Content-Type": "application/json" } },
      );
    }

    const html = await pageRes.text();

    // 제목 추출
    const titleMatch = html.match(/<title[^>]*>([^<]*)<\/title>/i);
    const ogTitleMatch = html.match(/<meta\s+property="og:title"\s+content="([^"]*)"/i);
    const title = ogTitleMatch?.[1] || titleMatch?.[1]?.trim() || null;

    // 본문 텍스트 추출 (HTML 태그 제거)
    let text = html
      // script, style, nav, header, footer 제거
      .replace(/<script[\s\S]*?<\/script>/gi, "")
      .replace(/<style[\s\S]*?<\/style>/gi, "")
      .replace(/<nav[\s\S]*?<\/nav>/gi, "")
      .replace(/<header[\s\S]*?<\/header>/gi, "")
      .replace(/<footer[\s\S]*?<\/footer>/gi, "")
      .replace(/<aside[\s\S]*?<\/aside>/gi, "")
      // HTML 태그 제거
      .replace(/<[^>]+>/g, " ")
      // HTML 엔티티 디코딩
      .replace(/&nbsp;/g, " ")
      .replace(/&amp;/g, "&")
      .replace(/&lt;/g, "<")
      .replace(/&gt;/g, ">")
      .replace(/&quot;/g, '"')
      .replace(/&#39;/g, "'")
      // 공백 정리
      .replace(/\s+/g, " ")
      .trim();

    // 텍스트 길이 제한 (15000자)
    if (text.length > 15000) {
      text = text.substring(0, 15000) + "...";
    }

    return new Response(
      JSON.stringify({ title, text }),
      { headers: { "Content-Type": "application/json" } },
    );
  } catch (e) {
    return new Response(
      JSON.stringify({ error: (e as Error).message }),
      { status: 500, headers: { "Content-Type": "application/json" } },
    );
  }
});
