// This file was generated by Mendix Modeler.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package jwt.actions;

import java.io.UnsupportedEncodingException;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.DataValidationRuntimeException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.webui.CustomJavaAction;
import jwt.helpers.AlgorithmParser;
import jwt.helpers.RSAKeyPairReader;
import jwt.proxies.constants.Constants;
import com.mendix.systemwideinterfaces.core.IMendixObject;

/**
 * Decodes a JWT string into a plain text JSON for the header and payload. This enables the user to implement a specific JSON mapping that decodes the header or payload. Throws an exception when the token could not be decoded or verified.
 */
public class DecodeVerifyJWTPlainText extends CustomJavaAction<IMendixObject>
{
	private java.lang.String token;
	private java.lang.String secret;
	private jwt.proxies.ENU_Algorithm algorithm;
	private IMendixObject __publicKey;
	private jwt.proxies.JWTRSAPublicKey publicKey;

	public DecodeVerifyJWTPlainText(IContext context, java.lang.String token, java.lang.String secret, java.lang.String algorithm, IMendixObject publicKey)
	{
		super(context);
		this.token = token;
		this.secret = secret;
		this.algorithm = algorithm == null ? null : jwt.proxies.ENU_Algorithm.valueOf(algorithm);
		this.__publicKey = publicKey;
	}

	@Override
	public IMendixObject executeAction() throws Exception
	{
		this.publicKey = __publicKey == null ? null : jwt.proxies.JWTRSAPublicKey.initialize(getContext(), __publicKey);

		// BEGIN USER CODE
		ILogNode logger = Core.getLogger(Constants.getLOGNODE());
		
		if (this.token == null || this.token.equals("")) {
			logger.error("Cannot decode an empty token.");
			throw new DataValidationRuntimeException("Cannot decode an empty token.");
		}
		
		if (this.algorithm == null) {
			logger.error("Cannot decode token using an empty algorithm.");
			throw new DataValidationRuntimeException("Cannot decode token using an empty algorithm.");
		}
		
		RSAPublicKey rsaPublicKey = null;
		
		if(publicKey != null) {
			RSAKeyPairReader rsaKeyPairReader = new RSAKeyPairReader();
			rsaPublicKey = rsaKeyPairReader.getPublicKey(this.context(), publicKey);
		}
		
		DecodedJWT jwt = null;
		
		try {
			Algorithm alg = new AlgorithmParser().parseAlgorithm(algorithm, secret, rsaPublicKey, null);
			logger.debug("Starting to decode JWT token with algorithm " + alg.getName() + ".");
			
			Verification verification = JWT.require(alg);
			
			JWTVerifier verifier = verification.build();
			jwt = verifier.verify(token);
			
			logger.debug("Verifying token successfull.");
		} catch (UnsupportedEncodingException exception){
		    logger.error("Token encoding unsupported.", exception);
		    throw exception;
		} catch (JWTVerificationException exception){
			logger.info("Verification of token signature/claims failed: " + exception.getMessage());
			throw exception;
		}
		
		String header = new String(Base64.getDecoder().decode(jwt.getHeader()));
		String payload = new String(Base64.getDecoder().decode(jwt.getPayload()));
		
		IMendixObject jwtPlainText = Core.instantiate(this.context(), "JWT.JWTPlainText");
		jwtPlainText.setValue(this.context(), "Header", header);
		jwtPlainText.setValue(this.context(), "Payload", payload);
		
		return jwtPlainText;
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@Override
	public java.lang.String toString()
	{
		return "DecodeVerifyJWTPlainText";
	}

	// BEGIN EXTRA CODE
	// END EXTRA CODE
}
