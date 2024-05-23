package com.nashss.se.noteworthy.lambda;

import com.nashss.se.noteworthy.activity.requests.GetNotesRequest;
import com.nashss.se.noteworthy.activity.results.GetNotesResult;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetNotesLambda
        extends LambdaActivityRunner<GetNotesRequest, GetNotesResult>
        implements RequestHandler<AuthenticatedLambdaRequest<GetNotesRequest>, LambdaResponse> {

    private final Logger log = LogManager.getLogger();

    @Override
    public LambdaResponse handleRequest(AuthenticatedLambdaRequest<GetNotesRequest> input, Context context) {
        log.info("handleRequest");
        return super.runActivity(
            () -> {
                return input.fromUserClaims(claims ->
                        GetNotesRequest.builder()
                                .withEmail(claims.get("email"))
                                .build());
            },
            (request, serviceComponent) ->
                    serviceComponent.provideGetNotesActivity().handleRequest(request)
        );
    }
}
