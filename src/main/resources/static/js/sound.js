/**
 * Emergency Alert Audio Synthesizer (Web Audio API)
 * Self-contained audio generation - does not require external MP3 files.
 */
class EmergencyAudio {
    constructor() {
        this.audioCtx = null;
        this.isMuted = false;
    }

    init() {
        if (!this.audioCtx) {
            const AudioContext = window.AudioContext || window.webkitAudioContext;
            if (AudioContext) {
                this.audioCtx = new AudioContext();
            }
        }
    }

    toggleMute() {
        this.isMuted = !this.isMuted;
        return this.isMuted;
    }

    /**
     * Plays emergency attention tones based on severity
     * @param {string} severity - 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'
     */
    playAlertTone(severity) {
        if (this.isMuted) return;

        try {
            this.init();
            if (!this.audioCtx) return;
            if (this.audioCtx.state === 'suspended') {
                this.audioCtx.resume();
            }

            const now = this.audioCtx.currentTime;

            if (severity === 'CRITICAL') {
                // Two-tone urgent pulse (850Hz & 1050Hz alternating)
                this.playToneSequence([
                    { freq: 880, start: 0.0, duration: 0.18, type: 'sawtooth' },
                    { freq: 660, start: 0.20, duration: 0.18, type: 'sawtooth' },
                    { freq: 880, start: 0.40, duration: 0.18, type: 'sawtooth' },
                    { freq: 660, start: 0.60, duration: 0.25, type: 'sawtooth' }
                ], 0.25);
            } else if (severity === 'HIGH') {
                // High warning chime (double pulse)
                this.playToneSequence([
                    { freq: 750, start: 0.0, duration: 0.2, type: 'sine' },
                    { freq: 1000, start: 0.25, duration: 0.3, type: 'sine' }
                ], 0.2);
            } else {
                // Gentle notification chime
                this.playToneSequence([
                    { freq: 520, start: 0.0, duration: 0.15, type: 'sine' },
                    { freq: 650, start: 0.18, duration: 0.25, type: 'sine' }
                ], 0.15);
            }
        } catch (e) {
            console.warn('Audio playback failed (user interaction required):', e);
        }
    }

    playToneSequence(tones, volume = 0.2) {
        const masterGain = this.audioCtx.createGain();
        masterGain.gain.setValueAtTime(volume, this.audioCtx.currentTime);
        masterGain.connect(this.audioCtx.destination);

        const now = this.audioCtx.currentTime;

        tones.forEach(t => {
            const osc = this.audioCtx.createOscillator();
            const gain = this.audioCtx.createGain();

            osc.type = t.type || 'sine';
            osc.frequency.setValueAtTime(t.freq, now + t.start);

            gain.gain.setValueAtTime(0, now + t.start);
            gain.gain.linearRampToValueAtTime(1, now + t.start + 0.02);
            gain.gain.exponentialRampToValueAtTime(0.001, now + t.start + t.duration);

            osc.connect(gain);
            gain.connect(masterGain);

            osc.start(now + t.start);
            osc.stop(now + t.start + t.duration);
        });
    }
}

window.emergencyAudio = new EmergencyAudio();
