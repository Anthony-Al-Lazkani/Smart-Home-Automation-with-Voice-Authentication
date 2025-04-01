import os
import subprocess
import ffmpeg

CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
# TEMP_DIR = 'temp_dir'

TEMP_DIR = os.path.join(CURRENT_DIR, 'temp_dir')
# Ensure the temp directory exists
os.makedirs(TEMP_DIR, exist_ok=True)

SAMPLE_RATE = 16000

def resample_audio(audio_path, output_path, sample_rate=SAMPLE_RATE):
    """
    Resamples an audio file to a specified sample rate using ffmpeg.

    :param audio_path: Path to the original audio file
    :param output_path: Path to save the resampled audio file
    :param sample_rate: Desired sample rate (default is 16kHz)
    """
    try:
        subprocess.run([
            'ffmpeg', '-y', '-i', audio_path, '-ar', str(sample_rate), output_path
        ], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        print(f"Resampled audio saved to {output_path}")
    except subprocess.CalledProcessError as e:
        print(f"Error resampling audio: {e.stderr.decode()}")


def convert_to_wav(input_path, output_path):
    """
    Converts an audio file to WAV format using ffmpeg.

    :param input_path: Path to the original audio file (could be AAC, MP3, etc.)
    :param output_path: Path to save the converted WAV file
    """
    try:
        subprocess.run([
            'ffmpeg', '-y', '-i', input_path, '-acodec', 'pcm_s16le', '-ar', '16000', '-ac', '1', output_path
        ], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        print(f"Converted {input_path} to {output_path}")
        return output_path
    except subprocess.CalledProcessError as e:
        print(f"Error converting audio to WAV: {e.stderr.decode()}")
        return None


def delete_temp_files():
    """
    Deletes all audio files from the temp_dir.
    """
    try:
        for filename in os.listdir(TEMP_DIR):
            file_path = os.path.join(TEMP_DIR, filename)
            if os.path.isfile(file_path):
                os.remove(file_path)
                print(f"Deleted: {file_path}")

        print("All temporary files deleted.")

    except Exception as e:
        print(f"Error deleting temp files: {e}")


def read_audio_file(file_path):
    with open(file_path, 'rb') as file:
        return file.read()

