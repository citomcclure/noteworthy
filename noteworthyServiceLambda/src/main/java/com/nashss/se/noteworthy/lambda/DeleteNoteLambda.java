package com.nashss.se.noteworthy.lambda;

import com.nashss.se.noteworthy.activity.requests.DeleteNoteRequest;
import com.nashss.se.noteworthy.activity.results.DeleteNoteResult;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeleteNoteLambda
        extends LambdaActivityRunner<DeleteNoteRequest, DeleteNoteResult>
        implements RequestHandler<AuthenticatedLambdaRequest<DeleteNoteRequest>, LambdaResponse> {

    private final Logger log = LogManager.getLogger();

    @Override
    public LambdaResponse handleRequest(AuthenticatedLambdaRequest<DeleteNoteRequest> input, Context context) {
        log.info("handleRequest");
        return super.runActivity(
            () -> {
                DeleteNoteRequest unauthenticatedRequest = input.fromBody(DeleteNoteRequest.class);
                return input.fromUserClaims(claims ->
                        DeleteNoteRequest.builder()
                                .withDateCreated(unauthenticatedRequest.getDateCreated())
                                .withEmail(claims.get("email"))
                                .build());
            },
            (request, serviceComponent) ->
                    serviceComponent.provideDeleteNoteActivity().handleRequest(request)
        );
    }
}

