/**
 * Autosave logic for primary note view. When title or content area receives input, autosave
 * will occur after 2 seconds of input timeout by calling ViewNotes.updateNote()
 */
export default class Autosave {
constructor(viewNotes) {
        // resets if 2 seconds without user input is not reached
        function debounce(func, timeout = 2000){
            let timer;
            return (...args) => {
              clearTimeout(timer);
              timer = setTimeout(() => { func.apply(this, args); }, timeout);
            };
        }

        function saveInput() {
            viewNotes.updateNote();
        }

        const processChanges = debounce(() => saveInput());

        document.getElementById('primary-note-title').addEventListener('input', processChanges);
        document.getElementById('primary-note-content').addEventListener('input', processChanges);
    }
}