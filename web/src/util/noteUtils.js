export default class NoteUtils {

    /**
     * static utility method to generate the note preview button for the note preview area.
     * @param {Object} note the note a button is being made for.
     * @returns the note preview button with attached event listener.
     */
    static createNotePreviewButton(note) {
        // Create button element, set button text to note title, and set values
        let notePreviewButton = document.createElement("button");
        notePreviewButton.className = "button";
        notePreviewButton.id = "note-preview-button";
        notePreviewButton.type = "button";
        notePreviewButton.textContent = note.title;
        notePreviewButton.noteTitle = note.title;
        notePreviewButton.noteContent = note.content;
        notePreviewButton.noteDateCreated = note.dateCreated;

        // Create listener. When note preview is clicked, update primary note with note values
        const primaryNoteTitle = document.querySelector(".primary-note-title");
        const primaryNoteContent = document.querySelector(".primary-note-content");
        const primaryNoteDateCreated = document.querySelector(".primary-note-date-created");
        notePreviewButton.addEventListener("click", function (evt) {
            // If voice note UI is being displayed, hide it
            NoteUtils.hideVoiceNoteUI();

            primaryNoteTitle.textContent = evt.target.noteTitle;
            primaryNoteContent.textContent = evt.target.noteContent;
            primaryNoteDateCreated.textContent = evt.target.noteDateCreated;
        });

        return notePreviewButton;
    }

    // Methods for showing/hiding different elements on page using CSS display field

    static hideVoiceNoteUI() {
        document.getElementById("overlay").style.display = "none";
        document.getElementById("primary-note-default").style.display = "flex";
    }

    static showVoiceNoteUI() {
        document.getElementById("primary-note-default").style.display = "none";
        document.getElementById("overlay").style.display = "block";
    }

    static swapStartWithStop() {
        document.getElementById("playback-start-recording-container").style.display = "none";
        document.getElementById("playback-stop-recording-container").style.display = "flex";
    }

    static swapStopWithTranscribing() {
        document.getElementById("playback-stop-recording-container").style.display = "none";
        document.getElementById("playback-transcribing-container").style.display = "flex";
    }

    static swapTranscribingWithStart() {
        document.getElementById("playback-transcribing-container").style.display = "none";
        document.getElementById("playback-start-recording-container").style.display = "flex";
    }
}