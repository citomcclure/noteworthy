package com.nashss.se.noteworthy.lambda;

import com.nashss.se.noteworthy.activity.requests.TranscribeAudioRequest;
import com.nashss.se.noteworthy.activity.results.TranscribeAudioResult;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TranscribeAudioLambda
        extends LambdaActivityRunner<TranscribeAudioRequest, TranscribeAudioResult>
        implements RequestHandler<AuthenticatedLambdaRequest<TranscribeAudioRequest>, LambdaResponse> {

    private final Logger log = LogManager.getLogger();

    @Override
    public LambdaResponse handleRequest(AuthenticatedLambdaRequest<TranscribeAudioRequest> input, Context context) {
        log.info("handleRequest");
        return super.runActivity(
            () -> {
                byte[] decodedMedia = input.fromBase64EncodedBodyAndParse();
                return input.fromUserClaims(claims ->
                        TranscribeAudioRequest.builder()
                                .withAudio(decodedMedia)
                                .withEmail(claims.get("email"))
                                .build());
            },
            (request, serviceComponent) ->
                    serviceComponent.provideTranscribeAudioActivity().handleRequest(request)
        );
    }
}


