import joblib

# Load the saved model and vectorizer
model = joblib.load("nlp_command_model.pkl")
vectorizer = joblib.load("nlp_vectorizer.pkl")


# Function to predict the command based on text input
def predict_command(text):
    # Vectorize the input text using the same vectorizer
    text_vec = vectorizer.transform([text])

    # Predict the command using the trained model
    prediction = model.predict(text_vec)

    # Return the predicted command
    return prediction[0]


# Test the model with some sample inputs
test_commands = [
    "turn on the lights",
    "switch off the lights",
    "turn lights on",
    "switch the lights off",
    "switch the fucking lights on mate",
]

for command in test_commands:
    predicted_command = predict_command(command)
    print(f"Input: {command} -> Predicted Command: {predicted_command}")
