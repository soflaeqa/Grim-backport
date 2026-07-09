package ac.grim.grimac.manager;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.manager.init.ReloadableInitable;
import ac.grim.grimac.manager.init.start.StartableInitable;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.anticheat.MessageUtil;
import ac.grim.grimac.utils.common.arguments.CommonGrimArguments;
import ac.grim.grimac.utils.data.webhook.discord.CompiledDiscordTemplate;
import ac.grim.grimac.utils.data.webhook.discord.Embed;
import ac.grim.grimac.utils.data.webhook.discord.EmbedField;
import ac.grim.grimac.utils.data.webhook.discord.EmbedFooter;
import ac.grim.grimac.utils.data.webhook.discord.WebhookMessage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Pattern;

public class DiscordManager implements StartableInitable, ReloadableInitable {
    private static final Pattern WEBHOOK_REGEX = Pattern.compile("^https://(?:canary\\.)?discord\\.com/api(?:/v\\d+)?/webhooks/\\d+/[\\w-]+(\\?thread_id=\\d+)?$");
    private static final Pattern HTTPS_URL_REGEX = Pattern.compile("^https://[^/\\s]+/\\S+$");
    private static final int timeoutMillis = CommonGrimArguments.URL_TIMEOUT.value();
    private static final ConcurrentLinkedDeque<PendingWebhookRequest> requests = new ConcurrentLinkedDeque<>();
    private static final AtomicBoolean taskStarted = new AtomicBoolean();
    private static final AtomicBoolean sending = new AtomicBoolean();
    private static long rateLimitedUntil;
    private URI url;
    private int embedColor;
    private CompiledDiscordTemplate compiledContent;
    private char backtickReplacement = '\u02CB';
    private String embedTitle = "";
    private boolean includeTimestamp;
    private boolean includeVerbose;
    private @Nullable String embedImageUrl;
    private @Nullable String embedThumbnailUrl;
    private @Nullable String embedFooterUrl;
    private String embedFooterText = "";

    private static final Pattern URL_PATTERN = Pattern.compile("^https?://(?:www\\.)?[-a-z0-9@:%._+~#=]{1,256}\\.[a-z0-9()]{1,6}\\b[-a-z0-9()@:%_+.~#?&/=]*$", Pattern.CASE_INSENSITIVE);

    private static String validatedConfigURL(String configPath, String defaultURL) {
        String url = GrimAPI.INSTANCE.getConfigManager().getConfig().getStringElse("embed-image-url", defaultURL);
        if (url == null || url.trim().isEmpty()) return null;
        if (URL_PATTERN.matcher(url).matches()) {
            return url;
        } else {
            LogUtil.warn("Invalid embed url for config path " + configPath + ": " + configPath);
            return defaultURL;
        }
    }

    @Override
    public void start() {
        reload();
    }

    @Override
    public void reload() {
        try {
            // Yes all of these fields should technically be volatile so they will be updated correctly on reload for HTTP threads to read
            // No we're not going to pay for atomic reads in the hot loop however cheap for a one in a billion chance to read an outdated config
            // When your discord webhook settings are changed (who changes them in prod?) that can be fixed with a restart
            ConfigManager config = GrimAPI.INSTANCE.getConfigManager().getConfig();
            if (!config.getBooleanElse("enabled", false)) {
                url = null;
                return;
            }

            String webhook = config.getStringElse("webhook", "");
            boolean strictValidation = !config.getBooleanElse("disable-webhook-validation", false);

            if (webhook.isEmpty()) {
                url = null;
            } else if (strictValidation) {
                if (!WEBHOOK_REGEX.matcher(webhook).matches()) {
                    LogUtil.error("Discord webhook URL does not match expected format"
                            + " (https://discord.com/api/webhooks/<id>/<token>): " + webhook);
                    LogUtil.error("If you are using a proxy or custom endpoint,"
                            + " set 'disable-webhook-validation: true' in the Discord config.");
                    url = null;
                } else {
                    url = new URI(webhook);
                }
            } else {
                if (!HTTPS_URL_REGEX.matcher(webhook).matches()) {
                    LogUtil.error("Discord webhook URL is not a valid HTTPS URL: " + webhook);
                    url = null;
                } else {
                    LogUtil.info("Webhook validation disabled — using custom endpoint: "
                            + webhook.substring(0, Math.min(webhook.length(), 40)) + "...");
                    url = new URI(webhook);
                }
            }
            // not adding these to the config since they may change in the future
            // mainly for just for allowing more customization
            embedImageUrl = validatedConfigURL("embed-image-url", null);
            embedThumbnailUrl = validatedConfigURL("embed-thumbnail-url", "https://crafthead.net/helm/%uuid%");
            embedFooterUrl = validatedConfigURL("embed-footer-url", "https://grim.ac/images/grim.png");
            embedFooterText = config.getStringElse("embed-footer-text", "v%grim_version%");
            embedTitle = config.getStringElse("embed-title", "**Grim Alert**");

            try {
                embedColor = Color.decode(config.getStringElse("embed-color", "#00FFFF")).getRGB();
            } catch (NumberFormatException e) {
                LogUtil.warn("Discord embed color is invalid");
            }

            StringBuilder sb = new StringBuilder();
            for (String string : config.getStringListElse("violation-content", getDefaultContents())) {
                sb.append(string).append("\n");
            }
            includeTimestamp = config.getBooleanElse("include-timestamp", true);
            includeVerbose = config.getBooleanElse("include-verbose", true);
            String btReplace = config.getStringElse("backtick-replacement-char", "\u02CB");
            backtickReplacement = (btReplace.isEmpty()) ? '\u02CB' : btReplace.charAt(0);
            compiledContent = CompiledDiscordTemplate.compile(sb.toString());
        } catch (Exception e) {
            LogUtil.error("Failed to load Discord webhook configuration", e);
        }
    }

    @Contract(value = " -> new", pure = true)
    private @NotNull @Unmodifiable List<@NotNull String> getDefaultContents() {
        return Collections.unmodifiableList(Arrays.asList(
                "**Player**: `%player%`",
                "**Check**: %check%",
                "**Violations**: %violations%",
                "**Client Version**: %version%",
                "**Brand**: `%brand%`",
                "**Ping**: %ping%",
                "**TPS**: %tps%"
        ));
    }

    public void sendAlert(@NotNull GrimPlayer player, String verbose, String checkName, int violations) {
        if (isDisabled()) {
            return;
        }

        // Per-alert overlay — avoids polluting the global static map
        Map<String, String> statics = new HashMap<>(GrimAPI.INSTANCE.getExternalAPI().getStaticReplacements());
        statics.put("%check%", checkName);
        statics.put("%violations%", Integer.toString(violations));

        Map<String, Function<GrimUser, String>> dynamics = GrimAPI.INSTANCE.getExternalAPI().getVariableReplacements();

        String content = compiledContent.render(player, statics, dynamics, backtickReplacement);

        Embed embed = new Embed(content)
                .color(embedColor)
                .title(embedTitle)
                .imageURL(MessageUtil.replacePlaceholders(player, embedImageUrl, false))
                .thumbnailURL(MessageUtil.replacePlaceholders(player, embedThumbnailUrl, false))
                .footer(new EmbedFooter(
                        MessageUtil.replacePlaceholders(player, embedFooterText, false),
                        MessageUtil.replacePlaceholders(player, embedFooterUrl, false)
                ));

        if (includeTimestamp) embed.timestamp(Instant.now());

        if (!verbose.isEmpty() && includeVerbose) {
            embed.addFields(new EmbedField("Verbose", CompiledDiscordTemplate.escapeMarkdown(verbose), true));
        }

        sendWebhookMessage(new WebhookMessage().addEmbeds(embed));
    }

    public CompletableFuture<Boolean> sendWebhookMessage(WebhookMessage message) {
        if (isDisabled()) return CompletableFuture.completedFuture(false);

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        requests.add(new PendingWebhookRequest(url, message.toJson().toString(), future));

        if (!taskStarted.getAndSet(true)) {
            // there's probably a better way to handle rate limits, but this works, so whatever.
            GrimAPI.INSTANCE.getScheduler().getAsyncScheduler().runAtFixedRate(GrimAPI.INSTANCE.getGrimPlugin(), DiscordManager::tick, 0, 1);
        }

        return future;
    }

    public boolean isDisabled() {
        return url == null;
    }

    private static void tick() {
        final PendingWebhookRequest request = requests.peek();
        if (request != null && rateLimitedUntil < System.currentTimeMillis() && !sending.getAndSet(true)) {
            try {
                HttpResult response = sendBlocking(request);

                if (response.statusCode == 429) {
                    rateLimitedUntil = Math.max(parseRateLimitUntil(response), rateLimitedUntil);
                    sending.set(false);
                    return;
                }

                requests.remove(request);
                sending.set(false);

                // TODO: handle 503 (Service Unavailable)?
                if (response.statusCode >= 400) {
                    LogUtil.error("Encountered status code " + response.statusCode + " with body " + response.body + " and headers " + response.headers + " while sending a Discord webhook alert.");
                    request.future.complete(false);
                } else {
                    request.future.complete(true);
                }
            } catch (Throwable throwable) {
                sending.set(false);
                LogUtil.error("Exception caught while sending a Discord webhook alert", throwable);
            }
        }
    }

    private static HttpResult sendBlocking(PendingWebhookRequest request) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) request.url.toURL().openConnection();
        connection.setConnectTimeout(timeoutMillis);
        connection.setReadTimeout(timeoutMillis);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        byte[] body = request.body.getBytes(StandardCharsets.UTF_8);
        connection.setFixedLengthStreamingMode(body.length);

        OutputStream output = null;
        try {
            output = connection.getOutputStream();
            output.write(body);
        } finally {
            if (output != null) output.close();
        }

        int statusCode = connection.getResponseCode();
        String responseBody = readBody(connection);
        Map<String, List<String>> headers = connection.getHeaderFields();
        connection.disconnect();
        return new HttpResult(statusCode, responseBody, headers);
    }

    private static String readBody(HttpURLConnection connection) throws IOException {
        InputStream input = connection.getErrorStream();
        if (input == null) {
            try {
                input = connection.getInputStream();
            } catch (IOException ignored) {
                return "";
            }
        }

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            input.close();
        }
    }

    private static long parseRateLimitUntil(HttpResult response) {
        long now = System.currentTimeMillis();

        String reset = firstHeader(response.headers, "X-RateLimit-Reset");
        if (reset != null) {
            try {
                return (long) (Double.parseDouble(reset) * 1000L);
            } catch (NumberFormatException ignored) {
            }
        }

        String retryAfter = firstHeader(response.headers, "Retry-After");
        if (retryAfter != null) {
            try {
                return now + (long) (Double.parseDouble(retryAfter) * 1000L);
            } catch (NumberFormatException ignored) {
            }
        }

        return now + 5000L;
    }

    private static String firstHeader(Map<String, List<String>> headers, String name) {
        if (headers == null) return null;
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                List<String> values = entry.getValue();
                return values == null || values.isEmpty() ? null : values.get(0);
            }
        }
        return null;
    }

    private static final class PendingWebhookRequest {
        private final URI url;
        private final String body;
        private final CompletableFuture<Boolean> future;

        private PendingWebhookRequest(URI url, String body, CompletableFuture<Boolean> future) {
            this.url = url;
            this.body = body;
            this.future = future;
        }
    }

    private static final class HttpResult {
        private final int statusCode;
        private final String body;
        private final Map<String, List<String>> headers;

        private HttpResult(int statusCode, String body, Map<String, List<String>> headers) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers;
        }
    }
}
