const APP_SECRET = Deno.env.get("APP_SECRET_KEY");

export function verifyAppAuth(req: Request): Response | null {
  const apiKey = req.headers.get("x-app-key");
  if (!apiKey || apiKey !== APP_SECRET) {
    return new Response(
      JSON.stringify({ error: "Unauthorized" }),
      { status: 401, headers: { "Content-Type": "application/json" } },
    );
  }
  return null; // auth passed
}
