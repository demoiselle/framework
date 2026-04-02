/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.ws.rs.core.Response;

/**
 * Represents an RFC 9457 Problem Details response.
 *
 * <p>Standard fields: {@code type}, {@code title}, {@code status},
 * {@code detail} and {@code instance}. Additional extension fields
 * are stored in a {@link LinkedHashMap} to preserve insertion order
 * and serialized at the JSON root level via Jackson annotations.</p>
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9457">RFC 9457</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetail {

    private static final Set<String> RESERVED_KEYS =
            Set.of("type", "title", "status", "detail", "instance");

    private String type = "about:blank";
    private String title;
    private int status;
    private String detail;
    private String instance;
    private final Map<String, Object> extensions = new LinkedHashMap<>();

    // ── Getters & Setters ──────────────────────────────────────────

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    /**
     * Sets the HTTP status code. Must be between 100 and 599 (inclusive).
     *
     * @param status HTTP status code
     * @throws IllegalArgumentException if {@code status} is outside 100-599
     */
    public void setStatus(int status) {
        if (status != 0 && (status < 100 || status > 599)) {
            throw new IllegalArgumentException(
                    "HTTP status must be between 100 and 599, got: " + status);
        }
        this.status = status;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    // ── RFC 9457 §4.2 helpers ──────────────────────────────────────

    /**
     * Returns the standard HTTP reason phrase for the given status code.
     *
     * @param status HTTP status code
     * @return the reason phrase, or {@code "Unknown Status"} if not recognized
     */
    public static String reasonPhrase(int status) {
        Response.Status s = Response.Status.fromStatusCode(status);
        return s != null ? s.getReasonPhrase() : "Unknown Status";
    }

    /**
     * When {@code type} is {@code "about:blank"} and {@code title} is
     * {@code null}, fills {@code title} with the standard HTTP reason
     * phrase for the current {@code status} (RFC 9457 §4.2).
     */
    public void applyAboutBlankDefaults() {
        if ("about:blank".equals(this.type) && this.title == null && this.status != 0) {
            this.title = reasonPhrase(this.status);
        }
    }

    // ── Extensions ─────────────────────────────────────────────────

    /**
     * Returns the extension fields. Serialized at the JSON root level.
     */
    @JsonAnyGetter
    public Map<String, Object> getExtensions() {
        return extensions;
    }

    /**
     * Adds or replaces an extension field during JSON deserialization.
     *
     * @param key   extension key (must not clash with standard fields)
     * @param value extension value
     * @throws IllegalArgumentException if {@code key} is a reserved field name
     */
    @JsonAnySetter
    public void setExtension(String key, Object value) {
        if (RESERVED_KEYS.contains(key)) {
            throw new IllegalArgumentException(
                    "Extension key must not be a standard field name: " + key);
        }
        extensions.put(key, value);
    }

    /**
     * Builder-style method to add an extension field.
     *
     * @param key   extension key
     * @param value extension value
     * @return this instance for chaining
     */
    public ProblemDetail extension(String key, Object value) {
        setExtension(key, value);
        return this;
    }

    // ── equals / hashCode / toString ───────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProblemDetail that = (ProblemDetail) o;
        return status == that.status
                && Objects.equals(type, that.type)
                && Objects.equals(title, that.title)
                && Objects.equals(detail, that.detail)
                && Objects.equals(instance, that.instance)
                && Objects.equals(extensions, that.extensions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, title, status, detail, instance, extensions);
    }

    @Override
    public String toString() {
        return "ProblemDetail{" +
                "type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", detail='" + detail + '\'' +
                ", instance='" + instance + '\'' +
                ", extensions=" + extensions +
                '}';
    }
}
