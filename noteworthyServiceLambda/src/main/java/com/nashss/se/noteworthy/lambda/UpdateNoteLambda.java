package com.nashss.se.noteworthy.lambda;

import com.nashss.se.noteworthy.activity.requests.UpdateNoteRequest;
import com.nashss.se.noteworthy.activity.results.UpdateNoteResult;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UpdateNoteLambda
        extends LambdaActivityRunner<UpdateNoteRequest, UpdateNoteResult>
        implements RequestHandler<AuthenticatedLambdaRequest<UpdateNoteRequest>, LambdaResponse> {

    private final Logger log = LogManager.getLogger();

    @Override
    public LambdaResponse handleRequest(AuthenticatedLambdaRequest<UpdateNoteRequest> input, Context context) {
        log.info("handleRequest");
        return super.runActivity(
                () -> {
                    UpdateNoteRequest unauthenticatedRequest = input.fromBody(UpdateNoteRequest.class);
                    return input.fromUserClaims(claims ->
                            UpdateNoteRequest.builder()
                                    .withNoteId(unauthenticatedRequest.getNoteId())
                                    .withTitle(unauthenticatedRequest.getTitle())
                                    .withContent(unauthenticatedRequest.getContent())
                                    .withEmail(claims.get("email"))
                                    .build());
                },
                (request, serviceComponent) ->
                        serviceComponent.provideUpdateNoteActivity().handleRequest(request)
        );
    }
}

