import os

import torch
from speechbrain.inference import SpeakerRecognition
from speechbrain.inference.ASR import EncoderDecoderASR
import torchaudio

CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
TMP_DIR = os.path.join(CURRENT_DIR, 'tmpdir')
DEVICE = "cuda" if torch.cuda.is_available() else "cpu"

# Load the pre-trained speaker verification model
verification_model = SpeakerRecognition.from_hparams(
    source="speechbrain/spkrec-ecapa-voxceleb",
    # savedir="tmpdir"
    savedir=TMP_DIR,
    run_opts={"device": DEVICE}
)

# Load the pre-trained Speech To text model
asr_model = EncoderDecoderASR.from_hparams(source="speechbrain/asr-wav2vec2-commonvoice-en", savedir="pretrained_models/asr-wav2vec2-commonvoice-en",run_opts={"device": DEVICE})


def verify_voice(file1_path, file2_path):
    try:
        # Load the audio files
        signal1, fs1 = torchaudio.load(file1_path)
        signal2, fs2 = torchaudio.load(file2_path)

        # Ensure sample rates match
        if fs1 != fs2:
            print("Sample rates do not match")
            return {"error": "Sample rates do not match"}

        # Perform speaker verification
        score, match = verification_model.verify_batch(signal1, signal2)

        return match[0].item()

    except Exception as e:
        print(f"Error: {str(e)}")
        return {"error": str(e)}

def speech_to_text(file_path):
    try:
        transcription = asr_model.transcribe_file(file_path)
        return transcription.lower()

    except Exception as e:
        print(f"Error: {str(e)}")
        return {"error": str(e)}


def verify_voice_enhanced(saved_voice_path, signal2):
    try:
        signal1, fs1 = torchaudio.load(saved_voice_path)
        if fs1 != 16000:
            return False
        score, match = verification_model.verify_batch(signal1, signal2)
        return match[0].item()
    except Exception as e:
        print(f"Verification Error: {e}")
        return False


def speech_to_text_enhanced(signal):
    try:
        wav_lens = torch.tensor([signal.shape[1] / 16000])  # Assuming 16kHz
        result = asr_model.transcribe_batch(signal, wav_lens)

        return result[0][0].lower()
    except Exception as e:
        print(f"Transcription Error: {e}")
        return ""

# Gets the current working directory
# directory = os.getcwd()

# Define file paths for the two audio files
# file_path = os.path.join(directory, 'lazkani.wav')
# file_path1 = os.path.join(directory, 'lazkani.wav')
# file_path2 = os.path.join(directory, 'lazkani.wav')
#
# print(verify_voice(file_path1, file_path2))
# print(speech_to_text(file_path))


