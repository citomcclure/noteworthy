import NoteworthyServiceClient from '../api/noteworthyServiceClient';
import BindingClass from "../util/bindingClass";
import {MediaRecorder, register} from 'extendable-media-recorder';
import {connect} from 'extendable-media-recorder-wav-encoder';

let mediaRecorder = null;
let audioBlobs = [];
let capturedStream = null;

/**
 * The audio recording component for the website.
 */
export default class audioRecording extends BindingClass {
    constructor() {
        super();

        const methodsToBind = [
            'startRecording', 'stopRecording'
        ];
        this.bindClassMethods(methodsToBind, this);

        this.client = new NoteworthyServiceClient();
    }

    // Register the extendable-media-recorder-wav-encoder
    async connect() {
        await register(await connect());
    }

    // Starts recording audio
    startRecording() {
        return navigator.mediaDevices.getUserMedia({
            audio: {
            echoCancellation: true,
            }
        }).then(stream => {
            audioBlobs = [];
            capturedStream = stream;

            // Use the extended MediaRecorder library
            mediaRecorder = new MediaRecorder(stream, {
                mimeType: 'audio/wav'
            });

            // Add audio blobs while recording 
            mediaRecorder.addEventListener('dataavailable', event => {
                audioBlobs.push(event.data);
            });

            mediaRecorder.start();
        }).catch((e) => {
            console.error(e);
        });
    }

    // Stops recording audio
    stopRecording() {
        return new Promise(resolve => {
          if (!mediaRecorder) {
            resolve(null);
            return;
          }
      
          mediaRecorder.addEventListener('stop', () => {
            const mimeType = mediaRecorder.mimeType;
            const audioBlob = new Blob(audioBlobs, { type: mimeType });
      
            if (capturedStream) {
              capturedStream.getTracks().forEach(track => track.stop());
            }
      
            resolve(audioBlob);
          });
          
          mediaRecorder.stop();
          
        });
      }
}