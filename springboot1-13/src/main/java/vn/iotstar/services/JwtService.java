package vn.iotstar.services;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class JwtService {

	@Value("${security.jwt.secret-key}")
	private String secretKey;

	@Value("${security.jwt.expiration-time}")
	private long expirationTime;

	private byte[] getSecretBytes() {
		return HexFormat.of().parseHex(secretKey);
	}

	public String generateToken(UserDetails userDetails) {

		try {
			Instant now = Instant.now();
			Instant expiration = now.plusMillis(expirationTime);

			JWTClaimsSet claims = new JWTClaimsSet.Builder().subject(userDetails.getUsername())
					.issueTime(Date.from(now)).expirationTime(Date.from(expiration)).build();

			JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256).type(com.nimbusds.jose.JOSEObjectType.JWT)
					.build();

			SignedJWT signedJWT = new SignedJWT(header, claims);

			signedJWT.sign(new MACSigner(getSecretBytes()));

			return signedJWT.serialize();

		} catch (JOSEException e) {
			throw new RuntimeException("Cannot generate JWT token", e);
		}
	}

	public String extractUsername(String token) {

		try {
			SignedJWT signedJWT = SignedJWT.parse(token);

			return signedJWT.getJWTClaimsSet().getSubject();

		} catch (Exception e) {
			throw new RuntimeException("Invalid JWT token", e);
		}
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {

		try {
			SignedJWT signedJWT = SignedJWT.parse(token);

			// Kiểm tra thuật toán
			if (!JWSAlgorithm.HS256.equals(signedJWT.getHeader().getAlgorithm())) {
				return false;
			}

			// Kiểm tra chữ ký
			boolean signatureValid = signedJWT.verify(new MACVerifier(getSecretBytes()));

			if (!signatureValid) {
				return false;
			}

			JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

			// Kiểm tra username
			String username = claims.getSubject();

			if (!userDetails.getUsername().equals(username)) {
				return false;
			}

			// Kiểm tra expiration
			Date expiration = claims.getExpirationTime();

			if (expiration == null) {
				return false;
			}

			return expiration.after(new Date());

		} catch (Exception e) {
			return false;
		}
	}

	public long getExpirationTime() {
		return expirationTime;
	}
}