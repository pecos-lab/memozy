import "@supabase/functions-js/edge-runtime.d.ts";

const SUPADATA_API_KEY = Deno.env.get("SUPADATA_API_KEY")!;

Deno.serve(async (req) => {
  if (req.method !== "GET") {
    return new Response("Method Not Allowed", { status: 405 });
  }

  try {
    const reqUrl = new URL(req.url);
    const videoUrl = reqUrl.searchParams.get("url");
    const lang = reqUrl.searchParams.get("lang") ?? "ko";

    if (!videoUrl) {
      return new Response(
        JSON.stringify({ error: "Missing 'url' parameter" }),
        { status: 400, headers: { "Content-Type": "application/json" } },
      );
    }

    const supadataUrl = new URL(
      "https://api.supadata.ai/v1/youtube/transcript",
    );
    supadataUrl.searchParams.set("url", videoUrl);
    supadataUrl.searchParams.set("lang", lang);

    const supadataRes = await fetch(supadataUrl.toString(), {
      headers: { "x-api-key": SUPADATA_API_KEY },
    });

    const data = await supadataRes.text();

    return new Response(data, {
      status: supadataRes.status,
      headers: { "Content-Type": "application/json" },
    });
  } catch (e) {
    return new Response(
      JSON.stringify({ error: (e as Error).message }),
      { status: 500, headers: { "Content-Type": "application/json" } },
    );
  }
});
