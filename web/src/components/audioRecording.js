import NoteworthyServiceClient from '../api/noteworthyServiceClient';
import BindingClass from "../util/bindingClass";
import {MediaRecorder, register} from 'extendable-media-recorder';
import {connect} from 'extendable-media-recorder-wav-encoder';
import NoteworthyUtils from "../util/noteworthyUtils";

let firstTime = true;

/**
 * The audio recording component for the website.
 */
export default class audioRecording extends BindingClass {
    constructor(dataStore) {
        super();

        const methodsToBind = ['transcribeAudio'];
        this.bindClassMethods(methodsToBind, this);

        this.dataStore = dataStore;
        this.client = new NoteworthyServiceClient();
        this.connect();

        document.getElementById('new-voice-note-start').addEventListener('click', this.transcribeAudio);
        document.getElementById('playback-start-recording-container').addEventListener('click', NoteworthyUtils.swapStartWithStop);
        document.getElementById('playback-stop-recording-container').addEventListener('click', NoteworthyUtils.swapStopWithTranscribing);
    }

    /**
     * Must be called to register additional dependency for creating media with audio/wav MIME type
     */
    async connect() {
        await register(await connect());
    }

    /**
     * Attaches event listeners to our start and stop recording playback buttons. On click, these start
     * and stop the recording. The media recorder makes 'dataavailable' after stop, which is added to our
     * chunks array. This is passed to createVoiceNote as 'blob'.
     */
    async transcribeAudio() {
        // Remove onboarding UI and show primary note container
        NoteworthyUtils.hideOnboarding();

        // Show voice note playback UI, with start recording button shown
        NoteworthyUtils.showVoiceNoteUI();
        debugger;
        // Only executed once in order to use the same stream and media player for multiple voice notes in one
        // session. Otherwise, we will start to stack event listeners and create duplicate media related instances.
        if (firstTime) {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true })

            // Add listeners for starting and stopping audio
            let start = document.getElementById('playback-start-recording-container');
            let stop = document.getElementById('playback-stop-recording-container');
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

                this.createVoiceNote(blob);
            }

            firstTime = false;
        }
    }

    /**
     * Passes audio blob to client to send request. After retrieving new voice note, switch to
     * primary note view and paint with new voice note values. Add the note to the datastore and
     * hide voice note playback UI.
     * @param {Array} blob The valid WAV audio blob.
     */
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

        // Add new note preview to preview area
        const newVoiceNotePreviewButton = NoteworthyUtils.createNotePreviewButton(newVoiceNote);
        const notePreviews = document.querySelector(".note-previews-container");
        notePreviews.prepend(newVoiceNotePreviewButton);

        // Display Sort By button in case this was first note
        document.getElementById('note-sort-and-search').style.display = "block";

        // Add new voice note in datastore
        const notes = await this.dataStore.get('notes');
        notes.unshift(newVoiceNote);
        this.dataStore.set('notes', notes);

        // Hide voice note UI and show start recording UI for next voice note
        NoteworthyUtils.hideVoiceNoteUI();
        NoteworthyUtils.swapTranscribingWithStart();
    }
}