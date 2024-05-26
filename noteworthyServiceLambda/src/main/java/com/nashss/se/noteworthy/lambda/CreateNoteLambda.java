package com.nashss.se.noteworthy.lambda;

import com.nashss.se.noteworthy.activity.requests.CreateNoteRequest;
import com.nashss.se.noteworthy.activity.results.CreateNoteResult;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CreateNoteLambda
        extends LambdaActivityRunner<CreateNoteRequest, CreateNoteResult>
        implements RequestHandler<AuthenticatedLambdaRequest<CreateNoteRequest>, LambdaResponse> {

    private final Logger log = LogManager.getLogger();

    @Override
    public LambdaResponse handleRequest(AuthenticatedLambdaRequest<CreateNoteRequest> input, Context context) {
        log.info("handleRequest");
        return super.runActivity(
            () -> {
                CreateNoteRequest unauthenticatedRequest = input.fromBody(CreateNoteRequest.class);
                return input.fromUserClaims(claims ->
                        CreateNoteRequest.builder()
                                .withTitle(unauthenticatedRequest.getTitle())
                                .withContent(unauthenticatedRequest.getContent())
                                .withEmail(claims.get("email"))
                                .build());
            },
            (request, serviceComponent) ->
                    serviceComponent.provideCreateNoteActivity().handleRequest(request)
        );
    }
}
