/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.hashcash.execution;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.apache.commons.lang3.time.DateParser;
import org.apache.commons.lang3.time.FastDateFormat;
import org.demoiselle.jee.security.hashcash.DemoiselleSecurityHashCashConfig;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

/**
 *
 * @author SERPRO
 */
@RequestScoped
public class Generator {

    @Inject
    private DemoiselleSecurityHashCashConfig config;

    private static Key key;

    @PostConstruct
    public void init() {
        if (key == null) {
            try {
                key = new HmacKey(config.getHashcashKey().getBytes("UTF-8"));
            } //throw new DemoiselleSecurityException(bundle.general(), Response.Status.UNAUTHORIZED.getStatusCode(), ex);
            catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Generator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String token() {
        long time = (org.jose4j.jwt.NumericDate.now().getValueInMillis() + (config.getTimetoLiveMilliseconds()));
        try {
            JwtClaims claims = new JwtClaims();
            claims.setExpirationTime(org.jose4j.jwt.NumericDate.fromMilliseconds(time));
            claims.setGeneratedJwtId();
            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(key);
            jws.setKeyIdHeaderValue("demoiselle-security-hashcash");
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
            return jws.getCompactSerialization();
        } catch (JoseException ex) {
            Logger.getLogger(Generator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setVerificationKey(key)
                    .setRelaxVerificationKeyValidation()
                    .build();
            if (!jwtConsumer.process(token).getJwtClaims().getJwtId().isEmpty()) {
                return true;
            }
        } catch (InvalidJwtException | MalformedClaimException ex) {
            Logger.getLogger(Generator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private static final FastDateFormat[] FORMATS = {
        FastDateFormat.getInstance("yyMMdd", TimeZone.getTimeZone("GMT")),
        FastDateFormat.getInstance("yyMMddHHmmss", TimeZone.getTimeZone("GMT")),
        FastDateFormat.getInstance("yyMMddHHmm", TimeZone.getTimeZone("GMT"))
    };

    public boolean validateHashCash(String cash) throws NoSuchAlgorithmException {

        String[] parts = cash.split(":");

        if ((parts.length != 6) && (parts.length != 7)) {
            throw new IllegalArgumentException("Improperly formed HashCash");
        }

        int version = Integer.parseInt(parts[0]);
        if (version < 0 || version > 1) {
            throw new IllegalArgumentException("The version is not supported");
        }

        if ((version == 0 && parts.length != 6)
                || (version == 1 && parts.length != 7)) {
            throw new IllegalArgumentException("Improperly formed HashCash");
        }

        int index = 1;
        int claimedBits = (version == 1) ? Integer.parseInt(parts[index++]) : 0;
        Date date = parseDate(parts[index++]);
        if (date == null) {
            throw new IllegalArgumentException("Improperly formed Date");
        }
        String resource = parts[index++];
        Map<String, List<String>> extensions = deserializeExtensions(parts[index++]);

        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.update(cash.getBytes());
        int computedBits = numberOfLeadingZeros(md.digest());
        return computedBits >= 20;
    }

    private static Date parseDate(String dateString) {
        if (dateString != null) {
            try {
                // try each date format starting with the most common one
                for (DateParser format : FORMATS) {
                    try {
                        return format.parse(dateString);
                    } catch (ParseException ex) {
                        /* gulp */ }
                }
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static Map<String, List<String>> deserializeExtensions(String extensions) {
        Map<String, List<String>> result = new ConcurrentHashMap<>();
        if (null == extensions || extensions.length() == 0) {
            return result;
        }

        String[] items = extensions.split(";");

        for (String item : items) {
            String[] parts = item.split("=", 2);
            if (parts.length == 1) {
                result.put(parts[0], null);
            } else {
                result.put(parts[0], Arrays.asList(parts[1].split(",")));
            }
        }

        return result;
    }

    private static int numberOfLeadingZeros(byte[] values) {
        int result = 0;
        int temp = 0;
        for (int i = 0; i < values.length; i++) {

            temp = numberOfLeadingZeros(values[i]);

            result += temp;
            if (temp != 8) {
                break;
            }
        }

        return result;
    }

    private static int numberOfLeadingZeros(byte value) {
        if (value < 0) {
            return 0;
        }
        if (value < 1) {
            return 8;
        } else if (value < 2) {
            return 7;
        } else if (value < 4) {
            return 6;
        } else if (value < 8) {
            return 5;
        } else if (value < 16) {
            return 4;
        } else if (value < 32) {
            return 3;
        } else if (value < 64) {
            return 2;
        } else if (value < 128) {
            return 1;
        } else {
            return 0;
        }
    }

}
