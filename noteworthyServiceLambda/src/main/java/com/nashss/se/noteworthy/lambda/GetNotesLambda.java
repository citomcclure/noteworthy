package com.nashss.se.noteworthy.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class GetNotesLambda implements RequestHandler<LambdaRequest<String>, LambdaResponse> {
    /**
     * Handles a Lambda Function request.
     *
     * @param input   The Lambda Function input
     * @param context The Lambda execution environment context object.
     * @return The Lambda Function output
     */
    @Override
    public LambdaResponse handleRequest(LambdaRequest<String> input, Context context) {
        return null;
    }
}
