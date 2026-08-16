package org.sophie.common;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Encode/decode for the opaque keyset cursors used by every paged RPC. The cursor is a URL-safe base64
 * of the row's sort-key parts joined by a unit separator — e.g. {@code (created_at, id)} for chat, a
 * single {@code task_number} for tasks, a {@code key} for permissions. Clients treat it as a black box;
 * only the owning service encodes/decodes it. Keeping this in sophie-protos means every service uses the
 * exact same wire format, so a cursor is at least debuggable/portable.
 *
 * <p>Not a security boundary — it is not signed or encrypted, just opaque. Callers must validate the
 * decoded parts (and tolerate a malformed/foreign cursor by treating it as "no cursor" or rejecting the
 * request, per the RPC).
 */
public final class PageCursor {

    // ASCII unit separator (U+001F) — never appears in the ids/timestamps/keys we encode.
    private static final String SEP = String.valueOf((char) 0x1F);
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private PageCursor() {}

    /** Encode the ordered sort-key parts into an opaque cursor. */
    public static String encode(String... parts) {
        String joined = String.join(SEP, parts);
        return ENCODER.encodeToString(joined.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decode a cursor back into its parts. Returns an empty array for a null/blank cursor (= first page).
     *
     * @throws IllegalArgumentException if the cursor is non-blank but not valid base64 (a malformed or
     *     tampered cursor) — callers map this to INVALID_ARGUMENT.
     */
    public static String[] decode(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return new String[0];
        }
        byte[] raw = DECODER.decode(cursor); // throws IllegalArgumentException on bad base64
        String joined = new String(raw, StandardCharsets.UTF_8);
        return joined.split(SEP, -1);
    }
}
