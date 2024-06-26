export default class NoteworthyUtils {

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
            NoteworthyUtils.hideVoiceNoteUI();

            primaryNoteTitle.textContent = evt.target.noteTitle;
            primaryNoteContent.textContent = evt.target.noteContent;
            primaryNoteDateCreated.textContent = evt.target.noteDateCreated;
        });

        return notePreviewButton;
    }

    // Methods for hiding/showing elements in audioRecording.js
    static hideVoiceNoteUI() {
        document.getElementById("primary-note-overlay").style.display = "none";
        document.getElementById("primary-note-container").style.display = "flex";
    }

    static showVoiceNoteUI() {
        document.getElementById("primary-note-container").style.display = "none";
        document.getElementById("primary-note-overlay").style.display = "block";
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

    // Methods for hiding/showing elements in header.js
    static createButton(text, clickHandler) {
        const button = document.createElement('a');
        button.classList.add('button');
        button.href = '#';
        button.innerText = text;

        button.addEventListener('click', async () => {
            await clickHandler();
        });

        return button;
    }

    static addAppOverlay() {
        document.getElementById('app-container').style.display = "none";
        document.getElementById('app-overlay').style.display = "flex";
    }

    static removeAppOverlay() {
        document.getElementById('app-overlay').style.display = "none";
        document.getElementById('app-container').style.display = "block";
    }

    // Methods for onboarding user in viewNotes.js
    static showOnboarding() {
        // Hides all elements just in case (besides new note buttons)
        document.getElementById('note-sort-and-search').style.display = "none";
        document.getElementById('primary-note-container').style.display = "none";
        document.getElementById('primary-note-overlay').style.display = "none";
        document.getElementById('onboard-user').style.display = "flex";
    }

    static hideOnboarding() {
        // Note: showing Sort By button is handled by calling method
        document.getElementById('onboard-user').style.display = "none";
        document.getElementById('primary-note-container').style.display = "flex";
    }
}