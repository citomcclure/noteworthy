import NoteworthyServiceClient from '../api/noteworthyServiceClient';
import BindingClass from "../util/bindingClass";
import {MediaRecorder, register} from 'extendable-media-recorder';
import {connect} from 'extendable-media-recorder-wav-encoder';
import NoteUtils from "../util/noteUtils";

/**
 * The audio recording component for the website.
 */
export default class audioRecording extends BindingClass {
    constructor(dataStore) {
        super();

        const methodsToBind = [ 'transcribeAudio' , 'showVoiceNoteUI', 'hideVoiceNoteUI'];
        this.bindClassMethods(methodsToBind, this);

        this.dataStore = dataStore;
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
            
            this.createVoiceNote(blob);

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
        this.hideVoiceNoteUI();
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
        document.getElementById("primary-note-default").style.display = "flex";
    }
}