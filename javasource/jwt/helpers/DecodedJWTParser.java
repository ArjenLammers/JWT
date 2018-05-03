package jwt.helpers;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;

import jwt.proxies.Audience;
import jwt.proxies.JWT;
import jwt.proxies.PublicClaimResponse;

public class DecodedJWTParser {
	
	public JWT parse(IContext context, ILogNode logger, DecodedJWT decodedJWT) {
		logger.debug("Started parsing of decoded JWT.");
		
		JWT jwt = new JWT(context);
		
		List<String> audiences = decodedJWT.getAudience();
		
		if (audiences != null) {
			for(String audienceString : audiences) {
				Audience audience = new Audience(context);
				logger.debug("Adding audience " + audienceString + ".");
				audience.setaud(audienceString);
				audience.setAudience_JWT(jwt);
			}
		}
		
		logger.debug("Setting other registered claims.");
		jwt.setiss(decodedJWT.getIssuer());
		jwt.setnbf(decodedJWT.getNotBefore());
		jwt.setsub(decodedJWT.getSubject());
		jwt.setiat(decodedJWT.getIssuedAt());
		jwt.setexp(decodedJWT.getExpiresAt());
		jwt.setjti(decodedJWT.getId());
		
		Map<String, Claim> claimMap = decodedJWT.getClaims();
		Set<Entry<String, Claim>> claimEntrySet = claimMap.entrySet();
		Iterator<Entry<String,Claim>> claimIterator = claimEntrySet.iterator();
		
		while (claimIterator.hasNext()) {
			Entry<String,Claim> claimEntry = claimIterator.next();
			String value = claimEntry.getKey();
			Claim claim = claimEntry.getValue();
			
			RegisteredClaimIdentifier registeredClaimIdentifier = new RegisteredClaimIdentifier();
			
			// Skip registered claims. These are included in the JWT object and associated audience objects.
			if (registeredClaimIdentifier.identify(value)) {
				logger.debug("Skip parsing claim: " + value + ", because registered claim is already included in JWT object.");
				continue;
			}
			
			PublicClaimResponse publicClaimResponse = new PublicClaimResponse(context);
			publicClaimResponse.setClaim_JWT(jwt);
			publicClaimResponse.setClaim(value);
			
			if (claim.asString() != null) {
				logger.debug("Parse claim " + value + " as String claim.");
				publicClaimResponse.setValueString(claim.asString());
			}
			
			if (claim.asBoolean() != null) {
				logger.debug("Parse claim " + value + " as Boolean claim.");
				publicClaimResponse.setValueBoolean(claim.asBoolean());
			}
			
			if (claim.asInt() != null) {
				logger.debug("Parse claim " + value + " as Integer claim.");
				publicClaimResponse.setValueInteger(claim.asInt());
			}
			
			if (claim.asLong() != null) {
				logger.debug("Parse claim " + value + " as Long claim.");
				publicClaimResponse.setValueLong(claim.asLong());
			}
			
			if (claim.asDouble() != null) {
				logger.debug("Parse claim " + value + " as Decimal claim.");
				publicClaimResponse.setValueDecimal(BigDecimal.valueOf(claim.asDouble()));
			}
			
			if (claim.asDate() != null) {
				logger.debug("Parse claim " + value + " as Date claim.");
				publicClaimResponse.setValueDateTime(claim.asDate());
			}
				
			// Claim has a format that is not yet supported, e.g. an array.
			logger.warn("Could not parse Claim " + value + " while decoding token. Format is not supported.");
			
		}
		
		logger.debug("Parsing token completed.");
		
		return jwt;
	}

}
