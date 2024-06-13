import NoteworthyServiceClient from '../api/noteworthyServiceClient';
import BindingClass from "../util/bindingClass";
import {MediaRecorder, register} from 'extendable-media-recorder';
import {connect} from 'extendable-media-recorder-wav-encoder';
import NoteUtils from "../util/noteUtils";

let firstTime = true;

/**
 * The audio recording component for the website.
 */
export default class audioRecording extends BindingClass {
    constructor(dataStore) {
        super();

        const methodsToBind = [ 'transcribeAudio'];
        this.bindClassMethods(methodsToBind, this);

        this.dataStore = dataStore;
        this.client = new NoteworthyServiceClient();
        this.connect();

        document.getElementById('new-voice-note-start').addEventListener('click', this.transcribeAudio);
    }

    async connect() {
        await register(await connect());
    }

    async transcribeAudio() {
        NoteUtils.showVoiceNoteUI();

        // Only executed once in order to use the same stream and media player for multiple voice notes in one 
        // session. Otherwise, we will start to stack event listeners and create duplicate media related instances.
        if (firstTime) {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
            
            // Add listeners for starting and stopping audio
            let start = document.getElementById('start-recording');
            let stop = document.getElementById('stop-recording');
            let mediaRecorder = new MediaRecorder(stream, {
                mimeType: 'audio/wav'
            });
            // streamAndMediaPlayerStarted = true;
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
                // Remove data for next use
                chunks = [];
                
                this.createVoiceNote(blob);
                
                // Stop getUserMedia stream. Currently causes issues with successive voice notes
                // stream.getTracks().forEach( track => track.stop());
    
                firstTime = false;
        }

        }
        

    }

    async createVoiceNote(blob) {
        // Create new voice note in database with wav blob
        const newVoiceNote = await this.client.transcribeAudio(blob);
        
        // Set primary note view to new voice note values
        const primaryNoteTitle = document.querySelector(".primary-note-title");
        const primaryNoteContent = document.querySelector(".primary-note-content");
        const primaryNoteDateCreated = document.querySelector(".primary-note-date-created");
        primaryNoteTitle.textContent = newVoiceNote.title;
        primaryNoteContent.textContent = newVoiceNote.content;
        primaryNoteDateCreated.textContent = newVoiceNote.dateCreated;
    
        // add new note preview to preview area
        const newVoiceNotePreviewButton = NoteUtils.createNotePreviewButton(newVoiceNote);
        const notePreviews = document.querySelector(".note-previews-container");
        notePreviews.prepend(newVoiceNotePreviewButton);

        // Add new voice note in datastore
        const notes = await this.dataStore.get('notes');
        notes.unshift(newVoiceNote);
        this.dataStore.set('notes', notes);

        // Hide voice note UI
        NoteUtils.hideVoiceNoteUI();
    }
}