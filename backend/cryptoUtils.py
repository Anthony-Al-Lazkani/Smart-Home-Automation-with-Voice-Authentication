import os
from cryptography.fernet import Fernet
from dotenv import load_dotenv

load_dotenv()  # Load from .env

ENCRYPTION_KEY = os.getenv("ENCRYPTION_KEY")
fernet = Fernet(ENCRYPTION_KEY.encode())

def encrypt_file(input_path, output_path):
    with open(input_path, "rb") as f:
        data = f.read()
    encrypted_data = fernet.encrypt(data)
    with open(output_path, "wb") as f:
        f.write(encrypted_data)

def decrypt_file(input_path, output_path):
    with open(input_path, "rb") as f:
        encrypted_data = f.read()
    decrypted_data = fernet.decrypt(encrypted_data)
    with open(output_path, "wb") as f:
        f.write(decrypted_data)
