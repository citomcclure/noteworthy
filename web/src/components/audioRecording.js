import NoteworthyServiceClient from '../api/noteworthyServiceClient';
import BindingClass from "../util/bindingClass";
import {MediaRecorder, register} from 'extendable-media-recorder';
import {connect} from 'extendable-media-recorder-wav-encoder';

/**
 * The audio recording component for the website.
 */
export default class audioRecording extends BindingClass {
    constructor() {
        super();

        const methodsToBind = [ 'transcribeAudio' , 'showVoiceNoteUI', 'hideVoiceNoteUI'];
        this.bindClassMethods(methodsToBind, this);

        this.client = new NoteworthyServiceClient();

        document.getElementById('new-voice-note-start').addEventListener('click', this.showVoiceNoteUI);
    }

    async transcribeAudio() {
        await register(await connect());
        
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
            
        // Add listeners for starting and stopping audio
        let start = document.getElementById('start-recording');
        let stop = document.getElementById('stop-recording');
        let mediaRecorder = new MediaRecorder(stream, {
            mimeType: 'audio/wav'
        });
        let chunks = [];
        
        start.addEventListener('click', (ev)=>{
            mediaRecorder.start();
            console.log(mediaRecorder.state);
        });

        stop.addEventListener('click', (ev)=>{
            mediaRecorder.stop();
            console.log(mediaRecorder.state);
        });

        mediaRecorder.ondataavailable = function(ev) {
            chunks.push(ev.data);
        }

        mediaRecorder.onstop = (ev)=>{
            const mimeType = mediaRecorder.mimeType;
            let blob = new Blob(chunks, { type: mimeType });
            chunks = [];

            console.log(blob);
            
            this.client.transcribeAudio(blob);
        }
    }

    showVoiceNoteUI() {
        // document.getElementById("new-voice-note-start").removeEventListener('click', this.showVoiceNoteUI);
        // document.getElementById('new-voice-note-start').addEventListener('click', this.hideVoiceNoteUI);
        document.getElementById("primary-note-default").style.display = "none";
        document.getElementById("overlay").style.display = "block";
    }
      
    hideVoiceNoteUI() {
        // document.getElementById("new-voice-note-start").removeEventListener('click', this.hideVoiceNoteUI);
        // document.getElementById('new-voice-note-start').addEventListener('click', this.showVoiceNoteUI);
        document.getElementById("overlay").style.display = "none";
        document.getElementById("primary-note-default").style.display = "block";
    }
}